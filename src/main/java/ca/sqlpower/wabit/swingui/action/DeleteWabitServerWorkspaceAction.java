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
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;

import org.apache.http.client.ClientProtocolException;

import ca.sqlpower.wabit.enterprise.client.WabitServerSession;

/**
 * A Swing Action that will send a request to a Wabit Server to delete the
 * given Wabit Workspace on the server.
 */
public class DeleteWabitServerWorkspaceAction extends AbstractAction {
	
	private final WabitServerSession session;

	public DeleteWabitServerWorkspaceAction(WabitServerSession session) {
		super("Delete this workspace");
		this.session = session;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			session.deleteServerWorkspace();
		} catch (ClientProtocolException e1) {
			throw new RuntimeException(e1);
		} catch (URISyntaxException e1) {
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		session.close();
	}
}
