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
import java.sql.SQLException;

import javax.naming.NamingException;

import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.WabitObject;

public class WabitOlapAxisTest extends AbstractWabitObjectTest {
    
    private WabitOlapAxis wabitAxis;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wabitAxis = new WabitOlapAxis(Axis.ROWS);
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
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        final Olap4jDataSource ds = plIni.getDataSource("World Facts OLAP Connection", Olap4jDataSource.class);
        
        final SQLDatabase db = new SQLDatabase(ds.getDataSource());
        
        final SQLDatabaseMapping dbMapping = new SQLDatabaseMapping() {
            
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return db;
            }
        };
        
        OlapConnectionMapping connectionMapping = new OlapConnectionMapping() {
            
            public OlapConnection createConnection(Olap4jDataSource dataSource)
                    throws SQLException, ClassNotFoundException,
                    NamingException {
                OlapConnectionPool pool = new OlapConnectionPool(ds, dbMapping);
                return pool.getConnection();
            }
            
        };
        
        OlapConnectionPool connectionPool = new OlapConnectionPool(ds, 
                new SQLDatabaseMapping() {
            private final SQLDatabase sqlDB = new SQLDatabase(ds.getDataSource());
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return sqlDB;
            }
        });
        
        Dimension dimension = connectionPool.getConnection().getSchema().getCubes().get("World Countries").getDimensions().get("Geography");
        
        OlapQuery query = new OlapQuery(null, connectionMapping, "GUI Query", "LOCALDB", "World", "World Countries");
        query.setOlapDataSource(ds);
        WabitOlapAxis rowAxis = new WabitOlapAxis(Axis.ROWS);
        WabitOlapAxis colAxis = new WabitOlapAxis(Axis.COLUMNS);
        query.addAxis(rowAxis);
        query.addAxis(colAxis);
        
        query.init();
        
        query.updateAttributes();
        
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
