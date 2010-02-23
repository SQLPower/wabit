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
import org.jfree.chart.plot.MultiplePiePlot;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;
import ca.sqlpower.wabit.swingui.chart.effect.interp.LinearInterpolator;
import ca.sqlpower.wabit.swingui.chart.effect.interp.PolynomialInterpolator;

public class PieChartAnimatorFactory extends AbstractChartAnimatorFactory {

    private Interpolator spinInterpolator = PolynomialInterpolator.easeOutInstance(7);
    private Interpolator alphaInterpolator = new LinearInterpolator();
    
    public boolean canAnimate(JFreeChart chart) {
        if (!(chart.getPlot() instanceof MultiplePiePlot)) {
            return false;
        }
        return true;
    }

    public ChartAnimator createAnimator(JFreeChart chart) throws CantAnimateException {
        MultiplePiePlot mpplot;
        if (chart.getPlot() instanceof MultiplePiePlot) {
            mpplot = (MultiplePiePlot) chart.getPlot();
        } else {
            throw new CantAnimateException(
                    "This animator only works with MultiplePiePlot. " +
                    "You gave me " + chart.getPlot());
        }
        
        return new PieChartAnimator(getFrameCount(), getFrameDelay(),
                mpplot, spinInterpolator, alphaInterpolator);
    }

}
