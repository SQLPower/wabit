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

import javax.swing.JLabel;
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

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        configureJLabel(this, value);
        
        return this;
    }

    /**
     * Configures the given JLabel (which all of the Default*CellRenderer
     * classes extend) with the correct text and icon for the given OLAP4J
     * metadata object.
     * 
     * @param configureMe
     *            The JLabel (probably a tree/list/table cell renderer, but can
     *            be a plain JLabel) to configure.
     * @param value
     *            The OLAP4J object to represent. Should be one of the types
     *            mentioned in
     *            {@link Olap4jTreeModel#Olap4jTreeModel(java.util.List)}, but
     *            other types cause this method to fail gracefully (it simply
     *            doesn't modify the label if the value type is unsupported).
     */
    static void configureJLabel(JLabel configureMe, Object value) {
        try {
            if (value instanceof OlapConnection) {
                configureMe.setText(((OlapConnection) value).getMetaData().getURL()); // XXX not ideal
            } else if (value instanceof Catalog) {
                configureMe.setText(((Catalog) value).getName());
            } else if (value instanceof Schema) {
                configureMe.setText(((Schema) value).getName());
                configureMe.setIcon(OlapIcons.SCHEMA_ICON);
            } else if (value instanceof Cube) {
                configureMe.setText(((Cube) value).getName());
                configureMe.setIcon(OlapIcons.CUBE_ICON);
            } else if (value instanceof Dimension) {
                configureMe.setText(((Dimension) value).getName());
                configureMe.setIcon(OlapIcons.DIMENSION_ICON);
            } else if (value instanceof Measure) {
                configureMe.setText(((Measure) value).getName());
                configureMe.setIcon(OlapIcons.MEASURE_ICON);
            } else if (value instanceof Hierarchy) {
                configureMe.setText(((Hierarchy) value).getName());
                configureMe.setIcon(OlapIcons.HIERARCHY_ICON);
            } else if (value instanceof Level) {
                configureMe.setText(((Level) value).getName());
                configureMe.setIcon(OlapIcons.LEVEL_ICON);
            } else if (value instanceof Property) {
                configureMe.setText(((Property) value).getName());
            } else if (value instanceof Member) {
                configureMe.setText(((Member) value).getName());
            } else if (value instanceof NamedSet) {
                configureMe.setText(((NamedSet) value).getName());
                configureMe.setIcon(OlapIcons.NAMEDSET_ICON);
            } else {
                logger.warn("Leaving default label for unknown tree node " + value);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
