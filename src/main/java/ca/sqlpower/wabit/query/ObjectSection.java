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
import java.util.Collections;
import java.util.List;


/**
 * This is a default section that can contain any type of object wrapped in an
 * Item.
 */
public class ObjectSection implements Section {
	
	private final List<Item> itemList;
	private Container parent;
	
	public ObjectSection() {
		itemList = new ArrayList<Item>();
	}

	public void addItem(Item item) {
		itemList.add(item);
		item.setParent(this);
	}
	
	public void removeItem(Item item) {
		itemList.remove(item);
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(itemList);
	}

	public Container getParent() {
		return parent;
	}
	
	public void setParent(Container parent) {
		this.parent = parent;
	}

}
