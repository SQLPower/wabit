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

package ca.sqlpower.wabit.swingui.chart;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.wabit.olap.OlapResultSet;
import ca.sqlpower.wabit.olap.RepeatedMember;

/**
 * Cell renderer for the body of the chart result sets. This renderer knows how
 * to handle the special wrapper types found in the {@link OlapResultSet}.
 */
public class ChartTableCellRenderer extends DefaultTableCellRenderer {
    
    private final Color repeatedMemberColour = new Color(0xdddddd);
    private final Color defaultForeground;
    
    public ChartTableCellRenderer() {
        defaultForeground = getForeground();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value instanceof RepeatedMember) {
            setForeground(repeatedMemberColour);
        } else {
            setForeground(defaultForeground);
        }
        
        return this;
    }
    
}
