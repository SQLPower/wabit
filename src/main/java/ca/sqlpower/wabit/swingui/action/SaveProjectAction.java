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
import javax.swing.ImageIcon;

import ca.sqlpower.wabit.dao.ProjectXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * This will save the file to the place where the user last loaded or saved.
 * If the user has not yet saved or loaded it will prompt the user for a file location
 * and name.
 */
public class SaveProjectAction extends AbstractAction {
	
	private final WabitSwingSession session;

	public SaveProjectAction(WabitSwingSession session) {
		super("Save", new ImageIcon(SaveProjectAction.class.getClassLoader().getResource("icons/wabit_save.png")));
		this.session = session;
		
	}

	public void actionPerformed(ActionEvent e) {
		save(session);
	}

	/**
	 * Saves the session's project to the file it was opened from or to a user
	 * specified file if the project was new.
	 * 
	 * @param session
	 *            The session to be saved
	 * @return If the save was successful or not
	 */
	public static boolean save(WabitSwingSession session) {
		if (session.getCurrentFile() != null) {
			ProjectXMLDAO projectSaver;
			try {
				projectSaver = new ProjectXMLDAO(new FileOutputStream(session.getCurrentFile()), session.getProject());
			} catch (FileNotFoundException e1) {
				throw new RuntimeException(e1);
			}
			projectSaver.save();
			return true;
		} else {
			return new SaveAsProjectAction(session).save();
		}
	}

}
