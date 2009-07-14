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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DatabaseListChangeEvent;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;

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
	private final JFrame frame;
	
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
	        try {
	            session.buildUI();
	        } catch (SQLObjectException e1) {
	            throw new SQLObjectRuntimeException(e1);
	        }
	        databaseAdded = true;
	        frame.dispose();
	    }
	};
	
	public NewWorkspaceScreen(final WabitSwingSessionContext context) {
		session = new WabitSwingSessionImpl(context);
		
        session.getWorkspace().addDatabaseListChangeListener(workspaceDataSourceListener);
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (!databaseAdded) {
					context.deregisterChildSession(session);
					frame.dispose();
				}
				session.getWorkspace().removeDatabaseListChangeListener(workspaceDataSourceListener);
			}
		});
		
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow"));
		final JLabel selectDSLabel = new JLabel("Select a data source for your new workspace.");
		selectDSLabel.setHorizontalAlignment(SwingConstants.CENTER);
		builder.append(selectDSLabel);
		builder.nextLine();
		final JLabel additionalDSLabel = new JLabel("(Additional data sources can be added later.)");
		additionalDSLabel.setHorizontalAlignment(SwingConstants.CENTER);
		builder.append(additionalDSLabel);
		builder.nextLine();
		builder.append(WorkspacePanel.createDBConnectionManager(session, frame).getPanel());
		
		frame.setIconImage(WabitSwingSessionImpl.FRAME_ICON.getImage());
		frame.add(builder.getPanel());
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public void showFrame() {
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		frame.setLocation((int) (toolkit.getScreenSize().getWidth() / 2 - frame.getWidth() / 2), 
		        (int) (toolkit.getScreenSize().getHeight() / 2 - frame.getHeight() / 2));
		frame.setVisible(true);
	}

}
