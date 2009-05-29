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

import java.net.URI;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;

/**
 * A class akin to {@link SPDataSource}, but for specifying the connection
 * parameters for an olap4j connection.
 * <p>
 * olap4j currently has two types of connections: Mondrian in-process, and XML/A
 * to a remote server. This data source allows either type of connection to be
 * specified.
 */
public class Olap4jDataSource {

    public static enum Type {
        IN_PROCESS, XMLA;
    }
    
    private JDBCDataSource dataSource;
    private URI mondrianSchema;
    private URI xmlaServer;
    private Type type;
    
    /**
     * Creates a data source initially configured for nothing.
     */
    public Olap4jDataSource() {
        // no op
    }
    
    /**
     * Creates a data source initially configured for in-process Mondrian.
     */
    public Olap4jDataSource(JDBCDataSource dataSource, URI mondrianSchema) {
        this.dataSource = dataSource;
        this.mondrianSchema = mondrianSchema;
        type = Type.IN_PROCESS;
    }
    
    /**
     * Creates a data source initially configured for connection to a remote XML/A server.
     */
    public Olap4jDataSource(URI xmlaServer) {
        this.xmlaServer = xmlaServer;
        type = Type.XMLA;
    }
    
    public JDBCDataSource getDataSource() {
        return dataSource;
    }
    public void setDataSource(JDBCDataSource dataSource) {
        this.dataSource = dataSource;
    }
    public URI getMondrianSchema() {
        return mondrianSchema;
    }
    public void setMondrianSchema(URI mondrianSchema) {
        this.mondrianSchema = mondrianSchema;
    }
    public URI getXmlaServer() {
        return xmlaServer;
    }
    public void setXmlaServer(URI xmlaServer) {
        this.xmlaServer = xmlaServer;
    }

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
}
