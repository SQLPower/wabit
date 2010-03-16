/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

/**
 * 
 */
package ca.sqlpower.wabit.report;

import java.awt.FontMetrics;

/**
 * Enumeration of the possible vertical alignment rules.
 * 
 * @see HorizontalAlignment
 */
public enum VerticalAlignment {

    /**
     * Indicates that the top of the content should be aligned with the top of
     * the containing box.
     */
    TOP,

    /**
     * Indicates that the vertical midpoint of the content should be aligned
     * with the vertical midpoint of the containing box.
     */
    MIDDLE,
    
    /**
     * Indicates that the bottom of the content should be aligned with the bottom of
     * the containing box.
     */
    BOTTOM;

    /**
     * Computes the starting Y position within the given box for content that
     * has the given height.
     * 
     * @param containingBoxHeight
     *            The height of the containing box
     * @param itemHeight
     *            The height of the item to align within the containing box
     * @return The y-coordinate for the top edge of the item that makes it align
     *         according to this alignment rule. The y-coordinate is relative to
     *         the containing box's top edge, so a value of 0 is on the box's
     *         top edge, negative values are outside the box (above it),
     *         positive values less than containingBoxHeight are inside the box,
     *         and positive values larger than containingBoxHeight are outside
     *         the box (above it).
     */
    public double calculateStartY(double containingBoxHeight, double itemHeight) {
        double y;
        if (this == VerticalAlignment.TOP) {
            y = 0;
        } else if (this == VerticalAlignment.MIDDLE) {
            y = containingBoxHeight/2 - itemHeight/2;
        } else if (this == VerticalAlignment.BOTTOM) {
            y = containingBoxHeight - itemHeight;
        } else {
            throw new IllegalStateException("Unknown vertical alignment: " + this);
        }
        return y;
    }

    /**
     * A specialized version of {@link #calculateStartY(int, int)} which returns
     * the correct Y value for the baseline of the first line of text.
     * 
     * @param containingBoxHeight
     *            The height of the containing box
     * @param itemHeight
     *            The height of the item to align within the containing box
     * @param fm
     *            The font metrics for the font the item will be rendered in
     * @return The y-coordinate for the baseline of the first line of text in
     *         the item. See the documentation for
     *         {@link #calculateStartY(int, int)} for a detailed description of
     *         how to interpret the Y value.
     */
    public double calculateStartY(double containingBoxHeight, double itemHeight, FontMetrics fm) {
        return calculateStartY(containingBoxHeight, itemHeight) + fm.getAscent();
    }

}