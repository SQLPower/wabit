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

package ca.sqlpower.wabit.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;

/**
 * This test class is for testing problems with Wabit and the demo project.
 */
public class DemoDatabaseTest extends TestCase {

    
    
    private WabitSessionContext context;
	private StubWabitSession session;

	public void testVersionNumberUpToDate() throws Exception {
    
    	final PlDotIni plIni = new PlDotIni();
    	plIni.read(ClassLoader.getSystemResourceAsStream("ca/sqlpower/sql/default_database_types.ini"));
    	plIni.read(ClassLoader.getSystemResourceAsStream("ca/sqlpower/demodata/example_database.ini"));
        
        final Olap4jDataSource olapDS = plIni.getDataSource("World Facts OLAP Connection", 
        		Olap4jDataSource.class);
        if (olapDS == null) throw new IllegalStateException("Cannot find 'World Facts OLAP Connection'");
        final OlapConnectionPool connectionPool = new OlapConnectionPool(olapDS, 
        		new SQLDatabaseMapping() {
        	private final SQLDatabase sqlDB = new SQLDatabase(olapDS.getDataSource());
        	public SQLDatabase getDatabase(JDBCDataSource ds) {
        		return sqlDB;
        	}
        });
    	
    	
    	this.context = new StubWabitSessionContext() {
    		public org.olap4j.OlapConnection createConnection(Olap4jDataSource dataSource) 
    			throws java.sql.SQLException ,ClassNotFoundException ,javax.naming.NamingException {
    				return connectionPool.getConnection();
    		};
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return plIni;
    		}
    	};
    	
    	this.session = new StubWabitSession(context) {
    		
    		@Override
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return getContext().getDataSources();
    		}
    		
    		@Override
    		public WabitWorkspace getWorkspace() {
    			return null;
    		}
    	};
        
        File file = new File("build/ca/sqlpower/wabit/example_workspace.wabit");
        FileInputStream fis = new FileInputStream(file);
        try {
        	OpenWorkspaceXMLDAO workspaceDAO = new OpenWorkspaceXMLDAO(context, fis, OpenWorkspaceXMLDAO.UNKNOWN_STREAM_LENGTH);
        	workspaceDAO.openWorkspaces();
        } finally {
        	fis.close();
        }
            
    }
    
}
