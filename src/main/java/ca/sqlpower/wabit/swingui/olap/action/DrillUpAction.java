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

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;

import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;

/**
 * A Member action that replaces the root of the hierarchy of the given member
 * with that of the given ancestor member, and all ancestor members in all
 * levels in between the target ancestor's level and the given member's level
 * will be added to the query selection. Note that if the given ancestor member
 * actually is not an ancestor, then the query will not be changed.
 */
public class DrillUpAction extends MemberAction {

    private Member targetAncestor;

	/**
	 * @param query
	 *            The query whose selection will be modified
	 * @param member
	 *            The member whose ancestor Members will be added to the query
	 *            selection
	 * @param targetAncestor
	 *            The ancestor Member of member that will be set as the root
	 *            selection in member's hierarchy
	 */
	public DrillUpAction(OlapQuery query, Member member, Member targetAncestor) {
        super("Drill up to '" + targetAncestor.getName() + "'", query, member);
        this.targetAncestor = targetAncestor;
    }

	@Override
	protected void performMemberAction(Member member, OlapQuery query) throws OlapException, QueryInitializationException {
		query.drillUpTo(member, targetAncestor);
		query.execute();
	}
}
