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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.beanutils.ConversionException;
import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.PreparedOlapStatement;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.OlapConnectionProvider;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.WabitOlapAxis;
import ca.sqlpower.wabit.rs.olap.WabitOlapDimension;
import ca.sqlpower.wabit.rs.olap.WabitOlapExclusion;
import ca.sqlpower.wabit.rs.olap.WabitOlapInclusion;

public class ResultSetRendererOlapTest extends AbstractWabitObjectTest {

    private ResultSetRenderer renderer;
    
    private Graphics graphics;
    
    private ContentBox cb;
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return ContentBox.class;
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignorable = super.getPropertiesToNotPersistOnObjectPersist();
    	ignorable.add("colBeingDragged");
    	ignorable.add("columnInfoList");
    	return ignorable;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
//        this.query = new QueryCache(getContext());
//        getWorkspace().addQuery(query, getSession());
//        query.setDataSource((JDBCDataSource)getSession().getDataSources().getDataSource("regression_test"));
//        
//        getWorkspace().addChild(query, 0);
//		renderer = new ResultSetRenderer(query);
//        parentCB = new ContentBox();
//        parentCB.setContentRenderer(renderer);
//        
//        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
//        graphics = image.getGraphics();
//        
//        Report report = new Report("report");
//        report.getPage().addContentBox(parentCB);
//        getWorkspace().addReport(report);
        
        OlapQuery query;
        PlDotIni plIni;
        final Olap4jDataSource ds;
        final SQLDatabase db;
        OlapConnectionPool connectionPool;
        
        plIni = new PlDotIni();
        plIni.read(new File("src/test/resources/pl.regression.ini"));
        ds = plIni.getDataSource("World Facts OLAP Connection", Olap4jDataSource.class);
        
        db = new SQLDatabase(ds.getDataSource());
        
        final SQLDatabaseMapping dbMapping = new SQLDatabaseMapping() {
        	public SQLDatabase getDatabase(JDBCDataSource ds) {
        		return db;
        	}
        };
        
        OlapConnectionProvider connectionMapping = new OlapConnectionProvider() {
    		public OlapConnection createConnection(Olap4jDataSource dataSource)
    				throws SQLException, ClassNotFoundException,
    				NamingException {
    			OlapConnectionPool pool = new OlapConnectionPool(ds, dbMapping);
    			return pool.getConnection();
    		}
    		public PreparedOlapStatement createPreparedStatement(
    	    		Olap4jDataSource dataSource, String mdx, SPVariableHelper helper) 
    	    {
    	    	try {
    	    		OlapConnection conn = createConnection(dataSource);
    				return helper.substituteForDb(conn, mdx);
    			} catch (SQLException e) {
    				throw new RuntimeException(e);
    			} catch (ClassNotFoundException e) {
    				throw new RuntimeException(e);
    			} catch (NamingException e) {
    				throw new RuntimeException(e);
    			}
    	    }
        	
        };
        

        
        query = new OlapQuery(
        				null, 
        				connectionMapping, 
        				"Life Expectancy And GNP Correlation", 
        				"GUI Query", 
        				"LOCALDB", 
        				"World", 
        				"World Countries",
        				null);
        
        query.setOlapDataSource(ds);
        
        connectionPool = new OlapConnectionPool(ds, 
                new SQLDatabaseMapping() {
            private final SQLDatabase sqlDB = new SQLDatabase(ds.getDataSource());
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return sqlDB;
            }
        });
        
        String catalogName = query.getCatalogName();
        String schemaName = query.getSchemaName();
        String cubeName = query.getCubeName();
        
		Catalog catalog;
		try {
			catalog = connectionMapping.createConnection(ds).getCatalogs().get(catalogName);
		} catch (Exception ex) {
			throw new ConversionException("Error connecting to data source " + catalogName + 
					" to get cube", ex);
		}
		Schema schema;
		try {
			schema = catalog.getSchemas().get(schemaName);
			Cube cube = schema.getCubes().get(cubeName);
			query.setCurrentCube(cube, false);
			
		} catch (OlapException e) {
			throw new ConversionException("The cube could not be retrieved.", e);
		}
		
        getWorkspace().addOlapQuery(query);
        getWorkspace().addDataSource(query.getOlapDataSource());
        
        WabitOlapAxis rowsAxis = new WabitOlapAxis(Axis.ROWS);
        WabitOlapDimension rowsDimension = new WabitOlapDimension("Geography");
        rowsAxis.addDimension(rowsDimension);
        WabitOlapInclusion rowsInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Geography].[World]");
        WabitOlapExclusion rowsExclusion = new WabitOlapExclusion(Operator.MEMBER, "[Geography].[World].[Africa]");
        rowsDimension.addExclusion(rowsExclusion);
        rowsDimension.addInclusion(rowsInclusion);
        
        WabitOlapAxis columnsAxis = new WabitOlapAxis(Axis.COLUMNS);
        WabitOlapDimension columnsDimension = new WabitOlapDimension("Measures");
        columnsAxis.addDimension(columnsDimension);
        WabitOlapInclusion colInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Life Expectancy]");
        columnsDimension.addInclusion(colInclusion);
        
        query.addAxis(columnsAxis);
        query.addAxis(rowsAxis);
        
        Report report = new Report("report");
        getWorkspace().addReport(report);
        
        cb = new ContentBox();
        report.getPage().addContentBox(cb);
        cb.setWidth(100);
        cb.setHeight(200);
        renderer = new ResultSetRenderer(query);
        cb.setContentRenderer(renderer);
        renderer.refresh();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return renderer;
    }
}
