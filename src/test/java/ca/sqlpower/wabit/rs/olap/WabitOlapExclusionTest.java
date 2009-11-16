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

package ca.sqlpower.wabit.rs.olap;

import java.util.Set;

import org.olap4j.Axis;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.WabitOlapAxis;
import ca.sqlpower.wabit.rs.olap.WabitOlapDimension;
import ca.sqlpower.wabit.rs.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.util.StubOlapConnectionMapping;

public class WabitOlapExclusionTest extends AbstractWabitObjectTest {
    
    private WabitOlapExclusion wabitExclusion;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wabitExclusion = new WabitOlapExclusion(Operator.MEMBER, "unique/member/name");
        
        WabitOlapDimension wabitDimension = new WabitOlapDimension("dimension");
        wabitDimension.addChild(wabitExclusion, 0);
        OlapQuery query = new OlapQuery(new StubOlapConnectionMapping());
        WabitOlapAxis axis = new WabitOlapAxis(Axis.ROWS);
        axis.addDimension(wabitDimension);
        query.addChild(axis, 0);
        getWorkspace().addOlapQuery(query);
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> objects = super.getPropertiesToNotPersistOnObjectPersist();
    	objects.add("initialized");
    	return objects;
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return wabitExclusion;
    }

}
