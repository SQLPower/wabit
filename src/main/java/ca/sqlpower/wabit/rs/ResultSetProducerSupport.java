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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Convenient implementation support for the {@link ResultSetProducer} interface.
 */
public class ResultSetProducerSupport {

    private final ResultSetProducer eventSource;
    private final List<ResultSetListener> listeners = new ArrayList<ResultSetListener>();

    /**
     * Count of the number of times the {@link #fireResultSetEvent(ResultSet)}
     * method has been called.
     */
    private long eventsFired;
    
    public ResultSetProducerSupport(@Nonnull ResultSetProducer eventSource) {
        this.eventSource = eventSource;
        if (eventSource == null) {
            throw new NullPointerException("Null source not allowed");
        }
    }
    
    /** @see ResultSetProducer#addResultSetListener(ResultSetListener) */
    public void addResultSetListener(@Nonnull ResultSetListener listener) {
        if (listener == null) {
            throw new NullPointerException("Null listener not allowed");
        }
        listeners.add(listener);
    }

    /** @see ResultSetProducer#removeResultSetListener(ResultSetListener) */
    public void removeResultSetListener(ResultSetListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of the new result set. The event's
     * source is the ResultSetProducer specified to this instance's constructor.
     * <p>
     * Calling this method has the side effect of incrementing the eventsFired
     * property by exactly 1.
     * 
     * @param results
     *            The new result sets. If it is already a CachedRowSet, it will
     *            be embedded in the event object as-is; otherwise, it will be
     *            copied into a new CachedRowSet. In the latter case, the row
     *            cursor of <code>results</code> will have been moved to the
     *            <i>afterLast</i> position as a side effect of populating the
     *            new CachedRowSet. Furthermore, for the CachedRowSet to
     *            populate properly, the row cursor of <code>results</code>
     *            should be at the <i>beforeFirst</i> position prior to calling
     *            this method.
     */
    public void fireResultSetEvent(@Nullable ResultSetAndUpdateCountCollection results) {
        
    	eventsFired++;
        
        ResultSetProducerEvent evt = new ResultSetProducerEvent(eventSource, results);
        
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).resultSetProduced(evt);
        }
    }

    /**
     * Returns the count of how many times
     * {@link #fireResultSetEvent(ResultSet)} has been called.
     */
    public long getEventsFired() {
        return eventsFired;
    }
}
