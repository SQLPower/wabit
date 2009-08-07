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

package ca.sqlpower.wabit.report.resultset;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import junit.framework.TestCase;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;

public class ReportPositionRendererTest extends TestCase {

    private Graphics graphics;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        graphics = image.getGraphics();
    }
    
    @Override
    protected void tearDown() throws Exception {
        graphics.dispose();
        super.tearDown();
    }
    
    /**
     * Simple test to confirm the padding of cells increases when the borders move from NONE
     * to HORIZONTAL. This test should prevent the text of cells from being placed on border lines.
     */
    public void testPaddingIncreaseOnHorizontal() throws Exception {
        Font font = graphics.getFont();
        ReportPositionRenderer defaultRenderer = new ReportPositionRenderer(font, font, BorderStyles.NONE, 1000, "");
        ReportPositionRenderer horizontalRenderer = new ReportPositionRenderer(font, font, BorderStyles.HORIZONTAL, 1000, "");
        ColumnInfo ci = new ColumnInfo("Column");
        Insets defaultPadding = defaultRenderer.getPadding(ci);
        Insets horizontalPadding = horizontalRenderer.getPadding(ci);
        
        assertTrue(defaultPadding.top < horizontalPadding.top);
        assertTrue(defaultPadding.bottom < horizontalPadding.bottom);
    }
    
    /**
     * Simple test to confirm the padding of cells increases when the borders move from NONE
     * to VERTICAL. This test should prevent the text of cells from being placed on border lines.
     */
    public void testPaddingIncreaseOnVertical() throws Exception {
        Font font = graphics.getFont();
        ReportPositionRenderer defaultRenderer = new ReportPositionRenderer(font, font, BorderStyles.NONE, 1000, "");
        ReportPositionRenderer horizontalRenderer = new ReportPositionRenderer(font, font, BorderStyles.VERTICAL, 1000, "");
        ColumnInfo ci = new ColumnInfo("Column");
        Insets defaultPadding = defaultRenderer.getPadding(ci);
        Insets horizontalPadding = horizontalRenderer.getPadding(ci);
        
        assertTrue(defaultPadding.left < horizontalPadding.left);
        assertTrue(defaultPadding.right < horizontalPadding.right);
    }
    
    /**
     * Simple test to confirm the padding of cells increases when the borders move from NONE
     * to FULL. This test should prevent the text of cells from being placed on border lines.
     */
    public void testPaddingIncreaseOnFull() throws Exception {
        Font font = graphics.getFont();
        ReportPositionRenderer defaultRenderer = new ReportPositionRenderer(font, font, BorderStyles.NONE, 1000, "");
        ReportPositionRenderer horizontalRenderer = new ReportPositionRenderer(font, font, BorderStyles.FULL, 1000, "");
        ColumnInfo ci = new ColumnInfo("Column");
        Insets defaultPadding = defaultRenderer.getPadding(ci);
        Insets horizontalPadding = horizontalRenderer.getPadding(ci);
        
        assertTrue(defaultPadding.left < horizontalPadding.left);
        assertTrue(defaultPadding.right < horizontalPadding.right);
        assertTrue(defaultPadding.top < horizontalPadding.top);
        assertTrue(defaultPadding.bottom < horizontalPadding.bottom);
    }
    
}
