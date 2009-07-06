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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapWrapper;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Schema;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection.Operator;
import org.xml.sax.Attributes;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.xml.XMLHelper;

/**
 * This is the model of an OLAP query. This will store all values that need to be persisted
 * in an OLAP query.
 */
public class OlapQuery extends AbstractWabitObject {
    
    /**
     * This will create a copy of the query.
     */
    public OlapQuery createCopyOfSelf() throws SQLException {
        OlapQuery newQuery = new OlapQuery();
        for (int mickey = 0; mickey < this.rootNodes.size(); mickey++) {
            newQuery.appendElement(
                    this.rootNodes.get(mickey), this.attributes.get(mickey));
        }
        newQuery.setOlapDataSource(this.getOlapDataSource());
        newQuery.setMdxQuery(this.getMdxQueryCopy());
        return newQuery;
    }
    
    /**
     * The current query. Gets replaced whenever a new cube is selected via
     * {@link #setCurrentCube(Cube)}.
     */
    private Query mdxQuery = null;
    
    private boolean initDone = false;
    
    private boolean wasLoadedFromXml = false;
    
    /**
     * The current cube (this can be selected/changed via the GUI or the
     * {@link #setCurrentCube(Cube)} method). Null by default.
     */
    private Cube currentCube;

    /**
     * This is the data source that this query obtains its connections from. 
     */
    private Olap4jDataSource olapDataSource;

    /**
     * This JNDI context is used as a temporary holding place during the process
     * of creating Olap4j connections.
     */
    private final Context ctx;
    
    /**
     * Memorizes the saved XML structure for last minute load.
     */
    private List<String> rootNodes = new ArrayList<String>();
    
    private List<Map<String,String>> attributes = new ArrayList<Map<String,String>>();

    /**
     * Creates a new, empty query with no set persistent object ID.
     */
    public OlapQuery() {
        this(null);
    }

