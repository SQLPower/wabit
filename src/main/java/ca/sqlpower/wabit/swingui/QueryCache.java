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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.wabit.swingui.querypen.ContainerPane;
import ca.sqlpower.wabit.swingui.querypen.ItemPNode;
import ca.sqlpower.wabit.swingui.querypen.JoinLine;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

/**
 * This class will cache all of the parts of a select
 * statement and also listen to everything that could
 * change the select statement.
 */
public class QueryCache {
	
	private static final Logger logger = Logger.getLogger(QueryCache.class);
	
	/**
	 * The grouping function defined on a group by event if the column is
	 * to be grouped by and not aggregated on.
	 */
	public static final String GROUP_BY = "(GROUP BY)";
	
	/**
	 * The arguments that can be added to a column in the 
	 * order by clause.
	 */
	public enum OrderByArgument {
		ASC,
		DESC
	}
	
	/**
	 * This will map SQLColumns to aliases that are in the SELECT statement.
	 */
	private Map<Item, String> aliasMap;
	
	/**
	 * Tracks if there are groupings added to this select statement.
	 * This will affect when columns are added to the group by collections.
	 */
	private boolean groupingEnabled = false;

	/**
	 * This maps the SQLColumns in the select statement to their group by
	 * functions. If the column is in the GROUP BY clause and is not being
	 * aggregated on it should appear in the group by list.
	 */
	private Map<Item, SQLGroupFunction> groupByAggregateMap;
	
	/**
	 * A list of columns we are grouping by. These are not
	 * being aggregated on but are in the GROUP BY clause. 
	 */
	private List<Item> groupByList;

	/**
	 * This maps SQLColumns to having clauses. The entry with a null key
	 * contains the generic having clause that is not defined for a specific
	 * column.
	 */
	private Map<Item, String> havingMap;
	
	/**
	 * The columns in the SELECT statement that will be returned.
	 * These columns are stored in the order they will be returned
	 * in.
	 */
	private final List<Item> selectedColumns;
	
	/**
	 * This map contains the columns that have an ascending
	 * or descending argument and is in the order by clause.
	 */
	private final Map<Item, OrderByArgument> orderByArgumentMap;
	
	/**
	 * The order by list keeps track of the order that columns were selected in.
	 */
	private final List<Item> orderByList;
	
	/**
	 * The list of tables that we are selecting from.
	 */
	private final List<Container> fromTableList;
	
	/**
	 * This maps each table to a list of SQLJoin objects.
	 * These column pairs defines a join in the select statement.
	 */
	private final Map<Container, List<SQLJoin>> joinMapping;
	
	/**
	 * This maps the where clause defined on a column specific basis to their
	 * columns.
	 */
	private final Map<Item, String> whereMapping;
	
	/**
	 * This is the global where clause that is for all non-column-specific where
	 * entries.
	 */
	private String globalWhereClause;
	
	/**
	 * This maps containers to aliases used in a select statement.
	 */
	private final Map<Container, String> tableAliasMap;
	
