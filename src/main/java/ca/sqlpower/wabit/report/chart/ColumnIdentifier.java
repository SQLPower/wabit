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

import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ChartRenderer.DataTypeSeries;

/**
 * This object identifies a single column in a table being used to define a chart.
 * Each column should be able to be uniquely identified.
 */
public interface ColumnIdentifier extends WabitObject {

    /**
     * Returns the object that uniquely identifies this column.
     */
    Object getUniqueIdentifier();

    /**
     * Returns the name of the column for use in defining series names.
     */
    String getName();

    /**
     * Returns the data type this column is being used as. If the column is
     * being used in a chart that uses a category dataset this value can be set
     * to {@link DataTypeSeries#CATEGORY}.
     */
    DataTypeSeries getDataType();

    /**
     * Defines how this column is used in the query. If the column is being used
     * in a chart that uses a category dataset the given value can be
     * {@link DataTypeSeries#CATEGORY}.
     */
    void setDataType(DataTypeSeries dataType);

    /**
     * Returns the column that is used as the x values of this column
     * identifier. This only needs to be set for XY datasets and if the data
     * type is {@link DataTypeSeries#SERIES}.
     */
    ColumnIdentifier getXAxisIdentifier();
    
    /**
     * Defines the column to use as X axis values when this column is used
     * as a series in an XY dataset.
     */
    void setXAxisIdentifier(ColumnIdentifier xAxisIdentifier);
}
