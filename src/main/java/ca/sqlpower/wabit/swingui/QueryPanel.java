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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.architect.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.swingui.action.CreateLayoutFromQueryAction;
import ca.sqlpower.wabit.swingui.action.ExportQueryAction;
import ca.sqlpower.wabit.swingui.action.ExportSQLScriptAction;
import ca.sqlpower.wabit.swingui.action.ForumAction;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class QueryPanel implements DataEntryPanel {
	
	private static final Logger logger = Logger.getLogger(QueryPanel.class);
	
	private static final String SQL_TEXT_TAB_HEADING = "SQL";
	
	private static final ImageIcon THROBBER = new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/throbber.gif"));
	
	private static final ImageIcon ICON = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/swingui/query/search.png"));
	
	/**
	 * The background colour given to the JTables when they are being updated. This will
	 * give the users a more noticeable change when there is an update occurring.
	 */
	private static final Color REFRESH_GREY = new Color(0xeeeeee);
    
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
	
	private SQLQueryUIComponents queryUIComponents;
	private JCheckBox groupingCheckBox;
	private final JLabel groupingLabel = new JLabel("Group Function");
	private final JLabel havingLabel = new JLabel ("Having");
	private final JLabel columnNameLabel = new JLabel ();
	private QueryPen queryPen;
	
	/**
	 * This is the panel in the top left of the results table. It will
	 * give row headers for the group by and having fields.
	 */
	private final JPanel cornerPanel;
	
	/**
	 * Stores the parts of the query.
	 */
	private QueryCache queryCache;
	
	/**
	 * This stores a copy of the query cache for each query that is executed
	 * through this session. This way we can get at parts of the query for the
	 * tables that result from executing these queries. If a query is found to
	 * be used part way through this list the queries before it will be removed
	 * as the tables that represent the query should have been removed prior to
	 * the new tables being added to the result set.
	 */
	private final List<QueryCache> queuedQueryCache;
	
	/**
	 * This is the tabbed pane that contains the query pen and text editor.
	 * All the query editing UI should be in this tabbed pane.
	 */
	private JTabbedPane queryPenAndTextTabPane;

	private final QueryController queryController;

	private final WabitSwingSession session;
	
	private JTree dragTree;
	private JComboBox reportComboBox;

	/**
	 * This is the main JComponent for this query. All other components
	 * are placed in this.
	 */
	private final JSplitPane mainSplitPane;
	
	/**
	 * This is the TopRight SplitPane of wabbit that divides the QueryTabbedPen and the dragTree
	 * with comboBox
	 */
	private final JSplitPane rightTopPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * This is the root of the JTree on the right of the query builder. This
	 * will let the user drag and drop components into the query.
	 */
	private SQLObjectRoot rootNode;

	/**
	 * This will listen to any change in the query cache and update the results table as needed.
	 */
	private final PropertyChangeListener queryCacheListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			executeQueryInCache();
		}
	};

	/**
	 * This is the panel that holds the QueryPen and the GUI SQL select in the tabbed pane.
	 */
	private JPanel queryPenPanel;

	/**
	 * This is the panel that holds the text editor for the query.
	 */
	private JPanel queryToolPanel;

	/**
	 * The field that will search for a given string across all result sets simultaneously.
	 */
	private JTextField searchField;
	
	public QueryPanel(WabitSwingSession session, QueryCache cache) {
		logger.debug("Constructing new query panel.");
		this.session = session;
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryCache = cache;
		queryPen = new QueryPen(session, this, queryCache);
		queryPen.getGlobalWhereText().setText(cache.getGlobalWhereClause());
		queryUIComponents = new SQLQueryUIComponents(session, session.getProject(), mainSplitPane);
		queryUIComponents.setShowSearchOnResults(false);
		queryController = new QueryController(queryCache, queryPen, queryUIComponents.getDatabaseComboBox(), queryUIComponents.getQueryArea(), queryPen.getZoomSlider());
		queryPen.setZoom(queryCache.getZoomLevel());
		queuedQueryCache = new ArrayList<QueryCache>();
		reportComboBox = queryUIComponents.getDatabaseComboBox();
		
		cornerPanel = new JPanel();
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref", "pref, pref, pref"), cornerPanel);
		groupingLabel.setFont(new JTableHeader().getFont());
		
		//Resize grouping and having labels to the height of a combo box to be spaced properly
		//beside the headers in the results table. This is done by a listener as the components
		//aren't realized until they are displayed.
		reportComboBox.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				groupingLabel.setPreferredSize(new Dimension((int) groupingLabel.getPreferredSize().getWidth(), reportComboBox.getHeight()));
				havingLabel.setPreferredSize(new Dimension((int) havingLabel.getPreferredSize().getWidth(), reportComboBox.getHeight()));
			}
		});
		havingLabel.setFont(new JTableHeader().getFont());
		builder.append(groupingLabel);
		builder.append(havingLabel);
		builder.append(columnNameLabel);
		
		dragTree = new JTree();
		rootNode = new SQLObjectRoot();
		reportComboBox.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				try {
					for (int i = rootNode.getChildren().size() - 1; i >= 0; i--) {
						rootNode.removeChild(i);
					}
					if(reportComboBox.getSelectedItem() != null) {
						rootNode.addChild(new SQLDatabase(
								(SPDataSource) reportComboBox.getSelectedItem()));
						DBTreeModel tempTreeModel = new DBTreeModel(rootNode);
						dragTree.setModel(tempTreeModel);
						dragTree.setVisible(true);
					} 
				} catch (ArchitectException e) {
					throw new RuntimeException(
							"Could not add DataSource to rootNode", e);
				}

			}
		});
		if (session.getProject().getDataSources().size() != 0 ) {
			if(queryCache.getDataSource() == null) {
				dragTree.setVisible(false);
			} else {
			reportComboBox.setSelectedItem(queryCache.getDataSource());
			dragTree.setVisible(true);
			}
		} else {
			dragTree.setVisible(false);
		}
		dragTree.setCellRenderer(new DBTreeCellRenderer());
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(dragTree, DnDConstants.ACTION_COPY, new DragGestureListener() {
			
			public void dragGestureRecognized(DragGestureEvent dge) {
				
				if(dragTree.getSelectionPaths() == null) {
					return;
				}
				ArrayList<SQLObject> list = new ArrayList<SQLObject>();
				for (TreePath path : dragTree.getSelectionPaths()) {
					Object selectedNode = path.getLastPathComponent();
					if (!(selectedNode instanceof SQLObject)) {
						throw new IllegalStateException("DBTrees are not allowed to contain non SQLObjects. This tree contains a " + selectedNode.getClass());
					}
					list.add((SQLObject) selectedNode);
				}
					
				Object firstSelectedObject = dragTree.getSelectionPath().getLastPathComponent();
				String name;
				if (firstSelectedObject instanceof SQLObject) {
					name = ((SQLObject) firstSelectedObject).getName();
				} else {
					name = firstSelectedObject.toString();
				}
				
				Transferable dndTransferable = new SQLObjectSelection(list);
				dge.getDragSource().startDrag(dge, null, dndTransferable, new DragSourceAdapter() {});
			}
		});

		
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
				
				((JScrollPane)table.getParent().getParent()).setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerPanel);
				addGroupingTableHeaders();
				queryController.listenToCellRenderer(renderer);
				
				columnNameLabel.setIcon(null);
				
				searchField.setDocument(queryUIComponents.getSearchDocument());
			}
		});
    	
		buildUI();

	}

	
	private void buildUI() {
		JTabbedPane resultPane = queryUIComponents.getResultTabPane();

		queryToolPanel = new JPanel(new BorderLayout());
		JToolBar queryToolBar = new JToolBar();
		queryToolBar.setFloatable(false);
		JButton executeButton = queryUIComponents.getExecuteButton();
		ImageIcon executeIcon = new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/wabit_execute.png"));
		executeButton.setIcon(executeIcon);
		executeButton.setToolTipText(executeButton.getText());
		executeButton.setText("");
		queryToolBar.add(executeButton);
		queryUIComponents.getStopButton().setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/stop.png")));
		queryUIComponents.getStopButton().setToolTipText(queryUIComponents.getStopButton().getText());
		queryUIComponents.getStopButton().setText("");
		queryToolBar.add(queryUIComponents.getStopButton());
		queryToolBar.addSeparator();
		queryUIComponents.getClearButton().setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/page_white.png")));
		queryUIComponents.getClearButton().setToolTipText(queryUIComponents.getClearButton().getText());
		queryUIComponents.getClearButton().setText("");
		queryToolBar.add(queryUIComponents.getClearButton());
		queryUIComponents.getUndoButton().setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/undo_arrow16.png")));
		queryUIComponents.getUndoButton().setToolTipText(queryUIComponents.getUndoButton().getText());
		queryUIComponents.getUndoButton().setText("");
		queryToolBar.add(queryUIComponents.getUndoButton());
		queryUIComponents.getRedoButton().setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/redo_arrow16.png")));
		queryUIComponents.getRedoButton().setToolTipText(queryUIComponents.getRedoButton().getText());
		queryUIComponents.getRedoButton().setText("");
		queryToolBar.add(queryUIComponents.getRedoButton());
		queryToolBar.addSeparator();
		JButton exportQuery = new JButton(new ExportQueryAction(session, queryCache));
		exportQuery.setToolTipText("Export query to Wabit file.");
		queryToolBar.add(exportQuery);
		JButton exportSQL = new JButton(new ExportSQLScriptAction(session, queryCache));
		exportSQL.setToolTipText("Export query to SQL script.");
		queryToolBar.add(exportSQL);
		queryToolBar.addSeparator();
		queryToolBar.add(new CreateLayoutFromQueryAction(session, session.getProject(), queryCache));
		
		JToolBar wabitBar = new JToolBar();
		wabitBar.setFloatable(false);
		JButton forumButton = new JButton(new ForumAction());
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		wabitBar.add(forumButton);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BorderLayout());
		toolBar.add(queryToolBar, BorderLayout.CENTER);
		toolBar.add(wabitBar, BorderLayout.EAST);
		
		queryToolPanel.add(toolBar, BorderLayout.NORTH);
		queryToolPanel.add(new RTextScrollPane(300,200, queryUIComponents.getQueryArea(), true),BorderLayout.CENTER);
    	
    	queryPenAndTextTabPane = new JTabbedPane();
		queryCache.addPropertyChangeListener(queryCacheListener);
    	JPanel playPen = queryPen.createQueryPen();
    	DefaultFormBuilder queryExecuteBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 10dlu, pref"));

    	queryPenPanel = new JPanel(new BorderLayout());
    	queryPenPanel.add(playPen, BorderLayout.CENTER);
    	queryPenPanel.add(queryExecuteBuilder.getPanel(), BorderLayout.SOUTH);
    	queryPenAndTextTabPane.add(queryPenPanel,"PlayPen");
    	queryPenAndTextTabPane.add(queryToolPanel,SQL_TEXT_TAB_HEADING);
    	if (queryCache.isScriptModified()) {
    		queryPenAndTextTabPane.setSelectedComponent(queryToolPanel);
    		queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
    	}
    	queryPenAndTextTabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (queryPenPanel == queryPenAndTextTabPane.getSelectedComponent()) {
					//This is temporary until we can parse the user string and set the query cache to look like 
					//the query the user modified.
					int retval = JOptionPane.OK_OPTION;
					if (queryCache.isScriptModified()) {
						retval = JOptionPane.showConfirmDialog(getPanel(), "Changes will be lost to the SQL script if you go back to the PlayPen. \nDo you wish to continue?", "Changing", JOptionPane.YES_NO_OPTION);
					}
					
					if (retval != JOptionPane.OK_OPTION) {
						queryPenAndTextTabPane.setSelectedComponent(queryToolPanel);
					} else {
						
						//The RTextArea needs to have its text set to the empty string
						//or else it will set its text to the empty string before changing
						//its text when we come back to the query side and it will get
						//the query in a state where it thinks the user changed the query twice.
						queryUIComponents.getQueryArea().setText("");
						
						queryCache.removeUserModifications();
					}
					
				} else if (queryToolPanel == queryPenAndTextTabPane.getSelectedComponent()) {
					queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
				}
			}
		});
    	
    	groupingCheckBox = new JCheckBox("Grouping");
    	groupingCheckBox.setSelected(queryCache.isGroupingEnabled());
    	groupingCheckBox.addActionListener(new AbstractAction() {

    		public void actionPerformed(ActionEvent e) {
    			queryCache.setGroupingEnabled(groupingCheckBox.isSelected());
    			executeQueryInCache();
    		}
    	});
    	FormLayout layout = new FormLayout("pref, 3dlu, pref:grow, 3dlu, max(pref;50dlu), 3dlu, pref, 3dlu, pref"
    			,"pref, pref, fill:min(pref;100dlu):grow");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append("Where:", queryPen.getGlobalWhereText());
    	JPanel searchPanel = new JPanel(new BorderLayout());
    	searchPanel.add(new JLabel(ICON), BorderLayout.WEST);
    	searchField = new JTextField(queryUIComponents.getSearchDocument(), null, 0);
		searchPanel.add(searchField, BorderLayout.CENTER);
    	southPanelBuilder.append(searchPanel);
    	southPanelBuilder.append(new JLabel("Row Limit"));
    	final JSpinner rowLimitSpinner = queryUIComponents.getRowLimitSpinner();
    	rowLimitSpinner.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				executeQueryInCache();
			}
		});
    	rowLimitSpinner.setValue(new Integer(1000));
    	southPanelBuilder.append(rowLimitSpinner);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(groupingCheckBox);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(resultPane, 9);
    	
    	JPanel rightTreePanel = new JPanel(new BorderLayout());
    	rightTreePanel.add(new JScrollPane(dragTree),BorderLayout.CENTER);
    	rightTreePanel.add(reportComboBox, BorderLayout.NORTH);
    	
    	rightTopPane.setOneTouchExpandable(true);
    	rightTopPane.setResizeWeight(1);
    	rightTopPane.add(queryPenAndTextTabPane, JSplitPane.LEFT);
    	rightTopPane.add(rightTreePanel, JSplitPane.RIGHT);
    	rightTreePanel.setMinimumSize(new Dimension(0, 0));
    	
    	mainSplitPane.setOneTouchExpandable(true);
    	mainSplitPane.setResizeWeight(1);
    	mainSplitPane.add(rightTopPane, JSplitPane.TOP);
    	JPanel southPanel = southPanelBuilder.getPanel();
		mainSplitPane.add(southPanel, JSplitPane.BOTTOM);
		southPanel.setMinimumSize(new Dimension(0, 0));
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
	public synchronized void executeQueryInCache() {
		queuedQueryCache.add(new QueryCache(queryCache));
		queryUIComponents.executeQuery(queryCache.generateQuery());
		columnNameLabel.setIcon(THROBBER);
		for (JTable table : queryUIComponents.getResultTables()) {
			table.setBackground(REFRESH_GREY);
		}
	}
	
	public JComponent getPanel() {
		return mainSplitPane;
	}
	
	public JSplitPane getFullSplitPane() {
		return mainSplitPane;
	}
	
	public JSplitPane getTopRightSplitPane() {
		return rightTopPane;
	}
	
	public SQLObjectRoot getRootNode() {
		return rootNode;
	}


	public boolean applyChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		disconnect();
		return true;
	}


	public void discardChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		disconnect();
	}


	public boolean hasUnsavedChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		return false;
	}
	
	/**
	 * Disconnects all listeners in the query cache. Also closes any open database
	 * connections.
	 */
	private void disconnect() {
		queryController.disconnect();
		queryCache.removePropertyChangeListener(queryCacheListener);
		logger.debug("Removed the query panel change listener on the query cache");
		queryUIComponents.closeConMap();
		queryUIComponents.disconnectListeners();
		try {
			for (int i = rootNode.getChildren().size() - 1; i >= 0; i--) {
				rootNode.removeChild(i);
			}
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}
		queryPen.cleanup();
	}

}
