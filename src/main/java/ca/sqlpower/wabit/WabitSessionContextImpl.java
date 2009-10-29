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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompter;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSession;
import ca.sqlpower.wabit.enterprise.client.WorkspaceLocation;
import ca.sqlpower.wabit.olap.OlapConnectionPool;

import com.rc.retroweaver.runtime.Collections;

/**
 * This is the canonical headless implementation of WabitSessionContext
 * interface. Other session context implementations that cover more specialized
 * use cases can either extend this one or delegate certain operations to it.
 */
public class WabitSessionContextImpl implements WabitSessionContext {
	
	private static final Logger logger = Logger.getLogger(WabitSessionContextImpl.class);

	/**
	 * This is a preference that stores the location of the pl.ini.
	 */
	private static final String PREFS_PL_INI_PATH = "PL_INI_PATH";
	
    protected JmDNS jmdns;
    private final List<WabitServerInfo> manuallyConfiguredServers = new ArrayList<WabitServerInfo>();
    
	private DataSourceCollection<SPDataSource> dataSources;
	protected final List<WabitSession> childSessions = new ArrayList<WabitSession>();
	
	/**
	 * If this flag is true, this session context will halt the VM when its
	 * last session closes.
	 */
	protected boolean terminateWhenLastSessionCloses;
	
	/**
	 *  Stores true when the OS is MAC
	 */
    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    /**
     * This is the path to the user's pl.ini file.
     */
    private String plDotIniPath;
    
    /**
     * The connection pools we've created due to calling {@link #createConnection(Olap4jDataSource)}.
     */
    private final Map<Olap4jDataSource, OlapConnectionPool> olapConnectionPools = 
        new HashMap<Olap4jDataSource, OlapConnectionPool>();
    
    /**
     * The database instances we've created due to calls to {@link #getDatabase(SPDataSource)}.
     */
    private final Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();
    
    /**
     * This prefs node stores context specific prefs. At current this is the pl.ini location.
     */
    protected final Preferences prefs = Preferences.userNodeForPackage(WabitSessionContextImpl.class);
    
    /**
     * Used to fire property changes.
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * This flag will be true if the context is in the process of loading from a
     * file. During loading some operations may be different because the frame
     * has not been realized.
     * 
     * @see #isLoading()
     */
    private boolean loading;
    
    /**
     * This is the current session that is being changed by the user.
     */
    private WabitSession activeSession;
    
    /**
     * If the data sources have been loaded this will be set to true. It will be false
     * if the data sources still need to be loaded.
     */
    private boolean dataSourcesLoaded = false;
    
    /**
     * This lifecycle listener will remove the session's tree from the tabbed
     * pane when the session is removed.
     */
    private final SessionLifecycleListener<WabitSession> sessionLifecycleListener = new SessionLifecycleListener<WabitSession>() {
    
        public void sessionClosing(SessionLifecycleEvent<WabitSession> e) {
            deregisterChildSession(e.getSource());
        }
    };
    
    /**
     * These listeners will be notified when server information is added or removed from the context.
     */
    private final List<ServerListListener> serverListeners = new ArrayList<ServerListListener>();

	/**
	 * Creates a new Wabit session context.
	 * 
	 * @param terminateWhenLastSessionCloses
	 *            If this flag is true, this session context will halt the VM
	 *            when its last session closes.
	 * @param useJmDNS
	 *            If this flag is true, then this session will create a JmDNS
	 *            instance for searching for Wabit servers.
	 * @throws IOException
	 *             If the startup configuration files can't be read
	 * @throws SQLObjectException
	 *             If the pl.ini is invalid.
	 */
	public WabitSessionContextImpl(boolean terminateWhenLastSessionCloses, boolean useJmDNS) throws IOException, SQLObjectException {
		this(terminateWhenLastSessionCloses, useJmDNS, null);
	}

	/**
	 * Creates a new Wabit session context.
	 * 
	 * @param terminateWhenLastSessionCloses
	 *            If this flag is true, this session context will halt the VM
	 *            when its last session closes.
	 * @param useJmDNS
	 *            If this flag is true, then this session will create a JmDNS
	 *            instance for searching for Wabit servers.
	 * @param initialCollection
	 *            This collection will be used to load the data sources and data
	 *            source types when they are first retrieved. If this is null a
	 *            new data source collection will be created to do the loading.
	 * @throws IOException
	 *             If the startup configuration files can't be read
	 * @throws SQLObjectException
	 *             If the pl.ini is invalid.
	 */
	public WabitSessionContextImpl(boolean terminateWhenLastSessionCloses, boolean useJmDNS, 
			DataSourceCollection<SPDataSource> initialCollection) 
			throws IOException, SQLObjectException {
		this.terminateWhenLastSessionCloses = terminateWhenLastSessionCloses;
		dataSources = initialCollection;
		if (useJmDNS) {
			try {
				jmdns = JmDNS.create();
			} catch (Exception e) {
				jmdns = null;
			}
		} else {
			jmdns = null;
		}
		
        setPlDotIniPath(ArchitectUtils.checkForValidPlDotIni(prefs.get(PREFS_PL_INI_PATH, null), "Wabit"));
        
        try {
            manuallyConfiguredServers.addAll(readServersFromPrefs());
        } catch (BackingStoreException ex) {
            logger.error("Preferences unavailable! Not reading server infos from prefs.", ex);
        }
	}

