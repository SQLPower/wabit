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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.OlapConnectionProvider;
import ca.sqlpower.wabit.SqlConnectionProvider;
import ca.sqlpower.wabit.rs.ResultSetHandle.ResultSetType;


/**
 * Convenient implementation support for the {@link ResultSetProducer} interface.
 * 
 * 
 */
public class ResultSetProducerSupport {

	private final List<ResultSetHandle> handles = new CopyOnWriteArrayList<ResultSetHandle>();
	
    private final List<ResultSetProducerListener> listeners = new CopyOnWriteArrayList<ResultSetProducerListener>();

	private final ResultSetProducerStatusInformant informant;
	
	private final ResultSetListener internalListener = new ResultSetListener() {
	
		/**
	     * These implementations of {@link ResultSetListener} are used
	     * to listen to the child handles. Do not call this from any other class.
	     */
		public void executionComplete(ResultSetEvent evt) {
			// We get called here when one of our handles has completed it's work.
			ResultSetProducerSupport.this.fireExecutionComplete();
		}
		
		/**
	     * These implementations of {@link ResultSetListener} are used
	     * to listen to the child handles. Do not call this from any other class.
	     */
		public void newData(ResultSetEvent evt) {
			// we don't care about those.
		}
		
		public void executionStarted(ResultSetEvent evt) {
			fireExecutionStarted();
		}
	};

	private final ResultSetProducer source;
	
	
	/**
	 * Constructs a helper object that will keep track of all
	 * distributed {@link ResultSetHandle} objects and will
	 * notify listeners wether they are running or not.
	 */
    public ResultSetProducerSupport(@Nonnull ResultSetProducer source) {
		this(source, null);
    }
    
    /**
	 * Constructs a helper object that will keep track of all
	 * distributed {@link ResultSetHandle} objects and will
	 * notify listeners wether they are running or not.
	 * 
	 * @param source The {@link ResultSetProducer} for which this 
	 * support object was created.
	 * 
	 * @param isRunning Callback used in conjunction with 
	 * this object's internal state that will determine if this object
	 * is running or not. Some objects might want to report themselves
	 * in an active state even if the distributed handles are all idle.
	 */
    public ResultSetProducerSupport(
    		@Nonnull ResultSetProducer source, 
    		@Nullable ResultSetProducerStatusInformant informant) {
		this.source = source;
		this.informant = informant;
    }
    
    
    public void addResultSetListener(@Nonnull ResultSetProducerListener listener) {
        if (listener == null) {
            throw new NullPointerException("Null listener not allowed");
        }
    	listeners.add(listener);
    }

    public void removeResultSetListener(ResultSetProducerListener listener) {
    	listeners.remove(listener);
    }
    
    /**
     * Builds a {@link ResultSetHandle} and will trigger it's execution
     * in the background.
     * 
     * This version takes a query and a {@link SPVariableResolver} (usually an 
     * instance of {@link SPVariableHelper}) and will build the 
     * {@link PreparedStatement} in the background.
     * 
     * @param query A query with variables included.
     * @param variablesContext A variable resulver from which to resolve variables.
     * @param isStreaming Whether or not this is a streaming query.
     * @param rowLimit The row limit for queries 
     * @param listener A listener to bind to the {@link ResultSetHandle}
     * @return An executing {@link ResultSetHandle}
     * @throws SQLException
     */
    public ResultSetHandle execute(
    		@Nonnull final SqlConnectionProvider connectionProvider,
    		@Nonnull final JDBCDataSource dataSource,
    		@Nonnull final String query,
    		@Nonnull final SPVariableHelper variablesContext,
    		@Nonnull final ResultSetType type,
            final int rowLimit,
            @Nullable final ResultSetListener listener,
            boolean async) throws SQLException
    {
    		
		ResultSetHandle rsh = 
			new ResultSetHandle(
					connectionProvider,
					dataSource,
					query,
					variablesContext,
					type,
					rowLimit,
					null);
		
		rsh.addResultSetListener(internalListener);
		if (listener != null) {
			rsh.addResultSetListener(listener);
		}
		
		// Save this new one
		this.handles.add(rsh);
		
		rsh.populate(async);
		
		return rsh;
    }
    
