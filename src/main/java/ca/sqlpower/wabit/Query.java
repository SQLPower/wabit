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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;

/**
 * The interface for anything that can provide data in a report. The canonical
 * example is an SQL query.
 */
public interface Query extends WabitObject {

	/**
	 * Executes the current query represented by this query object, returning a
	 * cached copy of the result set. The returned copy of the result set is
	 * guaranteed to be scrollable, and does not hold any remote network or
	 * database resources.
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
    public ResultSet fetchResultSet() throws SQLException;
    
    void setDataSource(SPDataSource ds);

    /**
     * Returns the short name for this object.
     */
    String getName();
    
    /**
     * Sets the name for this object 
     */
    void setName(String name);
    
    UUID getUUID();

	String generateQuery();
	
	/**
	 * This is the data source the query is connected to.
	 */
	WabitDataSource getWabitDataSource();

	/**
	 * Returns true if the user edited the script by hand. False if it is generated
	 * by parts of the query tool.
	 */
	public boolean isScriptModified();
	
	/**
	 * Returns the database mapping this query's data source is contained in. Used for copying
	 * queries.
	 */
	public SQLDatabaseMapping getDBMapping();
	
	/**
	 * Sets the session for this query if the query is being moved to a different
	 * session or is being imported into a new session from an old session.
	 */
	public void setDBMapping(SQLDatabaseMapping dbMapping);
	
	/**
	 * Call this when the query is to be disposed of.
	 */
	public void cleanup();
	
	/**
	 * Returns true if the data source this query is based on is a streaming query.
	 * Returns false otherwise.
	 */
	public boolean isStreaming();
	
	/**
	 * Returns true if the query is currently executing, false otherwise. 
	 */
	public boolean isRunning();
	
	/**
	 * Attempts to stop the query by calling cancel on it. This will also close
	 * any open streaming statements/connections.
	 */
	public void stopRunning();
	
	/**
	 * Sets the limit of rows a streaming query will retain as new results come
	 * in. If the limit is reached the oldest rows will be removed for the new 
	 * rows.
	 */
	public void setStreamingRowLimit(int streamingRowLimit);
	
	/**
	 * Gets the limit of rows a streaming query will retain as new results come
	 * in. If the limit is reached the oldest rows will be removed for the new 
	 * rows.
	 */
	public int getStreamingRowLimit();
	
	/**
	 * This listener will be notified when a new row limit has been defined
	 * for a query's result set.
	 */
	public PropertyChangeListener getRowLimitChangeListener();
	
	public void addPropertyChangeListener(PropertyChangeListener l);
	
	public void removePropertyChangeListener(PropertyChangeListener l);
    
}
