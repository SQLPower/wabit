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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.swingui.WabitApplicationPreferencesPanel;

public class ShowWabitApplicationPreferencesAction extends AbstractAction {

	private JDialog dialog;
	
	public ShowWabitApplicationPreferencesAction(Window parent, Preferences prefs) {
		super("Preferences...");
		DataEntryPanel panel = new WabitApplicationPreferencesPanel(prefs);
		dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(panel, parent, "Preferences", "OK");
		
	}
	
	public void actionPerformed(ActionEvent e) {
		dialog.setVisible(true);
	}
}
