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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JSpinner;
import javax.swing.JTree;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;

/**
 * This stubbed swing session does nothing for all of its methods.
 */
public class StubWabitSwingSession implements WabitSwingSession {
	
	WabitSessionContext context = new StubWabitSessionContext();
	private WabitProject wabitProject;
	private WabitSession delegateSession;
	
	private final List<SPSwingWorker> workers = new ArrayList<SPSwingWorker>();
	
	public StubWabitSwingSession() {
		wabitProject = new WabitProject();
		delegateSession = new StubWabitSession(context);
	}

	public WabitSessionContext getContext() {
		return context;
	}

	public void registerSwingWorker(SPSwingWorker worker) {
		workers.add(worker);
	}

	public void removeSwingWorker(SPSwingWorker worker) {
		workers.remove(worker);
	}
	
	public List<SPSwingWorker> getWorkers() {
		return Collections.unmodifiableList(workers);
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

	public Logger getUserInformationLogger() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public WabitProject getProject() {
		return wabitProject;
	}
	
	public JMenu createDataSourcesMenu() {
	    // TODO Auto-generated method stub
	    return null;
	}

	public JFrame getFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean setEditorPanel(WabitObject entryPanelModel) {
		// TODO Auto-generated method stub
		return false;
	}

	public void buildUI() throws SQLObjectException {
		// TODO Auto-generated method stub
		
	}

    public JTree getTree() {
        // TODO Auto-generated method stub
        return null;
    }

	public DatabaseConnectionManager getDbConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getCurrentFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCurrentFile(File file) {
		// TODO Auto-generated method stub
		
	}

	public UserPrompter createUserPrompter(String question, UserPromptType responseType,
			UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse,
			String ... buttonNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public JSpinner getRowLimitSpinner() {
		return new JSpinner();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public int getRowLimit() {
		return 100;
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
        // no-op
    }

    public Connection borrowConnection(SPDataSource dataSource) {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLDatabase getDatabase(SPDataSource dataSource) {
    	return delegateSession.getDatabase(dataSource);
    }
}
