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

import ca.sqlpower.query.StringItem;
import ca.sqlpower.sqlobject.StubSQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.rs.query.QueryCache;

public class ColumnInfoTest extends AbstractWabitObjectTest {

    private ColumnInfo ci;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ci = new ColumnInfo(new StringItem("Item"), "column name");
        
        QueryCache query = new QueryCache(new StubSQLDatabaseMapping());
        ResultSetRenderer renderer = new ResultSetRenderer(query);
        renderer.addChild(ci, 0);
        ContentBox contentBox = new ContentBox();
        contentBox.setContentRenderer(renderer);
        Report report = new Report("report");
        report.getPage().addContentBox(contentBox);
        
        getWorkspace().addReport(report);
        
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return ci;
    }
    
    
}
