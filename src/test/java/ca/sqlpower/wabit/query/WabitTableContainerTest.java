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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.query.SQLObjectItem;
import ca.sqlpower.query.TableContainer;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.StubSQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitColumnItem;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitTableContainer;

public class WabitTableContainerTest extends AbstractWabitObjectTest {
    
    private WabitTableContainer container;
    private PlDotIni plIni;
    private JDBCDataSource ds;
    private SQLDatabase db;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        ds = plIni.getDataSource("regression_test", JDBCDataSource.class);
        db = new SQLDatabase(ds);
        
        final List<SQLObjectItem> items = new ArrayList<SQLObjectItem>();
        TableContainer delegate = new TableContainer("new-id", db, "tableName", "schemaName", "catalogName", items);
        container = new WabitTableContainer(delegate);
        
        QueryCache query = new QueryCache(new StubSQLDatabaseMapping());
        query.addChild(container, 0);
        getWorkspace().addChild(query, 0);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return container;
    }
    
    /**
     * Tests that children can be added to and removed from this container.
     * Although the children of this container are based on the children of
     * the delegate container and the delegate container's children cannot
     * change once the table has been setup some classes, like the undo manager
     * still need to be able to modify the child list of this class. Changing
     * the child wrappers on this component merely changes the visibility of the
     * delegate objects but does not actually change the delegate.
     */
    public void testAddAndRemoveChild() throws Exception {
        SQLObjectItem item1 = new SQLObjectItem("item1", "uuid1");
        SQLObjectItem item2 = new SQLObjectItem("item2", "uuid2");
        List<SQLObjectItem> items = new ArrayList<SQLObjectItem>();
        items.add(item1);
        items.add(item2);
        TableContainer del = new TableContainer("uuid", db, "table", "schema", "catalog", items);
        WabitTableContainer testWrapper = new WabitTableContainer(del);
        
        assertEquals(2, testWrapper.getChildren().size());
        
        WabitColumnItem wrapperItem = (WabitColumnItem) testWrapper.getChildren().get(0);
        
        testWrapper.removeChild(wrapperItem);
        
        assertEquals(1, testWrapper.getChildren().size());
        assertFalse(testWrapper.getChildren().contains(wrapperItem));
        
        testWrapper.addChild(wrapperItem, 1);
        
        assertEquals(2, testWrapper.getChildren().size());
        assertEquals(wrapperItem, testWrapper.getChildren().get(1));
        
    }
    
}
