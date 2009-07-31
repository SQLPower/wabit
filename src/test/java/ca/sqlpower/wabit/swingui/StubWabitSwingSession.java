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

package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.List;

import javax.swing.JTree;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel;

/**
 * This stubbed swing session does nothing for all of its methods.
 */
public class StubWabitSwingSession implements WabitSwingSession {
	
	WabitSessionContext context = new StubWabitSessionContext();
	private WabitWorkspace workspace;
	private WabitSession delegateSession;
	
	public StubWabitSwingSession() {
		workspace = new WabitWorkspace();
		delegateSession = new StubWabitSession(context);
	}

	public WabitSessionContext getContext() {
		return context;
	}

	public void addSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		//Do nothing
	}

	public boolean close() {
		//Do nothing
		return false;
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		//Do nothing
	}

	public WabitWorkspace getWorkspace() {
		return workspace;
	}
	
	public DatabaseConnectionManager getDbConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public UserPrompter createUserPrompter(String question, UserPromptType responseType,
			UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
			String ... buttonNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

    public Connection borrowConnection(JDBCDataSource dataSource) {
        // TODO Auto-generated method stub
        return null;
    }

    public JTree getTree() {
        return new JTree();
    }

    public WorkspaceTreeModel getWorkspaceTreeModel() {
        return new WorkspaceTreeModel(workspace);
    }

    public DataSourceCollection<SPDataSource> getDataSources() {
        // TODO Auto-generated method stub
        return null;
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
}
