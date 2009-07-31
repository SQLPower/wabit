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
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * This action will export a given query to a file that
 * can be opened as a separate workspace.
 */
public class ExportQueryAction extends AbstractAction {

	private final WabitSwingSession session;
	private final WabitSwingSessionContext context;
	private final QueryCache query;

	public ExportQueryAction(WabitSwingSession session, QueryCache query) {
		super("", new ImageIcon(ExportQueryAction.class.getClassLoader().getResource("icons/wabit-exportQuery.png")));
		this.session = session;
		context = (WabitSwingSessionContext) session.getContext();
		this.query = query;
		
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(context.getCurrentFile());
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
		
		workspaceSaver.save(Collections.singletonList(query));

	}

}
