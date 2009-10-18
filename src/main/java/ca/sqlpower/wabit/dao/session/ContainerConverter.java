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
import java.util.List;

import org.apache.commons.beanutils.ConversionException;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.wabit.WabitSession;

public class ContainerConverter implements BidirectionalConverter<String, Container> {
	
	/**
	 * New delimiter that must be different from {@link BidirectionalConverter#DELIMITER}.
	 */
	private static final String ITEM_DELIMITER = ">" + DELIMITER + "<";
	
	private final WabitSession session;
	
	private final ItemConverter itemConverter;

	public ContainerConverter(WabitSession session) {
		this.session = session;
		itemConverter = new ItemConverter(session.getWorkspace());
	}

	public Container convertToComplexType(String convertFrom)
			throws ConversionException {
		
		int firstSeparator = convertFrom.indexOf(DELIMITER);
		
		if (firstSeparator == -1) {
			throw new IllegalArgumentException("Cannot find the class type for the string " + 
					convertFrom);
		}
		
		String className = convertFrom.substring(0, firstSeparator);
		String pattern = convertFrom.substring(firstSeparator + 1);
		
		if (className.equals(ItemContainer.class.getSimpleName())) {
			String[] pieces = SessionPersisterUtils.splitByDelimiter(pattern, 2);

			ItemContainer container = new ItemContainer(pieces[0], pieces[1]);

			return container;
		} else if (className.equals(TableContainer.class.getSimpleName())) {
			String[] tableAndItems = pattern.split(ITEM_DELIMITER);
			
			String[] pieces = SessionPersisterUtils.splitByDelimiter(tableAndItems[0], 5);
			
			JDBCDataSource ds = session.getDataSources().getDataSource(pieces[1], JDBCDataSource.class);
			SQLDatabase db = session.getContext().getDatabase(ds);
			if (db == null) {
				throw new NullPointerException("The data source for name " + pieces[1] + " cannot be found.");
			}
			List<SQLObjectItem> l = new ArrayList<SQLObjectItem>();
			for (int i = 1; i < tableAndItems.length; i++) {
				l.add((SQLObjectItem) itemConverter.convertToComplexType(tableAndItems[i]));
			}
			
			TableContainer container = new TableContainer(pieces[0], db, pieces[2], pieces[3], pieces[4], l);
			return container;
		} else {
			throw new IllegalArgumentException("Unknown container class " + className + " for " + convertFrom);
		}
		
	}

	public String convertToSimpleType(Container convertFrom,
			Object... additionalInfo) {
		StringBuilder result = new StringBuilder();
		
		if (convertFrom.getClass().equals(ItemContainer.class)) {
			result.append(ItemContainer.class.getSimpleName());
			result.append(DELIMITER);
			result.append(convertFrom.getName());
			result.append(DELIMITER);
			result.append(convertFrom.getUUID());
		} else if (convertFrom.getClass().equals(TableContainer.class)) {
			TableContainer container = (TableContainer) convertFrom;
			result.append(TableContainer.class.getSimpleName());
			result.append(DELIMITER);
			result.append(convertFrom.getUUID());
			result.append(DELIMITER);
			result.append(container.getDatabase().getName());
			result.append(DELIMITER);
			result.append(convertFrom.getName());
			result.append(DELIMITER);
			result.append(container.getSchema());
			result.append(DELIMITER);
			result.append(container.getCatalog());
			
			for (Item i : container.getItems()) {
				result.append(ITEM_DELIMITER);
				result.append(itemConverter.convertToSimpleType(i));
			}
		} else {
			throw new IllegalArgumentException("Unknown container type to convert: " + 
					convertFrom.getClass());
		}
		
		return result.toString();
	}

}
