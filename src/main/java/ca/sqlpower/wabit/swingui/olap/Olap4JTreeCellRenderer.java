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

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedSet;
import org.olap4j.metadata.Property;
import org.olap4j.metadata.Schema;

public class Olap4JTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Logger logger = Logger.getLogger(Olap4JTreeCellRenderer.class);

    private static final ImageIcon SCHEMA_ICON = makeIcon("schema");
    private static final ImageIcon CUBE_ICON = makeIcon("cube");
    private static final ImageIcon DIMENSION_ICON = makeIcon("dimension");
    private static final ImageIcon LEVEL_ICON = makeIcon("level");
    private static final ImageIcon HIERARCHY_ICON = makeIcon("hierarchy");
    private static final ImageIcon NAMEDSET_ICON = makeIcon("namedSet");
    private static final ImageIcon MEASURE_ICON = makeIcon("measure");
    
    private static final ImageIcon makeIcon(String resourceName) {
        URL iconUrl = Olap4JTreeCellRenderer.class.getResource(
                "/ca/sqlpower/swingui/olap/" + resourceName + ".png");
        if (iconUrl == null) {
            throw new RuntimeException("Missing icon for " + resourceName);
        } else {
            return new ImageIcon(iconUrl);
        }
    }
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        try {
            if (value instanceof OlapConnection) {
                setText(((OlapConnection) value).getMetaData().getURL()); // XXX not ideal
            } else if (value instanceof Catalog) {
                setText(((Catalog) value).getName());
            } else if (value instanceof Schema) {
                setText(((Schema) value).getName());
                setIcon(SCHEMA_ICON);
            } else if (value instanceof Cube) {
                setText(((Cube) value).getName());
                setIcon(CUBE_ICON);
            } else if (value instanceof Dimension) {
                setText(((Dimension) value).getName());
                setIcon(DIMENSION_ICON);
            } else if (value instanceof Measure) {
                setText(((Measure) value).getName());
                setIcon(MEASURE_ICON);
            } else if (value instanceof Hierarchy) {
                setText(((Hierarchy) value).getName());
                setIcon(HIERARCHY_ICON);
            } else if (value instanceof Level) {
                setText(((Level) value).getName());
                setIcon(LEVEL_ICON);
            } else if (value instanceof Property) {
                setText(((Property) value).getName());
            } else if (value instanceof Member) {
                setText(((Member) value).getName());
            } else if (value instanceof NamedSet) {
                setText(((NamedSet) value).getName());
                setIcon(NAMEDSET_ICON);
            } else {
                logger.warn("Leaving default label for unknown tree node " + value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        return this;
    }
}
