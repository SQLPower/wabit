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

package ca.sqlpower.wabit.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.util.WebColour;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.olap.MemberHierarchyComparator;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.OlapQueryEvent;
import ca.sqlpower.wabit.olap.OlapQueryListener;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.report.chart.ColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnNameColumnIdentifier;
import ca.sqlpower.wabit.report.chart.PositionColumnIdentifier;
import ca.sqlpower.wabit.report.chart.RowAxisColumnIdentifier;

/**
 * This class will render a chart from a query's result set in a chart format
 * defined by the user.
 */
public class ChartRenderer extends AbstractWabitObject implements ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(ChartRenderer.class);

    /**
     * This separator is used to separate category names when more then one
     * column is selected as the category in a bar chart.
     */
	private static final String CATEGORY_SEPARATOR = ", ";
	
	/**
	 * This enum contains the values that each column can be defined as
	 * for laying out a chart.
	 */
	public enum DataTypeSeries {
		NONE,
		CATEGORY,
		SERIES
	};
	
	/**
	 * The types of charts this renderer can create.
	 */
	public enum ExistingChartTypes {
		BAR,
		CATEGORY_LINE,
		LINE,
		SCATTER
	}
	
	/**
	 * The possible positions a legend can occupy on a chart
	 */
	public enum LegendPosition {
		NONE,
		TOP,
		LEFT,
		RIGHT,
		BOTTOM
	}

	/**
	 * This object is used to define a row in a category dataset for an OLAP dataset. Each row
	 * can be defined by a combination of a {@link Position} and any number of strings which 
	 * are the values in the columns defined as categories. A position is always less than
	 * a string for the comparison and a shorter list is less than a longer one.
	 */
	private static class ComparableCategoryRow implements Comparable<ComparableCategoryRow> {

	    /**
	     * This list contains the elements being compared to in the order they are to be compared.
	     */
	    private final List<Object> comparableObjects = new ArrayList<Object>();
	    
	    private final MemberHierarchyComparator comparator = new MemberHierarchyComparator();
	    
        public int compareTo(ComparableCategoryRow o) {
            int i;
            for (i = 0; i < comparableObjects.size(); i++) {
                if (o.comparableObjects.size() == i) return 1;
                
                Object thisObject = comparableObjects.get(i);
                Object otherObject = o.comparableObjects.get(i);
                
                if (thisObject instanceof String && otherObject instanceof Position) return 1;
                if (thisObject instanceof Position && otherObject instanceof String) return -1;
                if (thisObject instanceof String && otherObject instanceof String) {
                    int comparedValue = ((String) thisObject).compareTo((String) otherObject);
                    if (comparedValue != 0) return comparedValue;
                } else if (thisObject instanceof Position && otherObject instanceof Position) {
                    int j;
                    final Position thisPosition = (Position) thisObject;
                    final Position otherPosition = (Position) otherObject;
                    for (j = 0; j < thisPosition.getMembers().size(); j++) {
                        if (otherPosition.getMembers().size() == j) return 1;
                        Member thisMember = thisPosition.getMembers().get(j);
                        Member otherMember = otherPosition.getMembers().get(j);
                        int comparedValue = comparator.compare(thisMember, otherMember);
                        if (comparedValue != 0) return comparedValue;
                    }
                    if (j < otherPosition.getMembers().size()) return -1;
                }
            }
            if (i < o.comparableObjects.size()) return -1;
            return 0;
        }

        public void add(String formattedValue) {
            comparableObjects.add(formattedValue);
        }

        public void add(Position position) {
            comparableObjects.add(position);
        }
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (Object o : comparableObjects) {
                if (!first) sb.append(", ");
                if (o instanceof String) {
                    sb.append((String) o);
                } else if (o instanceof Position) {
                    boolean firstMember = true;
                    for (Member member : ((Position) o).getMembers()) {
                        if (!first || !firstMember) sb.append(", ");
                        sb.append(member.getName());
                        firstMember = false;
                    }
                }
                first = false;
            }
            return sb.toString();
        }
	    
	}
	
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
	 * This maps each column in the result set to a DataTypeSeries. The types
	 * decide how each column in the result set are used to display on the chart. 
	 */
	private final Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes = new HashMap<ColumnIdentifier, DataTypeSeries>();

	/**
	 * This map contains each column listed as a series and the column to be used as X axis values.
	 * This mapping is used for line and line-like charts.
	 */
	private final Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis = new HashMap<ColumnIdentifier, ColumnIdentifier>();
	
	/**
	 * This change listener watches for changes to the streaming query and refreshes the
	 * chart when a change occurs.
	 */
	private final RowSetChangeListener queryListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			firePropertyChange("resultSetRowAdded", null, e.getRow());
		}
	};
	
	/**
	 * This is a listener placed on OLAP queries to find if columns removed from a query were in use
	 * in this chart. 
	 * 
	 * XXX This can be simplified when the olap4j query can be listened to and we can specifically
	 * listen for members in the column axis being removed.
	 */
	private final OlapQueryListener olapQueryChangeListener = new OlapQueryListener() {

	    public void queryExecuted(OlapQueryEvent e) {
            
            if (!(query instanceof OlapQuery)) throw new IllegalStateException("The listener to update the chart on OLAP query changes was added to a query of type " + query + " which does not extend OlapQuery.");
            
            final CellSet cellSet = e.getCellSet();
            CellSetAxis columnAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
            
            //XXX Positions aren't comparable so going to compare based on the unique names of their member list.
            //This can be simplified when positions become comparable or their equals method is defined.
            List<List<String>> positionMemberUniqueNamesInColumnAxis = new ArrayList<List<String>>();
            for (Position position : columnAxis.getPositions()) {
                List<String> positionMembers = new ArrayList<String>();
                for (Member member : position.getMembers()) {
                    positionMembers.add(member.getUniqueName());
                }
                positionMemberUniqueNamesInColumnAxis.add(positionMembers);
            }
            
            List<ColumnIdentifier> positionColumnsInUse = new ArrayList<ColumnIdentifier>();
            for (Map.Entry<ColumnIdentifier, DataTypeSeries> entry : columnsToDataTypes.entrySet()) {
                if (entry.getValue() != DataTypeSeries.NONE && entry.getKey() instanceof PositionColumnIdentifier) {
                    positionColumnsInUse.add(entry.getKey());
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
	 * removed columns in use in the chart, and then when to modify or use the chart.
	 */
	private final List<ColumnIdentifier> missingIdentifiers = new ArrayList<ColumnIdentifier>();
	
	public ChartRenderer(ContentBox parent, String uuid) {
		super(uuid);
	}
	
	/**
	 * This will reset the missing identifiers list. This list contains all of the
	 * column identifiers that are defined in the chart but do not exist in the
	 * query that is being used to get values for this chart. The reason why
	 * some of the columns are missing is usually due to the query being modified.
	 */
	public void clearMissingIdentifiers() {
	    missingIdentifiers.clear();
    }

    public ChartRenderer() {
        super();
		setName("Chart");
	}
    
    //TODO when charts become first class citizens have a copy constructor for a chart and chart renderer
//    public ChartRenderer(ChartRenderer chartRenderer) {
//    	throw new UnsupportedOperationException("not implemented yet");
//	}
	
	public void cleanup() {
		if (query instanceof StatementExecutor) {
			((StatementExecutor) query).removeRowSetChangeListener(queryListener);
		}
	}

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
	    if (!missingIdentifiers.isEmpty()) {
	        int fontHeight = g.getFontMetrics().getHeight();
	        int startingYPos = (int) ((contentBox.getHeight() - fontHeight) / 2);
	        String errorString = "There are columns missing from the query but used in the chart.";
            g.drawString(errorString, (int) ((contentBox.getWidth() - g.getFontMetrics().stringWidth(errorString)) / 2), startingYPos);
	        errorString = "Edit the query to update the columns.";
	        g.drawString(errorString, (int) ((contentBox.getWidth() - g.getFontMetrics().stringWidth(errorString)) / 2), fontHeight + startingYPos);
	        return false;
	    }
	        
		JFreeChart chart = null;
		try {
			if (query != null) {
			    if (query instanceof QueryCache) {
			        chart = ChartRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, ((QueryCache) query).fetchResultSet(), chartType, selectedLegendPosition, getName(), yaxisName, xaxisName);
			    } else if (query instanceof OlapQuery) {
			        final OlapQuery olapQuery = (OlapQuery) query;
                    logger.debug("The olap query being charted is " + olapQuery.getName() + " and the query text is " + olapQuery.getMdxText());
			        chart = ChartRenderer.createJFreeChart(columnNamesInOrder, columnsToDataTypes, columnSeriesToColumnXAxis, olapQuery.execute(), chartType, selectedLegendPosition, getName(), yaxisName, xaxisName);
			    } else {
			        throw new IllegalStateException("Unknown query type " + query.getClass() + " when trying to create a chart.");
			    }
			}
			if (chart == null) {
			    g.drawString("Empty Chart", 0, g.getFontMetrics().getHeight());
			    return false;
			}
			chart.draw(g, new Rectangle2D.Double(0, 0, contentBox.getWidth(), contentBox.getHeight()));
		} catch (Exception e) {
		    logger.error("Error while rendering chart", e);
		    g.drawString("Could not render chart: " + e.getMessage(), 0, g.getFontMetrics().getHeight());
		}
		return false;
	}

    /**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart.
     * 
     * @param columnNamesInOrder
     *            A list of {@link ColumnIdentifier}s that define the order the
     *            columns are in. This is used to decide the order the columns
     *            marked as series come in when creating the chart.
     * @param columnsToDataTypes
     *            This maps the {@link ColumnIdentifier}s to a data type that
     *            defines how the column is used in the chart. This is used for
     *            bar charts.
     * @param columnSeriesToColumnXAxis
     *            This maps {@link ColumnIdentifier}s defined to be series in a
     *            chart to columns that are used as the x-axis values. This is
     *            used for line and scatter charts.
     * @param resultSet
     *            The result set to take values from for chart data.
     * @param chartType
     *            The type of chart to create.
     * @param legendPosition
     *            Where the legend should go in the chart.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y-axis of the chart.
     * @param xaxisName
     *            The name of the x-axis of the chart.
     */
	public static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
	        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
	        Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
	        ResultSet resultSet, ExistingChartTypes chartType, LegendPosition legendPosition, 
	        String chartName, String yaxisName, String xaxisName) {
	    return createJFreeChart(columnNamesInOrder, columnsToDataTypes,
	            columnSeriesToColumnXAxis, resultSet, null, chartType,
	            legendPosition, chartName, yaxisName, xaxisName);
	}
	
	/**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart.
     * 
     * @param columnNamesInOrder
     *            A list of {@link ColumnIdentifier}s that define the order the
     *            columns are in. This is used to decide the order the columns
     *            marked as series come in when creating the chart.
     * @param columnsToDataTypes
     *            This maps the {@link ColumnIdentifier}s to a data type that
     *            defines how the column is used in the chart. This is used for
     *            bar charts.
     * @param columnSeriesToColumnXAxis
     *            This maps {@link ColumnIdentifier}s defined to be series in a
     *            chart to columns that are used as the x-axis values. This is
     *            used for line and scatter charts.
     * @param cellSet
     *            The cell set to take values from for chart data.
     * @param chartType
     *            The type of chart to create.
     * @param legendPosition
     *            Where the legend should go in the chart.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y-axis of the chart.
     * @param xaxisName
     *            The name of the x-axis of the chart.
     */
    public static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
            Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
            Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
            CellSet cellSet, ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        return createJFreeChart(columnNamesInOrder, columnsToDataTypes,
                columnSeriesToColumnXAxis, null, cellSet, chartType,
                legendPosition, chartName, yaxisName, xaxisName);
    }

    /**
     * Creates a JFreeChart based on the given column and series data. Returns
     * null if the data given cannot create a chart. If the chart is to be
     * created with a result set then the cellSet should be null. If a chart is
     * to be created with a cell set then the resultSet should be null. Only one
     * of the two values should not be null.
     */
	public static JFreeChart createJFreeChart(List<ColumnIdentifier> columnNamesInOrder, 
	        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes, 
	        Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, 
	        ResultSet resultSet, CellSet cellSet, ExistingChartTypes chartType, LegendPosition legendPosition, 
	        String chartName, String yaxisName, String xaxisName) {
		if (chartType == null) {
			return null;
		}
		RectangleEdge rEdge = RectangleEdge.BOTTOM;
		boolean showLegend = true;
		switch (legendPosition) {
		case NONE: showLegend = false;
					break;
		case TOP: rEdge = RectangleEdge.TOP; 
					break;
		case LEFT: rEdge = RectangleEdge.LEFT; 
					break;
		case RIGHT: rEdge = RectangleEdge.RIGHT; 
					break;
		case BOTTOM: break;
		default:
			throw new IllegalStateException("Unknown legend position " + legendPosition);
		}
		
		JFreeChart chart;
		XYDataset xyCollection;
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		BarRenderer.setDefaultBarPainter(new StandardBarPainter());
		
		switch (chartType) {
		case BAR :
		case CATEGORY_LINE:
			if (!columnsToDataTypes.containsValue(DataTypeSeries.CATEGORY) || !columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			List<ColumnIdentifier> categoryColumns = findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes);
			CategoryDataset dataset;
			if (resultSet != null) {
			    dataset = createCategoryDataset(columnNamesInOrder,
			            columnsToDataTypes, resultSet, categoryColumns);
			} else if (cellSet != null) {
			    dataset = createOlapCategoryDataset(columnNamesInOrder,
                        columnsToDataTypes, cellSet, categoryColumns);
			} else {
			    return null;
			}
			List<String> categoryColumnNames = new ArrayList<String>();
			for (ColumnIdentifier identifier : categoryColumns) {
			    categoryColumnNames.add(identifier.getName());
			}
			if (chartType == ExistingChartTypes.BAR) {
			    chart = ChartFactory.createBarChart(chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
			} else if (chartType == ExistingChartTypes.CATEGORY_LINE) {
			    chart = ChartFactory.createLineChart(chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
			} else {
			    throw new IllegalArgumentException("Unknown chart type " + chartType + " for a category dataset.");
			}
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			
			CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
			int seriesSize = chart.getCategoryPlot().getDataset().getRowCount();
			for (int i = 0; i < seriesSize; i++) {
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				renderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			if (renderer instanceof BarRenderer) {
			    BarRenderer barRenderer = (BarRenderer) renderer;
			    barRenderer.setShadowVisible(false);
			}
			setTransparentChartBackground(chart);
			return chart;
		case LINE :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			if (resultSet != null) {
			    xyCollection = createSeriesCollection(
			            columnSeriesToColumnXAxis, resultSet);
			} else if (cellSet != null) {
			    xyCollection = createOlapSeriesCollection(columnSeriesToColumnXAxis, cellSet);
			} else {
			    return null;
			}
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createXYLineChart(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, showLegend, true, false);
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			final XYItemRenderer xyirenderer = chart.getXYPlot().getRenderer();
			int xyLineSeriesSize = chart.getXYPlot().getDataset().getSeriesCount();
			for (int i = 0; i < xyLineSeriesSize; i++) {
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				xyirenderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			setTransparentChartBackground(chart);
			return chart;
		case SCATTER :
			if (!columnsToDataTypes.containsValue(DataTypeSeries.SERIES)) {
				return null;
			}
			if (resultSet != null) {
                xyCollection = createSeriesCollection(
                        columnSeriesToColumnXAxis, resultSet);
            } else if (cellSet != null) {
                xyCollection = createOlapSeriesCollection(columnSeriesToColumnXAxis, cellSet);
            } else {
                return null;
            }
			if (xyCollection == null) {
				return null;
			}
			chart = ChartFactory.createScatterPlot(chartName, xaxisName, yaxisName, xyCollection, PlotOrientation.VERTICAL, showLegend, true, false);
			if (legendPosition != LegendPosition.NONE) {
				chart.getLegend().setPosition(rEdge);
				chart.getTitle().setPadding(4,4,15,4);
			}
			final XYItemRenderer xyIrenderer = chart.getXYPlot().getRenderer();
			BasicStroke circle = new BasicStroke();
			int xyScatterSeriesSize = chart.getXYPlot().getDataset().getSeriesCount();
			for (int i = 0; i < xyScatterSeriesSize; i++) {
				xyIrenderer.setSeriesShape(i, circle.createStrokedShape(new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0)));
				//XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
				xyIrenderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
			}
			setTransparentChartBackground(chart);
			return chart; 
		default:
			throw new IllegalStateException("Unknown chart type " + chartType);
		}
	}
	
	private static void setTransparentChartBackground(JFreeChart chart) {
		chart.setBackgroundPaint(new Color(255,255,255,0));
		chart.getPlot().setBackgroundPaint(new Color(255,255,255,0));	
		chart.getPlot().setBackgroundAlpha(0.0f);
	}

    /**
     * This will find the columns labeled as the category column in a bar chart.
     * If there is no category column an empty list will be returned. If
     * multiple columns are selected the values in each column should be
     * appended to each other to create the value's name. The column names
     * should be ordered by the columnNamesInOrder list. This list gives users
     * the ability to define the column name order.
     */
	private static List<ColumnIdentifier> findCategoryColumnNames(
	        List<ColumnIdentifier> columnNamesInOrder,
			Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes) {
		List<ColumnIdentifier> categoryColumnNames = new ArrayList<ColumnIdentifier>();
		for (ColumnIdentifier identifier : columnNamesInOrder) {
		    if (columnsToDataTypes.get(identifier) == DataTypeSeries.CATEGORY) {
		        categoryColumnNames.add(identifier);
		    }
		}
		return categoryColumnNames;
	}
	
	/**
     * This is a helper method for creating a CategoryDataset for OLAP
     * queries. This method takes in a {@link CellSet} as well as information
     * about what columns to set as categories and series to make a dataset.
     */
    private static CategoryDataset createOlapCategoryDataset(
            List<ColumnIdentifier> columnNamesInOrder,
            Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes,
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
            ColumnIdentifier colToTypeIdentifier = null;
            DataTypeSeries dataType = null;
            for (Map.Entry<ColumnIdentifier, DataTypeSeries> colToTypeIdentifierEntry : columnsToDataTypes.entrySet()) {
                if (colToTypeIdentifierEntry.getKey().equals(identifier)) {
                    colToTypeIdentifier = colToTypeIdentifierEntry.getKey();
                    dataType = colToTypeIdentifierEntry.getValue();
                    break;
                }
            }
            if (dataType != DataTypeSeries.SERIES) continue;
            
            seriesPositions.add(((PositionColumnIdentifier) identifier).getPosition(cellSet).getOrdinal());
            seriesNames.add(colToTypeIdentifier.getName());
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
			Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes,
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
			    String categoryRowName = createCategoryName(categoryRowNames);
				if (!uniqueNamesInCategory.contains(categoryRowName)) {
					uniqueNamesInCategory.add(categoryRowName);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
        List<String> seriesColumnNames = new ArrayList<String>();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
        	if (columnsToDataTypes.get(identifier) == DataTypeSeries.SERIES) {
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
                String categoryRowName = createCategoryName(categoryRowNames);
        		for (String colName : seriesColumnNames) {
        			if (logger.isDebugEnabled() && (seriesColumnNames.indexOf(colName) == -1 || uniqueNamesInCategory.indexOf(categoryRowName) == -1)) {
        				logger.debug("Index of series " + colName + " is " + seriesColumnNames.indexOf(colName) + ", index of category " + categoryColumnIdentifiers + " is " + uniqueNamesInCategory.indexOf(categoryRowName));
        			}
        			data[seriesColumnNames.indexOf(colName)][uniqueNamesInCategory.indexOf(categoryRowName)] += resultSet.getDouble(colName); //XXX Getting numeric values as double causes problems for BigDecimal and BigInteger.
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
	 * Helper method for creating line and scatter charts in the
	 * createJFreeChart method. This is for relational queries only.
	 * @return An XYDataset for use in a JFreeChart or null if an 
	 * XYDataset cannot be created.
	 */
	private static XYDataset createSeriesCollection(
			Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, ResultSet resultSet) {
		boolean allNumeric = true;
		boolean allDate = true;
		try {
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
				int columnType = resultSet.getMetaData().getColumnType(resultSet.findColumn(((ColumnNameColumnIdentifier) entry.getValue()).getColumnName()));
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
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
			    ColumnNameColumnIdentifier seriesColIdentifier = ((ColumnNameColumnIdentifier) entry.getKey());
			    ColumnNameColumnIdentifier xAxisColIdentifier = ((ColumnNameColumnIdentifier) entry.getValue());
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
			for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
			    ColumnNameColumnIdentifier seriesColIdentifier = ((ColumnNameColumnIdentifier) entry.getKey());
                ColumnNameColumnIdentifier xAxisColIdentifier = ((ColumnNameColumnIdentifier) entry.getValue());
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
	
	/**
     * Helper method for creating line and scatter charts in the
     * createJFreeChart method. This is for olap queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    private static XYDataset createOlapSeriesCollection(
            Map<ColumnIdentifier, ColumnIdentifier> columnSeriesToColumnXAxis, CellSet cellSet) {
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        for (Map.Entry<ColumnIdentifier, ColumnIdentifier> entry : columnSeriesToColumnXAxis.entrySet()) {
            PositionColumnIdentifier seriesColIdentifier = ((PositionColumnIdentifier) entry.getKey());
            PositionColumnIdentifier xAxisColIdentifier = ((PositionColumnIdentifier) entry.getValue());
            List<String> memberNames = new ArrayList<String>();
            for (Member member : seriesColIdentifier.getPosition(cellSet).getMembers()) {
                memberNames.add(member.getName());
            }
            XYSeries newSeries = new XYSeries(createCategoryName(memberNames));
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
	
	public void resetToFirstPage() {
		//do nothing.
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return new ArrayList<WabitObject>();
	}

	public ExistingChartTypes getChartType() {
		return chartType;
	}

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

	public WabitObject getQuery() {
		return query;
	}

	public void defineQuery(WabitObject query) throws SQLException {
		if (this.query instanceof StatementExecutor) {
			if (this.query != null) {
				((StatementExecutor) this.query).removeRowSetChangeListener(queryListener);
			}
		} else if (this.query instanceof OlapQuery) {
		    if (this.query != null) {
		        ((OlapQuery) this.query).removeOlapQueryListener(olapQueryChangeListener);
		    }
		}
		if (query instanceof StatementExecutor) {
			((StatementExecutor) query).addRowSetChangeListener(queryListener);
		} else if (query instanceof OlapQuery) {
		    ((OlapQuery) query).addOlapQueryListener(olapQueryChangeListener);
		    if (logger.isDebugEnabled()) {
		        logger.debug("Getting MDX Query");
		        try {
                    logger.debug("MDX Query is " + ((OlapQuery) query).getMdxText());
                } catch (QueryInitializationException e) {
                    logger.debug("Error while trying to print mdx text ", e);
                }
		    }
		}
		this.query = query;
	}
	
	public List<ColumnIdentifier> getColumnNamesInOrder() {
		return columnNamesInOrder;
	}
	
	public void setColumnNamesInOrder(List<ColumnIdentifier> newColumnOrdering) {
		firePropertyChange("columnNamesInOrder", this.columnNamesInOrder, newColumnOrdering);
		columnNamesInOrder.clear();
		columnNamesInOrder.addAll(newColumnOrdering);
	}

	public Map<ColumnIdentifier, DataTypeSeries> getColumnsToDataTypes() {
		return columnsToDataTypes;
	}
	
	public void setColumnsToDataTypes(Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes) {
		firePropertyChange("columnsToDataTypes", this.columnsToDataTypes, columnsToDataTypes);
		this.columnsToDataTypes.clear();
		this.columnsToDataTypes.putAll(columnsToDataTypes);
	}

	public void setYaxisName(String yaxisName) {
		firePropertyChange("yaxisName", this.yaxisName, yaxisName);
		this.yaxisName = yaxisName;
	}

	public String getYaxisName() {
		return yaxisName;
	}

	public Map<ColumnIdentifier, ColumnIdentifier> getColumnSeriesToColumnXAxis() {
		return columnSeriesToColumnXAxis;
	}
	
	public void setColumnSeriesToColumnXAxis(Map<ColumnIdentifier, ColumnIdentifier> newMapping) {
		columnSeriesToColumnXAxis.clear();
		columnSeriesToColumnXAxis.putAll(newMapping);
	}

	public void setXaxisName(String xaxisName) {
		this.xaxisName = xaxisName;
	}

	public String getXaxisName() {
		return xaxisName;
	}
	
	public Dataset createDataset() {
	    if (query instanceof QueryCache) {
	        try {
	            switch (chartType) {
	            case BAR:
	            case CATEGORY_LINE:
	                return ChartRenderer.createCategoryDataset(columnNamesInOrder, columnsToDataTypes, ((QueryCache) query).fetchResultSet(), ChartRenderer.findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes));
	            case LINE:
	            case SCATTER:
	                return ChartRenderer.createSeriesCollection(columnSeriesToColumnXAxis, ((QueryCache) query).fetchResultSet());
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
                    return ChartRenderer.createOlapCategoryDataset(columnNamesInOrder, columnsToDataTypes, ((OlapQuery) query).execute(), ChartRenderer.findCategoryColumnNames(columnNamesInOrder, columnsToDataTypes));
                case LINE:
                case SCATTER:
                    return ChartRenderer.createOlapSeriesCollection(columnSeriesToColumnXAxis, ((OlapQuery) query).execute());
                default :
                    throw new IllegalStateException("Unknown chart type " + chartType);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
	    } else {
	        throw new IllegalStateException("Unknown query type " + query.getClass() + " when creating a " + chartType + " chart dataset.");
	    }
	}

	public List<String> getSeriesColours() {
		List<String> colourList = new ArrayList<String>();
		for(WebColour wb : ColourScheme.BREWER_SET19) {
			colourList.add(wb.toString());
		}
		return colourList;
	}

    public List<WabitObject> getDependencies() {
        if (query == null) return Collections.emptyList();
        return Collections.singletonList(query);
    }

    public List<ColumnIdentifier> getMissingIdentifiers() {
        return Collections.unmodifiableList(missingIdentifiers);
    }
    
    public void addMissingIdentifier(ColumnIdentifier identifier) {
        missingIdentifiers.add(identifier);
    }
	
}
