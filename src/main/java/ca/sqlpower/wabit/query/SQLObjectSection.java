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

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;

/**
 * This is a generic Section that contains a SQLObject as it's parent. 
 * It uses the parent's child list to create the sections.
 */
public class SQLObjectSection implements Section {
	
	private final Container parent;
	
	private final List<Item> itemList;
	
	public SQLObjectSection(Container parent, SQLObject containedObject) {
		this.parent = parent;
		itemList = new ArrayList<Item>();
		try {
			for (Object child : containedObject.getChildren()) {
				SQLObjectItem item = new SQLObjectItem((SQLObject)child);
				item.setParent(this);
				itemList.add(item);
				
			}
		} catch (ArchitectException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Item> getItems() {
		return itemList;
	}
	
	public Container getParent() {
		return parent;
	}

	public void addItem(Item stringItem) {
		throw new IllegalStateException("Cannot add arbitrary items to a SQLObject.");
	}

	public void removeItem(Item item) {
		throw new IllegalStateException("Cannot remove arbitrary items from a SQLObject.");		
	}
}
