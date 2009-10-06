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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;

import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.WabitObject;

public class OlapQueryTest extends AbstractWabitObjectTest {

    private OlapQuery query;
    
    private PlDotIni plIni;

    private Olap4jDataSource ds;
    
    private SQLDatabase db;
    
    private OlapConnectionPool connectionPool;
    
    private final SQLDatabaseMapping dbMapping = new SQLDatabaseMapping() {
    	
    	public SQLDatabase getDatabase(JDBCDataSource ds) {
    		return db;
    	}
    };
    
    private OlapConnectionMapping connectionMapping = new OlapConnectionMapping() {
    	
		public OlapConnection createConnection(Olap4jDataSource dataSource)
				throws SQLException, ClassNotFoundException,
				NamingException {
			OlapConnectionPool pool = new OlapConnectionPool(ds, dbMapping);
			return pool.getConnection();
		}
    	
    };
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        
        plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        ds = plIni.getDataSource("World Facts OLAP Connection", Olap4jDataSource.class);
        
        db = new SQLDatabase(ds.getDataSource());
        

        
        query = new OlapQuery(null, connectionMapping, "GUI Query", "LOCALDB", "World", "World Countries");
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
    
    /**
     * Tests the initialization of a query as though it was loaded from a file.
     */
    public void testInitAsLoaded() throws Exception {
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
    	
    	//This should not throw an exception
    	query.executeOlapQuery();
    	
    	assertNotNull(rowsAxis.getQueryAxis());
    	assertTrue(rowsAxis.getQueryAxis().getDimensions().contains(rowsDimension.getDimension()));
    	
    	assertNotNull(columnsAxis.getQueryAxis());
    	assertTrue(columnsAxis.getQueryAxis().getDimensions().contains(columnsDimension.getDimension()));
    	
    	assertNotNull(rowsDimension.getDimension());
    	assertNotNull(columnsDimension.getDimension());
    	
    	assertNotNull(rowsInclusion.getSelection());
    	assertNotNull(rowsExclusion.getSelection());
    	assertNotNull(colInclusion.getSelection());
	}

	/**
	 * Tests the initialization of a query as though it was loaded from a file
	 * and then tries to expand a member.
	 */
    public void testInitAsLoadedAndExpandMember() throws Exception {
    	WabitOlapAxis rowsAxis = new WabitOlapAxis(Axis.ROWS);
    	WabitOlapDimension rowsDimension = new WabitOlapDimension("Geography");
    	rowsAxis.addDimension(rowsDimension);
    	WabitOlapInclusion rowsInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Geography].[World]");
    	WabitOlapExclusion rowsExclusion = new WabitOlapExclusion(Operator.MEMBER, "[Geography].[World].[Africa]");
    	rowsDimension.addExclusion(rowsExclusion);
    	rowsDimension.addInclusion(rowsInclusion);
    	
