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
import java.util.Map;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;
import org.olap4j.metadata.Cube;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;

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

//    /**
//     * If there is no connection to the database this class will be used to
//     * assure that the user does not lose any of their work. It will store all
//     * the data that is normally stored when a workspace is saved. This is all
//     * because we cannot create an {@link Cube} and {@link Query} when there is
//     * no {@link Olap4jDataSource} to connect to.
//     */
//    public class SavedQueryData { 
//        
//        public class SavedMemberData {
//            private String name;
//            private String operation;
//
//            public String getName() {
//                return name;
//            }
//            public void setName(String name) {
//                this.name = name;
//            }
//            public String getOperation() {
//                return operation;
//            }
//            public void setOperation(String operation) {
//                this.operation = operation;
//            }
//        }
//        
//        private String catalogName;
//        private String schemaName;
//        private String cubeName;
//        
//        private Map<String, Map<String, TreeSet<SavedMemberData>>> axisData;
//        
//        
//        public Map<String, Map<String, SavedMemberData>> getAxisData() {
//            return axisData;
//        }
//
//        public void setAxisData(Map<String, Map<String, SavedMemberData>> axisData) {
//            this.axisData = axisData;
//        }
//
//        public void setCatalogName(String catalogName) {
//            this.catalogName = catalogName;
//        }
//        
//        public String getCatalogName() {
//            return catalogName;
//        }
//
//        public void setSchemaName(String schemaName) {
//            this.schemaName = schemaName;
//        }
//
//        public String getSchemaName() {
//            return schemaName;
//        }
//
//        public void setCubeName(String cubeName) {
//            this.cubeName = cubeName;
//        }
//
//        public String getCubeName() {
//            return cubeName;
//        }
//        
//    }
//    
//    /**
//     * This allows us to persist all the data a user might have
//     * for an {@link Query} in case a user does not have a database connection
//     */
//    private SavedQueryData savedQueryData;
    
    /**
     * This will create a copy of the query.
     */
    public static OlapQuery createCopy(OlapQuery copy) throws SQLException {
        OlapQuery newQuery = new OlapQuery();
        newQuery.setCurrentCube(copy.getCurrentCube());
        newQuery.setOlapDataSource(copy.getOlapDataSource());
        newQuery.setMdxQuery(copy.getMdxQueryCopy());
        return newQuery;
    }
    
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
        this(null);
    }
    
    public OlapQuery(String uuid) {
        super(uuid);
        // FIXME this should be configured in an external jndi.properties file.
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
        Query copyQuery = OlapUtils.copyMDXQuery(mdxQuery);
        return copyQuery;
    }

    /**
     * This getter should only be used in saving and loading. Modifying the
     * query returned here will not fire property change events which will cause
     * parts of Wabit to not be notified of changes to the query and not update
     * accordingly.
     */
    public Query getMDXQuery() {
        return mdxQuery;
    }

	/**
	 * This function is called by the 'Reset Query' button on the toolbar. It
	 * will replace the current MDX Query with a blank one.
	 */
    public void resetMDXQuery() {
        if (mdxQuery == null) return;
        for (Map.Entry<Axis, QueryAxis> axisEntry : mdxQuery.getAxes().entrySet()) {
            for (QueryDimension dimension : axisEntry.getValue().getDimensions()) {
                dimension.getSelections().clear();
            }
        }
    }

    public OlapConnection createOlapConnection() throws SQLException, ClassNotFoundException, NamingException 
    {
        final String unique_name = UUID.randomUUID().toString();
        
        // FIXME This validation should not be performed here.
        if (getOlapDataSource() == null) return null;
        
        JDBCDataSource ds = olapDataSource.getDataSource();
        
        try {
            
            ctx.bind(unique_name, new DataSourceAdapter(ds));
            
            if (getOlapDataSource().getType().equals(Olap4jDataSource.Type.IN_PROCESS)) {
                if (getOlapDataSource().getMondrianSchema() == null
                        || getOlapDataSource().getDataSource() == null) {
                    // FIXME This validation should not be performed here.
                    return null;
                }
                
                // Init the class loader. This might not be necessary with JDK 1.6, but just for kicks....
                Class.forName(Olap4jDataSource.IN_PROCESS_DRIVER_CLASS_NAME);
                
                // Build a JDBC URL for Mondrian driver connection
                StringBuilder url = new StringBuilder("jdbc:mondrian:");
                url.append("DataSource='").append(unique_name);
                url.append("';Catalog=").append(getOlapDataSource().getMondrianSchema().toString());
                
                Connection connection = DriverManager.getConnection(url.toString());
                return ((OlapWrapper) connection).unwrap(OlapConnection.class);
                
            } else if (getOlapDataSource().getType().equals(Olap4jDataSource.Type.XMLA)) {

                // Init the class loader
                Class.forName(Olap4jDataSource.XMLA_DRIVER_CLASS_NAME);
                
                // Build the JDBC URL for an XMLA connection.
                StringBuilder url = new StringBuilder("jdbc:xmla:");
                url.append("Server=").append(getOlapDataSource().getXmlaServer()); // FIXME This requires validation. Should be performed with the other ones identified higher up in this function.
                
                // Establish the connection
                Connection conn = DriverManager.getConnection(url.toString());
                OlapConnection olapConn = ((OlapWrapper) conn).unwrap(OlapConnection.class);
                
                return olapConn;
                
            } else {
                throw new RuntimeException("Someone forgot to add a connection type handler in the code.");
            }
        } finally {
            ctx.unbind(unique_name);
        }
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
    
    @Override
    /**
     * Returns the name of the string. This way reasonable text appears when
     * these queries are placed in a combo box.
     */
    public String toString() {
        return getName();
    }
    
}
