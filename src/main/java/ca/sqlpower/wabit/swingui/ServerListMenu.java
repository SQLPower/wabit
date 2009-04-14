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
import java.util.List;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitSessionContext;

/**
 * A JMenu which maintains its own set of entries based on services discovered by mDNS information.
 */
public class ServerListMenu extends JMenu {

    private static final Logger logger = Logger.getLogger(ServerListMenu.class);

    private final WabitSwingSessionContext context;

    private final Component dialogOwner;

    private final ServerListMenuItemFactory itemFactory;

    /**
     * Creates a throwaway popup menu containing the current list of servers.
     * The resulting popup menu will not change over time like the regular
     * ServerListMenu does, so you should make a new popup instance every time
     * you need one.
     */
    public static JPopupMenu createPopupInstance(WabitSwingSessionContext context, Component dialogOwner) {
        JPopupMenu popup = new JPopupMenu();
        List<ServiceInfo> servers = context.getEnterpriseServers();
        if (servers.isEmpty()) {
            JMenuItem mi = new JMenuItem("Searching for servers...");
            mi.setEnabled(false);
            popup.add(mi);
        } else {
            for (ServiceInfo si : servers) {
                popup.add(new OpenOnServerMenu(dialogOwner, si));
            }
        }
        return popup;
    }

    /**
     * Creates a new server list menu that registers itself as a listener on the
     * context's JmDNS instance. This menu will alter its contents to match the
     * set of currently-available servers on the network.
     * 
     * @param context
     *            The context whose JmDNS instance to use. This is also the
     *            context that will own any sessions created on the server.
     */
    public ServerListMenu(WabitSwingSessionContext context, String name,
            Component dialogOwner, ServerListMenuItemFactory itemFactory) {
        super(name);
        this.context = context;
        this.dialogOwner = dialogOwner;
        this.itemFactory = itemFactory;
        refillMenu.run();
        context.getJmDNS().addServiceListener(
                WabitSessionContext.WABIT_ENTERPRISE_SERVER_MDNS_TYPE, serviceListener);
    }
    
    private final Runnable refillMenu = new Runnable() {
        public void run() {
            List<ServiceInfo> servers = context.getEnterpriseServers();
            logger.debug("Refilling server menu. servers = " + servers);
            removeAll();
            if (servers.isEmpty()) {
                JMenuItem mi = new JMenuItem("Searching for servers...");
                mi.setEnabled(false);
                add(mi);
            } else {
                for (ServiceInfo si : servers) {
                    add(itemFactory.createMenuEntry(si, dialogOwner));
                }
            }
        }
    };
    
    private final ServiceListener serviceListener = new ServiceListener() {

        public void serviceAdded(ServiceEvent event) {
            rebuildMenu();
        }

        public void serviceRemoved(ServiceEvent event) {
            rebuildMenu();
        }

        public void serviceResolved(ServiceEvent event) {
            rebuildMenu();
        }

        private void rebuildMenu() {
            SwingUtilities.invokeLater(refillMenu);
        }
    };

}
