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
import java.util.List;

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
	 * This is the list of supported datasets in the chart at current.
	 */
	public enum DatasetTypes {
	    CATEGORY,
	    XY
	}
	
	/**
	 * The types of charts this renderer can create.
	 */
	public enum ExistingChartTypes {
		BAR(DatasetTypes.CATEGORY),
		CATEGORY_LINE(DatasetTypes.CATEGORY),
		LINE(DatasetTypes.XY),
		SCATTER(DatasetTypes.XY);
		
		private final DatasetTypes type;

        private ExistingChartTypes(DatasetTypes type) {
            this.type = type;
		}

        public DatasetTypes getType() {
            return type;
        }
	}
	
	/**
	 * The possible positions a legend can occupy on a chart
	 */
	public enum LegendPosition {
		NONE(null),
		TOP(RectangleEdge.TOP),
		LEFT(RectangleEdge.LEFT),
		RIGHT(RectangleEdge.RIGHT),
		BOTTOM(RectangleEdge.BOTTOM);
		
		/**
		 * The edge that this legend position represents
		 */
		private final RectangleEdge rectangleEdge;
		
		private LegendPosition(RectangleEdge representationEdge) {
		    rectangleEdge = representationEdge;
		}
		
		public RectangleEdge getRectangleEdge() {
		    return rectangleEdge;
		}
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
	 * This change listener watches for changes to the streaming query and refreshes the
	 * chart when a change occurs.
	 */
	private final RowSetChangeListener queryListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			firePropertyChange("resultSetRowAdded", null, e.getRow());
		}
	};

    /**
     * This is a listener placed on OLAP queries to find if columns removed from
     * a query were in use in this chart.
     */
	private final OlapQueryListener olapQueryChangeListener = new OlapQueryListener() {

	    public void queryExecuted(OlapQueryEvent e) {
            updateMissingIdentifierList(e.getCellSet());
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
		    Object data;
			if (query != null) {
			    if (query instanceof QueryCache) {
			        data = ((QueryCache) query).fetchResultSet();
			    } else if (query instanceof OlapQuery) {
			        final OlapQuery olapQuery = (OlapQuery) query;
                    logger.debug("The olap query being charted is " + olapQuery.getName() +
                            " and the query text is " + olapQuery.getMdxText());
			        data = olapQuery.execute();
			    } else {
			        throw new IllegalStateException("Unknown query type " + query.getClass() + 
			                " when trying to create a chart.");
			    }
			    chart = createChartFromQuery(columnNamesInOrder, data, chartType, getLegendPosition(), 
			            getName(), yaxisName, xaxisName);
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
     * Creates a JFreeChart based on the given query results and how the columns
     * are defined in the chart. The query given MUST be a query type that is
     * allowed in the chart, either a {@link QueryCache} or an {@link OlapQuery}
     * . Anything else will throw an exception.
     * 
     * @param columnNamesInOrder
     *            The order the columns should come in in the chart. The first
     *            column defined as a series will be the first bar or line, the
     *            second defined as a series will be the second bar or line in
     *            the chart and so on. The order of the category columns is also
     *            enforced here and will decide the order the category names are
     *            concatenated in.
     * @param columnsToDataTypes
     *            Defines which columns are series and which ones are
     *            categories.
     * @param data
     *            This must be either a {@link ResultSet} or a {@link CellSet}
     *            that contains the data that will be displayed in the chart.
     *            TODO If we change the cell set to be a cached row set this
     *            just becomes a result set. 
     * @param chartType
     *            The type of chart to create from the data.
     * @param legendPosition
     *            The position where the legend will appear or NONE if it will
     *            not be displayed.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y axis.
     * @param xaxisName
     *            The name of the x axis.
     * @return A chart based on the data in the query of the given type.
     */
    public static JFreeChart createChartFromQuery(List<ColumnIdentifier> columnNamesInOrder, 
            Object data, ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        JFreeChart chart = null;
        if (data == null) return chart;
        
        if (chartType.getType().equals(DatasetTypes.CATEGORY)) {
            chart = createCategoryChartFromQuery(columnNamesInOrder, data, chartType, 
                    legendPosition, chartName, yaxisName, xaxisName);
        } else if (chartType.getType().equals(DatasetTypes.XY)) {
            chart = createXYChartFromQuery(columnNamesInOrder, data, chartType, 
                    legendPosition, chartName, yaxisName, xaxisName);
        } else {
            throw new IllegalStateException("Unknown chart dataset type " +
                    chartType.getType());
        }
        return chart;
    }
	
    /**
     * Creates a JFreeChart based on the given query results and how the columns
     * are defined in the chart. The query given MUST be a query type that is
     * allowed in the chart, either a {@link QueryCache} or an {@link OlapQuery}
     * . Anything else will throw an exception.
     * 
     * @param columnNamesInOrder
     *            The order the columns should come in in the chart. The first
     *            column defined as a series will be the first bar or line, the
     *            second defined as a series will be the second bar or line in
     *            the chart and so on. The order of the category columns is also
     *            enforced here and will decide the order the category names are
     *            concatenated in.
     * @param columnsToDataTypes
     *            Defines which columns are series and which ones are
     *            categories.
     * @param data
     *            This must be either a {@link ResultSet} or a {@link CellSet}
     *            that contains the data that will be displayed in the chart.
     *            TODO If we change the cell set to be a cached row set this
     *            just becomes a result set. 
     * @param chartType
     *            The type of chart to create from the data. This can be a bar
     *            chart, line chart, or anything else that takes a category
     *            dataset.
     * @param legendPosition
     *            The position where the legend will appear or NONE if it will
     *            not be displayed.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y axis.
     * @param xaxisName
     *            The name of the x axis.
     * @return A chart based on the data in the query of the given type.
     */
    private static JFreeChart createCategoryChartFromQuery(List<ColumnIdentifier> columnNamesInOrder, 
            Object data, ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        
        boolean containsCategory = false;
        boolean containsSeries = false;
        for (ColumnIdentifier col : columnNamesInOrder) {
            if (col.getDataType().equals(DataTypeSeries.CATEGORY)) {
                containsCategory = true;
            } else if (col.getDataType().equals(DataTypeSeries.SERIES)) {
                containsSeries = true;
            }
        }
        if (!containsCategory || !containsSeries) {
            return null;
        }
        
        List<ColumnIdentifier> categoryColumns = 
            findCategoryColumnNames(columnNamesInOrder);
        List<String> categoryColumnNames = new ArrayList<String>();
        for (ColumnIdentifier identifier : categoryColumns) {
            categoryColumnNames.add(identifier.getName());
        }
        CategoryDataset dataset;
        
        if (data instanceof CellSet) {
            CellSet cellSet = (CellSet) data;
            dataset = createOlapCategoryDataset(columnNamesInOrder,
                    cellSet, categoryColumns);
        } else if (data instanceof ResultSet) {
            ResultSet rs = (ResultSet) data;
            dataset = createCategoryDataset(columnNamesInOrder,
                    rs, categoryColumns);
        } else {
            throw new IllegalStateException("Unknown result set type " 
                    + data.getClass() + " when trying to create a chart.");
        }
        return createCategoryChartFromDataset(dataset, chartType, 
                legendPosition, chartName, yaxisName, xaxisName);
    }

    /**
     * This is a helper method for creating charts and is split off as it does
     * only the chart creation and tweaking of a category chart. All of the
     * decisions for what columns are defined as what and how the data is stored
     * should be done in the method that calls this.
     * <p>
     * Given a dataset and other chart properties this will create an
     * appropriate JFreeChart.
     * 
     * @param dataset
     *            The data to create a chart from.
     * @param chartType
     *            The type of chart to create for the category dataset.
     * @param legendPosition
     *            The position where the legend should appear.
     * @param chartName
     *            The title of the chart.
     * @param yaxisName
     *            The title of the Y axis.
     * @param xaxisName
     *            The title of the X axis.
     * @return A JFreeChart that represents the dataset given.
     */
    private static JFreeChart createCategoryChartFromDataset( 
            CategoryDataset dataset, ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        
        if (chartType == null || dataset == null) {
            return null;
        }
        boolean showLegend = !legendPosition.equals(LegendPosition.NONE);
        
        JFreeChart chart;
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        
        if (chartType == ExistingChartTypes.BAR) {
            chart = ChartFactory.createBarChart(chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
        } else if (chartType == ExistingChartTypes.CATEGORY_LINE) {
            chart = ChartFactory.createLineChart(chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL, showLegend, true, false);
        } else {
            throw new IllegalArgumentException("Unknown chart type " + chartType + " for a category dataset.");
        }
        if (chart == null) return null;
        
        if (legendPosition != LegendPosition.NONE) {
            chart.getLegend().setPosition(legendPosition.getRectangleEdge());
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
    }
    
    /**
     * Creates a chart based on the data in the given query.
     * @param columnNamesInOrder The order the column
     * @param columnNamesInOrder
     *            The order the columns should come in in the chart. The first
     *            column defined as a series will be the first bar or line, the
     *            second defined as a series will be the second bar or line in
     *            the chart and so on. The order of the category columns is also
     *            enforced here and will decide the order the category names are
     *            concatenated in.
     * @param columnsToDataTypes
     *            Defines which columns are series and which ones are
     *            categories.
     * @param data
     *            Either a {@link ResultSet} or a {@link CellSet} from a query
     *            that will contain the information to chart.
     *            TODO If we place the cell set into a cached row set this value
     *            can become a result set.
     * @param chartType
     *            The type of chart to create from the data. This can be a bar
     *            chart, line chart, or anything else that takes a category
     *            dataset.
     * @param legendPosition
     *            The position where the legend will appear or NONE if it will
     *            not be displayed.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y axis.
     * @param xaxisName
     *            The name of the x axis.
     * @return A chart based on the data in the query of the given type.
     */
    private static JFreeChart createXYChartFromQuery(List<ColumnIdentifier> columnNamesInOrder, 
            Object data, ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        if (chartType == null) {
            return null;
        }
        
        XYDataset xyCollection;
        
        boolean containsSeries = false;
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (identifier.getDataType().equals(DataTypeSeries.SERIES)) {
                containsSeries = true;
                break;
            }
        }
        if (!containsSeries) {
            return null;
        }
        
        if (data instanceof CellSet) {
            CellSet cellSet = (CellSet) data;
            xyCollection = createOlapSeriesCollection(columnNamesInOrder, cellSet);
        } else if (data instanceof ResultSet) {
            ResultSet resultSet = (ResultSet) data;
            xyCollection = createSeriesCollection(columnNamesInOrder, resultSet);
        } else {
            throw new IllegalStateException("Unknown query type " + data.getClass() +
            " when trying to create a chart.");
        }
        if (xyCollection == null) {
            return null;
        }
        return createChartFromXYDataset(xyCollection, chartType, legendPosition, 
                chartName, yaxisName, xaxisName);
    }

    /**
     * This is a helper method for creating a line chart. This should only do
     * the chart creation and not setting up the dataset. The calling method
     * should do the logic for the dataset setup.
     * 
     * @param xyCollection
     *            The dataset to display a chart for.
     * @param chartType
     *            The chart type. This must be a valid chart type that can be
     *            created from an XY dataset. At current only line and scatter
     *            are supported
     * @param legendPosition
     *            The position of the legend.
     * @param chartName
     *            The name of the chart.
     * @param yaxisName
     *            The name of the y axis.
     * @param xaxisName
     *            The name of the x axis.
     * @return A chart of the specified chartType based on the given dataset.
     */
    private static JFreeChart createChartFromXYDataset(XYDataset xyCollection,
            ExistingChartTypes chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        boolean showLegend = !legendPosition.equals(LegendPosition.NONE);
        JFreeChart chart;
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        if (chartType.equals(ExistingChartTypes.LINE)) {
            chart = ChartFactory.createXYLineChart(chartName, xaxisName, yaxisName, xyCollection, 
                    PlotOrientation.VERTICAL, showLegend, true, false);
        } else if (chartType.equals(ExistingChartTypes.SCATTER)) {
            chart = ChartFactory.createScatterPlot(chartName, xaxisName, yaxisName, xyCollection, 
                    PlotOrientation.VERTICAL, showLegend, true, false);
        } else {
            throw new IllegalArgumentException("Unknown chart type " + chartType + " for an XY dataset.");
        }
        if (chart == null) return null;
        
        if (legendPosition != LegendPosition.NONE) {
            chart.getLegend().setPosition(legendPosition.getRectangleEdge());
            chart.getTitle().setPadding(4,4,15,4);
        }
        final XYItemRenderer xyirenderer = chart.getXYPlot().getRenderer();
        int xyLineSeriesSize = chart.getXYPlot().getDataset().getSeriesCount();
        for (int i = 0; i < xyLineSeriesSize; i++) {
            //XXX:AS LONG AS THERE ARE ONLY 10 SERIES!!!!
            xyirenderer.setSeriesPaint(i, ColourScheme.BREWER_SET19.get(i));
            if (chartType.equals(ExistingChartTypes.SCATTER)) {
                BasicStroke circle = new BasicStroke();
                xyirenderer.setSeriesShape(i, circle.createStrokedShape(
                        new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0)));
            }
        }
        setTransparentChartBackground(chart);
        return chart;
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
	        List<ColumnIdentifier> columnNamesInOrder) {
		List<ColumnIdentifier> categoryColumnNames = new ArrayList<ColumnIdentifier>();
		for (ColumnIdentifier identifier : columnNamesInOrder) {
		    if (identifier.getDataType().equals(DataTypeSeries.CATEGORY)) {
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
            if (!identifier.getDataType().equals(DataTypeSeries.SERIES)) continue;
            
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
        	if (identifier.getDataType().equals(DataTypeSeries.SERIES)) {
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
			List<ColumnIdentifier> columnNamesInOrder, ResultSet resultSet) {
		boolean allNumeric = true;
		boolean allDate = true;
		try {
			for (ColumnIdentifier identifier : columnNamesInOrder) {
			    final ColumnNameColumnIdentifier xAxisIdentifier = 
			        (ColumnNameColumnIdentifier) identifier.getXAxisIdentifier();
                if (!identifier.getDataType().equals(DataTypeSeries.SERIES)
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
			    if (!identifier.getDataType().equals(DataTypeSeries.SERIES)
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
                if (!identifier.getDataType().equals(DataTypeSeries.SERIES)
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
	
	/**
     * Helper method for creating line and scatter charts in the
     * createJFreeChart method. This is for olap queries only.
     * @return An XYDataset for use in a JFreeChart or null if an 
     * XYDataset cannot be created.
     */
    private static XYDataset createOlapSeriesCollection(
            List<ColumnIdentifier> columnNamesInOrder, CellSet cellSet) {
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        for (ColumnIdentifier identifier : columnNamesInOrder) {
            if (!(identifier instanceof PositionColumnIdentifier)) continue;
            PositionColumnIdentifier seriesColIdentifier 
                = ((PositionColumnIdentifier) identifier);
            PositionColumnIdentifier xAxisColIdentifier 
                = ((PositionColumnIdentifier) identifier.getXAxisIdentifier());
            if (!identifier.getDataType().equals(DataTypeSeries.SERIES)
                    || xAxisColIdentifier == null) continue;
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
	
	//XXX Get rid of this method and make the column identifiers the children of a chart.
	public void setColumnNamesInOrder(List<ColumnIdentifier> newColumnOrdering) {
	    List<ColumnIdentifier> oldIdentifiers = new ArrayList<ColumnIdentifier>(columnNamesInOrder);
		columnNamesInOrder.clear();
		columnNamesInOrder.addAll(newColumnOrdering);
		firePropertyChange("columnNamesInOrder", oldIdentifiers, newColumnOrdering);
	}
	
	public void addColumnIdentifier(ColumnIdentifier newColumnIdentifier) {
	    //XXX now that the column information is folded into the ColumnIdentifier class
	    //this list would be better to be the children of the chart renderer and use
	    //child added and removed events.
	    List<ColumnIdentifier> oldIdentifiers = new ArrayList<ColumnIdentifier>(columnNamesInOrder);
	    columnNamesInOrder.add(newColumnIdentifier);
	    firePropertyChange("columnNamesInOrder", oldIdentifiers, columnNamesInOrder);
	}

	public void setYaxisName(String yaxisName) {
		firePropertyChange("yaxisName", this.yaxisName, yaxisName);
		this.yaxisName = yaxisName;
	}

	public String getYaxisName() {
		return yaxisName;
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
	                return ChartRenderer.createCategoryDataset(columnNamesInOrder, ((QueryCache) query).fetchResultSet(), ChartRenderer.findCategoryColumnNames(columnNamesInOrder));
	            case LINE:
	            case SCATTER:
	                return ChartRenderer.createSeriesCollection(columnNamesInOrder, ((QueryCache) query).fetchResultSet());
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
                    return ChartRenderer.createOlapCategoryDataset(columnNamesInOrder, ((OlapQuery) query).execute(), ChartRenderer.findCategoryColumnNames(columnNamesInOrder));
                case LINE:
                case SCATTER:
                    return ChartRenderer.createOlapSeriesCollection(columnNamesInOrder, ((OlapQuery) query).execute());
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
