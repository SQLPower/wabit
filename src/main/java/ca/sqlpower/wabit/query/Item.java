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

package ca.sqlpower.wabit.query;

import ca.sqlpower.wabit.WabitObject;


/**
 * A class implementing this interface wraps an item in a container.
 */
public interface Item extends WabitObject {
	
	/**
	 * Defines a property change of an alias on an item.
	 */
	public static final String PROPERTY_ALIAS = "ALIAS";
	
	/**
	 * Defines the item contained by this Item was added
	 * or removed from the select list.
	 */
	public static final String PROPERTY_SELECTED = "SELECTED";
	
	/**
	 * Defines a change of the where filer for this contained item.
	 */
	public static final String PROPERTY_WHERE = "WHERE";

	/**
	 * Defines a change to the item contained by this Item itself.
	 */
	public static final String PROPERTY_ITEM = "ITEM";
	
	/**
	 * Defines a change that the item this ItemPNode contains will be removed
	 * from its container immediately after this event is fired. The item is
	 * removed immediately after this event so the parent container will get the event.
	 */
	public static final String PROPERTY_ITEM_REMOVED = "ITEM_REMOVED";

	Object getItem();
	
	Container getContainer();
	
	void setAlias(String alias);
	
	String getAlias();
	
	void setWhere(String where);
	
	String getWhere();
	
	void setSelected(boolean selected);
	
	boolean isSelected();
	
	Integer getColumnWidth();
	
	void setColumnWidth(Integer width);
	
}
