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
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;
import ca.sqlpower.wabit.swingui.chart.effect.interp.PolynomialInterpolator;

public class ScatterChartAnimatorFactory extends AbstractChartAnimatorFactory {

    private Interpolator interpolator = PolynomialInterpolator.easeInInstance();
    
    public boolean canAnimate(JFreeChart chart) {
        Plot plot = chart.getPlot();
        if (!(plot instanceof XYPlot)) {
            return false;
        }
        return true;
    }

    public ChartAnimator createAnimator(JFreeChart chart) throws CantAnimateException {
        XYPlot xyplot;
        if (chart.getPlot() instanceof XYPlot) {
            xyplot = (XYPlot) chart.getPlot();
        } else {
            throw new CantAnimateException(
                    "This animator needs an XYPlot. You gave " + chart.getPlot());
        }
        return new ScatterChartAnimator(getFrameCount(), getFrameDelay(), xyplot, interpolator);
    }

}
