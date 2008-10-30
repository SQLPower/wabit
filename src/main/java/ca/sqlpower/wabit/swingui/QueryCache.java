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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.swingui.table.TableModelSortDecorator;
import ca.sqlpower.wabit.swingui.querypen.ItemPNode;
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
	private Map<SQLColumn, String> aliasMap;
	
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
	private Map<SQLColumn, SQLGroupFunction> groupByAggregateMap;
	
	/**
	 * A list of columns we are grouping by. These are not
	 * being aggregated on but are in the GROUP BY clause. 
	 */
	private List<SQLColumn> groupByList;
	
	/**
	 * A listener that handles changes to the group by and having clauses.
	 */
	private PropertyChangeListener groupByAndHavingListener = new PropertyChangeListener() {
	
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(ComponentCellRenderer.PROPERTY_GROUP_BY)) {
				SQLColumn column = selectedColumns.get(cellRenderer.getComboBoxes().indexOf((JComboBox)e.getSource()));
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
				SQLColumn column = selectedColumns.get(indexOfTextField);
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
				SQLColumn column = selectedColumns.get(i);
				if ((sortStatus == TableModelSortDecorator.NOT_SORTED && orderByArgumentMap.get(column) == null)
						|| (sortStatus == TableModelSortDecorator.ASCENDING && orderByArgumentMap.get(column) == OrderByArgument.ASC)
						|| (sortStatus == TableModelSortDecorator.DESCENDING && orderByArgumentMap.get(column) == OrderByArgument.DESC)) {
					continue;
				}
				sortChanged = true;
				if (sortStatus == TableModelSortDecorator.NOT_SORTED) {
					orderByArgumentMap.remove(column);
					orderByList.remove(column);
				} else if (sortStatus == TableModelSortDecorator.ASCENDING) {
					orderByArgumentMap.put(column, OrderByArgument.ASC);
					orderByList.add(column);
				} else if (sortStatus == TableModelSortDecorator.DESCENDING) {
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
			if (e.getSource() instanceof ItemPNode) {
				ItemPNode itemNode = (ItemPNode)e.getSource();
				if (itemNode.getItem().getItem() instanceof SQLColumn) {
					SQLColumn column = (SQLColumn) itemNode.getItem().getItem();
					if (itemNode.getAlias().length() > 0) {
						aliasMap.put(column, itemNode.getAlias());
						logger.debug("Put " + column.getName() + " and " + itemNode.getAlias() + " in the alias map.");
					} else {
						aliasMap.remove(column);
					}
				}
			}
		}
	};
	
	/**
	 * The columns in the SELECT statement that will be returned.
	 * These columns are stored in the order they will be returned
	 * in.
	 */
	private final List<SQLColumn> selectedColumns;
	
	/**
	 * This map contains the columns that have an ascending
	 * or descending argument and is in the order by clause.
	 */
	private final Map<SQLColumn, OrderByArgument> orderByArgumentMap;
	
	/**
	 * The order by list keeps track of the order that columns were selected in.
	 */
	private final List<SQLColumn> orderByList;
	
	/**
	 * Listens for changes to the select checkbox on the column of a table in 
	 * the query pen.
	 */
	private PropertyChangeListener selectedColumnListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getSource() instanceof ItemPNode) {
				ItemPNode itemNode = (ItemPNode)e.getSource();
				if (itemNode.getItem().getItem() instanceof SQLColumn) {
					SQLColumn column = (SQLColumn) itemNode.getItem().getItem();
					if (e.getPropertyName().equals(ItemPNode.PROPERTY_SELECTED) && e.getNewValue().equals(true)) {
						selectedColumns.add(column);
						if (groupingEnabled) {
							groupByList.add(column);
						}
						logger.debug("Added " + column.getName() + " to the column list");
					} else if (e.getPropertyName().equals(ItemPNode.PROPERTY_SELECTED) && e.getNewValue().equals(false)) {
						selectedColumns.remove(column);
						aliasMap.remove(column);
						groupByList.remove(column);
						groupByAggregateMap.remove(column);
						havingMap.remove(column);
					}
				}
			}
		}
	};
	
	/**
	 * This is the current cell renderer we are listening on for group by 
	 * and having values.
	 */
	private ComponentCellRenderer cellRenderer;

	/**
	 * This maps SQLColumns to having clauses. The entry with a null key
	 * contains the generic having clause that is not defined for a specific
	 * column.
	 */
	private Map<SQLColumn, String> havingMap;
	
	private final QueryPen pen;
	
	/**
	 * These listeners will fire an event whenever the query has changed.
	 */
	private final List<ChangeListener> queryChangeListeners;
	
	public QueryCache(QueryPen pen) {
		this.pen = pen;
		orderByArgumentMap = new HashMap<SQLColumn, OrderByArgument>();
		orderByList = new ArrayList<SQLColumn>();
		selectedColumns = new ArrayList<SQLColumn>();
		queryChangeListeners = new ArrayList<ChangeListener>();
		aliasMap = new HashMap<SQLColumn, String>();
		groupByAggregateMap = new HashMap<SQLColumn, SQLGroupFunction>();
		groupByList = new ArrayList<SQLColumn>();
		havingMap = new HashMap<SQLColumn, String>();
		
		pen.addQueryListener(aliasListener);
		pen.addQueryListener(selectedColumnListener);
	}
	
	public void setGroupingEnabled(boolean enabled) {
		logger.debug("Setting grouping enabled to " + enabled);
		if (!groupingEnabled && enabled) {
			for (SQLColumn col : selectedColumns) {
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
	 * Generates the query based on the cache.
	 * 
	 * TODO: Store the select, from, and where portions
	 * in the cache and remove the string parameter from this method.
	 */
	public String generateQuery() {
		if (selectedColumns.size() ==  0) {
			return "";
		}
		StringBuffer query = new StringBuffer();
		query.append("SELECT");
		boolean isFirstSelect = true;
		for (SQLColumn col : selectedColumns) {
			if (isFirstSelect) {
				query.append(" ");
				isFirstSelect = false;
			} else {
				query.append(", ");
			}
			if (groupByAggregateMap.containsKey(col)) {
				query.append(groupByAggregateMap.get(col).toString() + "(");
			}
			query.append(col.getParentTable().getName() + "." + col.getName());
			if (groupByAggregateMap.containsKey(col)) {
				query.append(")");
			}
			if (aliasMap.get(col) != null) {
				query.append(" AS " + aliasMap.get(col));
			}
		}
		query.append(pen.createQueryString());
		if (!groupByList.isEmpty()) {
			query.append("\nGROUP BY");
			boolean isFirstGroupBy = true;
			for (SQLColumn col : groupByList) {
				if (isFirstGroupBy) {
					query.append(" ");
					isFirstGroupBy = false;
				} else {
					query.append(", ");
				}
				query.append(col.getParentTable().getName() + "." + col.getName());
			}
			query.append(" ");
		}
		if (!havingMap.isEmpty()) {
			query.append("\nHAVING");
			boolean isFirstHaving = true;
			for (Map.Entry<SQLColumn, String> entry : havingMap.entrySet()) {
				if (isFirstHaving) {
					query.append(" ");
					isFirstHaving = false;
				} else {
					query.append(", ");
				}
				SQLColumn column = entry.getKey();
				if (groupByAggregateMap.get(column) != null) {
					query.append(groupByAggregateMap.get(column).toString() + "(");
				}
				query.append(column.getParentTable().getName() + "." + column.getName() + " ");
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
			for (SQLColumn col : orderByList) {
				if (isFirstOrder) {
					query.append(" ");
					isFirstOrder = false;
				} else {
					query.append(", ");
				}
				query.append(col.getParentTable().getName() + "." + col.getName() + " ");
				if (orderByArgumentMap.get(col) != null) {
					query.append(orderByArgumentMap.get(col).toString() + " ");
				}
			}
		}
		logger.debug(" Query is : " + query.toString());
		return query.toString();
	}

	public void listenToCellRenderer(ComponentCellRenderer renderer) {
		cellRenderer = renderer;
		renderer.addGroupAndHavingListener(groupByAndHavingListener);
		renderer.addTableListenerToSortDecorator(orderByListener);
	}
	
	public void addQueryChangeListener(ChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeLister(ChangeListener l) {
		queryChangeListeners.remove(l);
	}

	public List<SQLColumn> getSelectedColumns() {
		return Collections.unmodifiableList(selectedColumns);
	}

	/**
	 * Returns the grouping function if the column is being aggregated on
	 * or null otherwise.
	 */
	public SQLGroupFunction getGroupByAggregate(SQLColumn column) {
		return groupByAggregateMap.get(column);
	}
	
	/**
	 * Returns the having clause of a specific column if it has text. Returns
	 * null otherwise.
	 * @param column
	 * @return
	 */
	public String getHavingClause(SQLColumn column) {
		return havingMap.get(column);
	}

	public OrderByArgument getOrderByArgument(SQLColumn column) {
		return orderByArgumentMap.get(column);
	}

}
