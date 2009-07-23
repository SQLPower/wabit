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
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Schema;
import org.olap4j.metadata.Member.Type;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.olap4j.query.QueryDimension.HierarchizeMode;
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
     * This is the query name given to all Olap4j queries in this class.
     */
    private static final String OLAP4J_QUERY_NAME = "GUI Query";
    
    /**
     * This will create a copy of the query.
     * @throws QueryInitializationException 
     */
    public OlapQuery createCopyOfSelf() throws SQLException, QueryInitializationException {
    	OlapQuery newQuery = new OlapQuery(olapMapping);
    	newQuery.setOlapDataSource(this.getOlapDataSource());
        if (hasCachedXml()) {
        	for (int i = 0; i < this.rootNodes.size(); i++) {
        		newQuery.appendElement(
        				this.rootNodes.get(i), this.attributes.get(i));
        	}
        } else {
        	newQuery.setNonEmpty(isNonEmpty());
        	newQuery.setCurrentCube(mdxQuery.getCube());
        	newQuery.setMdxQuery(this.getMdxQueryCopy());
        }
        newQuery.setName(getName());
        return newQuery;
    }
    
    /**
     * The current query. Gets replaced whenever a new cube is selected via
     * {@link #setCurrentCube(Cube)}. This variable should only be accessed
     * through {@link #getMDXQuery()} to ensure that it is properly initialised.
     * However init() can access this function directly because {@link #getMDXQuery()}
     * calls init() and this would cause an infinite loop.
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
     * This boolean tracks if the ROWS axis of this query omits empty positions.
     * This is used when the mdxQuery is null.
     */
    private boolean nonEmpty = false;
    
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
				setMdxQuery(new Query(OLAP4J_QUERY_NAME, currentCube));
            } catch (Exception e) {
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
	 * @throws QueryInitializationException 
	 */
    public CellSet execute() throws OlapException, QueryInitializationException {
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
    
	/**
	 * Removes the given {@link Hierarchy} from the given {@link Axis} in the
	 * query
	 * 
	 * @param hierarchy
	 *            The {@link Hierarchy} to remove
	 * @param axis
	 *            The {@link Axis} to remove the {@link Hierarchy} from
	 * @throws QueryInitializationException 
	 */
	public void removeHierarchy(Hierarchy hierarchy, Axis axis) throws QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(hierarchy.getDimension().getName());
        if (qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
        	qd.clearExclusions();
            qa.removeDimension(qd);
            hierarchiesInUse.remove(qd);
        }
    }
    
    /**
     * Replaces the current olap4j query with the given one, and fires a
     * property change event. The query passed in should not be null.
     * 
     * @param mdxQuery The new query. 
     * @throws OlapException 
     */
    private void setMdxQuery(Query mdxQuery) throws OlapException {
    	if (mdxQuery == null) throw new NullPointerException();
        this.mdxQuery = mdxQuery;
        mdxQuery.getAxis(Axis.ROWS).setNonEmpty(nonEmpty);
        try {
			this.currentCube = this.getMDXQuery().getCube();
			execute();
		} catch (QueryInitializationException e) {
			throw new AssertionError("The initialization of the MDX query failed when an Olap4j " +
					"Query object was specified. The initialization method should not do anything " +
					"in this case.");
		}
    }

    private Query getMdxQueryCopy() throws SQLException, QueryInitializationException {
        Query copyQuery = OlapUtils.copyMDXQuery(getMDXQuery());
        return copyQuery;
    }

	/**
	 * This is and must remain a package private method. The modifier on this
	 * method should not be changed in order to maintain our encapsulation of
	 * the Olap4jQuery object. This is used within the OlapQuery object in order
	 * to modify it and should not be used outside the class. This is only used
	 * in saving and loading outside the class because in order to save an
	 * Olap4jQuery we need to iterate through the query. The init() function should
	 * not use this method because this method calls init(), init is allowed to access
	 * the mdxQuery directly.
	 * 
	 * @return Returns the Olap4jQuery model for saving and loading.
	 * @throws QueryInitializationException
	 *             Thrown if any problem occurrs while initializing the query.
	 */
    Query getMDXQuery() throws QueryInitializationException { //Do not change my modifier, i am package private so the Olap4jQuery doesn't leak out
        this.init();
        return mdxQuery;
    }

    /**
     * This method lets you know whether or not the OlapQuery has been initialized.
     * 
     * @return
     * 		The boolean value that is responsible for tracking whether or not the
     * 		OlapQuery has been initialized.
     */
    public boolean isInitDone() {
    	return initDone;
    }

	/**
	 * This function is called by the 'Reset Query' button on the toolbar. It
	 * will replace the current MDX Query with a blank one.
	 * @throws SQLException 
	 */
    public void reset() throws SQLException {
        try {
			setMdxQuery(new Query(OLAP4J_QUERY_NAME, getCurrentCube()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	 * @throws QueryInitializationException 
	 */
    public void excludeMember(String dimensionName, Member memberToExclude, Selection.Operator operator) throws QueryInitializationException {
        this.getMDXQuery().getDimension(dimensionName).exclude(operator, memberToExclude);
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
    
    /**
     * This method will initialize the query based upon which data was loaded. This is necessary
     * because if we were to initialize the query during saving and loading exceptions will be thrown
     * which will corrupt the workspace and performance will be bad. All exceptions in this method
     * will be wrapped in a QueryInitializationException, including runtime exceptions, as methods
     * calling init should be aware of any errors with intialization.
     */
    private void init() throws QueryInitializationException {
        if (!this.wasLoadedFromXml || this.initDone || this.mdxQuery!=null) return;
        
        Query localMDXQuery = null;
        try {
        	QueryAxis queryAxis = null;
        	QueryDimension queryDimension = null;

        	for (int cpt = 0; cpt < this.rootNodes.size(); cpt++) {

        		Map<String,String> entry = this.attributes.get(cpt);

        		if (this.rootNodes.get(cpt).equals("olap-cube")) {
        			String catalogName = entry.get("catalog");
        			String schemaName = entry.get("schema");
        			String cubeName = entry.get("cube-name");

        			if (getOlapDataSource() == null) {
        				throw new QueryInitializationException("Missing database for cube " + cubeName + " for use in " + getName() + ".");
        			}
        			try {
        				OlapConnection createOlapConnection = createOlapConnection();
        				Catalog catalog = createOlapConnection.getCatalogs().get(catalogName);
        				Schema schema = catalog.getSchemas().get(schemaName);
        				Cube cube = schema.getCubes().get(cubeName);
        				this.currentCube = cube; // We don't like firing property changes which wipe out the query
        			} catch (Exception e) {
        				throw new QueryInitializationException("Cannot connect to " + getOlapDataSource(), e);
        			}
        		} else if (this.rootNodes.get(cpt).equals("olap4j-query")) {
        			String queryName = entry.get("name");
        			try {
        			    localMDXQuery = new Query(queryName, getCurrentCube());
        			} catch (SQLException e) {
        				throw new QueryInitializationException(e);
        			}
        		} else if (this.rootNodes.get(cpt).equals("olap4j-axis")) {
        			String ordinalNumber = entry.get("ordinal");
        			Axis axis = Axis.Factory.forOrdinal(Integer.parseInt(ordinalNumber));
					queryAxis = localMDXQuery.getAxes().get(axis);
        			if (entry.get("non-empty") != null) {
        			    setNonEmpty(Boolean.parseBoolean(entry.get("non-empty")));
        			}
        		} else if (this.rootNodes.get(cpt).equals("olap4j-dimension")) {
        			String dimensionName = entry.get("dimension-name");
        			queryDimension =  localMDXQuery.getDimension(dimensionName);
        			queryDimension.setHierarchizeMode(HierarchizeMode.PRE);
        			queryAxis.addDimension(queryDimension);
        		} else if (this.rootNodes.get(cpt).equals("olap4j-selection")) {
        			String operation = entry.get("operator");
        			Member actualMember = findMember(entry, getCurrentCube());
        			if (actualMember == null) {
        			    throw new QueryInitializationException("Could not find member " + entry.get("unique-member-name") + " in the cube" + (getCurrentCube() != null?" " + getCurrentCube().getName():"") + ".");
        			}
        			queryDimension.include(Operator.valueOf(operation), actualMember);

        			// Not optimal to do this for every selection, but we're not recording
        			// the hierarchy with the <dimension> element.
        			hierarchiesInUse.put(queryDimension, actualMember.getHierarchy());
        		} else if (this.rootNodes.get(cpt).equals("olap4j-exclusion")) {
        			String operation = entry.get("operator");
        			Member actualMember = findMember(entry, getCurrentCube());
        			queryDimension.exclude(Operator.valueOf(operation), actualMember);

        			// Not optimal to do this for every selection, but we're not recording
        			// the hierarchy with the <dimension> element.
        			hierarchiesInUse.put(queryDimension, actualMember.getHierarchy());
        		} else if (this.rootNodes.get(cpt).startsWith("/")) {
        			// we can safely ignore end tags here.
        		} else {
        			throw new QueryInitializationException("Missing element parsing code for element name :".concat(this.rootNodes.get(cpt)));
        		}
        	}
        } catch (RuntimeException e) {
        	throw new QueryInitializationException(e);
        }
        try {
			setMdxQuery(localMDXQuery);
		} catch (OlapException e) {
			throw new QueryInitializationException(e);
		}
        this.initDone = true;
    }
    
    public void appendElement(String elementName, Attributes attributes) {
        Map<String,String> attributesMap = new HashMap<String, String>();
        for (int cpt = 0; cpt < attributes.getLength(); cpt++) {
            attributesMap.put(attributes.getQName(cpt), attributes.getValue(cpt));
        }
        appendElement(elementName, attributesMap);
    }
    
    public void appendElement(String elementName, Map<String,String> attributesMap) {
    	wasLoadedFromXml = true;
        rootNodes.add(elementName);
        attributes.add(attributesMap);
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
     * @throws QueryInitializationException 
     * @throws OlapException if the list of child members can't be retrieved
     */
    public void toggleMember(Member member) throws QueryInitializationException {
        Dimension d = member.getDimension();
        QueryDimension qd = getMDXQuery().getDimension(d.getName());
        boolean wasExpanded = false;
        for (Iterator<Selection> it = qd.getInclusions().iterator(); it.hasNext(); ) {
            Selection s = it.next();
            if (OlapUtils.isDescendant(member, s.getMember()) || 
            		(member.equals(s.getMember()) && (s.getOperator() == Operator.CHILDREN || s.getOperator() == Operator.INCLUDE_CHILDREN))) {
                // XXX query model docs now say not to do this,
                // but there is no other way in the API
                it.remove();
                wasExpanded = true;
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
        if (this.wasLoadedFromXml) {
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
     * @throws QueryInitializationException 
     */
    public String getMdxText() throws QueryInitializationException {
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
	 * @throws QueryInitializationException 
	 */
    public void addToAxis(int ordinal, Member member, Axis axis) throws OlapException, QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(member.getDimension().getName());
        logger.debug("Moving dimension " + qd.getName() + " to Axis " + qa.getName() + " in ordinal " + ordinal);
        if (!qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
			// qa.addDimension(qd);
			// This way doesn't fire events like addDimension does, but I see
			// currently no other way to add a dimension at a given index
            qa.getDimensions().add(ordinal, qd);
        } else {
        	int index = qa.getDimensions().indexOf(qd);
        	if (index >= 0) {
        		qa.getDimensions().remove(index);
        		if (index < ordinal) {
        			ordinal--;
        		}
        		qa.getDimensions().add(ordinal, qd);
        	}
        }
        hierarchiesInUse.put(qd, member.getHierarchy());
        if (!isIncluded(member)) {
        	qd.include(Operator.MEMBER, member);
        }
        Type memberType = member.getMemberType();
        logger.debug("memberType = " + memberType);
		if (!(member instanceof Measure)) {
			qd.setHierarchizeMode(HierarchizeMode.PRE);
        }
    	execute();
    }

    public List<Hierarchy> getRowHierarchies() throws QueryInitializationException {
        return getHierarchies(Axis.ROWS);
    }

    public List<Hierarchy> getColumnHierarchies() throws QueryInitializationException {
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
    
    private List<Hierarchy> getHierarchies(Axis axis) throws QueryInitializationException {
    	if (getMDXQuery() == null) return Collections.emptyList();
        QueryAxis qa = getMDXQuery().getAxis(axis);
        List<Hierarchy> selectedHierarchies = new ArrayList<Hierarchy>();
        for (QueryDimension qd : qa.getDimensions()) {
            Hierarchy h = hierarchiesInUse.get(qd);
            assert h != null : qd + " not in " + hierarchiesInUse;
            selectedHierarchies.add(h);
        }
        return selectedHierarchies;
    }

    /**
     * Modifies this query's selection so that members equal to and descended
     * from the given member remain selected as before, but other members are no
     * longer selected.
     * 
     * @param member
     *            The member to drill replace on. This member will be the new
     *            root of the selection for its hierarchy.
     * @throws QueryInitializationException 
     */
    public void drillReplace(Member member) throws QueryInitializationException {
        QueryDimension qd = findQueryDimension(member);
        for (Iterator<Selection> it = qd.getInclusions().iterator(); it.hasNext(); ) {
            Selection s = it.next();
            Member victim = s.getMember();
            logger.debug("member = " + member.getName() + ", victim = " + victim.getName());
            if (!OlapUtils.isDescendantOrEqualTo(member, victim)) {
            	if (OlapUtils.isChild(victim, member) && 
            			(s.getOperator() == Operator.CHILDREN)) {
					// If the member is there as one of its parent's children
					// rather than itself, then we need to replace the selection
					// of children with itself in the inclusions
            		qd.include(member);
            	}
                it.remove();
            }
        }
    }

	/**
	 * Modifies this query's selection so that targetAncestor, and every
	 * ancestor of fromMember in between will be included. If targetAncestor
	 * isn't actually an ancestor, it returns right away.
	 * 
	 * @param fromMember
	 *            The member whose ancestors will be added to the query
	 *            selection
	 * @param targetAncestor
	 *            The highest ancestor member that will be added to the query
	 *            selection
	 * @throws QueryInitializationException
	 */
    public void drillUpTo(Member fromMember, Member targetAncestor) throws QueryInitializationException {
    	// is targetAncestor REALLY an ancestor?
    	if (!OlapUtils.isDescendant(targetAncestor, fromMember)) return;
    	
    	Member member = fromMember;
    	while (member.getParentMember() != null && !member.equals(targetAncestor)) {
			member = member.getParentMember();
			if (!isIncluded(member)) {
				includeMember(member);
			}
		}
    }
    
	/**
	 * Checks if the given {@link Member} is already included in the given
	 * {@link QueryDimension}.
	 * 
	 * Note that this currently does not take into account the value of
	 * {@link Selection#getOperator()}
	 * 
	 * @param member
	 *            The member to check if it's included in the given
	 *            QueryDimension
	 * @param dimension
	 *            The QueryDimension to check for inclusion.
	 * @return True if member is in one of the {@link Selection}s in dimension's
	 *         inclusions. Otherwise false.
	 */
    public boolean isIncluded(Member member) throws QueryInitializationException {
    	QueryDimension dimension = findQueryDimension(member);
    	for (Selection s: dimension.getInclusions()) {
    		if (s.getMember().equals(member)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private QueryDimension findQueryDimension(Member member) throws QueryInitializationException {
        Dimension d = member.getDimension();
        QueryDimension qd = getMDXQuery().getDimension(d.getName());
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
     * @throws QueryInitializationException 
     */
    public void includeMember(Member member) throws QueryInitializationException {
        QueryDimension qd = findQueryDimension(member);
        qd.include(member);
    }

    /**
     * Sets the ROWS axis of this query to omit empty positions.
     * 
     * @param nonEmpty
     *            True means to omit empty positions; false means to include
     *            them.
     */
    public void setNonEmpty(boolean nonEmpty) {
    	this.nonEmpty = nonEmpty;
    	if (mdxQuery != null) {
    		mdxQuery.getAxis(Axis.ROWS).setNonEmpty(nonEmpty);
    		logger.debug("Query has rows non-empty? " + mdxQuery.getAxis(Axis.ROWS).isNonEmpty());
    	}
    }

    /**
     * Tells whether the ROWS axis of this query omits empty positions.
     * 
     * @return True is this query omits empty rows; false if it includes them.
     */
    public boolean isNonEmpty() {
    	return nonEmpty;
    }
    
    public void addOlapQueryListener(OlapQueryListener listener) {
    	listeners.add(listener);
    }
    
    public void removeOlapQueryListener(OlapQueryListener listener) {
    	listeners.remove(listener);
    }

    /**
     * Takes the given Hierarchy and clears all exclusions from its Dimension.
     * @param hierarchy
     * @throws QueryInitializationException
     */
	public void clearExclusions(Hierarchy hierarchy) throws QueryInitializationException {
		QueryDimension dimension = getMDXQuery().getDimension(hierarchy.getDimension().getName());
		dimension.clearExclusions();
	}
}
