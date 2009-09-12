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

import org.jfree.chart.plot.XYPlot;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;


public class ScatterChartAnimator extends AbstractChartAnimator {

    private final Interpolator interpolator;
    private final XYPlot xyplot;

    /**
     * @param frameCount
     * @param frameDelay
     * @param xyplot 
     */
    public ScatterChartAnimator(int frameCount, int frameDelay, XYPlot xyplot, Interpolator interpolator) {
        super(frameCount, frameDelay);
        this.xyplot = xyplot;
        this.interpolator = interpolator;
    }

    @Override
    protected void doFrame(int frame, double pct) {
        // TODO I want the dots to start big and become small
        xyplot.setForegroundAlpha((float) interpolator.value(0.0, 1.0, pct));
    }
    
}
