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
import javax.swing.JFileChooser;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * This will save a given workspace to a user specified file.
 */
public class SaveWorkspaceAsAction extends AbstractAction {
    
    private static final ImageIcon SAVE_WABIT_ICON = 
        new ImageIcon(SaveWorkspaceAsAction.class.getClassLoader().getResource("icons/wabit_save.png"));

	public static final String WABIT_FILE_EXTENSION = ".wabit";
    private final WabitSwingSessionContext context;

	public SaveWorkspaceAsAction(WabitSwingSessionContext context) {
		super("Save Workspace To...", SAVE_WABIT_ICON);
        this.context = context;
	}

	public void actionPerformed(ActionEvent e) {
		save();
	}
	
	/**
	 * Saves the workspace to a user specified file. Returns true if the file was
	 * saved. Returns false if the file was not saved or cancelled.
	 */
	public boolean save() {
	    if (context.getActiveSession() == null) return false;
	    
		JFileChooser fc = new JFileChooser(context.getActiveSwingSession().getCurrentFile());
		fc.setDialogTitle("Select the directory to save to.");
		fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
		
		int fcChoice = fc.showSaveDialog(context.getFrame());

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return false;
		}
		
		File selectedFile = fc.getSelectedFile();
		FileOutputStream out = null;
		int lastIndexOfDecimal = selectedFile.getName().lastIndexOf(".");
		if (lastIndexOfDecimal < 0 || !selectedFile.getName().substring(lastIndexOfDecimal).equals(WABIT_FILE_EXTENSION)) {
		    selectedFile = new File(selectedFile.getAbsoluteFile() + WABIT_FILE_EXTENSION);
		}
		try {
		    out = new FileOutputStream(selectedFile);
		} catch (FileNotFoundException e1) {
		    throw new RuntimeException(e1);
		}
		WorkspaceXMLDAO workspaceSaver = new WorkspaceXMLDAO(out, context);
		workspaceSaver.saveActiveWorkspace();
		try {
		    out.close();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
		
		this.context.getActiveSwingSession().setCurrentFile(selectedFile);
		
		context.putRecentFileName(selectedFile.getAbsolutePath());
		context.setStatusMessage("Saved " + context.getActiveSession().getWorkspace().getName() + " to " +
		        selectedFile.getName());
		
		return true;
	}

}
