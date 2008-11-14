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

package ca.sqlpower.wabit.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SQLGroupFunction;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitChildListener;
import ca.sqlpower.wabit.WabitObject;

/**
 * This class will cache all of the parts of a select
 * statement and also listen to everything that could
 * change the select statement.
 */
public class QueryCache implements Query {
	
	private static final Logger logger = Logger.getLogger(QueryCache.class);
	
	/**
	 * A property name that is thrown in PropertyChangeListeners when part of
	 * the query has changed. This is a generic default change to a query
	 * rather than a specific query change.
	 */
	private static final String PROPERTY_QUERY = "PROPERTY_QUERY";
	
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
	 * This graph represents the tables in the SQL statement. Each table in
	 * the statement is a vertex in the graph. Each join is an edge in the 
	 * graph coming from the left table and moving towards the right table.
	 */
	private class TableJoinGraph implements GraphModel<Container, SQLJoin> {

		public Collection<Container> getAdjacentNodes(Container node) {
			List<Container> adjacencyNodes = new ArrayList<Container>();
			if (joinMapping.get(node) != null) {
				for (SQLJoin join : joinMapping.get(node)) {
					if (join.getLeftColumn().getParent().getParent() == node) {
						adjacencyNodes.add(join.getRightColumn().getParent().getParent());
					}
				}
			}
			return adjacencyNodes;
		}

		public Collection<SQLJoin> getEdges() {
			List<SQLJoin> edgesList = new ArrayList<SQLJoin>();
			for (List<SQLJoin> joinList : joinMapping.values()) {
				for (SQLJoin join : joinList) {
					edgesList.add(join);
				}
			}
			return edgesList;
		}

		public Collection<SQLJoin> getInboundEdges(Container node) {
			List<SQLJoin> inboundEdges = new ArrayList<SQLJoin>();
			if (joinMapping.get(node) != null) {
				for (SQLJoin join : joinMapping.get(node)) {
					if (join.getRightColumn().getParent().getParent() == node) {
						inboundEdges.add(join);
					}
				}
			}
			return inboundEdges;
		}

		public Collection<Container> getNodes() {
			return fromTableList;
		}

