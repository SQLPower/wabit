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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

import javax.naming.NamingException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.mdx.ParseTreeWriter;
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

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.OlapConnectionMapping;
import ca.sqlpower.wabit.ResultSetAndUpdateCountCollection;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetProducerEvent;
import ca.sqlpower.wabit.rs.ResultSetProducerException;
import ca.sqlpower.wabit.rs.ResultSetProducerSupport;

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
public class OlapQuery extends AbstractWabitObject implements ResultSetProducer {
    
    private static final Logger logger = Logger.getLogger(OlapQuery.class);
    
    /**
     * This is the query name given to all Olap4j queries in this class.
     */
    private static final String OLAP4J_QUERY_NAME = "GUI Query";
    
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
            OlapQuery newQuery = new OlapQuery(oldOlapQuery.olapMapping);
            newQuery.setOlapDataSource(oldOlapQuery.getOlapDataSource());
            newQuery.setName(oldOlapQuery.getName());
            newQuery.setQueryName(oldOlapQuery.getQueryName());
            newQuery.setCatalogName(oldOlapQuery.getCatalogName());
            newQuery.setSchemaName(oldOlapQuery.getSchemaName());
            newQuery.setCubeName(oldOlapQuery.getCubeName());
            
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
    private final OlapConnectionMapping olapMapping;
    
    /**
     * A List of {@link OlapQueryListener} objects that will be listening to
     * changes to this OlapQuery instance. Currently, it is primarily used
     * to notify listeners when the query has been executed.
     * <p>
     * Although this appears to overlap somewhat with the PropertyChangeEvent
     * for "running" which indicates to the session that this WabitObject is
     * doing work in the background, it serves a slightly different purpose
     * since it actually delivers the new successful result to the listener.
     * All the "running" property change does is notify listeners of the state
     * transitions themselves. 
     */
    @GuardedBy("itself")
    private final List<OlapQueryListener> listeners =
        Collections.synchronizedList(new ArrayList<OlapQueryListener>());

    /**
     * A semaphore with one permit. This is the mechanism by which we serialize
     * calls to {@link #executeOlapQuery()}.
     */
    private final Semaphore executionSemaphore = new Semaphore(1);
    
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
    private final ResultSetProducerSupport rsps = new ResultSetProducerSupport(this);
    
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
    
    public void setCurrentCube(Cube currentCube) throws SQLException {
        Cube oldCube = this.currentCube;
        this.currentCube = currentCube;
        
        if (currentCube != oldCube && currentCube != null) {
        	setMdxQuery(new Query(OLAP4J_QUERY_NAME, currentCube));
        }
        
        firePropertyChange("currentCube", oldCube, currentCube);
    }

    public synchronized Cube getCurrentCube() {
        return currentCube;
    }

