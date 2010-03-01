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

package ca.sqlpower.wabit.rs;

import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;
import org.olap4j.CellSet;
import org.olap4j.PreparedOlapStatement;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.OlapConnectionProvider;
import ca.sqlpower.wabit.SqlConnectionProvider;
import ca.sqlpower.wabit.swingui.ExceptionHandler;

/**
 * This object is a wrapper for a background executing query that will return
 * a {@link ResultSet} once it completes.
 * 
 * All events fired from this object will be fired on the Swing event thread.
 */
public class ResultSetHandle {
	
	public enum ResultSetType {
		OLAP,
		RELATIONAL,
		STREAMING
	}
	
	public enum ResultSetStatus {
		NEW,
		RUNNING,
		SUCCESS,
		ERROR
	}
	
	private final ResultSetType rsType;
	
	private ResultSetStatus status = ResultSetStatus.NEW;
	
	private Exception exception = null;

	private final Task task;
	
	private boolean populated = false;

	private final int rowLimit;
    
    private static final Logger logger = Logger.getLogger(ResultSetHandle.class);
    
    /**
     * This is the internal {@link CachedRowSet} data object.
     * We will populate it on a background thread.
     */
    private final CachedRowSet cachedRowSet;
    
    /**
     * This is the internal CellSet object 
     */
    private CellSet olapCellSet = null;
    
    private static ExecutorService executorService;
    
