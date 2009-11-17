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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.enterprise.client.WabitClientSession;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Swing Action that brings up a dialog that allows the user to change the
 * name of the given WabitWorkspace. Once the user has set the name and presed
 * 'OK', it will send a request to a Wabit Server to attempt to change the
 * workspace name.
 */
public class RenameWabitServerWorkspaceAction extends AbstractAction {

	private final WabitClientSession session;
	private final Component dialogParent;

	public RenameWabitServerWorkspaceAction(WabitClientSession session, Component dialogParent) {
		super("Rename workspace...");
		this.session = session;
		this.dialogParent = dialogParent;
	}

	public void actionPerformed(ActionEvent e) {
		final DataEntryPanel renamePanel = createRenamePanel();
		Callable<Boolean> okCall = new Callable<Boolean>() {
			public Boolean call() {
				renamePanel.applyChanges();
				return Boolean.TRUE;
			}
		};
		
		Callable<Boolean> cancelCall = new Callable<Boolean>() {
			public Boolean call() {
				return Boolean.TRUE;
			}
		};
		JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                renamePanel, dialogParent, "Rename workspace", "OK",
                okCall, cancelCall);
		dialog.setVisible(true);
	}

	private DataEntryPanel createRenamePanel() {
		DefaultFormBuilder builder = 
			new DefaultFormBuilder(new FormLayout("pref, 4dlu, max(100dlu; pref):grow"));
        final JTextField nameField = new JTextField(session.getWorkspace().getName());
		builder.append("Name", nameField);
        final JPanel panel = builder.getPanel();
        return new DataEntryPanel() {

			public boolean applyChanges() {
				session.getWorkspace().setName(nameField.getText());
				return true;
			}

			public void discardChanges() {
				//no-op
			}

			public JComponent getPanel() {
				return panel;
			}

			public boolean hasUnsavedChanges() {
				return false;
			}
        };
 	}
}
