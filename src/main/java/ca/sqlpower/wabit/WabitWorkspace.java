/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.wabit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.UndoableEditListener;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;

/**
 * The WabitWorkspace is the root WabitObject of a WabitSession. It directly
 * corresponds with the root workspace node in the XML representation of a Wabit
 * workspace. It belongs to exactly one WabitSession.
 */
public class WabitWorkspace extends AbstractWabitObject implements DataSourceCollection<SPDataSource> {
	
	
	private static final Logger logger = Logger.getLogger(WabitWorkspace.class);
	
    /**
     * The data sources that feed the queries for this workspace.
     */
    private final List<WabitDataSource> dataSources = new ArrayList<WabitDataSource>();
    
    /**
     * The queries that fetch result sets for this workspace.
     * <p>
     * TODO an SQL query is too specific; we should have a generic result set provider
     * class that could be anything (XPath, SQL query, gdata query, JavaScript that builds a table of data, ...)
     */
    private final List<QueryCache> queries = new ArrayList<QueryCache>();
    
    /**
     * This is all of the queries in the workspace that connects to an OLAP database.
     */
    private final List<OlapQuery> olapQueries = new ArrayList<OlapQuery>();
    
	/**
	 * The list of Listeners to notify when a datasource is added or removed.
	 */
	List<DatabaseListChangeListener> listeners;
    
    /**
     * The reports in this workspace.
     */
    private final List<Report> reports = new ArrayList<Report>();
    
    /**
     * The templates in this workspace.
     */
    private final List<Template> templates = new ArrayList<Template>();
    
    /**
     * The images in this workspace.
     */
    private final List<WabitImage> images = new ArrayList<WabitImage>();
    
    /**
     * The charts in this workspace.
     */
    private final List<Chart> charts = new ArrayList<Chart>();
    
    /**
     * The list of users in this workspace. Only to be used if this is the system workspace.
     */
    private final List<User> users = new ArrayList<User>();
    
    /**
     * The list of groups in this workspace. Only to be used if this is the system workspace.
     */
    private final List<Group> groups = new ArrayList<Group>();
    
    /**
     * TODO: These listeners are never fired at current as they are only used for
     * DS Type undo events in the library currently. These listeners are unused
     * until the workspace supports changing DS Types or other undoable edits are
     * needed for the DS Collection.
     */
    private final List<UndoableEditListener> dsCollectionUndoListeners = new ArrayList<UndoableEditListener>();
    
    /**
     * This is the current editor panel's model that is being being edited.
     * This allows the workspace to know what panel to load when it is loaded. 
     */
    private WabitObject editorPanelModel;

    /**
     * The session this workspace belongs to. Sessions and workspaces have a 1:1
     * correspondence.
     */
    private WabitSession session;

    /**
     * Creates a new Wabit workspace. This is normally done by the session it
     * belongs to.
     */
    public WabitWorkspace() {
        listeners = new ArrayList<DatabaseListChangeListener>();
        setName("Unsaved Workspace");
    }
    
    public List<WabitObject> getChildren() {
    	List<WabitObject> allChildren = new ArrayList<WabitObject>();
    	if (isSystemWorkspace()) {
    		allChildren.addAll(users);
    		allChildren.addAll(groups);
    	} else {
    		allChildren.addAll(dataSources);
    		allChildren.addAll(queries);
    		allChildren.addAll(olapQueries);
    		allChildren.addAll(charts);
    		allChildren.addAll(images);
    		allChildren.addAll(templates);
    		allChildren.addAll(reports);
    	}
    	return allChildren;
    }
    
    /**
     * Sets the session which this workspace belongs to. This should normally
     * only be called by that workspace. It's exposed as a publicly settable
     * method so that "wrapper" sessions in other packages can claim ownership
     * of this workspace from the "core" or "delegate" session they wrap.
     * <p>
     * This setter does not fire an event because it is not supposed to be called
     * after the workspace has been initialized.
     * 
     * @param session
     *            The session this workspace belongs to.
     */
    public void setSession(WabitSession session) {
        this.session = session;
    }
    
