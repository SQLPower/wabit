/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui;

import ca.sqlpower.architect.SQLColumn;


/**
 * A simple SQL object that joins two columns together in a select
 * statement. This will also store how the two columns are being
 * compared. 
 */
public class SQLJoin {
	
	/**
	 * The left column of this join.
	 */
	private final SQLColumn leftColumn;

	/**
	 * The right column in the join.
	 */
	private final SQLColumn rightColumn;

	public SQLJoin(SQLColumn leftColumn, SQLColumn rightColumn) {
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
	}

	public SQLColumn getLeftColumn() {
		return leftColumn;
	}
	
	public SQLColumn getRightColumn() {
		return rightColumn;
	}

	/**
	 * This will return the comparator between the two columns.
	 * 
	 * XXX At current this is always = but later it could be
	 * things like 'LIKE'.
	 * @return
	 */
	public String getComparator() {
		return "=";
	}
}
