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

import org.apache.commons.beanutils.ConversionException;

import ca.sqlpower.query.Item;
import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.StringItem;

public class ItemConverter implements BidirectionalConverter<String, Item> {

	public Item convertToComplexType(String convertFrom)
			throws ConversionException {
		String[] pieces = SessionPersisterUtils.splitByDelimiter(convertFrom, 3);
		
		if (pieces[0].equals(SQLObjectItem.class.getSimpleName())) {
			return new SQLObjectItem(pieces[1], pieces[2]);
		} else if (pieces[0].equals(StringItem.class.getSimpleName())) {
			return new StringItem(pieces[1], pieces[2]);
		} else {
			throw new IllegalArgumentException("Unknown class of item for " + pieces[0] + " to convert " + convertFrom);
		}
	}

	public String convertToSimpleType(Item convertFrom,
			Object... additionalInfo) {
		StringBuffer buffer = new StringBuffer();
		
		if (convertFrom instanceof SQLObjectItem) {
			buffer.append(SQLObjectItem.class.getSimpleName()); 
		} else if (convertFrom instanceof StringItem) {
			buffer.append(StringItem.class.getSimpleName());
		} else {
			throw new IllegalArgumentException("Unknown item type of " + convertFrom.getClass());
		}
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getName());
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getUUID());
		
		return buffer.toString();
	}

}
