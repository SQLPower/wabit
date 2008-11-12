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
import java.awt.GridLayout;
import java.awt.Toolkit;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
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
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.StringItem;
import ca.sqlpower.wabit.query.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.swingui.action.LogAction;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSessionImpl implements WabitSwingSession {
	
	private static Logger logger = Logger.getLogger(WabitSwingSessionImpl.class);
	
	private static final String SQL_TEXT_TAB_HEADING = "SQL";
    
	private final String QUERY_EXECUTE = "Execute";
	private final WabitSessionContext sessionContext;
	
    private SQLQueryUIComponents queryUIComponents;
	private JTree projectTree;
	private JFrame frame;
	private JCheckBox groupingCheckBox;
	private static JLabel statusLabel;
	private final JLabel groupingLabel = new JLabel("Group Function");
	private final JLabel havingLabel = new JLabel ("Having");
	private final JLabel columnNameLabel = new JLabel ();
	private JPanel cornerPanel;

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
	 * This stores a copy of the query cache for each query that is executed
	 * through this session. This way we can get at parts of the query for the
	 * tables that result from executing these queries. If a query is found to
	 * be used part way through this list the queries before it will be removed
	 * as the tables that represent the query should have been removed prior to
	 * the new tables being added to the result set.
	 */
	private final List<QueryCache> queuedQueryCache;

	private QueryController queryController;

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSessionImpl(WabitSessionContext context) {
		sessionContext = context;
		sessionContext.registerChildSession(this);
		queryPen = new QueryPen(this);
		queryCache = new QueryCache();
		
		queryController = new QueryController(queryCache, queryPen);
		
		statusLabel= new JLabel();
		queuedQueryCache = new ArrayList<QueryCache>();
	}
	/**
	 * sets the StatusMessage
	 */
	public static void setStatusMessage (String msg) {
		statusLabel.setText(msg);	
	}
	
	/**
	 * This is a listModel that just returns the row Number for the rowHeaderRender
	 */
	private class RowListModel extends AbstractListModel{
		int tableRowSize;
		public RowListModel(JTable table) {
			tableRowSize = table.getRowCount();
		}
		public Object getElementAt(int index) {
			return index+1;
		}
		public int getSize() {
			return tableRowSize;
		}
		
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
				logger.debug("Table added.");
				queryController.unlistenToCellRenderer();
				TableModelSortDecorator sortDecorator = null;
				JTable table = e.getChangedTable();
				if (table instanceof FancyExportableJTable) {
					FancyExportableJTable fancyTable = (FancyExportableJTable)table;
					sortDecorator = fancyTable.getTableModelSortDecorator();
				}
				ComponentCellRenderer renderer = new ComponentCellRenderer(table, sortDecorator);
				table.getTableHeader().setDefaultRenderer(renderer);
				
				ListModel lm = new RowListModel(table);
				JList rowHeader = new JList(lm);
				rowHeader.setFixedCellWidth(groupingLabel.getPreferredSize().width + 2);
				rowHeader.setCellRenderer(new RowHeaderRenderer(table));
				
				((JScrollPane)table.getParent().getParent()).setRowHeaderView(rowHeader);
				
				GridLayout layout = new GridLayout(0,1);
				cornerPanel = new JPanel(layout);
				if(queryPenAndTextTabPane.getSelectedIndex() == 0) {
					groupingLabel.setFont(table.getTableHeader().getFont());
					havingLabel.setFont(table.getTableHeader().getFont());
					havingLabel.setVerticalAlignment(JLabel.BOTTOM);
					cornerPanel.add(groupingLabel);
					cornerPanel.add(havingLabel);
				}
				cornerPanel.add(columnNameLabel);
				((JScrollPane)table.getParent().getParent()).setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerPanel);
				addGroupingTableHeaders();
				queryController.listenToCellRenderer(renderer);
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
    	queryCache.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				executeQueryInCache();
			}
		});
    	JPanel playPen = queryPen.createQueryPen();
    	DefaultFormBuilder queryExecuteBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 10dlu, pref"));
    	AbstractAction queryExecuteAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				executeQueryInCache();
			}
		};
    	JButton playPenExecuteButton = new JButton(queryExecuteAction);
    	ImageIcon executeIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/execute.png"));
    	playPenExecuteButton.setIcon(executeIcon);
    	playPenExecuteButton.setToolTipText(QUERY_EXECUTE + "(Shortcut "+ queryPen.getAcceleratorKeyString()+ " R)");
    	queryPen.getQueryPenBar().add(playPenExecuteButton);
    	queryPen.getQueryPenCavas().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                , QUERY_EXECUTE);
    	queryPen.getQueryPenCavas().getActionMap().put(QUERY_EXECUTE, queryExecuteAction);
    	
    	JPanel queryPenPanel = new JPanel(new BorderLayout());
    	queryPenPanel.add(playPen, BorderLayout.CENTER);
    	queryPenPanel.add(queryExecuteBuilder.getPanel(), BorderLayout.SOUTH);
    	queryPenAndTextTabPane.add(queryPenPanel,"PlayPen");
    	queryPenAndTextTabPane.add(queryToolPanel,SQL_TEXT_TAB_HEADING);
    	queryPenAndTextTabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
			}
		});
    	
    	groupingCheckBox = new JCheckBox("Grouping");
    	groupingCheckBox.addActionListener(new AbstractAction() {

    		public void actionPerformed(ActionEvent e) {
    			queryCache.setGroupingEnabled(groupingCheckBox.isSelected());
    			if (groupingCheckBox.isSelected()) {
    				for (Item item :queryCache.getSelectedColumns()) {
    					if (item instanceof StringItem) {
    						queryCache.setGrouping(item, SQLGroupFunction.COUNT.toString());
    					}
    				}
    			}
    			executeQueryInCache();
    		}
    	});
    	FormLayout layout = new FormLayout("pref, 3dlu, pref:grow, 5dlu, max(pref;80dlu)"
    			,"pref, pref,  pref, fill:min(pref;100dlu):grow");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append(new JLabel("Database connection:"));
    	southPanelBuilder.append(queryUIComponents.getDatabaseComboBox());
    	JSpinner rowLimitSpinner = queryUIComponents.getRowLimitSpinner();
    	rowLimitSpinner.setValue(new Integer(1000));
    	southPanelBuilder.append(rowLimitSpinner);
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
				
				if(projectTree.getSelectionPaths() == null) {
					return;
				}
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
        
        JPanel statusPane = new JPanel(new BorderLayout());
        statusPane.add(statusLabel, BorderLayout.CENTER);
		
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
    	WabitSessionContext context = new WabitSessionContextImpl(true);
        WabitSwingSessionImpl wss = new WabitSwingSessionImpl(context);
        wss.buildUI();
    }

	public SQLObjectRoot getRootNode() {
		return rootNode;
	}
	
	public WabitSessionContext getContext() {
		return sessionContext;
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
	 * 
	 * @param initialDisplay If true this header will be displayed with default values for the
	 * headers. If false it will display the header with only what is defined in the QueryCache.
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
				QueryCache cache = null;
				List<QueryCache> removeCacheList = new ArrayList<QueryCache>();
				for (QueryCache c : queuedQueryCache) {
					if (c.generateQuery().equals(queryUIComponents.getQueryForJTable(t))) {
						cache = c;
						break;
					}
					removeCacheList.add(c);
				}
				for (QueryCache c : removeCacheList) {
					queuedQueryCache.remove(c);
				}
				if (cache == null) {
					// There are no QueryCache objects that define the header for
					// this table so we cannot add a header.
					logger.debug("There was no cache matching the table from query " + queryUIComponents.getQueryForJTable(t));
					return;
				}
				ComponentCellRenderer renderPanel = (ComponentCellRenderer)t.getTableHeader().getDefaultRenderer();
				if(groupingCheckBox.isSelected()) {
					renderPanel.setGroupingEnabled(true);
					logger.debug("Grouping Enabled");
					groupingLabel.setVisible(true);
					havingLabel.setVisible(true);
				} else {
					renderPanel.setGroupingEnabled(false);
					groupingLabel.setVisible(false);
					havingLabel.setVisible(false);
				}
				for (int i = 0; i < renderPanel.getComboBoxes().size(); i++) {
					SQLGroupFunction groupByAggregate = cache.getGroupByAggregate(cache.getSelectedColumns().get(i));
					if (groupByAggregate != null) {
						renderPanel.getComboBoxes().get(i).setSelectedItem(groupByAggregate.toString());
					}
				}

				for (int i = 0; i < renderPanel.getTextFields().size(); i++) {
					String havingText = cache.getHavingClause(cache.getSelectedColumns().get(i));
					if (havingText != null) {
						renderPanel.getTextFields().get(i).setText(havingText);
					}
				}
				
				LinkedHashMap<Integer, Integer> columnSortMap = new LinkedHashMap<Integer, Integer>();
				for (Item column : cache.getOrderByList()) {
					int columnIndex = cache.getSelectedColumns().indexOf(column);
					OrderByArgument arg = cache.getOrderByArgument(column);
					if (arg != null) {
						if (arg == OrderByArgument.ASC) {
							columnSortMap.put(columnIndex, TableModelSortDecorator.ASCENDING);
						} else if (arg == OrderByArgument.DESC) {
							columnSortMap.put(columnIndex, TableModelSortDecorator.DESCENDING);
						} else {
							logger.debug("Order by argument for column " + columnIndex + " is " + arg.toString() + " but was not set for an unknown reason.");
						}
					}
				}
				renderPanel.setSortingStatus(columnSortMap);
				
			}
		}
	}
	
	/**
	 * This will execute the current query in the QueryCache and
	 * store a copy of the QueryCache in the queued list.
	 */
	private synchronized void executeQueryInCache() {
		queuedQueryCache.add(new QueryCache(queryCache));
		queryUIComponents.executeQuery(queryCache.generateQuery());
		
	}
}
