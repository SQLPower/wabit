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

public interface ChartAnimatorFactory {

    /**
     * Returns true if this chart animation factory can animate the given chart.
     * 
     * @param chart
     *            The chart in question
     * @return True if this factory can create an animation for
     *         <code>chart</code>; false otherwise.
     */
    public boolean canAnimate(JFreeChart chart);

    /**
     * Creates a new chart animator for the given chart.
     * 
     * @param chart
     *            The chart to animate. Some aspects of the chart may be
     *            permanently modified as a side effect of this call (for
     *            example, disable autoranging on a bar chart's category axis),
     *            but no Swing timers will have been started.
     * @return a new animator for the given chart. The animator will not be
     *         started.
     * @throws CantAnimateException
     *             if {@link #canAnimate(JFreeChart)} would return false for the
     *             given chart.
     */
    public ChartAnimator createAnimator(JFreeChart chart) throws CantAnimateException;
    
}
