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

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.jfree.data.general.Dataset;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.util.WebColour;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.OlapQueryEvent;
import ca.sqlpower.wabit.olap.OlapQueryListener;
import ca.sqlpower.wabit.olap.OlapUtils;
import ca.sqlpower.wabit.olap.QueryInitializationException;

public class Chart extends AbstractWabitObject {

    private static final Logger logger = Logger.getLogger(Chart.class);
    
    /**
     * The background colour for this renderer and chart background. 
     */
    private Color backgroundColour;
        
    /**
     * The Y axis label in the chart.
     */
    private String yaxisName;
    
    /**
     * The X axis label in the chart.
     */
    private String xaxisName;
    
    /**
     * This is the current style of chart the user has made.
     */
    private ExistingChartTypes chartType;
    
    /**
     * The position of the legend in relation to the chart. This
     * is defaulted to below the chart.
     */
    private LegendPosition selectedLegendPosition = LegendPosition.BOTTOM;
    
    /**
     * The query the chart is based off of. This can be either a {@link QueryCache}
     * or an {@link OlapQuery} object.
     */
    private WabitObject query;
    
    /**
     * This is the ordering of the columns in the result set the user specified
     * in the properties panel. This is preserved to have the properties panel
     * show in the same way each time the user opens the property panel and to
     * also decide which columns comes before another when multiple series are
     * involved.
     */
    private final List<ColumnIdentifier> columnNamesInOrder = new ArrayList<ColumnIdentifier>();

    /**
     * This change listener watches for changes to the streaming query and refreshes the
     * chart when a change occurs.
     */
    private final RowSetChangeListener queryListener = new RowSetChangeListener() {
        public void rowAdded(RowSetChangeEvent e) {
            // FIXME this is simply a repaint request; should be more explicit
            firePropertyChange("resultSetRowAdded", null, e.getRow());
        }
    };

