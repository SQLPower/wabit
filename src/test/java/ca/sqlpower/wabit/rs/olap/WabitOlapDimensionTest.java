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
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.util.StubOlapConnectionMapping;

public class WabitOlapDimensionTest extends AbstractWabitObjectTest {
    
    private WabitOlapDimension wabitDimension;
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return WabitOlapAxis.class;
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    	ignored.add("exclusions");
    	ignored.add("inclusions");
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        wabitDimension = new WabitOlapDimension("Dimension");
        OlapQuery query = new OlapQuery(new StubOlapConnectionMapping());
        WabitOlapAxis axis = new WabitOlapAxis(Axis.ROWS);
        query.addAxis(axis);
        axis.addDimension(wabitDimension);
        getWorkspace().addOlapQuery(query);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return wabitDimension;
    }
    
    /**
     * Tests adding and removing a child from the dimension without
     * initializing the dimension.
     */
    public void testAddAndRemoveInclusionChild() throws Exception {
    	
        WabitOlapInclusion inclusion = new WabitOlapInclusion(Operator.CHILDREN, "Member");
        
        wabitDimension.addChild(inclusion, 0);
        
        assertTrue(wabitDimension.getInclusions().contains(inclusion));
        
        wabitDimension.removeChild(inclusion);
        
        assertFalse(wabitDimension.getInclusions().contains(inclusion));
    }
    
    /**
     * Tests adding and removing a child from the dimension without
     * initializing the dimension.
     */
    public void testAddAndRemoveExclusionChild() throws Exception {
    	
        WabitOlapExclusion exclusion = new WabitOlapExclusion(Operator.MEMBER, "Member");
        
        wabitDimension.addChild(exclusion, 0);
        
        assertTrue(wabitDimension.getExclusions().contains(exclusion));
        
        wabitDimension.removeChild(exclusion);
        
        assertFalse(wabitDimension.getExclusions().contains(exclusion));
    }
    
    /**
     * Test adding and removing a child from the dimension after it
     * has been initialized.
     * @throws Exception
     */
    public void testAddAndRemoveInclusionInitialized() throws Exception {
    	
    	OlapQuery query = new OlapQuery(
				null, 
				getContext(), 
				"Life Expectancy And GNP Correlation", 
				"GUI Query", 
				"LOCALDB", 
				"World", 
				"World Countries",
				null);
        
        getWorkspace().addOlapQuery(query);
        
        WabitOlapAxis rowAxis = new WabitOlapAxis(Axis.ROWS);
        WabitOlapAxis colAxis = new WabitOlapAxis(Axis.COLUMNS);
        query.addAxis(rowAxis);
        query.addAxis(colAxis);
        query.setOlapDataSource((Olap4jDataSource)getSession().getDataSources().getDataSource("World Facts OLAP Connection"));
        
        query.init();
        query.updateAttributes();
        
        final Cube cube = query.getCurrentCube();
        Dimension dimension = cube.getDimensions().get("Geography");
        
        //arbitrary measure to add
        Member measure = cube.getDimensions().get("Measures").getDefaultHierarchy().getDefaultMember();
        
        //arbitrary member to exclude
        Member excludedMember = dimension.getDefaultHierarchy().getLevels().get(1).getMembers().get(0);
        
        query.addToAxis(0, dimension.getDefaultHierarchy().getDefaultMember(), Axis.ROWS);
        query.addToAxis(0, measure, Axis.COLUMNS);
        query.excludeMember(dimension.getName(), excludedMember, Operator.MEMBER);
        
        this.wabitDimension = query.getDimension("Geography");

        //get the first included member on the rows axis to try and remove it
        WabitOlapDimension dimensionToRemoveFrom = null;
        WabitOlapInclusion inclusionToRemove = null;
        for (WabitObject queryChild : query.getChildren()) {
            if (((WabitOlapAxis) queryChild).getOrdinal().equals(Axis.ROWS)) {
                dimensionToRemoveFrom = ((WabitOlapAxis) queryChild).getDimensions().get(0);
                inclusionToRemove = dimensionToRemoveFrom.getInclusions().get(0);
            }
        }
        
        assertNotNull(inclusionToRemove);
        
        boolean inclusionInDimension = false;
        for (Selection s : dimensionToRemoveFrom.getDimension().getInclusions()) {
            if (s.getMember().equals(inclusionToRemove.getSelection().getMember()) && 
                    s.getOperator().equals(inclusionToRemove.getOperator())) {
                inclusionInDimension = true;
            }
        }
        assertTrue(inclusionInDimension);
        
        dimensionToRemoveFrom.removeChild(inclusionToRemove);
        
        inclusionInDimension = false;
        for (Selection s : dimensionToRemoveFrom.getDimension().getInclusions()) {
            if (s.getMember().equals(inclusionToRemove.getSelection().getMember()) && 
                    s.getOperator().equals(inclusionToRemove.getOperator())) {
                inclusionInDimension = true;
            }
        }
        assertFalse(inclusionInDimension);
        
        dimensionToRemoveFrom.addChild(inclusionToRemove, 0);
        
        inclusionInDimension = false;
        for (Selection s : dimensionToRemoveFrom.getDimension().getInclusions()) {
            if (s.getMember().equals(inclusionToRemove.getSelection().getMember()) && 
                    s.getOperator().equals(inclusionToRemove.getOperator())) {
                inclusionInDimension = true;
            }
        }
        assertTrue(inclusionInDimension);
    }
}
