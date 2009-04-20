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

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSessionContext;
import ca.sqlpower.wabit.swingui.action.OpenProjectOnServerAction;

public class OpenOnServerMenu extends JMenu {
    
    private static final Logger logger = Logger.getLogger(OpenOnServerMenu.class);
    private final WabitServerInfo serviceInfo;
    private final Component dialogOwner;
    
    public OpenOnServerMenu(Component dialogOwner, WabitServerInfo si) {
        super(WabitUtils.serviceInfoSummary(si));
        this.dialogOwner = dialogOwner;
        this.serviceInfo = si;
        refreshProjects();
    }
    
    private Action refreshAction = new AbstractAction("Refresh this list") {
        public void actionPerformed(ActionEvent e) {
            refreshProjects();
        }
    };
    
    private void refreshProjects() {
        logger.debug("Refreshing project list...");
        removeAll();
        try {
            WabitServerSessionContext ctx = WabitServerSessionContext.getInstance(serviceInfo);
            for (String projectName : ctx.getProjectNames()) {
                add(new OpenProjectOnServerAction(dialogOwner, serviceInfo, projectName));
            }
        } catch (Exception ex) {
            JMenuItem mi = new JMenuItem("Error getting project names: " + ex);
            mi.setEnabled(false);
            add(mi);
        }
        addSeparator();
        add(refreshAction);
    }

}
