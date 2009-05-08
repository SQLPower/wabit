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

package ca.sqlpower.wabit.swingui.olap;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * A collection of OLAP-related icons.
 */
public class OlapIcons {

    public static final ImageIcon SCHEMA_ICON = makeIcon("schema");
    public static final ImageIcon CUBE_ICON = makeIcon("cube");
    public static final ImageIcon DIMENSION_ICON = makeIcon("dimension");
    public static final ImageIcon LEVEL_ICON = makeIcon("level");
    public static final ImageIcon HIERARCHY_ICON = makeIcon("hierarchy");
    public static final ImageIcon NAMEDSET_ICON = makeIcon("namedSet");
    public static final ImageIcon MEASURE_ICON = makeIcon("measure");
    
    private static final ImageIcon makeIcon(String resourceName) {
        URL iconUrl = Olap4JTreeCellRenderer.class.getResource(
                "/ca/sqlpower/swingui/olap/" + resourceName + ".png");
        if (iconUrl == null) {
            throw new RuntimeException("Missing icon for " + resourceName);
        } else {
            return new ImageIcon(iconUrl);
        }
    }
}
