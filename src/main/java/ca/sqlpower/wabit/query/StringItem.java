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

import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;


/**
 * This class stores generic strings, such as functions, constants and any other
 * string that can be included in a SQL statement, but is not a column.
 */
public class StringItem extends AbstractWabitObject implements Item {
	
	private String alias;
	private boolean selected;
	private String where;

	public StringItem(String name) {
		this(name, null);
	}
	
	/**
	 * If a null uuid is given a new UUID will be generated.
	 */
	public StringItem(String name, String uuid) {
		super(uuid);
		this.alias = "";
		this.where = "";
		this.selected = false;
		setName(name);
	}
	
	public Object getItem() {
		return getName();
	}

	public void setName(String name) {
		String oldName = getName();
		if (name.equals(oldName)) {
			return;
		}
		super.setName(name);
		firePropertyChange(PROPERTY_ITEM, oldName, name);
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		String oldAlias = this.alias;
		if(alias.equals(oldAlias)) {
			return;
		}
		this.alias = alias;
		firePropertyChange(PROPERTY_ALIAS, oldAlias, alias);
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
		firePropertyChange(PROPERTY_SELECTED, oldSelect, selected);
	}
	
	public void setWhere(String where) {
		String oldWhere = this.where;
		if (where.equals(oldWhere)) {
			return;
		}
		this.where = where;
		firePropertyChange(PROPERTY_WHERE, oldWhere, where);
	}
	
	public boolean allowsChildren() {
		return false;
	}
	
	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}
	
	public List<? extends WabitObject> getChildren() {
		return null;
	}
	public Container getContainer() {
		return (Container)getParent();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringItem) {
			if (((StringItem) obj).getUUID().equals(getUUID())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + getUUID().hashCode();
		return result;
	}

}
