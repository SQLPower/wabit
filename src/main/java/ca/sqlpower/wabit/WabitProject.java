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
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.wabit.report.Layout;

public class WabitProject extends AbstractWabitObject implements DataSourceCollection {
	
	
	private static final Logger logger = Logger.getLogger(WabitProject.class);
	
    /**
     * The data sources that feed the queries for this project.
     */
    private final List<WabitDataSource> dataSources = new ArrayList<WabitDataSource>();
    
    /**
     * The queries that fetch result sets for this project.
     * <p>
     * TODO an SQL query is too specific; we should have a generic result set provider
     * class that could be anything (XPath, SQL query, gdata query, JavaScript that builds a table of data, ...)
     */
    private final List<Query> queries = new ArrayList<Query>();
    
	/**
	 * The list of Listeners to notify when a datasource is added or removed.
	 */
	List<DatabaseListChangeListener> listeners;
    
    /**
     * The report layouts in this project.
     */
    private final List<Layout> layouts = new ArrayList<Layout>();
    
    /**
     * TODO: These listeners are never fired at current as they are only used for
     * DS Type undo events in the library currently. These listeners are unused
     * until the project supports changing DS Types or other undoable edits are
     * needed for the DS Collection.
     */
    private final List<UndoableEditListener> dsCollectionUndoListeners = new ArrayList<UndoableEditListener>();
    
    /**
     * This is the current editor panel's model that is being being edited.
     * This allows the project to know what panel to load when it is loaded. 
     */
    private WabitObject editorPanelModel;

    public WabitProject() {
    	listeners = new ArrayList<DatabaseListChangeListener>();
        setName("New Project");
    }
    
    public List<WabitObject> getChildren() {
        List<WabitObject> allChildren = new ArrayList<WabitObject>();
        allChildren.addAll(dataSources);
        allChildren.addAll(queries);
        allChildren.addAll(layouts);
        return allChildren;
    }
    
    public void addDataSource(WabitDataSource ds) {
    	logger.debug("adding WabitDataSource");
        int index = dataSources.size();
        dataSources.add(index, ds);
        ds.setParent(this);
        fireChildAdded(WabitDataSource.class, ds, index);
        if(ds instanceof JDBCDataSource) {
        	fireAddEvent(((JDBCDataSource)ds).getSPDataSource());
        }
    }

