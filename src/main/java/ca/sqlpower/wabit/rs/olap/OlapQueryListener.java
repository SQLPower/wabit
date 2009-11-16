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
 * An event listener interface to listen for important events from an
 * {@link OlapQuery} that a listening class may be interested in, particularly
 * UI classes.
 */
public interface OlapQueryListener {

    /**
     * Called when {@link OlapQuery#executeOlapQuery()} has finished, and provides a
     * reference to the cell set produced by the execution of the query.
     * 
     * <h2>Thread Safety</h2> If you are consuming this event from a
     * single-threaded subsystem (such as a Swing user interface), beware that
     * this event might be delivered on a worker thread!
     */
	public void queryExecuted(OlapQueryEvent e);

}
