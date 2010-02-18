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


public interface ResultSetListener {

    /**
     * Called every time a {@link ResultSetProducer}'s has been populated 
     * with fresh data.
     * 
     * @param evt
     *            The result set event which gives more information about the
     *            event, including access to the newly-produced result set.
     */
    void newData(ResultSetEvent evt);
    
    /**
     * Called when the result set population is complete.
     * @param evt
     *            The result set event which gives more information about the
     *            event, including access to the newly-produced result set.
     */
    void executionComplete(ResultSetEvent evt);
    
    /**
     * Gets thrown when this handle starts executing.
     * 
     * This is mostly used internally and is not of any use to the
     * external code, since by the time this event gets fired,
     * the processing will most likely be done already.
     * 
     * @param evt
     * 			The event.
     */
    void executionStarted(ResultSetEvent evt);
}
