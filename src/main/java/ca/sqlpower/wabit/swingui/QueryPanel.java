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
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.QueryChangeEvent;
import ca.sqlpower.query.QueryChangeListener;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.Query.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.querypen.QueryPen;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.swingui.action.CreateLayoutFromQueryAction;
import ca.sqlpower.wabit.swingui.action.ExportQueryAction;
import ca.sqlpower.wabit.swingui.action.ExportSQLScriptAction;
import ca.sqlpower.wabit.swingui.action.ShowQueryPropertiesAction;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class QueryPanel implements WabitPanel {
	
	private static final Logger logger = Logger.getLogger(QueryPanel.class);
	
	private static final String SQL_TEXT_TAB_HEADING = "SQL";
	
	private static final ImageIcon THROBBER = new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/throbber.gif"));
	
	private static final ImageIcon ICON = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/swingui/query/search.png"));
	
	/**
	 * This is the property name for changes to the width on a {@link TableColumn}.
	 * The constant COLUMN_WIDTH_PROPERTY is not the property that will be fired
	 * on a column width change.
	 */
	private static final String TABLE_COLUMN_WIDTH = "preferredWidth";
	
	/**
	 * The background colour given to the JTables when they are being updated. This will
	 * give the users a more noticeable change when there is an update occurring.
	 */
	private static final Color REFRESH_GREY = new Color(0xeeeeee);

	/**
	 * The action for expanding and un-expanding the query and text panel quickly.
	 */
	private static final Object EXPAND_ACTION = "expandAction";
    
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
     * This list stores a query string for each queued QueryCache in the
     * queuedQueryCache list. The queries stored here come from the original
     * QueryCache and may be different from the query generated from it's copy.
     * <p>
     * XXX This is a temporary solution to the problem where a Query and its
     * copy do not return the same generated query as the depth first search
     * returns the tables in the from clause in a different order. Because the
     * original query can change while it is being executed the columns
     * selected, or other aspects of the query could be different in the
     * original query when the result set table is returned. This can result in
     * the query trying to place values in the header of the result table on the
     * wrong columns. To solve this problem a copy is made of the query to keep
     * track of the correct header values for each column. The recent problem
     * that is occurring is the original query, which is passed to the
     * SQLQueryUIComponents, does not generate the same query string as its
     * copy. This can cause the addGroupingTableHeaders method to not correctly
     * match the copy of the QueryCache to its result set table and not add any
     * headers. This then causes the UI to look like it lost the grouping and
     * sort order. <br>
     * A better way to implement this would be on every call to
     * executeQueryInCache create a copy of the query and pass that copy as a
     * StatementExecutor to the SQLQueryUIComponents. Then have the
     * SQLQueryUIComponents store the executor with the table so you can get
     * back the query copy without storing it in a queue in the query panel.
     * Passing just the copy to the SQLQueryUIComponents would mean that the
     * query copy would not be able to change while in the SQLQueryUIComponents
     * and could be immutable. This may also simplify the addGroupingTableHeaders
     * method. The problem I'm unsure how to solve is with this approach the
     * original QueryCache never executes so it's cache becomes stale. If the
     * cache is stale it will not update when placed on a report and may cause
     * exceptions if there are fewer columns in the cached result set than
     * the query. The query copies would have to update the original query's
     * cached result set correctly or remove the original query's cache
     * every time a query is executed in the QueryPanel and take a performance
     * hit when switching to a report layout.
     */
	private final List<String> queuedQueryCacheQueries;
	
	/**
	 * This is the tabbed pane that contains the query pen and text editor.
	 * All the query editing UI should be in this tabbed pane.
	 */
	private JTabbedPane queryPenAndTextTabPane;

	private final QueryController queryController;

	private final WabitSwingSession session;
	
	private final WabitSwingSessionContext context;
	
	/**
	 * The tree on the right-hand side that you drag tables into the query pen from.
	 */
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
	private final QueryChangeListener queryListener = new QueryChangeListener() {
    
        public void propertyChangeEvent(PropertyChangeEvent evt) {
            if (queryCache.getQuery().getCanExecuteQuery() 
                    && evt.getPropertyName() != Query.USER_MODIFIED_QUERY 
                    && evt.getPropertyName() != "running") {
                executeQueryInCache();
            }
        }
    
        public void joinRemoved(QueryChangeEvent evt) {
            executeQuery();
        }

        private void executeQuery() {
            if (queryCache.getQuery().getCanExecuteQuery()) {
                executeQueryInCache();
            }
        }
    
        public void joinPropertyChangeEvent(PropertyChangeEvent evt) {
            executeQuery();
        }
    
        public void joinAdded(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void itemRemoved(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void itemPropertyChangeEvent(PropertyChangeEvent evt) {
            executeQuery();
        }
    
        public void itemOrderChanged(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void itemAdded(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void containerRemoved(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void containerAdded(QueryChangeEvent evt) {
            executeQuery();
        }
    
        public void canExecuteQuery() {
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
	
	/**
	 * This is the current column model of the JTable being displayed in the results.
	 * The column model will tell the query cache the size changes of each column
	 * to keep them the same size.
	 */
	TableColumnModel tableColumnModel;
	
	/**
	 * The listener that will update the column sizes in the model. This will
	 * allow changing the query while keeping the sizes of the remaining columns
	 * the same.
	 */
	private final PropertyChangeListener resizingColumnChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(TABLE_COLUMN_WIDTH) && !((Integer) evt.getNewValue()).equals(evt.getOldValue())) {
				Enumeration<TableColumn> columns = tableColumnModel.getColumns();
				int i = 0;
				while (columns.hasMoreElements()) {
					if (columns.nextElement() == evt.getSource()) {
						break;
					}
					i++;
				}
				logger.debug("Received column width change on column " + i + " the new width is " + (Integer) evt.getNewValue());
				Item resizedItem = queryCache.getQuery().getSelectedColumns().get(i);
				resizedItem.setColumnWidth((Integer) evt.getNewValue());
				
			}
		}
	};
	
	/**
	 * This listens to mouse dragging of a column in a table. This handles 
	 * auto-scrolling during the drag and drop operation, in the case that the
	 * user wants to drag the column past the visible region on the screen.
	 */
	private final MouseMotionListener reorderSelectionByHeaderAutoScrollTable = new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {
			Rectangle rect = new Rectangle(e.getX(), e.getY(), 1, 1);
			((JTableHeader) e.getSource()).getTable().scrollRectToVisible(rect);
		}	
	};

	public QueryPanel(WabitSwingSession session, QueryCache cache) {
		logger.debug("Constructing new query panel.");
		this.session = session;
		context = (WabitSwingSessionContext) session.getContext();
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryCache = cache;
		
		final Action queryPenExecuteButtonAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                executeQueryInCache();
            }
        };
        queryPen = new QueryPen(
                queryPenExecuteButtonAction,
                queryCache.getQuery(),
                WabitSwingSessionContextImpl.FORUM_ACTION);
		queryPen.setExecuteIcon(new ImageIcon(QueryPen.class.getClassLoader().getResource("ca/sqlpower/swingui/querypen/wabit_execute.png")));
		queryPen.setQueryPenToolBar(createQueryPenToolBar(queryPen));
		queryPen.getGlobalWhereText().setText(cache.getQuery().getGlobalWhereClause());
		
		queryUIComponents = new SQLQueryUIComponents(context, 
		        new SpecificDataSourceCollection<JDBCDataSource>(session.getWorkspace(), JDBCDataSource.class), 
		        context, mainSplitPane, queryCache);
		queryUIComponents.setRowLimitSpinner(context.getRowLimitSpinner());
		queryUIComponents.setShowSearchOnResults(false);
		queryController = new QueryController(queryCache.getQuery(), queryPen, queryUIComponents.getDatabaseComboBox(), queryUIComponents.getQueryArea(), queryPen.getZoomSlider());
		queryPen.setZoom(queryCache.getQuery().getZoomLevel());
		queuedQueryCache = new ArrayList<QueryCache>();
		queuedQueryCacheQueries = new ArrayList<String>();
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
					    // FIXME the session (or session context) should be maintaining a map of data
					    // sources to SQLDatabase instances. Each SQLDatabase instance has its own connection pool! 
						rootNode.addChild(context.getDatabase((JDBCDataSource) reportComboBox.getSelectedItem()));
						DBTreeModel tempTreeModel = new DBTreeModel(rootNode);
						dragTree.setModel(tempTreeModel);
						dragTree.setVisible(true);
					} 
				} catch (SQLObjectException e) {
					throw new RuntimeException(
							"Could not add DataSource to rootNode", e);
				}

			}
		});
		if (session.getWorkspace().getDataSources().size() != 0) {
            if (queryCache.getQuery().getDatabase() == null) {
                dragTree.setVisible(false);
            } else {
                reportComboBox.setSelectedItem((SPDataSource) queryCache.getQuery().getDatabase().getDataSource());
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
				if (tableColumnModel != null) {
					Enumeration<TableColumn> enumeration = tableColumnModel.getColumns();
					while (enumeration.hasMoreElements()) {
						enumeration.nextElement().removePropertyChangeListener(resizingColumnChangeListener);
					}
					e.getChangedTable().getTableHeader().removeMouseMotionListener(reorderSelectionByHeaderAutoScrollTable);
				}
			}
		
			public void tableAdded(TableChangeEvent e) {
				
				logger.debug("Table added.");
				queryController.unlistenToCellRenderer();
				TableModelSortDecorator sortDecorator = null;
				final JTable table = e.getChangedTable();
				if (table instanceof FancyExportableJTable) {
					FancyExportableJTable fancyTable = (FancyExportableJTable)table;
					sortDecorator = fancyTable.getTableModelSortDecorator();
				}
				ComponentCellRenderer renderer = new ComponentCellRenderer(table, sortDecorator);
				table.getTableHeader().setDefaultRenderer(renderer);

				ListModel lm = new RowListModel(table);
				final JList rowHeader = new JList(lm);
				rowHeader.setFixedCellWidth(groupingLabel.getPreferredSize().width + 2);
				
				rowHeader.setCellRenderer(new RowHeaderRenderer(table));
				
				table.addPropertyChangeListener("rowHeight", new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						rowHeader.setFixedCellHeight(table.getRowHeight());
					}
		        });
		        
				rowHeader.setFixedCellHeight(table.getRowHeight());
				
				((JScrollPane)table.getParent().getParent()).setRowHeaderView(rowHeader);
				
				((JScrollPane)table.getParent().getParent()).setCorner(JScrollPane.UPPER_LEFT_CORNER, cornerPanel);
				addGroupingTableHeaders();
				
				tableColumnModel = e.getChangedTable().getColumnModel();
				Enumeration<TableColumn> enumeration = tableColumnModel.getColumns();
				while (enumeration.hasMoreElements()) {
					enumeration.nextElement().addPropertyChangeListener(resizingColumnChangeListener);
				}
				table.getTableHeader().addMouseMotionListener(reorderSelectionByHeaderAutoScrollTable);
				
				//TODO: Add the new renderer to result sets on both tabs when a parser exists to go between them easier.
				if (queryPenAndTextTabPane.getSelectedComponent() != queryToolPanel) {
					queryController.listenToCellRenderer(renderer);
				}
				
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
		JButton prevQueryButton = queryUIComponents.getPrevQueryButton();
		prevQueryButton.setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/arrow_left.png")));
		prevQueryButton.setToolTipText("Previous Executed Query");
		prevQueryButton.setText("");
		queryToolBar.add(prevQueryButton);
		JButton nextQueryButton = queryUIComponents.getNextQueryButton();
		nextQueryButton.setIcon(new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/arrow_right.png")));
		nextQueryButton.setToolTipText("Next Executed Query");
		nextQueryButton.setText("");
		queryToolBar.add(nextQueryButton);
		queryToolBar.addSeparator();
		JButton executeButton = queryUIComponents.getExecuteButton();
		ImageIcon executeIcon = new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/wabit_execute.png"));
		executeButton.setIcon(executeIcon);
		executeButton.setToolTipText(executeButton.getText());
		executeButton.setText("");
		queryToolBar.add(executeButton);
		queryUIComponents.getStopButton().setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				queryCache.stopRunning();
			}
		});
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
		queryToolBar.add(new CreateLayoutFromQueryAction(session.getWorkspace(), queryCache, queryCache.getName()));
		queryToolBar.add(new ShowQueryPropertiesAction(queryCache, context.getFrame()));
		
		JToolBar wabitBar = new JToolBar();
		wabitBar.setFloatable(false);
		JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		wabitBar.add(forumButton);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setLayout(new BorderLayout());
		toolBar.add(queryToolBar, BorderLayout.CENTER);
		toolBar.add(wabitBar, BorderLayout.EAST);
		
		queryToolPanel.add(toolBar, BorderLayout.NORTH);
		queryToolPanel.add(new RTextScrollPane(300,200, queryUIComponents.getQueryArea(), true),BorderLayout.CENTER);
    	
    	queryPenAndTextTabPane = new JTabbedPane();
		queryCache.getQuery().addQueryChangeListener(queryListener);
    	JPanel playPen = queryPen.createQueryPen();
    	DefaultFormBuilder queryExecuteBuilder = new DefaultFormBuilder(new FormLayout("pref:grow, 10dlu, pref"));

    	queryPenPanel = new JPanel(new BorderLayout());
    	queryPenPanel.add(playPen, BorderLayout.CENTER);
    	queryPenPanel.add(queryExecuteBuilder.getPanel(), BorderLayout.SOUTH);
    	queryPenAndTextTabPane.add(queryPenPanel,"QueryPen");
    	queryPenAndTextTabPane.add(queryToolPanel,SQL_TEXT_TAB_HEADING);
    	if (queryCache.isScriptModified()) {
    		queryPenAndTextTabPane.setSelectedComponent(queryToolPanel);
    		queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
    	}
    	
    	final JLabel whereText = new JLabel("Where:");
    	
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
						
						queryCache.getQuery().removeUserModifications();
						queryPen.getGlobalWhereText().setVisible(true);
						groupingCheckBox.setVisible(true);
						whereText.setVisible(true);
					}
				} else if (queryToolPanel == queryPenAndTextTabPane.getSelectedComponent()) {
					queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
					queryPen.getGlobalWhereText().setVisible(false);
					groupingCheckBox.setVisible(false);
					whereText.setVisible(false);
				}
				executeQueryInCache();
			}
		});
    	
    	groupingCheckBox = new JCheckBox("Grouping");
    	groupingCheckBox.setSelected(queryCache.getQuery().isGroupingEnabled());
    	groupingCheckBox.addActionListener(new AbstractAction() {

    		public void actionPerformed(ActionEvent e) {
    			queryCache.getQuery().setGroupingEnabled(groupingCheckBox.isSelected());
    			executeQueryInCache();
    		}
    	});
    	FormLayout layout = new FormLayout("pref, 5dlu, pref, 3dlu, pref:grow, 5dlu, max(pref;50dlu)"
    			,"pref, fill:min(pref;100dlu):grow");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append(groupingCheckBox);
    	southPanelBuilder.append(whereText, queryPen.getGlobalWhereText());
    	JPanel searchPanel = new JPanel(new BorderLayout());
    	searchPanel.add(new JLabel(ICON), BorderLayout.WEST);
    	searchField = new JTextField(queryUIComponents.getSearchDocument(), null, 0);
		searchPanel.add(searchField, BorderLayout.CENTER);
    	southPanelBuilder.append(searchPanel);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(resultPane, 7);
    	
    	JPanel rightTreePanel = new JPanel(new BorderLayout());
    	rightTreePanel.add(new JScrollPane(dragTree),BorderLayout.CENTER);
    	rightTreePanel.add(reportComboBox, BorderLayout.NORTH);
    	
    	rightTopPane.setOneTouchExpandable(true);
    	rightTopPane.setResizeWeight(1);
    	rightTopPane.add(queryPenAndTextTabPane, JSplitPane.LEFT);
    	rightTopPane.add(rightTreePanel, JSplitPane.RIGHT);
    	rightTreePanel.setMinimumSize(new Dimension(DBTreeCellRenderer.DB_ICON.getIconWidth() * 3, 0));
    	
    	mainSplitPane.setOneTouchExpandable(true);
    	mainSplitPane.setResizeWeight(1);
    	mainSplitPane.add(rightTopPane, JSplitPane.TOP);
    	JPanel southPanel = southPanelBuilder.getPanel();
		mainSplitPane.add(southPanel, JSplitPane.BOTTOM);
		southPanel.setMinimumSize(new Dimension(0, ICON.getIconHeight() * 5));
		
		executeQueryInCache();
	}
	
	private JToolBar createQueryPenToolBar(QueryPen pen) {
	    JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
	    toolBar.setFloatable(false);
	    
	    toolBar.add(pen.getPlayPenExecuteButton());
	    toolBar.addSeparator();

	    JButton exportQuery = new JButton(new ExportQueryAction(session, queryCache));
	    exportQuery.setToolTipText("Export query to Wabit file.");
	    toolBar.add(exportQuery);
	    JButton exportSQL = new JButton(new ExportSQLScriptAction(session, queryCache));
	    exportSQL.setToolTipText("Export query to SQL script.");
	    toolBar.add(exportSQL);
	    toolBar.addSeparator();
	    
	    toolBar.add(pen.getDeleteButton());
	    toolBar.add(pen.getCreateJoinButton());
	    toolBar.addSeparator();
	    toolBar.add(pen.getZoomSliderContainer());
	    toolBar.addSeparator();
	        
	    toolBar.add(new CreateLayoutFromQueryAction(session.getWorkspace(), queryCache, queryCache.getName()));
	    toolBar.add(new ShowQueryPropertiesAction(queryCache, context.getFrame()));
	    
	    return toolBar;
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
		if (queryPenAndTextTabPane.getSelectedIndex() == 1) {
			groupingLabel.setVisible(false);
			havingLabel.setVisible(false);
		}
		if (queryPenAndTextTabPane.getSelectedIndex() == 0) {
			ArrayList<JTable> tables = queryUIComponents.getResultTables();
			for(JTable t : tables)	{
				QueryCache cache = null;
				List<QueryCache> removeCacheList = new ArrayList<QueryCache>();
				List<String> removeQueryStringList = new ArrayList<String>();
				for (int i = 0; i < queuedQueryCacheQueries.size(); i++) {
				    String query = queuedQueryCacheQueries.get(i);
				    QueryCache c = queuedQueryCache.get(i);
					if (query.equals(queryUIComponents.getQueryForJTable(t))) {
						cache = c;
						break;
					}
					removeCacheList.add(c);
					removeQueryStringList.add(query);
				}
				for (QueryCache c : removeCacheList) {
					queuedQueryCache.remove(c);
				}
				for (String s : removeQueryStringList) {
				    queuedQueryCacheQueries.remove(s);
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
				    final SQLGroupFunction groupBy = cache.getQuery().getSelectedColumns().get(i).getGroupBy();
					if (!groupBy.equals(SQLGroupFunction.GROUP_BY)) {
                        String groupByAggregate = groupBy.getGroupingName();
						renderPanel.getComboBoxes().get(i).setSelectedItem(groupByAggregate);
					}
				}

				for (int i = 0; i < renderPanel.getTextFields().size(); i++) {
					String havingText = cache.getQuery().getSelectedColumns().get(i).getHaving();
					if (havingText != null) {
						renderPanel.getTextFields().get(i).setText(havingText);
					}
				}
				
				LinkedHashMap<Integer, Integer> columnSortMap = new LinkedHashMap<Integer, Integer>();
				for (Item column : cache.getQuery().getOrderByList()) {
					int columnIndex = cache.getQuery().getSelectedColumns().indexOf(column);
					OrderByArgument arg = column.getOrderBy();
					if (arg != null && arg != OrderByArgument.NONE) {
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
				
				for (int i = 0; i < t.getColumnCount(); i++) {
					Integer width = cache.getQuery().getSelectedColumns().get(i).getColumnWidth();
					logger.debug("Width in cache for column " + i + " is " + width);
					if (width != null) {
						t.getColumnModel().getColumn(i).setPreferredWidth(width);
					}
				}
			}
		}
	}
	
	/**
	 * This will execute the current query in the QueryCache and
	 * store a copy of the QueryCache in the queued list.
	 */
	public synchronized void executeQueryInCache() {
		queuedQueryCache.add(new QueryCache(queryCache));
		queuedQueryCacheQueries.add(queryCache.generateQuery());
		queryUIComponents.executeQuery(queryCache);
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
		queryCache.getQuery().removeQueryChangeListener(queryListener);
		logger.debug("Removed the query panel change listener on the query cache");
		queryUIComponents.closeConMap();
		queryUIComponents.disconnectListeners();
		try {
			for (int i = rootNode.getChildren().size() - 1; i >= 0; i--) {
				rootNode.removeChild(i);
			}
		} catch (SQLObjectException e) {
			throw new RuntimeException(e);
		}
		queryPen.cleanup();
	}

	public void maximizeEditor() {
		if (mainSplitPane.getDividerLocation() == mainSplitPane.getMaximumDividerLocation() 
				&& rightTopPane.getDividerLocation() == rightTopPane.getMaximumDividerLocation()) {
			mainSplitPane.setDividerLocation(mainSplitPane.getLastDividerLocation());
			rightTopPane.setDividerLocation(rightTopPane.getLastDividerLocation());
		} else {
			mainSplitPane.setDividerLocation(mainSplitPane.getMaximumDividerLocation());
			rightTopPane.setDividerLocation(rightTopPane.getMaximumDividerLocation());
		}
	}
	
	/**
	 * Returns the queryUIComponents used in this query panel for testing purposes.
	 */
	SQLQueryUIComponents getQueryUIComponents() {
		return queryUIComponents;
	}

}
