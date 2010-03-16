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

/**
 * Enumeration of possible horizontal alignments.
 * 
 * @see VerticalAlignment
 */
public enum HorizontalAlignment {

    /**
     * Indicates the item should run from the left-hand boundary of its
     * containing box to its natural width. If the item is too wide for the box,
     * it will be clipped at the right-hand boundary of the containing box.
     */
    LEFT,

    /**
     * Indicates the item's centre should be lined up with the centre of the
     * containing box. If the item is wider than its containing box, both its
     * right and left sides will be clipped by the containing box.
     */
    CENTER,

    /**
     * Indicates the item should run to the right-hand boundary of its
     * containing box from the position of the right-hand boundary less its
     * natural width. If the item is too wide for the box, it will still end at
     * the right-hand boundary of the containing box and its left portion will
     * be clipped by the left-hand side of the box.
     */
    RIGHT;

    /**
     * Computes the starting X position within the given box for content that
     * has the given width.
     * 
     * @param containingBoxWidth
     *            The width of the containing box
     * @param itemWidth
     *            The width of the item to align within the containing box
     * @return The x-coordinate for the left-hand side of the item that makes it
     *         align according to this alignment rule. The x-coordinate is
     *         relative to the containing box's left-hand side, so a value of 0
     *         is on the box's left-hand edge, negative values are outside the
     *         box (to its left), positive values less than
     *         containingBoxWidth are inside the box, and positive values larger
     *         than containingBoxWidth are outside the box (to its right).
     */
    public double computeStartX(double containingBoxWidth, double itemWidth) {
    	double x;
        if (this == HorizontalAlignment.LEFT) {
            x = 0;
        } else if (this == HorizontalAlignment.CENTER) {
            x = containingBoxWidth/2 - itemWidth/2;
        } else if (this == HorizontalAlignment.RIGHT) {
            x = containingBoxWidth - itemWidth;
        } else {
            throw new IllegalStateException("Unknown horizontal alignment: " + this);
        }
        return x;
    }

}