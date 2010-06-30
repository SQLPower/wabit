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

import ca.sqlpower.dao.session.BidirectionalConverter;
import ca.sqlpower.dao.session.SPFontLoader;
import ca.sqlpower.wabit.FontStyle;

public class FontConverter implements BidirectionalConverter<String, Font> {

	private final SPFontLoader loader;

	public FontConverter(SPFontLoader loader) {
		this.loader = loader;
	}

	public Font convertToComplexType(String convertFrom)
			throws ConversionException {
		return loader.loadFontFromSpecs(convertFrom);
	}

	public String convertToSimpleType(Font convertFrom, Object ... additionalInfo) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(convertFrom.getName());
		buffer.append("-");
		buffer.append(FontStyle.getStyleByValue(convertFrom.getStyle()).getEncodedName());
		buffer.append("-");
		buffer.append(convertFrom.getSize());
		return buffer.toString();
	}
}
