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

package ca.sqlpower.wabit;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.sql.SPDataSource;

/**
 * An implementation of {@link WabitDataSource} that deals with data sources
 * that are accessed using the JDBC API.
 */
public class JDBCDataSource extends AbstractWabitObject implements WabitDataSource {

	/**
	 * Underlying {@link SPDataSource} object that actually contains all the
	 * database connection info.
	 */
	private SPDataSource dataSource;

	public JDBCDataSource(SPDataSource ds) {
	    this.dataSource = ds;
	}
	
	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		throw new UnsupportedOperationException("This object doesn't have children at all");
	}

	public List<WabitObject> getChildren() {
		return Collections.emptyList();
	}
}