    @Override
    public WabitSession getSession() {
        if (session != null) return session;
        throw new SessionNotFoundException("No session exists for " + getName() + " of type " +
                getClass());
    }
    
    public void addDataSource(WabitDataSource ds) {
        addDataSource(ds, dataSources.size());
    }
    
    public void addDataSource(WabitDataSource ds, int index) {
    	logger.debug("adding WabitDataSource");
        dataSources.add(index, ds);
        ds.setParent(this);
        fireChildAdded(WabitDataSource.class, ds, index);
        if(ds instanceof WabitDataSource) {
        	fireAddEvent(((WabitDataSource)ds).getSPDataSource());
        }
    }

    private boolean removeDataSource(WabitDataSource ds) {
    	logger.debug("removing WabitDataSource");
    	int index = dataSources.indexOf(ds);
    	if (index != -1) {
    		dataSources.remove(ds);
    		fireChildRemoved(WabitDataSource.class, ds, index);
    		if(ds instanceof WabitDataSource) {
    			fireRemoveEvent(index, ((WabitDataSource)ds).getSPDataSource());
    		}
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Returns an unmodifiable view of the data sources in this workspace.
     * <p>
     * TODO change this to List&lt;WabitDataSource&gt; getDatabases()
     */
    public List<WabitDataSource> getDataSources() {
        return Collections.unmodifiableList(dataSources);
    }
    
    public void addQuery(QueryCache query, WabitSession session) {
        addQuery(query, session, queries.size());
    }
    
    public void addQuery(QueryCache query, WabitSession session, int index) {
        queries.add(index, query);
        query.setParent(this);
        query.setDBMapping(session.getContext());
        fireChildAdded(QueryCache.class, query, index);
        setEditorPanelModel(query);
    }

    private boolean removeQuery(QueryCache query, WabitSession session) {
    	int index = queries.indexOf(query);
    	if (index != -1) {
    		query.cleanup();
    		queries.remove(query);
    		fireChildRemoved(QueryCache.class, query, index);
    		if (editorPanelModel == query) {
                setEditorPanelModel(this);
            }
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void addTemplate(Template template) {
        addTemplate(template, templates.size());
    }
    
    public void addTemplate(Template template, int index) {
        templates.add(index, template);
        template.setParent(this);
        fireChildAdded(Template.class, template, index);
        setEditorPanelModel(template);
	}
    
    private boolean removeTemplate(Template template) {
    	int index = templates.indexOf(template);
    	if (index != -1) {
    		templates.remove(template);
    		fireChildRemoved(Template.class, template, index);
    		if (editorPanelModel == template) {
    		    setEditorPanelModel(this);
    		}
    		return true;
    	} else {
    		return false;
    	}
	}
    
    public void addReport(Report report) {
        addReport(report, reports.size());
    }
    
    public void addReport(Report report, int index) {
        reports.add(index, report);
        report.setParent(this);
        fireChildAdded(Report.class, report, index);
        setEditorPanelModel(report);
    }
    
    private boolean removeReport(Report report) {
    	int index = reports.indexOf(report);
    	if (index != -1) {
    		reports.remove(report);
    		fireChildRemoved(Report.class, report, index);
    		if (editorPanelModel == report) {
    		    setEditorPanelModel(this);
    		}
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void addImage(WabitImage image) {
        addImage(image, images.size());
    }
    
    public void addImage(WabitImage image, int index) {
        images.add(index, image);
        image.setParent(this);
        fireChildAdded(WabitImage.class, image, index);
        setEditorPanelModel(image);
    }
    
    private boolean removeImage(WabitImage image) {
        int index = images.indexOf(image);
        if (index != -1) {
            images.remove(image);
            fireChildRemoved(WabitImage.class, image, index);
            if (editorPanelModel == image) {
                setEditorPanelModel(this);
            }
            return true;
        } else {
            return false;
        }
    }
    
    public List<WabitImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public void addChart(Chart chart) {
        addChart(chart, charts.size());
    }
    
    public void addChart(Chart chart, int index) {
        charts.add(index, chart);
        chart.setParent(this);
        fireChildAdded(Chart.class, chart, index);
        setEditorPanelModel(chart);
    }
    
    private boolean removeChart(Chart chart) {
        int index = charts.indexOf(chart);
        if (index != -1) {
            charts.remove(chart);
            fireChildRemoved(Chart.class, chart, index);
            if (editorPanelModel == chart) {
                setEditorPanelModel(this);
            }
            return true;
        } else {
            return false;
        }
    }
    
    public List<Chart> getCharts() {
        return Collections.unmodifiableList(charts);
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        int offset = 0;

        if (isSystemWorkspace()) {
        	if (childType == User.class) return offset;
        	offset += users.size();
        	
        	if (childType == Group.class) return offset;
        	
        	throw new IllegalArgumentException("System Workspace does not have children of type " + childType);
        } else {
        	// TODO consider whether this should be instaceOf or strict equality
        	if (childType == WabitDataSource.class) return offset;
        	offset += dataSources.size();

        	if (childType == QueryCache.class) return offset;
        	offset += queries.size();

        	if (childType == OlapQuery.class) return offset;
        	offset += olapQueries.size();

        	if (childType == Chart.class) return offset;
        	offset += charts.size();

        	if (childType == WabitImage.class) return offset;
        	offset += images.size();

        	if (childType == Template.class) return offset;
        	offset += templates.size();

        	if (childType == Report.class) return offset;
        	offset += reports.size();
        	
        	if (childType == User.class) return offset;
        	offset += users.size();
        	
        	if (childType == Group.class) return offset;

        	throw new IllegalArgumentException("Objects of this type don't have children of type " + childType);
        }
    }
    
    public List<QueryCache> getQueries() {
    	return Collections.unmodifiableList(queries);
    }
    
    public List<Report> getReports() {
    	return Collections.unmodifiableList(reports);
    }
    
    public List<Template> getTemplates() {
		return Collections.unmodifiableList(templates);
	}

    /**
     * Returns the first Report child having the given name, or null
     * if no reports have the requested name.
     * 
     * @param name The name to search. If null, null will be returned.
     */
    public Report getReportByName(String name) {
    	if (name == null) return null;
    	for (Report r : reports) {
    		if (name.equals(r.getName())) {
    			return r;
    		}
    	}
    	return null;
    }
    
    public void addUser(User u) {
    	addUser(u, users.size());
    }
    
    public void addUser(User u, int index) {
    	users.add(index, u);
    	fireChildAdded(User.class, u, index);
    }
    
    public void removeUser(User u) {
    	int index = users.indexOf(u);
    	boolean success = users.remove(u);
    	if (success) {
    		fireChildRemoved(User.class, u, index);
    	}
    }
    
    public List<User> getUsers() {
    	return Collections.unmodifiableList(users);
    }
    
    public void addGroup(Group g) {
    	addGroup(g, groups.size());
    }
    
    public void addGroup(Group g, int index) {
    	groups.add(index, g);
    	fireChildAdded(Group.class, g, index);
    }
    
    public void removeGroup(Group g) {
    	int index = groups.indexOf(g);
    	boolean success = groups.remove(g);
    	if (success) {
    		fireChildRemoved(Group.class, g, index);
    	}
    }
    
    public List<Group> getGroups() {
    	return Collections.unmodifiableList(groups);
    }
    
    public boolean isSystemWorkspace() {
    	logger.debug("Workspace UUID is " + getUUID());
    	return getUUID().equals("system");
    }
    
	public WabitObject getParent() {
		return null;
	}
	
	public boolean allowsChildren() {
		return true;
	}

	public void addDataSource(SPDataSource dbcs) {
		String newName = dbcs.getDisplayName();
		if (dsAlreadyAdded(dbcs)) {
			throw new IllegalArgumentException(
					"There is already a datasource with the name " + newName);
		}
		logger.debug("adding SPDataSource");
		addDataSource(new WabitDataSource(dbcs));
		
	}
	
	public boolean dsAlreadyAdded(SPDataSource dbcs) {
		String newName = dbcs.getDisplayName();
		for (WabitDataSource o : dataSources) {
			if (o instanceof WabitDataSource) {
				SPDataSource oneDbcs = ((WabitDataSource) o).getSPDataSource();
				if (newName.equalsIgnoreCase(oneDbcs.getDisplayName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeDataSource(SPDataSource dbcs) {
		logger.debug("removing SPDataSource");
		removeDataSource(new WabitDataSource(dbcs));
	}

	public void addDataSourceType(JDBCDataSourceType dataSourceType) {
		throw new UnsupportedOperationException("We currently do not support this");
		
	}

	public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
		synchronized(listeners) {
			logger.debug("added DatabaseListChangeListener :"+ l.toString());
			listeners.add(l);
		}
	}
	
    private void fireAddEvent(SPDataSource dbcs) {
    	logger.debug("firing databaseAddedEvent :");
		int index = dataSources.size()-1;
		final DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, index, dbcs);
    	synchronized(listeners) {
    	    Runnable runner = new Runnable() {
                public void run() {
                    for(DatabaseListChangeListener listener : listeners) {
                        logger.debug("\n"+ listener.toString());
                        listener.databaseAdded(e);
                    }
                }
            };
            runInForeground(runner);
		}
	}

    private void fireRemoveEvent(int i, SPDataSource dbcs) {
    	logger.debug("firing databaseRemovedEvent:");
    	final DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, i, dbcs);
    	synchronized(listeners) {
    	    Runnable runner = new Runnable() {
                public void run() {
                    for(DatabaseListChangeListener listener : listeners) {
                        logger.debug("\n"+ listener.toString());
                        listener.databaseRemoved(e);
                    }
                }
    	    };
    	    runInForeground(runner);
		}
    }
    
    public List<SPDataSource> getConnections() {
        return getConnections(SPDataSource.class);
    }
    
    public <C extends SPDataSource> List<C> getConnections(Class<C> classType) {
        ArrayList<C> list = new ArrayList<C>();
        Iterator<WabitDataSource> it = dataSources.iterator();
        while (it.hasNext()) {
            WabitDataSource next = it.next();
            if (classType.isInstance(next.getSPDataSource())) {
                list.add(classType.cast(((WabitDataSource)next).getSPDataSource()));
            }
        }
        Collections.sort(list);
        return list;
    }

	public SPDataSource getDataSource(String name) {
	    return getDataSource(name, SPDataSource.class);
	}
	
    public <C extends SPDataSource> C getDataSource(String name,
            Class<C> classType) {
        Iterator<WabitDataSource> it = dataSources.iterator();
        while (it.hasNext()) {
            WabitDataSource next = it.next();
            if (classType.isInstance(next.getSPDataSource())) {
                C ds = classType.cast(((WabitDataSource)next).getSPDataSource());
                if (ds.getName().equals(name)) return ds;
            }
        }
    return null;
    }

	public List<JDBCDataSourceType> getDataSourceTypes() {
		throw new UnsupportedOperationException("We currently do not support this");
	}

	public void mergeDataSource(SPDataSource dbcs) {
		throw new UnsupportedOperationException("We currently do not support this");
	}

	public void mergeDataSourceType(JDBCDataSourceType dst) {
		throw new UnsupportedOperationException("We currently do not support this");
		
	}

	public void read(File location) throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
		
	}

	public void read(InputStream inStream) throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
	}
	

	public boolean removeDataSourceType(JDBCDataSourceType dataSourceType) {
		throw new UnsupportedOperationException("We currently do not support this");
	}

    public URI getServerBaseURI() {
        // IMPORTANT: if you implement this, re-enable the tests for this
        // property in WabitWorkspaceTest and WorkspaceCMLDAOTest!
        throw new UnsupportedOperationException("We currently do not support this");
    }

    public void setServerBaseURI(URI serverBaseURI) {
        // IMPORTANT: if you implement this, re-enable the tests for this
        // property in WabitWorkspaceTest and WorkspaceCMLDAOTest!
        throw new UnsupportedOperationException("We currently do not support this");
    }

	public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
    	synchronized(listeners) {
    		listeners.remove(l);
    	}
	}

	/**
	 * DataSourceCollection interface method which is currently not supported.
	 */
	public void write() throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
	}

    /**
     * DataSourceCollection interface method which is currently not supported.
     */
	public void write(File location) throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
	}

    /**
     * DataSourceCollection interface method which is currently not supported.
     */
	public void write(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("We currently do not support this");
	}
	
	public void addUndoableEditListener(UndoableEditListener l) {
		dsCollectionUndoListeners.add(l);
	}

	public void removeUndoableEditListener(UndoableEditListener l) {
		dsCollectionUndoListeners.remove(l);
	}

	public void setEditorPanelModel(WabitObject editorPanelModel) {
		WabitObject oldEditorPanelModel = this.editorPanelModel;
		this.editorPanelModel = editorPanelModel;
		firePropertyChange("editorPanelModel", oldEditorPanelModel, editorPanelModel);
	}

	public WabitObject getEditorPanelModel() {
		return editorPanelModel;
	}

    public void addOlapQuery(OlapQuery newQuery) {
        addOlapQuery(newQuery, olapQueries.size());
    }
    
    public void addOlapQuery(OlapQuery newQuery, int index) {
        olapQueries.add(index, newQuery);
        newQuery.setParent(this);
        fireChildAdded(OlapQuery.class, newQuery, index);
        setEditorPanelModel(newQuery);
    }

    private boolean removeOlapQuery(OlapQuery query) {
    	int index = olapQueries.indexOf(query);
    	if (index != -1) {
    		olapQueries.remove(query);
    		fireChildRemoved(OlapQuery.class, query, index);
    		if (editorPanelModel == query) {
                setEditorPanelModel(this);
            }
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public List<OlapQuery> getOlapQueries() {
        return Collections.unmodifiableList(olapQueries);
    }
    
    public void removeDependency(WabitObject dependency) {
        //do nothing
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

    /**
     * Locates the WabitObject inside this workspace which has the given UUID,
     * returning null if the item is not found. Throws ClassCastException if in
     * item is found, but it is not of the expected type.
     * 
     * @param <T>
     *            The expected type of the item
     * @param uuid
     *            The UUID of the item
     * @param expectedType
     *            The type of the item with the given UUID. If you are uncertain
     *            what type of object it is, or you do not want a
     *            ClassCastException in case the item is of the wrong type, use
     *            <tt>WabitObject.class</tt> for this parameter.
     * @return The item, or null if no item with the given UUID exists in this
     *         workspace.
     */
    public <T extends WabitObject> T findByUuid(String uuid, Class<T> expectedType) {
        return expectedType.cast(findRecursively(this, uuid));
    }

    /**
     * Performs a preorder traversal of the given WabitObject and its
     * descendants, returning the first WabitObject having the given UUID.
     * Returns null if no such WabitObject exists under startWith.
     * 
     * @param startWith
     *            The WabitObject to start the search with.
     * @param uuid
     *            The UUID to search for
     * @return the first WabitObject having the given UUID in a preorder
     *         traversal of startWith and its descendants. Returns null if no
     *         such WabitObject exists.
     */
    private static WabitObject findRecursively(WabitObject startWith, String uuid) {
        if (uuid.equals(startWith.getUUID())) {
            return startWith;
        }
        for (WabitObject child : startWith.getChildren()) {
            WabitObject found = findRecursively(child, uuid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        if (child instanceof WabitDataSource) {
            return removeDataSource((WabitDataSource) child);
        } else if (child instanceof QueryCache) {
            return removeQuery((QueryCache) child, session);
        } else if (child instanceof OlapQuery) {
            return removeOlapQuery((OlapQuery) child);
        } else if (child instanceof WabitImage) {
            return removeImage((WabitImage) child);
        } else if (child instanceof Chart) {
            return removeChart((Chart) child);
        } else if (child instanceof Template) {
            return removeTemplate((Template) child);
        } else if (child instanceof Report) {
            return removeReport((Report) child);
        } else {
            throw new IllegalStateException("Cannot remove child of type " + child.getClass());
        }
    }
    
    @Override
    protected void addChildImpl(WabitObject child, int index) {
        int innerIndex = index - childPositionOffset(child.getClass());
        if (child instanceof WabitDataSource) {
            addDataSource((WabitDataSource) child, innerIndex);
        } else if (child instanceof QueryCache) {
            addQuery((QueryCache) child, session, innerIndex);
        } else if (child instanceof OlapQuery) {
            addOlapQuery((OlapQuery) child, innerIndex);
        } else if (child instanceof WabitImage) {
            addImage((WabitImage) child, innerIndex);
        } else if (child instanceof Chart) {
            addChart((Chart) child, innerIndex);
        } else if (child instanceof Template) {
            addTemplate((Template) child, innerIndex);
        } else if (child instanceof Report) {
            addReport((Report) child, innerIndex);
        } else {
            throw new AssertionError("Adding child " + child.getName() + " of type " + child.getClass() + 
                    " is not valid for a workspace and should have been checked already");
        }
    }

    /**
     * This method will merge all of the objects in this workspace into the
     * given workspace. Once this workspace has been merged in this workspace
     * can be thrown away as it will have no children and will just be a
     * leftover husk of what it once was.
     * 
     * @param workspace
     *            The workspace to move all of the current workspace's children
     *            into.
     * @return The number of objects imported into the given workspace.
     */
    public int mergeIntoWorkspace(WabitWorkspace workspace) {
        int importObjectCount = 0;
        for (WabitObject importObject : getChildren()) {
            generateNewUUIDsForMerge(importObject);
            if (importObject instanceof WabitDataSource) {
                if (!session.getWorkspace().dsAlreadyAdded(((WabitDataSource) importObject).getSPDataSource())) {
                    removeDataSource((WabitDataSource) importObject);
                    workspace.addDataSource((WabitDataSource) importObject);
                }
            } else if (importObject instanceof QueryCache) {
                removeQuery((QueryCache) importObject, getSession());
                workspace.addQuery((QueryCache) importObject, session);
            } else if (importObject instanceof OlapQuery) {
                removeOlapQuery((OlapQuery) importObject);
                workspace.addOlapQuery((OlapQuery) importObject);
            } else if (importObject instanceof WabitImage) {
                removeImage((WabitImage) importObject);
                workspace.addImage((WabitImage) importObject);
            } else if (importObject instanceof Chart) {
                removeChart((Chart) importObject);
                workspace.addChart((Chart) importObject);
            } else if (importObject instanceof Report) {
                removeReport((Report) importObject);
                workspace.addReport((Report) importObject);
            } else if (importObject instanceof Template) {
                removeTemplate((Template) importObject);
                workspace.addTemplate((Template) importObject);
            } else {
                throw new IllegalStateException("Cannot import the WabitObject type " + importObject.getClass());
            }
            importObjectCount++;
        }
        
        return importObjectCount;
    }

    /**
     * This is a helper method for merging this workspace into another workspace.
     * The given object and all of it's descendants will have its UUID changed
     * to a new UUID. 
     */
    private void generateNewUUIDsForMerge(WabitObject importObject) {
        importObject.generateNewUUID();
        for (WabitObject child : importObject.getChildren()) {
            generateNewUUIDsForMerge(child);
        }
    }
}
