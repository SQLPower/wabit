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
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;

public class TableContainerTest extends AbstractWabitObjectTest {

	private TableContainer tableContainer;
	private QueryCache queryCache;
	private PlDotIni plIni;
	private SQLDatabase db;
	
	@Override
	protected void setUp() throws Exception {
		plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        SPDataSource ds = plIni.getDataSource("regression_test");

        db = new SQLDatabase(ds);
        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
        stmt.execute("CREATE TABLE test_table (col1 varchar(50), col2 varchar(50))");
        stmt.close();
        con.close();
        
        SQLTable testTable = db.getTableByName("test_table");
		queryCache = new QueryCache(new StubWabitSession(new StubWabitSessionContext()));
		
		tableContainer = new TableContainer(queryCache, testTable);
	}
	
	@Override
	public WabitObject getObjectUnderTest() {
		return tableContainer;
	}
	
	@Override
	public void testAddChildren() throws Exception {
		//Table container does not allow children being added or removed once created.
	}
	
	/**
	 * Previously loading a table from a broken data source would throw an exception 
	 * preventing projects from being loaded if they were saved with a broken connection
	 * or if the data source was altered so the table did not exist. This test checks
	 * that loading a table when it does not exist in the data source does not cause
	 * a crash to allow the project to be loaded.
	 */
	public void testGetNameWithoutException() throws Exception {
		
        SQLTable testTable = db.getTableByName("test_table");
        assertNotNull(testTable);
		queryCache = new QueryCache(new StubWabitSession(new StubWabitSessionContext()));
		
		tableContainer = new TableContainer(UUID.randomUUID().toString(), queryCache, "test_table", "", "public", new ArrayList<SQLObjectItem>());
		
        Connection con = db.getConnection();
        Statement stmt = con.createStatement();
		stmt.execute("DROP TABLE test_table");
		stmt.close();
		con.close();

		tableContainer.loadTableByQualifiedName();
	}

}
