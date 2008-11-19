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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * This container is used to hold a generic list of items in
 * the same section.
 */
public class ItemContainer extends AbstractWabitObject implements Container {
	
	private static final Logger logger = Logger.getLogger(ItemContainer.class);

	/**
	 * The user visible name to this container.
	 */
	private String name;

	/**
	 * This section holds all of the Items containing the strings in this
	 * container.
	 */
	private final List<Item> itemList;
	
	private String alias;
	
	private Point2D position;
	
	public ItemContainer(String name) {
		this.name = name;
		itemList = new ArrayList<Item>();
		logger.debug("Container created.");
		position = new Point(0, 0);
	}
	
	public Object getContainedObject() {
		return Collections.unmodifiableList(itemList);
	}

	public Item getItem(Object item) {
		for (Item i : itemList) {
			if (i.getItem().equals(item)) {
				return i;
			}
		}
		return null;
	}
	
	public void addItem(Item item) {
		itemList.add(item);
		item.setParent(this);
		firePropertyChange(Container.CONTAINTER_ITEM_ADDED, null, item);
		fireChildAdded(Item.class, item, itemList.indexOf(item));
	}
	
	public void removeItem(Item item) {
		int index = itemList.indexOf(item);
		itemList.remove(item);
		firePropertyChange(Container.CONTAINER_ITEM_REMOVED, item, null);
		fireChildRemoved(Item.class, item, index);
	}

	public String getName() {
		return name;
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(itemList);
	}

	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias of the container. Null is not allowed.
	 */
	public void setAlias(String alias) {
		String oldAlias = this.alias;
		if (alias.equals(oldAlias)) {
			return;
		}
		this.alias = alias;
		firePropertyChange(CONTAINTER_ALIAS_CHANGED, oldAlias, alias);
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D p) {
		position = p;
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return itemList;
	}

}
