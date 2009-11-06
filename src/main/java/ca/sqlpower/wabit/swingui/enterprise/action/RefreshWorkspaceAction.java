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

package ca.sqlpower.wabit.swingui.enterprise.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * An action that discards an in-memory session then reloads its current
 * contents from the server. Only works on server-based workspaces, of course.
 */
public class RefreshWorkspaceAction extends AbstractAction {

    private final WabitSwingSessionContext context;

    public RefreshWorkspaceAction(WabitSwingSessionContext context) {
        super("Refresh",
              new ImageIcon(RefreshWorkspaceAction.class.getClassLoader()
                      .getResource("icons/32x32/refresh-server.png")));
        this.context = context;
    }
    
    public void actionPerformed(ActionEvent e) {
        WabitSwingSessionImpl activeSwingSession = (WabitSwingSessionImpl) context.getActiveSwingSession();
        if (activeSwingSession == null) {
            JOptionPane.showMessageDialog(context.getFrame(),
                    "That button refreshes the current workspace,\n" +
                    "but there is no workspace selected right now.");
            return;
        }
        activeSwingSession.refresh();
    }
}
