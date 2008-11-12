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
import java.util.List;

import ca.sqlpower.wabit.swingui.Item;
import ca.sqlpower.wabit.swingui.Section;

/**
 * This class stores generic strings, such as functions, constants and any other
 * string that can be included in a SQL statement, but is not a column.
 */
public class StringItem implements Item {
	
	private String name;
	private String alias;
	private Section parent;
	private final List<PropertyChangeListener> changeListeners;
	private boolean selected;
	private String where;

	public StringItem(String name) {
		this.name = name;
		this.alias = "";
		this.where = "";
		this.selected = false;
		changeListeners = new ArrayList<PropertyChangeListener>();
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
	
	public void setName(String name) {
		String oldName = this.name;
		if (name.equals(oldName)) {
			return;
		}
		this.name = name;
		for (PropertyChangeListener l : changeListeners) {
			l.propertyChange(new PropertyChangeEvent(this, PROPERTY_ITEM, oldName, name));
		}
	}
	
	public void addChangeListener(PropertyChangeListener l) {
		changeListeners.add(l);
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void removeChangeListener(PropertyChangeListener l) {
		changeListeners.remove(l);
	}
	
	public void setAlias(String alias) {
		String oldAlias = this.alias;
		if(alias.equals(oldAlias)) {
			return;
		}
		this.alias = alias;
		for (PropertyChangeListener l : changeListeners) {
			l.propertyChange(new PropertyChangeEvent(this, PROPERTY_ALIAS, oldAlias, alias));
		}
	}
	
	public String getWhere() {
		return where;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		boolean oldSelect = this.selected;
		if (oldSelect == selected) {
			return;
		}
		this.selected = selected;
		for (PropertyChangeListener l : changeListeners) {
			l.propertyChange(new PropertyChangeEvent(this, PROPERTY_SELECTED, oldSelect, selected));
		}
	}
	
	public void setWhere(String where) {
		String oldWhere = this.where;
		if (where.equals(oldWhere)) {
			return;
		}
		this.where = where;
		for (PropertyChangeListener l : changeListeners) {
			l.propertyChange(new PropertyChangeEvent(this, PROPERTY_WHERE, oldWhere, where));
		}
		
	}

}
