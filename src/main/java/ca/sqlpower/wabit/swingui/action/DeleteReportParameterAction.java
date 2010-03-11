/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.swingui.WabitIcons;

public class DeleteReportParameterAction extends AbstractAction {

	private final Component dialogOwner;
	private final Selector selector;
	private final Runnable reportRefresher;

	public DeleteReportParameterAction(
			Component dialogOwner,
			Selector selector, 
			Runnable reportRefresher) 
	{
		super("", WabitIcons.CLOSE_WORKSPACE);
		this.dialogOwner = dialogOwner;
		this.selector = selector;
		this.reportRefresher = reportRefresher;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		int result = 
				JOptionPane.showConfirmDialog(
						dialogOwner, 
						"Delete this parameter?",
						"",
						JOptionPane.YES_NO_OPTION);
		
		if (result == JOptionPane.YES_OPTION) {
			try {
				selector.cleanup();
				selector.getParent().removeChild(selector);
				SwingUtilities.invokeLater(reportRefresher);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}

}
