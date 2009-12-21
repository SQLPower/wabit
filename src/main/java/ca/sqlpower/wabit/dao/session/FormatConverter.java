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

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import org.apache.commons.beanutils.ConversionException;

import ca.sqlpower.dao.session.BidirectionalConverter;

public class FormatConverter implements BidirectionalConverter<String, Format> {

	public Format convertToComplexType(String convertFrom)
			throws ConversionException {
		
		//The patterns can contain the delimiter character.
		int firstSeparator = convertFrom.indexOf(DELIMITER);
		
		if (firstSeparator == -1) {
			throw new IllegalArgumentException("Cannot find the class type for the string " + 
					convertFrom);
		}
		
		String className = convertFrom.substring(0, firstSeparator);
		String pattern = convertFrom.substring(firstSeparator + 1);
		
		if (className.equals(DecimalFormat.class.getSimpleName())) {
			return new DecimalFormat(pattern);
		} else if (className.equals(SimpleDateFormat.class.getSimpleName())) {
			return new SimpleDateFormat(pattern);
		} else {
			throw new IllegalStateException("Unknown class " + className + 
					" to create a format on based on the pattern " + pattern);
		}
	}

	public String convertToSimpleType(Format convertFrom,
			Object... additionalInfo) {
		if (convertFrom instanceof DecimalFormat) {
			DecimalFormat decimal = (DecimalFormat) convertFrom;
			return DecimalFormat.class.getSimpleName() + DELIMITER + decimal.toPattern();
		} else if (convertFrom instanceof SimpleDateFormat) {
			SimpleDateFormat date = (SimpleDateFormat) convertFrom;
			return SimpleDateFormat.class.getSimpleName() + DELIMITER + date.toPattern();
		} else {
			throw new IllegalStateException("Unknown format to convert from " + convertFrom.getClass());
		}
	}

}
