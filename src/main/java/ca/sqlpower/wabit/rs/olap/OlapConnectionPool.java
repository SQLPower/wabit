/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of SQL Power Library.
 *
 * SQL Power Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.rs.olap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;

/**
 * This class will create one connection to an {@link Olap4jDataSource}. This
 * class is useful to prevent opening lots of connections causing extra caching.
 */
public class OlapConnectionPool {
    
    private static final Logger logger = Logger.getLogger(OlapConnectionPool.class);

    /**
     * This is the data source this pool is storing connections for.
     */
    private final Olap4jDataSource dataSource;
    
    /**
     * This JNDI context is used as a temporary holding place during the process
     * of creating Olap4j connections.
     */
    private final Context ctx;

    private final SQLDatabaseMapping dbMapping;
    
    private final OlapConnection con;
    
    /**
     * Creates and pools a new connection based on the information in {@link #dataSource}.
     * 
     * @param dbMapping
     *            If the {@link #dataSource} is an in-process data source this
     *            should contain the mapping from the {@link JDBCDataSource} to
     *            the {@link SQLDatabase} that is pooling the connections. If
     *            the {@link #dataSource} is an XML/A data source this value can
     *            be null.
     * @throws NamingException 
     * @throws ClassNotFoundException 
     * @throws SQLException 
     */
    public OlapConnectionPool(Olap4jDataSource ds, SQLDatabaseMapping dbMapping) throws SQLException, ClassNotFoundException, NamingException {
        dataSource = ds;
        this.dbMapping = dbMapping;
        // FIXME this should be configured in an external jndi.properties file.
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        try {
            ctx = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        con = createOlapConnection();
        logger.debug("Created a connection to the OLAP data source");
    }
    
    public OlapConnection getConnection() throws SQLException, ClassNotFoundException, NamingException {
        return con;
    }

    /**
     * Creates a new connection based on the information in {@link #dataSource}.
     * This is a helper method for the constructor
     * 
     * @param dbMapping
     *            If the {@link #dataSource} is an in-process data source this
     *            should contain the mapping from the {@link JDBCDataSource} to
     *            the {@link SQLDatabase} that is pooling the connections. If
     *            the {@link #dataSource} is an XML/A data source this value can
     *            be null.
     */
    private OlapConnection createOlapConnection()
    throws SQLException, ClassNotFoundException, NamingException {
        final String uniqueName = UUID.randomUUID().toString();
        
        try {
            
            if (dataSource.getType().equals(Olap4jDataSource.Type.IN_PROCESS)) {
                SQLDatabase database = dbMapping.getDatabase(dataSource.getDataSource());
                ctx.bind(uniqueName, new DataSourceAdapter(database));
                
                if (dataSource.getMondrianSchema() == null
                        || dataSource.getDataSource() == null) {
                    // FIXME This validation should not be performed here.
                    return null;
                }
                
                // Init the class loader. This might not be necessary with JDK 1.6, but just for kicks....
                Class.forName(Olap4jDataSource.IN_PROCESS_DRIVER_CLASS_NAME);
                
                // Build a JDBC URL for Mondrian driver connection
                StringBuilder url = new StringBuilder("jdbc:mondrian:");
                url.append("DataSource='").append(uniqueName);
                url.append("';Catalog=").append(dataSource.getMondrianSchema().toString());
                
                Connection connection = DriverManager.getConnection(url.toString());
                return ((OlapWrapper) connection).unwrap(OlapConnection.class);
                
            } else if (dataSource.getType().equals(Olap4jDataSource.Type.XMLA)) {

                // Init the class loader
                Class.forName(Olap4jDataSource.XMLA_DRIVER_CLASS_NAME);
                
                // Build the JDBC URL for an XMLA connection.
                StringBuilder url = new StringBuilder("jdbc:xmla:");
                url.append("Server=").append(dataSource.getXmlaServer()); // FIXME This requires validation. Should be performed with the other ones identified higher up in this function.
                
                // Establish the connection
                Connection conn = DriverManager.getConnection(url.toString());
                OlapConnection olapConn = ((OlapWrapper) conn).unwrap(OlapConnection.class);
                
                return olapConn;
                
            } else {
                throw new RuntimeException("Someone forgot to add a connection type handler in the code.");
            }
        } finally {
            ctx.unbind(uniqueName);
        }
    }
    
    /**
     * This method will close all of the open connections in the pool.
     */
    public void disconnect() throws SQLException  {
        if (!con.isClosed()) {
            con.close();
        }
    }
    
    public Olap4jDataSource getDataSource() {
        return dataSource;
    }
    
}
