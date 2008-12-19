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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import ca.sqlpower.wabit.dao.ProjectXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * This will save a given project to a user specified file.
 */
public class SaveProjectAction extends AbstractAction {

	/**
	 * The project in this session will be saved to a file.
	 */
	private final WabitSwingSession session;

	public SaveProjectAction(WabitSwingSession session) {
		super("Save Project");
		this.session = session;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select the file to save to.");
	
		int fcChoice = fc.showSaveDialog(session.getFrame());

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return;
		}
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fc.getSelectedFile());
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		ProjectXMLDAO projectSaver = new ProjectXMLDAO(out, session.getProject());
		
		projectSaver.save();
	}

}
