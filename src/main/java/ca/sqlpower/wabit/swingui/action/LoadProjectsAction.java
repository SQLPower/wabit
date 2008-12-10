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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.dao.LoadProjectXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * This action will load in projects from a user selected file to a given
 * context.
 */
public class LoadProjectsAction extends AbstractAction {

	/**
	 * This is the context within Wabit that will have the projects
	 * loaded into.
	 */
	private final WabitSessionContext context;
	
	/**
	 * This session will be used to parent dialogs from this action to.
	 */
	private final WabitSwingSession session;

	public LoadProjectsAction(WabitSwingSession session, WabitSessionContext context) {
		super("Load Project");
		this.session = session;
		this.context = context;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(session.getCurrentFile());
		fc.setDialogTitle("Select the file to load from.");
		fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
		
		File importFile = null;
		int fcChoice = fc.showOpenDialog(session.getFrame());

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return;
		}
		importFile = fc.getSelectedFile();

		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(importFile));
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		LoadProjectXMLDAO projectLoader = new LoadProjectXMLDAO(context, in);
		List<WabitSession> sessions = projectLoader.loadProjects();
		for (WabitSession session : sessions) {
			try {
				((WabitSwingSession)session).buildUI();
				((WabitSwingSession)session).setCurrentFile(importFile);
			} catch (ArchitectException e1) {
				throw new RuntimeException(e1);
			}
		}
		
	}

}
