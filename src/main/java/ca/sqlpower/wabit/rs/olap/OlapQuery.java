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

package ca.sqlpower.wabit.rs.olap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.mdx.SelectNode;
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
import org.olap4j.query.SortOrder;
import org.olap4j.query.QueryDimension.HierarchizeMode;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPSimpleVariableResolver;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.object.SPVariableResolverProvider;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.OlapConnectionProvider;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.rs.ResultSetHandle;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducerException;
import ca.sqlpower.wabit.rs.ResultSetProducerListener;
import ca.sqlpower.wabit.rs.ResultSetProducerSupport;
import ca.sqlpower.wabit.rs.WabitResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetHandle.ResultSetType;

/**
 * This is the model of an OLAP query. This will store all values that need to
 * be persisted in an OLAP query.
 * <p>
 * Instances of OlapQuery are thread safe: their state can me modified safely by
 * multiple threads. To avoid thrashing the remote database, execution of the
 * query is serialized so that only one MDX query evaluation at a time is in
 * progress. However, while the query is executing, threads attempting to
 * further modify the query's state are not blocked as long as they don't
 * involve an attempt to execute the query.
 * <p>
 * Sometimes, you might want to perform a series of operations (such as adding
 * or removing many members) atomically. You can do so by synchronizing on the
 * OlapQuery instance, like this:
 * <pre>
 *  OlapQuery query = ...;
 *  synchronized (query) {
 *    for (Member member : membersToInclude) {
 *      query.includeMember(member);
 *    }
 *  }
 * </pre>
 * This will guarantee all the members in the list have been added to the query
 * with no intervening modifications to the query and no possibility that the
 * query would start executing on another thread when only half the members have
 * been added.
 */
@ThreadSafe
public class OlapQuery extends AbstractWabitObject implements WabitResultSetProducer, SPVariableResolverProvider {
    
    private static final Logger logger = Logger.getLogger(OlapQuery.class);
    
    /**
     * This is the query name given to all Olap4j queries in this class.
     */
    private static final String OLAP4J_QUERY_NAME = "GUI Query";
    
    /**
     * This object will be passed to people who are interested in using
     * this olap query's variables.
     */
    private OlapVariableResolver variableProvider = null;
    
    private final class OlapVariableResolver extends SPSimpleVariableResolver {
    	
    	private AtomicBoolean updateNeeded = new AtomicBoolean(true);
    	private AtomicBoolean isUpdating = new AtomicBoolean(false);
    	
