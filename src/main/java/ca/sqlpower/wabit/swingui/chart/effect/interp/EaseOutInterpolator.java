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

package ca.sqlpower.wabit.swingui.chart.effect.interp;

import org.apache.log4j.Logger;

/**
 * An interpolator that starts with relatively large steps but eases out to the
 * end of its range with gradually smaller steps.
 */
public class EaseOutInterpolator implements Interpolator {

    private static final Logger logger = Logger.getLogger(EaseOutInterpolator.class);
    
    private final double exponent;

    /**
     * Produces an ease out interpolator with a curve exponent of 2.
     */
    public EaseOutInterpolator() {
        this(2);
    }

    /**
     * Produces an ease out interpolator with the given curve exponent. Higher
     * exponent values produce a sharper break from initially fast movement to
     * eventually slow movement.
     */
    public EaseOutInterpolator(double exponent) {
        this.exponent = exponent;
    }
    
    public double value(double start, double end, double pct) {
        double val = start + (end - start) * Math.pow(pct, 1.0/exponent);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(
                    "EaseOut(s=%8.5f e=%8.5f pct=%8.5f) -> %8.5f",
                    start, end, pct, val));
        }
        return val;
    }
    
    
}
