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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.QueryChangeEvent;
import ca.sqlpower.query.QueryChangeListener;
import ca.sqlpower.query.QueryCompoundEditEvent;
import ca.sqlpower.query.QueryImpl;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetProducerException;
import ca.sqlpower.wabit.rs.ResultSetProducerSupport;
import ca.sqlpower.wabit.swingui.ExceptionHandler;

/**
 * This method will be able to execute and cache the results of a query. It also
 * delegates some of the methods to the {@link QueryImpl} contained in it.
 */
public class QueryCache extends AbstractWabitObject implements Query, StatementExecutor, ResultSetProducer {
    
    private static final Logger logger = Logger.getLogger(QueryCache.class);
    
    private final QueryImpl query;
    
    @GuardedBy("rsps")
    private final ResultSetProducerSupport rsps = new ResultSetProducerSupport(this);
    
    private final List<CachedRowSet> resultSets = new ArrayList<CachedRowSet>();
    
    private final List<Integer> updateCounts = new ArrayList<Integer>();
    
    private int resultPosition = 0;
    
    /**
     * This is the statement currently entering result sets into this query cache.
     * This lets the query cancel a running statement.
     * <p>
     * This is only used if {@link QueryImpl#streaming} is true.
     */
    private Statement currentStatement;
    
    /**
     * This is the connection currently entering result sets into this query cache.
     * This lets the query close a running connection
     * <p>
     * This is only used if {@link QueryImpl#streaming} is true.
     */
    private Connection currentConnection; 
    
