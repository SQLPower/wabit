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

import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * This container item wraps a SQLColumn for use in a ContainerPane.
 */
public class SQLObjectItem extends AbstractItem {
	
	private SQLObject sqlObject;
	
	private String alias;
	
	private boolean selected;

	private String where;

	public SQLObjectItem(SQLObject object) {
		sqlObject = object;
		this.alias = "";
		this.where = "";
		this.selected = false;
	}
	
	/**
	 * If a null uuid is given to this constructor a new UUID will
	 * be generated.
	 */
	public SQLObjectItem(String name, String uuid) {
		super(uuid);
		sqlObject = null;
		super.setName(name);
		this.alias = "";
		this.where = "";
		this.selected = false;
	}
	
	public String getName() {
		if (sqlObject == null) {
			return super.getName();
		}
		return sqlObject.getName();
	}
	
	public void setName(String name) {
		throw new IllegalStateException("Cannot set the name of a SQL object retrieved from the database through a query.");
	}
	
	public void setItem(SQLObject object) {
		sqlObject = object;
	}
	
	/**
	 * Since the sql objects are loaded lazily this can be a moderately long
	 * operation if the file was loaded but the sql objects themselves have
	 * not yet been loaded.
	 */
	public Object getItem() {
		if (sqlObject == null) {
			((TableContainer) getParent()).loadTableByQualifiedName();
		}
		return sqlObject;
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
		return (Container) getParent();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SQLObjectItem) {
			if (((SQLObjectItem) obj).getUUID().equals(getUUID())) {
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
