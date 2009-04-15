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

import java.io.OutputStream;
import java.util.List;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import ca.sqlpower.sql.DataSourceCollection;

public interface WabitSessionContext {

    /**
     * The service type to look for when discovering enterprise servers on the
     * local network. This is the fully-qualified name of the service, as in
     * <code>_wabitenterprise._tcp.local.</code>.
     */
    public static final String WABIT_ENTERPRISE_SERVER_MDNS_TYPE = "_wabitenterprise._tcp.local.";
    
	DataSourceCollection getDataSources();
	
	/**
	 * Adds the given Wabit session to the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	void registerChildSession(WabitSession child);

	/**
	 * Removes the given Wabit session from the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	void deregisterChildSession(WabitSession child);
		
	/**
	 * returns true if the OS is Mac
	 */
	boolean isMacOSX();
	
	/**
	 * This will create an appropriate session for the current context and will
	 * register the session with the context.
	 */
	WabitSession createSession();
	
	/**
	 * Returns the number of active sessions in the context.
	 */
	int getSessionCount();

	/**
	 * Returns this context's JmDNS client instance.
	 */
	JmDNS getJmDNS();
	
	/**
     * Returns the list of currently-known enterprise servers. This list will change over
     * time, and may be empty for the first few seconds after startup.
     * 
     * @return contact information for the known enterprise servers
     */
    List<ServiceInfo> getEnterpriseServers();

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
}
