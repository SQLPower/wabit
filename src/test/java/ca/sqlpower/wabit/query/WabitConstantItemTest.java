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

package ca.sqlpower.wabit.query;

import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.sqlobject.StubSQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitConstantItem;
import ca.sqlpower.wabit.WabitConstantsContainer;
import ca.sqlpower.wabit.WabitObject;

public class WabitConstantItemTest extends AbstractWabitObjectTest {
    
    private WabitConstantItem constantItem;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        StringItem delegate = new StringItem("name"); 
        ItemContainer containerDel = new ItemContainer("constants");
        containerDel.addItem(delegate);
        WabitConstantsContainer constants = new WabitConstantsContainer(containerDel);
        constantItem = (WabitConstantItem) constants.getChildren().get(0);

        QueryCache query = new QueryCache(new StubSQLDatabaseMapping(), false, constants);
        getWorkspace().addChild(query, 0);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return constantItem;
    }

}
