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
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSession;
import ca.sqlpower.wabit.swingui.action.OpenServerWorkspaceAction;

public class OpenOnServerMenu extends JMenu {
    
    private static final Logger logger = Logger.getLogger(OpenOnServerMenu.class);
    private final WabitServerInfo serviceInfo;
    private final Component dialogOwner;
    private final WabitSessionContext context;
    
    public OpenOnServerMenu(Component dialogOwner, WabitServerInfo si, WabitSessionContext context) {
        super(WabitUtils.serviceInfoSummary(si));
        this.dialogOwner = dialogOwner;
        this.serviceInfo = si;
        this.context = context;
        refresh();
    }
    
    private Action refreshAction = new AbstractAction("Refresh this list") {
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    };
    
    /**
     * Reconstructs the menu items contained by this menu based on the current
     * list of workspaces the session context knows about.
     */
    private void refresh() {
        logger.debug("Refreshing workspace list...");
        removeAll();
        try {
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 2000);
            HttpClient httpClient = new DefaultHttpClient(params);
            for (String workspaceName : WabitServerSession.getWorkspaceNames(httpClient, serviceInfo)) {
                add(new OpenServerWorkspaceAction(dialogOwner, serviceInfo, workspaceName, context));
            }
        } catch (Exception ex) {
            JMenuItem mi = new JMenuItem("Error getting workspace names: " + ex);
            mi.setEnabled(false);
            add(mi);
        }
        addSeparator();
        add(refreshAction);
    }

}
