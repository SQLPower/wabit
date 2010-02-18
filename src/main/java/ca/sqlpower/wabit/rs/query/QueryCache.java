/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.rs.query;

import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPSimpleVariableResolver;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.object.SPVariableResolverProvider;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.QueryChangeEvent;
import ca.sqlpower.query.QueryChangeListener;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.swingui.query.StatementExecutorListener;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.rs.ResultSetEvent;
import ca.sqlpower.wabit.rs.ResultSetHandle;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducerException;
import ca.sqlpower.wabit.rs.ResultSetProducerListener;
import ca.sqlpower.wabit.rs.ResultSetProducerSupport;
import ca.sqlpower.wabit.rs.WabitResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetHandle.ResultSetType;

/**
 * This method will be able to execute and cache the results of a query. It also
 * delegates some of the methods to the {@link QueryImpl} contained in it.
 */
public class QueryCache extends AbstractWabitObject implements StatementExecutor, Query, WabitResultSetProducer, SPVariableResolverProvider {
    
    private static final Logger logger = Logger.getLogger(QueryCache.class);
    
    private final QueryImpl query;
    
    @GuardedBy("rsps")
    private final ResultSetProducerSupport rsps = new ResultSetProducerSupport(this);
    
    /**
     * Tracks if the user should be prompted every time a query is going to
     * be executed and it contains cross joins. This property can be set from
     * the prompt itself or the query's settings.
     */
    private boolean promptForCrossJoins = true;

    /**
     * If the user has selected to no longer be prompted when executing queries
     * with cross joins this boolean will store true if queries with cross joins
     * should be executed and false otherwise. If the user is being prompted
     * when executing queries with cross joins the state of this value is not
     * defined.
     */
    private boolean executeQueriesWithCrossJoins;
    
    /**
     * If true the query should be executed every time there is a change to the
     * query. If false the query should only be executed by user request.
     */
    private boolean automaticallyExecuting = true;
    
    /**
     * These are the listeners that want to listen directly to the query that
     * this object delegates to. The events that get fired to this listener
     * should have this object as its source instead of the delegate.
     */
    @GuardedBy("queryListeners")
    private final List<QueryChangeListener> queryListeners = new ArrayList<QueryChangeListener>();

