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
import ca.sqlpower.wabit.AbstractWabitObjectTest;
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
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return container;
    }

}
