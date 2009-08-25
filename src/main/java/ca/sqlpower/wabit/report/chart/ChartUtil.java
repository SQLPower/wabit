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

import java.util.List;

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
}
