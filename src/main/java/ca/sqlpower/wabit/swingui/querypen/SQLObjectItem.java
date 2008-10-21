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

import ca.sqlpower.architect.SQLObject;

/**
 * This container item wraps a SQLColumn for use in a ContainerPane.
 */
public class SQLObjectItem implements Item {
	
	private final SQLObject sqlObject;
	
	private String alias;
	
	public SQLObjectItem(SQLObject object) {
		sqlObject = object;
	}
	
	public String getName() {
		return sqlObject.getName();
	}
	
	public Object getItem() {
		return sqlObject;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}

}
