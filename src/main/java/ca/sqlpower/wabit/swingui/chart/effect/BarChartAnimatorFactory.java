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

package ca.sqlpower.wabit.swingui.chart.effect;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChartAnimatorFactory implements ChartAnimatorFactory {

    public boolean canAnimate(JFreeChart chart) {
        if (!(chart.getPlot() instanceof CategoryPlot)) {
            return false;
        }
        CategoryPlot cplot = chart.getCategoryPlot();
        if (cplot.getDatasetCount() != 1) {
            return false;
        }
        if (!(cplot.getDataset() instanceof DefaultCategoryDataset)) {
            return false;
        }
        return true;
    }

    /**
     * Number of milliseconds between frames.
     */
    private int frameDelay = 20;
    
    /**
     * Number of frames the chart animation should play for.
     */
    private int frameCount = 30;

    public ChartAnimator createAnimator(JFreeChart chart) throws CantAnimateException {
        DefaultCategoryDataset dataset;
        if (chart.getCategoryPlot().getDataset() instanceof DefaultCategoryDataset) {
            dataset = (DefaultCategoryDataset) chart.getCategoryPlot().getDataset();
        } else {
            throw new CantAnimateException("Unsupported dataset type " + chart.getCategoryPlot().getDataset());
        }
        
        // TODO handle multiple Y axes
        chart.getCategoryPlot().getRangeAxis().setAutoRange(false);
        Range dataRange = chart.getCategoryPlot().getDataRange(chart.getCategoryPlot().getRangeAxis());
        double stepSize = dataRange.getUpperBound() / ((double) frameCount);
        
        return new BarChartAnimator(dataset, frameDelay, stepSize);
    }

}
