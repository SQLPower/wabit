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

public class NewProjectScreen {
	
	private static Logger logger = Logger.getLogger(NewProjectScreen.class);
	
	private final WabitSwingSession session;
	private final JFrame frame;
	private boolean databaseAdded = false;
	
	public NewProjectScreen(final WabitSwingSessionContext context) {
		session = new WabitSwingSessionImpl(context);
		
		session.getProject().addDatabaseListChangeListener(new DatabaseListChangeListener() {
			public void databaseRemoved(DatabaseListChangeEvent e) {
				//Cannot remove databases from this panel.
			}
		
			public void databaseAdded(DatabaseListChangeEvent e) {
				databaseAdded = true;
				frame.dispose();
				try {
					session.buildUI();
				} catch (SQLObjectException e1) {
					throw new SQLObjectRuntimeException(e1);
				}
			}
		});
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!databaseAdded) {
					context.deregisterChildSession(session);
					frame.dispose();
				}
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
		builder.append(ProjectPanel.createDBConnectionManager(session, frame).getPanel());
		
		frame.setIconImage(WabitSwingSessionImpl.FRAME_ICON.getImage());
		frame.add(builder.getPanel());
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public void showFrame() {
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		frame.setLocation((int) (toolkit.getScreenSize().getWidth() / 2 - frame.getWidth() / 2), (int) (toolkit.getScreenSize().getHeight() / 2 - frame.getHeight() / 2));
		frame.setVisible(true);
	}

}
