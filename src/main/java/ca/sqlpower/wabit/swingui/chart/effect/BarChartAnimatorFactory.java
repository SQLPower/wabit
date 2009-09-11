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
import org.jfree.data.category.DefaultCategoryDataset;

import ca.sqlpower.wabit.swingui.chart.effect.interp.BallDropInterpolator;
import ca.sqlpower.wabit.swingui.chart.effect.interp.EaseOutInterpolator;
import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;

public class BarChartAnimatorFactory extends AbstractChartAnimatorFactory {

    private final Interpolator interpolator = new BallDropInterpolator();//new LinearInterpolator();
    
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

    public ChartAnimator createAnimator(JFreeChart chart) throws CantAnimateException {
        DefaultCategoryDataset dataset;
        if (chart.getCategoryPlot().getDataset() instanceof DefaultCategoryDataset) {
            dataset = (DefaultCategoryDataset) chart.getCategoryPlot().getDataset();
        } else {
            throw new CantAnimateException("Unsupported dataset type " +
                    chart.getCategoryPlot().getDataset());
        }

        chart.getCategoryPlot().getRangeAxis().setAutoRange(false);

        return new BarChartAnimator(dataset, getFrameDelay(), getFrameCount(), interpolator);
    }

}
