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

/**
 * Defines the contract for a class that produces interpolated values using
 * arbitrary interpolation rules.
 * <p>
 * Instances of Interpolator must be immutable.
 */
@Immutable
public interface Interpolator {

    /**
     * Calculates and returns an interpolated value.
     * 
     * @param start
     *            The value that will be returned when pct == 0.0
     * @param end
     *            The value that will be returned when pct == 1.0
     * @param pct
     *            A value between 0.0 and 1.0 that indicates how far
     *            "along the way" the interpolated value should be.
     *            Values outside the range 0.0..1.0 produce undefined results.
     * @return An interpolated value calculated according to the particular
     *         Interpolator in use.
     */
    public double value(double start, double end, double pct);
    
}
