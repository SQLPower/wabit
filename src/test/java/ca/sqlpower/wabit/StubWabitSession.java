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

import javax.naming.NamingException;

import org.olap4j.OlapConnection;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.olap.OlapConnectionPool;

public class StubWabitSession implements WabitSession {
	
	private final WabitSessionContext context;
	private WabitWorkspace workspace;
	private final Map<SPDataSource, SQLDatabase> databases = new HashMap<SPDataSource, SQLDatabase>();

    /**
     * The connection pools we've created due to calling {@link #createConnection(Olap4jDataSource)}.
     */
    private final Map<Olap4jDataSource, OlapConnectionPool> olapConnectionPools = new HashMap<Olap4jDataSource, OlapConnectionPool>();
	
	public StubWabitSession(WabitSessionContext context) {
		this.context = context;
		workspace = new WabitWorkspace();
		workspace.setSession(this); // XXX leaking a reference to partially-constructed session!
	}
	
	public void addSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		// TODO Auto-generated method stub

	}

	public boolean close() {
	    for (SQLDatabase db : databases.values()) {
	        db.disconnect();
	    }
	    return true;
	}

	public WabitSessionContext getContext() {
		return context;
	}

	public WabitWorkspace getWorkspace() {
		return workspace;
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		// TODO Auto-generated method stub

	}

	public UserPrompter createUserPrompter(String question,
			UserPromptType responseType, UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			String... buttonNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public int getRowLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public void setRowLimit(int newLimit) {
		// TODO Auto-generated method stub
		
	}

    public boolean isLoading() {
        return false;
    }

    public void setLoading(boolean loading) {
        // no op
    }

    public Connection borrowConnection(JDBCDataSource dataSource) throws SQLObjectException {
        return getDatabase(dataSource).getConnection();
    }

    public SQLDatabase getDatabase(JDBCDataSource dataSource) {
    	if (dataSource == null) return null;
        SQLDatabase db = databases.get(dataSource);
        if (db == null) {
            dataSource = new JDBCDataSource(dataSource);
            db = new SQLDatabase(dataSource);
            databases.put(dataSource, db);
        }
        return db;
    }

    public OlapConnection createConnection(Olap4jDataSource dataSource)
            throws SQLException, ClassNotFoundException, NamingException {
    	if (dataSource == null) return null;
	    OlapConnectionPool olapConnectionPool = olapConnectionPools.get(dataSource);
	    if (olapConnectionPool == null) {
	        olapConnectionPool = new OlapConnectionPool(dataSource, context);
	        olapConnectionPools.put(dataSource, olapConnectionPool);
	    }
	    return olapConnectionPool.getConnection();
    }

    public DataSourceCollection<SPDataSource> getDataSources() {
        return this.context.getDataSources();
    }

	public UserPrompter createDatabaseUserPrompter(String question,
			List<Class<? extends SPDataSource>> dsTypes,
			UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			DataSourceCollection<SPDataSource> dsCollection,
			String... buttonNames) {
		// TODO Auto-generated method stub
		return null;
	}

    public void runInBackground(Runnable runner) {
        runner.run();
    }

    public void runInForeground(Runnable runner) {
        runner.run();
    }

	public boolean isEnterpriseServerSession() {
		return false;
	}

	public WabitWorkspace getSystemWorkspace() {
		return null;
	}
}
