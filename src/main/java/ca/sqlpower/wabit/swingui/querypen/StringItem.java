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

package ca.sqlpower.wabit.swingui.querypen;

import ca.sqlpower.wabit.swingui.Item;
import ca.sqlpower.wabit.swingui.Section;

/**
 * This class stores generic strings, such as functions, constants and any other
 * string that can be included in a SQL statement, but is not a column.
 */
public class StringItem implements Item {
	
	private final String name;
	private Section parent;

	public StringItem(String name) {
		this.name = name;
	}
	public Object getItem() {
		return name;
	}

	public String getName() {
		return name;
	}

	public Section getParent() {
		return parent;
	}
	
	public void setParent(Section parent) {
		this.parent = parent;
	}

}
