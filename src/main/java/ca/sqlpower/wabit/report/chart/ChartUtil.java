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

package ca.sqlpower.wabit.report.chart;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.chart.ChartColumn.DataType;

/**
 * A bucket of static goodness for dealing with the chart API.
 */
public class ChartUtil {

    /**
     * This separator is used to separate category names when more then one
     * column is selected as the category in a bar chart.
     */
    private static final String CATEGORY_SEPARATOR = ", ";

    /**
     * This class should not be instantiated. Don't call this.
     */
    private ChartUtil() { /* don't */ }
    
    /**
     * Simple helper method that concatenates the names of a row of categories.
     * This way all of the category names are consistent.
     */
    static String createCategoryName(List<String> names) {
        StringBuilder sb = new StringBuilder();
        if (names.size() == 0) return "";
        sb.append(names.get(0));
        for (int i = 1; i < names.size(); i++) {
            sb.append(CATEGORY_SEPARATOR + names.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Sets the given chart to its default settings. The particular defaults chosen
     * depend on the chart's current type. All other settings are subject to change.
     * <p>
     * Note: with a little thought, we could move this behaviour into the DatasetTypes
     * enum. That would be better design.
     * 
     * @param chart The chart to set to a useful default configuration.
     */
    public static void setDefaults(@Nonnull Chart chart) {
        if (chart.getType() == null) {
            return;
        } else if (chart.getType().getDatasetType() == DatasetTypes.CATEGORY) {
            chart.setXAxisLabelRotation(-45.0);
            if (chart.getQuery() instanceof OlapQuery) {
                // policy: -last string column (deepest level) is the only category
                //         -all numeric columns are series
                //         -not sure what to do with date columns
                ChartColumn lastStringCol = null;
                for (ChartColumn cc : chart.getColumns()) {
                    if (cc.getDataType() == DataType.TEXT) {
                        cc.setRoleInChart(ColumnRole.NONE);
                        lastStringCol = cc;
                    } else if (cc.getDataType() == DataType.NUMERIC) {
                        cc.setRoleInChart(ColumnRole.SERIES);
                    } else {
                        cc.setRoleInChart(ColumnRole.NONE);
                    }
                }
                if (lastStringCol != null) {
                    lastStringCol.setRoleInChart(ColumnRole.CATEGORY);
                }
            } else {
                // policy: -all string and date columns are category
                //         -all numeric columns are series
                for (ChartColumn cc : chart.getColumns()) {
                    if (cc.getDataType() == DataType.TEXT || cc.getDataType() == DataType.DATE) {
                        cc.setRoleInChart(ColumnRole.CATEGORY);
                    } else if (cc.getDataType() == DataType.NUMERIC) {
                        cc.setRoleInChart(ColumnRole.SERIES);
                    } else {
                        cc.setRoleInChart(ColumnRole.NONE);
                    }
                }
            }
        } else if (chart.getType().getDatasetType() == DatasetTypes.XY) {
            // policy: -first numeric or date column is X axis
            //         -all subsequent numeric columns are Y values plotted against it
            ChartColumn xAxisCol = null;
            Iterator<ChartColumn> ccit = chart.getColumns().iterator();
            while (ccit.hasNext()) {
                ChartColumn cc = ccit.next();
                cc.setRoleInChart(ColumnRole.NONE);
                if (cc.getDataType() == DataType.NUMERIC || cc.getDataType() == DataType.DATE) {
                    xAxisCol = cc;
                    break;
                }
            }
            while (ccit.hasNext()) {
                ChartColumn cc = ccit.next();
                if (cc.getDataType() == DataType.NUMERIC) {
                    cc.setRoleInChart(ColumnRole.SERIES);
                    cc.setXAxisIdentifier(xAxisCol);
                } else {
                    cc.setRoleInChart(ColumnRole.NONE);
                }
            }
        } else {
            throw new UnsupportedOperationException("Unknown chart type " + chart.getType());
        }
    }
}
