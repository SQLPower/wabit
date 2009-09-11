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

package ca.sqlpower.wabit.swingui.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3DGradient;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.TableOrder;

import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.report.chart.DatasetType;
import ca.sqlpower.wabit.report.chart.LegendPosition;

/**
 * This is a collection of swing specific chart utilities. You should not
 * make an instance of this class.
 */
public class ChartSwingUtil {

    private static final Logger logger = Logger.getLogger(ChartSwingUtil.class);
    
    /**
     * This is a collection of swing specific chart utilities. You should not
     * make an instance of this class.
     */
    private ChartSwingUtil() {/* don't */}

    /**
     * The stroke that gets used for horizontal and vertical grid lines in all
     * charts that need them.
     */
    private static final BasicStroke GRIDLINE_STROKE = new BasicStroke(
            .5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            0f, new float[] {.5f, 7f}, 0f);

    /**
     * Creates a JFreeChart based on the current query results produced by the
     * given chart.
     * 
     * @param c
     *            The chart from which to produce a JFreeChart component. Must
     *            not be null.
     * @return A chart based on the data and settings in the given chart, or
     *         null if the given chart is not sufficiently configured (for
     *         example, if its type is not set) or it is currently unable to
     *         produce a result set.
     */
    public static JFreeChart createChartFromQuery(Chart c) throws SQLException, QueryInitializationException, InterruptedException {
        logger.debug("Creating JFreeChart for Wabit chart " + c);
        ChartType chartType = c.getType();
        
        if (c.getResultSet() == null) {
            logger.debug("Returning null (chart's result set was null)");
            return null;
        }
        
        if (chartType == null) {
            logger.debug("Returning null (chart's type is not set)");
            return null;
        }
        
        JFreeChart chart;
        if (chartType.getDatasetType().equals(DatasetType.CATEGORY)) {
            JFreeChart categoryChart = createCategoryChart(c);
            logger.debug("Made a new category chart: " + categoryChart);
            
            if (categoryChart != null) {
                double rotationRads = Math.toRadians(c.getXaxisLabelRotation());
                CategoryLabelPositions clp;
                if (Math.abs(rotationRads) < 0.05) {
                    clp = CategoryLabelPositions.STANDARD;
                } else if (rotationRads < 0) {
                    clp = CategoryLabelPositions.createUpRotationLabelPositions(-rotationRads);
                } else {
                    clp = CategoryLabelPositions.createDownRotationLabelPositions(rotationRads);
                }
                
                if (categoryChart.getPlot() instanceof CategoryPlot) {
                    CategoryAxis domainAxis = categoryChart.getCategoryPlot().getDomainAxis();
                    domainAxis.setCategoryLabelPositions(clp);
                }
            }
            
            chart = categoryChart;
        } else if (chartType.getDatasetType().equals(DatasetType.XY)) {
            JFreeChart xyChart = createXYChart(c);
            logger.debug("Made a new XY chart: " + xyChart);
            chart = xyChart;
        } else {
            throw new IllegalStateException(
                    "Unknown chart dataset type " + chartType.getDatasetType());
        }
        
        makeChartNice(chart);
        
        return chart;
    }

    /**
     * Sets the colours and gradients to be used when painting the given JFreeChart.
     * 
     * @param chart
     * 			The JFreeChart to make nice.
     */
    public static void makeChartNice(JFreeChart chart) {
    	Plot plot = chart.getPlot();
        chart.setBackgroundPaint(null);
        chart.setBorderStroke(new BasicStroke(1f));
        chart.setBorderPaint(new Color(0xDDDDDD));
        chart.setBorderVisible(true);
        
        // overall plot
        plot.setOutlinePaint(null);
        plot.setInsets(new RectangleInsets(20, 20, 20, 20)); // also the overall chart panel
        plot.setBackgroundPaint(null);
        plot.setDrawingSupplier(new WabitDrawingSupplier());
        
        // legend
        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setBorder(0, 0, 0, 0);
            legend.setBackgroundPaint(null);
        }

        if (plot instanceof CategoryPlot) {
            CategoryPlot cplot = (CategoryPlot) plot;
            
            CategoryItemRenderer renderer = cplot.getRenderer();
            if (renderer instanceof BarRenderer) {
                BarRenderer brenderer = (BarRenderer) renderer;

                brenderer.setBarPainter(new StandardBarPainter());
                brenderer.setDrawBarOutline(false);
                brenderer.setShadowVisible(false);

                brenderer.setGradientPaintTransformer(
                        new StandardGradientPaintTransformer(
                                GradientPaintTransformType.HORIZONTAL));
                
            } else if (renderer instanceof LineAndShapeRenderer) {
                // it's all taken care of by WabitDrawingSupplier
                
            } else {
                logger.warn("I don't know how to make " + renderer + " pretty. Leaving ugly.");
            }
            
            cplot.setRangeGridlinePaint(Color.BLACK);
            cplot.setRangeGridlineStroke(GRIDLINE_STROKE);
            
            // axes
            for (int i = 0; i < cplot.getDomainAxisCount(); i++) {
                CategoryAxis axis = cplot.getDomainAxis(i);
                axis.setAxisLineVisible(false);
            }
            
            for (int i = 0; i < cplot.getRangeAxisCount(); i++) {
                ValueAxis axis = cplot.getRangeAxis(i);
                axis.setAxisLineVisible(false);
            }

        }
        
