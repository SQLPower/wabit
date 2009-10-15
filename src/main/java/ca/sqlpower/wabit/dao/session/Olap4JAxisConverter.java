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
import org.olap4j.Axis;
import org.olap4j.Axis.Standard;

/**
 * Convert between an Olap4j axis and its name. While the {@link Axis} is described
 * as an enum in documentation it is actually an interface.
 */
public class Olap4JAxisConverter implements BidirectionalConverter<String, Axis> {

	public Axis convertToComplexType(String convertFrom)
			throws ConversionException {
		if (convertFrom == null) return null;
		
		Standard axis = Axis.Standard.valueOf(convertFrom);
		
		return axis;
	}

	public String convertToSimpleType(Axis convertFrom,
			Object... additionalInfo) {
		
		//Some Axis are null as they are depreciated.
		if (convertFrom == null) return null;
		
		return convertFrom.name();
	}

}
