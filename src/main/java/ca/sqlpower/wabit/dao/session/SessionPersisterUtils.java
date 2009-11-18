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

import java.awt.Image;

import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.object.SPObject;

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
		String[] pieces = toSplit.split(BidirectionalConverter.DELIMITER);

		if (pieces.length > numPieces) {
			throw new IllegalArgumentException("Cannot convert string \""
					+ toSplit + "\" with an invalid number of properties.");
		} else if (pieces.length < numPieces) {
			//split will strip off empty space that comes after a delimiter instead of
			//appending an empty string to the array so we have to do that ourselves.
			String[] allPieces = new String[numPieces];
			
			int i = 0;
			for (String piece : pieces) {
				allPieces[i] = piece;
				i++;
			}
			for (; i < numPieces; i++) {
				allPieces[i] = "";
			}
			return allPieces;
		}
		return pieces;
	}
	
    /**
     * Gets the correct data type based on the given class for the wabit persister.
     */
    public static DataType getDataType(Class<? extends Object> classForDataType) {
    	if (Integer.class.isAssignableFrom(classForDataType)) {
    		return DataType.INTEGER;
    	} else if (Boolean.class.isAssignableFrom(classForDataType)) {
    		return DataType.BOOLEAN;
    	} else if (Double.class.isAssignableFrom(classForDataType)) {
    		return DataType.DOUBLE;
    	} else if (String.class.isAssignableFrom(classForDataType)) {
    		return DataType.STRING;
    	} else if (Image.class.isAssignableFrom(classForDataType)) {
    		return DataType.PNG_IMG;
    	} else if (SPObject.class.isAssignableFrom(classForDataType)) {
    		return DataType.REFERENCE;
    	} else if (Void.class.isAssignableFrom(classForDataType)) {
    		return DataType.NULL;
    	} else {
    		return DataType.STRING;
    	}
    }
	
	private SessionPersisterUtils() {
		//cannot make an instance of this class.
	}

}