        if (plot instanceof MultiplePiePlot){
        	MultiplePiePlot mpplot = (MultiplePiePlot) plot;
        	JFreeChart pchart = mpplot.getPieChart();
        	PiePlot3DGradient pplot = (PiePlot3DGradient) pchart.getPlot();
        	pplot.setBackgroundPaint(null);
        	pplot.setOutlinePaint(null);
        	
        	pplot.setFaceGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL));
        	pplot.setSideGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL));
        	
        	CategoryDataset data = mpplot.getDataset();
        	Color[][] colours = WabitDrawingSupplier.SERIES_COLOURS;
        	
        	//Set all colours
        	for (int i = 0; i < colours.length; i++) {
            	if (data.getColumnCount() >= i+1) {
            		pplot.setSectionOutlinePaint(data.getColumnKey(i), null);
            		GradientPaint gradient = new GradientPaint(
            			0, 0f, colours[i][0],
            			100, 0f, colours[i][1]);
            		pplot.setSectionPaint(data.getColumnKey(i), gradient);
            		gradient = new GradientPaint(
                			0, 0f, colours[i][1],
                			100, 0f, colours[i][0]);
            		pplot.setSidePaint(data.getColumnKey(i), gradient);
            	}
            }
        }        
    }

    /**
     * Creates a JFreeChart based on the given query results and how the columns
     * are defined in the chart.
     * 
     * @param c
     *            The chart from which to extract the data set and configure the
     *            JFreeChart instance
     * @return A chart based on the data in the query of the given type, or null
     *         if any of the following conditions hold:
     *         <ul>
     *          <li>The chart is currently unable to produce a result set
     *          <li>The chart does not have at least one category and one series
     *             column defined
     *         </ul>
     */
    private static JFreeChart createCategoryChart(Chart c) {
        
        if (c.getType().getDatasetType() != DatasetType.CATEGORY) {
            throw new IllegalStateException(
                    "Chart is not currently set up as a category chart " +
                    "(it is a " + c.getType() + ")");
        }
        
        List<ChartColumn> columnNamesInOrder = c.getColumns();
        LegendPosition legendPosition = c.getLegendPosition(); 
        String chartName = c.getName();
        String yaxisName = c.getYaxisName();
        String xaxisName = c.getXaxisName();

        boolean containsCategory = false;
        boolean containsSeries = false;
        for (ChartColumn col : columnNamesInOrder) {
            if (col.getRoleInChart().equals(ColumnRole.CATEGORY)) {
                containsCategory = true;
            } else if (col.getRoleInChart().equals(ColumnRole.SERIES)) {
                containsSeries = true;
            }
        }
        if (!containsCategory || !containsSeries) {
            return null;
        }
        
        List<ChartColumn> categoryColumns = c.findRoleColumns(ColumnRole.CATEGORY);
        List<String> categoryColumnNames = new ArrayList<String>();
        for (ChartColumn identifier : categoryColumns) {
            categoryColumnNames.add(identifier.getName());
        }
        
        // because of the chart type check at the beginning of this method, the
        // following cast should always work
        CategoryDataset dataset = (CategoryDataset) c.createDataset();
        
        return createCategoryChartFromDataset(dataset, c.getType(), 
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
            CategoryDataset dataset, ChartType chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        
        if (chartType == null || dataset == null) {
            return null;
        }
        boolean showLegend = !legendPosition.equals(LegendPosition.NONE);
        
        JFreeChart chart;
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarRenderer.setDefaultBarPainter(new StandardBarPainter());
        
        if (chartType == ChartType.BAR) {
            chart = ChartFactory.createBarChart(
                    chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL,
                    showLegend, true, false);
            
        } else if (chartType == ChartType.PIE) {
            chart = createPieChart(
                    chartName, dataset, TableOrder.BY_ROW,
                    showLegend, true, false);
            
        } else if (chartType == ChartType.CATEGORY_LINE) {
            chart = ChartFactory.createLineChart(
                    chartName, xaxisName, yaxisName, dataset, PlotOrientation.VERTICAL,
                    showLegend, true, false);
            
        } else {
            throw new IllegalArgumentException("Unknown chart type " + chartType + " for a category dataset.");
        }
        if (chart == null) return null;
        
        if (legendPosition != LegendPosition.NONE) {
            chart.getLegend().setPosition(legendPosition.getRectangleEdge());
            chart.getTitle().setPadding(4,4,15,4);
        }
        
        if (chart.getPlot() instanceof MultiplePiePlot) {
            MultiplePiePlot mplot = (MultiplePiePlot) chart.getPlot();
            PiePlot plot = (PiePlot) mplot.getPieChart().getPlot();
            plot.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);
            if (showLegend) {
                // for now, legend and items labels are mutually exclusive. Could make this a user pref.
                plot.setLabelGenerator(null);
            }
        }
        
        return chart;
    }
    
    /**
     * Creates a chart that displays multiple 3D pie plots that have a GradientPaintTransformer.  
     * The chart object returned by this method uses a {@link MultiplePiePlot} instance as the plot.
     *
     * @param title  the chart title (<code>null</code> permitted).
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param order  the order that the data is extracted (by row or by column)
     *               (<code>null</code> not permitted).
     * @param legend  include a legend?
     * @param tooltips  generate tooltips?
     * @param urls  generate URLs?
     *
     * @return A chart.
     */
    private static JFreeChart createPieChart(String title,
    		CategoryDataset dataset,
    		TableOrder order,
    		boolean legend,
    		boolean tooltips,
    		boolean urls) {

    	if (order == null) {
    		throw new IllegalArgumentException("Null 'order' argument.");
    	}
    	MultiplePiePlot plot = new MultiplePiePlot(dataset);
    	plot.setDataExtractOrder(order);
    	plot.setBackgroundPaint(null);
    	plot.setOutlineStroke(null);

    	JFreeChart pieChart = new JFreeChart(new PiePlot3DGradient(null));
    	TextTitle seriesTitle = new TextTitle("Series Title",
    			new Font("SansSerif", Font.BOLD, 12));
    	seriesTitle.setPosition(RectangleEdge.BOTTOM);
    	pieChart.setTitle(seriesTitle);
    	pieChart.removeLegend();
    	pieChart.setBackgroundPaint(null);
    	plot.setPieChart(pieChart);

    	if (tooltips) {
    		PieToolTipGenerator tooltipGenerator
    		= new StandardPieToolTipGenerator();
    		PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
    		pp.setToolTipGenerator(tooltipGenerator);
    	}

    	if (urls) {
    		PieURLGenerator urlGenerator = new StandardPieURLGenerator();
    		PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
    		pp.setURLGenerator(urlGenerator);
    	}

    	JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
    			plot, legend);
    	return chart;

    }

    /**
     * Creates a chart based on the data in the given query.
     * 
     * @param c
     *            The chart to extract the dataset and JFreeChart settings from.
     * @return A chart based on the data in the query of the given type.
     */
    private static JFreeChart createXYChart(Chart c) {
        if (c.getType().getDatasetType() != DatasetType.XY) {
            throw new IllegalStateException(
                    "Chart is not currently set up as an XY chart " +
                    "(it is a " + c.getType() + ")");
        }
        
        List<ChartColumn> columnNamesInOrder = c.getColumns();
        LegendPosition legendPosition = c.getLegendPosition(); 
        String chartName = c.getName();
        String yaxisName = c.getYaxisName();
        String xaxisName = c.getXaxisName();
        
        final XYDataset xyCollection = (XYDataset) c.createDataset();
        
        boolean containsSeries = false;
        for (ChartColumn identifier : columnNamesInOrder) {
            if (identifier.getRoleInChart().equals(ColumnRole.SERIES)) {
                containsSeries = true;
                break;
            }
        }
        if (!containsSeries) {
            return null;
        }
        
        if (xyCollection == null) {
            return null;
        }
        return createChartFromXYDataset(xyCollection, c.getType(), legendPosition, 
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
            ChartType chartType, LegendPosition legendPosition, 
            String chartName, String yaxisName, String xaxisName) {
        boolean showLegend = !legendPosition.equals(LegendPosition.NONE);
        JFreeChart chart;
        if (chartType.equals(ChartType.LINE)) {
            chart = ChartFactory.createXYLineChart(chartName, xaxisName, yaxisName, xyCollection, 
                    PlotOrientation.VERTICAL, showLegend, true, false);
        } else if (chartType.equals(ChartType.SCATTER)) {
            chart = ChartFactory.createScatterPlot(chartName, xaxisName, yaxisName, xyCollection, 
                    PlotOrientation.VERTICAL, showLegend, true, false);
        } else {
            throw new IllegalArgumentException(
                    "Unknown chart type " + chartType + " for an XY dataset.");
        }
        if (chart == null) return null;
        XYPlot plot = (XYPlot) chart.getPlot();
        
        // XXX the following instance check is brittle; there are many ways to represent a time
        // series in JFreeChart. This check uses knowledge of the inner workings of DatasetUtil.
        if (xyCollection instanceof TimePeriodValuesCollection) {
            logger.debug("Switching x-axis to date axis so labels render properly");
            plot.setDomainAxis(new DateAxis(xaxisName));
            // TODO user-settable date format
            // axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
        }
        
        return chart;
    }
    
}
