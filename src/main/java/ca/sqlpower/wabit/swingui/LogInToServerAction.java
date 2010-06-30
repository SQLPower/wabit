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

package ca.sqlpower.wabit.swingui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitClientSession;

/**
 * An action that, when invoked, opens all visible sessions on a specific target server.
 */
public class LogInToServerAction extends AbstractAction {

    private final SPServerInfo serviceInfo;
    private final Component dialogOwner;
    private final WabitSessionContext context;
    
    public LogInToServerAction(
    		Component dialogOwner, 
    		SPServerInfo si, 
    		WabitSessionContext context) {
        super(SQLPowerUtils.serviceInfoSummary(si));
        this.dialogOwner = dialogOwner;
        this.serviceInfo = si;
        this.context = context;
    }

	public void actionPerformed(ActionEvent e) {
		try {
			WabitClientSession.openServerSessions(context, serviceInfo);
		} catch (Exception ex) {
			SPSUtils.showExceptionDialogNoReport(dialogOwner,
					"Log in to server "
							+ SQLPowerUtils.serviceInfoSummary(serviceInfo)
							+ "failed.", ex);
		}
	}

}
