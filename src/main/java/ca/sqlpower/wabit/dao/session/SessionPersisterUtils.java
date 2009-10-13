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

/**
 * Utilities that are specific to the session persisters.
 */
public class SessionPersisterUtils {

	/**
	 * Splits a string by the converter delimiter and checks that the correct
	 * number of pieces are returned or it throws an
	 * {@link IllegalArgumentException}. This is a simple place to do general
	 * error checking when first converting a string into an object.
	 * 
	 * @param toSplit
	 *            The string to split by the delimiter.
	 * @param numPieces
	 *            The number of pieces the string must be split into.
	 * @return The pieces the string is split into.
	 */
	static String[] splitByDelimiter(String toSplit, int numPieces) {
		String[] pointPieces = toSplit.split(BidirectionalConverter.DELIMITER);

		if (pointPieces.length != 2) {
			throw new IllegalArgumentException("Cannot convert string \""
					+ toSplit + "\" with an invalid number of properties.");
		}
		return pointPieces;
	}
	
	private SessionPersisterUtils() {
		//cannot make an instance of this class.
	}

}
