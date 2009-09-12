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

/**
 * Base class that takes care of all the boilerplate code required for a typical
 * implementation of ChartAnimator.
 */
public abstract class AbstractChartAnimator implements ChartAnimator {

    /**
     * Calls doFrame() and increments frame number. Stops the animation if we've
     * reached the final frame count.
     */
    private final ActionListener timerHandler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (frame > frameCount) {
                stopAnimation();
                return;
            }
            final double pct = ((double) frame) / ((double) frameCount);
            try {
                doFrame(frame, pct);
            } finally {
                frame++;
            }
        }
    };
    
    /**
     * Current frame number. Gets reset to 0 by {@link #startAnimation()}.
     */
    private int frame;
    
    /**
     * Total number of frames in the animation.
     */
    private final int frameCount;

    /**
     * The Swing timer that triggers each frame. Controlled through
     * {@link #startAnimation()} and {@link #stopAnimation()}.
     */
    private final Timer timer;

    /**
     * Sets up the support infrastructure for a chart animator.
     * 
     * @param frameCount
     *            Total number of frames before the animation completes
     * @param frameDelay
     *            Amount of time (milliseconds) between each frame.
     */
    public AbstractChartAnimator(int frameCount, int frameDelay) {
        this.frameCount = frameCount;
        timer = new Timer(frameDelay, timerHandler);
    }

    /**
     * Subclasses define their actual animation behaviour by overriding this
     * method.
     * 
     * @param frame
     *            The current frame number (of {@link #getFrameCount()})
     * @param pct
     *            The percentage (between 0.0 and 1.0 inclusive) of the way that
     *            the current frame is.
     */
    protected abstract void doFrame(int frame, double pct);

    public final void startAnimation() {
        frame = 0;
        setup();
        
        // although it is not documented, inspection of the library code shows
        // this method has no effect if the timer is already running
        timer.start();
    }

    /**
     * Subclasses that wish to perform some setup action whenever the animation
     * starts or restarts may override this method. It is called from
     * {@link #startAnimation()} before the invocation of
     * {@link #doFrame(int, double)} with a frame number of 0.
     */
    protected void setup() {
        // no-op (this is a subclass hook)
    }

    public final void stopAnimation() {
        timer.stop();
    }
    
    public int getFrameCount() {
        return frameCount;
    }
}
