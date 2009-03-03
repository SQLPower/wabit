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

import java.sql.ResultSet;
import java.sql.SQLException;

import ca.sqlpower.sql.SPDataSource;

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

	void setName(String string);

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
	 * Returns the session this query is contained in. Used for copying
	 * queries.
	 */
	public WabitSession getSession();
	
	/**
	 * Sets the session for this query if the query is being moved to a different
	 * session or is being imported into a new session from an old session.
	 */
	public void setSession(WabitSession session);
	
	/**
	 * Call this when the query is to be disposed of.
	 */
	public void cleanup();
    
}
