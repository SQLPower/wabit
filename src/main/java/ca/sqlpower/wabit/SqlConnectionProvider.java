/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;

public interface SqlConnectionProvider extends SQLDatabaseMapping {

	/**
	 * Creates a SQL connection.
	 * @param dataSource The data source to use.
	 */
	public Connection createConnection(JDBCDataSource dataSource) throws SQLObjectException; 
	
	public PreparedStatement createPreparedStatement(
			JDBCDataSource dataSource,
			String sql,
			SPVariableHelper helper) throws SQLObjectException; 
	
}