    /**
     * The threads in this list are used to stream queries from a connection into
     * this query cache.
     */
    private final List<Thread> streamingThreads = new ArrayList<Thread>();
    
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
     * Flag to indicate whether or not the query is currently running.
     */
    @GuardedBy("this")
    private int currentExecutionCount = 0;
    
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
        }
    
        public void joinRemoved(QueryChangeEvent evt) {
            fireJoinRemoved(evt);
        }
    
        public void joinPropertyChangeEvent(PropertyChangeEvent evt) {
            fireJoinPropertyChangeEvent(evt);
        }
    
        public void joinAdded(QueryChangeEvent evt) {
            fireJoinAdded(evt);
        }
    
        public void itemRemoved(QueryChangeEvent evt) {
            fireItemRemoved(evt);
        }
    
        public void itemPropertyChangeEvent(PropertyChangeEvent evt) {
            fireItemPropertyChangeEvent(evt);
        }
    
        public void itemOrderChanged(QueryChangeEvent evt) {
            fireItemOrderChanged(evt);
        }
    
        public void itemAdded(QueryChangeEvent evt) {
            fireItemAdded(evt);
        }
    
        public void containerRemoved(QueryChangeEvent evt) {
            fireContainerRemoved(evt);
        }
    
        public void containerAdded(QueryChangeEvent evt) {
            fireContainerAdded(evt);
        }
    
        public void compoundEditStarted(QueryCompoundEditEvent evt) {
            fireCompoundEditStarted(evt);
        }
    
        public void compoundEditEnded(QueryCompoundEditEvent evt) {
            fireCompoundEditEnded(evt);
        }
    };
    
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
        
        for (CachedRowSet rs : q.getResultSets()) {
            if (rs == null) {
                resultSets.add(null);
            } else {
                try {
                    resultSets.add((CachedRowSet) rs.createShared());
                } catch (SQLException e) {
                    throw new RuntimeException("This should not be able to happen", e);
                }
            }
        }
    }
    
    public QueryCache(SQLDatabaseMapping dbMapping) {
        query = new QueryImpl(dbMapping);
        query.addQueryChangeListener(queryChangeListener);
    }

    /**
     * Executes the current SQL query, returning a cached copy of the result
     * set. The returned copy of the result set is guaranteed to be scrollable,
     * and does not hold any remote database resources.
     * 
     * @return an in-memory copy of the result set produced by this query
     *         cache's current query. You are not required to close the returned
     *         result set when you are finished with it, but you can if you
     *         like.
     * @throws SQLException
     *             If the query fails to execute for any reason.
     */
    public boolean executeStatement() throws SQLException {
        return executeStatement(false);
    }
    
    /**
     * Executes the current SQL query, returning a cached copy of the result set
     * that is either a subset of the full results, limited by the session's row
     * limit, or the full result set. The returned copy of the result set is
     * guaranteed to be scrollable, and does not hold any remote database
     * resources.
     * 
     * @param fullResultSet
     *            If true the full result set will be retrieved. If false then a
     *            limited result set will be retrieved based on the session's
     *            row limit.
     * @return an in-memory copy of the result set produced by this query
     *         cache's current query. You are not required to close the returned
     *         result set when you are finished with it, but you can if you
     *         like.
     * @throws SQLException
     *             If the query fails to execute for any reason.
     */
    public boolean executeStatement(boolean fetchFullResults) throws SQLException {
        cancel();
        resultPosition = 0;
        resultSets.clear();
        updateCounts.clear();
        if (query.getDatabase() == null || query.getDatabase().getDataSource() == null) {
            throw new NullPointerException("Data source is null.");
        }
        String sql = query.generateQuery();
        ResultSet rs = null;
        try {
            setRunning(true);
            currentConnection = query.getDatabase().getConnection();
            currentStatement = currentConnection.createStatement();
            if (!fetchFullResults) {
                currentStatement.setMaxRows(query.getRowLimit());
            }
            boolean initialResult = currentStatement.execute(sql);
            boolean sqlResult = initialResult;
            boolean hasNext = true;
            while (hasNext) {
                if (sqlResult) {
                    final CachedRowSet crs = new CachedRowSet();
                    if (query.isStreaming()) {
                        final ResultSet streamingRS = currentStatement.getResultSet();
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler());
                                    crs.follow(streamingRS, getStreamingRowLimit());
                                } catch (SQLException e) {
                                    logger.error("Exception while streaming result set", e);
                                } finally {
                                    setRunning(false);
                                }
                            }
                        };
                        t.start();
                        streamingThreads.add(t);
                    } else {
                        crs.populate(currentStatement.getResultSet());
                    }
                    resultSets.add(crs);
                } else {
                    resultSets.add(null);
                }
                updateCounts.add(currentStatement.getUpdateCount());
                sqlResult = currentStatement.getMoreResults();
                hasNext = !((sqlResult == false) && (currentStatement.getUpdateCount() == -1));
            }
            
            final CachedRowSet resultsToFire;
            if (resultSets.size() > 0) {
                // results will be null if the first statement produced an update count,
                // but that's allowed by the ResultSetProducer interface.
                resultsToFire = resultSets.get(0);
            } else {
                // no statements were executed. ResultSetProducer promises to deliver a
                // null result set in this case.
                resultsToFire = null;
            }
            runInForeground(new Runnable() {
                public void run() {
                    synchronized(rsps) {
                        try {
                            rsps.fireResultSetEvent(resultsToFire);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            
            return initialResult;
            
        } catch (SQLObjectException e) {
            throw new SQLObjectRuntimeException(e);
        } finally {
            if (!query.isStreaming()) {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception ex) {
                        logger.warn("Failed to close result set. Squishing this exception: ", ex);
                    }
                }
                if (currentStatement != null) {
                    try {
                        currentStatement.close();
                        currentStatement = null;
                    } catch (Exception ex) {
                        logger.warn("Failed to close statement. Squishing this exception: ", ex);
                    }
                }
                if (currentConnection != null) {
                    try {
                        currentConnection.close();
                        currentConnection = null;
                    } catch (Exception ex) {
                        logger.warn("Failed to close connection. Squishing this exception: ", ex);
                    }
                }
                setRunning(false);
            }
        }
    }

    /**
     * Cancels this query's execution if it is currently running. Cancellation
     * is not guaranteed to work perfectly, because it is partly the underlying
     * JDBC driver's responsibility to provide an effective implementation of
     * {@link Statement#cancel()}.
     */
    public void cancel() {
        if (currentStatement != null) {
            try {
                currentStatement.cancel();
                currentStatement.close();
                currentStatement = null;
            } catch (SQLException e) {
                logger.error("Exception while closing old streaming statement", e);
            }
        }
        if (currentConnection != null) {
            try {
                currentConnection.close();
                currentConnection = null;
            } catch (SQLException e) {
                logger.error("Exception while closing old streaming connection", e);
            }
        }
        streamingThreads.clear();
    }

    public ResultSet getResultSet() {
        return getCachedRowSet();
    }
    
    public CachedRowSet getCachedRowSet() {
        if (resultPosition >= resultSets.size()) {
            return null;
        }
        return resultSets.get(resultPosition);
    }
    
    protected List<CachedRowSet> getResultSets() {
        return Collections.unmodifiableList(resultSets);
    }

    public int getUpdateCount() {
        if (resultPosition >= updateCounts.size()) {
            return -1;
        }
        return updateCounts.get(resultPosition);
    }

    public boolean getMoreResults() {
        resultPosition++;
        return resultPosition < resultSets.size() && resultSets.get(resultPosition) != null;
    }

    /**
     * Returns the most up to date result set in the query cache. This may
     * execute the query on the database.
     */
    public CachedRowSet fetchResultSet() throws SQLException {
        if (!resultSets.isEmpty()) {
            for (CachedRowSet rs : resultSets) {
                if (rs != null) {
                    return rs.createShared();
                }
            }
            return null;
        }
        executeStatement();
        for (CachedRowSet rs : resultSets) {
            if (rs != null) {
                return rs.createShared();
            }
        }
        return null;
    }
    
    @Override
    public CleanupExceptions cleanup() {
        CleanupExceptions exceptions = new CleanupExceptions();
        if (currentStatement != null) {
            try {
                currentStatement.cancel();
                currentStatement.close();
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        if (currentConnection != null) {
            try {
                currentConnection.close();
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        return exceptions;
    }
    
    public WabitDataSource getWabitDataSource() {
        if (query.getDatabase() == null || query.getDatabase().getDataSource() == null) {
            return null;
        }
        return new WabitDataSource(query.getDatabase().getDataSource());
    }

    public List<Thread> getStreamingThreads() {
        return Collections.unmodifiableList(streamingThreads);
    }

    public synchronized boolean isRunning() {
        return currentExecutionCount > 0;
    }

    /**
     * Maintains the counter that indicates if this query is currently running.
     * Since there is no synchronization to prevent multiple threads from
     * executing this query simultaneously, this method actually keeps a running
     * count of how many concurrent executions are in progress.
     * 
     * @param isRunning
     *            An argument of true means a new query execution is beginning;
     *            false means a query execution has just completed.
     */
    private synchronized void setRunning(boolean isRunning) {
        int prevExecCount;
        if (isRunning) {
            prevExecCount = currentExecutionCount++;
        } else {
            prevExecCount = currentExecutionCount--;
        }
        
        boolean wasRunning = prevExecCount > 0;
        firePropertyChange("running", wasRunning, isRunning);
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public List<? extends WabitObject> getChildren() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<WabitObject> getDependencies() {
        if (getWabitDataSource() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getWabitDataSource()));
    }
    
    public void removeDependency(WabitObject dependency) {
        if (dependency.equals(getWabitDataSource())) {
            setDataSource(null);
        }
    }
    
    /**
     * If this returns true then this query cache is a query that should
     * not be hooked up to listeners. 
     */
    public boolean isPhantomQuery() {
        return getParent() == null;
    }

    public void setPromptForCrossJoins(boolean promptForCrossJoins) {
        this.promptForCrossJoins = promptForCrossJoins;
    }

    public boolean getPromptForCrossJoins() {
        return promptForCrossJoins;
    }

    public void setExecuteQueriesWithCrossJoins(boolean executeQueriesWithCrossJoins) {
        this.executeQueriesWithCrossJoins = executeQueriesWithCrossJoins;
    }

    public boolean getExecuteQueriesWithCrossJoins() {
        return executeQueriesWithCrossJoins;
    }

    public void setAutomaticallyExecuting(boolean automaticallyExecute) {
        this.automaticallyExecuting = automaticallyExecute;
    }

    public boolean isAutomaticallyExecuting() {
        return automaticallyExecuting;
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }
    
    // ------------------ ResultSetProducer interface ----------------------
    public void addResultSetListener(ResultSetListener listener) {
        synchronized(rsps) {
            rsps.addResultSetListener(listener);
        }
    }

    public void removeResultSetListener(ResultSetListener listener) {
        synchronized(rsps) {
            rsps.removeResultSetListener(listener);
        }
    }

    public ResultSet execute() throws ResultSetProducerException {
        synchronized(rsps) {
            try {
                long eventCount = rsps.getEventsFired();
                final CachedRowSet resultSet = fetchResultSet();
                if (rsps.getEventsFired() == eventCount) {
                    runInForeground(new Runnable() {
                        public void run() {
                            synchronized(rsps) {
                                try {
                                    rsps.fireResultSetEvent(resultSet);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
                return resultSet;
            } catch (SQLException e) {
                throw new ResultSetProducerException(e);
            }
        }
    }
    // ------------------ end ResultSetProducer interface ----------------------

    
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
        query.setDataSource(ds);
    }

    public void setName(String string) {
        query.setName(string);
    }
    
    public String getName() {
        return query.getName();
    }
    
    public SQLDatabaseMapping getDBMapping() {
        return query.getDbMapping();
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
        resultSets.clear();
        updateCounts.clear();
        query.setRowLimit(rowLimit);
    }
    
    public void addItem(Item col) {
        query.addItem(col);
    }

    public void addJoin(SQLJoin join) {
        query.addJoin(join);
    }

    public void addQueryChangeListener(QueryChangeListener l) {
        synchronized(queryListeners) {
            queryListeners.add(l);        
        }
    }

    public void addTable(Container container) {
        query.addTable(container);
    }

    public void defineUserModifiedQuery(String query) {
        this.query.defineUserModifiedQuery(query);
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

    public SQLDatabaseMapping getDbMapping() {
        return query.getDbMapping();
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
        return query.getOrderByList();
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

    public void moveSortedItemToEnd(Item item) {
        query.moveSortedItemToEnd(item);
    }

    public Container newConstantsContainer(String uuid) {
        return query.newConstantsContainer(uuid);
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
    }
    
    protected void fireCompoundEditStarted(final QueryCompoundEditEvent evt) {
        final QueryCompoundEditEvent newEvent = 
            QueryCompoundEditEvent.createStartCompoundEditEvent(this, evt.getMessage());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).compoundEditStarted(newEvent);
                    }
                }
            }
        });
    }
    
    protected void fireCompoundEditEnded(QueryCompoundEditEvent evt) {
        final QueryCompoundEditEvent newEvent =
            QueryCompoundEditEvent.createEndCompoundEditEvent(this);
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).compoundEditEnded(newEvent);
                    }
                }
            }
        });
    }
    
    protected void fireItemOrderChanged(final QueryChangeEvent evt) {
        final QueryChangeEvent newEvent = new QueryChangeEvent(this, evt.getItemChanged());
        runInForeground(new Runnable() {
            public void run() {
                synchronized(queryListeners) {
                    for (int i = queryListeners.size() - 1; i >= 0; i--) {
                        queryListeners.get(i).itemOrderChanged(newEvent);
                    }
                }
            }
        });
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
    }
    
    //---------------------end event handling for query delegate--------------

}
