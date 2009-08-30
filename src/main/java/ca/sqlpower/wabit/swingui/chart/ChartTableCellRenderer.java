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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ca.sqlpower.wabit.olap.OlapResultSet;
import ca.sqlpower.wabit.olap.RepeatedMember;
import ca.sqlpower.wabit.report.chart.Chart;

/**
 * Cell renderer for the body of the chart result sets. This renderer knows how
 * to handle the special wrapper types found in the {@link OlapResultSet}.
 */
public class ChartTableCellRenderer extends DefaultTableCellRenderer {
    
    private final Color repeatedMemberColour = new Color(0xcccccc);
    private final Color defaultForeground;
    private final Color defaultBackground;
    private final Color filteredOutRowBackground = new Color(0xeeeeee);

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance();
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    
    /**
     * The chart we're rendering data for.
     */
    private final Chart chart;
    
    /**
     * Creates a new cell renderer that's aware of the row filter installed on
     * the given chart.
     * 
     * @param chart The chart whose data set this renderer is rendering cells of.
     */
    public ChartTableCellRenderer(Chart chart) {
        this.chart = chart;
        defaultForeground = getForeground();
        defaultBackground = getBackground();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value instanceof Number) {
            setText(numberFormat.format(value));
            setHorizontalAlignment(RIGHT);
        } else if (value instanceof Date) {
            setText(dateFormat.format(value));
            setHorizontalAlignment(LEFT);
        } else {
            setHorizontalAlignment(LEFT);
        }
        
        if (!isSelected) {
            if (value instanceof RepeatedMember) {
                setForeground(repeatedMemberColour);
            } else {
                setForeground(defaultForeground);
            }
            
            /*
             * For posterity: it feels expensive to evaluate the row filter once for
             * every column, but it's really not an issue. Keep in mind the JTable is
             * only going to ask us to render the cells that are currently visible on
             * screen.
             */
            try {
                if (chart.getUnfilteredResultSet().wouldPass(row + 1, chart.getResultSetFilter())) {
                    setBackground(defaultBackground);
                } else {
                    setBackground(filteredOutRowBackground);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        return this;
    }
    
}
