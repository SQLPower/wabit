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
import javax.swing.JDialog;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.report.selectors.ComboBoxSelectorPanel;
import ca.sqlpower.wabit.swingui.report.selectors.TextBoxSelectorPanel;

public class EditReportParameterAction extends AbstractAction {

	private final Component dialogOwner;
	private final Selector selector;

	public EditReportParameterAction(
			Component dialogOwner,
			Selector selector) 
	{
		super("", WabitIcons.EDIT_12);
		this.dialogOwner = dialogOwner;
		this.selector = selector;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		final DataEntryPanel dep;
		if (selector instanceof ComboBoxSelector) {
			dep = new ComboBoxSelectorPanel(
						dialogOwner,
						selector.getParent(), 
						(ComboBoxSelector)selector);
		} else if (selector instanceof TextBoxSelector) {
			
			dep = new TextBoxSelectorPanel(
					dialogOwner, 
					(TextBoxSelector)selector);
			
		} else {
			throw new AssertionError();
		}
		
		JDialog dialog = 
			DataEntryPanelBuilder.createDataEntryPanelDialog(
				dep,
		        this.dialogOwner, 
		        "", 
		        "OK");
		
		dialog.setVisible(true);
	}

}
