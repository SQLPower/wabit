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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.wabit.swingui.Container;
import ca.sqlpower.wabit.swingui.Item;
import ca.sqlpower.wabit.swingui.Section;

/**
 * A model for the {@link ContainerPane}. This will store objects of a defined type and
 * can be grouped when adding the items to the model.
 *
 * @param <C> The type of object this model will store.
 */
public class TableContainer implements Container {

	private final SQLTable table;
	
	/**
	 * The section object that contains all of the
	 * columns of the table.
	 */
	private final Section section;
	
	private String alias;

	private final List<PropertyChangeListener> modelListeners;
	
	public TableContainer(SQLTable t) {
		table = t;
		section = new SQLObjectSection(this, table.getColumnsFolder());
		modelListeners = new ArrayList<PropertyChangeListener>();
	}
	
	public List<Section> getSections() {
		return Collections.singletonList(section);
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
		for (PropertyChangeListener l : modelListeners) {
			l.propertyChange(new PropertyChangeEvent(this, CONTAINTER_ALIAS_CHANGED, oldAlias, alias));
		}
	}

	public Item getItem(Object item) {
		for (Item i : section.getItems()) {
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


	public void addChangeListener(PropertyChangeListener l) {
		modelListeners.add(l);
	}

	public void removeChangeListener(PropertyChangeListener l) {
		modelListeners.remove(l);
	}

}
