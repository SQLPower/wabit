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

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class is used to display a screen for adding the first data source to a
 * new workspace. If this screen is closed without choosing a data source the
 * welcome screen will be displayed.
 */
public class NewWorkspaceScreen {
	
	private static Logger logger = Logger.getLogger(NewWorkspaceScreen.class);
	
	/**
	 * The new session that needs a starting data source added to it.
	 */
	private final WabitSwingSession session;
	
	/**
	 * The frame displaying data source choices to the user.
	 */
	private final JDialog dialog;
	
	/**
	 * A flag tracking if a data source was added to the workspace. Used
	 * to decide if the session should be disposed when this window closes.
	 */
	private boolean databaseAdded = false;
	
	/**
	 * This listener will dispose of the frame and show the session's UI when
	 * a data source was added to the workspace.
	 */
	private final DatabaseListChangeListener workspaceDataSourceListener = new DatabaseListChangeListener() {
	    public void databaseRemoved(DatabaseListChangeEvent e) {
	        //Cannot remove databases from this panel.
	    }
	    
	    public void databaseAdded(DatabaseListChangeEvent e) {
	        logger.debug("Added data source " + e.getDataSource().getName());
	        databaseAdded = true;
	        dialog.dispose();
	    }
	};

    private final WabitSwingSessionContext context;
	
	public NewWorkspaceScreen(final WabitSwingSessionContext context) {
		this.context = context;
        session = context.createSession();
		
        session.getWorkspace().addDatabaseListChangeListener(workspaceDataSourceListener);
		
		dialog = new JDialog(context.getFrame());
		
		buildUI();
	}
	
	public NewWorkspaceScreen(WabitSwingSessionContext context, SPServerInfo serverInfo) {
	    this.context = context;
        session = context.createServerSession(serverInfo);
        
        session.getWorkspace().addDatabaseListChangeListener(workspaceDataSourceListener);
        
        dialog = new JDialog(context.getFrame());
        
        buildUI();
	}
	
	private void buildUI() {
	    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    dialog.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosed(WindowEvent e) {
	            if (databaseAdded) {
	                SPObject currentEditor = session.getWorkspace().getEditorPanelModel();
	                try {
	                    final URI resource = WabitSwingSessionContextImpl.class.getResource(
	                            WabitSessionContext.NEW_WORKSPACE_URL).toURI();
	                    URL importURL = resource.toURL();
	                    URLConnection urlConnection = importURL.openConnection();
	                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
	                    final OpenWorkspaceXMLDAO workspaceLoader = 
	                        new OpenWorkspaceXMLDAO(context, in, urlConnection.getContentLength());
	                    workspaceLoader.importWorkspaces(session);
	                } catch (Exception ex) {
	                    throw new RuntimeException("Cannot find the templates file at " +
	                    		"location " + WabitSessionContext.NEW_WORKSPACE_URL);
	                }
	                session.getWorkspace().setEditorPanelModel(currentEditor);
	                
	                context.registerChildSession(session);
	            }
	            session.getWorkspace().removeDatabaseListChangeListener(workspaceDataSourceListener);
	        }
	    });
	    
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow"));
		final JLabel selectDSLabel = new JLabel("Select a data source for your new workspace.");
		selectDSLabel.setHorizontalAlignment(SwingConstants.CENTER);
		builder.append(selectDSLabel);
		builder.nextLine();
		final JLabel additionalDSLabel = new JLabel("(Additional data sources can be added later.)");
		additionalDSLabel.setHorizontalAlignment(SwingConstants.CENTER);
		builder.append(additionalDSLabel);
		builder.nextLine();
		builder.append(WorkspacePanel.createDBConnectionManager(session, dialog).getPanel());
		
		dialog.add(builder.getPanel());
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	public void showFrame() {
		dialog.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		dialog.setLocation((int) (toolkit.getScreenSize().getWidth() / 2 - dialog.getWidth() / 2), 
		        (int) (toolkit.getScreenSize().getHeight() / 2 - dialog.getHeight() / 2));
		dialog.setVisible(true);
	}

}
