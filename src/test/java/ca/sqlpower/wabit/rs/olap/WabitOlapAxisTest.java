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
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.util.StubOlapConnectionMapping;

public class WabitOlapAxisTest extends AbstractWabitObjectTest {
    
    private WabitOlapAxis wabitAxis;
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> notPersisting = super.getPropertiesToNotPersistOnObjectPersist();
    	notPersisting.add("dimensions");
    	return notPersisting;
    }
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return OlapQuery.class;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wabitAxis = new WabitOlapAxis(Axis.ROWS);
        OlapQuery query = new OlapQuery(new StubOlapConnectionMapping());
        query.addChild(wabitAxis, 0);
        getWorkspace().addOlapQuery(query);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return wabitAxis;
    }
    
    public void testAddAndRemoveChild() throws Exception {
        WabitOlapDimension dimension = new WabitOlapDimension("Dimension");
        
        assertFalse(wabitAxis.getChildren().contains(dimension));
        
        wabitAxis.addChild(dimension, 0);
        
        assertTrue(wabitAxis.getChildren().contains(dimension));
        
        wabitAxis.removeChild(dimension);
        
        assertFalse(wabitAxis.getChildren().contains(dimension));
    }
    
    public void testAddAndRemoveChildAfterInit() throws Exception {
        
        OlapQuery query = new OlapQuery(
        						null, 
        						getContext(), 
        						"Life Expectancy And GNP Correlation", 
        						"GUI Query", 
        						"LOCALDB", 
        						"World", 
        						"World Countries",
        						null);
        
        WabitOlapAxis rowAxis = new WabitOlapAxis(Axis.ROWS);
        WabitOlapAxis colAxis = new WabitOlapAxis(Axis.COLUMNS);
        query.addAxis(rowAxis);
        query.addAxis(colAxis);
        
        query.setOlapDataSource((Olap4jDataSource)getSession().getDataSources().getDataSource("World Facts OLAP Connection"));
        
        getWorkspace().addOlapQuery(query);
        query.init();
        query.updateAttributes();
        
        Dimension dimension = query.getCurrentCube().getDimensions().get("Geography");
        
        final Member defaultMember = dimension.getDefaultHierarchy().getDefaultMember();
        query.addToAxis(0, defaultMember, rowAxis.getOrdinal());

        assertEquals(1, rowAxis.getChildren().size());
        
        WabitOlapDimension wabitDimension = (WabitOlapDimension) rowAxis.getChildren().get(0);
        
        rowAxis.removeChild(wabitDimension);
        
        assertFalse(rowAxis.getChildren().contains(wabitDimension));
        assertEquals(0, rowAxis.getChildren().size());
        
        rowAxis.addChild(wabitDimension, 0);
        
        assertTrue(rowAxis.getChildren().contains(wabitDimension));
        assertEquals(1, rowAxis.getChildren().size());
    }

}
