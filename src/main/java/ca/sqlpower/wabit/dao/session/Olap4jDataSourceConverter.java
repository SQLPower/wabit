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

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.WabitWorkspace;

public class Olap4jDataSourceConverter implements BidirectionalConverter<String, Olap4jDataSource> {

	private final WabitWorkspace workspace;

	public Olap4jDataSourceConverter(WabitWorkspace workspace) {
		this.workspace = workspace;
	}
	
	public Olap4jDataSource convertToComplexType(String convertFrom)
			throws ConversionException {
		return workspace.getDataSource(convertFrom, Olap4jDataSource.class);
	}

	public String convertToSimpleType(Olap4jDataSource convertFrom,
			Object... additionalInfo) {
		return convertFrom.getName();
	}

}
