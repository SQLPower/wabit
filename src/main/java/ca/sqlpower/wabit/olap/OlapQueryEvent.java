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

package ca.sqlpower.wabit.olap;

import org.olap4j.CellSet;

/**
 * Event object that carries the notification of the fact that a certain
 * OlapQuery has recently been executed.
 * <p>
 * Instances of this class are immutable.
 */
public class OlapQueryEvent {

    /**
     * The {@link OlapQuery} that fired this event. Never null.
     */
    private final OlapQuery source;

    /**
     * The cellSet that has just been produced, or null if the source OlapQuery
     * is not currently able to produce a result.
     */
	private final CellSet cellSet;

    /**
     * Creates a new event object. As a best/expected practice, only one
     * instance of this event class should be created for each event--the same
     * instance should be passed to each of the current listeners.
     * 
     * @param source
     *            The OLAP query that produced this event. Must not be null.
     * @param cellSet
     *            The cell set that has just been produced. If the source
     *            OlapQuery is not currently able to produce a result, this
     *            value can be null.
     */
	public OlapQueryEvent(OlapQuery source, CellSet cellSet) {
	    if (source == null) {
	        throw new NullPointerException("Null source not allowed");
	    }
	    this.source = source;
		this.cellSet = cellSet;
	}

    /**
     * Returns the {@link OlapQuery} that fired this event. Never null.
     */
	public OlapQuery getSource() {
        return source;
    }

    /**
     * Returns the cellSet that has just been produced, or null if the source
     * OlapQuery is not currently able to produce a result.
     */
	public CellSet getCellSet() {
		return cellSet;
	}
}
