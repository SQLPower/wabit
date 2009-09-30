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

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.sqlobject.StubSQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitJoin;
import ca.sqlpower.wabit.WabitObject;

public class WabitJoinTest extends AbstractWabitObjectTest {
    
    private WabitJoin wabitJoin;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        QueryCache cache = new QueryCache(new StubSQLDatabaseMapping());
        cache.setName("query");
        Container container1 = new ItemContainer("container1");
        Item item1 = new StringItem("item1");
        container1.addItem(item1);
        cache.addTable(container1);
        Container container2 = new ItemContainer("container2");
        Item item2 = new StringItem("item2");
        container2.addItem(item2);
        cache.addTable(container2);
        SQLJoin delegate = new SQLJoin(item1, item2);
        wabitJoin = new WabitJoin(cache, delegate);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return wabitJoin;
    }

}
