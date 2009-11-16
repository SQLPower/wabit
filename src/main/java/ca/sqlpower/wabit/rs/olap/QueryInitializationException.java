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

package ca.sqlpower.wabit.rs.olap;

/**
 * This exception is thrown when a query is being initialized but 
 * failed. The failure could come from different places such as:
 * not being able to connect to a data source, not being able to 
 * find parts of the query in the data source. Any operation that
 * throws this exception can be retried later in case the connection
 * was temporarily missing or other temporary error-prone situations.
 */
public class QueryInitializationException extends Exception {
	
	public QueryInitializationException() {
		super ();
	}
	
	public QueryInitializationException(String message) {
		super(message);
	}
	
	public QueryInitializationException(Throwable t) {
		super(t);
	}
	
	public QueryInitializationException(String message, Throwable t) {
		super(message, t);
	}
}
