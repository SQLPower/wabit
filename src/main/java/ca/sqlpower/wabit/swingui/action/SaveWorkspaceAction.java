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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * This will save the file to the place where the user last loaded or saved.
 * If the user has not yet saved or loaded it will prompt the user for a file location
 * and name.
 */
public class SaveWorkspaceAction extends AbstractAction {
	
    private final WabitSwingSessionContext context;

	public SaveWorkspaceAction(WabitSwingSessionContext context) {
		super("Save All Workspaces", new ImageIcon(SaveWorkspaceAction.class.getClassLoader().getResource("icons/wabit_save.png")));
        this.context = context;
	}

	public void actionPerformed(ActionEvent e) {
		save(context);
	}

	/**
	 * Saves the session's workspace to the file it was opened from or to a user
	 * specified file if the workspace was new.
	 * 
	 * @param session
	 *            The session to be saved
	 * @return If the save was successful or not
	 */
	public static boolean save(WabitSwingSessionContext context) {
		File currentFile = context.getCurrentFile();
        if (currentFile != null) {
			WorkspaceXMLDAO workspaceSaver;
			int lastIndexOfDecimal = currentFile.getName().lastIndexOf(".");
	        if (lastIndexOfDecimal < 0 || !currentFile.getName().substring(lastIndexOfDecimal).equals(SaveWorkspaceAsAction.WABIT_FILE_EXTENSION)) {
	            currentFile = new File(currentFile.getAbsoluteFile() + SaveWorkspaceAsAction.WABIT_FILE_EXTENSION);
	        }
			try {
			    final FileOutputStream out = new FileOutputStream(currentFile);
			    workspaceSaver = new WorkspaceXMLDAO(out, context);
			    out.close();
			} catch (FileNotFoundException e1) {
			    throw new RuntimeException(e1);
			} catch (IOException e) {
			    throw new RuntimeException(e);
			}
			workspaceSaver.save();
			context.putRecentFileName(currentFile.getAbsolutePath());
			return true;
		} else {
			return new SaveWorkspaceAsAction(context).save();
		}
	}

}
