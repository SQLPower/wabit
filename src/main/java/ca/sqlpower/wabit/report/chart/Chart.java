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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.jfree.data.general.Dataset;
import org.olap4j.CellSet;

import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.RowFilter;
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
import ca.sqlpower.wabit.olap.RepeatedMember;

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
    private ChartType type;
    
    /**
     * The position of the legend in relation to the chart. This
     * is defaulted to below the chart.
     */
    private LegendPosition legendPosition = LegendPosition.BOTTOM;
    
    /**
     * The query the chart is based off of. This can be either a {@link QueryCache}
     * or an {@link OlapQuery} object.
     */
    private WabitObject query;

    /**
     * Keeps track of all the columns in the result set, along with the role
     * each column plays in this chart (category, series, and so on). This
     * list constitutes the child list of this WabitObject.
     */
    private final List<ChartColumn> chartColumns = new ArrayList<ChartColumn>();

    /**
     * This change listener watches for changes to the streaming query and refreshes the
     * chart when a change occurs.
     */
    private final RowSetChangeListener queryListener = new RowSetChangeListener() {
        public void rowAdded(RowSetChangeEvent e) {
            SPSUtils.runOnSwingThread(new Runnable() {
                public void run() {
                    try {
                        refreshData();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
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
                    try {
                        final CellSet cellSet = e.getCellSet();
                        if (cellSet != null) {
                            setUnfilteredResultSet(OlapUtils.toResultSet(cellSet));
                        }
                        else{
                        	setUnfilteredResultSet(null);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    };

    /**
     * This list tracks all of the column identifiers currently in use in the
     * query but cannot be found in the actual query object that backs this
     * chart. The common reason for columns being missing is that the user
     * created a chart, modified the query and removed columns in use in the
     * chart, and then went to modify or use the chart.
     * <p>
     * NOTE: this is a holdover from a previous incarnation of the charting
     * system. It is not presently in use, but it still seems like a good idea.
     * It's kept here as a reminder that we need to reinstate this
     * functionality.
     */
    private final List<ChartColumn> missingColumns = new ArrayList<ChartColumn>();

    /**
     * Filter that accepts or rejects rows from the result set that underlies
     * the chart data. This is applied to the result set before creating the
     * dataset. If null, all rows of the result set will be accepted.
     */
    private RowFilter resultSetFilter;

    /**
     * The current result set (not filtered). Gets updated by refreshData(), and
     * can be retrieved by {@link #getUnfilteredResultSet()}.
     */
    private CachedRowSet unfilteredResults;

    /**
     * Rotation amount, in degrees, of the text along the X axis. 0 means
     * horizontal; negative values mean increasing counterclockwise rotation;
     * positive values mean increasing clockwise rotation. Valid range is from
     * -180.0 to 180.0 inclusive.
     */
    private double xAxisLabelRotation;

    /**
     * Flag to indicate if meaningless but visually fun animations should be
     * applied to this chart when it is displayed.
     */
    private boolean gratuitouslyAnimated;

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
        missingColumns.clear();
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
    public List<ChartColumn> getMissingIdentifiers() {
        return Collections.unmodifiableList(missingColumns);
    }

    /**
     * Adds the given column identifier to the end of the missing identifiers list.
     * This method is only of practical use for code that's restoring the state
     * of a chart that's being read from a file.
     * 
     * @param ci The column identifier to add. Must not be null.
     */
    public void addMissingIdentifier(@Nonnull ChartColumn ci) {
        if (ci == null) {
            throw new NullPointerException("null column identifier");
        }
        missingColumns.add(ci);
    }

    public Color getBackgroundColour() {
        return backgroundColour;
    }

    /**
     * Returns the current result set of the query that supplies data to this
     * chart. If the query is an OlapQuery, its CellSet will be converted to a
     * ResultSet by calling {@link OlapUtil#toResultSet(CellSet)}.
     * <p>
     * No matter how the result set was obtained, it will be filtered through
     * the current {@link #resultSetFilter} before being returned.
     * 
     * @return The current result set that should be charted, filtered through
     *         {@link #resultSetFilter}. If {@link #refreshData()} has not been
     *         called on this chart instance since its creation or since the
     *         most recent call to {@link #defineQuery(WabitObject)}, this
     *         method returns null.
     * @see #getUnfilteredResultSet()
     */
    public ResultSet getResultSet()
        throws SQLException, QueryInitializationException, InterruptedException {
        
        ResultSet rs = getUnfilteredResultSet();
        if (rs == null) {
            return null;
        }
        
        if (resultSetFilter == null) {
            return rs;
        } else {
            CachedRowSet filteredRs = new CachedRowSet();
            filteredRs.populate(rs, resultSetFilter);
            rs.close();
            return filteredRs;
        }
    }

    /**
     * Returns the current result set of the query that supplies data to this
     * chart. If the query is an OlapQuery, its CellSet will be converted to a
     * ResultSet by calling {@link OlapUtil#toResultSet(CellSet)}.
     * <p>
     * The result set returned will contain all the rows supplied by the current
     * query. Specifically, it will not be subject to the
     * {@link #resultSetFilter}. This is useful in user interfaces, which can
     * show all the rows and visually indicate which ones are being used in the
     * chart and which are not.
     * 
     * @return The unfiltered version of the current result set. If
     *         {@link #refreshData()} has not been called on this chart instance
     *         since its creation or since the most recent call to
     *         {@link #defineQuery(WabitObject)}, this method returns null.
     * @throws SQLException
     * @see #getResultSet()
     */
    public CachedRowSet getUnfilteredResultSet() throws SQLException {
        if (unfilteredResults == null) {
            return null;
        } else {
            return unfilteredResults.createShared();
        }
    }

    /**
     * Refreshes the current result set in this chart. This should be called
     * automatically whenever the data provider claims it has new/different data,
     * but you can call it yourself if you want the query to run.
     * <p>
     * This method can fire a property change event for "unfilteredResultSet".
     */
    public void refreshData()
    throws SQLException, QueryInitializationException, InterruptedException {
        if (query == null) {
            setUnfilteredResultSet(null);
        } else if (query instanceof QueryCache) {
        	// force query to clear result set to prevent getting a stale cached resultset
        	((QueryCache) query).executeStatement(); 
            setUnfilteredResultSet(((QueryCache) query).fetchResultSet());
        } else if (query instanceof OlapQuery) {
            final OlapQuery olapQuery = (OlapQuery) query;
            olapQuery.execute(); // results will come to us in an OlapQueryEvent
        } else {
            throw new IllegalStateException("Unknown query type " + query.getClass() + 
            " when trying to create a chart.");
        }
    }

    /**
     * This ultimately the correct and lowest-level way to update the result set
     * that this chart is based on. However, it is easier and more convenient to
     * call {@link #refreshData()}.
     * <p>
     * This method fires a property change event for the property
     * "unfilteredResultSet". The result sets it passes around are not recreated
     * for each listener, so relying on the current row cursor position or
     * attempting to maniuplate it will lead to unpredictable results.
     * 
     * @param rs
     *            the new unfiltered result set to base the chart data on.
     * @throws SQLException 
     */
    private void setUnfilteredResultSet(CachedRowSet rs) throws SQLException {
        CachedRowSet oldUnfilteredResults = unfilteredResults;
        unfilteredResults = rs;
        
        // synchronize chart columns with new result set
        List<ChartColumn> oldCols = new ArrayList<ChartColumn>(chartColumns);
        List<ChartColumn> newCols = new ArrayList<ChartColumn>();
        if (rs != null) {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                ChartColumn existing = findByName(oldCols, columnName);
                if (existing != null) {
                    newCols.add(existing);
                } else {
                    int columnType = rsmd.getColumnType(i);
                    newCols.add(new ChartColumn(columnName, columnType));
                }
            }
        }
        
        // this part fires childRemoved and childAdded events
        removeAllColumns();
        for (ChartColumn col : newCols) {
            addChartColumn(col);
        }
        
        // notifying AFTER the column list has been synchronized with the new query
        firePropertyChange("unfilteredResultSet", oldUnfilteredResults, unfilteredResults);
    }
    
    private static ChartColumn findByName(List<ChartColumn> cols, String name) {
        for (ChartColumn col : cols) {
            if (col.getName().equals(name)) {
                return col;
            }
        }
        return null;
    }
    
    /**
     * Removes all column identifier from the end of the column names list back
     * to the beginning. Fires a childRemoved event once for each column
     * removed.
     */
    private void removeAllColumns() {
        for (int i = chartColumns.size() - 1; i >= 0; i--) {
            removeColumnIdentifier(chartColumns.get(i));
        }
    }

    /**
     * For internal use only (while refreshing the column identifier list).
     * Removes the given column identifier from the column names list. If there
     * is no such column, no action is taken. Fires a childRemoved event if a
     * column was actually removed.
     */
    private void removeColumnIdentifier(ChartColumn col) {
        int index = chartColumns.indexOf(col);
        boolean removed = chartColumns.remove(col);
        if (removed) {
            fireChildRemoved(ChartColumn.class, col, index);
        }
    }
    
    /**
     * Creates an independent JFreeChart dataset based on the current data available
     * in this chart's underlying query. The type of dataset returned depends on the
     * current chart type setting.
     * 
     * @return A JFreeChart dataset; either XYDataSet or CategoryDataSet.
     * @see #setQuery()
     * @see #setType(ChartType)
     */
    public Dataset createDataset() {
        try {
            ResultSet rs = getResultSet();
            if (rs == null) {
                logger.debug("Returning null data set because getResultSet() returned null");
                return null;
            }

            switch (type.getDatasetType()) {
            case CATEGORY:
                return DatasetUtil.createCategoryDataset(
                        chartColumns, rs,
                        findRoleColumns(ColumnRole.CATEGORY));
            case XY:
                return DatasetUtil.createSeriesCollection(
                        chartColumns, rs);
            default :
                throw new IllegalStateException("Unknown chart type " + type);
            }
        } catch (InterruptedException e) {
            // query run must have been interrupted; restore interrupted state of thread
            logger.debug("Returning null data set because thread was interrupted");
            Thread.currentThread().interrupt();
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (QueryInitializationException e) {
            throw new RuntimeException(e);
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

    /**
     * Alias for {@link #getColumns()}. Provided to satisfy {@link WabitObject} interface.
     */
    public List<? extends WabitObject> getChildren() {
        // TODO when we reinstate missing identifiers, make them children too
        return getColumns();
    }

    public List<WabitObject> getDependencies() {
        if (query == null) return Collections.emptyList();
        return Collections.singletonList(query);
    }
    
    public void removeDependency(WabitObject dependency) {
        if (dependency.equals(query)) {
            try {
                defineQuery(null);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        for (WabitObject child : getChildren()) {
            child.removeDependency(dependency);
        }
    }

    // ============= END of WabitObject implementation
    
    /**
     * Returns the currently-selected chart type.
     */
    public ChartType getType() {
        return type;
    }

    /**
     * Selects a new chart type for this chart.
     */
    public void setType(ChartType newType) {
        ChartType oldType = this.type;
        this.type = newType;
        firePropertyChange("type", oldType, newType);
    }
    
    public LegendPosition getLegendPosition() {
        return legendPosition;
    }
    
    public void setLegendPosition(LegendPosition selectedLegendPosition) {
        LegendPosition oldValue = this.legendPosition;
        this.legendPosition = selectedLegendPosition;
        firePropertyChange("legendPosition", oldValue, selectedLegendPosition);
    }

    public void setYaxisName(String yaxisName) {
        String oldValue = this.yaxisName;
        this.yaxisName = yaxisName;
        firePropertyChange("yaxisName", oldValue, yaxisName);
    }

    public String getYaxisName() {
        return yaxisName;
    }

    public void setXaxisName(String xaxisName) {
        String oldValue = this.xaxisName;
        this.xaxisName = xaxisName;
        firePropertyChange("xaxisName", oldValue, xaxisName);
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
     * <p>
     * You'll probably want to call {@link #refreshData()} after defining a new query.
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
            setResultSetFilter(null);
        } else if (newQuery instanceof OlapQuery) {
            ((OlapQuery) newQuery).addOlapQueryListener(olapQueryChangeListener);
            setResultSetFilter(new OlapRowFilter());
        }
    }

    private void setResultSetFilter(RowFilter resultSetFilter) {
        RowFilter oldValue = this.resultSetFilter;
        this.resultSetFilter = resultSetFilter;
        firePropertyChange("resultSetFilter", oldValue, resultSetFilter);
    }
    
    public RowFilter getResultSetFilter() {
        return resultSetFilter;
    }
    
    /**
     * Returns the current query that this chart gets its datasets from. This
     * could be either a QueryCache or an OlapQuery, or null.
     */
    public WabitObject getQuery() {
        return query;
    }

    /**
     * Returns an unmodifiable view of the result set columns this chart knows
     * about, along with information about their role in the chart.
     * 
     * @return the current column list. It is not modifiable by you, but
     *         it may appear to change.
     */
    public List<ChartColumn> getColumns() {
        return Collections.unmodifiableList(chartColumns);
    }

    /**
     * For internal use only (while reading a Workspace file or refreshing the
     * column list when a new result set comes in). Adds the given column
     * identifier to the end of the column names list. Fires a childAdded event
     * once the new column identifier has been added.
     * 
     * @param newColumnIdentifier
     *            The new column identifier to add. Must not be null.
     */
    public void addChartColumn(@Nonnull ChartColumn newColumnIdentifier) {
        if (newColumnIdentifier == null) {
            throw new NullPointerException("Null column identifier");
        }
        int index = chartColumns.size();
        chartColumns.add(newColumnIdentifier);
        newColumnIdentifier.setParent(this);
        fireChildAdded(ChartColumn.class, newColumnIdentifier, index);
    }

    /**
     * Returns a list of the identifiers for all columns labeled as a given
     * role in a bar chart. If there are no such columns, an empty list
     * will be returned. If multiple columns are selected, the values in each
     * column will be appended to each other to create the value's name. The
     * returned column identifiers will be ordered the same as in the
     * columnNamesInOrder list, which gives users the ability to define the
     * column name order.
     */
    public List<ChartColumn> findRoleColumns(ColumnRole role) {
        List<ChartColumn> categoryColumnNames = new ArrayList<ChartColumn>();
        for (ChartColumn identifier : chartColumns) {
            if (identifier.getRoleInChart().equals(role)) {
                categoryColumnNames.add(identifier);
            }
        }
        return categoryColumnNames;
    }

    /**
     * A result set filter that hides the appropriate rows from being charted in
     * an MDX-derived result set.
     */
    private final class OlapRowFilter implements RowFilter {

        public boolean acceptsRow(Object[] row) throws SQLException {
            
            // it would be nice to cache this, but we'd need a notification mechanism
            // for flushing the cache every time the dependant data changes
            List<ChartColumn> categoryColumns = findRoleColumns(ColumnRole.CATEGORY);
            if (categoryColumns.isEmpty()) {
                return true;
            }
            
            int nullCategories = 0;
            int repeatedMembers = 0;
            
            for (ChartColumn catCol : categoryColumns) {
                int idx = unfilteredResults.findColumn(catCol.getName());
                Object val = row[idx - 1];
                
                if (val == null) {
                    nullCategories++;
                } else if (val instanceof RepeatedMember) {
                    repeatedMembers++;
                }
            }
            
            return nullCategories + repeatedMembers < categoryColumns.size();
        }
        
    }

    /**
     * Returns the desired rotation for the X-axis category/item labels. 0 means
     * horizontal; -90 means read bottom to top; 90 means read top to bottom.
     */
    public double getXaxisLabelRotation() {
        return xAxisLabelRotation;
    }

    /**
     * Sets the desired rotation for the X-axis category/item labels. 0 means
     * horizontal; -90 means read bottom to top; 90 means read top to bottom.
     * <p>
     * Fires a property change if the new value differs from the existing value.
     * 
     * @param xAxisLabelRotation
     *            The desired rotation. Must be between -90 and 90 inclusive.
     */
    public void setXAxisLabelRotation(double xAxisLabelRotation) {
        if (xAxisLabelRotation < -90.0 || xAxisLabelRotation > 90.0) {
            throw new IllegalArgumentException(
                    "Rotation " + xAxisLabelRotation +
                    " is outside the range [-90.0 .. 90.0]");
        }
        double oldValue = this.xAxisLabelRotation;
        this.xAxisLabelRotation = xAxisLabelRotation;
        firePropertyChange("xAxisLabelRotation", oldValue, xAxisLabelRotation);
    }

    public void setGratuitouslyAnimated(boolean gratuitouslyAnimated) {
        boolean oldValue = this.gratuitouslyAnimated;
        this.gratuitouslyAnimated = gratuitouslyAnimated;
        firePropertyChange("gratuitouslyAnimated", oldValue, gratuitouslyAnimated);
    }

    public boolean isGratuitouslyAnimated() {
        return gratuitouslyAnimated;
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        if (getColumns().contains(child)) {
            throw new IllegalStateException("The children of the renderer are maintained internally." +
                " There should be no need to remove them outside of this class.");
        }
        return false;
    }
}
