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

import java.util.List;

import org.apache.commons.beanutils.ConversionException;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;

public class SQLJoinConverter implements BidirectionalConverter<String, SQLJoin> {

	/**
	 * This workspace is used to look up items for a join.
	 */
	private final WabitObject root;

	public SQLJoinConverter(WabitObject root) {
		this.root = root;
	}

	public SQLJoin convertToComplexType(String convertFrom)
			throws ConversionException {
		String[] pieces = SessionPersisterUtils.splitByDelimiter(convertFrom, 5);
		
		String queryID = pieces[0];
		String leftTableID = pieces[1];
		String leftItemID = pieces[2];
		String rightTableID = pieces[3];
		String rightItemID = pieces[4];
		QueryCache query = WabitUtils.findByUuid(root, queryID, QueryCache.class);
		
		List<Container> fromTables = query.getFromTableList();
		Container leftContainer = null;
		Container rightContainer = null;
		for (Container table : fromTables) {
			if (table.getUUID().equals(leftTableID)) {
				leftContainer = table;
			} else if (table.getUUID().equals(rightTableID)) {
				rightContainer = table;
			}
		}
		Item leftItem = null;
		for (Item item : leftContainer.getItems()) {
			if (item.getUUID().equals(leftItemID)) {
				leftItem = item;
				break;
			}
		}
		
		Item rightItem = null;
		for (Item item : rightContainer.getItems()) {
			if (item.getUUID().equals(rightItemID)) {
				rightItem = item;
				break;
			}
		}
		
		SQLJoin join = new SQLJoin(leftItem, rightItem);
		query.addJoin(join);
		return join;
	}

	public String convertToSimpleType(SQLJoin convertFrom,
			Object... additionalInfo) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(convertFrom.getParent().getUUID());
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getLeftColumn().getParent().getUUID());
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getLeftColumn().getUUID());
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getRightColumn().getParent().getUUID());
		buffer.append(DELIMITER);
		buffer.append(convertFrom.getRightColumn().getUUID());
		
		return buffer.toString();
	}

}
