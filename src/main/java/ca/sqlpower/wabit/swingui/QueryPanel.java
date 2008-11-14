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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
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
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.query.SQLQueryUIComponents;
import ca.sqlpower.swingui.query.TableChangeEvent;
import ca.sqlpower.swingui.query.TableChangeListener;
import ca.sqlpower.swingui.table.FancyExportableJTable;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.dao.ProjectXMLDAO;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.StringItem;
import ca.sqlpower.wabit.query.QueryCache.OrderByArgument;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class QueryPanel {
	
	private static final Logger logger = Logger.getLogger(QueryPanel.class);
	
	private static final String SQL_TEXT_TAB_HEADING = "SQL";
    
	private static final String QUERY_EXECUTE = "Execute";
	
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
	private JPanel cornerPanel;
	
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
	private JSplitPane rightTopPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * This is the root of the JTree on the right of the query builder. This
	 * will let the user drag and drop components into the query.
	 */
	private SQLObjectRoot rootNode;
	
	public QueryPanel(WabitSwingSession session) {
		this.session = session;
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryCache = new QueryCache();
		queryPen = new QueryPen(session, this, queryCache);
		queryUIComponents = new SQLQueryUIComponents(session, session.getContext().getDataSources(), mainSplitPane);
		queryUIComponents.enableMultipleQueries(false);
		queryController = new QueryController(queryCache, queryPen, queryUIComponents.getDatabaseComboBox());
		queuedQueryCache = new ArrayList<QueryCache>();
		
		dragTree = new JTree();
		rootNode = new SQLObjectRoot();
		reportComboBox = queryUIComponents.getDatabaseComboBox();
		reportComboBox.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				try {
					for (int i = rootNode.getChildren().size() - 1; i >= 0; i--) {
						rootNode.removeChild(i);
					}
					rootNode.addChild(new SQLDatabase(
							(SPDataSource) reportComboBox.getSelectedItem()));
					DBTreeModel tempTreeModel = new DBTreeModel(rootNode);
					dragTree.setModel(tempTreeModel);
				} catch (ArchitectException e) {
					throw new RuntimeException(
							"Could not add DataSource to rootNode", e);
				}

			}
		});
		reportComboBox.setSelectedIndex(0);
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
				
				QueryPanel.this.session.getUserInformationLogger().info(queryUIComponents.getLogTextArea().getText());
			}
		});
    	
		buildUI();
	}

	
	private void buildUI() {
		JPanel resultPanel = queryUIComponents.getFirstResultPanel();

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
    	queryPen.getQueryPenBar().add(playPenExecuteButton, 0);
    	queryPen.getQueryPenCavas().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                , QUERY_EXECUTE);
    	queryPen.getQueryPenCavas().getActionMap().put(QUERY_EXECUTE, queryExecuteAction);
    	
    	queryPen.getQueryPenBar().add(new JButton(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				int retval = fc.showSaveDialog(QueryPanel.this.getFullSplitPane());
				if (retval == JFileChooser.APPROVE_OPTION) {
					ProjectXMLDAO dao;
					try {
						logger.debug("Starting save");
						FileOutputStream out = new FileOutputStream(fc.getSelectedFile());
						dao = new ProjectXMLDAO(out);
						dao.saveQueryCache(queryCache);
						dao.close();
						out.flush();
						out.close();
						logger.debug("Save complete");
					} catch (FileNotFoundException e1) {
						throw new RuntimeException(e1);
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		}), 0);
    	
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
    	FormLayout layout = new FormLayout("pref, 3dlu, pref:grow, 3dlu, pref, 3dlu, min(pref;50dlu)"
    			,"pref, 2dlu, pref, 2dlu,  pref, fill:min(pref;100dlu):grow");
    	DefaultFormBuilder southPanelBuilder = new DefaultFormBuilder(layout);
    	southPanelBuilder.append(new JLabel(""));
    	southPanelBuilder.append(new JLabel(""));
    	southPanelBuilder.append(new JLabel("Row Limit"));
    	JSpinner rowLimitSpinner = queryUIComponents.getRowLimitSpinner();
    	rowLimitSpinner.setValue(new Integer(1000));
    	southPanelBuilder.append(rowLimitSpinner);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append("Where:", queryPen.getGlobalWhereText(), 5);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(groupingCheckBox);
    	southPanelBuilder.append(new JLabel(""));
    	southPanelBuilder.append(queryUIComponents.getFilterAndLabelPanel(),3);
    	southPanelBuilder.nextLine();
    	southPanelBuilder.append(resultPanel, 7);
    	
    	JPanel rightTreePanel = new JPanel(new BorderLayout());
    	rightTreePanel.add(new JScrollPane(dragTree),BorderLayout.CENTER);
    	rightTreePanel.add(reportComboBox, BorderLayout.NORTH);
    	
    	rightTopPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	rightTopPane.add(queryPenAndTextTabPane, JSplitPane.LEFT);
    	rightTopPane.add(rightTreePanel, JSplitPane.RIGHT);
    	
    	mainSplitPane.add(rightTopPane, JSplitPane.TOP);
    	mainSplitPane.add(southPanelBuilder.getPanel(), JSplitPane.BOTTOM);
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
	
	public JSplitPane getFullSplitPane() {
		return mainSplitPane;
	}
	
	public JSplitPane getTopRightSplitPane() {
		return rightTopPane;
	}
	
	public SQLObjectRoot getRootNode() {
		return rootNode;
	}
}
