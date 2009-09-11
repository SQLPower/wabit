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
import java.util.List;

import javax.swing.Timer;

import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChartAnimator implements ChartAnimator {

    private final ActionListener timerHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            boolean changedSomething = false;
            for (Comparable rowKey : (List<Comparable>) animatingData.getRowKeys()) {
                for (Comparable colKey : (List<Comparable>) animatingData.getColumnKeys()) {
                    Number orig = originalData.getValue(rowKey, colKey);
                    Number anim = animatingData.getValue(rowKey, colKey);
                    
                    if (anim.doubleValue() < orig.doubleValue()) {
                        // TODO proper interpolator
                        animatingData.setValue(
                                Math.min(anim.doubleValue() + stepSize, orig.doubleValue()),
                                rowKey, colKey);
                        changedSomething = true;
                    }
                }
            }
            if (!changedSomething || stepSize <= 0) {
                stopAnimation();
            }
        }
    };

    private final DefaultCategoryDataset animatingData;
    private final DefaultCategoryDataset originalData;
    private final Timer timer;

    /**
     * Actual number to add to the data value in each frame. This is calculated
     * based on {@link #stepPercent} and the actual data values in the chart.
     */
    private final double stepSize;

    public BarChartAnimator(DefaultCategoryDataset dataset, int frameDelay, double stepSize) {
        this.stepSize = stepSize;
        animatingData = dataset;
        try {
            originalData = (DefaultCategoryDataset) animatingData.clone();
        } catch (CloneNotSupportedException impossible) {
            throw new RuntimeException("Can't clone DefaultCategoryDataset!?", impossible);
        }
        timer = new Timer(frameDelay, timerHandler);
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.wabit.swingui.chart.effect.ChartAnimator#startAnimation()
     */
    public void startAnimation() {
        
        for (Comparable rowKey : (List<Comparable>) animatingData.getRowKeys()) {
            for (Comparable colKey : (List<Comparable>) animatingData.getColumnKeys()) {
                animatingData.setValue(0.0, rowKey, colKey);
            }
        }
        
        // although it is not documented, inspection of the library code shows
        // this method has no effect if the timer is already running
        timer.start();
    }

    /* (non-Javadoc)
     * @see ca.sqlpower.wabit.swingui.chart.effect.ChartAnimator#stopAnimation()
     */
    public void stopAnimation() {
        timer.stop();
    }
    
}
