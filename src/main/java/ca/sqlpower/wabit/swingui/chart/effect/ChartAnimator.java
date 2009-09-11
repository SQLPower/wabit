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

public interface ChartAnimator {

    /**
     * Resets all bars to 0 then starts the animation timer. If the animation is
     * already running, this method causes it to restart from the beginning.
     */
    public void startAnimation();

    /**
     * Stops the animation. The animation does stop on its own eventually, but
     * this method can be invoked to stop the animation early. If the animation
     * is already stopped, this method has no effect.
     */
    public void stopAnimation();

}
