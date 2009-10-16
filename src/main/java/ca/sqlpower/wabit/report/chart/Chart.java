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
import ca.sqlpower.util.WebColour;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.CleanupExceptions;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.olap.RepeatedMember;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetProducerEvent;

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
     * The source of results this chart uses to create its dataset.
     */
    private ResultSetProducer query;

    /**
     * Keeps track of all the columns in the result set, along with the role
     * each column plays in this chart (category, series, and so on). This
     * list constitutes the child list of this WabitObject.
     */
    private final List<ChartColumn> chartColumns = new ArrayList<ChartColumn>();

    /**
     * This is a listener placed on the ResultSetProducer to find if columns removed from
     * a query were in use in this chart.
     */
    private final ResultSetListener resultSetListener = new ResultSetListener() {
        public void resultSetProduced(final ResultSetProducerEvent evt) {
            runInForeground(new Runnable() {
                public void run() {
                    try {
                        setUnfilteredResultSet(evt.getResults().getFirstNonNullResultSet());
                    } catch (SQLException e) {
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
     * List of currently-registered data listeners. This list contains no nulls.
     */
    private final List<ChartDataListener> dataListeners = new ArrayList<ChartDataListener>();

    /**
     * Fires a ChartDataChangedEvent whenever the current unfiltered result set
     * notifies us about new data. The hooked-upedness of this listener is managed
     * by {@link #setUnfilteredResultSet(CachedRowSet)}.
     */
    private final RowSetChangeListener rowSetListener = new RowSetChangeListener() {
        public void rowAdded(RowSetChangeEvent e) {
            fireDataChangedEvent();
        }
    };

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
        fireChildAdded(ChartColumn.class, ci, missingColumns.indexOf(ci));
    }

    public Color getBackgroundColour() {
        return backgroundColour;
    }
    
    public void setBackgroundColour(Color backgroundColour) {
    	Color oldColour = this.backgroundColour;
    	this.backgroundColour = backgroundColour;
    	firePropertyChange("backgroundColour", oldColour, backgroundColour);
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
     * This ultimately the correct and lowest-level way to update the result set
     * that this chart is based on. However, it is easier and more convenient to
     * call {@link #getQuery()}.execute().
     * <p>
     * This method fires a property change event for the property
     * "unfilteredResultSet". The result sets it passes around are not recreated
     * for each listener, so relying on the current row cursor position or
     * attempting to manipulate it will lead to unpredictable results.
     * 
     * @param rs
     *            the new unfiltered result set to base the chart data on.
     * @throws SQLException 
     */
    private void setUnfilteredResultSet(CachedRowSet rs) throws SQLException {
        if (unfilteredResults != null) {
            unfilteredResults.removeRowSetListener(rowSetListener );
        }
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
        
        if (unfilteredResults != null) {
            unfilteredResults.addRowSetListener(rowSetListener);
        }
        
        fireDataChangedEvent();
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
        return Collections.singletonList((WabitObject) query);
    }
    
    public void removeDependency(WabitObject dependency) {
        if (dependency.equals(query)) {
            try {
                setQuery(null);
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
     * Replaces this chart's source of result sets with the given result set producer,
     * firing a property change event. The current result set filter will also be set
     * to an OlapRowFilter or null (depending on newQuery's type) as a side effect of
     * calling this method.
     * <p>
     * You'll probably want to call {@link #refreshData()} after defining a new query.
     * 
     * TODO nothing in here throws SQLException, remove it.
     * 
     * @param newQuery
     * @throws IllegalArgumentException if the query is not of a supported type.
     */
    public void setQuery(@Nullable ResultSetProducer newQuery) throws SQLException {
        
        // remove old listeners
        if (query != null) {
            query.removeResultSetListener(resultSetListener);
        }

        ResultSetProducer oldQuery = query;
        query = newQuery;

        if (newQuery instanceof OlapQuery) {
            setResultSetFilter(new OlapRowFilter());
        } else {
            setResultSetFilter(null);
        }
        
        // attach new listeners
        if (query != null) {
            query.addResultSetListener(resultSetListener);
        }
        
        firePropertyChange("query", oldQuery, newQuery);
    }

    private void setResultSetFilter(RowFilter resultSetFilter) {
        this.resultSetFilter = resultSetFilter;
    }
    
    public RowFilter getResultSetFilter() {
        return resultSetFilter;
    }

    /**
     * Returns the current query that this chart gets its datasets from. Can be
     * null.
     */
    public ResultSetProducer getQuery() {
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
        addChartColumn(newColumnIdentifier, chartColumns.size());
    }

    /**
     * For internal use only (while reading a Workspace file or refreshing the
     * column list when a new result set comes in). Adds the given column
     * identifier to the end of the column names list. Fires a childAdded event
     * once the new column identifier has been added.
     * 
     * @param newColumnIdentifier
     *            The new column identifier to add. Must not be null.
     * @param index
     *            The index to add the chart column at. Cannot be greater than
     *            the current number of identifiers in the chart.
     */
    public void addChartColumn(@Nonnull ChartColumn newColumnIdentifier, int index) {
        if (newColumnIdentifier == null) {
            throw new NullPointerException("Null column identifier");
        }
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
    public double getXAxisLabelRotation() {
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
        firePropertyChange("XAxisLabelRotation", oldValue, xAxisLabelRotation);
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
    /*
     * removing a child to a chart should only be done in special cases such as
     * through an undo manager or synchronizing with a server.
     */
    protected boolean removeChildImpl(WabitObject child) {
        if (getColumns().contains(child)) {
            removeColumnIdentifier((ChartColumn) child);
            return true;
        }
        return false;
    }
    
    @Override
    /*
     * Adding a child to a chart should only be done in special cases such as
     * through an undo manager or synchronizing with a server.
     */
    protected void addChildImpl(WabitObject child, int index) {
        addChartColumn((ChartColumn) child, index);
    }
    
    /**
     * Registers the given listener to receive an event every time the dataset
     * returned by {@link #createDataset()} might be different. These events
     * typically happen whenever the chart's result set provider changes, as
     * well as when a streaming query delivers a new row of data.
     * 
     * @param l the listener to add. Must not be null.
     */
    public void addChartDataListener(@Nonnull ChartDataListener l) {
        if (l == null) {
            throw new NullPointerException("Null listener");
        }
        dataListeners.add(l);
    }

    /**
     * Removes the given listener from the list of parties interested in data
     * change events. If the given listener was registered multiple times, this
     * call only removes one of the registrations. If the given listener is not
     * currently registered, this method has no effect.
     * 
     * @param l
     *            the listener to remove. Null is silently ignored.
     */
    public void removeChartDataListener(@Nullable ChartDataListener l) {
        dataListeners.remove(l);
    }
    
    /**
     * Delivers a data change notification to all registered listeners.
     */
    private void fireDataChangedEvent() {
        ChartDataChangedEvent evt = new ChartDataChangedEvent(this);
        for (int i = dataListeners.size() - 1; i >= 0; i--) {
            dataListeners.get(i).chartDataChanged(evt);
        }
    }
    
    @Override
    public CleanupExceptions cleanup() {
        CleanupExceptions exceptions = new CleanupExceptions();
        try {
            setQuery(null);
        } catch (Exception e) {
            exceptions.add(e);
        }
        try {
            setUnfilteredResultSet(null);
        } catch (Exception e) {
            exceptions.add(e);
        }
        
        return exceptions;
    }
}
