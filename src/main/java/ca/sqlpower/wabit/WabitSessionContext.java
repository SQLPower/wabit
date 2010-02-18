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
import java.sql.Connection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.UserPrompterFactory;

/**
 * A WabitSessionContext provides the basic non-session-specific services that
 * all Wabit code can depend upon. Each live session belongs to exactly one
 * session context, which is where the non-session-specific preferences are
 * stored. The session context manages the lifecycle of the sessions that belong
 * to it.
 * <p>
 * The session context is also a UserPrompterFactory (as are each of the sessions
 * it owns). The session context should be used as a user prompter factory for
 * alerts and questions that are not directly related to a live session, such as
 * questions and warnings that arise during application startup as well as when
 * in the process of opening a Wabit workspace file.
 */
public interface WabitSessionContext extends UserPrompterFactory, SqlConnectionProvider, OlapConnectionProvider {

    /**
     * The service type to look for when discovering enterprise servers on the
     * local network. This is the fully-qualified name of the service, as in
     * <code>_wabitenterprise._tcp.local.</code>.
     */
    public static final String WABIT_ENTERPRISE_SERVER_MDNS_TYPE = "_wabitenterprise._tcp.local.";
    
    /**
     * A constant for storing the global preference for disabling automatic execution of queries
     */
    public static final String DISABLE_QUERY_AUTO_EXECUTE = "disableQueryAutoExecute";
    
	public static final String NEW_WORKSPACE_URL = "/ca/sqlpower/wabit/new_workspace.wabit";
	
    
	DataSourceCollection<SPDataSource> getDataSources();

    /**
     * Adds the given Wabit session to the list of child sessions for this
     * context. This is normally done by the sessions themselves, so you
     * shouldn't need to call this method from your own code.
     * <p>
     * A given session should only be registered with one context at a time. The
     * context a session is registered with should generally be the one returned
     * by the session's {@link WabitSession#getContext() getContext()} method.
     * <p>
     * There is no "deregsiter" method to remove a session from its context.
     * Sessions fire a lifecycle event when they close down; the context listens
     * to all its registered sessions and removes its references to them when
     * they send a sessionClosing event.
     */
	void registerChildSession(WabitSession child);
	
	/**
	 * returns true if the OS is Mac
	 */
	boolean isMacOSX();
	
	/**
	 * This will create an appropriate local session for the current context. Registering
	 * the session with the context should be done immediately or shortly after creating
	 * the session.
	 */
	WabitSession createSession();
	
	/**
     * This will create an appropriate server session for the current context. Registering
     * the session with the context should be done immediately or shortly after creating
     * the session.
     */
    WabitSession createServerSession(SPServerInfo serverInfo);
	
	/**
	 * Returns the number of active sessions in the context.
	 */
	int getSessionCount();
	
	/**
	 * Returns an unmodifiable list of the active sessions in the context.
	 */
	List<WabitSession> getSessions();

	/**
	 * Returns this context's JmDNS client instance.
	 */
	JmDNS getJmDNS();

    /**
     * Returns the list of currently-known enterprise servers. This list will
     * change over time, and may be empty for the first few seconds after
     * startup.
     * 
     * @param includeDiscoveredServers
     *            if true, all known servers will be returned whether they were
     *            configured explicitly or discovered dynamically. If false, only
     *            the explicitly configured servers will be listed.
     * @return contact information for the known enterprise servers
     */
    List<SPServerInfo> getEnterpriseServers(boolean includeDiscoveredServers);

    /**
     * Adds a new user-configured server specification to this context. The
     * information will be added to the {@link #getEnterpriseServers(boolean)}
     * list immediately, and also stored persistently so it will be included in
     * the enterprise server list in future incarnations of WabitSessionContext.
     * 
     * @param serverInfo
     *            The serverInfo object to add.
     */
    void addServer(SPServerInfo serverInfo);

    /**
     * Removes a new user-configured server specification from this context. The
     * information will be removed immediately in the current context, and also
     * from persistent storage.
     * 
     * @param serverInfo
     *            The serverInfo object to remove. If this does not specify a
     *            manually-configured serverInfo (one that was returned by
     *            {@link #getEnterpriseServers(boolean)} with an argument of
     *            <code>true</code>), this method will have no effect.
     */
    void removeServer(SPServerInfo si);
    
    /**
     * Returns the preferences node used by this session context. This should
     * not normally be used by client code; it is primarily intended for use by
     * alternative session and session context implementations.
     */
    Preferences getPrefs();

	/**
	 * This will attempt to close all of the currently opened sessions and stop
	 * the app. Each session will close independently and if any one session
	 * does not close successfully then the closing operation will stop. Once
	 * all sessions have been properly closed the app will terminate. If not
	 * all sessions are properly closed the app will not terminate.
	 */
	void close();

	/**
	 * Returns the name for this session context. If this is server session,
	 * then return the server's name, returns "Local" otherwise.
	 */
	String getName();
	
    /**
     * Borrows a connection to the given data source from this session's
     * connection pool. You must call {@link Connection#close()} on the returned
     * object as soon as you are finished with it.
     * <p>
     * Design note: Equivalent to {@link #getDatabase(SPDataSource)}
     * .getConnection(). Normally we discourage adding convenience methods to an
     * interface, and this is indeed a convenience method on an interface. The
     * reason for this method is to reinforce the idea that connections to data
     * sources must be obtained via the SQLDatabase object held in the session.
     * 
     * @param dataSource
     *            The data source this connection comes from.
     * @return A connection to the given data source, which has been obtained
     *         from a connection pool that this session owns.
     * @throws SQLObjectException
     *             if it is not currently possible to connect to the given data
     *             source. This could be due to the remote database being
     *             unavailable, or an incorrect username or password, a missing
     *             JDBC driver, or many other things.
     */
    public Connection borrowConnection(JDBCDataSource dataSource) throws SQLObjectException;
    
    /**
     * Tells whether or not this session is currently being configured by a DAO.
     * It's not normally necessary to know this from outside the session, but
     * this method had to be public because it's part of the interface.
     */
    public boolean isLoading();

    /**
     * The DAO can tell this context that it's currently being configured based
     * on a workspace file being loaded. When this is the case, certain things
     * (such as GUI updates) will not be performed. If you're not a DAO, it's
     * not necessary or desirable for you to call this method!
     */
    public void startLoading();
    
    /**
     * The DAO can tell this context that it's no longer being configured based
     * on a workspace file being loaded. When this is the case, certain things
     * (such as GUI updates) will not be performed. If you're not a DAO, it's
     * not necessary or desirable for you to call this method!
     */
    public void endLoading();
    
    /**
     * Returns the number of rows that should be retrieved from the database for
     * any result set.
     */
    int getRowLimit();

	/**
	 * This listener will be notified when server information is added or
	 * removed from the server list.
	 */
	public void addServerListListener(ServerListListener l);

	/**
	 * This listener will stop being notified when server information is added
	 * or removed from the server list.
	 */
	public void removeServerListListener(ServerListListener l);
	
    /**
     * Sets the active session that is being viewed or modified. Some events
     * that depend on a session will act on the active session.
     */
    void setActiveSession(WabitSession session);
    
    /**
     * Returns the session that the user is currently viewing or editing.
     */
    WabitSession getActiveSession();
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
}
