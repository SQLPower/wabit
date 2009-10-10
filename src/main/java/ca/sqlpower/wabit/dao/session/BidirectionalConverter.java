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

/**
 * This interface allows conversions of one type of object to another and back
 * again. The types of object that this can convert are defined by S for the
 * simple type of object and C for the complex type of object.
 */
public interface BidirectionalConverter<S, C> {

	/**
	 * This method converts an object of type C to an object of type S.
	 * 
	 * @param convertFrom
	 *            The object to convert.
	 * @param additionalInfo
	 *            Some types of object require some kind of additional
	 *            information to be properly persisted which is passed through
	 *            here. One specific place is when converting Cubes to get their
	 *            data source. There is probably a better way of doing this.
	 * @return The simple type representation of the object.
	 */
	public S convertToSimpleType(C convertFrom, Object ... additionalInfo);

	/**
	 * Converts an object of type S to an object of type C. The given object we
	 * are converting from must contain a valid representation of the object or
	 * else an {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param convertFrom
	 *            The object to convert.
	 * @return The complex type representation of the object.
	 * @throws ConversionException
	 *             Thrown if the object could not be converted to a complex type
	 *             even though the simple type is a correct representation.
	 */
	public C convertToComplexType(S convertFrom) throws ConversionException;

}
