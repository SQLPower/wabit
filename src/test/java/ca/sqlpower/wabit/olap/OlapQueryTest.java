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

package ca.sqlpower.wabit.olap;

import java.io.File;

import org.olap4j.OlapConnection;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.util.StubOlapConnectionMapping;

public class OlapQueryTest extends AbstractWabitObjectTest {

    private OlapQuery query;
    
    private PlDotIni plIni;

    private Olap4jDataSource ds;
    
    private OlapConnectionPool connectionPool;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        ds = plIni.getDataSource("World Facts OLAP Connection", Olap4jDataSource.class);
        query = new OlapQuery(new StubOlapConnectionMapping());
        query.setOlapDataSource(ds);
        
        connectionPool = new OlapConnectionPool(ds, 
                new SQLDatabaseMapping() {
            private final SQLDatabase sqlDB = new SQLDatabase(ds.getDataSource());
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return sqlDB;
            }
        });
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return query;
    }

    /**
     * Tests that this class can successfully connect to the database for testing.
     */
    public void testConnectsToRegressionDS() throws Exception {
        OlapConnection connection = connectionPool.getConnection();
        connection.getSchema().getCubes().get("World Countries");
    }
    
}