    /**
     * This listener will be added to the query and refire events to the
     * {@link #queryListeners} list with this object as the source.
     */
    private final QueryChangeListener queryChangeListener = new QueryChangeListener() {
    
        public void propertyChangeEvent(PropertyChangeEvent evt) {
            firePropertyChangeEvent(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void joinRemoved(QueryChangeEvent evt) {
            fireJoinRemoved(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void joinPropertyChangeEvent(PropertyChangeEvent evt) {
            fireJoinPropertyChangeEvent(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void joinAdded(QueryChangeEvent evt) {
            fireJoinAdded(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void itemRemoved(QueryChangeEvent evt) {
            fireItemRemoved(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void itemPropertyChangeEvent(PropertyChangeEvent evt) {
            fireItemPropertyChangeEvent(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void itemAdded(QueryChangeEvent evt) {
            fireItemAdded(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void containerRemoved(QueryChangeEvent evt) {
            fireContainerRemoved(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void containerAdded(QueryChangeEvent evt) {
            fireContainerAdded(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void compoundEditStarted(TransactionEvent evt) {
            fireCompoundEditStarted(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }
    
        public void compoundEditEnded(TransactionEvent evt) {
            fireCompoundEditEnded(evt);
            rsps.fireStructureChanged();
            updateVariables();
        }

    };
    
    private void updateVariables() {
    	if (variableProvider != null) {            	
        	variableProvider.setUpdateNeeded(true);
        }
    }
    
    /**
     * When the constants container is set in the query a WabitObject wrapper is 
     * set at this variable.
     */
    private WabitConstantsContainer constantContainer;
    
    /**
     * This is a variable resolver to allow this query to 
     * expose it's columns as variables to other objects.
     */
    private QueryVariableResolver variableProvider = null;
    
    /**
     * Extends {@link SPSimpleVariableResolver} to make sure that we
     * initialize the variables before trying to resolve them.
     */
    private final class QueryVariableResolver extends SPSimpleVariableResolver {
    	private boolean updateNeeded = true;
    	public QueryVariableResolver(SPObject owner, String namespace, String userFriendlyName) {
			super(owner, namespace, userFriendlyName);
		}
    	public void setUpdateNeeded(boolean updateNeeded) {
    		this.updateNeeded = updateNeeded;
    	}
    	protected void beforeLookups(String key) {
    		if (this.resolvesNamespace(SPVariableHelper.getNamespace(key))
    				&& this.updateNeeded) {
    			this.updateVars(true);
    		}
    	}
    	protected void beforeKeyLookup(String namespace) {
    		if (this.updateNeeded) {
    			this.updateVars(false);
    		}
    	}
		private void updateVars(boolean completeUpdate) {
			try {
				execute(
						new SPVariableHelper(QueryCache.this),
						new ResultSetListener() {
							public void newData(ResultSetEvent evt) {
								if (isStreaming()) {
									update(evt.getSourceHandle().getResultSet());
								}
							}
							public void executionStarted(ResultSetEvent evt) {
								// don't care.
							};
							public void executionComplete(ResultSetEvent evt) {
								update(evt.getResults());
							}
							private void update(ResultSet rs) {
								try {
									variables.clear();
									if (rs != null &&
											rs.first()) {
										do {
											for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
												QueryVariableResolver.this.store(rs.getMetaData().getColumnName(i+1), rs.getObject(i+1));
											}
										} while (rs.next());
									}
								} catch (SQLException e) {
									logger.error("Failed to resolve available variables from a query.", e);
								}
							}
						},
						completeUpdate);
				
			} catch (ResultSetProducerException e) {
				logger.error("Failed to resolve available variables from a query.", e);
			} finally {
				this.updateNeeded = false;
			}
		}
    }
    
    /**
     * When each container is added to a query cache a WabitObject wrapper is made for 
     * it and placed in this list to let this query fire Wabit events based on it.
     */
    private final List<WabitTableContainer> containers = new ArrayList<WabitTableContainer>();

    /** 
     * When a join is added to a query cache a WabitObject wrapper is made for it
     * and placed in this list to let the query fire Wabit events based on it.
     */
    private final List<WabitJoin> joins = new ArrayList<WabitJoin>();
    
	/**
	 * FIXME This enum defines the {@link WabitObject} child classes a
	 * {@link QueryCache} takes as well as the ordinal order of these child
	 * classes such that the class going before does not depend on the class
	 * that goes after. This is here temporarily, see bug 2327 for future enhancements.
	 * http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=2327
	 */
	public enum WabitObjectOrder {
		WABIT_CONSTANTS_CONTAINER(WabitConstantsContainer.class),
		WABIT_TABLE_CONTAINER(WabitTableContainer.class),
		WABIT_JOIN(WabitJoin.class);
		
		private final Class<? extends WabitObject> clazz;
		
		private WabitObjectOrder(Class<? extends WabitObject> clazz) {
			this.clazz = clazz;
		}
		
		public Class<? extends WabitObject> getChildClass() {
			return clazz;
		}
		
		public static WabitObjectOrder getOrderBySimpleClassName(String name) {
			for (WabitObjectOrder order : values()) {
				if (order.clazz.getSimpleName().equals(name)) {
					return order;
				}
			}
			throw new IllegalArgumentException("The WabitObject class \"" + name + 
					"\" does not exist or is not a child type of WabitWorkspace.");
		}
		
	}

    /**
     * This makes a copy of the given query cache. The query in the given query cache
     * has its listeners disconnected to prevent copies from being affected by user
     * actions. This also makes cleanup of copies easier.
     */
    public QueryCache(QueryCache q) {
        this(q, false);
    }
    
    /**
     * This makes a copy of the given query cache. The query in the given query cache
     * can have its listeners connected to allow using this query cache in the workspace.
     */
    public QueryCache(QueryCache q, boolean connectListeners) {
    	this.query = new QueryImpl(q.query, connectListeners);
        query.addQueryChangeListener(queryChangeListener);
        query.setUUID(getUUID());
        createWabitObjectWrappers(false);
    }
    
    /**
     * Creates a {@link Query} that caches the last set of results that were
     * returned when during the last execution.
     * 
     * @param dbMapping
     *            A mapping of {@link SPDataSource}s to corresponding
     *            {@link SQLDatabase}s that allows getting the connection pool
     *            in the {@link SQLDatabase} based on the connection defined by
     *            the {@link SPDataSource}.
     */
    public QueryCache(SQLDatabaseMapping dbMapping) {
        this(dbMapping, true);
    }

    /**
     * Creates a {@link Query} that caches the last set of results that were
     * returned when during the last execution.
     * 
     * @param dbMapping
     *            A mapping of {@link SPDataSource}s to corresponding
     *            {@link SQLDatabase}s that allows getting the connection pool
     *            in the {@link SQLDatabase} based on the connection defined by
     *            the {@link SPDataSource}.
     * @param prepopulateConstants
     *            True if the constants table should be pre-populated with some
     *            useful default constants, false otherwise.
     */
    public QueryCache(SQLDatabaseMapping dbMapping, boolean prepopulateConstants) {
    	this(dbMapping, prepopulateConstants, null);
    }
    	
    /**
     * Creates a {@link Query} that caches the last set of results that were
     * returned when during the last execution.
     *
     * @see QueryImpl the query implementation constructor with the same args
     */
    public QueryCache(SQLDatabaseMapping dbMapping, boolean prepopulateConstants, 
    		WabitConstantsContainer newConstantsContainer) {
    	this(dbMapping, prepopulateConstants, newConstantsContainer, null);
    }
    
    public QueryCache(SQLDatabaseMapping dbMapping, boolean prepopulateConstants,
    		WabitConstantsContainer newConstantsContainer, JDBCDataSource dataSource) {
    	
    	if (newConstantsContainer != null) {
    		query = new QueryImpl(dbMapping, prepopulateConstants, 
    				newConstantsContainer.getDelegate(), dataSource);
    	} else {
    		query = new QueryImpl(dbMapping, prepopulateConstants, null, dataSource);
    	}
        query.addQueryChangeListener(queryChangeListener);
        query.setUUID(getUUID());
        if (newConstantsContainer != null) {
        	constantContainer = newConstantsContainer;
        	newConstantsContainer.setParent(this);
        }
        createWabitObjectWrappers(newConstantsContainer != null);
    }

    /**
     * Helper method for the constructors. This creates WabitObjects based on
     * the current objects in the query set in the constructor.
     */
    private void createWabitObjectWrappers(boolean constantsWrapperExists) {
    	if (!constantsWrapperExists) {
    		WabitConstantsContainer constants = new WabitConstantsContainer(query.getConstantsContainer());
    		constants.setParent(this);
    		constantContainer = constants;
    	}
        for (Container c : query.getFromTableList()) {
            WabitTableContainer child = new WabitTableContainer(c);
            child.setParent(this);
			containers.add(child);
        }
        for (SQLJoin j : query.getJoins()) {
            WabitJoin child = new WabitJoin(this, j);
            child.setParent(this);
			joins.add(child);
        }
    }

 // ------------------ ResultSetProducer interface ----------------------
    public void addResultSetProducerListener(ResultSetProducerListener listener) {
        synchronized(rsps) {
            rsps.addResultSetListener(listener);
        }
    }

    public void removeResultSetProducerListener(ResultSetProducerListener listener) {
        synchronized(rsps) {
            rsps.removeResultSetListener(listener);
        }
    }
    
    public boolean isRunning() {
    	return rsps.isRunning();
    }
    
    

    public ResultSetHandle execute(
    		SPVariableHelper variableContext, 
    		ResultSetListener listener) throws ResultSetProducerException 
    {
        return execute(variableContext, listener, true);
    }

    public ResultSetHandle execute(
    		SPVariableHelper variableContext, 
    		ResultSetListener listener, 
    		boolean fetchFullResults) throws ResultSetProducerException 
    {
        
        if (query.getDatabase() == null || query.getDatabase().getDataSource() == null) {
            throw new NullPointerException("Data source is null.");
        }
        	
    	String sql = query.generateQuery();
    	
    	try {
        	
    		SPVariableHelper helper;	
    		if (variableContext != null) {
    			helper = variableContext;
    		} else {
    			helper = new SPVariableHelper(this);
    		}
    		
    		return rsps.execute(
    				this.getSession().getContext(),
    				this.getDataSource(),
    				sql,
    				helper,
    				isStreaming() ? ResultSetType.STREAMING : ResultSetType.RELATIONAL,
    				getStreamingRowLimit(),
    				listener);
    		
    	} catch (Exception t) {
    		throw new ResultSetProducerException(t);
    	}
    }

    /**
     * Cancels this query's execution if it is currently running. Cancellation
     * is not guaranteed to work perfectly, because it is partly the underlying
     * JDBC driver's responsibility to provide an effective implementation of
     * {@link Statement#cancel()}.
     */
    public void cancel() {
        this.rsps.cancel();
    }
    
    // ------------------ end ResultSetProducer interface ----------------------
    
    @Override
    public CleanupExceptions cleanup() {
        CleanupExceptions exceptions = new CleanupExceptions();
        try {
        	rsps.cancel();
        } catch (Exception e) {
        	exceptions.add(e);
        }
        return exceptions;
    }
    
    public WabitDataSource getWabitDataSource() {
        if (query.getDatabase() == null || query.getDatabase().getDataSource() == null) {
            return null;
        }
        return new WabitDataSource(query.getDatabase().getDataSource());
    }
    
    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        int offset = 0;
        if (childType.equals(WabitConstantsContainer.class)) {
            return offset;
        }
        offset += 1;
        if (childType.equals(WabitTableContainer.class)) {
            return offset;
        }
        offset += containers.size();
        if (childType.equals(WabitJoin.class)) {
            return offset;
        } 
        throw new IllegalArgumentException("Unknown child type " + childType + " for " + getName());
    }

    public List<? extends WabitObject> getChildren() {
        List<WabitObject> children = new ArrayList<WabitObject>();
        children.add(constantContainer);
        children.addAll(containers);
        children.addAll(joins);
        if (logger.isDebugEnabled()) {
            logger.debug("Children of query cache " + getName() + " are " + children);
        }
        return Collections.unmodifiableList(children);
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<WabitObject> getDependencies() {
        if (getWabitDataSource() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getWabitDataSource()));
    }
    
    public void removeDependency(SPObject dependency) {
        if (dependency.equals(getWabitDataSource())) {
            setDataSource(null);
        }
    }

    public void setPromptForCrossJoins(boolean promptForCrossJoins) {
        boolean oldPromptJoins = this.promptForCrossJoins;
        this.promptForCrossJoins = promptForCrossJoins;
        firePropertyChange("promptForCrossJoins", oldPromptJoins, promptForCrossJoins);
    }

    public boolean getPromptForCrossJoins() {
        return promptForCrossJoins;
    }

    public void setExecuteQueriesWithCrossJoins(boolean executeQueriesWithCrossJoins) {
        boolean oldExecWithJoin = this.executeQueriesWithCrossJoins;
        this.executeQueriesWithCrossJoins = executeQueriesWithCrossJoins;
        firePropertyChange("executeQueriesWithCrossJoins", oldExecWithJoin, executeQueriesWithCrossJoins);
    }

    public boolean getExecuteQueriesWithCrossJoins() {
        return executeQueriesWithCrossJoins;
    }

    public void setAutomaticallyExecuting(boolean automaticallyExecute) {
        boolean oldAutoExec = this.automaticallyExecuting;
        this.automaticallyExecuting = automaticallyExecute;
        firePropertyChange("automaticallyExecuting", oldAutoExec, automaticallyExecute);
    }

    public boolean isAutomaticallyExecuting() {
        return automaticallyExecuting;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof WabitConstantsContainer) {
            throw new IllegalStateException("Cannot remove the constants container from this query.");
        } else if (child instanceof WabitTableContainer) {
            query.removeTable(((WabitTableContainer) child).getDelegate());
            return true;
        } else if (child instanceof WabitJoin) {
            query.removeJoin(((WabitJoin) child).getDelegate());
            return true;
        }
        return false;
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof WabitConstantsContainer) {
            throw new IllegalArgumentException("Cannot change the constants table of a query.");
        } else if (child instanceof WabitTableContainer) {
            final WabitTableContainer container = (WabitTableContainer) child;
            addTable(container, index);
        } else if (child instanceof WabitJoin) {
            //Joins are added to a map not a list so their order is not defined.
            final WabitJoin join = (WabitJoin) child;
            addJoin(join);
        }
    }

    /**
     * This adds a table as a child to the query cache. This is done here
     * instead of by listening for events from the query as we need to add an
     * appropriate child to this object and tables can only be added from
     * outside of the query, not as a side effect of a change to the query.
     * 
     * @param container
     *            The container to add to the query. This container will be
     *            added as a child of the cache and its delegate will be added
     *            to the delegate query.
     * @param index
     *            The index to add the container at. This non-negative value
     *            cannot be greater than the number of containers currently in
     *            the query.
     */
    private void addTable(WabitTableContainer container, int index) {
        query.addTable(container.getDelegate(), index);
        containers.add(container);
        container.setParent(this);
        fireChildAdded(container.getClass(), container, index);
    }

    /**
     * This adds a join as a child to the query cache. This is done here instead
     * of by listening for events from the query as we need to add an
     * appropriate child to this object and joins can only be added from outside
     * the query, not as a side effect of a change to the query.
     * 
     * @param join
     *            The join to add to the query. The delegate of the join will be
     *            added to the delegate query of this class. The items in the
     *            join must be in this query.
     */
    private void addJoin(WabitJoin join) {
        query.addJoin(join.getDelegate());
        int index = joins.size();
        joins.add(join);
        join.setParent(this);
        fireChildAdded(join.getClass(), join, index);
    }

    
    //------------------- start Query interface---------------------------------
    
    public String getStatement() {
        return query.generateQuery();
    }
    
    public String generateQuery() {
        return query.generateQuery();
    }
    
    public boolean containsCrossJoins() {
        return query.containsCrossJoins();
    }

    public boolean isScriptModified() {
        return query.isScriptModified();
    }
    

    public void setDataSource(JDBCDataSource ds) {
    	JDBCDataSource oldValue = this.getDataSource();
    	WabitWorkspace workspace = WabitUtils.getWorkspace(this);
    	if (workspace != null && !workspace.isMagicEnabled()) {
    		if (!query.setDataSourceWithoutSideEffects(ds)) {
    			return;
    		}
    	} else {
    		query.setDataSource(ds);
    	}
		firePropertyChange("dataSource", oldValue, ds);
    }
    
    public boolean setDataSourceWithoutSideEffects(JDBCDataSource dataSource) {
    	JDBCDataSource oldValue = this.getDataSource();
    	boolean returnValue = query.setDataSourceWithoutSideEffects(dataSource);
    	if (returnValue) {
    		firePropertyChange("dataSource", oldValue, dataSource);
    	}
    	return returnValue;
    }
    
    public JDBCDataSource getDataSource() {
    	return query.getDataSource();
    }

    public void setName(String string) {
        query.setName(string);
        if (this.variableProvider != null) {
        	this.variableProvider.setUserFriendlyName("Relational Query - " + string);
        }
    }
    
    public String getName() {
        return query != null ? query.getName() : null;
    }
    
    public void setDBMapping(SQLDatabaseMapping dbMapping) {
        query.setDBMapping(dbMapping);
    }

    public int getStreamingRowLimit() {
        return query.getStreamingRowLimit();
    }

    public void setStreamingRowLimit(int streamingRowLimit) {
        query.setStreamingRowLimit(streamingRowLimit);
    }

    public boolean isStreaming() {
        return query.isStreaming();
    }
    
    /**
     * This will set the row limit on the query. This also clears the cache.
     */
    public void setRowLimit(int rowLimit) {
        query.setRowLimit(rowLimit);
    }
    
    public void addItem(Item col) {
        query.addItem(col);
    }

    public void addJoin(SQLJoin join) {
        addJoin(new WabitJoin(this, join));
    }

    public void addQueryChangeListener(QueryChangeListener l) {
        synchronized(queryListeners) {
            queryListeners.add(l);        
        }
    }

    public void addTable(Container container) {
        addTable(container, query.getFromTableList().size());
    }
    
    public void addTable(Container container, int index) {
        addTable(new WabitTableContainer(container), index);
    }

    public void setUserModifiedQuery(String query) {
        this.query.setUserModifiedQuery(query);
    }

    public String getUserModifiedQuery() {
    	return query.getUserModifiedQuery();
    }
    
    public void endCompoundEdit() {
        query.endCompoundEdit();
    }

    public Container getConstantsContainer() {
        return query.getConstantsContainer();
    }

    public SQLDatabase getDatabase() {
        return query.getDatabase();
    }

    public SQLDatabaseMapping getDBMapping() {
        return query.getDBMapping();
    }

    public List<Container> getFromTableList() {
        return query.getFromTableList();
    }

    public String getGlobalWhereClause() {
        return query.getGlobalWhereClause();
    }

    public Collection<SQLJoin> getJoins() {
        return query.getJoins();
    }

    public List<Item> getOrderByList() {
        return Collections.unmodifiableList(query.getOrderByList());
    }
    
    public int getRowLimit() {
        return query.getRowLimit();
    }

    public List<Item> getSelectedColumns() {
        return query.getSelectedColumns();
    }

    public int getZoomLevel() {
        return query.getZoomLevel();
    }

    public boolean isGroupingEnabled() {
        return query.isGroupingEnabled();
    }

    public void moveItem(Item movedColumn, int toIndex) {
        query.moveItem(movedColumn, toIndex);
    }
    
    public void moveOrderBy(Item item, int index) {
        query.moveOrderBy(item, index);
    }

    public void moveOrderByItemToEnd(Item item) {
        query.moveOrderByItemToEnd(item);
    }

    public void removeItem(Item col) {
        query.removeItem(col);
    }

    public void removeJoin(SQLJoin joinLine) {
        query.removeJoin(joinLine);
    }

    public void removeQueryChangeListener(QueryChangeListener l) {
        synchronized(queryListeners) {
            queryListeners.remove(l);
        }
    }

    public void removeTable(Container table) {
        query.removeTable(table);
    }

    public void removeUserModifications() {
        query.removeUserModifications();
    }

    public void reset() {
        query.reset();
    }

    public void setGlobalWhereClause(String whereClause) {
        query.setGlobalWhereClause(whereClause);
    }

    public void setGroupingEnabled(boolean enabled) {
        query.setGroupingEnabled(enabled);
    }

    public void setStreaming(boolean streaming) {
        query.setStreaming(streaming);
    }

    public void setZoomLevel(int zoomLevel) {
        query.setZoomLevel(zoomLevel);
    }

    public void startCompoundEdit(String message) {
        query.startCompoundEdit(message);
    }
    
    public int indexOfSelectedItem(Item item) {
        return query.indexOfSelectedItem(item);
    }
    
    public void selectItem(Item item) {
        query.selectItem(item);
    }
    
    public void unselectItem(Item item) {
        query.unselectItem(item);
    }
    
    public void orderColumn(Item item, OrderByArgument ordering) {
        query.orderColumn(item, ordering);
    }
    
    //------------------- end Query interface---------------------------------
    

    
    //------------------- event handling for query delegate-------------------
    
    protected void fireJoinAdded(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getJoinChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).joinAdded(newEvent);
                    }
                }
            }
        });
        //Joins added by the addJoin(WabitJoin) method.
    }

    protected void fireJoinRemoved(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getJoinChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).joinRemoved(newEvent);
                    }
                }
            }
        });
        
        WabitJoin joinToRemove = null;
        for (WabitJoin join : joins) {
        	if (join.getDelegate().equals(evt.getJoinChanged())) {
        		joinToRemove = join;
        		break;
        	}
        }
        if (joinToRemove == null) {
        	throw new IllegalStateException("The query has come out of sync with its delegate. " +
        			"Cannot find a join to match " + evt.getJoinChanged().getName());
        }
        
        int index = joins.indexOf(joinToRemove);
        joins.remove(joinToRemove);
        joinToRemove.cleanup();
        
        fireChildRemoved(WabitJoin.class, joinToRemove, index);
    }
    
    protected void fireJoinPropertyChangeEvent(final PropertyChangeEvent evt) {
        final PropertyChangeEvent newEvent = new PropertyChangeEvent(this, 
                evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).joinPropertyChangeEvent(newEvent);
                    }
                }
            }
        });
    }
    
    protected void firePropertyChangeEvent(final PropertyChangeEvent evt) {
        final PropertyChangeEvent newEvent = new PropertyChangeEvent(this, 
                evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).propertyChangeEvent(newEvent);
                    }
                }
            }
        });
        super.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
    
    protected void fireCompoundEditStarted(final TransactionEvent evt) {
        final TransactionEvent newEvent = 
            TransactionEvent.createStartTransactionEvent(this, evt.getMessage());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).compoundEditStarted(newEvent);
                    }
                }
            }
        });
        fireTransactionStarted(evt.getMessage());
    }
    
    protected void fireCompoundEditEnded(TransactionEvent evt) {
        final TransactionEvent newEvent =
            TransactionEvent.createEndTransactionEvent(this);
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).compoundEditEnded(newEvent);
                    }
                }
            }
        });
        fireTransactionEnded();
    }
    
    protected void fireItemPropertyChangeEvent(final PropertyChangeEvent evt) {
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).itemPropertyChangeEvent(evt);
                    }
                }
            }
        });
    }
    
    protected void fireItemAdded(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getItemChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).itemAdded(newEvent);
                    }
                }
            }
        });
        
        //This does not need to fire a wabit event for an item being added.
    }
    
    protected void fireItemRemoved(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getItemChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).itemRemoved(newEvent);
                    }
                }
            }
        });
        
      //This does not need to fire a wabit event for an item being added.
    }
    
    protected void fireContainerRemoved(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getContainerChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).containerRemoved(newEvent);
                    }
                }
            }
        });
        
        WabitContainer<?> childToRemove = null;
        Class<? extends WabitContainer<?>> classToRemove = null;
        int index = -1;
        for (WabitTableContainer container : containers) {
            if (container.getDelegate().equals(evt.getContainerChanged())) {
                childToRemove = container;
                classToRemove = WabitTableContainer.class;
                index = containers.indexOf(childToRemove);
                containers.remove(childToRemove);
                break;
            }
        }
        if (childToRemove == null && constantContainer.getDelegate().equals(evt.getContainerChanged())) {
            childToRemove = constantContainer;
            classToRemove = WabitConstantsContainer.class;
            index = 0;
            constantContainer = null;
        }
        if (childToRemove == null) 
            throw new IllegalStateException("QueryCache is out of sync with the query it is " +
            		"delegating to. Cannot find the container " + 
            		evt.getContainerChanged().getName() + " to remove from the query.");
        
        childToRemove.cleanup();
        
        fireChildRemoved(classToRemove, childToRemove, index);
    }
    
    protected void fireContainerAdded(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getContainerChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).containerAdded(newEvent);
                    }
                }
            }
        });
        
        //Adding tables is done by the addTable method. The constants container cannot change.
    }

    //---------------------end event handling for query delegate--------------

    public WabitConstantsContainer getWabitConstantsContainer() {
        return constantContainer;
    }
    
    @Override
    public void setUUID(String uuid) {
    	super.setUUID(uuid);
    	query.setUUID(uuid);
    	if (this.variableProvider != null)
    		this.variableProvider.setNamespace(uuid);
    }
    
    @Override
    public void generateNewUUID() {
    	super.generateNewUUID();
    	if (query != null) {
    		query.setUUID(getUUID());
    	}
    	if (this.variableProvider != null)
    		this.variableProvider.setNamespace(getUUID());
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(WabitConstantsContainer.class);
    	types.add(WabitTableContainer.class);
    	types.add(WabitJoin.class);
    	return types;
    }

    public SPVariableResolver getVariableResolver() {
    	return this.variableProvider;
    }
    
    @Override
    public void setParent(SPObject parent) {
    	super.setParent(parent);
    	
    	// Create a variable context.
        this.variableProvider = new QueryVariableResolver(this, this.uuid, "Relational Query - " + this.getName());
    }

    
    
    
    
    
    
    // XXX All this stuff will have to be removed once the SQLP lib has cleared up his act.
    
    
    private final List<StatementExecutorListener> executorListeners = new ArrayList<StatementExecutorListener>();
    private ResultSetHandle internalHandle = null;
    

	public boolean executeStatement() throws SQLException {
		try {
			this.internalHandle = this.execute(new SPVariableHelper(this), null);
			return true;
		} catch (ResultSetProducerException e) {
			SQLException se = new SQLException();
			se.initCause(e);
			throw se;
		}
	}

	public boolean getMoreResults() throws SQLException {
		return false;
	}

	public ResultSet getResultSet() throws SQLException {
		return this.internalHandle.getResultSet();
	}

	public int getUpdateCount() {
		return -1;
	}

	public void removeStatementExecutorListener(StatementExecutorListener sel) {
		this.executorListeners.remove(sel);
	}
	public void addStatementExecutorListener(StatementExecutorListener sel) {
		this.executorListeners.add(sel);
	}
}