    /**
     * This is a listener placed on OLAP queries to find if columns removed from
     * a query were in use in this chart.
     */
    private final OlapQueryListener olapQueryChangeListener = new OlapQueryListener() {

        public void queryExecuted(final OlapQueryEvent e) {
            SPSUtils.runOnSwingThread(new Runnable() {
                public void run() {
                    updateMissingIdentifierList(e.getCellSet());
                }
            });
        }

        /**
         * Looks through the given cell set (which can be null if the source
         * query has become invalid/empty) and figures out which parts of this
         * chart's data set are not available in the new cell set.
         * 
         * @param cellSet
         *            The new cell set this chart should be based on. Can be
         *            null if there's no longer a cell set available.
         */
        private void updateMissingIdentifierList(CellSet cellSet) {
            
            //XXX Positions aren't comparable. This is the current workaround. See bug 2101.
            List<List<String>> positionMemberUniqueNamesInColumnAxis = new ArrayList<List<String>>();
            
            if (cellSet != null) {
                CellSetAxis columnAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());

                for (Position position : columnAxis.getPositions()) {
                    List<String> positionMembers = new ArrayList<String>();
                    for (Member member : position.getMembers()) {
                        positionMembers.add(member.getUniqueName());
                    }
                    positionMemberUniqueNamesInColumnAxis.add(positionMembers);
                }

            }
            List<ColumnIdentifier> positionColumnsInUse = new ArrayList<ColumnIdentifier>();
            for (ColumnIdentifier identifier : columnNamesInOrder) {
                if (identifier.getDataType() != DataTypeSeries.NONE 
                        && identifier instanceof PositionColumnIdentifier) {
                    positionColumnsInUse.add(identifier);
                }
            }
            
            missingIdentifiers.clear();
            for (ColumnIdentifier identifier : positionColumnsInUse) {
                if (!positionMemberUniqueNamesInColumnAxis.contains(((PositionColumnIdentifier) identifier).getUniqueMemberNames())) {
                    missingIdentifiers.add(identifier);
                }
            }
        }

    };
    
    /**
     * This list tracks all of the column identifiers currently in use in the query but
     * cannot be found in the actual query object that backs this chart. The common reason
     * for columns being missing is that the user created a chart, modified the query and
     * removed columns in use in the chart, and then went to modify or use the chart.
     */
    private final List<ColumnIdentifier> missingIdentifiers = new ArrayList<ColumnIdentifier>();

    /**
     * Creates a new chart with a new unique ID.
     */
    public Chart() {
        this(null);
    }

    /**
     * Creates a new chart having the given unique ID (primarily meant for
     * reading objects from storage).
     * 
     * @param uuid
     *            The uuid the new object should have.
     */
    public Chart(String uuid) {
        super(uuid);
    }
    
    // TODO copy constructor
    
    /**
     * Resets the missing identifiers list. This list contains all of the
     * column identifiers that are defined in the chart but do not exist in the
     * query that is being used to get values for this chart. The reason why
     * some of the columns are missing is usually due to the query being modified.
     */
    public void clearMissingIdentifiers() {
        missingIdentifiers.clear();
    }

    public Color getBackgroundColour() {
        return backgroundColour;
    }

    /**
     * Returns an unmodifiable view of the missing identifiers list. The missing
     * identifiers are the parts of the chart's underlying data set which the
     * chart refers to but can no longer find (probably because the data set's
     * structure has changed since the chart was originally configured).
     * 
     * @return the current missing identifier list. It is not modifiable by you,
     *         but it may appear to change.
     */
    public List<ColumnIdentifier> getMissingIdentifiers() {
        return Collections.unmodifiableList(missingIdentifiers);
    }

    /**
     * Adds the given column identifier to the end of the missing identifiers list.
     * This method is only of practical use for code that's restoring the state
     * of a chart that's being read from a file.
     * 
     * @param ci The column identifier to add. Must not be null.
     */
    public void addMissingIdentifier(@Nonnull ColumnIdentifier ci) {
        if (ci == null) {
            throw new NullPointerException("null column identifier");
        }
        missingIdentifiers.add(ci);
    }
    
    /**
     * Returns the current result set of the query that supplies data to this
     * chart. If the query is an OlapQuery, its CellSet will be converted to a
     * resultset by calling {@link OlapUtil#toResultSet(CellSet)}.
     * 
     * @return The current result set that should be charted. If this chart's
     *         underlying query is null, this method returns null.
     */
    public ResultSet getResultSet()
        throws SQLException, QueryInitializationException, InterruptedException {
        
        if (query == null) return null;
        if (query instanceof QueryCache) {
            return ((QueryCache) query).fetchResultSet();
        } else if (query instanceof OlapQuery) {
            final OlapQuery olapQuery = (OlapQuery) query;
            logger.debug("The olap query being charted is " + olapQuery.getName() +
                    " and the query text is " + olapQuery.getMdxText());
            return OlapUtils.toResultSet(olapQuery.execute());
        } else {
            throw new IllegalStateException("Unknown query type " + query.getClass() + 
            " when trying to create a chart.");
        }
    }

    /**
     * Creates an independent JFreeChart dataset based on the current data available
     * in this chart's underlying query. The type of dataset returned depends on the
     * current chart type setting.
     * 
     * @return A JFreeChart dataset; either XYDataSet or CategoryDataSet.
     * @see #setQuery()
     * @see #setChartType(ExistingChartTypes)
     */
    public Dataset createDataset() {
        if (query instanceof QueryCache) {
            try {
                switch (chartType) {
                case BAR:
                case CATEGORY_LINE:
                    return DatasetUtil.createCategoryDataset(
                            columnNamesInOrder, ((QueryCache) query).fetchResultSet(),
                            findCategoryColumns());
                case LINE:
                case SCATTER:
                    return DatasetUtil.createSeriesCollection(
                            columnNamesInOrder, ((QueryCache) query).fetchResultSet());
                default :
                    throw new IllegalStateException("Unknown chart type " + chartType);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (query instanceof OlapQuery) {
            try {
                switch (chartType) {
                case BAR:
                case CATEGORY_LINE:
                    return DatasetUtil.createOlapCategoryDataset(
                            columnNamesInOrder, ((OlapQuery) query).execute(),
                            findCategoryColumns());
                case LINE:
                case SCATTER:
                    return DatasetUtil.createOlapSeriesCollection(
                            columnNamesInOrder, ((OlapQuery) query).execute());
                default :
                    throw new IllegalStateException("Unknown chart type " + chartType);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException(
                    "Unknown query type " + query.getClass() + " when creating a " +
                    chartType + " chart dataset.");
        }
    }

    // --------------- WabitObject implementation ------------------
    
    @Override
    public void setParent(WabitObject parent) {
        super.setParent(parent);
        
        // clean up if we're now detached from the workspace
        if (parent == null) {
            if (query instanceof StatementExecutor) {
                ((StatementExecutor) query).removeRowSetChangeListener(queryListener);
            }
        }
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return getColumnNamesInOrder();
    }

    public List<WabitObject> getDependencies() {
        if (query == null) return Collections.emptyList();
        return Collections.singletonList(query);
    }

    // ============= END of WabitObject implementation
    
    /**
     * Returns the currently-selected chart type.
     */
    public ExistingChartTypes getType() {
        return chartType;
    }

    /**
     * Selects a new chart type for this chart.
     */
    public void setChartType(ExistingChartTypes chartType) {
        firePropertyChange("chartType", this.chartType, chartType);
        this.chartType = chartType;
    }
    
    public LegendPosition getLegendPosition() {
        return selectedLegendPosition;
    }
    
    public void setLegendPosition(LegendPosition selectedLegendPosition) {
        firePropertyChange("legendPosition", this.selectedLegendPosition, selectedLegendPosition);
        this.selectedLegendPosition = selectedLegendPosition;
    }

    public void setYaxisName(String yaxisName) {
        firePropertyChange("yaxisName", this.yaxisName, yaxisName);
        this.yaxisName = yaxisName;
    }

    public String getYaxisName() {
        return yaxisName;
    }

    public void setXaxisName(String xaxisName) {
        firePropertyChange("xaxisName", this.yaxisName, yaxisName);
        this.xaxisName = xaxisName;
    }

    public String getXaxisName() {
        return xaxisName;
    }

    public List<String> getSeriesColours() {
        List<String> colourList = new ArrayList<String>();
        for(WebColour wb : ColourScheme.BREWER_SET19) {
            colourList.add(wb.toString());
        }
        return colourList;
    }

    /**
     * The query MUST be a query type that is allowed in the chart: either a
     * {@link QueryCache} or an {@link OlapQuery}.
     * 
     * @param newQuery
     * @throws IllegalArgumentException if the query is not of a supported type.
     */
    public void defineQuery(@Nullable WabitObject newQuery) throws SQLException {
        if (newQuery != null &&
                !(newQuery instanceof QueryCache || newQuery instanceof OlapQuery)) {
            throw new IllegalArgumentException(
                    "Invalid query type for chart: " + newQuery.getClass());
        }
        
        // remove old listeners
        if (query instanceof StatementExecutor) {
            if (query != null) {
                ((StatementExecutor) query).removeRowSetChangeListener(queryListener);
            }
        } else if (query instanceof OlapQuery) {
            if (query != null) {
                ((OlapQuery) query).removeOlapQueryListener(olapQueryChangeListener);
            }
        } else if (query != null) {
            throw new IllegalStateException(
                    "Invalid pre-existing query type for chart?! " + query.getClass());
        }

        query = newQuery;

        // attach new listeners
        if (newQuery instanceof StatementExecutor) {
            ((StatementExecutor) newQuery).addRowSetChangeListener(queryListener);
        } else if (newQuery instanceof OlapQuery) {
            ((OlapQuery) newQuery).addOlapQueryListener(olapQueryChangeListener);
            if (logger.isDebugEnabled()) {
                logger.debug("Getting MDX Query");
                try {
                    logger.debug("MDX Query is " + ((OlapQuery) newQuery).getMdxText());
                } catch (QueryInitializationException e) {
                    logger.debug("Error while trying to print mdx text ", e);
                }
            }
        }
    }

    /**
     * Returns the current query that this chart gets its datasets from. This
     * could be either a QueryCache or an OlapQuery, or null.
     */
    public WabitObject getQuery() {
        return query;
    }

    /**
     * Returns an unmodifiable view of the column names in the order they should
     * appear in the properties panel.
     * 
     * @return the current column names list. It is not modifiable by you,
     *         but it may appear to change.
     */
    public List<ColumnIdentifier> getColumnNamesInOrder() {
        return Collections.unmodifiableList(columnNamesInOrder);
    }

    /**
     * Adds the given column identifier to the end of the column names list.
     * Fires a childAdded event once the new column identifier has been added.
     * 
     * @param newColumnIdentifier The new column identifier to add. Must not be null.
     */
    public void addColumnIdentifier(@Nonnull ColumnIdentifier newColumnIdentifier) {
        if (newColumnIdentifier == null) {
            throw new NullPointerException("Null column identifier");
        }
        int index = columnNamesInOrder.size();
        columnNamesInOrder.add(newColumnIdentifier);
        newColumnIdentifier.setParent(this);
        fireChildAdded(ColumnIdentifier.class, newColumnIdentifier, index);
    }

    /**
     * Removes all column identifier from the end of the column names list back
     * to the beginning. Fires a childRemoved event once for each column
     * removed.
     */
    public void clearColumnIdentifiers() {
        for (int i = columnNamesInOrder.size() - 1; i >= 0; i--) {
            removeColumnIdentifier(columnNamesInOrder.get(i));
        }
    }

    /**
     * Removes the given column identifier from the column names list. If there
     * is no such column, no action is taken. Fires a childRemoved event if a
     * column was actually removed.
     */
    public void removeColumnIdentifier(ColumnIdentifier identifier) {
        int index = columnNamesInOrder.size();
        boolean removed = columnNamesInOrder.remove(identifier);
        if (removed) {
            fireChildRemoved(ColumnIdentifier.class, identifier, index);
        }
    }

    /**
     * Returns a list of the identifiers for all columns labeled as category
     * columns in a bar chart. If there are no category columns, an empty list
     * will be returned. If multiple columns are selected, the values in each
     * column will be appended to each other to create the value's name. The
     * returned column identifiers will be ordered the same as in the
     * columnNamesInOrder list, which gives users the ability to define the
     * column name order.
     */
    public List<ColumnIdentifier> findCategoryColumns() {
        List<ColumnIdentifier> categoryColumnNames = new ArrayList<ColumnIdentifier>();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (identifier.getDataType().equals(DataTypeSeries.CATEGORY)) {
                categoryColumnNames.add(identifier);
            }
        }
        return categoryColumnNames;
    }

}
