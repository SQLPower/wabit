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

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.SPSession;

/**
 * The basic interface for a Wabit session. This interface provides all the
 * UI-independent state and behaviour of a Wabit session. 
 */
public interface WabitSession extends SPSession{

	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	/**
	 * Returns the context this session belongs to.
	 */
	public WabitSessionContext getContext();

	/**
	 * Ends this session, disposing its frame and releasing any system
	 * resources that were obtained explicitly by this session. Also
	 * fires a sessionClosing lifecycle event, so any resources used up
	 * by subsystems dependent on this session can be freed by the appropriate
	 * parties.
	 * 
	 * @return True if the session was successfully closed. False if the
	 * session did not close due to an error or user intervention.
	 */
	public boolean close();

	/**
	 * Returns the workspace associated with this session.
	 */
	public WabitWorkspace getWorkspace();
	
    void addPropertyChangeListener(PropertyChangeListener l);
    
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Returns a collection of all the data sources that are available to this
     * session. The data sources do not have to be in the session's workspace.
     * All of the data sources in the session's workspace will be in this
     * collection.
     */
    DataSourceCollection<SPDataSource> getDataSources();

    /**
     * Returns true if the current session is in fact a remote session
     * with the wabit enterprise server.
     * @return
     */
    public boolean isEnterpriseServerSession();
    
    /**
     * This method might return a WabitWorkspace, the system one, located on the
     * Wabit server, if the current session is backed by a remote server session.
     * It will return null if it is not a server session or the user doesn't
     * have access to the system workspace. One can also use isEnterpriseSession()
     * to verify if this call will return null beforehand.
     * @return
     */
    public WabitWorkspace getSystemWorkspace();
}