    static {
    	executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * All of the listeners will be notified when events happen
     * in the underlying {@link CachedRowSet}
     */
    @GuardedBy("resultSetListeners")
    private final List<ResultSetListener> resultSetListeners = 
    		new CopyOnWriteArrayList<ResultSetListener>();
    
    /**
     * Internal listener to forward row updates to
     * our own listeners
     */
    private RowSetChangeListener internalListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			final ResultSetEvent rse = ResultSetEvent.getNewDataEvent(
									ResultSetHandle.this, 
									e.getRow(), 
									e.getRowNumber());
			Runnable runnable = new Runnable() {
	    		public void run() {
    				for (ResultSetListener listener : resultSetListeners) {
						listener.newData(rse);
					}
	    		}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				runnable.run();
			} else {
				SwingUtilities.invokeLater(runnable);
			}
		}
	};
	
	private class InternalExceptionHandler implements UncaughtExceptionHandler {
		private final UncaughtExceptionHandler delegate;
		public InternalExceptionHandler(UncaughtExceptionHandler delegate) {
			if (delegate == null) {
				this.delegate = new UncaughtExceptionHandler() {
					public void uncaughtException(Thread t, Throwable e) {
						SPSUtils.showExceptionDialogNoReport(null, "An unexpected exception has occured: ", e);
					}
				};
			} else {
				this.delegate = delegate;
			}
		}
		public void uncaughtException(Thread t, Throwable e) {
			ResultSetHandle.this.status = ResultSetStatus.ERROR;
			exception = new Exception(e);
			delegate.uncaughtException(t, e);
		}
	};

    
    
    public ResultSetHandle(
    		SqlConnectionProvider connectionProvider,
    		JDBCDataSource dataSource,
    		String query, 
    		SPVariableHelper variablesContext,
			ResultSetType type, 
			int rowLimit, 
			@Nullable final ExceptionHandler injectedHandler) 
    {
    	if (query == null) {
    		throw new NullPointerException("Query cannot be null");
    	}
    	if (variablesContext == null) {
    		throw new NullPointerException("Variables Context cannot be null");
    	}
    	
    	this.rowLimit = rowLimit;
    	this.rsType = type;
    	
    	if (this.rsType.equals(ResultSetType.OLAP)) {
    		this.cachedRowSet = new OlapResultSet();
    	} else {
    		this.cachedRowSet = new CachedRowSet();
    	}
    	
    	this.cachedRowSet.addRowSetListener(this.internalListener);
    	
    	// Create a threaded object that will execute this query
    	// in the background
    	this.task = 
    			new Task(
						connectionProvider,
						dataSource, 
						query, 
						variablesContext, 
						injectedHandler);
	}
    
    public ResultSetHandle(
    		OlapConnectionProvider connectionProvider,
    		Olap4jDataSource dataSource,
    		String query, 
    		SPVariableHelper variablesContext,
			ResultSetType type, 
			int rowLimit, 
			@Nullable final ExceptionHandler injectedHandler) 
    {
    	if (query == null) {
    		throw new NullPointerException("Query cannot be null");
    	}
    	if (variablesContext == null) {
    		throw new NullPointerException("Variables Context cannot be null");
    	}
    	
    	this.rowLimit = rowLimit;
    	this.rsType = type;
    	
    	if (this.rsType.equals(ResultSetType.OLAP)) {
    		this.cachedRowSet = new OlapResultSet();
    	} else {
    		this.cachedRowSet = new CachedRowSet();
    	}
    	
    	this.cachedRowSet.addRowSetListener(this.internalListener);
    	
    	// Create a threaded object that will execute this query
    	// in the background
    	this.task = 
    			new Task(
						connectionProvider,
						dataSource, 
						query, 
						variablesContext, 
						injectedHandler);
	}

	private class Task implements Runnable {
    	
		private PreparedStatement statement = null;
		
		private final UncaughtExceptionHandler handler;
		private String query = null;
		private SPVariableHelper variablesContext = null;

		private SqlConnectionProvider sqlConnectionProvider = null;
		private OlapConnectionProvider olapConnectionProvider = null;
		private JDBCDataSource jdbcDataSource = null;
		private Olap4jDataSource olapDataSource = null; 
		
		
		public Task(
				SqlConnectionProvider connectionProvider,
	    		JDBCDataSource dataSource,
				String query, 
				SPVariableHelper variablesContext,
				ExceptionHandler injectedHandler) 
		{
			this.sqlConnectionProvider = connectionProvider;
			this.jdbcDataSource = dataSource;
			this.query = query;
			this.variablesContext = variablesContext;
			handler = injectedHandler;
		}
		
		public Task(
				OlapConnectionProvider connectionProvider,
	    		Olap4jDataSource dataSource,
				String query, 
				SPVariableHelper variablesContext,
				ExceptionHandler injectedHandler) 
		{
			this.olapConnectionProvider = connectionProvider;
			this.olapDataSource = dataSource;
			this.query = query;
			this.variablesContext = variablesContext;
			handler = injectedHandler;
		}
		
		public void cancel() {
			try {
				if (statement != null) {
					statement.cancel();    				
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		public void run() {
			try {
            	
            	status = ResultSetStatus.RUNNING;
            	
            	// Place an exception handler
            	Thread.currentThread().setUncaughtExceptionHandler(
            			new InternalExceptionHandler(handler));
       
            	final ResultSetEvent evt = 
        			ResultSetEvent.getExecutionStartedEvent(ResultSetHandle.this);
            	Runnable runnable = new Runnable() {
        			public void run() {
    					for (ResultSetListener listener : resultSetListeners) {
    						listener.executionStarted(evt);
    					}
        			}
        		};
        		if (SwingUtilities.isEventDispatchThread()) {
        			runnable.run();
        		} else {
        			SwingUtilities.invokeLater(runnable);
        		}
        		
        		switch (rsType) {
        		
            		case OLAP:
            			statement = olapConnectionProvider.createPreparedStatement(olapDataSource, query, variablesContext);
                		break;
                		
            		case RELATIONAL:
            		case STREAMING:
            			statement = sqlConnectionProvider.createPreparedStatement(jdbcDataSource, query, variablesContext);
            			break;
            			
            		default:
            			throw new RuntimeException("Program error.");
            			
        		}
        		
            	switch (rsType) {
            	
                	case OLAP:
                		olapCellSet = ((PreparedOlapStatement)statement).executeQuery();
                		((OlapResultSet)cachedRowSet).populate(olapCellSet);
                		status = ResultSetStatus.SUCCESS;
                		break;
                
                	case STREAMING:
                		statement.execute();
                		final ResultSet streamingRS = statement.getResultSet();
                		status = ResultSetStatus.SUCCESS;
            			cachedRowSet.follow(streamingRS, rowLimit);
            			break;
                	
                	case RELATIONAL:
                		statement.setMaxRows(rowLimit);
                		statement.execute();
                		final ResultSet rs = statement.getResultSet();
                		if (rs != null) {
                			cachedRowSet.populate(rs);                			                			
                		}
                		status = ResultSetStatus.SUCCESS;
                		break;
                	default:
            			throw new RuntimeException("Program error.");
            	}
            	
            } catch (Throwable t) {
                logger.error("Exception ecountered while executing the query", t);
                status = ResultSetStatus.ERROR;
                SQLException e = new SQLException();
                e.initCause(t);
                exception = e;
            } finally {
            	try {
            		if (statement != null) {
            			statement.close();
                	}
            	} catch (Exception eX) {
            		logger.debug("Exception ecountered while closing the statement's connection", eX);
            	} finally {
            		final ResultSetEvent evt = 
            			ResultSetEvent.getExecutionCompleteEvent(ResultSetHandle.this);
            		Runnable task = new Runnable() {
            			public void run() {
        					for (ResultSetListener listener : resultSetListeners) {
        						listener.executionComplete(evt);
        					}
            			}
            		};
            		if (SwingUtilities.isEventDispatchThread()) {
            			task.run();
            		} else {
            			SwingUtilities.invokeLater(task);
            		}
            	}
            }
        }
    }

	/**
     * Triggers the population of this handle.
     */
    public void populate(boolean async) {
    	
    	if (populated)
    		throw new RuntimeException("Cannot populate a ResultSetHandle twice.");
    	
    	populated = true;
    	
    	// Streaming queries are always async.
    	if (this.rsType.equals(ResultSetType.STREAMING)) {
    		executorService.execute(task);
    		return;
    	}
    	
    	if (async && !System.getProperty("java.class.path").contains("junit")) {
    		executorService.execute(task);
    		return;
    	}
    	
    	ExecutorService adHodExecutor = Executors.newSingleThreadScheduledExecutor();
    	adHodExecutor.execute(task);
    	try {
    		adHodExecutor.shutdown();
			boolean completed = adHodExecutor.awaitTermination(60, TimeUnit.SECONDS);
			if (!completed) {
				throw new RuntimeException("Query Execution Timeout");
			}
		} catch (InterruptedException e) {
			task.cancel();
		}
		
    }

    /**
     * The added listener will be notified when all of the streaming queries have stopped.
     * This cannot be null.
     */
    public void addResultSetListener(@Nonnull ResultSetListener l) {
        resultSetListeners.add(l);
    }

    /**
     * The removed listener will no longer be notified when all of the streaming
     * queries have stopped streaming in this collection.
     */
    public void removeResultSetListener(ResultSetListener l) {
        resultSetListeners.remove(l);
    }
    
    /**
     * Gets a {@link ResultSet} from this handle.
     * 
     * This call is not guaranteed to return a result set that is
     * populated yet. It is quite possible that the contents of the result
     * set will be populated later.
     * 
     * This method is typically used to know ahead of time what the 
     * {@link ResultSet}'s {@link ResultSetMetaData} will be.
     * 
     * To get notified when the rows start coming in, one can listen to
     * this object by registering a {@link ResultSetListener}
     */
    public ResultSet getResultSet() {
        return this.cachedRowSet;
    }
    
    /**
     * This method will block and return the CellSet object as soon as the
     * execution is finished. It is preferable to use {@link ResultSetHandle#isRunning()}
     * or a listener to be notified when the CellSet is ready for use.
     */
    public CellSet getCellSet() {
    	if (!this.rsType.equals(ResultSetType.OLAP)) {
    		throw new UnsupportedOperationException("Cannot obtain a CellSet object from a ResultSetHandle that is not of OLAP type.");
    	}
    	return this.olapCellSet;
    }
    
    /**
     * Cancels this 
     */
    public void cancel() {
    	if (this.isRunning()) {
    		this.task.cancel();
    	}
    }
    
    /**
     * Returns true if at least one of the distributed handles is still running.
     */
    public boolean isRunning() {
    	return this.status.equals(ResultSetStatus.RUNNING);
    }
    
    /**
     * Returns which type of query is attached to this handle.
     */
    public ResultSetType getResultSetType() {
    	return this.rsType;
    }
    
    /**
     * Returns the status of this {@link ResultSetHandle}
     */
    public ResultSetStatus getStatus() {
    	return this.status;
    }
    
    /**
     * Returns the exception encountered while populating the result set, if any.
     * Might return null.
     * @see {@link ResultSetHandle#getStatus()}
     */
    public Exception getException() {
		return exception;
	}
}
