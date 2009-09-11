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

import org.olap4j.Axis;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.query.SortOrder;

import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * A MemberAction that sorts the query results by a given axis and sort order on
 * the given member if it's an instance of Measure.
 */
public class SortByMeasureAction extends MemberAction {

	/**
	 * The axis to sort
	 */
	private final Axis axis;
	
	/**
	 * The order to sort by
	 */
	private final SortOrder order;

	public SortByMeasureAction(WabitSwingSession session, String name, OlapQuery query, 
			Member member, Axis axis, SortOrder order) {
		super(session, name, query, member);
		this.axis = axis;
		this.order = order;
	}

	@Override
	protected void performMemberAction(Member member, OlapQuery query)
			throws QueryInitializationException {
		if (member instanceof Measure) {
			query.sortBy(axis, order, (Measure) member);
		}
	}

}
