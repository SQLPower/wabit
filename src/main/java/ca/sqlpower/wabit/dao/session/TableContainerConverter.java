/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.dao.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.ConversionException;

import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.wabit.WabitSession;

public class TableContainerConverter implements BidirectionalConverter<String, TableContainer> {
	
	private final WabitSession session;

	public TableContainerConverter(WabitSession session) {
		this.session = session;
		
	}

	public TableContainer convertToComplexType(String convertFrom)
			throws ConversionException {
		String[] pieces = SessionPersisterUtils.splitByDelimiter(convertFrom, 5);
		
		JDBCDataSource ds = session.getDataSources().getDataSource(pieces[1], JDBCDataSource.class);
		SQLDatabase db = session.getContext().getDatabase(ds);
		List<SQLObjectItem> l = new ArrayList<SQLObjectItem>();
		TableContainer container = new TableContainer(pieces[0], db, pieces[2], pieces[3], pieces[4], l);
		
		return container;
	}

	public String convertToSimpleType(TableContainer convertFrom,
			Object... additionalInfo) {
		StringBuilder result = new StringBuilder();

		result.append(convertFrom.getUUID());
		result.append(DELIMITER);
		result.append(convertFrom.getDatabase().getName());
		result.append(DELIMITER);
		result.append(convertFrom.getName());
		result.append(DELIMITER);
		result.append(convertFrom.getSchema());
		result.append(DELIMITER);
		result.append(convertFrom.getCatalog());
		result.append(DELIMITER);
		result.append(convertFrom.getAlias());

		return result.toString();
	}

}
