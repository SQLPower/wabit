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
import ca.sqlpower.wabit.WabitObject;

public class LayoutTest extends AbstractWabitObjectTest {

    private Report layout;
    
    @Override
    public Set<String> getPropertiesToIgnoreForEvents() {
    	Set<String> ignored = super.getPropertiesToIgnoreForEvents();
    	//This is actually a child.
    	ignored.add("page");
    	ignored.add("variableResolver");
    	ignored.add("variables");
    	return ignored;
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    	ignored.add("currentlyPrinting");
    	ignored.add("numberOfPages");
    	ignored.add("page");
    	ignored.add("pageFormat");
    	ignored.add("printable");
    	ignored.add("varContext");
    	ignored.add("zoomLevel");
    	ignored.add("variableResolver");
    	ignored.add("variables");
    	return ignored;
    }
    
    @Override
    public Set<String> getPropertiesToIgnoreForPersisting() {
    	Set<String> ignored = super.getPropertiesToIgnoreForPersisting();
    	ignored.add("zoomLevel");
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        layout = new Report("test layout");
        getWorkspace().addChild(layout, 0);
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return layout;
    }
}
