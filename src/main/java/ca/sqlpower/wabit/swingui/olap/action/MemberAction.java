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

import org.olap4j.metadata.Member;

import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * Abstract base action for all of the actions that make modifications to an
 * {@link OlapQuery} based on a provided {@link Member}
 */
public abstract class MemberAction extends OlapQueryAction {

    /**
     * The {@link Member} that this DrillAction is being performed on.
     */
    private final Member member;

    protected MemberAction(WabitSwingSession session, String name, OlapQuery query, Member member) {
        super(session, query, name);
        this.member = member;
    }
    
    @Override
    protected final void performOlapQueryAction(OlapQuery query)
        throws QueryInitializationException {
    	performMemberAction(member, query);
    }

    /**
     * Subclass hook. When implementing this method, manipulate the given member
     * in the given query in whatever way makes sense for your specific action.
     * Do not execute the query; this will be done after you return.
     * 
     * @param member
     *            The member that was selected as the subject of this action.
     *            <p>
     *            This is the same member as returned by {@link #getMember()};
     *            it's provided for your convenience.
     * @param query
     *            The query to manipulate. Don't execute it!
     *            <p>
     *            This is the same query as returned by {@link #getQuery()};
     *            it's provided for your convenience.
     * @throws QueryInitializationException
     *             If the query failed to initialize itself as a side effect of
     *             manipulating it.
     */
    protected abstract void performMemberAction(Member member, OlapQuery query)
        throws QueryInitializationException;

	public Member getMember() {
        return member;
    }
}
