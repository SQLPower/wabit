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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.jmdns.ServiceInfo;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import ca.sqlpower.wabit.swingui.action.StartServerSessionAction;

public class ServerMenu extends JMenu {

    private static final Logger logger = Logger.getLogger(ServerMenu.class);
    private final ServiceInfo serviceInfo;
    private final WabitSwingSessionContext sessionContext;
    
    public ServerMenu(WabitSwingSessionContext sessionContext, ServiceInfo si) {
        super(si.getName() + " (" + si.getInetAddress().getHostName() + ":" + si.getPort() + ")");
        this.sessionContext = sessionContext;
        this.serviceInfo = si;
        refreshProjects();
    }
    
    private Action refreshAction = new AbstractAction("Refresh this list") {
        public void actionPerformed(ActionEvent e) {
            refreshProjects();
        }
    };
    
    private void refreshProjects() {
        removeAll();
        try {
            for (String projectName : getProjectNames(serviceInfo)) {
                add(new StartServerSessionAction(sessionContext, serviceInfo, projectName));
            }
        } catch (Exception ex) {
            JMenuItem mi = new JMenuItem("Error getting project names: " + ex);
            mi.setEnabled(false);
            add(mi);
        }
        addSeparator();
        add(refreshAction);
    }

    private static List<String> getProjectNames(ServiceInfo serviceInfo) throws IOException, URISyntaxException {
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String contextPath = serviceInfo.getPropertyString("path");
            URI uri = new URI("http", null, serviceInfo.getHostAddress(), serviceInfo.getPort(), contextPath + "project", null, null);
            HttpGet httpget = new HttpGet(uri);
            logger.debug("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            logger.debug(responseBody);
            List<String> projects = Arrays.asList(responseBody.split("\n"));
            return projects;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

}
