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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * This tests that the dimension returned for the column headers could at least contain the
     * headers. If this test fails then the headers of a result set are being cut off.
     */
    public void testRenderColumnHeadersDimension() {
        Font font = graphics.getFont();
        ReportPositionRenderer defaultRenderer = new ReportPositionRenderer(font, font, BorderStyles.NONE, 1000, "");
        
        List<ColumnInfo> colInfo = new ArrayList<ColumnInfo>();
        List<BigDecimal> subtotals = new ArrayList<BigDecimal>();
        int minWidth = 0;
        
        ColumnInfo newColInfo = new ColumnInfo("Col 1");
        newColInfo.setWidth(75);
        minWidth += 75;
        colInfo.add(newColInfo);
        subtotals.add(null);
        newColInfo = new ColumnInfo("Col 2");
        newColInfo.setWidth(100);
        minWidth += 100;
        colInfo.add(newColInfo);
        subtotals.add(null);
        newColInfo = new ColumnInfo("Col 3");
        newColInfo.setWidth(125);
        minWidth += 125;
        colInfo.add(newColInfo);
        subtotals.add(null);
        Section section = new Section(0, 0, subtotals, new ArrayList<Object>());
        
        Dimension renderColumnHeader = defaultRenderer.renderColumnHeader((Graphics2D) graphics, colInfo, section);
        
        assertTrue(renderColumnHeader.getWidth() > minWidth);
        assertTrue(renderColumnHeader.getHeight() > graphics.getFontMetrics(font).getHeight());
    }
    
    /**
     * This tests that a column defined to be a break is not included in the headers.
     * The column that is defined as a break is extremely wide to prevent any added
     * spacing from making the table wide enough that i could encompass the column
     * defined as a break.
     */
    public void testRenderColumnHeadersSkipsBreakColumns() {
        Font font = graphics.getFont();
        ReportPositionRenderer defaultRenderer = new ReportPositionRenderer(font, font, BorderStyles.NONE, 1000, "");
        
        List<ColumnInfo> colInfo = new ArrayList<ColumnInfo>();
        List<BigDecimal> subtotals = new ArrayList<BigDecimal>();
        int minWidth = 0;
        
        //This value should be kept very large or else additional 
        //padding in the column may make the test fail.
        int breakColWidth = 500;
        ColumnInfo newColInfo = new ColumnInfo("Col 1");
        newColInfo.setWidth(breakColWidth);
        newColInfo.setWillBreak(true);
        colInfo.add(newColInfo);
        subtotals.add(null);
        newColInfo = new ColumnInfo("Col 2");
        newColInfo.setWidth(100);
        minWidth += 100;
        colInfo.add(newColInfo);
        subtotals.add(null);
        newColInfo = new ColumnInfo("Col 3");
        newColInfo.setWidth(125);
        minWidth += 125;
        colInfo.add(newColInfo);
        subtotals.add(null);
        Section section = new Section(0, 0, subtotals, new ArrayList<Object>());
        
        Dimension renderColumnHeader = defaultRenderer.renderColumnHeader((Graphics2D) graphics, colInfo, section);
        
        assertTrue(renderColumnHeader.getWidth() > minWidth);
        assertTrue(renderColumnHeader.getWidth() < minWidth + breakColWidth);
    }
    
}
