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
import javax.swing.JTree;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.wabit.WabitSession;

public interface WabitSwingSession extends SwingWorkerRegistry, WabitSession {
	
    Logger getUserInformationLogger();
    
    JFrame getFrame();

	/**
	 * Sets the panel that allows editing of the current selection in the tree.
	 * A new panel will be created based on the type of model passed into this method
	 * if the user has no changes or wants to discard the current changes.
	 */
	void setEditorPanel(Object entryPanelModel);
	
	/**
	 *  Builds and displays the GUI.
	 * @throws ArchitectException 
	 */
	void buildUI() throws ArchitectException;
	
	JTree getTree();
	
	DatabaseConnectionManager getDbConnectionManager();
	
	/**
	 * Set the file that the session was most recently loaded from or saved to.
	 */
	void setCurrentFile(File file);
	
	/**
	 * Get the file that the session was most recently loaded from or saved to.
	 */
	File getCurrentFile();

}