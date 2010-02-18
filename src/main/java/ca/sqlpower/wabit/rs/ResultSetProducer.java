/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of SQL Power Library.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ca.sqlpower.object.SPVariableHelper;

/**
 * Interface that provides all necessary methods for working with any type of
 * SPObject that can produce a ResultSet.
 */
public interface ResultSetProducer {

    
	/**
     * Executes the current query represented by this object, returning the
     * results from the query's execution.
     * 
     * Takes a variables context to produce this result set from. Can be null,
     * in which case it is assumed that the resultset was produced in it's native 
     * context.
     * 
     * Takes a listener as an argument to register before the result set starts
     * populating on a background thread. can be null. The calling code can
     * register a listener on the handle later if it wants to.
     * 
     * @return The results of executing of the query. If the query is not
     *         currently in a state where it can be executed, this method
     *         returns null.
     * @throws ResultSetProducerException
     *             If there was any problem obtaining the result set
     */
    ResultSetHandle execute(
    		@Nullable SPVariableHelper variablesContext,
    		@Nullable ResultSetListener listener) throws ResultSetProducerException;
    
    

    /**
     * Adds the given listener to this result set producer's list of interested
     * parties. Each listener on the list receives an event whenever a new
     * result set is available (as a result of the query be executed).
     * 
     * @param listener The listener to add (must not be null).
     */
    void addResultSetProducerListener(@Nonnull ResultSetProducerListener listener);

    /**
     * Removes the given listener from the listener list. Has no effect if the
     * given listener was not already on the list.
     * 
     * @param listener The listener to remove. Nulls are ignored.
     */
    void removeResultSetProducerListener(ResultSetProducerListener listener);

    /**
     * Returns true if at least one of the distributed handles is still running.
     * Do not confise these listeners with those that listen to the 
     * handles. 
     */
    boolean isRunning();

    /**
     * Calling this method will stop the execution of all distributed handles.
     */
    void cancel();
}