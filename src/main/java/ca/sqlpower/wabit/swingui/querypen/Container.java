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

import java.util.List;

/**
 * A container model stores an object that has multiple items 
 * stored in different sections.
 */
public interface Container {
	
	/**
	 * Gets all of the sections of the contained object.
	 */
	List<Section> getSections();
	
	/**
	 * Gets the name of the object this container holds.
	 */
	String getName();

	/**
	 * Gets the Item wrapper that contains the given item. Returns null if the
	 * object is not contained in this container.
	 */
	Item getItem(Object item);
	
	/**
	 * Gets the object this container is modeling. This object will be the object
	 * that contains the children wrapped by the items and possibly contains more.
	 */
	Object getContainedObject();

}