	/**
	 * A listener that handles changes to the group by and having clauses.
	 */
	private PropertyChangeListener groupByAndHavingListener = new PropertyChangeListener() {
	
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ComponentCellRenderer.PROPERTY_GROUP_BY)) {
				Item column = selectedColumns.get(cellRenderer.getComboBoxes().indexOf((JComboBox)e.getSource()));
				if (e.getNewValue().equals(GROUP_BY)) {
					if (groupByList.contains(column)) {
						return;
					}
					groupByList.add(column);
					groupByAggregateMap.remove(column);
					logger.debug("Added " + column.getName() + " to group by list.");
				} else {
					String newValue = (String)e.getNewValue();
					if (SQLGroupFunction.valueOf(newValue).equals(groupByAggregateMap.get(column))) {
						return;
					}
					groupByAggregateMap.put(column, SQLGroupFunction.valueOf(newValue));
					groupByList.remove(column);
					logger.debug("Added " + column.getName() + " with aggregate " + newValue + " to aggregate group by map.");
				}
			} else if (e.getPropertyName().equals(ComponentCellRenderer.PROPERTY_HAVING)) {
				int indexOfTextField = cellRenderer.getTextFields().indexOf((JTextField)e.getSource());
				if (indexOfTextField < 0) {
					return;
				}
				Item column = selectedColumns.get(indexOfTextField);
				String newValue = (String)e.getNewValue();
				if (newValue != null && newValue.length() > 0) {
					if (!newValue.equals(havingMap.get(column))) {
						havingMap.put(column, newValue);
					}
				} else {
					havingMap.remove(column);
				}
			}
			for (ChangeListener l : queryChangeListeners) {
				l.stateChanged(new ChangeEvent(QueryCache.this));
			}
		}
	};
	
	/**
	 * This handles listening for order by changes in the sort decorator of a component
	 * cell renderer.
	 */
	private TableModelListener orderByListener = new TableModelListener() {
	
		public void tableChanged(TableModelEvent e) {
			TableModelSortDecorator sortDecorator = (TableModelSortDecorator)e.getSource();
			boolean sortChanged = false;
			for (int i = 0; i < sortDecorator.getColumnCount(); i++) {
				int sortStatus = sortDecorator.getSortingStatus(i);
				Item column = selectedColumns.get(i);
				if ((sortStatus == TableModelSortDecorator.NOT_SORTED && orderByArgumentMap.get(column) == null)
						|| (sortStatus == TableModelSortDecorator.ASCENDING && orderByArgumentMap.get(column) == OrderByArgument.ASC)
						|| (sortStatus == TableModelSortDecorator.DESCENDING && orderByArgumentMap.get(column) == OrderByArgument.DESC)) {
					if (sortStatus != TableModelSortDecorator.NOT_SORTED) {
						logger.debug("Column " + column.getName() + " is sorted by type " + sortStatus + " and has a stored sort order of " + orderByArgumentMap.get(column));
					}
					continue;
				}
				sortChanged = true;
				orderByList.remove(column);
				if (sortStatus == TableModelSortDecorator.NOT_SORTED) {
					orderByArgumentMap.remove(column);
				} else if (sortStatus == TableModelSortDecorator.ASCENDING) {
					logger.debug("Setting sort order of " + column.getName() + " to ascending.");
					orderByArgumentMap.put(column, OrderByArgument.ASC);
					orderByList.add(column);
				} else if (sortStatus == TableModelSortDecorator.DESCENDING) {
					logger.debug("Setting sort order of " + column.getName() + " to descending.");
					orderByArgumentMap.put(column, OrderByArgument.DESC);
					orderByList.add(column);
				} else {
					throw new IllegalStateException("The column " + column.getName() + " was sorted in an unknown way");
				}
			}
			if (sortChanged) {
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			}
		}
	};
	
	/**
	 * Listens for changes to the alias on ItemPNodes and updates
	 * the map accordingly. This would be better if it was placed
	 * directly on the ItemPNode and listened to only the alias change. 
	 */
	private PropertyChangeListener aliasListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ItemPNode.PROPERTY_ALIAS) && e.getSource() instanceof ItemPNode) {
				aliasChanged((ItemPNode)e.getSource());
			}
		}
	};
	
	/**
	 * Listens for changes to the select checkbox on the column of a table in 
	 * the query pen.
	 */
	private PropertyChangeListener selectedColumnListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ItemPNode.PROPERTY_SELECTED) && e.getSource() instanceof ItemPNode) {
				selectionChanged((ItemPNode)e.getSource(), (Boolean)e.getNewValue());
			}
		}
	};
	
	private PropertyChangeListener joinChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(QueryPen.PROPERTY_JOIN_ADDED)) {
				JoinLine joinLine = (JoinLine) e.getNewValue();
				ItemPNode leftNode = joinLine.getLeftNode();
				ItemPNode rightNode = joinLine.getRightNode();
				Item leftColumn = leftNode.getItem();
				Item rightColumn = rightNode.getItem();
				SQLJoin join = new SQLJoin(leftColumn, rightColumn);
				Container leftContainer = leftColumn.getParent().getParent();
				Container rightContainer = rightColumn.getParent().getParent();
				if (joinMapping.get(leftContainer) == null) {
					List<SQLJoin> joinList = new ArrayList<SQLJoin>();
					joinList.add(join);
					joinMapping.put(leftContainer, joinList);
				} else {
					joinMapping.get(leftContainer).add(join);
				}

				if (joinMapping.get(rightContainer) == null) {
					List<SQLJoin> joinList = new ArrayList<SQLJoin>();
					joinList.add(join);
					joinMapping.put(rightContainer, joinList);
				} else {
					joinMapping.get(rightContainer).add(join);
				}
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			} else if (e.getPropertyName().equals(QueryPen.PROPERTY_JOIN_REMOVED)) {
				JoinLine joinLine = (JoinLine) e.getOldValue();
				ItemPNode leftNode = joinLine.getLeftNode();
				ItemPNode rightNode = joinLine.getRightNode();
				Item leftColumn = leftNode.getItem();
				Item rightColumn = rightNode.getItem();

				List<SQLJoin> leftJoinList = joinMapping.get(leftColumn.getParent().getParent());
				for (SQLJoin join : leftJoinList) {
					if (leftColumn == join.getLeftColumn() && rightColumn == join.getRightColumn()) {
						leftJoinList.remove(join);
						break;
					}
				}

				List<SQLJoin> rightJoinList = joinMapping.get(rightColumn.getParent().getParent());
				for (SQLJoin join : rightJoinList) {
					if (leftColumn == join.getLeftColumn() && rightColumn == join.getRightColumn()) {
						rightJoinList.remove(join);
						break;
					}
				}
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			}
		}
	};
	
	private PropertyChangeListener fromChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(QueryPen.PROPERTY_TABLE_ADDED)) {
				if (evt.getNewValue() instanceof ContainerPane<?>) {
					ContainerPane<?> container = (ContainerPane<?>)evt.getNewValue();
					fromTableList.add(container.getModel());
					for (ChangeListener l : queryChangeListeners) {
						l.stateChanged(new ChangeEvent(QueryCache.this));
					}
				}
			} else if (evt.getPropertyName().equals(QueryPen.PROPERTY_TABLE_REMOVED)) {
				if (evt.getOldValue() instanceof ContainerPane<?>) {
					ContainerPane<?> container = (ContainerPane<?>)evt.getOldValue();
					Container table = container.getModel();
					fromTableList.remove(table);
					tableAliasMap.remove(table);
					for (Section section : table.getSections()) {
						for (Item col : section.getItems()) {
							whereMapping.remove(col);
							removeColumnSelection(col);
						}
					}
					for (ChangeListener l : queryChangeListeners) {
						l.stateChanged(new ChangeEvent(QueryCache.this));
					}
				}
			}
		}
	};
	
	private PropertyChangeListener whereListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ItemPNode.PROPERTY_WHERE)) {
				if (e.getSource() instanceof ItemPNode) {
					ItemPNode itemNode = (ItemPNode) e.getSource();
					if (e.getNewValue() != null && ((String)e.getNewValue()).length() > 0) {
						whereMapping.put(itemNode.getItem(), (String)e.getNewValue());
					} else {
						whereMapping.remove(itemNode.getItem());
					}
					for (ChangeListener l : queryChangeListeners) {
						l.stateChanged(new ChangeEvent(QueryCache.this));
					}
				}
			} else if (e.getPropertyName().equals(QueryPen.PROPERTY_WHERE_MODIFIED)) {
				globalWhereClause = (String)e.getNewValue();
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			}
		}
	};
	
	private final PropertyChangeListener tableAliasListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ContainerPane.PROPERTY_CONTAINTER_ALIAS)) {
				ContainerPane<?> pane = (ContainerPane<?>)e.getSource();
				if (pane.getContainerAlias() == null || pane.getContainerAlias().length() <= 0) {
					tableAliasMap.remove(pane.getModel());
				} else {
					tableAliasMap.put(pane.getModel(), pane.getContainerAlias());
				}
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			}
		}
	};

	/**
	 * This is the last column move in the result table registered by the
	 * reorderSelectionByHeaderListener listener. This will be null if there was
	 * no column move since the last time a table column was dragged.
	 */
	private TableColumnModelEvent lastTableColumnMove = null;

	/**
	 * This listens to the mouse releases on a table to know when to try and
	 * handle a table column move. The table columns should only be moved after
	 * the user is done dragging.
	 */
	private final MouseListener reorderSelectionByHeaderMouseListener = new MouseAdapter() {
		@Override
		public void mouseReleased(MouseEvent e) {
			if (lastTableColumnMove != null) {
				logger.debug("Moving column in select from " + lastTableColumnMove.getFromIndex() + " to " + lastTableColumnMove.getToIndex());
				Item movedColumn = selectedColumns.get(lastTableColumnMove.getFromIndex());
				selectedColumns.remove(movedColumn);
				selectedColumns.add(lastTableColumnMove.getToIndex(), movedColumn);
				lastTableColumnMove = null;
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(QueryCache.this));
				}
			}
		}
	};
	
	/**
	 * This will listen to the table for column reordering and update the select statement accordingly.
	 */
	private final TableColumnModelListener reorderSelectionByHeaderListener = new TableColumnModelListener() {
		public void columnSelectionChanged(ListSelectionEvent e) {
			//do nothing
		}
		public void columnRemoved(TableColumnModelEvent e) {
			//do nothing	
		}
		public void columnMoved(TableColumnModelEvent e) {
			if (e.getToIndex() != e.getFromIndex()) {
				if (lastTableColumnMove == null) {
					lastTableColumnMove = e;
				} else {
					lastTableColumnMove = new TableColumnModelEvent((TableColumnModel)e.getSource(), lastTableColumnMove.getFromIndex(), e.getToIndex());
				}
			}
		}
		public void columnMarginChanged(ChangeEvent e) {
			//do nothing
		}
		public void columnAdded(TableColumnModelEvent e) {
			//do nothing
		}
	};
	
	/**
	 * This is the current cell renderer we are listening on for group by 
	 * and having values.
	 */
	private ComponentCellRenderer cellRenderer;
	
	/**
	 * These listeners will fire an event whenever the query has changed.
	 */
	private final List<ChangeListener> queryChangeListeners;
	
	public QueryCache(QueryPen pen) {
		tableAliasMap = new HashMap<Container, String>();
		orderByArgumentMap = new HashMap<Item, OrderByArgument>();
		orderByList = new ArrayList<Item>();
		selectedColumns = new ArrayList<Item>();
		fromTableList = new ArrayList<Container>();
		joinMapping = new HashMap<Container, List<SQLJoin>>();
		whereMapping = new HashMap<Item, String>();
		queryChangeListeners = new ArrayList<ChangeListener>();
		aliasMap = new HashMap<Item, String>();
		groupByAggregateMap = new HashMap<Item, SQLGroupFunction>();
		groupByList = new ArrayList<Item>();
		havingMap = new HashMap<Item, String>();
		
		pen.addQueryListener(aliasListener);
		pen.addQueryListener(selectedColumnListener);
		pen.addQueryListener(fromChangeListener);
		pen.addQueryListener(joinChangeListener);
		pen.addQueryListener(whereListener);
		pen.addQueryListener(tableAliasListener);
	}
	
	/**
	 * A copy constructor for the query cache. This will not
	 * hook up listeners.
	 */
	public QueryCache(QueryCache copy) {
		selectedColumns = new ArrayList<Item>();
		aliasMap = new HashMap<Item, String>();
		fromTableList = new ArrayList<Container>();
		tableAliasMap = new HashMap<Container, String>();
		joinMapping = new HashMap<Container, List<SQLJoin>>();
		whereMapping = new HashMap<Item, String>();
		groupByList = new ArrayList<Item>();
		groupByAggregateMap = new HashMap<Item, SQLGroupFunction>();
		havingMap = new HashMap<Item, String>();
		orderByList = new ArrayList<Item>();
		orderByArgumentMap = new HashMap<Item, OrderByArgument>();
		
		selectedColumns.addAll(copy.getSelectedColumns());
		aliasMap.putAll(copy.getAliasMap());
		fromTableList.addAll(copy.getFromTableList());
		tableAliasMap.putAll(copy.getTableAliasMap());
		joinMapping.putAll(copy.getJoinMapping());
		whereMapping.putAll(copy.getWhereMapping());
		groupByList.addAll(copy.getGroupByList());
		groupByAggregateMap.putAll(copy.getGroupByAggregateMap());
		havingMap.putAll(copy.getHavingMap());
		orderByList.addAll(copy.getOrderByList());
		orderByArgumentMap.putAll(copy.getOrderByArgumentMap());
		globalWhereClause = copy.getGlobalWhereClause();
		groupingEnabled = copy.isGroupingEnabled();
		
		queryChangeListeners = new ArrayList<ChangeListener>();
	}
	
	public void setGroupingEnabled(boolean enabled) {
		logger.debug("Setting grouping enabled to " + enabled);
		if (!groupingEnabled && enabled) {
			for (Item col : selectedColumns) {
				groupByList.add(col);
			}
		} else if (!enabled) {
			groupByList.clear();
			groupByAggregateMap.clear();
			havingMap.clear();
		}
		groupingEnabled = enabled;
	}
	
	/**
	 * Removes the column from the selected columns list and all other
	 * related lists.
	 */
	private void removeColumnSelection(Item column) {
		selectedColumns.remove(column);
		aliasMap.remove(column);
		groupByList.remove(column);
		groupByAggregateMap.remove(column);
		havingMap.remove(column);
		orderByList.remove(column);
		orderByArgumentMap.remove(column);
	}
	
	/**
	 * Generates the query based on the cache.
	 */
	public String generateQuery() {
		if (selectedColumns.size() ==  0) {
			return "";
		}
		StringBuffer query = new StringBuffer();
		query.append("SELECT");
		boolean isFirstSelect = true;
		for (Item col : selectedColumns) {
			if (isFirstSelect) {
				query.append(" ");
				isFirstSelect = false;
			} else {
				query.append(", ");
			}
			String alias = tableAliasMap.get(col.getParent().getParent());
			if (alias == null) {
				alias = col.getParent().getParent().getName();
			}
			if (groupByAggregateMap.containsKey(col)) {
				query.append(groupByAggregateMap.get(col).toString() + "(");
			}
			query.append(alias + "." + col.getName());
			if (groupByAggregateMap.containsKey(col)) {
				query.append(")");
			}
			if (aliasMap.get(col) != null) {
				query.append(" AS " + aliasMap.get(col));
			}
		}
		query.append(" \nFROM");
		boolean isFirstFrom = true;
		for (Container table : fromTableList) {
			String qualifiedName;
			if (table.getContainedObject() instanceof SQLTable) {
				qualifiedName = ((SQLTable)table.getContainedObject()).toQualifiedName();
			} else {
				qualifiedName = table.getName();
			}
			String alias = tableAliasMap.get(table);
			if (alias == null) {
				alias = table.getName();
			}
			if (isFirstFrom) {
				query.append(" " + qualifiedName + " " + alias);
				isFirstFrom = false;
			} else {
				query.append(" \n\tINNER JOIN ");
				query.append(qualifiedName + " " + alias + " ON ");
				if (joinMapping.get(table) == null || joinMapping.get(table).isEmpty()) {
					query.append("TRUE");
				} else {
					boolean isFirstJoin = true;
					for (SQLJoin join : joinMapping.get(table)) {
						Item otherColumn;
						if (join.getLeftColumn().getParent().getParent() == table) {
							otherColumn = join.getRightColumn();
						} else {
							otherColumn = join.getLeftColumn();
						}
						for (int i = 0; i < fromTableList.indexOf(table); i++) {
							if (otherColumn.getParent().getParent() == fromTableList.get(i)) {
								if (isFirstJoin) {
									isFirstJoin = false;
								} else {
									query.append(" AND ");
								}
								String leftAlias = tableAliasMap.get(join.getLeftColumn().getParent().getParent());
								if (leftAlias == null) {
									leftAlias = join.getLeftColumn().getParent().getParent().getName();
								}
								String rightAlias = tableAliasMap.get(join.getRightColumn().getParent().getParent());
								if (rightAlias == null) {
									rightAlias = join.getRightColumn().getParent().getParent().getName();
								}
								query.append(leftAlias + "." + join.getLeftColumn().getName() + 
										" " + join.getComparator() + " " + 
										rightAlias + "." + join.getRightColumn().getName());
							}
						}
					}
				}
			}
		}
		query.append(" ");
		if (!whereMapping.isEmpty() || (globalWhereClause != null && globalWhereClause.length() > 0)) {
			query.append(" \nWHERE");
			boolean isFirstWhere = true;
			for (Map.Entry<Item, String> entry : whereMapping.entrySet()) {
				if (entry.getValue().length() > 0) {
					if (isFirstWhere) {
						query.append(" ");
						isFirstWhere = false;
					} else {
						query.append(" AND ");
					}
					String alias = tableAliasMap.get(entry.getKey().getParent().getParent());
					if (alias == null) {
						alias = entry.getKey().getParent().getParent().getName();
					}
					query.append(alias + "." + entry.getKey().getName() + " " + entry.getValue());
				}
			}
			if (!isFirstWhere && (globalWhereClause != null && globalWhereClause.length() > 0)) {
				query.append(" AND");
			}
			if (globalWhereClause != null) {
				query.append(" " + globalWhereClause);
			}
		}
		if (!groupByList.isEmpty()) {
			query.append("\nGROUP BY");
			boolean isFirstGroupBy = true;
			for (Item col : groupByList) {
				if (isFirstGroupBy) {
					query.append(" ");
					isFirstGroupBy = false;
				} else {
					query.append(", ");
				}
				String alias = tableAliasMap.get(col.getParent().getParent());
				if (alias == null) {
					alias = col.getParent().getParent().getName();
				}
				query.append(alias + "." + col.getName());
			}
			query.append(" ");
		}
		if (!havingMap.isEmpty()) {
			query.append("\nHAVING");
			boolean isFirstHaving = true;
			for (Map.Entry<Item, String> entry : havingMap.entrySet()) {
				if (isFirstHaving) {
					query.append(" ");
					isFirstHaving = false;
				} else {
					query.append(", ");
				}
				Item column = entry.getKey();
				if (groupByAggregateMap.get(column) != null) {
					query.append(groupByAggregateMap.get(column).toString() + "(");
				}
				String alias = tableAliasMap.get(column.getParent().getParent());
				if (alias == null) {
					alias = column.getParent().getParent().getName();
				}
				query.append(alias + "." + column.getName() + " ");
				if (groupByAggregateMap.get(column) != null) {
					query.append(")");
				}
				query.append(entry.getValue());
			}
			query.append(" ");
		}
		
		if (!orderByArgumentMap.isEmpty()) {
			query.append("\nORDER BY");
			boolean isFirstOrder = true;
			for (Item col : orderByList) {
				if (isFirstOrder) {
					query.append(" ");
					isFirstOrder = false;
				} else {
					query.append(", ");
				}
				String alias = tableAliasMap.get(col.getParent().getParent());
				if (alias == null) {
					alias = col.getParent().getParent().getName();
				}
				if (groupByAggregateMap.containsKey(col)) {
					query.append(groupByAggregateMap.get(col) + "(");
				}
				query.append(alias + "." + col.getName());
				if (groupByAggregateMap.containsKey(col)) {
					query.append(")");
				}
				query.append(" ");
				if (orderByArgumentMap.get(col) != null) {
					query.append(orderByArgumentMap.get(col).toString() + " ");
				}
			}
		}
		logger.debug(" Query is : " + query.toString());
		return query.toString();
	}

	public void listenToCellRenderer(ComponentCellRenderer renderer) {
		unlistenToCellRenderer();
		cellRenderer = renderer;
		renderer.addGroupAndHavingListener(groupByAndHavingListener);
		renderer.addTableListenerToSortDecorator(orderByListener);
		renderer.getTable().getColumnModel().addColumnModelListener(reorderSelectionByHeaderListener);
		renderer.getTable().getTableHeader().addMouseListener(reorderSelectionByHeaderMouseListener);
	}
	
	public void unlistenToCellRenderer() {
		if (cellRenderer != null) {
			cellRenderer.removeGroupAndHavingListener(groupByAndHavingListener);
			cellRenderer.removeTableListenerToSortDecorator(orderByListener);
			cellRenderer.getTable().getColumnModel().removeColumnModelListener(reorderSelectionByHeaderListener);
			cellRenderer.getTable().getTableHeader().removeMouseListener(reorderSelectionByHeaderMouseListener);
		}
	}
	
	public void addQueryChangeListener(ChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeLister(ChangeListener l) {
		queryChangeListeners.remove(l);
	}

	public List<Item> getSelectedColumns() {
		return Collections.unmodifiableList(selectedColumns);
	}

	/**
	 * Returns the grouping function if the column is being aggregated on
	 * or null otherwise.
	 */
	public SQLGroupFunction getGroupByAggregate(Item column) {
		return groupByAggregateMap.get(column);
	}
	
	/**
	 * Returns the having clause of a specific column if it has text. Returns
	 * null otherwise.
	 * @param column
	 * @return
	 */
	public String getHavingClause(Item column) {
		return havingMap.get(column);
	}

	public OrderByArgument getOrderByArgument(Item column) {
		return orderByArgumentMap.get(column);
	}

	public List<Item> getOrderByList() {
		return Collections.unmodifiableList(orderByList);
	}
	
	/**
	 * This method will change the selection of a column.
	 * 
	 * Package private for testing
	 */
	void selectionChanged(ItemPNode itemNode, Boolean isSelected) {
		Item column = itemNode.getItem();

		if (isSelected.equals(true)) {
			selectedColumns.add(column);
			if (groupingEnabled) {
				groupByList.add(column);
			}
			logger.debug("Added " + column.getName() + " to the column list");
		} else if (isSelected.equals(false)) {
			removeColumnSelection(column);
		}
		logger.debug("Firing change for selection.");
		for (ChangeListener l : queryChangeListeners) {
			l.stateChanged(new ChangeEvent(QueryCache.this));
		}
	}
	
	/**
	 * This method will change the alias on a column to the alias that is stored in the ItemPNode.
	 * 
	 * Package private for testing.
	 */
	void aliasChanged(ItemPNode itemNode) {
		Item column = itemNode.getItem();
		if (itemNode.getAlias().length() > 0) {
			aliasMap.put(column, itemNode.getAlias());
			logger.debug("Put " + column.getName() + " and " + itemNode.getAlias() + " in the alias map.");
		} else {
			aliasMap.remove(column);
		}
		for (ChangeListener l : queryChangeListeners) {
			l.stateChanged(new ChangeEvent(QueryCache.this));
		}
	}

	Map<Item, String> getAliasList() {
		return Collections.unmodifiableMap(aliasMap);
	}

	protected Map<Item, String> getAliasMap() {
		return aliasMap;
	}

	protected boolean isGroupingEnabled() {
		return groupingEnabled;
	}

	protected Map<Item, SQLGroupFunction> getGroupByAggregateMap() {
		return groupByAggregateMap;
	}

	protected List<Item> getGroupByList() {
		return groupByList;
	}

	protected Map<Item, String> getHavingMap() {
		return havingMap;
	}

	protected Map<Item, OrderByArgument> getOrderByArgumentMap() {
		return orderByArgumentMap;
	}

	protected List<Container> getFromTableList() {
		return fromTableList;
	}

	protected Map<Container, List<SQLJoin>> getJoinMapping() {
		return joinMapping;
	}

	protected Map<Item, String> getWhereMapping() {
		return whereMapping;
	}

	protected String getGlobalWhereClause() {
		return globalWhereClause;
	}

	protected Map<Container, String> getTableAliasMap() {
		return tableAliasMap;
	}

}
