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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DatasetChangeEvent;

import ca.sqlpower.wabit.swingui.chart.effect.interp.Interpolator;

public class PieChartAnimator implements ChartAnimator {

    private static final Logger logger = Logger.getLogger(PieChartAnimator.class);
    
    private final ActionListener frameHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JFreeChart pieChart = mpplot.getPieChart();
            PiePlot plot = (PiePlot) pieChart.getPlot();
            
            final double pct = ((double) frame) / ((double) frameCount);
            plot.setStartAngle(spinInterpolator.value(initialAngle, finalAngle, pct));
            plot.setForegroundAlpha((float) alphaInterpolator.value(0.0, 1.0, pct));
            // need to trigger a repaint, because the pie plot is just a stamper
            mpplot.datasetChanged(new DatasetChangeEvent(pieChart, mpplot.getDataset()));
            
            if (frame >= frameCount) {
                stopAnimation();
            }
            
            frame++;
        }
    };
    
    private final Timer timer;

    private final MultiplePiePlot mpplot;
    
    private final double finalAngle = 90.0;
    private final double initialAngle = -45.0;
    private final Interpolator spinInterpolator;

    private final int frameCount;
    private int frame;

    private final Interpolator alphaInterpolator;
    
    public PieChartAnimator(
            MultiplePiePlot mpplot, int frameDelay,
            int frameCount, Interpolator spinInterpolator,
            Interpolator alphaInterpolator) {
        this.mpplot = mpplot;
        this.frameCount = frameCount;
        this.spinInterpolator = spinInterpolator;
        this.alphaInterpolator = alphaInterpolator;
        timer = new Timer(frameDelay, frameHandler);
    }

    public void startAnimation() {
        frame = 0;
        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

}