    	public OlapVariableResolver(SPObject owner, String namespace, String userFriendlyName) {
			super(owner, namespace, userFriendlyName);
		}
    	public void setUpdateNeeded(boolean updateNeeded) {
    		this.updateNeeded.set(updateNeeded);
    	}
    	protected void beforeLookups(String key) {
    		if (this.resolvesNamespace(SPVariableHelper.getNamespace(key))) {
    			this.updateVars();
    		}
    	}
    	protected void beforeKeyLookup(String namespace) {
    			this.updateVars();
    	}
		public void updateVars() {
			try {
				
				if (!this.updateNeeded.get()
						|| isUpdating.get()) return;
				
				isUpdating.set(true);
				
				ResultSetHandle handle = execute(
						new SPVariableHelper(OlapQuery.this),
						null,
						false);
				try {
					variables.clear();
					ResultSet rs = handle.getResultSet();
					if (rs != null &&
							rs.first()) {
				        do {
							for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
								store(rs.getMetaData().getColumnName(i+1), rs.getObject(i+1));
							}
						} while (rs.next());
					}
				} catch (SQLException e) {
					logger.error("Failed to resolve available variables from a query.", e);
				}
				
				this.updateNeeded.set(false);
				
			} catch (ResultSetProducerException e) {
				logger.error("Failed to resolve available variables from a query.", e);
			} finally {
				isUpdating.set(false);
			}
		}
    }
    
    /**
     * This is the member to filter by, since mondrian does not support compound
     * slicers this variable is needed to assure that our user is unable to add
     * more than one member to their slicer axis
     */
    @GuardedBy("this") private Member slicerMember = null;
    
    /**
     * Creates a copy of the given OlapQuery.
     */
    public static OlapQuery copyOlapQuery(OlapQuery oldOlapQuery) throws SQLException, 
            QueryInitializationException {
        synchronized (oldOlapQuery) {
            OlapQuery newQuery = 
            		new OlapQuery(
            				null, 
            				oldOlapQuery.olapMapping, 
            				oldOlapQuery.getName(), 
            				oldOlapQuery.getQueryName(), 
            				oldOlapQuery.getCatalogName(), 
            				oldOlapQuery.getSchemaName(), 
            				oldOlapQuery.getCubeName(),
            				oldOlapQuery.getModifiedOlapQuery(),
            				oldOlapQuery.actsAsVariableProvider);
            
            newQuery.setOlapDataSource(oldOlapQuery.getOlapDataSource());
            newQuery.setName(oldOlapQuery.getName());
            newQuery.setNonEmpty(oldOlapQuery.isNonEmpty());
            
            oldOlapQuery.updateAttributes();
            for (WabitOlapAxis axis : oldOlapQuery.getAxes()) {
            	newQuery.addAxis(new WabitOlapAxis(axis));
            }
            
            return newQuery;
        }
    }
    /**
     * The current query. Gets replaced whenever a new cube is selected via
     * {@link #setCurrentCube(Cube)}. This variable should only be accessed
     * through {@link #getMDXQuery()} to ensure that it is properly initialised.
     * However init() can access this function directly because {@link #getMDXQuery()}
     * calls init() and this would cause an infinite loop.
     */
    private Query mdxQuery = null;
    
    /**
     * Custom MDX query that the user has constructed / modified
     */
    private String modifiedOlapQuery = null;
    
    private boolean initDone = false;
    
    private boolean wasLoadedFromDao = false;
    
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
     * This mapping is used to get an OLAP connection based on an
     * {@link Olap4jDataSource}. This allows us to reuse the same connection and
     * reduced the number of times an object gets cached. The connections in
     * this mapping should not be closed as they may be used by other objects.
     */
    private final OlapConnectionProvider olapMapping;
    
    /**
     * Name of the Query object, used for loading
     */
    private String queryName;
    
    /**
     * Name of the catalog, used for loading
     */
    private String catalogName;
    
    /**
     * Name of the schema, used for loading
     */
    private String schemaName;
    
    /**
     * Name of the Cube object, used for loading
     */
    private String cubeName;

	/**
	 * Wrapper classes for the Olap axes, and children, used for loading and
	 * saving. An Olap Query can be generated by being provided with these, and
	 * all important data can be retrieved through them. Before acessing
	 * existing wrappers, call {@link #updateAttributes()} on the OlapQuery
	 * object to refresh them.
	 */
    private List<WabitOlapAxis> axes = new ArrayList<WabitOlapAxis>();
    
    /**
     * This boolean tracks if the ROWS axis of this query omits empty positions.
     * This is used when the mdxQuery is null.
     */
    @GuardedBy("this")
    private boolean nonEmpty = false;
    
    /**
     * Helps with the ResultSetProducer implementation.
     */
    @GuardedBy("this")
    private ResultSetProducerSupport rsps = new ResultSetProducerSupport(this);
    
    /**
     * Because reports nest their own version of the OLAP query,
     * we have to prevent those nested ones form exposing variables.
     */
	private boolean actsAsVariableProvider;
    
    /**
     * Creates a new, empty query with no set persistent object ID.
     */
    public OlapQuery(OlapConnectionProvider olapMapping) {
        this(null, olapMapping, "", OLAP4J_QUERY_NAME, null, null, null, null);
    }
    
    /**
     * Creates a new, empty query that will use the given persistent object ID
     * when it's saved. This constructor is only of particular use to the
     * persistence layer.
     */
    public OlapQuery(
    		String uuid, 
    		OlapConnectionProvider olapMapping, 
    		String name, 
    		String queryName, 
    		String catalogName, 
    		String schemaName, 
    		String cubeName, 
    		String modifiedOlapQuery) 
    {
    	this(uuid, olapMapping, name, queryName, catalogName, schemaName, cubeName, modifiedOlapQuery, true);
    }
    
    /**
     * Creates a new, empty query that will use the given persistent object ID
     * when it's saved. This constructor is only of particular use to the
     * persistence layer.
     * 
     * <p>This constructor exposes a supplemental parameter that tells the query if it should
     * provide the workspace with variables. Olap queries that are wrapped by a report should not 
     * act as variable providers.
     * 
     */
    public OlapQuery(
    		String uuid, 
    		OlapConnectionProvider olapMapping, 
    		String name, 
    		String queryName, 
    		String catalogName, 
    		String schemaName, 
    		String cubeName, 
    		String modifiedOlapQuery, 
    		boolean actsAsVariableProvider) 
    {
        super(uuid);
        this.olapMapping = olapMapping;
		this.actsAsVariableProvider = actsAsVariableProvider;
        this.setName(name);
		this.queryName = queryName;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.cubeName = cubeName;
		this.modifiedOlapQuery = modifiedOlapQuery;
    }
    
    public void setCurrentCube(Cube currentCube) throws SQLException {
    	setCurrentCube(currentCube, true);
    }
    
    public void setCurrentCube(Cube currentCube, boolean resetQuery) throws SQLException {
        Cube oldCube = this.currentCube;
        this.currentCube = currentCube;
        
        if (currentCube != oldCube && currentCube != null && resetQuery) {
        	setMdxQuery(new Query(OLAP4J_QUERY_NAME, currentCube));
        }
        
        if (currentCube != null) {
        	cubeName = currentCube.getName();
        	schemaName = currentCube.getSchema().getName();
        	catalogName = currentCube.getSchema().getCatalog().getName();
        } else {
        	cubeName = null;
        	schemaName = null;
        	catalogName = null;
        }
        
        firePropertyChange("currentCube", oldCube, currentCube);
        fireStructureChanged();
    }

    public synchronized Cube getCurrentCube() {
        return currentCube;
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
	public synchronized void removeHierarchy(Hierarchy hierarchy, Axis axis) throws QueryInitializationException {
	    removeDimension(hierarchy.getDimension(), axis);
	}
	
	/**
     * Removes the given {@link Dimension} from the given {@link Axis} in the
     * query
     * 
     * @param dimension
     *            The {@link Dimension} to remove
     * @param axis
     *            The {@link Axis} to remove the {@link Dimension} from
     * @throws QueryInitializationException 
     */
	public synchronized void removeDimension(Dimension dimension, Axis axis) throws QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(dimension.getName());
        
        if (qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
        	qd.clearExclusions();
        	qa.removeDimension(qd);
        	
        	if (axis != Axis.FILTER) {
        		hierarchiesInUse.remove(qd);
        	} else {
        		slicerMember = null;
        	}
        }
        updateAttributes();
    }

    /**
     * Replaces the current olap4j query with the given one. Preserves the
     * current non-empty rows setting.
     * 
     * @param mdxQuery
     *            The new query. Must not be null.
     * @throws OlapException
     */
    private synchronized void setMdxQuery(Query mdxQuery) {
    	if (mdxQuery == null) throw new NullPointerException();
        this.mdxQuery = mdxQuery;
        mdxQuery.getAxis(Axis.ROWS).setNonEmpty(nonEmpty);
        this.currentCube = mdxQuery.getCube();
        
        clearAxes();
        for (QueryAxis axis : mdxQuery.getAxes().values()) {
        	if (axis.getLocation() != null) {
        		addAxis(new WabitOlapAxis(axis));
        	}
        }
        if (isMagicEnabled()){
			this.setModifiedOlapQuery(null);
		}
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
    public synchronized boolean isInitDone() {
    	return initDone;
    }

	/**
	 * This function is called by the 'Reset Query' button on the toolbar. It
	 * will replace the current MDX Query with a blank one.
	 * @throws SQLException 
	 */
    public synchronized void reset() throws SQLException {
    	slicerMember = null;
    	hierarchiesInUse = new HashMap<QueryDimension, Hierarchy>();
    	if (getCurrentCube() != null){
    		setMdxQuery(new Query(OLAP4J_QUERY_NAME, getCurrentCube()));
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
    public synchronized void excludeMember(String dimensionName, Member memberToExclude, Selection.Operator operator) throws QueryInitializationException {
        this.getMDXQuery().getDimension(dimensionName).exclude(operator, memberToExclude);
        updateAttributes();
    }

    /**
     * Removes the given member with the given operator from the list of members
     * that are currently being excluded from the query.
     * 
     * @param memberToExclude
     *            The member that was excluded that we now want to remove from
     *            the list of exclusions.
     * @param operator
     *            The operator that the member is being excluded with.
     * @return True if the member was successfully removed from the list of
     *         exclusions, false otherwise.
     * @throws QueryInitializationException
     */
    public synchronized boolean removeExcludedMember(Member memberToExclude, 
            Selection.Operator operator) throws QueryInitializationException {
        QueryDimension qd = findQueryDimension(memberToExclude);
        
        Selection selectionToRemove = null;
        for (Selection s : qd.getExclusions()) {
            if (s.getMember().equals(memberToExclude) && s.getOperator().equals(operator)) {
                selectionToRemove = s;
                break;
            }
        }
        if (selectionToRemove == null) return false;
        qd.getExclusions().remove(selectionToRemove);
        
        updateAttributes();
        return true;
    }

    /**
     * Sets the data source that this query obtains its connections from,
     * and fires a property change event.
     */
    public synchronized void setOlapDataSource(Olap4jDataSource olapDataSource) {
        Olap4jDataSource oldDS = this.olapDataSource;
        this.olapDataSource = olapDataSource;
        firePropertyChange("olapDataSource", oldDS, olapDataSource);
    }

    /**
     * Returns the data source that this query obtains its connections from.
     */
    public synchronized Olap4jDataSource getOlapDataSource() {
        return olapDataSource;
    }
    
    /**
     * This method returns the single slicer member, since Olap4j does not track
     * whether or not you have more than one member in your slicer and since
     * mondrian does not support compound slicers we will use this variable to
     * make sure that the user does not add more than one member to their slicer
     */
    public synchronized Member getSlicerMember() {
    	return slicerMember;
    }

	/**
	 * OlapQuery has WabitOlapAxis as children, so this method returns true.
	 */
    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType.equals(WabitOlapAxis.class)) {
        	return 0;
        } else {
        	throw new IllegalArgumentException("An OlapQuery doesn't have children of type " + childType);
        }
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.unmodifiableList(axes);
    }
    
    /**
     * Returns the name of the string. This way reasonable text appears when
     * these queries are placed in a combo box.
     */
    @Override
    public synchronized String toString() {
        return getName();
    }
    
    @Override
    public void setName(String name) {
    	super.setName(name);
    	if (this.variableProvider != null) {
    		this.variableProvider.setUserFriendlyName("OLAP Query - " + name);
    	}
    }

    public List<WabitObject> getDependencies() {
        ArrayList<WabitObject> dependencies = new ArrayList<WabitObject>();
        
        // For now only the selected datasource is a dependency.
        if (getOlapDataSource()!=null) {
            dependencies.addAll(Collections.singleton(new WabitDataSource(getOlapDataSource())));
        }
        return dependencies;
    }
    
    public void removeDependency(SPObject dependency) {
        if (dependency.equals(getOlapDataSource())) {
            setOlapDataSource(null);
        }
    }
    
    /**
     * This method will initialize the query based upon which data was loaded. This is necessary
     * because if we were to initialize the query during saving and loading exceptions will be thrown
     * which will corrupt the workspace and performance will be bad. All exceptions in this method
     * will be wrapped in a QueryInitializationException, including runtime exceptions, as methods
     * calling init should be aware of any errors with initialization.
     * <p>
     * This is package private for testing.
     */
    void init() throws QueryInitializationException {
    	logger.debug("Initializing Olap Query");
    	logger.debug("Was loaded " + wasLoadedFromDao + ", init done " + initDone + ", mdxQuery is null " + (mdxQuery == null));
        if (!this.wasLoadedFromDao || this.initDone || this.mdxQuery!=null) return;
        
        if (getOlapDataSource() == null) {
			throw new QueryInitializationException("Missing database for cube " + cubeName + " for use in " + getName() + ".");
		}
		try {
			logger.debug("Creating cube with catalog=" + catalogName + ", schema=" + schemaName + ", cube=" + cubeName);
			OlapConnection createOlapConnection = this.getSession().getContext().createConnection(getOlapDataSource());
			Catalog catalog = createOlapConnection.getCatalogs().get(catalogName);
			Schema schema = catalog.getSchemas().get(schemaName);
			Cube cube = schema.getCubes().get(cubeName);
			this.currentCube = cube; // We don't like firing property changes which wipe out the query
		} catch (Exception e) {
			throw new QueryInitializationException("Cannot connect to " + getOlapDataSource(), e);
		}
		
		Query localMDXQuery = null;
		try {
			logger.debug("Creating new Query with name " + queryName + " and cube " + getCurrentCube());
		    localMDXQuery = new Query(queryName, getCurrentCube());
		} catch (SQLException e) {
			throw new QueryInitializationException(e);
		}
		
		for (WabitOlapAxis axis : axes) {
			axis.init(this, localMDXQuery);
			
			for (WabitOlapDimension dimension : axis.getDimensions()) {
				hierarchiesInUse.put(dimension.getDimension(), dimension.getHierarchy());
			}
		}
		
        mdxQuery = localMDXQuery;
        this.initDone = true;
    }

	/**
	 * Updates the child references on all Olap wrapper classes owned by this
	 * query. Also fires events for all additions and removals since the last
	 * call.
	 */
    public void updateAttributes() {
    	for (WabitOlapAxis axis :  axes) {
    		axis.updateChildren();
    	}
    	if (this.variableProvider!=null) {
    		this.variableProvider.setUpdateNeeded(true);
    	}
    	fireStructureChanged();
    }

    /**
     * Adds the given axis to the query. Adding an axis this way will define the
     * query to be loaded as a side effect.
     */
	public void addAxis(WabitOlapAxis axis) {
		wasLoadedFromDao = true;
		axes.add(axis);
		axis.setParent(this);
		fireChildAdded(WabitOlapAxis.class, axis, axes.size() - 1);
	}
	
	private void clearAxes() {
		while (axes.size() > 0) {
			WabitOlapAxis axis = axes.get(0);
			axes.remove(0);
			fireChildRemoved(WabitOlapAxis.class, axis, 0);
		}
	}

	/**
	 * Returns the list of Axis wrappers stored by this query.
	 */
	public List<WabitOlapAxis> getAxes() {
		return Collections.unmodifiableList(axes);
	}

	/**
	 * Gets the wrapper around the {@link Dimension} with the given name.
	 */
	public WabitOlapDimension getDimension(String dimensionName) {
		for (WabitOlapAxis axis : axes) {
			for (WabitOlapDimension dimension : axis.getDimensions()) {
				if (dimension.getName().equals(dimensionName)) {
					return dimension;
				}
			}
		}
		return null;
	}

	/**
	 * Finds a member from the current cube based on the unique member name.
	 */
	synchronized Member findMember(String uniqueMemberName) {
		String[] uniqueMemberNameList = uniqueMemberName.split("\\]\\.\\[");
		uniqueMemberNameList[0] = uniqueMemberNameList[0].substring(1); //remove starting [ bracket
		final int lastMemberNamePosition = uniqueMemberNameList.length - 1;
		uniqueMemberNameList[lastMemberNamePosition] = uniqueMemberNameList[lastMemberNamePosition].substring(0, uniqueMemberNameList[lastMemberNamePosition].length() - 1); //remove ending ] bracket
		try {
			return currentCube.lookupMember(uniqueMemberNameList);
		} catch (OlapException e) {
			throw new RuntimeException(e);
		}
	}

    
    /**
     * This method finds a member from a cube based on given attributes.
     */
    public synchronized Member findMember(Map<String,String> attributes, Cube cube) {
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
     * @return Returns true if the query was NOT expanded
     */
    public synchronized boolean toggleMember(Member member) throws QueryInitializationException {
        Dimension d = member.getDimension();
        QueryDimension qd = getMDXQuery().getDimension(d.getName());
        boolean wasCollapsed = false;
        for (Iterator<Selection> it = qd.getInclusions().iterator(); it.hasNext(); ) {
            Selection s = it.next();
            logger.debug("Checking if " + s.getMember().getName() + " is a descendant of " + member.getName());
            if (OlapUtils.isDescendant(member, s.getMember()) || 
            		(member.equals(s.getMember()) && (s.getOperator() == Operator.CHILDREN || s.getOperator() == Operator.INCLUDE_CHILDREN))) {
            	logger.debug(s.getMember().getName() + " was collapsed and removed");
                // XXX query model docs now say not to do this,
                // but there is no other way in the API
                it.remove();
                wasCollapsed = true;
            }
        }
        
        if (!wasCollapsed) {
            qd.include(Operator.CHILDREN, member);
        }
        updateAttributes();
        
        return wasCollapsed;
    }
 
    /**
     * Tells if the connection was initialized.
     */
    public synchronized boolean hasCachedAttributes() {
        return (!this.initDone && this.wasLoadedFromDao);
    }
    
	/**
	 * Returns the {@link SortOrder} of the given {@link Axis} of the query.
	 * 
	 * @param axis
	 *            The {@link Axis} for which you want to find the SortOrder for
	 * @return The current {@link SortOrder} of the given {@link Axis}
	 * @throws QueryInitializationException
	 *             If there is an error initializing the query
	 */
    public synchronized SortOrder getSortOrder(Axis axis) throws QueryInitializationException {
    	return getMDXQuery().getAxis(axis).getSortOrder();
    }
        
    /**
     * Returns the current MDX text that this query object's state represents.
     * @throws QueryInitializationException 
     */
    public synchronized String getMdxText() throws QueryInitializationException {
    	if (getMDXQuery() == null) return null;
        return getMDXQuery().getSelect().toString();
    }

	/**
	 * Adds the given member to the given axis.
	 * 
	 * @param ordinal
	 *            If the given member's dimension is not already on the given
	 *            axis, the dimension will be added to the axis at the given
	 *            ordinal. Otherwise, this argument is ignored.
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
    public synchronized void addToAxis(int ordinal, Member member, Axis axis) throws OlapException, QueryInitializationException {
        addToAxis(ordinal, member, Operator.MEMBER, axis);
    }

    /**
     * Adds the given member to the given axis with a defined operator.
     * 
     * @param ordinal
     *            If the given member's dimension is not already on the given
     *            axis, the dimension will be added to the axis at the given
     *            ordinal. Otherwise, this argument is ignored.
     * @param member
     *            The member to add. If the member's dimension is not already on
     *            the given axis, it will be added to the axis automatically.
     * @param operator
     *            The operator to add with the member to the axis.
     * @param axis
     *            The axis to add the member to. Must be either Axis.ROWS or
     *            Axis.COLUMNS.
     * @throws OlapException
     *             If a database error occurs
     * @throws QueryInitializationException
     */
    public synchronized void addToAxis(int ordinal, Member member, Operator operator, Axis axis) 
            throws OlapException, QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(member.getDimension().getName());
        addDimensionToAxis(ordinal, axis, qd);
        
        
        if (qa.getLocation() == Axis.FILTER) {
        	//The filter axis does not support multiple members
        	qd.clearInclusions();
        	if (slicerMember != null) {
        		String oldDimensionName = slicerMember.getDimension().getName();
        		QueryDimension oldQueryDimension = mdxQuery.getDimension(oldDimensionName);
        		QueryAxis oldAxis = oldQueryDimension.getAxis();
        		if (oldAxis != null && oldAxis.getDimensions().indexOf(oldQueryDimension) >= 0) {
        			oldAxis.removeDimension(oldQueryDimension);
        		}
        	}
        	hierarchiesInUse.remove(qd);
        } else {
        	hierarchiesInUse.put(qd, member.getHierarchy());
        	if (slicerMember != null){
        		String oldDimensionName = slicerMember.getDimension().getName();
        		QueryDimension oldQueryDimension = mdxQuery.getDimension(oldDimensionName);
        		if (oldQueryDimension == qd) {
        			slicerMember = null;
        		}
        	}
        }
        
        if (!isIncluded(member)) {
        	qd.include(operator, member);
        }
        
        //The filter axis does not support multiple members
        if (qa.getLocation() == Axis.FILTER ) {
        	if (slicerMember != null) {
        		String oldDimensionName = slicerMember.getDimension().getName();
        		QueryDimension oldQueryDimension = mdxQuery.getDimension(oldDimensionName);
        		if (oldQueryDimension != qd) {
        			oldQueryDimension.clearInclusions();
        		}
        	}
        	slicerMember = member;
        }

        Type memberType = member.getMemberType();
        logger.debug("memberType = " + memberType);
		if (!(member instanceof Measure) && qa.getLocation() != Axis.FILTER) {
			qd.setHierarchizeMode(HierarchizeMode.PRE);
        }
		
		updateAttributes();
    }

    /**
     * Adds a dimension to an axis at the given ordinal. This is package private for
     * use by other parts of the query.
     * @param ordinal The position in the axis to add the dimension at.
     * @param qa The axis to add the dimension to.
     * @param qd The dimension to add to the axis.
     */
    void addDimensionToAxis(int ordinal, Axis axis, QueryDimension qd) throws QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        if (!qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
        	qa.addDimension(ordinal, qd);
        	if (qa.getLocation() != Axis.FILTER) {
        		hierarchiesInUse.put(qd, qd.getDimension().getDefaultHierarchy());
        	}
        } else {
        	int index = qa.getDimensions().indexOf(qd);
        	if (index >= 0) {
        		qa.getDimensions().remove(index);
        		if (index < ordinal) {
        			ordinal--;
        		}
        		qa.getDimensions().add(ordinal, qd);
        		if (qa.getLocation() != Axis.FILTER) {
            		hierarchiesInUse.put(qd, qd.getDimension().getDefaultHierarchy());
            	}
        	}
        }
    }

    public synchronized List<Hierarchy> getRowHierarchies() throws QueryInitializationException {
        return getHierarchies(Axis.ROWS);
    }

    public synchronized List<Hierarchy> getColumnHierarchies() throws QueryInitializationException {
        return getHierarchies(Axis.COLUMNS);
    }

    /**
     * MDX only allows members from one hierarchy at a time, but the query model
     * currently does not enforce that. Until Hierarchy can be added to the
     * QueryDimension class, we'll maintain this mapping here to associate
     * Dimensions with Hierarchies.
     */
    @GuardedBy("this")
    private Map<QueryDimension, Hierarchy> hierarchiesInUse =
        new HashMap<QueryDimension, Hierarchy>();
    
    private synchronized List<Hierarchy> getHierarchies(Axis axis) throws QueryInitializationException {
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
    public synchronized void drillReplace(Member member) throws QueryInitializationException {
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
        
        updateAttributes();
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
    public synchronized void drillUpTo(Member fromMember, Member targetAncestor) throws QueryInitializationException {
    	// is targetAncestor REALLY an ancestor?
    	if (!OlapUtils.isDescendant(targetAncestor, fromMember)) return;
    	
    	Member member = fromMember;
    	while (member.getParentMember() != null && !member.equals(targetAncestor)) {
			member = member.getParentMember();
			if (!isIncluded(member)) {
				includeMember(member);
			}
		}
    	
    	updateAttributes();
    }
    
	/**
	 * Sorts the OLAP results on the given {@link Axis} by the given
	 * {@link Measure} in the given {@link SortOrder}
	 */
    public synchronized void sortBy(Axis axis, SortOrder order, Measure measure) throws QueryInitializationException {
    	getMDXQuery().getAxis(axis).sort(order, measure);
    	if (isMagicEnabled()){
			this.setModifiedOlapQuery(null);
		}
    	fireStructureChanged();
    }
    
	/**
	 * Removes the sort order on the given axis
	 * 
	 * @param axis
	 *            The Axis from which to remove the sort order
	 * @throws QueryInitializationException
	 */
	public synchronized void clearSort(Axis axis) throws QueryInitializationException {
		getMDXQuery().getAxis(axis).clearSort();
		if (isMagicEnabled()){
			this.setModifiedOlapQuery(null);
		}
		fireStructureChanged();
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
    public synchronized boolean isIncluded(Member member) throws QueryInitializationException {
    	QueryDimension dimension = findQueryDimension(member);
    	for (Selection s: dimension.getInclusions()) {
    		if (s.getMember().equals(member)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private synchronized QueryDimension findQueryDimension(Member member) throws QueryInitializationException {
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
    public synchronized void includeMember(Member member) throws QueryInitializationException {
        QueryDimension qd = findQueryDimension(member);
        qd.include(member);
        
        updateAttributes();
    }

    /**
     * Removes the given member from the list of members included in this query.
     * 
     * @param member
     *            The member to remove.
     * @param operator
     *            The operator used in the inclusion.
     * @return True if the member was successfully removed, false otherwise.
     * @throws QueryInitializationException
     */
    public synchronized boolean removeIncludedMember(Member member, Selection.Operator operator) 
            throws QueryInitializationException {
        QueryDimension qd = findQueryDimension(member);
        //XXX There should probably be a better way to remove the member
        //from the list of included members. This also needs to be fixed
        //in toggleMember
        Selection selectionToRemove = null;
        for (Selection s : qd.getInclusions()) {
            if (s.getMember().equals(member) && s.getOperator().equals(operator)) {
                selectionToRemove = s;
                break;
            }
        }
        if (selectionToRemove == null) return false;
        qd.getInclusions().remove(selectionToRemove);
        
        updateAttributes();
        return true;
    }

    /**
     * Sets the ROWS axis of this query to omit empty positions.
     * 
     * @param nonEmpty
     *            True means to omit empty positions; false means to include
     *            them.
     */
    public synchronized void setNonEmpty(boolean nonEmpty) {
    	boolean oldVal = this.nonEmpty;
    	this.nonEmpty = nonEmpty;
    	if (mdxQuery != null) {
    		mdxQuery.getAxis(Axis.ROWS).setNonEmpty(nonEmpty);
    		logger.debug("Query has rows non-empty? " + mdxQuery.getAxis(Axis.ROWS).isNonEmpty());
    	}
    	firePropertyChange("nonEmpty", oldVal, nonEmpty);
    	fireStructureChanged();
    }

    /**
     * Tells whether the ROWS axis of this query omits empty positions.
     * 
     * @return True is this query omits empty rows; false if it includes them.
     */
    public synchronized boolean isNonEmpty() {
    	return nonEmpty;
    }

    /**
     * Takes the given Hierarchy and clears all exclusions from its Dimension.
     * @param hierarchy
     * @throws QueryInitializationException
     */
	public synchronized void clearExclusions(Hierarchy hierarchy) throws QueryInitializationException {
		QueryDimension dimension = getMDXQuery().getDimension(hierarchy.getDimension().getName());
		dimension.clearExclusions();
		updateAttributes();
	}
	
	/**
	 * This method should only be used internally or in special cases such as
     * an undo manager or synchronizing with a server. Removing a child of the query
     * will only remove the Wabit wrapper around the axis of the query object.
	 */
	@Override
	protected boolean removeChildImpl(SPObject child) {
	    if (axes.contains(child)) {
	        int index = axes.indexOf(child);
	        axes.remove(child);
	        fireChildRemoved(child.getClass(), child, index);
	        fireStructureChanged();
	    }
	    return false;
	}

    /**
     * This method should only be used internally or in special cases such as
     * an undo manager or synchronizing with a server. Adding a child to the
     * query will only add a Wabit wrapper for an axis of the query.
     */
	@Override
	protected void addChildImpl(SPObject child, int index) {
	    WabitOlapAxis axis = (WabitOlapAxis) child;
	    for (WabitObject existingChild : axes) {
	        if (((WabitOlapAxis) existingChild).getOrdinal().equals(axis.getOrdinal())) {
	            throw new IllegalArgumentException("There already exists a child for the axis " + 
	                    axis.getOrdinal() + " in the query " + getName());
	        }
	    }
	    
	    axes.add(index, axis);
	    axis.setParent(this);
	    wasLoadedFromDao = true;
	    fireChildAdded(child.getClass(), child, index);
	    fireStructureChanged();
	}
	
	public synchronized String getQueryName() {
		if (isInitDone()) {
			return mdxQuery.getName();
		} else {
			return queryName;
		}
	}

	public String getCatalogName() {
		if (isInitDone()) {
			return currentCube.getSchema().getCatalog().getName();
		} else {
			return catalogName;
		}
	}

	public String getSchemaName() {
		if (isInitDone()) {
			return currentCube.getSchema().getName();
		} else {
			return schemaName;
		}
	}

	public String getCubeName() {
		if (isInitDone()) {
			return currentCube.getName();
		} else {
			return cubeName;
		}
	}
	
	public String getModifiedOlapQuery() {
		return modifiedOlapQuery;
	}
	
	public void setModifiedOlapQuery(String modifiedOlapQuery) {
		String oldMdx = this.getModifiedOlapQuery();
		this.modifiedOlapQuery = modifiedOlapQuery;
		firePropertyChange("modifiedOlapQuery", oldMdx, modifiedOlapQuery);
	}

	// -------------- WabitBackgroundWorker interface --------------
	

    public void cancel() {
        rsps.cancel();
        // TODO cancel any internal running queries as well.
    }
    
    public boolean isRunning() {
        return rsps.isRunning();
    }
    
    // -------------- End of WabitBackgroundWorker interface --------------

    // -------------- ResultSetProducer interface --------------

    public synchronized void addResultSetProducerListener(ResultSetProducerListener listener) {
        rsps.addResultSetListener(listener);
    }

    public synchronized void removeResultSetProducerListener(ResultSetProducerListener listener) {
        rsps.removeResultSetListener(listener);
    }
    
    public boolean isStreaming() {
    	return false;
    }
    
    public ResultSetHandle execute(
    		@Nullable SPVariableHelper variablesContext,
    		@Nullable ResultSetListener listener) throws ResultSetProducerException
	{
    	return this.execute(variablesContext, listener, true);
	}

    public ResultSetHandle execute(
    		@Nullable SPVariableHelper variablesContext,
    		@Nullable ResultSetListener listener,
    		boolean async) throws ResultSetProducerException
	{
        try {
        	
        	String textualQuery;
        	if (this.modifiedOlapQuery == null) {
        		// take the snapshot
        		SelectNode mdx;
        		synchronized (this) {
        			if (getRowHierarchies().isEmpty() || getColumnHierarchies().isEmpty()) {
        				return null;
        			} else {
        				mdx = getMDXQuery().getSelect();
        			}
        		}
        		
        		textualQuery = mdx.toString();
        	
        	} else {
        	
        		textualQuery = this.getModifiedOlapQuery();
        		
        	}
        	
        	return 
        		rsps.execute(
        				this.getSession().getContext(),
        				getOlapDataSource(),
        				textualQuery,
        				variablesContext,
        				ResultSetType.OLAP, 
	        			0, 
	        			listener,
	        			async);	
            
        } catch (Exception e) {
            throw new ResultSetProducerException("Couldn't create database connection for Olap query", e);
        } finally {
        	if (this.variableProvider!=null) {
        		this.variableProvider.setUpdateNeeded(true);
            }
        }
    }

    // -------------- end ResultSetProducer interface --------------

    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(WabitOlapAxis.class);
    	return types;
    }

	public SPVariableResolver getVariableResolver() {
		return this.variableProvider;
	}
	
	@Override
	public void setParent(SPObject parent) {
		super.setParent(parent);
		// Initialize the variables provider once this object is hooked up to the tree only.
		if (actsAsVariableProvider) {
			this.variableProvider = new OlapVariableResolver(this, this.uuid, "OLAP Query - " + this.getName());
		}
	}
	
	public void setActsAsVariableProvider(boolean actsAsVariableProvider) {
		this.actsAsVariableProvider = actsAsVariableProvider;
	}
	
	private void fireStructureChanged() {
		if (!this.wasLoadedFromDao || this.initDone || this.mdxQuery!=null) {
			rsps.fireStructureChanged();
		}
	}
    
}
