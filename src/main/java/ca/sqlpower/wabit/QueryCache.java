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
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import ca.sqlpower.query.Query;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.wabit.swingui.ExceptionHandler;

/**
 * This method will be able to execute and cache the results of a query. It also
 * delegates some of the methods to the {@link Query} contained in it.
 */
public class QueryCache extends AbstractWabitObject implements StatementExecutor, WabitBackgroundWorker {
    
    private static final Logger logger = Logger.getLogger(QueryCache.class);
    
    /**
     * A {@link PropertyChangeEvent} should fire a property change of this
     * property when it wishes to notify the {@link Query} that the row
     * limit has changed. The new value given in the property change should be
     * the new row limit in {@link Integer} form so the query cache knows how
     * large of a result to cache.
     */
    public static final String ROW_LIMIT = "rowLimit";
    
    private final Query query;
    
    private final List<CachedRowSet> resultSets = new ArrayList<CachedRowSet>();
    
    private final List<Integer> updateCounts = new ArrayList<Integer>();
    
    private int resultPosition = 0;
    
    /**
     * This is the statement currently entering result sets into this query cache.
     * This lets the query cancel a running statement.
     * <p>
     * This is only used if {@link Query#streaming} is true.
     */
    private Statement currentStatement;
    
    /**
     * This is the connection currently entering result sets into this query cache.
     * This lets the query close a running connection
     * <p>
     * This is only used if {@link Query#streaming} is true.
     */
    private Connection currentConnection; 
    
    /**
     * A change listener to flush the cached row sets on a row limit change.
     * This listener should be attached to the object that will fire a "rowLimit"
     * change which would notify components that a new row limit has been set for
     * queries.
     */
    private final PropertyChangeListener rowLimitChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ROW_LIMIT)) {
                resultSets.clear();
                updateCounts.clear();
                query.setRowLimit((Integer) evt.getNewValue());
            }
        }
    };
    
    /**
     * This listener will be notified when additional rows are added to a streaming
     * row set. This listener will then notify the listeners of this query that
     * a result set has changed.
     */
    private final RowSetChangeListener rsChangeListener = new RowSetChangeListener() {
        public void rowAdded(RowSetChangeEvent e) {
            for (int i = rowSetChangeListeners.size() - 1; i >= 0; i--) {
                rowSetChangeListeners.get(i).rowAdded(e);
            }
        }
    };
    
    /**
     * The listeners to be updated from a change to the result set.
     */
    private final List<RowSetChangeListener> rowSetChangeListeners = new ArrayList<RowSetChangeListener>();
    
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
        this.query = new Query(q.query, connectListeners);
        
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
        this(null, dbMapping);
    }
    
    public QueryCache(String uuid, SQLDatabaseMapping dbMapping) {
        query = new Query(uuid, dbMapping);
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
                                    crs.follow(streamingRS, rsChangeListener, getStreamingRowLimit());
                                } catch (SQLException e) {
                                    logger.error("Exception while streaming result set", e);
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

    public String getStatement() {
        return query.generateQuery();
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
     * 
     * @param fullResultSet
     *            If true the full result set will be retrieved. If false then a
     *            limited result set will be retrieved based on the session's
     *            row limit.
     */
    public ResultSet fetchResultSet() throws SQLException {
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
    
    public void cleanup() {
        if (currentStatement != null) {
            try {
                currentStatement.cancel();
                currentStatement.close();
            } catch (SQLException e) {
                logger.error("Error while closing old streaming statement", e);
            }
        }
        if (currentConnection != null) {
            try {
                currentConnection.close();
            } catch (SQLException e) {
                logger.error("Error while closing old streaming connection", e);
            }
        }
    }
    
    public WabitDataSource getWabitDataSource() {
        if (query.getDatabase() == null || query.getDatabase().getDataSource() == null) {
            return null;
        }
        return new WabitDataSource(query.getDatabase().getDataSource());
    }

    public void addRowSetChangeListener(RowSetChangeListener l) {
        rowSetChangeListeners.add(l);
    }

    public void removeRowSetChangeListener(RowSetChangeListener l) {
        rowSetChangeListeners.remove(l);        
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
    
    public PropertyChangeListener getRowLimitChangeListener() {
        return rowLimitChangeListener;
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

    public String getName() {
        return query.getName();
    }

    public String getUUID() {
        return query.getUUID();
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

    public Query getQuery() {
        return query;
    }

    public boolean isStreaming() {
        return query.isStreaming();
    }
    
    @Override
    public String toString() {
        return getName();
    }

    public List<WabitObject> getDependencies() {
        if (getWabitDataSource() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getWabitDataSource()));
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

}
