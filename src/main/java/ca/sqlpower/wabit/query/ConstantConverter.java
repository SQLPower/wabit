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

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;

/**
 * This constant converter will convert constants defined by users and the system to
 * database specific constants. This is necessary for pre-defined constants in Wabit
 * which are not defined on all database platforms supported.
 * <p>
 * For example: MySQL, Oracle, and Postgres support current_time and current_date but
 * SQL Server only supports current_timestamp.
 */
public class ConstantConverter {
	
	private static final Logger logger = Logger.getLogger(ConstantConverter.class);

	public static ConstantConverter getConverter(SPDataSource ds) {
		if (ds == null) {
			return new ConstantConverter();
		}
		if (ds.getDriverClass().toLowerCase().contains("sqlserver")) {
			return new SQLServerConstantConverter();
		} else if (ds.getDriverClass().toLowerCase().contains("mysql")) {
			return new MySQLConstantConverter();
		} else {
			return new ConstantConverter();
		}
	}
	
	public String getName(Item col) {
		return col.getName();
	}
}
