/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui.tree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;

public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

    public static final Icon PAGE_ICON = new ImageIcon(ProjectTreeCellRenderer.class.getResource("/icons/page_white.png"));
    public static final Icon LAYOUT_ICON = new ImageIcon(ProjectTreeCellRenderer.class.getResource("/icons/layout.png"));
    public static final Icon BOX_ICON = new ImageIcon(ProjectTreeCellRenderer.class.getResource("/icons/shape_square.png"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        ProjectTreeCellRenderer r = (ProjectTreeCellRenderer) super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof WabitObject) {
            WabitObject wo = (WabitObject) value;
            
            if (wo instanceof WabitDataSource) {
                r.setIcon(DBTreeCellRenderer.DB_ICON);
            } else if (wo instanceof Page) {
                r.setIcon(PAGE_ICON);
            } else if (wo instanceof Layout) {
                r.setIcon(LAYOUT_ICON);
            } else if (wo instanceof ContentBox) {
                r.setIcon(BOX_ICON);
            }

            r.setText(wo.getName());
        }
        return r;
    }
    
}
