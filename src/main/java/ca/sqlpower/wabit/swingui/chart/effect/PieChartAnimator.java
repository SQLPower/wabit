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
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DatasetChangeEvent;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;

public class PieChartAnimator extends AbstractChartAnimator {

    private final MultiplePiePlot mpplot;
    
    private final double finalAngle = 90.0;
    private final double initialAngle = -45.0;
    private final Interpolator spinInterpolator;

    private final Interpolator alphaInterpolator;
    
    public PieChartAnimator(
            int frameCount, int frameDelay,
            MultiplePiePlot mpplot, 
            Interpolator spinInterpolator,
            Interpolator alphaInterpolator) {
        super(frameCount, frameDelay);
        this.mpplot = mpplot;
        this.spinInterpolator = spinInterpolator;
        this.alphaInterpolator = alphaInterpolator;
    }

    @Override
    protected void doFrame(int frame, double pct) {
        JFreeChart pieChart = mpplot.getPieChart();
        PiePlot plot = (PiePlot) pieChart.getPlot();

        plot.setStartAngle(spinInterpolator.value(initialAngle, finalAngle, pct));
        plot.setForegroundAlpha((float) alphaInterpolator.value(0.0, 1.0, pct));
        
        // need to trigger a repaint, because the pie plot is just a stamper
        mpplot.datasetChanged(new DatasetChangeEvent(pieChart, mpplot.getDataset()));
    }
    
}
