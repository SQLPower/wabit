/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Wabit.
 *
 * SQL Power Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

/**
 * 
 */
package ca.sqlpower.wabit;

import java.awt.Font;

import ca.sqlpower.dao.session.SPFontLoader;

/**
 * This class is able to load fonts from a Wabit server. It has a shared
 * static cache among instances so it only fetches fonts only once.
 */
public class LocalFontLoader implements SPFontLoader {
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.dao.session.SPFontLoader#loadFontFromSpecs(java.lang.String)
	 */
	public Font loadFontFromSpecs(String fontSpecs) {
		return Font.decode(fontSpecs);
	}
	
	/* (non-Javadoc)
	 * @see ca.sqlpower.wabit.dao.session.SPFontLoader#loadFontFromName(java.lang.String)
	 */
	public Font loadFontFromName(String fontName) {
		return Font.decode(fontName);
	}
}