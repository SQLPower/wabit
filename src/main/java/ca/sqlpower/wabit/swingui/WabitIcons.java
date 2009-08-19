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

package ca.sqlpower.wabit.swingui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A utility class acting as a single place to store commonly-used icons in
 * Wabit
 */
public class WabitIcons {
	
	public static final Icon REPORT_ICON_16 = makeIcon("dashboard-16");
	
    private static final ImageIcon makeIcon(String resourceName) {
        URL iconUrl = WabitIcons.class.getClassLoader().getResource(
                "icons/" + resourceName + ".png");
        if (iconUrl == null) {
            throw new RuntimeException("Missing icon for " + resourceName);
        } else {
            return new ImageIcon(iconUrl);
        }
    }
}