    /**
     * Executes the current MDX query represented by this object, returning the
     * cell set that results from the query's execution.
     * <p>
     * Every call to this method results in a CellSetEvent and a
     * ResultSetProducerEvent being fired.
     * 
     * @throws SQLException
     *             If the cell set cannot be iterated over or its values cannot
     *             be retrieved to create a result set based off of the cell
     *             set.
     * 
     * @see #innerExecuteOlapQuery()
     */
    public CellSet executeOlapQuery() throws QueryInitializationException, InterruptedException, SQLException {
        CellSet cellSet = innerExecuteOlapQuery();
        fireResultSetEvent(cellSet);
        return cellSet;
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
     * Every call to this method results in a CellSetEvent being fired. If the
     * query is in a state where it can't be executed (because one or more axes
     * is empty), the event will still be fired, but it will deliver a null
     * CellSet to listeners. <b>The CellSetEvent is fired while this query is still
     * locked against execution, so it is vitally important that CellSetListeners
     * do not attempt to re-execute the query in response to any CellSetEvent.</b>
     * Such behaviour by CellSetListeners is guaranteed to cause deadlock.
     * 
     * <h2>Thread safety</h2>
     * When the query begins to execute, it obtains this OlapQuery instance's
     * monitor, takes a snapshot of the current query state, then releases the
     * monitor. Query execution then proceeds while the OlapQuery instance
     * itself remains unlocked. This allows other threads (especially the Swing
     * GUI) to continue to modify the query while waiting for the results of the
     * previous execution.
     * <p>
     * However, each OlapQuery instance does prevent itself from making
     * overlapping execution requests. Calls to execute() are serialized using
     * an internal synchronization mechanism. Each call to execute() does not
     * take its snapshot of the query state until any previously in-flight
     * execution has completed. This increases the chances that several blocked
     * calls to execute() will end up executing the same MDX query and will
     * therefore return the same cached CellSet rather than each wasting a
     * potentially large amount of time executing a query that is no longer
     * desired.
     * 
     * <h2>The name of this method</h2>
     * This method has a silly name in order for it not to collide with its
     * companion method, {@link #execute()}, whose name was specified in an interface.
     *  
     * @return The {@link CellSet} result of the execution of the query. If the
     *         query has no dimensions in either it's row or column axis
     *         however, it will not be able to execute, in which case it returns
     *         null and no OlapQueryEvent is fired.
     * @throws OlapException
     *             If there was a database error
     * @throws QueryInitializationException
     *             If this query has not yet been initialized and the attempted
     *             initialization fails.
     * @throws InterruptedException
     *             If the calling thread is interrupted while blocked waiting
     *             for another call to execute() to complete.
     */
    private CellSet innerExecuteOlapQuery() throws OlapException, QueryInitializationException, InterruptedException {
        try {
            executionSemaphore.acquire();
            try {
                setRunning(true);

                // take the snapshot
                SelectNode mdx;
                synchronized (this) {
                	// TODO if there is one, execute the textual query instead
                    if (getRowHierarchies().isEmpty() || getColumnHierarchies().isEmpty()) {
                        mdx = null;
                    } else {
                    	mdx = getMDXQuery().getSelect();
                    }
                }
                
                // now run the query (while holding the semaphore but not the OlapQuery monitor)
                CellSet cellSet;
                if (mdx != null) {
                	// The following code looks like it leaks an OlapConnection and an OlapStatement,
                	// but both the connection and statement are actually just "retrieved"; not
                	// "created" as their method names suggest
                	OlapStatement olapStatement = createOlapConnection().createStatement();
					cellSet = olapStatement.executeOlapQuery(mdx);
                } else {
                	// still need to notify listeners about the lack of a cell set
                	cellSet = null;
                }
                
                fireQueryExecuted(cellSet);
                return cellSet;
                
            } catch (SQLException e) {
                throw new OlapException("Couldn't create database connection for Olap query", e);
            } catch (ClassNotFoundException e) {
                throw new OlapException("Couldn't create database connection for Olap query", e);
            } catch (NamingException e) {
                throw new OlapException("Couldn't create database connection for Olap query", e);
            } finally {
                setRunning(false);
            }
        } finally {
            executionSemaphore.release();
        }
    }
    
    private void fireQueryExecuted(CellSet cellSet) {
        final OlapQueryEvent e = new OlapQueryEvent(this, cellSet);
        runInForeground(new Runnable() {
            public void run() {
                for (int i = listeners.size() - 1; i >= 0; i--) {
                    listeners.get(i).queryExecuted(e);
                }
            }
        });
    }

    /**
     * Creates a new {@link ResultSetAndUpdateCountCollection} based on the
     * given {@link CellSet} and fires a {@link ResultSetProducerEvent}
     * containing the new results.
     * 
     * @param cellSet
     *            The cell set to wrap in a
     *            {@link ResultSetAndUpdateCountCollection} to notify listeners
     *            of new results.
     * @return The result set collection that was sent to listeners.
     * @throws SQLException
     *             If the cellSet is not null and its values cannot be iterated
     *             over or retrieved.
     */
    private ResultSetAndUpdateCountCollection fireResultSetEvent(
            CellSet cellSet) throws SQLException {
        OlapResultSet results;
        if (cellSet == null) {
            results = null;
        } else {
            results = new OlapResultSet();
            results.populate(cellSet);
        }
        
        final ResultSetAndUpdateCountCollection rsCollection = 
            new ResultSetAndUpdateCountCollection(results, OlapQuery.this);
        
        runInForeground(new Runnable() {
            public void run() {
                synchronized (this) {
                    rsps.fireResultSetEvent(rsCollection);
                }
            }
        });
        return rsCollection;
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
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(hierarchy.getDimension().getName());
        
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
    }

    public synchronized OlapConnection createOlapConnection()
    throws SQLException, ClassNotFoundException, NamingException {
        return olapMapping.createConnection(getOlapDataSource());
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
    public synchronized String toString() {
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
    
    public void removeDependency(WabitObject dependency) {
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
     */
    private void init() throws QueryInitializationException {
    	logger.debug("Initializing Olap Query");
    	logger.debug("Was loaded " + wasLoadedFromDao + ", init done " + initDone + ", mdxQuery is null " + (mdxQuery == null));
        if (!this.wasLoadedFromDao || this.initDone) return;
        
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
		
		Query localMDXQuery = null;
		try {
		    localMDXQuery = new Query(queryName, getCurrentCube());
		} catch (SQLException e) {
			throw new QueryInitializationException(e);
		}
		
		for (WabitOlapAxis axis : axes) {
			axis.init(this, localMDXQuery);
		}
		
        setMdxQuery(localMDXQuery);
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
    }

	public void addAxis(WabitOlapAxis axis) {
		wasLoadedFromDao = true;
		axes.add(axis);
	}
	
	public void removeAxis(WabitOlapAxis axis) {
		wasLoadedFromDao = true;
		axes.remove(axis);
	}
	
	public List<WabitOlapAxis> getAxes() {
		return Collections.unmodifiableList(axes);
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
     * @return Returns true if the query was expanded
     */
    public synchronized boolean toggleMember(Member member) throws QueryInitializationException {
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
        return wasExpanded;
    }
 
    /**
     * Tells if the connection was initialized.
     */
    public synchronized boolean hasCachedAttributes() {
        // Create a copy of the init flag so the object is immutable.
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
    public synchronized void addToAxis(int ordinal, Member member, Axis axis) throws OlapException, QueryInitializationException {
        QueryAxis qa = getMDXQuery().getAxis(axis);
        QueryDimension qd = getMDXQuery().getDimension(member.getDimension().getName());
        logger.debug("Moving dimension " + qd.getName() + " to Axis " + qa.getName() + " in ordinal " + ordinal);
        if (!qa.equals(qd.getAxis())) {
        	qd.clearInclusions();
        	qa.addDimension(ordinal, qd);
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
        	qd.include(Operator.MEMBER, member);
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
    }
    
	/**
	 * Sorts the OLAP results on the given {@link Axis} by the given
	 * {@link Measure} in the given {@link SortOrder}
	 */
    public synchronized void sortBy(Axis axis, SortOrder order, Measure measure) throws QueryInitializationException {
    	getMDXQuery().getAxis(axis).sort(order, measure);
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
    }

    /**
     * Sets the ROWS axis of this query to omit empty positions.
     * 
     * @param nonEmpty
     *            True means to omit empty positions; false means to include
     *            them.
     */
    public synchronized void setNonEmpty(boolean nonEmpty) {
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
    public synchronized boolean isNonEmpty() {
    	return nonEmpty;
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.wabit.olap.ResultSetProducer#addOlapQueryListener(ca.sqlpower.wabit.olap.OlapQueryListener)
     */
    public void addOlapQueryListener(OlapQueryListener listener) {
    	listeners.add(listener);
    }
    
    /* (non-Javadoc)
     * @see ca.sqlpower.wabit.olap.ResultSetProducer#removeOlapQueryListener(ca.sqlpower.wabit.olap.OlapQueryListener)
     */
    public void removeOlapQueryListener(OlapQueryListener listener) {
    	listeners.remove(listener);
    }

    /**
     * Takes the given Hierarchy and clears all exclusions from its Dimension.
     * @param hierarchy
     * @throws QueryInitializationException
     */
	public synchronized void clearExclusions(Hierarchy hierarchy) throws QueryInitializationException {
		QueryDimension dimension = getMDXQuery().getDimension(hierarchy.getDimension().getName());
		dimension.clearExclusions();
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
	    return false;
	}
	
	public void setQueryName(String queryName) {
		String oldQueryName = this.queryName;
		this.queryName = queryName;
		wasLoadedFromDao = true;
		initDone = false;
		firePropertyChange("query-name", oldQueryName, queryName);
	}
	
	public String getQueryName() {
		if (isInitDone()) {
			return mdxQuery.getName();
		} else {
			return queryName;
		}
	}

	public void setCatalogName(String catalogName) {
		String oldCatalogName = this.catalogName;
		this.catalogName = catalogName;
		wasLoadedFromDao = true;
		initDone = false;
		firePropertyChange("catalog-name", oldCatalogName, catalogName);
	}

	public String getCatalogName() {
		if (isInitDone()) {
			return currentCube.getSchema().getCatalog().getName();
		} else {
			return catalogName;
		}
	}

	public void setSchemaName(String schemaName) {
		String oldSchemaName = this.schemaName;
		this.schemaName = schemaName;
		wasLoadedFromDao = true;
		initDone = false;
		firePropertyChange("schema-name", oldSchemaName, schemaName);
	}

	public String getSchemaName() {
		if (isInitDone()) {
			return currentCube.getSchema().getName();
		} else {
			return schemaName;
		}
	}

	public void setCubeName(String cubeName) {
		String oldCubeName = this.cubeName;
		this.cubeName = cubeName;
		wasLoadedFromDao = true;
		firePropertyChange("cube-name", oldCubeName, cubeName);
	}

	public String getCubeName() {
		if (isInitDone()) {
			return currentCube.getName();
		} else {
			return cubeName;
		}
	}

	// -------------- WabitBackgroundWorker interface --------------
	
	private volatile boolean backgroundWorkerRunning;
	
    public void cancel() {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setRunning(boolean running) {
        boolean oldValue = backgroundWorkerRunning;
        backgroundWorkerRunning = running;
        firePropertyChange("running", oldValue, running);
    }
    
    public boolean isRunning() {
        return backgroundWorkerRunning;
    }
    // -------------- End of WabitBackgroundWorker interface --------------

    // -------------- ResultSetProducer interface --------------

    public synchronized void addResultSetListener(ResultSetListener listener) {
        rsps.addResultSetListener(listener);
    }

    public synchronized void removeResultSetListener(ResultSetListener listener) {
        rsps.removeResultSetListener(listener);
    }

    /**
     * Executes the underlying MDX query, causing all the side effects described
     * in {@link #executeOlapQuery()}, then converts those results to an
     * {@link OlapResultSet} and notifies the ResultSetListeners with that
     * converted result.
     */
    public Future<ResultSetAndUpdateCountCollection> execute() throws ResultSetProducerException, 
            InterruptedException {
        Callable<ResultSetAndUpdateCountCollection> callable = 
            new Callable<ResultSetAndUpdateCountCollection>() {
        
            public ResultSetAndUpdateCountCollection call() throws Exception {
                try {
                    CellSet cellSet = executeOlapQuery();
                    final ResultSetAndUpdateCountCollection rsCollection = fireResultSetEvent(cellSet);
                    
                    return rsCollection;
                    
                } catch (Exception e) {
                    throw new ResultSetProducerException(e);
                }
            }
        };
        FutureTask<ResultSetAndUpdateCountCollection> futureTask = 
            new FutureTask<ResultSetAndUpdateCountCollection>(callable);
        runInBackground(futureTask);
        return futureTask;
    }

    // -------------- end ResultSetProducer interface --------------

	

    
}
