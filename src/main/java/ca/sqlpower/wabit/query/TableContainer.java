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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * A model that stores SQLTable elements. This will store objects of a defined type and
 * can be grouped when adding the items to the model.
 *
 * @param <C> The type of object this model will store.
 */
public class TableContainer extends AbstractWabitObject implements Container {
	
	private static final Logger logger = Logger.getLogger(TableContainer.class);

	private final SQLTable table;
	
	/**
	 * The list contains all of the columns of the table.
	 */
	private final List<Item> itemList;
	
	private String alias;
	
	private Point2D position;

	public TableContainer(SQLTable t) {
		table = t;
		alias = "";
		itemList = new ArrayList<Item>();
		try {
			for (Object child : t.getColumnsFolder().getChildren()) {
				SQLObjectItem item = new SQLObjectItem((SQLObject)child);
				item.setParent(this);
				itemList.add(item);
				
			}
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}
		position = new Point2D.Double(0, 0);
	}
	
	public List<Item> getItems() {
		return Collections.unmodifiableList(itemList);
	}
	
	public String getName() {
		return table.getName();
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
		logger.debug("Alias set to " + alias);
	}

	public Item getItem(Object item) {
		for (Item i : itemList) {
			if (i.getItem() == item) {
				return i;
			}
		}
		return null;
	}

	public Object getContainedObject() {
		return table;
	}

	public void addItem(Item item) {
		throw new IllegalStateException("Cannot add arbitrary items to a SQLObject.");		
	}

	public void removeItem(Item item) {
		throw new IllegalStateException("Cannot remove arbitrary items from a SQLObject.");		
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
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
