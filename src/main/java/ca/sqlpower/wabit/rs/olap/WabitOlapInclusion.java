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

import org.apache.log4j.Logger;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection;
import org.olap4j.query.Selection.Operator;

/**
 * This is a dummy class used to differentiate between inclusions and exclusions
 */
public class WabitOlapInclusion extends WabitOlapSelection {

	private static final Logger logger = Logger
			.getLogger(WabitOlapInclusion.class);
	
	public WabitOlapInclusion(WabitOlapSelection selection) {
		super(selection);
	}

	public WabitOlapInclusion(Selection selection) {
		super(selection);
	}

	public WabitOlapInclusion(Operator operator, String uniqueMemberName) {
		super(operator, uniqueMemberName);
	}
	
	/**
	 * Initializes the WabitOlapSelection, and finds the wrapped Selection based
	 * on the given unique member name.
	 */
	void init(OlapQuery query) {
		logger.debug("Initializing Selection" + uniqueMemberName);
		Member member = query.findMember(uniqueMemberName);
		
		for (Selection s : ((WabitOlapDimension) getParent()).getDimension().getInclusions()) {
			if (s.getMember().equals(member)) {
				selection = s;
			}
		}
		selection.setOperator(operator);
		initialized = true;
	}

}
