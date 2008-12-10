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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.Query;

/**
 * This action will export the given query as a SQL Script file when it is executed.
 * A prompt will be displayed for users to select a file location as well.
 */
public class ExportSQLScript extends AbstractAction {
	
	private static final String SQL_FILE_EXTENSION = ".sql";

	private final Query query;
	
	/**
	 * A component to parent dialogs to.
	 */
	private final Component parent;

	public ExportSQLScript(Component parent, Query query) {
		super("Export SQL");
		this.parent = parent;
		this.query = query;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select the file to save to.");
		chooser.addChoosableFileFilter(SPSUtils.SQL_FILE_FILTER);
		
		int retval = chooser.showSaveDialog(parent);
		if (retval != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = chooser.getSelectedFile();
		if (!file.getPath().endsWith(SQL_FILE_EXTENSION)) { //$NON-NLS-1$
			file = new File(file.getPath()+SQL_FILE_EXTENSION); //$NON-NLS-1$
        }
		
		FileOutputStream out;
		try {
			 out = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		try {
			writer.write(query.generateQuery());
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
	}

}
