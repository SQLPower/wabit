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
import javax.swing.JOptionPane;

import org.apache.http.client.ClientProtocolException;

import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * A Swing Action that will send a request to a Wabit Server to delete the
 * given Wabit Workspace on the server.
 */
public class DeleteWabitServerWorkspaceAction extends AbstractAction {
	
	private final WabitSwingSessionContext context;

	public DeleteWabitServerWorkspaceAction(WabitSwingSessionContext context) {
		super("Delete this workspace");
		this.context = context;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			WabitSwingSessionImpl activeSwingSession = (WabitSwingSessionImpl) context.getActiveSwingSession();
	        if (activeSwingSession == null) {
	            JOptionPane.showMessageDialog(context.getFrame(),
	                    "That button refreshes the current workspace,\n" +
	                    "but there is no workspace selected right now.");
	            return;
	        }
	        activeSwingSession.delete();
		} catch (ClientProtocolException ex) {
			throw new RuntimeException(ex);
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
