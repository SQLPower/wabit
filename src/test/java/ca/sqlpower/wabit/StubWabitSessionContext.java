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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.naming.NamingException;

import org.olap4j.OlapConnection;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

public class StubWabitSessionContext implements WabitSessionContext {
	
	private final Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();

	public DataSourceCollection getDataSources() {
		return null;
	}

	public boolean isMacOSX() {
		return false;
	}

	public void registerChildSession(WabitSession child) {
	    // no op
	}

	public WabitSession createSession() {
		return new StubWabitSession(this);
	}

	public int getSessionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public JmDNS getJmDNS() {
	    return null;
	}

    public Preferences getPrefs() {
        return null;
    }

	public String getName() {
		return null;
	}

    public void addServer(SPServerInfo serverInfo) {
        // TODO Auto-generated method stub
        
    }

    public List<SPServerInfo> getEnterpriseServers(
            boolean includeDiscoveredServers) {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeServer(SPServerInfo si) {
        // TODO Auto-generated method stub
        
    }

    public UserPrompter createUserPrompter(String question,
            UserPromptType responseType, UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse,
            String... buttonNames) {
        return null;
    }

    public Connection borrowConnection(JDBCDataSource dataSource)
            throws SQLObjectException {
        // TODO Auto-generated method stub
        return null;
    }

    public WabitSession createServerSession(SPServerInfo serverInfo) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRowLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<WabitSession> getSessions() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isLoading() {
        // TODO Auto-generated method stub
        return false;
    }

    public void startLoading() {
    	// TODO Auto-generated method stub
    	
    }
    
    public void endLoading() {
    	// TODO Auto-generated method stub
    	
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
    	if (ds == null) return null;
        SQLDatabase db = databases.get(ds);
        if (db == null) {
            ds = new JDBCDataSource(ds);
            db = new SQLDatabase(ds);
            databases.put(ds, db);
        }
        return db;
    }

    public OlapConnection createConnection(Olap4jDataSource dataSource)
            throws SQLException, ClassNotFoundException, NamingException {
        // TODO Auto-generated method stub
        return null;
    }

	public UserPrompter createDatabaseUserPrompter(String question,
			List<Class<? extends SPDataSource>> dsTypes,
			UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			DataSourceCollection<SPDataSource> dsCollection,
			String... buttonNames) {
		return null;
	}

	public void addServerListListener(ServerListListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeServerListListener(ServerListListener l) {
		// TODO Auto-generated method stub
		
	}

    public WabitSession getActiveSession() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setActiveSession(WabitSession session) {
        // TODO Auto-generated method stub
        
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // TODO Auto-generated method stub
        
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        // TODO Auto-generated method stub
        
    }

}
