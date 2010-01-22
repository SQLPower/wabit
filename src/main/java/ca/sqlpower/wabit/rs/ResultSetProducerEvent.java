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

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;
import ca.sqlpower.sql.CachedRowSet;

/**
 * Event object that describes the outcome of a result set producer execution.
 */
@Immutable
public class ResultSetProducerEvent {

    private final ResultSetProducer source;
    private final ResultSetAndUpdateCountCollection results;

    /**
     * Creates a new event with the given parameters.
     * 
     * @param source
     *            The result set producer that plans to fire this event.
     * @param results
     *            The results themselves. Shared copies will be handed out to
     *            listeners.
     */
    public ResultSetProducerEvent(ResultSetProducer source, 
            ResultSetAndUpdateCountCollection results) {
        this.source = source;
        this.results = results;
    }

    /**
     * Returns a reference to the object that produced the result set and fired
     * this event.
     */
    @Nonnull
    public ResultSetProducer getSource() {
        return source;
    }

    /**
     * Returns a copy of the collection of result sets. The copy has a
     * collection of independent row cursor and other state (such as wasNull())
     * but shares the actual underlying data with (at least) all other invokers
     * of this method.
     * 
     * @return A copy containing mostly-independent copies of the new result
     *         sets, or null if an execution was attempted while the result set
     *         producer was not in a state where it could produce a result set.
     * @see CachedRowSet#createShared()
     */
    @Nullable
    public ResultSetAndUpdateCountCollection getResults() {
        if (results == null) return null;
        try {
			return new ResultSetAndUpdateCountCollection(results);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }
}
