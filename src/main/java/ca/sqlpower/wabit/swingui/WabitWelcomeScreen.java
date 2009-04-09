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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.swingui.action.HelpAction;
import ca.sqlpower.wabit.swingui.action.LoadProjectsAction;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the first screen new users of Wabit should see. This screen will also
 * be displayed if the user has closed all projects.
 */
public class WabitWelcomeScreen {
	
	/**
	 * The icon for the "New Project" button.
	 */
	private static final ImageIcon NEW_PROJECT_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/page_white.png"));
	
	/**
	 * The icon for the "Open Existing Project" button.
	 */
	private static final Icon OPEN_EXISTING_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/wabit_load.png"));
	
	/**
	 * The icon for the "Open Demonstration Project" button.
	 */
	private static final Icon OPEN_DEMO_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/wabit-16.png"));
	
	/**
	 * The main Wabit icon.
	 */
	private static final ImageIcon WABIT_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/wabit_header_app_wabit.png"));
	
	/**
	 * The main panel for the welcome screen.
	 */
	private final JFrame frame;

	private final WabitSwingSessionContext context;
	
	/**
	 * The welcome screen will terminate the application by default when 
	 * it is closed unless the action closing the welcome screen sets
	 * it to not terminate.
	 */
	private boolean terminate = true;
	
	public WabitWelcomeScreen(WabitSwingSessionContext context) {
		this.context = context;
		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		
		buildUI();
	}
	
	private void buildUI() {
		
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, 4dlu, pref, 4dlu, pref"));
		builder.setDefaultDialogBorder();
		builder.append(new JLabel(WABIT_ICON), 5);
		builder.nextLine();
		
		JButton newProjectButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				terminate = false;
				close();
				new NewProjectScreen(context).showFrame();
			}
		});
		newProjectButton.setIcon(NEW_PROJECT_ICON);
		builder.append(new JLabel());
		builder.append(newProjectButton);
		builder.append(Messages.getString("WabitWelcomeScreen.newProject"));
		builder.nextLine();
		
		JButton openExistingButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(context.createRecentMenu().getMostRecentFile());
				fc.setDialogTitle("Select the file to load from.");
				fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
				frame.dispose();
				
				File importFile = null;
				int fcChoice = fc.showOpenDialog(null);

				if (fcChoice != JFileChooser.APPROVE_OPTION) {
					showFrame();
				    return;
				}
				importFile = fc.getSelectedFile();

				LoadProjectsAction.loadFile(importFile, context);
				terminate = false;
			}
		});
		openExistingButton.setIcon(OPEN_EXISTING_ICON);
		builder.append(new JLabel());
		builder.append(openExistingButton);
		builder.append(Messages.getString("WabitWelcomeScreen.openExisting"));
		builder.nextLine();
		
		final JButton openOnServerButton = new JButton();
		openOnServerButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu popup = ServerListMenu.createPopupInstance(context);
                popup.show(openOnServerButton, 0, 0);
            }
		});
		openOnServerButton.setIcon(OPEN_EXISTING_ICON);
        builder.append(new JLabel());
        builder.append(openOnServerButton);
        builder.append("Open On Server");
        builder.nextLine();
		
		JButton openDemoButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				LoadProjectsAction.loadFile(WabitWelcomeScreen.class.getResourceAsStream(
				        "/ca/sqlpower/wabit/exProject.wabit"), context);
			}
		});
		
		openDemoButton.setIcon(OPEN_DEMO_ICON);
		builder.append(new JLabel());
		builder.append(openDemoButton);
		builder.append(Messages.getString("WabitWelcomeScreen.openDemo"));
		
		DefaultFormBuilder bottomPanelBuilder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref, 4dlu:grow, pref"));
		bottomPanelBuilder.setDefaultDialogBorder();
		JButton helpButton = new JButton(new HelpAction(frame));
		bottomPanelBuilder.append(helpButton);
		
		JButton tutorialButton = new JButton(new AbstractAction("View Tutorials") {
			public void actionPerformed(ActionEvent e) {
				// TODO Link to a website with demos of Wabit.
			}
		});
//		bottomPanelBuilder.append(tutorialButton);
		//Temporary spacer label until tutorials exist on the website.
		bottomPanelBuilder.append(new JLabel());
		
		JButton quitButton = new JButton(new AbstractAction("Quit") {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		bottomPanelBuilder.append(quitButton);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(builder.getPanel(), BorderLayout.CENTER);
		mainPanel.add(bottomPanelBuilder.getPanel(), BorderLayout.SOUTH);
		
		frame.setIconImage(WabitSwingSessionImpl.FRAME_ICON.getImage());
		frame.setTitle("Wabit");
		frame.add(mainPanel);
	}
	
	/**
	 * This method gets called when the main frame of the welcome window is closed.
	 */
	private void close() {
		if (terminate) {
			System.exit(0);
		}
		frame.dispose();
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public void showFrame() {
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		frame.setLocation((int) (toolkit.getScreenSize().getWidth() / 2 - frame.getWidth() / 2), (int) (toolkit.getScreenSize().getHeight() / 2 - frame.getHeight() / 2));
		frame.setVisible(true);
		terminate = true;
	}
	
}
