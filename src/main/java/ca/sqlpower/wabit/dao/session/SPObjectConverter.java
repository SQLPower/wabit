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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.WabitObject;


/**
 * Converts {@link WabitObject}s to a unique string per wabit object and
 * converts these strings into {@link WabitObject}s that are contained in
 * the root.
 */
public class SPObjectConverter implements BidirectionalConverter<String, SPObject> {
	
	/**
	 * This is the root object of the tree of {@link WabitObject}s that
	 * will be searched for the given object by unique id.
	 */
	private final SPObject root;

	public SPObjectConverter(SPObject root) {
		this.root = root;
	}

	public SPObject convertToComplexType(String convertFrom)
			throws ConversionException {
		return SQLPowerUtils.findByUuid(root, convertFrom, SPObject.class); 
	}

	public String convertToSimpleType(SPObject convertFrom, Object ... additionalValues) {
		return convertFrom.getUUID();
	}

}
