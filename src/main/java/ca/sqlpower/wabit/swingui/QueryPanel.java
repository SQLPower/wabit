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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.QueryChangeEvent;
import ca.sqlpower.query.QueryChangeListener;
import ca.sqlpower.query.QueryCompoundEditEvent;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.Query.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SpecificDataSourceCollection;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRoot;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.querypen.QueryPen;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.swingui.action.CreateLayoutFromQueryAction;
import ca.sqlpower.wabit.swingui.action.ExportSQLScriptAction;
import ca.sqlpower.wabit.swingui.action.ExportWabitObjectAction;
import ca.sqlpower.wabit.swingui.action.NewChartAction;
import ca.sqlpower.wabit.swingui.action.ShowQueryPropertiesAction;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class QueryPanel implements WabitPanel {
	
    /**
     * The icon added to actions that change the editor to the next
     * query that was executed in the list of queries executed. This
     * of course only works if there is a next query.
     */
    private static final ImageIcon NEXT_QUERY_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/next.png"));

    /**
     * This icon is added to actions that export the query.
     */
    private static final ImageIcon EXPORT_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/export.png"));
    
    /**
     * The icon added to actions that resets the query pen.
     */
    private static final ImageIcon RESET_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/cancel.png"));
    
    /**
     * The icon added to actions that allow users to create a join between two
     * columns in two different tables.
     */
    private static final ImageIcon CREATE_JOIN_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/join.png"));
    
    /**
     * Icon added to actions that create a chart based on the current query.
     */
    private static final ImageIcon CREATE_CHART_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/chart.png"));
    
    /**
     * Icon added to actions that execute the current query.
     */
    private static final ImageIcon EXECUTE_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/run.png"));
    
    /**
     * Icon on actions that reverses the last change to the editor.
     */
    private static final ImageIcon UNDO_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/undo.png"));
    
    /**
     * Icon on actions that repeats the last action that was undone.
     */
    private static final ImageIcon REDO_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/redo.png"));
    
    /**
     * Icon for the action to stop the query from executing.
     */
    private static final ImageIcon STOP_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/stop.png"));
    
    /**
     * Icon added to actions that will change the editor to display the 
     * query executed just before the current query.
     */
    private static final ImageIcon PREV_QUERY_ICON = 
        new ImageIcon(QueryPanel.class.getClassLoader().getResource(
                "icons/32x32/previous.png"));
    
	private static final Logger logger = Logger.getLogger(QueryPanel.class);
	
	private static final String SQL_TEXT_TAB_HEADING = "SQL";
	
	private static final ImageIcon THROBBER = new ImageIcon(QueryPanel.class.getClassLoader().getResource("icons/throbber16-01.png"));
	
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
    
	private static final Preferences prefs = Preferences.userNodeForPackage(QueryPanel.class);

	/**
	 * Prefs key for the horizontal split pane's divider location.
	 * <p>
	 * The value stored under this key is an <code>int</code>.
	 */
    private static final String RESULTS_DIVIDER_LOCATON_KEY = "QueryPanel.RESULTS_DIVIDER_LOCATON";
	
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
     * This class will display a modal dialog when it is created that will
     * prompt the user if they want to continue executing a query that contains
     * cross joins. Their response and their choice to keep seeing the prompt
     * are retrievable from methods in this class.
     */
	private static class CrossJoinDialog {
	    
	    private boolean continuingExecution;
	    
	    private boolean dontAskAgain = false;
	    
	    public CrossJoinDialog(JFrame parent) {
	        final JDialog crossJoinDialog = new JDialog(parent, "Query contains cross joins", true);
            JPanel crossJoinPanel = new JPanel(new MigLayout());
            final JLabel textLabel = new JLabel("<html>The query you are about to execute contains cross joins.<br> " +
                    "This query could take more time than expected to execute.<br> " +
                    "Do you wish to continue?</html>");
            textLabel.setHorizontalAlignment(SwingConstants.CENTER);
            crossJoinPanel.add(textLabel, "align 50%, span, wrap");
            final JCheckBox askAgainCheckBox = new JCheckBox("Do not ask me again.", dontAskAgain);
            crossJoinPanel.add(askAgainCheckBox, "align 50%, span, wrap");
            
            ButtonBarBuilder builder = new ButtonBarBuilder();
            builder.addGridded(new JButton(new AbstractAction("Continue") {
            
                public void actionPerformed(ActionEvent e) {
                    continuingExecution = true;
                    dontAskAgain = askAgainCheckBox.isSelected();
                    crossJoinDialog.dispose();
                }
            }));
            
            builder.addGridded(new JButton(new AbstractAction("Stop") {
            
                public void actionPerformed(ActionEvent e) {
                    continuingExecution = false;
                    dontAskAgain = askAgainCheckBox.isSelected();
                    crossJoinDialog.dispose();
                }
            }));
            crossJoinPanel.add(builder.getPanel(), "align right");
            
            crossJoinDialog.add(crossJoinPanel);
            crossJoinDialog.pack();
            crossJoinDialog.setLocationRelativeTo(parent);
            crossJoinDialog.setVisible(true);
	    }
	    
	    public boolean isContinuingExecution() {
            return continuingExecution;
        }
	    
	    public boolean getDontAskAgain() {
            return dontAskAgain;
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
	
    /**
     * Wraps {@link #dragTree}. This is the component returned by
     * {@link #getSourceComponent()}.
     */
    private JScrollPane dragTreeScrollPane;

	/**
	 * NOTE: This is the combo box for database connections. Not sure why it's called
	 * a report combo box.
	 */
	private JComboBox reportComboBox;

	/**
	 * This is the main JComponent for this query. All other components
	 * are placed in this.
	 */
	private final JSplitPane mainSplitPane;
	
	/**
	 * This is the root of the JTree on the right of the query builder. This
	 * will let the user drag and drop components into the query.
	 */
	private SQLObjectRoot rootNode;

	/**
	 * This will listen to any change in the query cache and update the results table as needed.
	 */
	private final QueryChangeListener queryListener = new QueryChangeListener() {
	    
	    private boolean inCompoundEdit = false;
    
        public void propertyChangeEvent(PropertyChangeEvent evt) {
            if (evt.getPropertyName() != Query.USER_MODIFIED_QUERY 
                    && evt.getPropertyName() != "running") {
                executeQuery();
            }
        }
    
        public void joinRemoved(QueryChangeEvent evt) {
            executeQuery();
        }

        private void executeQuery() {
            boolean disableAutoExecute = context.getPrefs().getBoolean(WabitSessionContext.DISABLE_QUERY_AUTO_EXECUTE, false);
			if (!inCompoundEdit
                    && queryCache.isAutomaticallyExecuting() 
                    && !disableAutoExecute) {
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
    
        public void compoundEditEnded(QueryCompoundEditEvent evt) {
            inCompoundEdit = false;
            executeQuery();
        }

        public void compoundEditStarted(QueryCompoundEditEvent evt) {
            inCompoundEdit = true;
        }
    };
    
	/**
	 * This is the panel that holds the QueryPen and the GUI SQL select in the tabbed pane.
	 */
	private JPanel queryPenPanel;

	/**
	 * This is the panel that holds the text editor for the query.
	 */
	private JComponent queryToolPanel;

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

	private final ExportWabitObjectAction<QueryCache> exportQueryAction;

    /**
     * This action does the exporting of the query to a file or straight text.
     */
    private Action exportAction = new AbstractAction("Export", EXPORT_ICON) {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
                JButton source = (JButton) e.getSource();
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem(exportQueryAction);
                menuItem.setText("Export Query to Workspace file");
                popupMenu.add(menuItem);
                menuItem = new JMenuItem(new ExportSQLScriptAction(session, queryCache));
                menuItem.setText("Export Query to SQL Script");
                popupMenu.add(menuItem);
                popupMenu.show(source, 0, source.getHeight());
            }
        }
    };

    /**
     * This toolbar builder is a permanent fixture of the query panel, but it
     * does get cleared out and refilled with different actions whenever we
     * switch views between the GUI (querypen) and the textual SQL editor.
     * 
     * @see #changeToTextToolBar()
     * @see #changeToGUIToolBar()
     */
    private final WabitToolBarBuilder toolBarBuilder = new WabitToolBarBuilder();
    
	public QueryPanel(WabitSwingSession session, QueryCache cache) {
		logger.debug("Constructing new QueryPanel@" + System.identityHashCode(this));
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
                queryCache.getQuery());
		queryPen.setExecuteIcon((ImageIcon) WabitIcons.RUN_ICON_32);
		queryPen.getGlobalWhereText().setText(cache.getQuery().getGlobalWhereClause());
		
		exportQueryAction = new ExportWabitObjectAction<QueryCache>(session,
				queryCache, WabitIcons.WABIT_FILE_ICON_16,
				"Export Query to Wabit file");
		
		queryUIComponents = new SQLQueryUIComponents(session, 
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
		dragTree.setRootVisible(false);
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
						dragTree.expandRow(0);
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
        		List<SPDataSource> dataSources = session.getWorkspace().getConnections();
        		List<JDBCDataSource> availableDS = new ArrayList<JDBCDataSource>();
        		for (SPDataSource ds : dataSources) {
        			if (ds instanceof JDBCDataSource) {
        				availableDS.add((JDBCDataSource) ds);
        			}
        		}
                final JDBCDataSource startingDataSource;
                if (availableDS.size() > 0) {
                	startingDataSource = (JDBCDataSource) availableDS.get(0);
                } else {
                	startingDataSource = null;
                }
                
                SPSwingWorker databaseLazyLoad = new SPSwingWorker(session) {
                	public void doStuff() throws Exception {
                		if (startingDataSource != null) {
                			//populate the database
                            SQLDatabase db = context.getDatabase(startingDataSource);
                		}
                	}
                	
        			public void cleanup() throws Exception {
        				if (reportComboBox.getSelectedItem() == null) {
        					reportComboBox.setSelectedItem(startingDataSource);
        					dragTree.setVisible(true);
        				}
        			}
                };
                //populates some data in a separate thread to create an easier workflow
                //when a user creates a new query (bug 2054)
                if (startingDataSource != null) {
                	databaseLazyLoad.run();
                }
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

		/*
		 * Default split pane size is 3/4-1/4 of the screen height or else
		 * the results won't be visible and the user won't see them update
		 */
		mainSplitPane.setDividerLocation(
		        prefs.getInt(
		                RESULTS_DIVIDER_LOCATON_KEY,
		                (int) (session.getContext().getFrame().getHeight() * 3 / 4)));

	}

	
	private void buildUI() {
		JTabbedPane resultPane = queryUIComponents.getResultTabPane();
		
		queryUIComponents.getQueryArea().setLineWrap(true);
		queryToolPanel = new RTextScrollPane(300,200, queryUIComponents.getQueryArea(), true);
    	
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
    	
    	final JPanel topPanel = new JPanel(new BorderLayout());
    	topPanel.add(queryPenAndTextTabPane, BorderLayout.CENTER);
    	
    	DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu, pref"));
    	builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        builder.append("Database Connection", reportComboBox);
        topPanel.add(builder.getPanel(), BorderLayout.NORTH);
        
        if (queryPenPanel == queryPenAndTextTabPane.getSelectedComponent()) {
        	changeToGUIToolBar();
        } else if (queryToolPanel == queryPenAndTextTabPane.getSelectedComponent()) {
        	changeToTextToolBar();
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
						
						queryCache.getQuery().removeUserModifications();
						queryPen.getGlobalWhereText().setVisible(true);
						groupingCheckBox.setVisible(true);
						whereText.setVisible(true);
						changeToGUIToolBar();
					}
				} else if (queryToolPanel == queryPenAndTextTabPane.getSelectedComponent()) {
					queryUIComponents.getQueryArea().setText(queryCache.generateQuery());
					queryPen.getGlobalWhereText().setVisible(false);
					groupingCheckBox.setVisible(false);
					whereText.setVisible(false);
					changeToTextToolBar();
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
    			,"pref, fill:0dlu:grow");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append(groupingCheckBox);
    	southPanelBuilder.append(whereText, queryPen.getGlobalWhereText());
    	JPanel searchPanel = new JPanel(new BorderLayout());
    	searchPanel.add(new JLabel(ICON), BorderLayout.WEST);
    	searchField = new JTextField(queryUIComponents.getSearchDocument(), null, 0);
		searchPanel.add(searchField, BorderLayout.CENTER);
    	southPanelBuilder.append(searchPanel);
    	southPanelBuilder.nextLine();
    	resultPane.setPreferredSize(new Dimension(
    	        (int) resultPane.getPreferredSize().getWidth(),
    	        0));
    	southPanelBuilder.append(resultPane, 7);
    	
    	dragTreeScrollPane = new JScrollPane(dragTree);
    	dragTreeScrollPane.setMinimumSize(new Dimension(DBTreeCellRenderer.DB_ICON.getIconWidth() * 3, 0));
    	
    	mainSplitPane.setOneTouchExpandable(true);
    	mainSplitPane.setResizeWeight(1);
    	mainSplitPane.add(topPanel, JSplitPane.TOP);
    	JPanel bottomPanel = southPanelBuilder.getPanel();
		mainSplitPane.add(bottomPanel, JSplitPane.BOTTOM);
		bottomPanel.setMinimumSize(new Dimension(0, ICON.getIconHeight() * 5));

		mainSplitPane.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
            	boolean disableAutoExecute = context.getPrefs().getBoolean(WabitSessionContext.DISABLE_QUERY_AUTO_EXECUTE, false);
                if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) > 0
                        && mainSplitPane.getParent() != null
                        && !queryCache.isScriptModified()
                        && !disableAutoExecute) {
                	executeQueryInCache();
                    mainSplitPane.removeHierarchyListener(this);
                }
            }
        });
	}

	/**
	 * This will modify the given tool bar to contain the buttons necessary to
	 * use the text editor of the query panel.
	 */
    private void changeToTextToolBar() {
        toolBarBuilder.clear();
        
		JButton prevQueryButton = queryUIComponents.getPrevQueryButton();
		toolBarBuilder.add(prevQueryButton, "Prev. Query", PREV_QUERY_ICON);
		JButton nextQueryButton = queryUIComponents.getNextQueryButton();
		toolBarBuilder.add(nextQueryButton, "Next Query", NEXT_QUERY_ICON);
		toolBarBuilder.addSeparator();
		JButton executeButton = queryUIComponents.getExecuteButton();
		toolBarBuilder.add(executeButton, "Execute", EXECUTE_ICON);

		queryUIComponents.getStopButton().setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				queryCache.cancel();
			}
		});
		toolBarBuilder.add(queryUIComponents.getStopButton(), "Stop", STOP_ICON);
		
		toolBarBuilder.addSeparator();
		
		JButton clearButton = queryUIComponents.getClearButton();
		toolBarBuilder.add(clearButton, "Clear", RESET_ICON);
		JButton undoButton = queryUIComponents.getUndoButton();
		toolBarBuilder.add(undoButton, "Undo", UNDO_ICON);
		JButton redoButton = queryUIComponents.getRedoButton();
		toolBarBuilder.add(redoButton, "Redo", REDO_ICON);
		toolBarBuilder.addSeparator();
		
		toolBarBuilder.add(exportAction, "Export", EXPORT_ICON);
		toolBarBuilder.addSeparator();
		
		toolBarBuilder.add(
		        new CreateLayoutFromQueryAction(session.getWorkspace(), 
		                queryCache, queryCache.getName()),
		                "Create Report");
		toolBarBuilder.add(new NewChartAction(session, queryCache), "Create Chart", 
		        CREATE_CHART_ICON);
		toolBarBuilder.add(new ShowQueryPropertiesAction(queryCache, context.getFrame()),
		        "Properties");
    }

    /**
     * This will add all of the necessary actions to the given tool bar. Before
     * any actions are added the tool bar will have all of its current buttons
     * and separators removed.
     * 
     * @param toolbarBuilder
     *            The tool bar to modify. The tool bar will contain all of the
     *            buttons necessary for editing the GUI query editor.
     */
	private void changeToGUIToolBar() {
	    
	    toolBarBuilder.clear();
	    
	    toolBarBuilder.add(queryPen.getExecuteQueryAction(), "Execute", 
	            WabitIcons.RUN_ICON_32);
	    toolBarBuilder.addSeparator();

		toolBarBuilder.add(exportAction, "Export");
	    toolBarBuilder.addSeparator();

	    final Action resetAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                queryCache.getQuery().reset();
                queryCache.cancel();
            }
        };
	    toolBarBuilder.add(resetAction, "Reset", RESET_ICON);
	    Action createJoinAction = queryPen.getJoinAction();
		toolBarBuilder.add(createJoinAction, "Join", CREATE_JOIN_ICON);
	    toolBarBuilder.addSeparator();
	    JPanel zoomSliderContainer = queryPen.getZoomSliderContainer();
		toolBarBuilder.add(zoomSliderContainer);
	    toolBarBuilder.addSeparator();
	        
	    Action createLayoutAction = new CreateLayoutFromQueryAction(
	            session.getWorkspace(), queryCache, queryCache.getName());
	    toolBarBuilder.add(createLayoutAction, "Create Report");
		toolBarBuilder.add(new NewChartAction(session, queryCache), "Create Chart", 
		        CREATE_CHART_ICON);
	    Action showPropertiesAction = 
	        new ShowQueryPropertiesAction(queryCache, context.getFrame());
	    toolBarBuilder.add(showPropertiesAction, "Properties");
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
	    if (queryCache.getPromptForCrossJoins() && queryCache.containsCrossJoins()) {
	        CrossJoinDialog dialog = new CrossJoinDialog(context.getFrame());
	        queryCache.setPromptForCrossJoins(!dialog.getDontAskAgain());
	        queryCache.setExecuteQueriesWithCrossJoins(dialog.isContinuingExecution());
	        if (!dialog.isContinuingExecution()) return;
	    } else if (!queryCache.getPromptForCrossJoins() && !queryCache.getExecuteQueriesWithCrossJoins()) {
	        return;
	    }
	    
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
	
	public SQLObjectRoot getRootNode() {
		return rootNode;
	}


	public boolean applyChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		cleanup();
		return true;
	}


	public void discardChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		cleanup();
	}


	public boolean hasUnsavedChanges() {
		//Changes are currently always done immediately. If we add a save button this will change.
		return false;
	}
	
	/**
	 * Disconnects all listeners in the query cache. Also closes any open database
	 * connections and updates prefs.
	 */
	private void cleanup() {
	    logger.debug("QueryPanel@" + System.identityHashCode(this) + " is cleaning up");
	    prefs.putInt(RESULTS_DIVIDER_LOCATON_KEY, mainSplitPane.getDividerLocation());
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
        logger.debug("QueryPanel@" + System.identityHashCode(this) + " cleanup done");
	}

	/**
	 * Returns the queryUIComponents used in this query panel for testing purposes.
	 */
	SQLQueryUIComponents getQueryUIComponents() {
		return queryUIComponents;
	}

	public String getTitle() {
		return "Query Editor - " + queryCache.getName();
	}
	
	public JComponent getSourceComponent() {
	    return dragTreeScrollPane;
	}
	
    public JToolBar getToolbar() {
        return toolBarBuilder.getToolbar();
    }
}