    /**
     * Creates a new, empty query that will use the given persistent object ID
     * when it's saved. This constructor is only of particular use to the
     * persistence layer.
     */
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
        }
        
        firePropertyChange("currentCube", oldCube, currentCube);
    }

    public Cube getCurrentCube() {
        return currentCube;
    }

    /**
     * Replaces the current olap4j query with the given one, and fires a
     * property change event.
     * 
     * @param mdxQuery The new query. 
     */
    public void setMdxQuery(Query mdxQuery) {
        Query oldMDXQuery = this.mdxQuery;
        this.mdxQuery = mdxQuery;
        this.currentCube = this.mdxQuery.getCube();
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
        this.init();
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
        this.init();
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
        // XXX I believe this should fire an event.
    }

    public OlapConnection createOlapConnection()
    throws SQLException, ClassNotFoundException, NamingException {
        final String uniqueName = UUID.randomUUID().toString();
        
        // FIXME This validation should not be performed here.
        if (getOlapDataSource() == null) return null;
        
        JDBCDataSource ds = olapDataSource.getDataSource();
        
        try {
            
            ctx.bind(uniqueName, new DataSourceAdapter(ds));
            
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
                url.append("DataSource='").append(uniqueName);
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
            ctx.unbind(uniqueName);
        }
    }

    /**
     * Sets the data source that this query obtains its connections from,
     * and fires a property change event.
     */
    public void setOlapDataSource(Olap4jDataSource olapDataSource) {
        Olap4jDataSource oldDS = this.olapDataSource;
        this.olapDataSource = olapDataSource;
        firePropertyChange("olapDataSource", oldDS, olapDataSource);
    }

    /**
     * Returns the data source that this query obtains its connections from.
     */
    public Olap4jDataSource getOlapDataSource() {
        return olapDataSource;
    }

    /**
     * OlapQuery is a leaf node, so this method returns false.
     */
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    /**
     * OlapQuery is a leaf node, so this method returns an unmodifiable empty
     * list.
     */
    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }
    
    /**
     * Returns the name of the string. This way reasonable text appears when
     * these queries are placed in a combo box.
     */
    @Override
    public String toString() {
        return getName();
    }

    public List<WabitObject> getDependencies() {
        ArrayList<WabitObject> dependencies = new ArrayList<WabitObject>();
        
        // For now only the selected datasource is a dependency.
        if (getOlapDataSource()!=null) {
            dependencies.addAll(Collections.singleton(new WabitDataSource(getOlapDataSource())));
        }
        return dependencies;
    }
    
    private void init() {
        
        if (this.initDone || this.mdxQuery!=null) return;
        
        QueryAxis queryAxis = null;
        QueryDimension queryDimension = null;
        
        for (int cpt = 0; cpt < this.rootNodes.size(); cpt++) {
            
            Map<String,String> entry = this.attributes.get(cpt);
            
            if (this.rootNodes.get(cpt).equals("olap-cube")
                    || this.rootNodes.get(cpt).equals("olap-report-cube")) {
                String catalogName = entry.get("catalog");
                String schemaName = entry.get("schema");
                String cubeName = entry.get("cube-name");
                
                if (getOlapDataSource() == null) {
                    throw new RuntimeException("Missing database for cube " + cubeName + " for use in " + getName() + ".");
                }
                try {
                    OlapConnection createOlapConnection = createOlapConnection();
                    Catalog catalog = createOlapConnection.getCatalogs().get(catalogName);
                    Schema schema = catalog.getSchemas().get(schemaName);
                    Cube cube = schema.getCubes().get(cubeName);
                    this.currentCube = cube; // We don't like firing property changes which wipe out the query
                } catch (Exception e) {
                    throw new RuntimeException("Cannot connect to " + getOlapDataSource(), e);
                }
            } else if (this.rootNodes.get(cpt).equals("olap4j-query")
                    || this.rootNodes.get(cpt).equals("olap4j-report-query")) {
                String queryName = entry.get("name");
                try {
                    this.mdxQuery = new Query(queryName, getCurrentCube());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (this.rootNodes.get(cpt).equals("olap4j-axis")
                    || this.rootNodes.get(cpt).equals("olap4j-report-axis")) {
                String ordinalNumber = entry.get("ordinal");
                Axis axis = Axis.Factory.forOrdinal(Integer.parseInt(ordinalNumber));
                queryAxis = new QueryAxis(getMDXQuery(), axis);
                getMDXQuery().getAxes().put(axis, queryAxis);
            } else if (this.rootNodes.get(cpt).equals("olap4j-dimension")
                    || this.rootNodes.get(cpt).equals("olap4j-report-dimension")) {
                String dimensionName = entry.get("dimension-name");
                Dimension dimension = getCurrentCube().getDimensions().get(dimensionName);
                queryDimension = new QueryDimension(getMDXQuery(), dimension);
                queryAxis.getDimensions().add(queryDimension);
            } else if (this.rootNodes.get(cpt).equals("olap4j-selection")
                    || this.rootNodes.get(cpt).equals("olap4j-report-selection")) {
                String operation = entry.get("operator");
                Member actualMember = findMember(entry, getCurrentCube());
                queryDimension.select(Operator.valueOf(operation), actualMember);
            } else if (this.rootNodes.get(cpt).startsWith("/")) {
                // we can safely ignore end tags here.
            } else {
                throw new UnsupportedOperationException("Missing element parsing code for element name :".concat(this.rootNodes.get(cpt)));
            }
        }
        //this.setMdxQuery(mdxQuery);
        this.initDone = true;
    }
    
    public void appendElement(String elementName, Attributes attributes) {
        this.wasLoadedFromXml=true;
        Map<String,String> attributesMap = new HashMap<String, String>();
        for (int cpt = 0; cpt < attributes.getLength(); cpt++) {
            attributesMap.put(attributes.getQName(cpt), attributes.getValue(cpt));
        }
        this.appendElement(elementName, attributesMap);
    }
    
    public void appendElement(String elementName, Map<String,String> attributesMap) {
        this.rootNodes.add(elementName);
        this.attributes.add(attributesMap);
    }
    
    /**
     * This method finds a member from a cube based on given attributes.
     */
    public Member findMember(Map<String,String> attributes, Cube cube) {
        String uniqueMemberName = attributes.get("unique-member-name");
        if (uniqueMemberName != null) {
            String[] uniqueMemberNameList = uniqueMemberName.split("\\]\\.\\[");
            uniqueMemberNameList[0] = uniqueMemberNameList[0].substring(1); //remove starting [ bracket
            final int lastMemberNamePosition = uniqueMemberNameList.length - 1;
            uniqueMemberNameList[lastMemberNamePosition] = uniqueMemberNameList[lastMemberNamePosition].substring(0, uniqueMemberNameList[lastMemberNamePosition].length() - 1); //remove ending ] bracket
            try {
                return cube.lookupMember(uniqueMemberNameList);
            } catch (OlapException e) {
                throw new RuntimeException(e);
            }
            
        } else {
            String dimensionName = attributes.get("dimension-name");
            String hierarchyName = attributes.get("hierarchy-name");
            String levelName = attributes.get("member-level");
            String memberName = attributes.get("member-name");
            Dimension dimension = cube.getDimensions().get(dimensionName);
            Member actualMember = null;
            final Hierarchy hierarchy = dimension.getHierarchies().get(hierarchyName);
            final Level level = hierarchy.getLevels().get(levelName);
            try {
                for (Member member : level.getMembers()) {
                    if (member.getName().equals(memberName)) {
                        actualMember = member;
                        break;
                    }
                }
            } catch (OlapException e) {
                throw new RuntimeException(e);
            }
            if (actualMember == null) {
                throw new NullPointerException("Cannot find member " + memberName + " in hierarchy " + hierarchyName + " in dimension " + dimensionName);
            }
            return actualMember;
        }
    }
    
    /**
     * Tells if the connection was initialized.
     */
    public boolean hasCachedXml() {
        // Create a copy of the init flag so the object is immutable.
        return (!this.initDone && this.wasLoadedFromXml);
    }
    
    
    /**
     * Will return the cached XML code that was saved in the original workspace file
     * if and only if the object was no initialized or modified in the meantime.
     * @return XML representation of the object, null if it was initialized
     * and the cached XML code is not relevant anymore.
     */
    public void writeCachedXml(XMLHelper xml, PrintWriter out) {
        // If it is already initialized, return null.
        if (!this.initDone && this.wasLoadedFromXml) {
            for (int i = 0; i < this.rootNodes.size(); i++) {
                StringBuilder sb = new StringBuilder("");
                sb.append("<")
                    .append(this.rootNodes.get(i));
                for (Entry<String,String> attribute : this.attributes.get(i).entrySet()) {
                    sb.append(" ")
                        .append(attribute.getKey())
                        .append("=\"")
                        .append(attribute.getValue())
                        .append("\"");
                }
                sb.append(">");
                xml.println(out, sb.toString());
            }
        }
    }
}
