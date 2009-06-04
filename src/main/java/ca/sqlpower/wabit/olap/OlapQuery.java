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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;
import org.olap4j.metadata.Cube;
import org.olap4j.query.Query;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * This is the model of an OLAP query. This will store all values that need to be persisted
 * in an OLAP query.
 */
public class OlapQuery extends AbstractWabitObject {
    
    /**
     * The current query. Gets replaced whenever a new cube is selected via
     * {@link #setCurrentCube(Cube)}.
     */
    private Query mdxQuery;
    
    /**
     * The current cube (this can be selected/changed via the GUI or the
     * {@link #setCurrentCube(Cube)} method). Null by default.
     */
    private Cube currentCube;

    /**
     * This is the {@link SPDataSource} used to connect to the OLAP data source.
     */
    private Olap4jDataSource olapDataSource;
    
    private final Context ctx;
    
    public OlapQuery() {
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory");
        System.setProperty("org.osjava.sj.jndi.shared", "true");
        try {
            ctx = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setCurrentCube(Cube currentCube) {
        Cube oldCube = this.currentCube;
        this.currentCube = currentCube;
        
        if (currentCube != oldCube && currentCube != null) {
            try {
                setMdxQuery(new Query("GUI Query", currentCube));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (currentCube == null) {
            setMdxQuery(null);
        }
        
        firePropertyChange("currentCube", oldCube, currentCube);
    }

    public Cube getCurrentCube() {
        return currentCube;
    }

    public void setMdxQuery(Query mdxQuery) {
        Query oldMDXQuery = this.mdxQuery;
        this.mdxQuery = mdxQuery;
        firePropertyChange("mdxQuery", oldMDXQuery, mdxQuery);
    }

    /**
     * This returns a deep copy of the {@link Query} contained in this class.
     * This makes the {@link Query} in this class unmodifiable directly and 
     * forces users to set the query in this class after they make changes to 
     * the query if changes are necessary. The reason for forcing users of this
     * class to set the query after modifications is there is no way to add
     * listeners for changes to the {@link Query}. By setting a new query other
     * classes can be notified of changes.
     */
    public Query getMdxQueryCopy() throws SQLException {
        return OlapUtils.copyMDXQuery(mdxQuery);
    }

    public OlapConnection createOlapConnection() throws SQLException, ClassNotFoundException, NamingException {
        if (getOlapDataSource() == null 
                || getOlapDataSource().getDataSource() == null
                || getOlapDataSource().getMondrianSchema() == null) return null;
        
        JDBCDataSource ds = olapDataSource.getDataSource();
        ctx.bind(ds.getName(), new DataSourceAdapter(ds));
        
        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
        Connection connection =
            DriverManager.getConnection(
                "jdbc:mondrian:"
                    + "DataSource='" + ds.getName() + "';"
                    + "Catalog='" + getOlapDataSource().getMondrianSchema().toString() + "';"
                    );
        
        ctx.unbind(ds.getName());
        
        return ((OlapWrapper) connection).unwrap(OlapConnection.class);
    }

    public void setOlapDataSource(Olap4jDataSource olapDataSource) {
        Olap4jDataSource oldDS = this.olapDataSource;
        this.olapDataSource = olapDataSource;
        firePropertyChange("olapDataSource", oldDS, olapDataSource);
    }

    public Olap4jDataSource getOlapDataSource() {
        return olapDataSource;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return new ArrayList<WabitObject>();
    }

}
