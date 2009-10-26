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

import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;

public class ContentBoxTest extends AbstractWabitObjectTest {

    private ContentBox cb;
    
    private Page parentPage;
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    	ignored.add("bounds");
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cb = new ContentBox();
        Report report = new Report("report");
        parentPage = report.getPage();
        parentPage.addContentBox(cb);
        
        getWorkspace().addReport(report);
        
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return cb;
    }
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return Page.class;
    }

    /**
     * Tests the content renderer can be set by calling addChild and removed by
     * calling removeChild.
     */
    public void testAddAndRemoveChild() throws Exception {
    	ContentBox cb = new ContentBox();
        StubWabitSession session = new StubWabitSession(new StubWabitSessionContext());
        Report report = new Report("New Report");
        session.getWorkspace().addReport(report);
        report.getPage().addContentBox(cb);
        
        ImageRenderer renderer = new ImageRenderer();
        assertNull(cb.getContentRenderer());
        cb.addChild(renderer, 0);
        assertEquals(renderer, cb.getContentRenderer());
        cb.removeChild(renderer);
        assertNull(cb.getContentRenderer());
    }
}
