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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.ButtonBarFactory;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.ServerListEvent;
import ca.sqlpower.wabit.ServerListListener;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.swingui.enterprise.ServerInfoManager;

/**
 * A JMenu which maintains its own set of entries based on services discovered by mDNS information.
 */
public class ServerListMenu extends JMenu {

    private static final Logger logger = Logger.getLogger(ServerListMenu.class);

    private final WabitSwingSessionContext context;

    private final Component dialogOwner;

    private final ServerListMenuItemFactory itemFactory;

    /**
     * The server manager action that is used in the dynamically changing popup
     * menus. We avoid making many of these in an attempt to avoid a
     * proliferation of ServerInfoManager instances.
     */
    private final AbstractAction serverManagerAction;

    /**
     * Creates a throwaway popup menu containing the current list of servers.
     * The resulting popup menu will not change over time like the regular
     * ServerListMenu does, so you should make a new popup instance every time
     * you need one.
     */
    public static JPopupMenu createPopupInstance(final WabitSwingSessionContext context, final Component dialogOwner) {
        JPopupMenu popup = new JPopupMenu();
        List<WabitServerInfo> servers = context.getEnterpriseServers(true);
        AbstractAction configureServersAction = makeServerManagerAction(context, dialogOwner);
        popup.add(configureServersAction);
        if (servers.isEmpty()) {
            JMenuItem mi = new JMenuItem("Searching for servers...");
            mi.setEnabled(false);
            popup.add(mi);
        } else {
            for (WabitServerInfo si : servers) {
                popup.add(new LogInToServerAction(dialogOwner, si, context));
            }
        }
        return popup;
    }

    private static AbstractAction makeServerManagerAction(
            final WabitSwingSessionContext context, final Component dialogOwner) {

        return new AbstractAction("Configure Server Connections...") {
            public void actionPerformed(ActionEvent e) {
            	
            	final JDialog d = SPSUtils.makeOwnedDialog(dialogOwner, "Server Connections");
            	Runnable closeAction = new Runnable() {
					public void run() {
                        d.dispose();
					}
				};
                ServerInfoManager sim = new ServerInfoManager(context, dialogOwner, closeAction);
                d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                d.setContentPane(sim.getPanel());
                
                SPSUtils.makeJDialogCancellable(d, null);
                d.pack();
                d.setLocationRelativeTo(dialogOwner);
                d.setVisible(true);
            }
        };
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
        super.setIcon(SPSUtils.createIcon("wabitServer-16", ""));
        this.context = context;
        this.dialogOwner = dialogOwner;
        this.itemFactory = itemFactory;
        this.serverManagerAction = makeServerManagerAction(context, dialogOwner);
        refillMenu.run();
        if (context.getJmDNS() != null) {
        	context.getJmDNS().addServiceListener(
                WabitSessionContext.WABIT_ENTERPRISE_SERVER_MDNS_TYPE, serviceListener);
        }
        //XXX This listener should be removed from the context when the session is closed
        //XXX and the list goes away. We are changing this list to be in the context in
        //XXX the UI remake which will make this change unnecessary then.
        context.addServerListListener(new ServerListListener() {
		
			public void serverRemoved(ServerListEvent e) {
				SwingUtilities.invokeLater(refillMenu);
			}
		
			public void serverAdded(ServerListEvent e) {
				SwingUtilities.invokeLater(refillMenu);
			}
		});
    }
    
    private final Runnable refillMenu = new Runnable() {
        public void run() {
            List<WabitServerInfo> servers = context.getEnterpriseServers(true);
            logger.debug("Refilling server menu. servers = " + servers);
            removeAll();
            add(serverManagerAction);
            if (servers.isEmpty()) {
                JMenuItem mi = new JMenuItem("Searching for servers...");
                mi.setEnabled(false);
                add(mi);
            } else {
                for (WabitServerInfo si : servers) {
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