		public Collection<SQLJoin> getOutboundEdges(Container node) {
			List<SQLJoin> outboundEdges = new ArrayList<SQLJoin>();
			if (joinMapping.get(node) != null) {
				for (SQLJoin join : joinMapping.get(node)) {
					if (join.getLeftColumn().getParent().getParent() == node) {
						outboundEdges.add(join);
					}
				}
			}
			return outboundEdges;
		}
		
	}
	
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
	 * Listens for changes to the alias on the item and fires events to its 
	 * listeners if the alias was changed.
	 */
	private PropertyChangeListener aliasListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Item.PROPERTY_ALIAS)) {
				Item column = (Item)e.getSource();
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, column.getAlias(), column.getAlias()));
					}
				}
			}
		}
	};
	
	/**
	 * Listens for changes to the select checkbox on the column of a table in 
	 * the query pen.
	 */
	private PropertyChangeListener selectedColumnListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Item.PROPERTY_SELECTED)) {
				selectionChanged((Item)e.getSource(), (Boolean)e.getNewValue());
			}
		}
	};
	
	private PropertyChangeListener joinChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SQLJoin.LEFT_JOIN_CHANGED)) {
				logger.debug("Got left join changed.");
				SQLJoin changedJoin = (SQLJoin) e.getSource();
				Container leftJoinContainer = changedJoin.getLeftColumn().getParent().getParent();
				for (SQLJoin join : joinMapping.get(leftJoinContainer)) {
					if (join.getLeftColumn().getParent().getParent() == leftJoinContainer) {
						join.setLeftColumnOuterJoin((Boolean)e.getNewValue());
					} else {
						join.setRightColumnOuterJoin((Boolean)e.getNewValue());
					}
				}
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(e);
					}
				}
			} else if (e.getPropertyName().equals(SQLJoin.RIGHT_JOIN_CHANGED)) {
				logger.debug("Got right join changed.");
				SQLJoin changedJoin = (SQLJoin) e.getSource();
				Container rightJoinContainer = changedJoin.getRightColumn().getParent().getParent();
				logger.debug("There are " + joinMapping.get(rightJoinContainer) + " joins on the table with the changed join.");
				for (SQLJoin join : joinMapping.get(rightJoinContainer)) {
					if (join.getLeftColumn().getParent().getParent() == rightJoinContainer) {
						logger.debug("Changing left side");
						join.setLeftColumnOuterJoin((Boolean)e.getNewValue());
					} else {
						logger.debug("Changing right side");
						join.setRightColumnOuterJoin((Boolean)e.getNewValue());
					}
				}
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(e);
					}
				}
			} else if (e.getPropertyName().equals(SQLJoin.COMPARATOR_CHANGED)) {
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(e);
					}
				}
			}
		}
	};
	
	private final PropertyChangeListener whereListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Item.PROPERTY_WHERE)) {
				Item item = (Item) e.getSource();
				if (e.getNewValue() != null && ((String)e.getNewValue()).trim().length() > 0) {
					whereMapping.put(item, (String)e.getNewValue());
				} else {
					whereMapping.remove(item);
				}
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(e);
					}
				}
			} else if (e.getPropertyName().equals(Container.PROPERTY_WHERE_MODIFIED)) {
				setGlobalWhereClause((String)e.getNewValue());
			}
		}
	};
	
	private final PropertyChangeListener tableAliasListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Container.CONTAINTER_ALIAS_CHANGED)) {
				Container pane = (Container)e.getSource();
				if (pane.getAlias() == null || pane.getAlias().length() <= 0) {
					tableAliasMap.remove(pane);
				} else {
					tableAliasMap.put(pane, pane.getAlias());
				}
				if (!compoundEdit) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(e);
					}
				}
			}
		}
	};
	
	/**
	 * This container holds the items that are considered constants in the SQL statement.
	 * This could include functions or other elements that don't belong in a table.
	 */
	private final Container constantsContainer;
	
	/**
	 * These listeners will fire an event whenever the query has changed.
	 */
	private final List<PropertyChangeListener> queryChangeListeners;

	/**
	 * The user defined name of the query this QueryCache represents
	 */
	private String name;

	/**
	 * The parent that contains this QueryCache.
	 */
	private WabitObject parent;

	/**
	 * If true the query cache will be in an editing state. When in this
	 * state events should not be fired. When the compound edit ends
	 * the query should fire a state changed if the query was changed.
	 */
	private boolean compoundEdit = false;

	/**
	 * Stores the current query at the start of a compound edit. For use
	 * when deciding how the query was changed.
	 */
	private String queryBeforeEdit;
	
	/**
	 * This is the currently selected data source for the query. This is the 
	 * datasource the queries will be executed on.
	 */
	private SPDataSource dataSource;
	
	public QueryCache() {
		tableAliasMap = new HashMap<Container, String>();
		orderByArgumentMap = new HashMap<Item, OrderByArgument>();
		orderByList = new ArrayList<Item>();
		selectedColumns = new ArrayList<Item>();
		fromTableList = new ArrayList<Container>();
		joinMapping = new HashMap<Container, List<SQLJoin>>();
		whereMapping = new HashMap<Item, String>();
		queryChangeListeners = new ArrayList<PropertyChangeListener>();
		groupByAggregateMap = new HashMap<Item, SQLGroupFunction>();
		groupByList = new ArrayList<Item>();
		havingMap = new HashMap<Item, String>();
		
		constantsContainer = new ItemContainer("Constants");
	}
	
	/**
	 * A copy constructor for the query cache. This will not
	 * hook up listeners.
	 */
	public QueryCache(QueryCache copy) {
		selectedColumns = new ArrayList<Item>();
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
		
		name = copy.getName();
		queryChangeListeners = new ArrayList<PropertyChangeListener>();
		constantsContainer = copy.getConstantsContainer();
	}
	
	public void setGroupingEnabled(boolean enabled) {
		logger.debug("Setting grouping enabled to " + enabled);
		if (!groupingEnabled && enabled) {
			for (Item col : selectedColumns) {
				if (!groupByAggregateMap.containsKey(col)) {
					groupByList.add(col);
				}
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
			if (groupByAggregateMap.containsKey(col)) {
				query.append(groupByAggregateMap.get(col).toString() + "(");
			}
			String alias = tableAliasMap.get(col.getParent().getParent());
			if (alias != null) {
				query.append(alias + ".");
			} else if (fromTableList.contains(col.getParent().getParent())) {
				query.append(col.getParent().getParent().getName() + ".");
			}
			query.append(col.getName());
			if (groupByAggregateMap.containsKey(col)) {
				query.append(")");
			}
			if (col.getAlias() != null && col.getAlias().trim().length() > 0) {
				query.append(" AS " + col.getAlias());
			}
		}
		if (!fromTableList.isEmpty()) {
			query.append(" \nFROM");
		}
		boolean isFirstFrom = true;
		
		DepthFirstSearch<Container, SQLJoin> dfs = new DepthFirstSearch<Container, SQLJoin>();
		dfs.performSearch(new TableJoinGraph());
		Container previousTable = null;
		for (Container table : dfs.getFinishOrder()) {
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
				boolean joinFound = false;
				if (previousTable != null && joinMapping.get(table) != null) {
					for (SQLJoin join : joinMapping.get(table)) {
						if (join.getLeftColumn().getParent().getParent() == previousTable) {
							joinFound = true;
							if (join.isLeftColumnOuterJoin() && join.isRightColumnOuterJoin()) {
								query.append(" \nFULL OUTER JOIN ");
							} else if (join.isLeftColumnOuterJoin() && !join.isRightColumnOuterJoin()) {
								query.append(" \nLEFT OUTER JOIN ");
							} else if (!join.isLeftColumnOuterJoin() && join.isRightColumnOuterJoin()) {
								query.append(" \nRIGHT OUTER JOIN ");
							} else {
								query.append(" \nINNER JOIN ");
							}
							break;
						}
					}
				}
				if (!joinFound) {
					query.append(" \nINNER JOIN ");
				}
				query.append(qualifiedName + " " + alias + " \n  ON ");
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
						for (int i = 0; i < dfs.getFinishOrder().indexOf(table); i++) {
							if (otherColumn.getParent().getParent() == dfs.getFinishOrder().get(i)) {
								if (isFirstJoin) {
									isFirstJoin = false;
								} else {
									query.append(" \n    AND ");
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
					if (isFirstJoin) {
						query.append("TRUE");
					}
				}
			}
			previousTable = table;
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
					if (alias != null) {
						query.append(alias + ".");
					} else if (fromTableList.contains(entry.getKey().getParent().getParent())) {
						query.append(entry.getKey().getParent().getParent().getName() + ".");
					}
					query.append(entry.getKey().getName() + " " + entry.getValue());
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
				if (alias != null) {
					query.append(alias + ".");
				} else if (fromTableList.contains(col.getParent().getParent())) {
					query.append(col.getParent().getParent().getName() + ".");
				}
				query.append(col.getName());
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
				if (alias != null) {
					query.append(alias + ".");
				} else if (fromTableList.contains(column.getParent().getParent())) {
					query.append(column.getParent().getParent().getName() + ".");
				}
				query.append(column.getName());
				if (groupByAggregateMap.get(column) != null) {
					query.append(")");
				}
				query.append(" ");
				query.append(entry.getValue());
			}
			query.append(" ");
		}
		
		if (!orderByArgumentMap.isEmpty()) {
			query.append("\nORDER BY");
			boolean isFirstOrder = true;
			for (Item col : orderByList) {
				if (col instanceof StringItem) {
					continue;
				}
				if (isFirstOrder) {
					query.append(" ");
					isFirstOrder = false;
				} else {
					query.append(", ");
				}
				if (groupByAggregateMap.containsKey(col)) {
					query.append(groupByAggregateMap.get(col) + "(");
				}
				String alias = tableAliasMap.get(col.getParent().getParent());
				if (alias != null) {
					query.append(alias + ".");
				} else if (fromTableList.contains(col.getParent().getParent())) {
					query.append(col.getParent().getParent().getName() + ".");
				}
				query.append(col.getName());
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

	public void addPropertyChangeListener(PropertyChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
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
	 */
	public void selectionChanged(Item column, Boolean isSelected) {
		if (isSelected.equals(true)) {
			selectedColumns.add(column);
			if (groupingEnabled) {
				if (column instanceof StringItem) {
					groupByAggregateMap.put(column, SQLGroupFunction.COUNT);
				} else {
					groupByList.add(column);
				}
			}
			logger.debug("Added " + column.getName() + " to the column list");
		} else if (isSelected.equals(false)) {
			removeColumnSelection(column);
		}
		logger.debug("Firing change for selection.");
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, column, column));
			}
		}
	}
	
	public void removeTable(Container table) {
		fromTableList.remove(table);
		tableAliasMap.remove(table);
		table.removeChangeListener(tableAliasListener);
		for (Section section : table.getSections()) {
			for (Item col : section.getItems()) {
				removeItem(col);
			}
		}
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, table, null));
			}
		}
	}

	public void addTable(Container container) {
		fromTableList.add(container);
		container.addChangeListener(tableAliasListener);
		for (Section section : container.getSections()) {
			for (Item col : section.getItems()) {
				addItem(col);
			}
		}
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, null, container));
			}
		}
	}
	
	/**
	 * This setter will fire a property change event.
	 */
	public void setGlobalWhereClause(String whereClause) {
		globalWhereClause = whereClause;
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, whereClause, whereClause));
			}
		}
	}
	
	public void removeJoin(SQLJoin joinLine) {
		joinLine.removeJoinChangeListener(joinChangeListener);
		Item leftColumn = joinLine.getLeftColumn();
		Item rightColumn = joinLine.getRightColumn();

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
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, joinLine, joinLine));
			}
		}
	}

	public void addJoin(SQLJoin join) {
		join.addJoinChangeListener(joinChangeListener);
		Item leftColumn = join.getLeftColumn();
		Item rightColumn = join.getRightColumn();
		Container leftContainer = leftColumn.getParent().getParent();
		Container rightContainer = rightColumn.getParent().getParent();
		if (joinMapping.get(leftContainer) == null) {
			List<SQLJoin> joinList = new ArrayList<SQLJoin>();
			joinList.add(join);
			joinMapping.put(leftContainer, joinList);
		} else {
			if (joinMapping.get(leftContainer).size() > 0) {
				SQLJoin prevJoin = joinMapping.get(leftContainer).get(0);
				if (prevJoin.getLeftColumn().getParent().getParent() == leftContainer) {
					join.setLeftColumnOuterJoin(prevJoin.isLeftColumnOuterJoin());
				} else if (prevJoin.getRightColumn().getParent().getParent() == leftContainer) {
					join.setLeftColumnOuterJoin(prevJoin.isRightColumnOuterJoin());
				}
			}
				
			joinMapping.get(leftContainer).add(join);
		}

		if (joinMapping.get(rightContainer) == null) {
			List<SQLJoin> joinList = new ArrayList<SQLJoin>();
			joinList.add(join);
			joinMapping.put(rightContainer, joinList);
		} else {
			if (joinMapping.get(rightContainer).size() > 0) {
				SQLJoin prevJoin = joinMapping.get(rightContainer).get(0);
				if (prevJoin.getLeftColumn().getParent().getParent() == rightContainer) {
					join.setRightColumnOuterJoin(prevJoin.isLeftColumnOuterJoin());
				} else if (prevJoin.getRightColumn().getParent().getParent() == rightContainer) {
					join.setRightColumnOuterJoin(prevJoin.isRightColumnOuterJoin());
				} else {
					throw new IllegalStateException("A table contains a join that is not connected to any of its columns in the table.");
				}
			}
			joinMapping.get(rightContainer).add(join);
		}
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, null, join));
			}
		}
	}
	
	/**
	 * This removes the item from all lists it could be
	 * contained in as well as disconnect its listeners.
	 */
	public void removeItem(Item col) {
		logger.debug("Item name is " + col.getName());
		whereMapping.remove(col);
		col.removeChangeListener(aliasListener);
		col.removeChangeListener(selectedColumnListener);
		col.removeChangeListener(whereListener);
		removeColumnSelection(col);
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, col, null));
			}
		}
	}
	
	/**
	 * This adds the appropriate listeners to the new Item.
	 */
	public void addItem(Item col) {
		col.addChangeListener(aliasListener);
		col.addChangeListener(selectedColumnListener);
		col.addChangeListener(whereListener);
	}
	
	/**
	 * This aggregate is either the toString of a SQLGroupFunction or the
	 * string GROUP_BY defined in this class.
	 */
	public void setGrouping(Item column, String groupByAggregate) {
		if (groupByAggregate.equals(GROUP_BY)) {
			if (groupByList.contains(column)) {
				return;
			}
			groupByList.add(column);
			groupByAggregateMap.remove(column);
			logger.debug("Added " + column.getName() + " to group by list.");
		} else {
			if (SQLGroupFunction.valueOf(groupByAggregate).equals(groupByAggregateMap.get(column))) {
				return;
			}
			groupByAggregateMap.put(column, SQLGroupFunction.valueOf(groupByAggregate));
			groupByList.remove(column);
			logger.debug("Added " + column.getName() + " with aggregate " + groupByAggregate + " to aggregate group by map.");
		}
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(this, PROPERTY_QUERY, groupByAggregate, groupByAggregate));
			}
		}		

	}
	
	public void removeSort(Item item) {
		orderByList.remove(item);
		orderByArgumentMap.remove(item);
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, null, null));
			}
		}
	}
	
	public void setSortOrder(Item item, OrderByArgument arg) {
		removeSort(item);
		orderByArgumentMap.put(item, arg);
		orderByList.add(item);
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, null, null));
			}
		}
	}
	
	public void setHavingClause(Item item, String havingText) {
		if (havingText != null && havingText.length() > 0) {
			if (!havingText.equals(havingMap.get(item))) {
				havingMap.put(item, havingText);
			}
		} else {
			havingMap.remove(item);
		}
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(this, PROPERTY_QUERY, havingText, havingText));
			}
		}
	}
	
	public void moveItem(Item movedColumn, int toIndex) {
		selectedColumns.remove(movedColumn);
		selectedColumns.add(toIndex, movedColumn);
		if (!compoundEdit) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(QueryCache.this, PROPERTY_QUERY, movedColumn, movedColumn));
			}
		}
	}
	
	public void startCompoundEdit() {
		compoundEdit = true;
		queryBeforeEdit = generateQuery();
	}
	
	public void endCompoundEdit() {
		compoundEdit = false;
		String currentQuery = generateQuery();
		if (!currentQuery.equals(queryBeforeEdit)) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(this, PROPERTY_QUERY, queryBeforeEdit, currentQuery));
			}
		}
		queryBeforeEdit = "";
	}

	public boolean isGroupingEnabled() {
		return groupingEnabled;
	}

	public Map<Item, SQLGroupFunction> getGroupByAggregateMap() {
		return Collections.unmodifiableMap(groupByAggregateMap);
	}

	protected List<Item> getGroupByList() {
		return Collections.unmodifiableList(groupByList);
	}

	public Map<Item, String> getHavingMap() {
		return Collections.unmodifiableMap(havingMap);
	}

	public Map<Item, OrderByArgument> getOrderByArgumentMap() {
		return Collections.unmodifiableMap(orderByArgumentMap);
	}

	public List<Container> getFromTableList() {
		return Collections.unmodifiableList(fromTableList);
	}

	protected Map<Container, List<SQLJoin>> getJoinMapping() {
		return Collections.unmodifiableMap(joinMapping);
	}
	
	/**
	 * This returns the joins between tables. Each join will be
	 * contained only once.
	 */
	public Collection<SQLJoin> getJoins() {
		Set<SQLJoin> joinSet = new HashSet<SQLJoin>();
		for (List<SQLJoin> joins : joinMapping.values()) {
			for (SQLJoin join : joins) {
				joinSet.add(join);
			}
		}
		return joinSet;
	}

	protected Map<Item, String> getWhereMapping() {
		return Collections.unmodifiableMap(whereMapping);
	}

	public String getGlobalWhereClause() {
		return globalWhereClause;
	}

	protected Map<Container, String> getTableAliasMap() {
		return Collections.unmodifiableMap(tableAliasMap);
	}

	public void addChildListener(WabitChildListener l) {
		throw new IllegalStateException("There are no children of a QueryCache.");
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		throw new IllegalStateException("There are no children of a QueryCache.");
	}

	public List<? extends WabitObject> getChildren() {
		return null;
	}

	public String getName() {
		return name;
	}

	public WabitObject getParent() {
		return parent;
	}

	public void removeChildListener(WabitChildListener l) {
		throw new IllegalStateException("There are no children of a QueryCache.");
	}

	public void setParent(WabitObject parent) {
		this.parent = parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Container getConstantsContainer() {
		return constantsContainer;
	}
	
	public SPDataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(SPDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}
