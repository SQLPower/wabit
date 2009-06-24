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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * This will import a project into an existing project.
 */
public class ImportWorkspaceAction extends AbstractAction {


	/**
	 * This session contains the project we will be loading into.
	 * This session will also be used to parent dialogs from this action to.
	 */
	private final WabitSwingSessionImpl session;

	public ImportWorkspaceAction(WabitSwingSessionImpl session) {
		super("Import...");
		this.session = session;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(session.getCurrentFile());
		fc.setDialogTitle("Select the file to import from.");
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
		OpenWorkspaceXMLDAO projectLoader = new OpenWorkspaceXMLDAO(session.getContext(), in);
		List<WabitSession> sessions = projectLoader.openWorkspaces();
		for (WabitSession sess : sessions) {
			List<WabitDataSource> dataSources = new ArrayList<WabitDataSource>(sess.getWorkspace().getDataSources());
			for (int i = dataSources.size() - 1; i >= 0; i--) {
				sess.getWorkspace().removeDataSource(dataSources.get(i));
				if (!(session.getWorkspace().getDataSources().contains(dataSources.get(i)))) {
					session.getWorkspace().addDataSource(dataSources.get(i));
				}
			}
			
			List<QueryCache> queries = new ArrayList<QueryCache>(sess.getWorkspace().getQueries());
			for (int i = queries.size() - 1; i >= 0; i--) {
				sess.getWorkspace().removeQuery(queries.get(i), sess);
				session.getWorkspace().addQuery(queries.get(i), session);
			}
			List<Layout> layouts = new ArrayList<Layout>(sess.getWorkspace().getLayouts());
			for (int i = layouts.size() - 1; i >= 0; i--) {
				sess.getWorkspace().removeLayout(layouts.get(i));
				session.getWorkspace().addLayout(layouts.get(i));
			}
			
			sess.getContext().deregisterChildSession(sess);
			
		}
	}

}
