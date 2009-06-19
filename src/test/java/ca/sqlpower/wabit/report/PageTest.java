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

package ca.sqlpower.wabit.report;

import java.awt.print.PageFormat;
import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Page.PageOrientation;

public class PageTest extends AbstractWabitObjectTest {

    private Page page;
    
    private final int LETTER_WIDTH = 72 * 8 + (72 / 2);
    private final int LETTER_HEIGHT = 72 * 11;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        page = new Page("test page", LETTER_WIDTH, LETTER_HEIGHT, PageOrientation.PORTRAIT);
    }
    
    @Override
    public Set<String> getPropertiesToIgnoreForEvents() {
        Set<String> ignore = super.getPropertiesToIgnoreForEvents();
        ignore.add("fontMetrics"); // this just depends on the font
        return ignore;
    }
    @Override
    public WabitObject getObjectUnderTest() {
        return page;
    }
    
    public void testAddContentBoxParenting() throws Exception {
        ContentBox cb = new ContentBox();
        page.addContentBox(cb);
        assertSame(page, cb.getParent());
    }
    
    public void testOrientationPortraitToLandscape() throws Exception {
        page.setOrientation(PageOrientation.LANDSCAPE);
        assertEquals(LETTER_HEIGHT, page.getWidth());
        assertEquals(LETTER_WIDTH, page.getHeight());
    }

    public void testOrientationPortraitToPortrait() throws Exception {
        page.setOrientation(PageOrientation.PORTRAIT);
        assertEquals(LETTER_HEIGHT, page.getHeight());
        assertEquals(LETTER_WIDTH, page.getWidth());
    }

    public void testOrientationPortraitToLandscapeToPortrait() throws Exception {
        page.setOrientation(PageOrientation.LANDSCAPE);
        page.setOrientation(PageOrientation.PORTRAIT);
        assertEquals(LETTER_HEIGHT, page.getHeight());
        assertEquals(LETTER_WIDTH, page.getWidth());
    }

    public void testOrientationPortraitToLandscapeToReverseLandscape() throws Exception {
        page.setOrientation(PageOrientation.LANDSCAPE);
        page.setOrientation(PageOrientation.REVERSE_LANDSCAPE);
        assertEquals(LETTER_HEIGHT, page.getWidth());
        assertEquals(LETTER_WIDTH, page.getHeight());
    }
    
    public void testConstructLandscapePageWithPrintAPI() throws Exception {
        PageFormat format = new PageFormat();
        format.setOrientation(PageFormat.LANDSCAPE);
        Page newPage = new Page("test", format);
        assertEquals(LETTER_WIDTH, newPage.getHeight());
        assertEquals(LETTER_HEIGHT, newPage.getWidth());
    }
    
    public void testConstructLandscapePage() throws Exception {
        Page newPage = new Page("test", LETTER_HEIGHT, LETTER_WIDTH, PageOrientation.LANDSCAPE);
        assertEquals(LETTER_WIDTH, newPage.getHeight());
        assertEquals(LETTER_HEIGHT, newPage.getWidth());
    }
}