	/**
	 * Adds the given Wabit session to the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void registerChildSession(WabitSession child) {
		childSessions.add(child);
		child.addSessionLifecycleListener(sessionLifecycleListener);
	}
	
	/**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link #plDotIni}.
     */
    public DataSourceCollection<SPDataSource> getDataSources() {
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (!dataSourcesLoaded) {
        	if (dataSources == null) {
        		dataSources = new PlDotIni();
        	}
        	String iniToLoad = "ca/sqlpower/sql/default_database_types.ini";
            try {
                logger.debug("Reading PL.INI defaults");
                dataSources.read(getClass().getClassLoader().getResourceAsStream(iniToLoad));
                iniToLoad = "/ca/sqlpower/demodata/example_database.ini";
                dataSources.read(WabitSessionContextImpl.class.getResourceAsStream(iniToLoad));
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read system resource " + iniToLoad,e));
            }
            try {
                if (dataSources != null) {
                    logger.debug("Reading new PL.INI instance");
                    dataSources.read(new File(path));
                }
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
            dataSourcesLoaded = true;
        }
        return dataSources;
    }

	/**
	 * Removes the given Wabit session from the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void deregisterChildSession(WabitSession child) {
	    if (!childSessions.contains(child)) {
	        throw new IllegalArgumentException("The session " + child.getWorkspace().getName() + 
	                " does not exist in this context.");
	    }
		childSessions.remove(child);
		child.removeSessionLifecycleListener(sessionLifecycleListener);
		logger.debug("Deregistered a child session " + childSessions.size() + " sessions still remain.");
		
		if (terminateWhenLastSessionCloses && childSessions.isEmpty()) {
			System.exit(0);
		}
	}
	/**
	 * returns true if the OS is Mac
	 * @return
	 */
	public boolean isMacOSX() {
		return MAC_OS_X ; 
	}

	public WabitSession createSession() {
		final WabitSessionImpl session = new WabitSessionImpl(this);
        return session;
	}

	protected void setPlDotIniPath(String plDotIniPath) {
		this.plDotIniPath = plDotIniPath;
	}

	private String getPlDotIniPath() {
		return plDotIniPath;
	}
	
	public int getSessionCount() {
		return childSessions.size();
	}

	public void close() {
	    if (getDataSources() != null && getPlDotIniPath() != null) {
	        try {
	            getDataSources().write(new File(getPlDotIniPath()));
	        } catch (IOException e) {
	            logger.error("Couldn't save PL.INI file!", e); //$NON-NLS-1$
	        }
	    }
		prefs.put(PREFS_PL_INI_PATH, getPlDotIniPath());
		if (jmdns != null) {
			jmdns.close();
		}	
		for (int i = childSessions.size() - 1; i >= 0; i--) {
			if (!childSessions.get(i).close()) {
				return;
			}
		}
	}
	
	/**
	 * Returns the JmDNS instance in this session context. Note that if this
	 * session context was initialzed not to use JmDNS, it will return null.
	 */
	public JmDNS getJmDNS() {
	    return jmdns;
	}
	
	public List<WabitServerInfo> getEnterpriseServers(boolean includeDiscovered) {
	    List<WabitServerInfo> servers = new ArrayList<WabitServerInfo>(manuallyConfiguredServers);
	    if (includeDiscovered && jmdns != null) {
	        for (ServiceInfo si : jmdns.list(WABIT_ENTERPRISE_SERVER_MDNS_TYPE)) {
	            servers.add(new WabitServerInfo(si));
	        }
	    }
	    return servers;
	}

	public void addServer(WabitServerInfo serverInfo) {
        manuallyConfiguredServers.add(serverInfo);
        Preferences servers = getServersPrefNode();
        Preferences thisServer = servers.node(serverInfo.getName());
        thisServer.put("name", serverInfo.getName());
        thisServer.put("serverAddress", serverInfo.getServerAddress());
        thisServer.putInt("port", serverInfo.getPort());
        thisServer.put("path", serverInfo.getPath());
        thisServer.put("username", serverInfo.getUsername());
        thisServer.put("password", serverInfo.getPassword());
        for (int i = serverListeners.size() - 1; i >= 0; i--) {
        	serverListeners.get(i).serverAdded(new ServerListEvent(serverInfo));
        }
    }

    public void removeServer(WabitServerInfo serverInfo) {
        manuallyConfiguredServers.remove(serverInfo);
        Preferences servers = getServersPrefNode();
        try {
            servers.node(serverInfo.getName()).removeNode();
        } catch (BackingStoreException ex) {
            throw new RuntimeException("Failed to remove server from list", ex);
        }
        for (int i = serverListeners.size() - 1; i >= 0; i--) {
        	serverListeners.get(i).serverAdded(new ServerListEvent(serverInfo));
        }
    }
    
    public void addServerListListener(ServerListListener l) {
    	if (l != null) {
    		serverListeners.add(l);
    	}
    }
    
    public void removeServerListListener(ServerListListener l) {
    	serverListeners.remove(l);
    }

    private List<WabitServerInfo> readServersFromPrefs() throws BackingStoreException {
        Preferences serversNode = getServersPrefNode();
        List<WabitServerInfo> serverList = new ArrayList<WabitServerInfo>();
        for (String nodeName : serversNode.childrenNames()) {
            Preferences serverNode = serversNode.node(nodeName);
            
            serverList.add(new WabitServerInfo(
                    serverNode.get("name", null),
                    serverNode.get("serverAddress", null),
                    serverNode.getInt("port", 0),
                    serverNode.get("path", null),
                    serverNode.get("username", ""),
                    serverNode.get("password", "")));
        }
        return serverList;
    }
    
    /**
     * Returns the prefs node under which the manually-configured server infos
     * are stored.
     */
    private Preferences getServersPrefNode() {
        return prefs.node("servers");
    }

    /**
     * Returns the preferences node used by this session context. This should
     * not normally be used by client code; it is primarily intended for use by
     * alternative session and session context implementations.
     */
	public Preferences getPrefs() {
        return prefs;
    }

	public String getName() {
		return "Local";
	}

    /**
     * Returns a user prompter that always gives the default response, since
     * this is a headless session context and there is no user to ask.
     */
    public UserPrompter createUserPrompter(String question,
            UserPromptType responseType, UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse,
            String... buttonNames) {
        return new DefaultUserPrompter(optionType, defaultResponseType, defaultResponse);
    }
    
    @SuppressWarnings("unchecked")
    public List<WabitSession> getSessions() {
        return Collections.unmodifiableList(childSessions);
    }

    public Connection borrowConnection(JDBCDataSource dataSource)
            throws SQLObjectException {
        return getDatabase(dataSource).getConnection();
    }

    public int getRowLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isLoading() {
        return loading;
    }
    
    public void setLoading(boolean loading) {
        boolean oldLoading = this.loading;
        this.loading = loading;
        pcs.firePropertyChange("loading", oldLoading, loading);
    }

    public SQLDatabase getDatabase(JDBCDataSource dataSource) {
        SQLDatabase db = databases.get(dataSource);
        if (db == null && dataSource != null) {
            dataSource = new JDBCDataSource(dataSource);  // defensive copy for cache key
            db = new SQLDatabase(dataSource);
            databases.put(dataSource, db);
            if (logger.isDebugEnabled()) {
                logger.debug("Added new SQLDatabase to map. New map contents:");
                for (Map.Entry<SPDataSource, SQLDatabase> ent : databases.entrySet()) {
                    logger.debug(ent.getKey() + " -> " + ent.getValue());
                }
            }
        }
        return db;
    }

    public OlapConnection createConnection(Olap4jDataSource dataSource) 
    throws SQLException, ClassNotFoundException, NamingException {
        if (dataSource == null) return null;
        OlapConnectionPool olapConnectionPool = olapConnectionPools.get(dataSource);
        if (olapConnectionPool == null) {
            olapConnectionPool = new OlapConnectionPool(dataSource, this);
            olapConnectionPools.put(dataSource, olapConnectionPool);
        }
        return olapConnectionPool.getConnection();
    }
    
    public UserPrompter createDatabaseUserPrompter(String question,
            List<Class<? extends SPDataSource>> dsTypes,
            UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse,
            DataSourceCollection<SPDataSource> dsCollection,
            String... buttonNames) {
        return new DefaultUserPrompter(optionType, defaultResponseType, defaultResponse);
    }
    
    public WabitSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(WabitSession activeSession) {
        WabitSession oldSession = this.activeSession;
        this.activeSession = activeSession;
        pcs.firePropertyChange("activeSession", oldSession, activeSession);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

	public WabitSession createServerSession(WabitServerInfo serverInfo) {
		String newWorkspaceId = UUID.randomUUID().toString();
		WorkspaceLocation workspaceLocation =
			new WorkspaceLocation("Unnamed Workspace", newWorkspaceId, serverInfo);
		WabitServerSession newSession = new WabitServerSession(workspaceLocation, this);
		try {
			newSession.persistWorkspaceToServer();
		} catch (WabitPersistenceException e) {
			throw new RuntimeException("An error occured while persisting new workspace to the Server", e);
		}
		//TODO
		logger.error("have to actually create the session on the server here (following call will cause update thread failure)");
		newSession.startUpdaterThread();
		return newSession;
	}

}
