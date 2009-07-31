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

import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSession;

public class OpenServerWorkspaceAction extends AbstractAction {

    private final WabitServerInfo serviceInfo;
    private final String workspaceName;
    private final WabitSessionContext context;

    public OpenServerWorkspaceAction(
            Component dialogOwner,
            WabitServerInfo si,
            String workspaceName,
            WabitSessionContext context) {
        super(workspaceName);
        this.serviceInfo = si;
        this.context = context;
        if (si == null) {
            throw new NullPointerException("Null service info");
        }
        this.workspaceName = workspaceName;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            WabitServerSession.openWorkspace(serviceInfo, workspaceName, context);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