    public boolean removeDataSource(WabitDataSource ds) {
    	logger.debug("removing WabitDataSource");
    	int index = dataSources.indexOf(ds);
    	if (index != -1) {
    		dataSources.remove(ds);
    		fireChildRemoved(WabitDataSource.class, ds, index);
    		if(ds instanceof JDBCDataSource) {
    			fireRemoveEvent(index, ((JDBCDataSource)ds).getSPDataSource());
    		}
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Returns an unmodifiable view of the data sources in this project.
     * <p>
     * TODO change this to List&lt;WabitDataSource&gt; getDatabases()
     */
    public List<WabitDataSource> getDataSources() {
        return Collections.unmodifiableList(dataSources);
    }
    
    public void addQuery(Query query, WabitSession session) {
        int index = queries.size();
        queries.add(index, query);
        query.setParent(this);
        query.setSession(session);
        fireChildAdded(Query.class, query, index);
        setEditorPanelModel(query);
    }

    public boolean removeQuery(Query query) {
    	int index = queries.indexOf(query);
    	if (index != -1) {
    		query.cleanup();
    		queries.remove(query);
    		fireChildRemoved(Query.class, query, index);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void addLayout(Layout layout) {
        int index = layouts.size();
        layouts.add(index, layout);
        layout.setParent(this);
        fireChildAdded(Layout.class, layout, index);
        setEditorPanelModel(layout);
    }
    
    public boolean removeLayout(Layout layout) {
    	int index = layouts.indexOf(layout);
    	if (index != -1) {
    		layouts.remove(layout);
    		fireChildRemoved(Layout.class, layout, index);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public int childPositionOffset(Class<? extends WabitObject> childType) {
        int offset = 0;

        // TODO consider whether this should be instaceOf or strict equality
        if (childType == WabitDataSource.class) return offset;
        offset += dataSources.size();

        if (childType == Query.class) return offset;
        offset += queries.size();
        
        if (childType == Layout.class) return offset;
        
        throw new IllegalArgumentException("Objects of this type don't have children of type " + childType);
    }
    
    public List<Query> getQueries() {
    	return Collections.unmodifiableList(queries);
    }
    
    public List<Layout> getLayouts() {
    	return Collections.unmodifiableList(layouts);
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
		addDataSource(new JDBCDataSource(dbcs));
		
	}
	
	public boolean dsAlreadyAdded(SPDataSource dbcs) {
		String newName = dbcs.getDisplayName();
		for (WabitDataSource o : dataSources) {
			if (o instanceof JDBCDataSource) {
				SPDataSource oneDbcs = ((JDBCDataSource) o).getSPDataSource();
				if (newName.equalsIgnoreCase(oneDbcs.getDisplayName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeDataSource(SPDataSource dbcs) {
		logger.debug("removing SPDataSource");
		removeDataSource(new JDBCDataSource(dbcs));
	}

	public void addDataSourceType(SPDataSourceType dataSourceType) {
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
		DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, index, dbcs);
    	synchronized(listeners) {
			for(DatabaseListChangeListener listener : listeners) {
				logger.debug("\n"+ listener.toString());
				listener.databaseAdded(e);
			}
		}
	}

    private void fireRemoveEvent(int i, SPDataSource dbcs) {
    	logger.debug("firing databaseRemovedEvent:");
    	DatabaseListChangeEvent e = new DatabaseListChangeEvent(this, i, dbcs);
    	synchronized(listeners) {
			for(DatabaseListChangeListener listener : listeners) {
				logger.debug("\n"+ listener.toString());
				listener.databaseRemoved(e);
			}
		}
    }

    public List<SPDataSource> getConnections() {
    	ArrayList<SPDataSource> list = new ArrayList<SPDataSource>();
    	Iterator it = dataSources.iterator();
    	while (it.hasNext()) {
    		Object next = it.next();
    		if (next instanceof JDBCDataSource) {
    			list.add(((JDBCDataSource)next).getSPDataSource());
    		}
    	}
    	Collections.sort(list, new SPDataSource.DefaultComparator());
    	return list;
    }

	public SPDataSource getDataSource(String name) {
	     Iterator it = dataSources.iterator();
	        while (it.hasNext()) {
	            Object next = it.next();
	            if (next instanceof JDBCDataSource) {
	                SPDataSource ds = ((JDBCDataSource)next).getSPDataSource();
	                if (ds.getName().equals(name)) return ds;
	            }
	        }
		return null;
	}

	public List<SPDataSourceType> getDataSourceTypes() {
		throw new UnsupportedOperationException("We currently do not support this");
	}

	public void mergeDataSource(SPDataSource dbcs) {
		throw new UnsupportedOperationException("We currently do not support this");
	}

	public void mergeDataSourceType(SPDataSourceType dst) {
		throw new UnsupportedOperationException("We currently do not support this");
		
	}

	public void read(File location) throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
		
	}

	public void read(InputStream inStream) throws IOException {
		throw new UnsupportedOperationException("We currently do not support this");
	}
	

	public boolean removeDataSourceType(SPDataSourceType dataSourceType) {
		throw new UnsupportedOperationException("We currently do not support this");
	}

    public URI getServerBaseURI() {
        // IMPORTANT: if you implement this, re-enable the tests for this
        // property in WabitProjectTest and ProjectCMLDAOTest!
        throw new UnsupportedOperationException("We currently do not support this");
    }

    public void setServerBaseURI(URI serverBaseURI) {
        // IMPORTANT: if you implement this, re-enable the tests for this
        // property in WabitProjectTest and ProjectCMLDAOTest!
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
}
