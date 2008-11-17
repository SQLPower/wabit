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

import java.awt.geom.Point2D;
import java.util.List;

import ca.sqlpower.wabit.WabitObject;


/**
 * A container model stores an object that has multiple items 
 * stored in different sections.
 */
public interface Container extends WabitObject {
	
	/**
	 * Defines the property change to be a name change on the container.
	 */
	public static final String CONTAINTER_ALIAS_CHANGED = "CONTAINER_ALIAS_CHANGED";
	public static final String CONTAINTER_ITEM_ADDED = "CONTAINER_ITEM_ADDED";
	public static final String CONTAINER_ITEM_REMOVED = "CONTAINER_ITEM_REMOVED";
	public static final String PROPERTY_TABLE_ADDED = "TABLE_ADDED";
	public static final String PROPERTY_TABLE_REMOVED = "TABLE_REMOVED";
	public static final String PROPERTY_WHERE_MODIFIED = "WHERE_MODIFIED";
	
	/**
	 * Gets all of the sections of the contained object.
	 */
	List<Item> getItems();
	
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

	void removeItem(Item item);

	void addItem(Item item);
	
	void setAlias(String alias);
	
	String getAlias();
	
	/**
	 * Sets the position of the container. This will allow any view to understand
	 * how the containers are laid out in relation to each other.
	 */
	void setPosition(Point2D p);
	
	/**
	 * Gets the position of the container. This will allow any view to understand
	 * how the containers are laid out in relation to each other.
	 */
	Point2D getPosition();

}