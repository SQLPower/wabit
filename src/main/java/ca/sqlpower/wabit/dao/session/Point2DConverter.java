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

import java.awt.geom.Point2D;

import org.apache.commons.beanutils.ConversionException;

public class Point2DConverter implements
		BidirectionalConverter<String, Point2D> {

	public Point2D convertToComplexType(String convertFrom)
			throws ConversionException {
		String[] pieces = SessionPersisterUtils.splitByDelimiter(convertFrom, 2);

		double x = Double.valueOf(pieces[0]);
		double y = Double.valueOf(pieces[1]);

		return (new Point2D.Double(x, y));
	}

	public String convertToSimpleType(Point2D convertFrom, Object ... additionalInfo) {
		StringBuilder result = new StringBuilder();

		result.append(convertFrom.getX());
		result.append(DELIMITER);
		result.append(convertFrom.getY());

		return result.toString();
	}

}
