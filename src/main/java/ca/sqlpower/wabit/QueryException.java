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

/**
 * An exception that is caused by any failure of data gathering by any
 * means implemented by the Query interface.
 */
public class QueryException extends Exception {

    private final String query;

    /**
     * Creates a new query exception.
     * 
     * @param query Some text describing the query that failed. For an SQL query
     * exception, this should be the SQL query that did not execute.
     * @param cause The underlying cause of the query failure. For an SQL query
     * exception, this should be the SQLException from the database.
     */
    public QueryException(String query, Throwable cause) {
        super(cause);
        this.query = query;
    }
    
    /**
     * Returns the text of the query that failed.
     */
    public String getQuery() {
        return query;
    }
}
