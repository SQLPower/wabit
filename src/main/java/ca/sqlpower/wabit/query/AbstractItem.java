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

package ca.sqlpower.wabit.query;

import ca.sqlpower.wabit.AbstractWabitObject;

public abstract class AbstractItem extends AbstractWabitObject implements Item {

	/**
	 * The width that this item should take up when displayed in a column of a table.
	 */
	private Integer columnWidth;
	
	public AbstractItem() {
		super();
	}
	
	public AbstractItem(String uuid) {
		super(uuid);
	}
	
	public void setColumnWidth(Integer width) {
		this.columnWidth = width;
	}
	
	public Integer getColumnWidth() {
		return columnWidth;
	}

}
