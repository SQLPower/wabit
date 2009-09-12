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

import net.jcip.annotations.Immutable;

import org.apache.log4j.Logger;

/**
 * A general polynomial interpolator that can be used to create "ease in",
 * "ease out", and linear interpolations.
 */
@Immutable
public class PolynomialInterpolator implements Interpolator {

    private static final Logger logger = Logger.getLogger(PolynomialInterpolator.class);
    
    /**
     * Returns an "ease out" instance (exponent 1/2).
     * <p>
     * <i>Ease out</i> interpolators start with relatively large steps but ease
     * out to the end of their range with gradually smaller steps.
     * 
     * @return An "ease out" polynomial interpolator.
     */
    public static PolynomialInterpolator easeOutInstance() {
        return new PolynomialInterpolator(0.5);
    }

    /**
     * Returns an "ease out" instance (exponent 1/degree).
     * <p>
     * <i>Ease out</i> interpolators start with relatively large steps but ease
     * out to the end of their range with gradually smaller steps.
     * 
     * @return An "ease out" polynomial interpolator.
     */
    public static PolynomialInterpolator easeOutInstance(double degree) {
        return new PolynomialInterpolator(1.0/degree);
    }

    /**
     * Returns an "ease in" instance (exponent 2).
     * <p>
     * <i>Ease in</i> interpolators ease in with relatively small steps but
     * approach the end of their range with increasingly larger steps.
     * 
     * @return An "ease in" polynomial interpolator.
     */
    public static PolynomialInterpolator easeInInstance() {
        return new PolynomialInterpolator(2.0);
    }

    /**
     * Returns an "ease in" instance of the given degree.
     * <p>
     * <i>Ease in</i> interpolators ease in with relatively small steps but
     * approach the end of their range with increasingly larger steps.
     * 
     * @return An "ease in" polynomial interpolator.
     */
    public static PolynomialInterpolator easeInInstance(double degree) {
        return new PolynomialInterpolator(degree);
    }

    /**
     * This instance's exponent. Controls the curve used for interpolation.
     */
    private final double exponent;

    /**
     * Creates a new interpolator instance which uses the given exponent.
     * 
     * @param exponent
     *            The degree of the polynomial that controls the step size. A
     *            value between 0 and 1 gives an "ease out" interpolator of
     *            decreasing severity; 1.0 gives a linear interpolator; greater
     *            than 1 gives an "ease in" instance of increasing severity.
     */
    public PolynomialInterpolator(double exponent) {
        this.exponent = exponent;
    }
    
    public double value(double start, double end, double pct) {
        double val = start + (end - start) * Math.pow(pct, exponent);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(
                    "[x^%3.1f] value(s=%8.5f e=%8.5f pct=%8.5f) -> %8.5f",
                    exponent, start, end, pct, val));
        }
        return val;
    }
    
    
}
