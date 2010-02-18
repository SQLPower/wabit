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

package ca.sqlpower.wabit.swingui.olap.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * An abstract base action meant to be extended by actions that modify
 * the query.
 * <p>
 * There's a specialized subclass, {@link MemberAction}, for actions that
 * work with a specific member of a query. 
 */
public abstract class OlapQueryAction extends AbstractAction {

    /**
     * The {@link OlapQuery} object that this action modifies.
     */
    private final OlapQuery query;
    
    /**
     * The session this action belongs to.
     */
    private final WabitSwingSession session;
    
    protected OlapQueryAction(WabitSwingSession session, OlapQuery query, String name) {
        super(name);
        this.session = session;
        this.query = query;
        setEnabled(query != null);
    }
    
    /**
     * Returns the {@link OlapQuery} object that this action modifies.
     */
    public OlapQuery getQuery() {
        return query;
    }
    
    public final void actionPerformed(ActionEvent e) {
    	try {
    		performOlapQueryAction(query);
    	} catch (Exception ex) {
    		throw new RuntimeException(ex);
    	}
    }

    /**
     * Manipulates the query but does not execute it. The
     * {@link #actionPerformed(ActionEvent)} method will begin background
     * execution of the query after calling this method.
     * 
     * @param query
     *            The query to manipulate. Don't execute it!
     *            <p>
     *            This is the same query as returned by {@link #getQuery()};
     *            it's provided for your convenience.
     * @throws QueryInitializationException
     *             If the query failed to initialize itself as a side effect of
     *             manipulating it.
     */
    protected abstract void performOlapQueryAction(OlapQuery query)
        throws QueryInitializationException;
}
