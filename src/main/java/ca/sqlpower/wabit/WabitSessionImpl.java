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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;


public class WabitSessionImpl implements WabitSession {

	private WabitSessionContext sessionContext;
	
	private WabitWorkspace workspace;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();
	
    public WabitSessionImpl(WabitSessionContext context) {
    	this.sessionContext = context;
    	workspace = new WabitWorkspace();
    	workspace.setSession(this); // XXX leaking a reference to partially-constructed session!
    }
    
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void addSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.add(l);
	}

	public boolean close() {
	    CleanupExceptions cleanupObject = WabitUtils.cleanupWabitObject(workspace);
	    WabitUtils.displayCleanupErrors(cleanupObject, sessionContext);
	    
    	SessionLifecycleEvent<WabitSession> lifecycleEvent =
    		new SessionLifecycleEvent<WabitSession>(this);
    	for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
    		lifecycleListeners.get(i).sessionClosing(lifecycleEvent);
    	}
    	
    	return true;
	}

	public UserPrompter createUserPrompter(String question,
			UserPromptType responseType, UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			String... buttonNames) {
		DefaultUserPrompterFactory dupf = new DefaultUserPrompterFactory();
		return dupf.createUserPrompter(question, responseType, optionType, defaultResponseType, defaultResponse, buttonNames);
	}

	public WabitSessionContext getContext() {
		return sessionContext;
	}

	public WabitWorkspace getWorkspace() {
		return workspace;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.remove(l);
	}

    public DataSourceCollection<SPDataSource> getDataSources() {
        return sessionContext.getDataSources();
    }
	
}