    	WabitOlapAxis columnsAxis = new WabitOlapAxis(Axis.COLUMNS);
    	WabitOlapDimension colDimension = new WabitOlapDimension("Measures");
    	columnsAxis.addDimension(colDimension);
    	WabitOlapInclusion colInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Life Expectancy]");
    	colDimension.addInclusion(colInclusion);
    	
    	query.addAxis(columnsAxis);
    	query.addAxis(rowsAxis);

    	//What a train wreck.
    	Cube cube = connectionMapping.createConnection(ds).getSchema().getCubes().get("World Countries");
    	Member worldMember = cube.getDimensions().get("Geography").getHierarchies().get("Geography").getLevels().get(0).getMembers().get(0);
    	
    	assertEquals("World", worldMember.getName());
    	
    	query.execute();
    	query.toggleMember(worldMember);

    	WabitOlapAxis afterRowsAxis = null;
    	for (WabitOlapAxis axis : query.getAxes()) {
    		if (axis.getOrdinal() == Axis.ROWS) {
    			afterRowsAxis = axis;
    			break;
    		}
    	}
    	
    	WabitOlapDimension afterRowsDimension = null;
    	for (WabitOlapDimension dimension : afterRowsAxis.getDimensions()) {
    		if (dimension.getName().equals("Geography")) {
    			afterRowsDimension = dimension;
    			break;
    		}
    	}
    	
    	assertEquals(2, afterRowsDimension.getInclusions().size());
    	WabitOlapInclusion inclusion1 = afterRowsDimension.getInclusions().get(0);
    	assertEquals("[Geography].[World]", inclusion1.getUniqueMemberName());
    	
    	WabitOlapInclusion inclusion2 = afterRowsDimension.getInclusions().get(1);
    	assertEquals("[Geography].[World]", inclusion2.getUniqueMemberName());
	}
    
    public void testInitAsLoadedAndExcludeMember() throws Exception {
    	WabitOlapAxis rowsAxis = new WabitOlapAxis(Axis.ROWS);
    	WabitOlapDimension rowsDimension = new WabitOlapDimension("Geography");
    	rowsAxis.addDimension(rowsDimension);
    	WabitOlapInclusion rowsInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Geography].[World]");
    	WabitOlapInclusion rowsInclusion2 = new WabitOlapInclusion(Operator.MEMBER, "[Geography].[World].[Asia]");
    	WabitOlapExclusion rowsExclusion = new WabitOlapExclusion(Operator.MEMBER, "[Geography].[World].[Africa]");
    	rowsDimension.addExclusion(rowsExclusion);
    	rowsDimension.addInclusion(rowsInclusion2);
    	rowsDimension.addInclusion(rowsInclusion);
    	
    	WabitOlapAxis columnsAxis = new WabitOlapAxis(Axis.COLUMNS);
    	WabitOlapDimension colDimension = new WabitOlapDimension("Measures");
    	columnsAxis.addDimension(colDimension);
    	WabitOlapInclusion colInclusion = new WabitOlapInclusion(Operator.MEMBER, "[Measures].[Life Expectancy]");
    	colDimension.addInclusion(colInclusion);
    	
    	query.addAxis(columnsAxis);
    	query.addAxis(rowsAxis);

    	//What a train wreck.
    	Cube cube = connectionMapping.createConnection(ds).getSchema().getCubes().get("World Countries");
    	Member asiaMember = null;
    	for (Member member : cube.getDimensions().get("Geography").getHierarchies().get("Geography").getLevels().get("Continent").getMembers()) {
    		if (member.getName().equals("Asia")) {
    			asiaMember = member;
    			break;
    		}
    	}
    	
    	assertEquals("Asia", asiaMember.getName());
    	
    	System.out.println("Inclusions");
    	for (WabitOlapInclusion sel : rowsDimension.getInclusions()) {
    		System.out.println(sel.getUniqueMemberName());
    	}
    	System.out.println("Exclusions");
    	for (WabitOlapExclusion sel : rowsDimension.getExclusions()) {
    		System.out.println(sel.getUniqueMemberName());
    	}
    	
    	query.execute();
    	
    	System.out.println("After execution");
    	
    	System.out.println("Inclusions");
    	List<Selection> afterExecInclusions = rowsDimension.getDimension().getInclusions();
		for (Selection sel : afterExecInclusions) {
    		System.out.println(sel.getMember().getUniqueName());
    	}
    	System.out.println("Exclusions");
    	for (Selection sel : rowsDimension.getDimension().getExclusions()) {
    		System.out.println(sel.getMember().getUniqueName());
    	}
    	
    	assertEquals(2, afterExecInclusions.size());
    	List<String> includedMemberNames = Arrays.asList(new String[]{"World", "Asia"});
    	assertTrue(includedMemberNames.contains(afterExecInclusions.get(0).getMember().getName()));
    	assertTrue(includedMemberNames.contains(afterExecInclusions.get(1).getMember().getName()));
    	
    	assertEquals(1, rowsDimension.getDimension().getExclusions().size());
    	assertEquals("Africa", rowsDimension.getDimension().getExclusions().get(0).getMember().getName());
    	
    	query.excludeMember("Geography", asiaMember, Operator.MEMBER);
    	
    	System.out.println("After exclusion occurred");

    	WabitOlapAxis afterRowsAxis = null;
    	for (WabitOlapAxis axis : query.getAxes()) {
    		if (axis.getOrdinal() == Axis.ROWS) {
    			afterRowsAxis = axis;
    			break;
    		}
    	}
    	
    	WabitOlapDimension afterRowsDimension = null;
    	for (WabitOlapDimension dimension : afterRowsAxis.getDimensions()) {
    		if (dimension.getName().equals("Geography")) {
    			afterRowsDimension = dimension;
    			break;
    		}
    	}
    	
    	System.out.println("Inclusions");
    	for (Selection sel : afterRowsDimension.getDimension().getInclusions()) {
    		System.out.println(sel.getMember().getUniqueName());
    	}
    	System.out.println("Exclusions");
    	for (Selection sel : afterRowsDimension.getDimension().getExclusions()) {
    		System.out.println(sel.getMember().getUniqueName());
    	}
    	assertEquals(2, afterRowsDimension.getInclusions().size());
    	assertEquals(2, afterRowsDimension.getExclusions().size());
    	List<Member> excludedMembers = new ArrayList<Member>();
    	for (Selection selection : afterRowsDimension.getDimension().getExclusions()) {
    		excludedMembers.add(selection.getMember());
    	}
    	assertTrue(excludedMembers.contains(asiaMember));
	}
    
}
