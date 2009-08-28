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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ca.sqlpower.sql.SQL;

/**
 * Package private helper class for building datasets from Chart instances.
 * 
 * @see Chart#createDataset()
 */
class DatasetUtil {

    private static final Logger logger = Logger.getLogger(DatasetUtil.class);
    
    /**
     * This is a helper method for creating a CategoryDataset for relational
     * queries. This method takes in a {@link ResultSet} as well as information
     * about what columns to set as categories and series to make a dataset.
     * This is done differently from the OLAP version as they each get
     * information in different ways.
     * <p>
     * This is package private for testing.
     */
    static CategoryDataset createCategoryDataset(
            List<ChartColumn> columnNamesInOrder,
            ResultSet resultSet, List<ChartColumn> categoryColumnIdentifiers) {
        
        //Create a list of unique category row names to label each bar with. Category rows
        //with the same name are currently summed.
        List<String> uniqueNamesInCategory = new ArrayList<String>();
        final List<String> categoryColumnNames = new ArrayList<String>();
        for (ChartColumn chartCol : categoryColumnIdentifiers) {
            categoryColumnNames.add(chartCol.getColumnName());
        }
        List<Integer> columnIndicies = new ArrayList<Integer>();
        try {
            for (String categoryColumnName : categoryColumnNames) {
                columnIndicies.add(resultSet.findColumn(categoryColumnName));
            }
            resultSet.beforeFirst();
            while (resultSet.next()) {
                List<String> categoryRowNames = new ArrayList<String>();
                for (Integer columnIndex : columnIndicies) {
                    categoryRowNames.add(resultSet.getString(columnIndex));
                }
                String categoryRowName = ChartUtil.createCategoryName(categoryRowNames);
                if (!uniqueNamesInCategory.contains(categoryRowName)) {
                    uniqueNamesInCategory.add(categoryRowName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        List<String> seriesColumnNames = new ArrayList<String>();
        for (ChartColumn chartCol : columnNamesInOrder) {
            if (chartCol.getRoleInChart().equals(ColumnRole.SERIES)) {
                seriesColumnNames.add(chartCol.getColumnName());
            }
        }
        
        double[][] data = new double[seriesColumnNames.size()][uniqueNamesInCategory.size()];
        try {
            resultSet.beforeFirst();
            int j = 0;
            while (resultSet.next()) {
                List<String> categoryRowNames = new ArrayList<String>();
                for (Integer columnIndex : columnIndicies) {
                    categoryRowNames.add(resultSet.getString(columnIndex));
                }
                String categoryRowName = ChartUtil.createCategoryName(categoryRowNames);
                for (String colName : seriesColumnNames) {
                    if (logger.isDebugEnabled() && (seriesColumnNames.indexOf(colName) == -1 || uniqueNamesInCategory.indexOf(categoryRowName) == -1)) {
                        logger.debug("Index of series " + colName + " is " + seriesColumnNames.indexOf(colName) + ", index of category " + categoryColumnIdentifiers + " is " + uniqueNamesInCategory.indexOf(categoryRowName));
                    }
                    //XXX Getting numeric values as double causes problems for BigDecimal and BigInteger.
                    //XXX Add a property to decide if the values should be summed or aggregated in a 
                    // different way like max, min, avg, etc
                    data[seriesColumnNames.indexOf(colName)][uniqueNamesInCategory.indexOf(categoryRowName)] += 
                        resultSet.getDouble(colName);
                }
                j++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(seriesColumnNames.toArray(new String[]{}), uniqueNamesInCategory.toArray(new String[]{}), data);
        
        return dataset;
    }

    /**
     * Helper method for creating line and scatter charts in the
     * createJFreeChart method. This is for relational queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    static XYDataset createSeriesCollection(
            List<ChartColumn> columnNamesInOrder, ResultSet resultSet) {
        boolean allNumeric = true;
        boolean allDate = true;
        try {
            for (ChartColumn chartCol : columnNamesInOrder) {
                final ChartColumn xAxisIdentifier = chartCol.getXAxisIdentifier();
                if (!chartCol.getRoleInChart().equals(ColumnRole.SERIES)
                        || xAxisIdentifier == null) continue;
                int columnType = resultSet.getMetaData().getColumnType(
                        resultSet.findColumn(
                                xAxisIdentifier.getColumnName()));
                if (columnType != Types.DATE && columnType != Types.TIMESTAMP) {
                    allDate = false;
                } 
                if (!SQL.isNumeric(columnType)) {
                    allNumeric = false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (allNumeric) {
            XYSeriesCollection xyCollection = new XYSeriesCollection();
            for (ChartColumn chartCol : columnNamesInOrder) {
                ChartColumn xAxisColIdentifier = chartCol.getXAxisIdentifier();
                if (!chartCol.getRoleInChart().equals(ColumnRole.SERIES)
                        || xAxisColIdentifier == null) continue;
                XYSeries newSeries = new XYSeries(chartCol.getColumnName());
                try {
                    resultSet.beforeFirst();
                    while (resultSet.next()) {
                        //XXX: need to switch from double to bigDecimal if it is needed.
                        newSeries.add(resultSet.getDouble(xAxisColIdentifier.getColumnName()), resultSet.getDouble(chartCol.getColumnName()));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                xyCollection.addSeries(newSeries);
            }
            return xyCollection;
        } else if (allDate) {
            TimePeriodValuesCollection timeCollection = new TimePeriodValuesCollection();
            for (ChartColumn chartCol : columnNamesInOrder) {
                ChartColumn xAxisColIdentifier = chartCol.getXAxisIdentifier();
                if (!chartCol.getRoleInChart().equals(ColumnRole.SERIES)
                        || xAxisColIdentifier == null) continue;
                TimePeriodValues newSeries = new TimePeriodValues(chartCol.getColumnName());
                try {
                    resultSet.beforeFirst();
                    while (resultSet.next()) {
                        int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(xAxisColIdentifier.getColumnName()));
                        if (columnType == Types.DATE) {
                            newSeries.add(new FixedMillisecond(resultSet.getDate(xAxisColIdentifier.getColumnName())), resultSet.getDouble(((ChartColumn) chartCol).getColumnName()));
                        } else if (columnType == Types.TIMESTAMP){
                            newSeries.add(new FixedMillisecond(resultSet.getTimestamp(xAxisColIdentifier.getColumnName())), resultSet.getDouble(((ChartColumn) chartCol).getColumnName()));
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                timeCollection.addSeries(newSeries);
            }
            return timeCollection;
        } else {
            return null;
        }
    }

}
