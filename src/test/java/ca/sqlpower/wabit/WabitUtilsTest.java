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

package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.util.SQLPowerUtils;

public class WabitUtilsTest extends TestCase {

    private static class TestWabitObject extends StubWabitObject {

        private String name;
        private int cleanupCallCount = 0;
        
        private final List<WabitObject> children = new ArrayList<WabitObject>();
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public CleanupExceptions cleanup() {
            cleanupCallCount++;
            CleanupExceptions exceptions = new CleanupExceptions();
            exceptions.add("Cleanup called on " + name);
            return exceptions;
        }
        
        public int getCleanupCallCount() {
            return cleanupCallCount;
        }
        
        public void addChild(WabitObject child) {
            children.add(child);
        }
        
        @Override
        public List<? extends WabitObject> getChildren() {
            return Collections.unmodifiableList(children);
        }
    }
    
    /**
     * Tests the cleanupWabitObject calls cleanup on the given object
     * and all of its children and that it collects the errors from each
     * object.
     */
    public void testCleanupWabitObject() throws Exception {
        TestWabitObject parent = new TestWabitObject();
        parent.setName("parent");
        TestWabitObject child = new TestWabitObject();
        child.setName("child");
        parent.addChild(child);
        TestWabitObject grandchild = new TestWabitObject();
        grandchild.setName("grandchild");
        child.addChild(grandchild);
        
        CleanupExceptions cleanupObject = SQLPowerUtils.cleanupSPObject(parent);
        
        assertFalse(cleanupObject.isCleanupSuccessful());
        assertEquals(3, cleanupObject.getErrorMessages().size());
        assertEquals(1, parent.getCleanupCallCount());
        assertEquals(1, child.getCleanupCallCount());
        assertEquals(1, grandchild.getCleanupCallCount());
    }
}
