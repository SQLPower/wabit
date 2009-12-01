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

package ca.sqlpower.wabit.swingui.chart;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.plot.DrawingSupplier;

import ca.sqlpower.util.WebColour;

/**
 * Provides Wabit-approved colours and shapes to JFreeChart.
 */
public class WabitDrawingSupplier implements DrawingSupplier {

    /**
     * An array of gradient specifications for bar colours. Each gradient spec
     * is itself an array with two entries: start colour and end colour.
     */
    public static final WebColour[][] SERIES_COLOURS =
    {
        { new WebColour(0x0060B6), new WebColour(0x003399) },
        { new WebColour(0xFFBF00), new WebColour(0xFF7F00) },
        { new WebColour(0x00A33D), new WebColour(0x006B33) },
        { new WebColour(0xBF0000), new WebColour(0x8F0000) },
        { new WebColour(0x885997), new WebColour(0x602169) },
        { new WebColour(0x99CCFF), new WebColour(0x00A0C6) },
        { new WebColour(0xFFFF00), new WebColour(0xFFBF00) },
        { new WebColour(0xD9C6C5), new WebColour(0xB0A790) },
        { new WebColour(0xABD498), new WebColour(0x7FB24A) },
        { new WebColour(0x809EAD), new WebColour(0x466F82) },
        { new WebColour(0x9A7F55), new WebColour(0x574109) },
        { new WebColour(0xC6C8CA), new WebColour(0x7A7C7E) },
        { new WebColour(0xCFDED8), new WebColour(0x8DBAA6) },
        { new WebColour(0xEBC5D0), new WebColour(0xE786B6) }
    };
    
    /**
     * Basic stroke returned every time by {@link #getNextStroke()}.
     */
    private static final Stroke STROKE = new BasicStroke(1f);
    
    /**
     * The shape returned every time by {@link #getNextShape()}.
     */
    private static final Shape DOT = STROKE.createStrokedShape(
            new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
    
    private int nextFillPaintIndex = 0;
    private int nextPaintIndex = 0;
    
    public Paint getNextFillPaint() {
        int i = nextFillPaintIndex % SERIES_COLOURS.length;
        Paint p = makeGradient(i);
        nextFillPaintIndex++;
        return p;
    }

    private Paint makeGradient(int i) {
        Paint p = new GradientPaint(
                0.0f, 0.0f, SERIES_COLOURS[i][0],
                0.0f, 0.0f, SERIES_COLOURS[i][1]);
        return p;
    }

    public Paint getNextOutlinePaint() {
        return null;
    }

    public Stroke getNextOutlineStroke() {
        return null;
    }

    public Paint getNextPaint() {
        int i = nextPaintIndex % SERIES_COLOURS.length;
        Paint p = makeGradient(i);
        nextPaintIndex++;
        return p;
    }

    public Shape getNextShape() {
        return DOT;
    }

    public Stroke getNextStroke() {
        return STROKE;
    }

    
}
