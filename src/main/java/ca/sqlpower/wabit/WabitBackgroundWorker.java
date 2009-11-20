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


/**
 * A Wabit Background Worker is a Wabit Object that encapsulates some sort
 * of background processing. For example, OlapQuery is a type of WabitObject
 * that can handle the running of an MDX query in the background. QueryCache
 * is the SQL-centred cousin of OlapQuery, and its background task is executing
 * SQL queries.
 */
public interface WabitBackgroundWorker extends WabitObject {

    /**
     * Returns true if this Wabit object is doing something in the background.
     * <p>
     * Note to implementers: this method is likely to be used for inter-thread
     * communication. Be sure to use proper synchronization when passing the
     * flag between threads!
     */
    boolean isRunning();

    /**
     * If this worker is currently doing something in the background, calling
     * this method requests the background processing be canceled. It is not
     * guaranteed that cancellation will happen immediately or at all, because
     * the background processing might involve something like database I/O with
     * a JDBC driver that doesn't properly support cancellation.
     */
    void cancel();
}
