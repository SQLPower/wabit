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

package ca.sqlpower.wabit.report.chart;

import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

/**
 * Event object that carries notifications about a change in chart data which
 * was not necessarily caused by a change in the chart's underlying result set.
 * This is especially useful for the chart to request a repaint while following
 * streaming queries.
 */
@Immutable
public class ChartDataChangedEvent {

    private final Chart source;

    /**
     * @param source
     */
    public ChartDataChangedEvent(@Nonnull Chart source) {
        if (source == null) {
            throw new NullPointerException("Null source not allowed");
        }
        this.source = source;
    }
    
    public @Nonnull Chart getSource() {
        return source;
    }
}
