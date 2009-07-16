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
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.mdx.ParseTreeWriter;
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
import org.olap4j.query.Selection;
import org.olap4j.query.QueryDimension.SortOrder;
import org.olap4j.query.Selection.Operator;
import org.xml.sax.Attributes;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.xml.XMLHelper;

/**
 * This is the model of an OLAP query. This will store all values that need to be persisted
 * in an OLAP query.
 */
public class OlapQuery extends AbstractWabitObject {
    
    private static final Logger logger = Logger.getLogger(OlapQuery.class);
    
    /**
     * This will create a copy of the query.
     */
    public OlapQuery createCopyOfSelf() throws SQLException {
        OlapQuery newQuery = new OlapQuery(olapMapping);
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
     * Memorizes the saved XML structure for last minute load.
     */
    private List<String> rootNodes = new ArrayList<String>();
    
    private List<Map<String,String>> attributes = new ArrayList<Map<String,String>>();

    /**
     * This mapping is used to get an OLAP connection based on an
     * {@link Olap4jDataSource}. This allows us to reuse the same connection and
     * reduced the number of times an object gets cached. The connections in
     * this mapping should not be closed as they may be used by other objects.
     */
    private final OlapConnectionMapping olapMapping;
    
    /**
     * A List of {@link OlapQueryListener} objects that will be listening to
     * changes to this OlapQuery instance. Currently, it is primarily used
     * to notify listeners when the query has been executed.
     */
    private final List<OlapQueryListener> listeners = new ArrayList<OlapQueryListener>();
    
    /**
     * Creates a new, empty query with no set persistent object ID.
     */
    public OlapQuery(OlapConnectionMapping olapMapping) {
        this(null, olapMapping);
    }

    /**
     * Creates a new, empty query that will use the given persistent object ID
     * when it's saved. This constructor is only of particular use to the
     * persistence layer.
     */
    public OlapQuery(String uuid, OlapConnectionMapping olapMapping) {
        super(uuid);
        this.olapMapping = olapMapping;
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
	 * Executes the current MDX query represented by this object, returning the
	 * cell set that results from the query's execution.
	 * <p>
	 * If this query has not been modified at all since it was last executed,
	 * this method may return a cached reference to the same cell set as the
	 * last one it returned. Olap4j CellSet instances can be safely used from
	 * multiple threads.
	 * <p>
	 * TODO discuss thread safety of this operation
	 * 
	 * @return The {@link CellSet} result of the execution of the query. If the
	 *         query has no dimensions in either it's row or column axis
	 *         however, it will not be able to execute, in which case it returns
	 *         null.
	 * @throws OlapException
	 *             If there was a database error
	 */
    public CellSet execute() throws OlapException {
        logger.debug("Executing MDX query...");
        // TODO execute the textual query if there is one
        CellSet cellSet = null;
        if (getRowHierarchies().size() > 0 &&
        		getColumnHierarchies().size() > 0) {
        	cellSet = getMDXQuery().execute();
        }
        fireQueryExecuted(cellSet);
		return cellSet;
    }
    
    private void fireQueryExecuted(CellSet cellSet) {
    	for (OlapQueryListener listener: listeners) {
    		listener.queryExecuted(new OlapQueryEvent(cellSet));
    	}
	}
    
    private void fireQueryReset() {
    	for (OlapQueryListener listener: listeners) {
    		listener.queryReset();
    	}
	}

	/**
	 * Removes the given {@link Hierarchy} from the given {@link Axis} in the
	 * query
	 * 
	 * @param hierarchy
	 *            The {@link Hierarchy} to remove
	 * @param axis
	 *            The {@link Axis} to remove the {@link Hierarchy} from
	 */
	public void removeHierarchy(Hierarchy hierarchy, Axis axis) {
        QueryAxis qa = mdxQuery.getAxis(axis);
        QueryDimension qd = mdxQuery.getDimension(hierarchy.getDimension().getName());
        if (qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
        	qd.clearExclusions();
            qa.removeDimension(qd);
            hierarchiesInUse.remove(qd);
        }
    }
    
    /**
     * Replaces the current olap4j query with the given one, and fires a
     * property change event.
     * 
     * @param mdxQuery The new query. 
     */
    private void setMdxQuery(Query mdxQuery) {
        Query oldMDXQuery = this.mdxQuery;
        this.mdxQuery = mdxQuery;
        this.currentCube = this.mdxQuery.getCube();
        firePropertyChange("mdxQuery", oldMDXQuery, mdxQuery);
    }

    private Query getMdxQueryCopy() throws SQLException {
        this.init();
        Query copyQuery = OlapUtils.copyMDXQuery(mdxQuery);
        return copyQuery;
    }

    private Query getMDXQuery() {
        this.init();
        return mdxQuery;
    }

	/**
	 * This function is called by the 'Reset Query' button on the toolbar. It
	 * will replace the current MDX Query with a blank one.
	 */
    public void reset() {
        if (mdxQuery != null) {
	        for (Map.Entry<Axis, QueryAxis> axisEntry : mdxQuery.getAxes().entrySet()) {
	        	for (Iterator<QueryDimension> i = axisEntry.getValue().getDimensions().iterator(); i.hasNext(); ) {
	                QueryDimension dimension = i.next();
	        		dimension.clearInclusions();
	        		dimension.clearExclusions();
	        		hierarchiesInUse.remove(dimension);
	        		i.remove();
	            }
	        }
        }
        fireQueryReset();
    }

	/**
	 * Adds the given {@link Member} or it's children (depending on the
	 * {@link Operator}) as an exclusion to this query.
	 * 
	 * @param dimensionName
	 *            The name of the Dimension to exclude Member from.
	 * @param memberToExclude
	 *            The Member to exclude from the query results
	 * @param operator
	 *            If value is {@link Operator#MEMBER}, the given Member is
	 *            excluded. If the value is {@link Operator#CHILDREN}, then the
	 *            Member's children get excluded. Due to insufficient
	 *            documentation in the Olap4j API, I am not certain what the
	 *            other values of {@link Operator} will do.
	 */
    public void excludeMember(String dimensionName, Member memberToExclude, Selection.Operator operator) {
        this.mdxQuery.getDimension(dimensionName).exclude(operator, memberToExclude);
    }

    public OlapConnection createOlapConnection()
    throws SQLException, ClassNotFoundException, NamingException {
        return olapMapping.createConnection(getOlapDataSource());
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
                queryAxis.addDimension(queryDimension);
            } else if (this.rootNodes.get(cpt).equals("olap4j-selection")
                    || this.rootNodes.get(cpt).equals("olap4j-report-selection")) {
                String operation = entry.get("operator");
                Member actualMember = findMember(entry, getCurrentCube());
                queryDimension.include(Operator.valueOf(operation), actualMember);
                
                // Not optimal to do this for every selection, but we're not recording
                // the hierarchy with the <dimension> element.
                hierarchiesInUse.put(queryDimension, actualMember.getHierarchy());
                
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
     * If the member is currently "expanded" (its children are part of the MDX
     * query), its children will be removed from the query. Otherwise (the
     * member's children are not showing), the member's children will be added
     * to the query. In either case, the query will be re-executed after the
     * member selections have been adjusted.
     * 
     * @param member The member whose drilldown state to toggle. Must not be null.
     * @throws OlapException if the list of child members can't be retrieved
     */
    public void toggleMember(Member member) {
        Dimension d = member.getDimension();
        QueryDimension qd = mdxQuery.getDimension(d.getName());
        boolean wasExpanded = false;
        for (Iterator<Selection> it = qd.getInclusions().iterator(); it.hasNext(); ) {
            Selection s = it.next();
            if (member.equals(s.getMember())) {
                Operator operator = s.getOperator();
                if (operator == Operator.CHILDREN || operator == Operator.INCLUDE_CHILDREN) {
                    
                    // XXX query model docs now say not to do this,
                    // but there is no other way in the API
                    it.remove();
                    
                    wasExpanded = true;
                }
            }
        }
        
        if (!wasExpanded) {
            qd.include(Operator.CHILDREN, member);
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

    /**
     * Returns the current MDX text that this query object's state represents.
     */
    public String getMdxText() {
        // TODO synchronization
        StringWriter sw = new StringWriter();
        ParseTreeWriter ptw = new ParseTreeWriter(new PrintWriter(sw));
        getMDXQuery().getSelect().unparse(ptw);
        return sw.toString();
    }

	/**
	 * Adds the given member to the given axis.
	 * 
	 * @param ordinal
	 *            If the given member's dimension is not already on the given
	 *            axis, the dimension will be added to the axis at the given
	 *            ordinal. Otherwise, this argument is ingored.
	 * @param member
	 *            The member to add. If the member's dimension is not already on
	 *            the given axis, it will be added to the axis automatically.
	 * @param axis
	 *            The axis to add the member to. Must be either Axis.ROWS or
	 *            Axis.COLUMNS.
	 * @throws OlapException
	 *             If a database error occurs
	 */
    public void addToAxis(int ordinal, Member member, Axis axis) throws OlapException {
        QueryAxis qa = mdxQuery.getAxis(axis);
        QueryDimension qd = mdxQuery.getDimension(member.getDimension().getName());
        if (!qa.equals(qd.getAxis())) {
            logger.debug("Moving dimension " + qd + " to Axis " + qa);
            qa.addDimension(qd);
        }

        hierarchiesInUse.put(qd, member.getHierarchy());
        qd.include(Operator.MEMBER, member);
        toggleMember(member);
        qd.setSortOrder(SortOrder.ASC);
    	execute();
    }

    public List<Hierarchy> getRowHierarchies() {
        return getHierarchies(Axis.ROWS);
    }

    public List<Hierarchy> getColumnHierarchies() {
        return getHierarchies(Axis.COLUMNS);
    }

    /**
     * MDX only allows members from one hierarchy at a time, but the query model
     * currently does not enforce that. Until Hierarchy can be added to the
     * QueryDimension class, we'll maintain this mapping here to associate
     * Dimensions with Hierarchies.
     */
    private final Map<QueryDimension, Hierarchy> hierarchiesInUse =
        new HashMap<QueryDimension, Hierarchy>();
    
    private List<Hierarchy> getHierarchies(Axis axis) {
    	if (mdxQuery == null) return Collections.emptyList();
        QueryAxis qa = mdxQuery.getAxis(axis);
        List<Hierarchy> selectedHierarchies = new ArrayList<Hierarchy>();
        for (QueryDimension qd : qa.getDimensions()) {
            Hierarchy h = hierarchiesInUse.get(qd);
            assert h != null : qd + " not in " + hierarchiesInUse;
            selectedHierarchies.add(h);
        }
        return selectedHierarchies;
    }

    /**
     * Absorbs the high-level changes in the given query into this query.
     * Specifically, for each axis, the set of hierarchies selected in this
     * query will be updated to match those in the given olapQuery. This can
     * entail removing some hierarchies from this query and adding others.
     * Hierarchies that exist on the same axis in both this query and the given
     * query will retain the state they had in this query prior to the absorb
     * operation. Those hierarchies that were added to an axis of this query
     * will have the same selections as they do in the given olapQuery.
     * 
     * @param olapQuery
     *            The query to absorb into this one. It will not be modified.
     *            Must not be null.
     */
    public void absorb(OlapQuery olapQuery) {
        for (Map.Entry<Axis, QueryAxis> axisEntry : olapQuery.mdxQuery.getAxes().entrySet()) {
            for (QueryDimension dimension : axisEntry.getValue().getDimensions()) {
                for (QueryDimension oldDimension : olapQuery.mdxQuery.getAxes().get(axisEntry.getKey()).getDimensions()) {
                    if (dimension.getDimension().equals(oldDimension.getDimension())) {
                        dimension.getInclusions().clear();
                        for (Selection selection : oldDimension.getInclusions()) {
                            dimension.getInclusions().add(selection);
                        }
                    }
                }
            }
        }
    }

    /**
     * Modifies this query's selection so that members equal to and descended
     * from the given member remain selected as before, but other members are no
     * longer selected.
     * 
     * @param member
     *            The member to drill replace on. This member will be the new
     *            root of the selection for its hierarchy.
     */
    public void drillReplace(Member member) {
        QueryDimension qd = findQueryDimension(member);
        for (Iterator<Selection> it = qd.getInclusions().iterator(); it.hasNext(); ) {
            Selection s = it.next();
            Member victim = s.getMember();
            if (!OlapUtils.isDescendantOrEqualTo(member, victim)) {
                it.remove();
            }
        }
    }

    private QueryDimension findQueryDimension(Member member) {
        Dimension d = member.getDimension();
        QueryDimension qd = mdxQuery.getDimension(d.getName());
        assert qd != null : d + " not in query!?";
        return qd;
    }

    /**
     * Includes the given member in the query, assuming its hierarchy is on a
     * visible axis.
     * 
     * @param member
     *            The member to include in the query. If the member is already
     *            included, this method has no effect.
     */
    public void includeMember(Member member) {
        QueryDimension qd = findQueryDimension(member);
        qd.include(member);
    }
    
    public void addOlapQueryListener(OlapQueryListener listener) {
    	listeners.add(listener);
    }
    
    public void removeOlapQueryListener(OlapQueryListener listener) {
    	listeners.remove(listener);
    }
}
