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
import java.util.Arrays;
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
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

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
            List<ColumnIdentifier> columnNamesInOrder,
            ResultSet resultSet, List<ColumnIdentifier> categoryColumnIdentifiers) {
        
        //Create a list of unique category row names to label each bar with. Category rows
        //with the same name are currently summed.
        List<String> uniqueNamesInCategory = new ArrayList<String>();
        final List<String> categoryColumnNames = new ArrayList<String>();
        for (ColumnIdentifier identifier : categoryColumnIdentifiers) {
            categoryColumnNames.add(((ColumnNameColumnIdentifier) identifier).getColumnName());
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
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (identifier.getRoleInChart().equals(ColumnRole.SERIES)) {
                seriesColumnNames.add(((ColumnNameColumnIdentifier) identifier).getColumnName());
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
     * This is a helper method for creating a CategoryDataset for OLAP
     * queries. This method takes in a {@link CellSet} as well as information
     * about what columns to set as categories and series to make a dataset.
     */
    static CategoryDataset createOlapCategoryDataset(
            List<ColumnIdentifier> columnNamesInOrder,
            CellSet cellSet, List<ColumnIdentifier> categoryColumnIdentifiers) {
        
        if (categoryColumnIdentifiers.isEmpty()) {
            throw new IllegalStateException("There are no categories defined when trying to create a chart.");
        }
        
        List<ComparableCategoryRow> uniqueCategoryRowNames = new ArrayList<ComparableCategoryRow>();
        CellSetAxis columnsAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
        CellSetAxis rowsAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
        if (logger.isDebugEnabled()) {
            logger.debug("column axis contains positions: ");
            for (Position pos : columnsAxis.getPositions()) {
                logger.debug("Position " + columnsAxis.getPositions().indexOf(pos));
                for (Member mem : pos.getMembers()) {
                    logger.debug("Member: " + mem.getName());
                }
            }
        }

        for (int i = 0; i < rowsAxis.getPositions().size(); i++) {
            ComparableCategoryRow categoryRow = new ComparableCategoryRow();
            for (ColumnIdentifier categoryColumnIdentifier : categoryColumnIdentifiers) {
                if (categoryColumnIdentifier instanceof PositionColumnIdentifier) {
                    PositionColumnIdentifier positionColumnIdentifier = (PositionColumnIdentifier) categoryColumnIdentifier;
                    categoryRow.add(cellSet.getCell(positionColumnIdentifier.getPosition(cellSet), rowsAxis.getPositions().get(i)).getFormattedValue());
                } else if (categoryColumnIdentifier instanceof RowAxisColumnIdentifier) {
                    categoryRow.add(rowsAxis.getPositions().get(i));
                } else {
                    throw new IllegalStateException("Creating a dataset on an OLAP cube. A column is used as a category but has neither a position or hierarchy.");
                }
            }
            uniqueCategoryRowNames.add(categoryRow);
        }
        
        List<Integer> seriesPositions = new ArrayList<Integer>();
        List<String> seriesNames = new ArrayList<String>();
        for (int colPosition = 0; colPosition < columnNamesInOrder.size(); colPosition++) {
            ColumnIdentifier identifier = columnNamesInOrder.get(colPosition);
            if (!(identifier instanceof PositionColumnIdentifier)) continue; //Only positions can be used as series, not hierarchies, as they are numeric.
            if (!identifier.getRoleInChart().equals(ColumnRole.SERIES)) continue;
            
            seriesPositions.add(((PositionColumnIdentifier) identifier).getPosition(cellSet).getOrdinal());
            seriesNames.add(identifier.getName());
        }
        
        double[][] data = new double[seriesPositions.size()][uniqueCategoryRowNames.size()];
        try {
            for (int row = 0; row < rowsAxis.getPositions().size(); row++) {
                for (Integer colPosition : seriesPositions) {
                    logger.debug("At row " + row + " of " + rowsAxis.getPositions().size() + " and column " + colPosition);
                    final Cell cell = cellSet.getCell(Arrays.asList(new Integer[]{colPosition, row}));
                    double value;
                    if (cell.getValue() != null) {
                        value = cell.getDoubleValue();
                    } else {
                        value = 0;
                    }
                    data[seriesPositions.indexOf(colPosition)][row] += value;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.debug("Series : " + seriesNames + ", Categories " + uniqueCategoryRowNames + ", data: " + Arrays.toString(data));
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(seriesNames.toArray(new String[]{}), uniqueCategoryRowNames.toArray(new ComparableCategoryRow[]{}), data);
        
        return dataset;
    }

    /**
     * Helper method for creating line and scatter charts in the
     * createJFreeChart method. This is for olap queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    static XYDataset createOlapSeriesCollection(
            List<ColumnIdentifier> columnNamesInOrder, CellSet cellSet) {
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (!(identifier instanceof PositionColumnIdentifier)) continue;
            PositionColumnIdentifier seriesColIdentifier 
                = ((PositionColumnIdentifier) identifier);
            PositionColumnIdentifier xAxisColIdentifier 
                = ((PositionColumnIdentifier) identifier.getXAxisIdentifier());
            if (!identifier.getRoleInChart().equals(ColumnRole.SERIES)
                    || xAxisColIdentifier == null) continue;
            List<String> memberNames = new ArrayList<String>();
            for (Member member : seriesColIdentifier.getPosition(cellSet).getMembers()) {
                memberNames.add(member.getName());
            }
            XYSeries newSeries = new XYSeries(ChartUtil.createCategoryName(memberNames));
            CellSetAxis rowAxis = cellSet.getAxes().get(Axis.ROWS.axisOrdinal());
            try {
                for (int rowNumber = 0; rowNumber < rowAxis.getPositionCount(); rowNumber++) {
                    Position rowPosition = rowAxis.getPositions().get(rowNumber);
                    final Cell xCell = cellSet.getCell(xAxisColIdentifier.getPosition(cellSet), rowPosition);
                    double xValue;
                    if (xCell.getValue() != null) {
                        xValue = xCell.getDoubleValue();
                    } else {
                        xValue = 0;
                    }
                    final Cell yCell = cellSet.getCell(seriesColIdentifier.getPosition(cellSet), rowPosition);
                    double yValue;
                    if (yCell.getValue() != null) {
                        yValue = yCell.getDoubleValue();
                    } else {
                        yValue = 0;
                    }
                    newSeries.add(xValue, yValue);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            xyCollection.addSeries(newSeries);
        }
        return xyCollection;
    }

    /**
     * Helper method for creating line and scatter charts in the
     * createJFreeChart method. This is for relational queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    static XYDataset createSeriesCollection(
            List<ColumnIdentifier> columnNamesInOrder, ResultSet resultSet) {
        boolean allNumeric = true;
        boolean allDate = true;
        try {
            for (ColumnIdentifier identifier : columnNamesInOrder) {
                final ColumnNameColumnIdentifier xAxisIdentifier = 
                    (ColumnNameColumnIdentifier) identifier.getXAxisIdentifier();
                if (!identifier.getRoleInChart().equals(ColumnRole.SERIES)
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
            for (ColumnIdentifier identifier : columnNamesInOrder) {
                ColumnNameColumnIdentifier seriesColIdentifier = 
                    ((ColumnNameColumnIdentifier) identifier);
                ColumnNameColumnIdentifier xAxisColIdentifier =
                    ((ColumnNameColumnIdentifier) identifier.getXAxisIdentifier());
                if (!identifier.getRoleInChart().equals(ColumnRole.SERIES)
                        || xAxisColIdentifier == null) continue;
                XYSeries newSeries = new XYSeries(seriesColIdentifier.getColumnName());
                try {
                    resultSet.beforeFirst();
                    while (resultSet.next()) {
                        //XXX: need to switch from double to bigDecimal if it is needed.
                        newSeries.add(resultSet.getDouble(xAxisColIdentifier.getColumnName()), resultSet.getDouble(seriesColIdentifier.getColumnName()));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                xyCollection.addSeries(newSeries);
            }
            return xyCollection;
        } else if (allDate) {
            TimePeriodValuesCollection timeCollection = new TimePeriodValuesCollection();
            for (ColumnIdentifier identifier : columnNamesInOrder) {
                ColumnNameColumnIdentifier seriesColIdentifier = 
                    ((ColumnNameColumnIdentifier) identifier);
                ColumnNameColumnIdentifier xAxisColIdentifier = 
                    ((ColumnNameColumnIdentifier) identifier.getXAxisIdentifier());
                if (!identifier.getRoleInChart().equals(ColumnRole.SERIES)
                        || xAxisColIdentifier == null) continue;
                TimePeriodValues newSeries = new TimePeriodValues(seriesColIdentifier.getColumnName());
                try {
                    resultSet.beforeFirst();
                    while (resultSet.next()) {
                        int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(xAxisColIdentifier.getColumnName()));
                        if (columnType == Types.DATE) {
                            newSeries.add(new FixedMillisecond(resultSet.getDate(xAxisColIdentifier.getColumnName())), resultSet.getDouble(seriesColIdentifier.getColumnName()));
                        } else if (columnType == Types.TIMESTAMP){
                            newSeries.add(new FixedMillisecond(resultSet.getTimestamp(xAxisColIdentifier.getColumnName())), resultSet.getDouble(seriesColIdentifier.getColumnName()));
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
