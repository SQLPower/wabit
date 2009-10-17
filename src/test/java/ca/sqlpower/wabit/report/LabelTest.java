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

import java.util.Collections;
import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.TestingVariableContext;
import ca.sqlpower.wabit.WabitObject;

public class LabelTest extends AbstractWabitObjectTest {

    private Label label;
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return ContentBox.class;
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    	ignored.add("variableContext");
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        label = new Label();
        label.setVariableContext(new TestingVariableContext());
        ContentBox cb = new ContentBox();
        cb.addChild(label, 0);
        
        Report report = new Report("report");
        report.getPage().addChild(cb, 0);
        getWorkspace().addReport(report);
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return label;
    }

    @Override
    public Set<String> getPropertiesToIgnoreForEvents() {
        return Collections.singleton("variableContext");
    }
    
}
