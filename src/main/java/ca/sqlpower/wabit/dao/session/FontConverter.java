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

import java.awt.Font;

import org.apache.commons.beanutils.ConversionException;

public class FontConverter implements BidirectionalConverter<String, Font> {
	
	/**
	 * This enum comes from the documentation on {@link Font#decode(String)}
	 * to convert from the font style to a string defined in the documentation
	 * and back.
	 */
	private enum FontStyle {
		BOLD("BOLD", Font.BOLD),
		ITALIC("ITALIC", Font.ITALIC),
		PLAIN("PLAIN", Font.PLAIN),
		BOLDITALIC("BOLDITALIC", Font.BOLD + Font.ITALIC);
		
		private final String encodedName;
		private final int value;

		private FontStyle(String encodedName, int value) {
			this.encodedName = encodedName;
			this.value = value;
			
		}

		public String getEncodedName() {
			return encodedName;
		}

		public int getValue() {
			return value;
		}
		
		public static FontStyle getStyleByValue(int value) {
			for (FontStyle style : values()) {
				if (style.getValue() == value) {
					return style;
				}
			}
			throw new IllegalArgumentException("Unknown format for font integer value " + value);
		}
		
	}

	public Font convertToComplexType(String convertFrom)
			throws ConversionException {
		return Font.decode(convertFrom);
	}

	public String convertToSimpleType(Font convertFrom, Object ... additionalInfo) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(convertFrom.getFontName());
		buffer.append("-");
		buffer.append(FontStyle.getStyleByValue(convertFrom.getStyle()).getEncodedName());
		buffer.append("-");
		buffer.append(convertFrom.getSize());
		
		return buffer.toString();
	}

}
