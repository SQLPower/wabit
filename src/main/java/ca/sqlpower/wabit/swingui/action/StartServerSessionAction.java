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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.jmdns.ServiceInfo;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

public class StartServerSessionAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(StartServerSessionAction.class);
    private final WabitSwingSessionContext sessionContext;
    private final ServiceInfo serviceInfo;
    private final String projectName;

    public StartServerSessionAction(WabitSwingSessionContext sessionContext, ServiceInfo si, String projectName) {
        super(projectName);
        this.sessionContext = sessionContext;
        this.serviceInfo = si;
        this.projectName = projectName;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            JOptionPane.showMessageDialog(null, "opening project " + projectName);
//        LoadProjectXMLDAO projectLoader = new LoadProjectXMLDAO(sessionContext, in);
        } catch (Exception ex) {
            SPSUtils.showExceptionDialogNoReport(
                    "Failed to retrieve project list from server " + serviceInfo, ex);
        }
    }
    
}
