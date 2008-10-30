/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.architect.swingui.dbtree.DnDTreePathTransferable;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.swingui.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.swingui.action.LogAction;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSession implements WabitSession, SwingWorkerRegistry {
	
	private static Logger logger = Logger.getLogger(WabitSwingSession.class);
    
	private final WabitSessionContext sessionContext;
	
    private SQLQueryUIComponents queryUIComponents;
	private JTree projectTree;
	private JFrame frame;
	private JCheckBox groupingCheckBox;

	/**
	 * Stores the parts of the query.
	 */
	private QueryCache queryCache;

	/**
	 * The list of all currently-registered background tasks.
	 */
	private final List<SPSwingWorker> activeWorkers =
		Collections.synchronizedList(new ArrayList<SPSwingWorker>());

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSession(WabitSessionContext context) {
		sessionContext = context;
		sessionContext.registerChildSession(this);
		queryPen = new QueryPen(this);
		queryCache = new QueryCache(queryPen);
	}
	
	/**
	 *  Builds the GUI
	 * @throws ArchitectException 
	 */
    public void buildUI() throws ArchitectException {
        frame = new JFrame("Power*Wabit");
        
        // this will be the frame's content pane
		JPanel cp = new JPanel(new BorderLayout());

    	queryUIComponents = new SQLQueryUIComponents(this, sessionContext.getDataSources(), cp);
    	queryUIComponents.enableMultipleQueries(false);
    	queryUIComponents.addTableChangeListener(new TableChangeListener() {
			public void tableRemoved(TableChangeEvent e) {
				// Do Nothing
			}
		
			public void tableAdded(TableChangeEvent e) {
				TableModelSortDecorator sortDecorator = null;
				JTable table = e.getChangedTable();
				if (table instanceof FancyExportableJTable) {
					FancyExportableJTable fancyTable = (FancyExportableJTable)table;
					sortDecorator = fancyTable.getTableModelSortDecorator();
				}
				ComponentCellRenderer renderer = new ComponentCellRenderer(table, sortDecorator);
				table.getTableHeader().setDefaultRenderer(renderer);
				queryCache.listenToCellRenderer(renderer);
			}
		});
    	
		JPanel queryToolPanel = new JPanel(new BorderLayout());
		JToolBar queryToolBar = new JToolBar();
		JButton executeButton = queryUIComponents.getExecuteButton();
		queryToolBar.add(executeButton);
		queryToolBar.add(queryUIComponents.getStopButton());
		queryToolBar.add(queryUIComponents.getClearButton());
		queryToolBar.add(queryUIComponents.getUndoButton());
		queryToolBar.add(queryUIComponents.getRedoButton());
		
		queryToolPanel.add(queryToolBar, BorderLayout.NORTH);
		queryToolPanel.add(new RTextScrollPane(300,200, queryUIComponents.getQueryArea(), true),BorderLayout.CENTER);
    	
    	JSplitPane wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	JSplitPane rightViewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	JPanel resultPanel = queryUIComponents.getFirstResultPanel();
        	
    	queryPenAndTextTabPane = new JTabbedPane();
    	queryPen.addQueryListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				StringBuffer query = new StringBuffer(queryCache.generateQuery());
				queryUIComponents.executeQuery(query.toString());
			}
		});
    	queryCache.addQueryChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				queryUIComponents.executeQuery(queryCache.generateQuery());
			}
		});
    	JPanel playPen = queryPen.createQueryPen(this);
    	DefaultFormBuilder queryExecuteBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 10dlu, pref"));
    	queryExecuteBuilder.append("", new JButton(new AbstractAction("Execute Query") {
			public void actionPerformed(ActionEvent e) {
				queryUIComponents.executeQuery(queryCache.generateQuery());
			}
		}));
    	
    	JPanel queryPenPanel = new JPanel(new BorderLayout());
    	queryPenPanel.add(playPen, BorderLayout.CENTER);
    	queryPenPanel.add(queryExecuteBuilder.getPanel(), BorderLayout.SOUTH);
    	queryPenAndTextTabPane.add(queryPenPanel,"PlayPen");
    	queryPenAndTextTabPane.add(queryToolPanel,"Query");
    	queryPenAndTextTabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
			}
		});
    	
    	groupingCheckBox = new JCheckBox("Grouping");
    	groupingCheckBox.addActionListener(new AbstractAction() {

    		public void actionPerformed(ActionEvent e) {
    			addGroupingTableHeaders();
    		}
    	});
    	queryUIComponents.getFirstResultPanel().addContainerListener(new ContainerListener() {
			public void componentRemoved(ContainerEvent e) {
				//Do nothing.
			}
			public void componentAdded(ContainerEvent e) {
				addGroupingTableHeaders();
			}
		});
    	FormLayout layout = new FormLayout("pref, 3dlu, pref:grow, 5dlu, pref");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append(new JLabel("Database connection:"));
    	southPanelBuilder.append(queryUIComponents.getDatabaseComboBox());
    	southPanelBuilder.append(new JLabel("                               "));
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append("Where:", queryPen.getGlobalWhereText(), 3);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(groupingCheckBox);
    	southPanelBuilder.append(new JLabel(""));
    	southPanelBuilder.append(queryUIComponents.getFilterAndLabelPanel());
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(resultPanel, 5);
    	
    	rightViewPane.add(queryPenAndTextTabPane, JSplitPane.TOP);
    	rightViewPane.add(southPanelBuilder.getPanel(), JSplitPane.BOTTOM);  	
    	
    	rootNode = new SQLObjectRoot();
        for (SPDataSource ds : sessionContext.getDataSources().getConnections()) {
            rootNode.addChild(new SQLDatabase(ds));
        }
    	final DBTreeModel treeModel = new DBTreeModel(rootNode);
		projectTree = new JTree(treeModel);
		projectTree.addMouseListener(new PopUpMenuListener());
    	projectTree.setCellRenderer(new DBTreeCellRenderer());
    	DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(projectTree, DnDConstants.ACTION_COPY, new DragGestureListener() {
			
			public void dragGestureRecognized(DragGestureEvent dge) {
				ArrayList<int[]> list = new ArrayList<int[]>();
				for (TreePath path : projectTree.getSelectionPaths()) {
					Object selectedNode = path.getLastPathComponent();
					if (!(selectedNode instanceof SQLObject)) {
						throw new IllegalStateException("DBTrees are not allowed to contain non SQLObjects. This tree contains a " + selectedNode.getClass());
					}
					int[] dndPathToNode = DnDTreePathTransferable.getDnDPathToNode((SQLObject)selectedNode, rootNode);
					list.add(dndPathToNode);
				}
					
				Object firstSelectedObject = projectTree.getSelectionPath().getLastPathComponent();
				String name;
				if (firstSelectedObject instanceof SQLObject) {
					name = ((SQLObject) firstSelectedObject).getName();
				} else {
					name = firstSelectedObject.toString();
				}
				
				Transferable dndTransferable = new DnDTreePathTransferable(list, name);
				dge.getDragSource().startDrag(dge, null, dndTransferable, new DragSourceListener() {
					public void dropActionChanged(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragOver(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragExit(DragSourceEvent dse) {
						//do nothing
					}
					public void dragEnter(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragDropEnd(DragSourceDropEvent dsde) {
						//do nothing
					}
				});
			}
		});

        wabitPane.add(new JScrollPane(projectTree), JSplitPane.LEFT);
        wabitPane.add(rightViewPane, JSplitPane.RIGHT);
        
        //the current status label will be replaced by the Session Messages once it is implemented
        JPanel statusPane = new JPanel(new BorderLayout());
        statusPane.add(new JLabel("Status Message here"), BorderLayout.CENTER);
		
		MemoryMonitor memoryMonitor = new MemoryMonitor();
		memoryMonitor.start();
		JLabel memoryLabel = memoryMonitor.getLabel();
		memoryLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
		statusPane.add(memoryLabel, BorderLayout.EAST);
		
		cp.add(wabitPane, BorderLayout.CENTER);
        cp.add(statusPane, BorderLayout.SOUTH);
        
        JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
        
		JMenu windowMenu = new JMenu("Window");
		fileMenu.setMnemonic('w');
		menuBar.add(windowMenu);
		JTextArea logTextArea = queryUIComponents.getLogTextArea();
		JMenuItem logMenuItem = new JMenuItem(new LogAction(frame, logTextArea ));
		windowMenu.add(logMenuItem);
		
		frame.setJMenuBar(menuBar);
        frame.setContentPane(cp);
        frame.setSize(800, 500);
        frame.setLocation(400, 300);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				close();
			}});
    }
    
    public JTree getTree() {
    	return projectTree;
    }

    /* docs inherited from interface */
	public void registerSwingWorker(SPSwingWorker worker) {
		activeWorkers.add(worker);
	}

    /* docs inherited from interface */
	public void removeSwingWorker(SPSwingWorker worker) {
		activeWorkers.remove(worker);
	}

	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();

	private SQLObjectRoot rootNode;

	private QueryPen queryPen;

	/**
	 * This is the tabbed pane that contains the query pen and text editor.
	 * All the query editing UI should be in this tabbed pane.
	 */
	private JTabbedPane queryPenAndTextTabPane;
	
	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.add(l);
	}

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.remove(l);
	}

	/**
	 * Ends this session, disposing its frame and releasing any system
	 * resources that were obtained explicitly by this session. Also
	 * fires a sessionClosing lifecycle event, so any resources used up
	 * by subsystems dependent on this session can be freed by the appropriate
	 * parties.
	 */
    public void close() {
    	SessionLifecycleEvent<WabitSession> e =
    		new SessionLifecycleEvent<WabitSession>(this);
    	for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
    		lifecycleListeners.get(i).sessionClosing(e);
    	}
    	frame.dispose();
    	sessionContext.deregisterChildSession(this);
    }
    
    /**
     * Launches the Wabit application by loading the configuration and
     * displaying the GUI.
     * 
     * @throws Exception if startup fails
     */
    public static void  main(String[] args) throws Exception {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wabit");
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
    	WabitSessionContext context = new WabitSessionContext(true);
        WabitSwingSession wss = new WabitSwingSession(context);
        wss.buildUI();
    }

	public SQLObjectRoot getRootNode() {
		return rootNode;
	}
	
	/**
	 * A PopUpMenuListener which is current used for the ProjectTree.
	 * It will Display a List of options once you right click on the ProjectTree.
	 *
	 */
	private class PopUpMenuListener extends MouseAdapter {

		JPopupMenu menu;
		DatabaseConnectionManager dbConnectionManager;

		PopUpMenuListener() {
			menu = new JPopupMenu();
			dbConnectionManager = new DatabaseConnectionManager(sessionContext.getDataSources());
			menu.add(new AbstractAction("Database ConnectionManager..."){

				public void actionPerformed(ActionEvent e) {
					 dbConnectionManager.showDialog(frame);
				}});

		}

		public void mouseClicked(MouseEvent e) {
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				menu.show(e.getComponent(), e.getX(), e.getY());
			} else {
				menu.setVisible(false);
			}

		}
	}
	
	/**
	 * This will add a {@link ComponentCellRenderer} to the table headers
	 * to allow grouping when the grouping checkbox is checked. This will
	 * need to be called each time the tables are recreated.
	 */
	private void addGroupingTableHeaders() {
		//XXX The group by and having clauses should be allowed
		// to be shown on both the query pen and text editor tabs
		// however we currently can't update the query cache from 
		// the text side so we won't be able to use these components
		// from the text side and they will cause errors as they won't
		// be able to synchronize with the new queries being run.
		if (queryPenAndTextTabPane.getSelectedIndex() == 0) {
			ArrayList<JTable> tables = queryUIComponents.getResultTables();
			for(JTable t : tables)	{
				ComponentCellRenderer renderPanel = (ComponentCellRenderer)t.getTableHeader().getDefaultRenderer();
				if(groupingCheckBox.isSelected()) {
					renderPanel.setGroupingEnabled(true);
					logger.debug("Grouping Enabled");
				} else {
					renderPanel.setGroupingEnabled(false);		
				}
				for (int i = 0; i < renderPanel.getComboBoxes().size(); i++) {
					SQLGroupFunction groupByAggregate = queryCache.getGroupByAggregate(queryCache.getSelectedColumns().get(i));
					if (groupByAggregate != null) {
						renderPanel.getComboBoxes().get(i).setSelectedItem(groupByAggregate.toString());
					}
				}

				for (int i = 0; i < renderPanel.getTextFields().size(); i++) {
					String havingText = queryCache.getHavingClause(queryCache.getSelectedColumns().get(i));
					if (havingText != null) {
						renderPanel.getTextFields().get(i).setText(havingText);
					}
				}
				
				// The combo box count should be the same as the column count.
				for (int i = 0; i < renderPanel.getComboBoxes().size(); i++) {
					OrderByArgument arg = queryCache.getOrderByArgument(queryCache.getSelectedColumns().get(i));
					if (arg != null) {
						if (arg == OrderByArgument.ASC) {
							renderPanel.setSortingStatus(i, TableModelSortDecorator.ASCENDING);
						} else if (arg == OrderByArgument.DESC) {
							renderPanel.setSortingStatus(i, TableModelSortDecorator.DESCENDING);
						}
					}
				}
				
			}
			queryCache.setGroupingEnabled(groupingCheckBox.isSelected());
		}
	}
}
