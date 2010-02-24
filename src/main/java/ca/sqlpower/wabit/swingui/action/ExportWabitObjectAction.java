/*
 * Copyright (c) 2009, SQL Power Group Inc.
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
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * An action for exporting a {@link WabitObject} into a workspace file. It uses
 * Wabit's saving mechanisms to save the given object and all other WabitObjects
 * it depends on. For example, if you're exporting a {@link QueryCache}, then
 * this action should also export the database connection that it depends on as
 * well.
 * 
 * The resulting file can be opened as a separate Workspace, or it can be
 * imported into another Workspace.
 * 
 * @param <T>
 */
public class ExportWabitObjectAction<T extends WabitObject> extends AbstractAction {

	private final WabitSwingSession session;
	private final WabitSwingSessionContext context;
	private final T object;

	/**
	 * @param session
	 *            The session that the WabitObject belongs to
	 * @param object
	 *            The WabitObject to be saved
	 * @param icon
	 *            An Icon to associate with the action for controls, such as
	 *            buttons
	 * @param description
	 *            A short description of the action that is used as a tooltip
	 */
	public ExportWabitObjectAction(WabitSwingSession session, T object, Icon icon, String description) {
		super("", icon);
		putValue(SHORT_DESCRIPTION, description);
		this.session = session;
		this.object = object;
		context = (WabitSwingSessionContext) session.getContext();
	}

    public void actionPerformed(ActionEvent e) {
	    File defaultFile = null;
	    if (context.getActiveSession() != null) {
	        defaultFile = context.getActiveSwingSession().getCurrentURIAsFile();
	    }
		JFileChooser fc = new JFileChooser(defaultFile);
		fc.setDialogTitle("Select the file to save to.");
		fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
		
		int fcChoice = fc.showSaveDialog(context.getFrame());

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return;
		}
		
		FileOutputStream out = null;
		try {
			File selectedFile = fc.getSelectedFile();

			if (!selectedFile.getPath().endsWith(SaveWorkspaceAsAction.WABIT_FILE_EXTENSION)) { //$NON-NLS-1$
				selectedFile = new File(selectedFile.getPath()+SaveWorkspaceAsAction.WABIT_FILE_EXTENSION); //$NON-NLS-1$
            }
			
			out = new FileOutputStream(selectedFile);
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		WorkspaceXMLDAO workspaceSaver = new WorkspaceXMLDAO(out, session.getContext());
		
		workspaceSaver.save(Collections.singletonList(object));

	}

}