    /**
     * Builds a {@link ResultSetHandle} and will trigger it's execution
     * in the background.
     * 
     * This version takes a query and a {@link SPVariableResolver} (usually an 
     * instance of {@link SPVariableHelper}) and will build the 
     * {@link PreparedStatement} in the background.
     * 
     * @param query A query with variables included.
     * @param variablesContext A variable resulver from which to resolve variables.
     * @param isStreaming Whether or not this is a streaming query.
     * @param rowLimit The row limit for queries 
     * @param listener A listener to bind to the {@link ResultSetHandle}
     * @return An executing {@link ResultSetHandle}
     * @throws SQLException
     */
    public ResultSetHandle execute(
    		@Nonnull final OlapConnectionProvider connectionProvider,
    		@Nonnull final Olap4jDataSource dataSource,
    		@Nonnull final String query,
    		@Nonnull final SPVariableHelper variablesContext,
    		@Nonnull final ResultSetType type,
            final int rowLimit,
            @Nullable final ResultSetListener listener,
            boolean async) throws SQLException
    {
		ResultSetHandle rsh = 
			new ResultSetHandle(
					connectionProvider,
					dataSource,
					query,
					variablesContext,
					type,
					rowLimit,
					null);
		
		rsh.addResultSetListener(internalListener);
		if (listener != null) {
			rsh.addResultSetListener(listener);
		}
		
		// Save this new one
		this.handles.add(rsh);
		
		rsh.populate(async);
		
		return rsh;
    }
    
    /**
     * Cancels the execution of every handle.
     */
    public void cancel() {
		for (int i = this.handles.size()-1; i >= 0; i--) {
			ResultSetHandle rsh = this.handles.get(i);
			rsh.cancel();
			this.handles.remove(i);
		}
    }

    /**
     * Notifies all listeners that this producer's structure has
     * changed and the subsequent handles will be different.
     */
	public synchronized void fireStructureChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (ResultSetProducerListener rspl : ResultSetProducerSupport.this.listeners) {
					rspl.structureChanged(new ResultSetProducerEvent(source));
				}
			}
		});
	}
	
	/**
	 * This method will determine if the execution is in fact started and
	 * will fire required events if necessary.
	 * 
	 * Callers of this method from the exterior should be aware that 
	 * the {@link ResultSetProducerStatusInformant} passed at construction
	 * time will be asked to report on the current status of the execution.
	 */
	public synchronized void fireExecutionStarted() {
		
		boolean isRunning = false;

		
		if (this.informant != null &&
			this.informant.isRunning()) {
			isRunning = true;
		}
		
		isRunning |= isRunning();
		
		if (isRunning) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (ResultSetProducerListener rspl : ResultSetProducerSupport.this.listeners) {
						rspl.executionStarted(new ResultSetProducerEvent(source));
					}
				}
			});
		}
	}
	
	/**
	 * This method will determine if the execution is in fact completed and
	 * will fire required events if necessary.
	 */
	public synchronized void fireExecutionComplete() {
		
		boolean isRunning = false;
			
		// Notify listeners if necessary that the last
		// handle has stopped executing
		if (informant != null &&
				informant.isRunning()) {
			isRunning = true;
		}
		
		isRunning |= isRunning();
		
		if (!isRunning) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (ResultSetProducerListener rspl : ResultSetProducerSupport.this.listeners) {
						rspl.executionStopped(new ResultSetProducerEvent(source));
					}
				}
			});
		}
	}
	
	public boolean isRunning() {
		boolean running = false;
		for (int i = this.handles.size()-1; i >= 0; i--) {
			ResultSetHandle rsh = this.handles.get(i);
			if (rsh.isRunning()) {
				running = true;
			} else {
				this.handles.remove(rsh);
			}
		}
		return running;
	}
}
