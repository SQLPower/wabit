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

import org.jfree.ui.RectangleEdge;

/**
 * The possible positions a legend can occupy on a chart
 */
public enum LegendPosition {
    NONE(null),
    TOP(RectangleEdge.TOP),
    LEFT(RectangleEdge.LEFT),
    RIGHT(RectangleEdge.RIGHT),
    BOTTOM(RectangleEdge.BOTTOM);
    
    /**
     * The edge that this legend position represents
     */
    private final RectangleEdge rectangleEdge;
    
    private LegendPosition(RectangleEdge representationEdge) {
        rectangleEdge = representationEdge;
    }
    
    public RectangleEdge getRectangleEdge() {
        return rectangleEdge;
    }
}
