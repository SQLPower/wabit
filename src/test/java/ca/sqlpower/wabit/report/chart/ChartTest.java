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

package ca.sqlpower.wabit.report.chart;

import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.chart.ChartColumn.DataType;

public class ChartTest extends AbstractWabitObjectTest {

    private Chart chart;
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    
    	ignored.add("streaming");
    	ignored.add("resultSet");
    	ignored.add("seriesColours");
    	ignored.add("unfilteredResultSet");
    	
    	// These are children.
    	ignored.add("columns");
    	
    	// This is currently not used.
    	ignored.add("missingIdentifiers");
    	
    	// This is an internal property.
    	ignored.add("resultSetFilter");
    	
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        chart = new Chart();
        
        getWorkspace().addChart(chart);
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return chart;
    }

    /**
     * Tests that children can be added and removed from a chart. While this is
     * uncommon to be done as the children are maintained by the chart itself
     * some parts of Wabit, such as the undo manager, still need to be able to
     * modify the children of a chart.
     */
    public void testAddAndRemoveChild() throws Exception {
        ChartColumn col = new ChartColumn("col1", DataType.TEXT);
        
        assertEquals(0, chart.getChildren().size());
        
        chart.addChild(col, 0);
        
        assertEquals(col, chart.getChildren().get(0));
        
        chart.removeChild(col);
        
        assertEquals(0, chart.getChildren().size());
        
    }

}
