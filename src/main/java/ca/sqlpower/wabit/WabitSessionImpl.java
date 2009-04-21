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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;


public class WabitSessionImpl implements WabitSession {

	private WabitSessionContext sessionContext;
	
	private WabitProject project;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private boolean loading;
	
	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();
	
    /**
     * The database instances we've created due to calls to {@link #getDatabase(SPDataSource)}.
     */
    private final Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();

    public WabitSessionImpl(WabitSessionContext context) {
    	this.sessionContext = context;
    	project = new WabitProject();
		sessionContext.registerChildSession(this);
    }
    
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void addSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.add(l);
	}

	public Connection borrowConnection(SPDataSource dataSource)
			throws SQLObjectException {
		return getDatabase(dataSource).getConnection();
	}

	public boolean close() {
    	SessionLifecycleEvent<WabitSession> lifecycleEvent =
    		new SessionLifecycleEvent<WabitSession>(this);
    	for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
    		lifecycleListeners.get(i).sessionClosing(lifecycleEvent);
    	}
    	
    	sessionContext.deregisterChildSession(this);
    	
    	for (SQLDatabase db : databases.values()) {
    	    db.disconnect();
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

	public WabitProject getProject() {
		return project;
	}

	public int getRowLimit() {
		return 0;
	}

	public SQLDatabase getDatabase(SPDataSource dataSource) {
		SQLDatabase db = databases.get(dataSource);
        if (db == null) {
            dataSource = new SPDataSource(dataSource);  // defensive copy for cache key
            db = new SQLDatabase(dataSource);
            databases.put(dataSource, db);
        }
        return db;
	}

	public boolean isLoading() {
		return loading;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.remove(l);
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

}
