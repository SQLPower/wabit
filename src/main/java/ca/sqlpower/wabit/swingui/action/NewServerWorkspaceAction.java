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

import javax.swing.AbstractAction;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.WabitClientSession;
import ca.sqlpower.wabit.enterprise.client.WorkspaceLocation;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

public class NewServerWorkspaceAction extends AbstractAction {

	private final WabitSwingSessionContext context;
	private final SPServerInfo serviceInfo;
	private final Component dialogOwner;

	public NewServerWorkspaceAction(
			Component dialogOwner, 
			WabitSwingSessionContext context,
			SPServerInfo server) {
		super(server.toString());
		this.dialogOwner = dialogOwner;
		this.context = context;
		this.serviceInfo = server;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			WorkspaceLocation workspaceLoc = WabitClientSession.createNewServerSession(serviceInfo);
			WabitClientSession.openServerSession(context, workspaceLoc);
		} catch (Exception ex) {
			SPSUtils.showExceptionDialogNoReport(dialogOwner,
					"Log in to server "
							+ WabitUtils.serviceInfoSummary(serviceInfo)
							+ "failed.", ex);
		}
	}

}
