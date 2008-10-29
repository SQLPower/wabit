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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shows the log content.
 */
public class LogAction extends AbstractAction {
	
	private JFrame parent;
	private JTextArea logTxtArea;
	final private int LOG_TEXTAREA_ROW_NUM = 25;
	
	public LogAction(JFrame parent, JTextArea aTextArea) {
		this.parent = parent;
		this.logTxtArea = aTextArea;
		super.putValue(AbstractAction.NAME, "Log");
	}

	public void actionPerformed(ActionEvent e) {
		JDialog  dialog = new JDialog(parent, "Log");
		logTxtArea.setRows(LOG_TEXTAREA_ROW_NUM);
		if (logTxtArea.getText().equals(""))
			logTxtArea.setText("No Log found. \t\t");
        dialog.add(new JScrollPane ( logTxtArea ));
        ((JComponent)dialog.getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 5));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
	}
	
}
