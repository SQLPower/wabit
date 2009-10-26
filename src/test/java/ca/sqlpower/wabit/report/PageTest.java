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
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.Page.PageOrientation;

public class PageTest extends AbstractWabitObjectTest {

    private Page page;
    
    private final int LETTER_WIDTH = 72 * 8 + (72 / 2);
    private final int LETTER_HEIGHT = 72 * 11;
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> notPersisting = super.getPropertiesToNotPersistOnObjectPersist();
    	
    	// This is a child.
    	notPersisting.add("contentBoxes");
    	
    	// These are generated from other properties.
    	notPersisting.add("leftMarginOffset");
    	notPersisting.add("lowerMarginOffset");
    	notPersisting.add("rightMarginOffset");
    	notPersisting.add("upperMarginOffset");
    	notPersisting.add("pageFormat");
    	
    	return notPersisting;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        WabitWorkspace workspace = new WabitWorkspace();
        workspace.setSession(session);
        Report report = new Report("Report");
        workspace.addReport(report);
        page = new Page("test page", LETTER_WIDTH, LETTER_HEIGHT, PageOrientation.PORTRAIT, true);
        report.page = this.page;
        page.setParent(report);
        
        getWorkspace().addReport(report);
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
        Page newPage = new Page("test", LETTER_HEIGHT, LETTER_WIDTH, PageOrientation.LANDSCAPE, true);
        assertEquals(LETTER_WIDTH, newPage.getHeight());
        assertEquals(LETTER_HEIGHT, newPage.getWidth());
    }

    /**
     * Tests that a content box can be added and removed with the addChild and
     * removeChild methods.
     */
    public void testAddAndRemoveContentBox() throws Exception {
        ContentBox cb = new ContentBox();
        
        page.addChild(cb, 0);
        assertTrue(page.getChildren().contains(cb));
        
        page.removeChild(cb);
        assertFalse(page.getChildren().contains(cb));
    }
    
    /**
     * Tests that a guide can be added and removed from a page with the addChild
     * and removeChild methods.
     */
    public void testAddAndRemoveGuide() throws Exception {
        Guide guide = new Guide(Axis.HORIZONTAL, 10);
        ContentBox box = new ContentBox();
        page.addContentBox(box);
        
        page.addChild(guide, 1);
        assertTrue(page.getChildren().contains(guide));
        assertEquals(guide, page.getGuides().get(1));
        
        page.removeChild(guide);
        assertFalse(page.getChildren().contains(guide));
    }
    
    @Override
    public void testPersisterAddsNewObject() throws Exception {
		// no-op because Page is never persisted as a new object, it is
		// persisted as a child of a Layout
    }
}
