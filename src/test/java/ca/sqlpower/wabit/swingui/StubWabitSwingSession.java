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

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTree;

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;

/**
 * This stubbed swing session does nothing for all of its methods.
 */
public class StubWabitSwingSession implements WabitSwingSession {
	
	WabitSessionContext context = new StubWabitSessionContext();

	public WabitSessionContext getContext() {
		return context;
	}

	public void registerSwingWorker(SPSwingWorker worker) {
		//Do nothing
	}

	public void removeSwingWorker(SPSwingWorker worker) {
		//Do nothing
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
	    // TODO Auto-generated method stub
	    return null;
	}
	
	public JMenu createDataSourcesMenu() {
	    // TODO Auto-generated method stub
	    return null;
	}

	public JFrame getFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setEditorPanel(Object entryPanelModel) {
		// TODO Auto-generated method stub
		
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

	public UserPrompter createUserPrompter(String question, String okText,
			String newText, String notOkText, String cancelText,
			UserPromptType responseType,
			UserPromptResponse defaultResponseType, Object defaultResponse) {
		// TODO Auto-generated method stub
		return null;
	}
}
