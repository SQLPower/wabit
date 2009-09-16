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

import java.sql.ResultSet;

import javax.annotation.Nonnull;

import ca.sqlpower.wabit.WabitBackgroundWorker;

/**
 * Interface that provides all necessary methods for working with any type of
 * WabitObject that can produce a ResultSet.
 */
public interface ResultSetProducer extends WabitBackgroundWorker {

    /**
     * Executes the current query represented by this object, returning the
     * results from the query's execution.
     * <p>
     * Every call to this method causes a ResultSetEvent to be fired, whether or
     * not the call simply returns the same cached results as a previous
     * invocation.
     * <p>
     * If the query is in a state where it can't be executed (because it is not
     * sufficiently configured to issue a sensible query, or because the
     * connection to the data source can't be established), the event will still
     * be fired if it results in a state change from the previous execution
     * attempt. In these cases, the event will deliver a null ResultSet to
     * listeners.
     * <p>
     * <b>Implementations of ResultSetProducer are allowed to fire the
     * ResultSetEvent while the query is locked against concurrent and/or
     * recursive execution, so it is vitally important that ResultSetListeners
     * do not attempt to re-execute the query in response to any
     * ResultSetEvent.</b> Such behaviour by ResultSetListeners is unsafe in
     * general, and may cause deadlock.
     * 
     * @return The results of executing of the query. If the query is not
     *         currently in a state where it can be executed, this method
     *         returns null and fires a ResultSetEvent with a null result set.
     * @throws ResultSetProducerException
     *             If there was any problem obtaining the result set
     * @throws InterruptedException
     *             If the calling thread is interrupted while blocked waiting
     *             for another call to execute() to complete.
     */
    ResultSet execute() throws ResultSetProducerException, InterruptedException;

    /**
     * Adds the given listener to this result set producer's list of interested
     * parties. Each listener on the list receives an event whenever a new
     * result set is available (as a result of the query be executed).
     * 
     * @param listener The listener to add (must not be null).
     */
    void addResultSetListener(@Nonnull ResultSetListener listener);

    /**
     * Removes the given listener from the listener list. Has no effect if the
     * given listener was not already on the list.
     * 
     * @param listener The listener to remove. Nulls are ignored.
     */
    void removeResultSetListener(ResultSetListener listener);

}