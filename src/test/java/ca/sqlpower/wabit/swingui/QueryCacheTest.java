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

package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import junit.framework.TestCase;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryChangeAdapter;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.wabit.QueryCache;

public class QueryCacheTest extends TestCase {
	
	private class CountingRowSetChangeListener implements RowSetChangeListener {
		private int rowCount = 0;
		
		public void rowAdded(RowSetChangeEvent e) {
			rowCount++;
		}
		
		public int getRowCount() {
			return rowCount;
		}
	};
	
	private class CountingQueryChangeListener extends QueryChangeAdapter {
	    
	    private int changeCount = 0;
	    
	    @Override
	    public void itemPropertyChangeEvent(PropertyChangeEvent evt) {
	        changeCount++;
	    }
	    
	    public int getChangeCount() {
            return changeCount;
        }
	}
	
	private QueryCache queryCache;
	
	protected void setUp() throws Exception {
		super.setUp();
		queryCache = new QueryCache(new StubWabitSwingSession());
	}
	
	public void testAliasListener() throws Exception {
		Item item = new StringItem("ItemName");
		queryCache.getQuery().addItem(item);
		item.setSelected(true);
		CountingQueryChangeListener listener = new CountingQueryChangeListener();
		queryCache.getQuery().addQueryChangeListener(listener);
		String newAlias = "Alias test.";
		item.setAlias(newAlias);
		assertEquals(1, listener.getChangeCount());
	}
	
	/**
	 * Checks that the query cache will throw a row set property change
	 * when a new row is added.
	 */
	public void testQueryFiresRSChange() throws Exception {
		PlDotIni plIni = new PlDotIni();
		plIni.read(new File("src/test/java/pl.regression.ini"));
		JDBCDataSource ds = plIni.getDataSource("regression_test", JDBCDataSource.class);
		Connection con = ds.createConnection();
		Statement stmt = con.createStatement();
		stmt.execute("create table rsTest (col1 varchar(50), col2 varchar(50))");
		stmt.execute("insert into rsTest (col1, col2) values ('hello', 'line1')");
		stmt.execute("insert into rsTest (col1, col2) values ('bye', 'line2')");
		stmt.close();
		con.close();
		
		CountingRowSetChangeListener listener = new CountingRowSetChangeListener();
		queryCache.addRowSetChangeListener(listener);
		queryCache.setDataSource(ds);
		queryCache.getQuery().setStreaming(true);
		queryCache.getQuery().defineUserModifiedQuery("select * from rsTest");
		queryCache.executeStatement();
		
		for (Thread t : queryCache.getStreamingThreads()) {
			t.join(10000);
		}
		
		assertEquals(2, listener.getRowCount());
		
	}

}
