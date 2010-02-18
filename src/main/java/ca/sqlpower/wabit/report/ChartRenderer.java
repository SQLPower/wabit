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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartDataChangedEvent;
import ca.sqlpower.wabit.report.chart.ChartDataListener;
import ca.sqlpower.wabit.report.chart.ChartGradientPainter;
import ca.sqlpower.wabit.swingui.chart.ChartSwingUtil;

/**
 * This class will render a chart from a query's result set in a chart format
 * defined by the user.
 */
public class ChartRenderer extends AbstractWabitObject implements WabitObjectReportRenderer {
		
	private static final Logger logger = Logger.getLogger(ChartRenderer.class);

	private final Chart chart;
	
	private Chart chartCache;
	
	private final ChartDataListener chartListener = new ChartDataListener() {
        public void chartDataChanged(ChartDataChangedEvent evt) {
            getParent().repaint();
        }
    };
    
    private final AbstractSPListener chartStructureListener = new AbstractSPListener() {
    	protected void propertyChangeImpl(java.beans.PropertyChangeEvent evt) {
    		ChartRenderer.this.chartCache = new Chart(ChartRenderer.this.chart);
    	};
	};
    
    public ChartRenderer(@Nonnull ChartRenderer renderer) {
    	this(renderer.chart);
    }
	
	public ChartRenderer(@Nonnull Chart chart) {
		if (chart == null) {
		    throw new NullPointerException("Null chart not permitted");
		}
		/*
		 * Because charts are mutable objects and cannot work in multi-threaded
		 * way, we have to grab a copy of the chart source and update it every time
		 * the source one changes.
		 */
        this.chart = chart;
        this.chartCache = new Chart(chart, this);
        chart.addChartDataListener(chartListener);
        chart.addSPListener(chartStructureListener);
		setName("Renderer of: " + chart.getName());
	}
    
	public Color getBackgroundColour() {
		return chart.getBackgroundColour();
	}
	
	public @Nonnull Chart getContent(){
		return chart;
	}

    // TODO we intend to remove this whole method into the SwingUI layer (SwingContentRenderer)
	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing, SPVariableResolver variablesContext) {
	    
		JFreeChart jFreeChart = null;
		try {
		    jFreeChart = ChartSwingUtil.createChartFromQuery(chartCache);
			if (jFreeChart == null) {
			    g.drawString("Empty Chart", 0, g.getFontMetrics().getHeight());
			    return false;
			}

			Rectangle2D area = new Rectangle2D.Double(
			        0, 0, contentBox.getWidth(), contentBox.getHeight());

			// first pass establishes rendering info but draws nothing
			ChartRenderingInfo info = new ChartRenderingInfo();
			Graphics2D dummyGraphics = (Graphics2D) g.create(0, 0, 0, 0);
            jFreeChart.draw(dummyGraphics, area, info);
			dummyGraphics.dispose();
			
			// now for real
			Rectangle2D plotArea = info.getPlotInfo().getDataArea();
            ChartGradientPainter.paintChartGradient(g, area, (int) plotArea.getMaxY());
			jFreeChart.draw(g, area);
            
		} catch (Exception e) {
		    logger.error("Error while rendering chart", e);
		    g.drawString("Could not render chart: " + e.getMessage(), 0, g.getFontMetrics().getHeight());
		}
		return false;
	}

	// XXX this should be commonly available to all content renderers
    private void renderError(Graphics2D g, ContentBox contentBox, String ... lines) {
        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight();
        int y = (int) ( (contentBox.getHeight() / 2) - (lines.length * fontHeight / 2) );
        for (String line : lines) {
            int x = (int) ((contentBox.getWidth() - fm.stringWidth(line)) / 2);
            if (x < 0) {
                x = 0;
            }
            g.drawString(line, x, y);
            y += fm.getHeight();
        }
    }

	public void resetToFirstPage() {
		//do nothing.
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
	    return Collections.emptyList();
	}

    public List<WabitObject> getDependencies() {
        return Collections.singletonList((WabitObject) chart);
    }
    
    public void removeDependency(SPObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }

    @Override
    public CleanupExceptions cleanup() {
        chart.removeChartDataListener(chartListener);
        chart.removeSPListener(chartStructureListener);
        return new CleanupExceptions();
    }

	public void refresh() {
		chart.removeSPListener(chartStructureListener);
		chartCache.cleanup();
		chart.refresh();
		ChartRenderer.this.chartCache = new Chart(ChartRenderer.this.chart);
	}

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    @Override
    public ContentBox getParent() {
        return (ContentBox) super.getParent();
	}
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }
}
