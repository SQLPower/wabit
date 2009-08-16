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
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.swingui.action.HelpAction;
import ca.sqlpower.wabit.swingui.action.OpenWorkspaceAction;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the first screen new users of Wabit should see. This screen will also
 * be displayed if the user has closed all workspaces.
 */
public class WabitWelcomeScreen {
	
	/**
	 * The icon for the "New Workspace" button.
	 */
	private static final ImageIcon NEW_WORKSPACE_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/welcome-new.png"));
	
	/**
	 * The icon for the "Open Existing Workspace" button.
	 */
	private static final Icon OPEN_EXISTING_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/welcome-open.png"));
	
	/**
	 * The icon for the "Open Demonstration Workspace" button.
	 */
	private static final Icon OPEN_DEMO_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/welcome-demo.png"));
	
	/**
	 * The main Wabit icon.
	 */
	private static final ImageIcon WABIT_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/wabit_header_app_wabit.png"));
	
	private final WabitSwingSessionContext context;
	
	/**
	 * This panel contains all of the welcome screen buttons and images.
	 */
    private JPanel mainPanel;
	
	public WabitWelcomeScreen(WabitSwingSessionContext context) {
		this.context = context;
		buildUI();
	}
	
	private void buildUI() {
		JPanel iconsPanel = new JPanel(new MigLayout("", "[center, grow][center, grow][center, grow]"));
		iconsPanel.add(new JLabel(WABIT_ICON), "span 3, wrap, center");
		
		JButton newWorkspaceButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				new NewWorkspaceScreen(context).showFrame();
			}
		});
		JPanel workspacePanel = new JPanel(new MigLayout("", "[center]"));
		workspacePanel.add(new JLabel(NEW_WORKSPACE_ICON), "wrap");
		workspacePanel.add(new JLabel(Messages.getString("WabitWelcomeScreen.newWorkspace")), "");
		newWorkspaceButton.add(workspacePanel);
//		newWorkspaceButton.setIcon(NEW_WORKSPACE_ICON);
		
		iconsPanel.add(newWorkspaceButton, "");
		
		JButton openExistingButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(context.createRecentMenu().getMostRecentFile());
				fc.setDialogTitle("Select the file to load from.");
				fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
				
				File importFile = null;
				int fcChoice = fc.showOpenDialog(null);

				if (fcChoice != JFileChooser.APPROVE_OPTION) {
				    return;
				}
				importFile = fc.getSelectedFile();
				OpenWorkspaceAction.loadFiles(context, importFile.toURI());
			}
		});
		openExistingButton.setIcon(OPEN_EXISTING_ICON);
		iconsPanel.add(openExistingButton, "center");
		
		//No need for this after the gui makeover? everything from servers will just
		//populate in your tree
//		final JButton openOnServerButton = new JButton();
//		openOnServerButton.setAction(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                JPopupMenu popup = ServerListMenu.createPopupInstance(context, frame);
//                popup.show(openOnServerButton, 0, 0);
//            }
//		});
//		openOnServerButton.setIcon(OPEN_EXISTING_ICON);
//		iconsPanel.add(openOnServerButton, "center");
//        builder.append("Open On Server");
//        builder.nextLine();
		
		AbstractAction openDemoAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
                try {
                    final URI resource = WabitWelcomeScreen.class.getResource(WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL).toURI();
                    OpenWorkspaceAction.loadFiles(context, resource);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
			}
		};
		JButton openDemoButton = new JButton(openDemoAction);
		
		openDemoButton.setIcon(OPEN_DEMO_ICON);
		iconsPanel.add(openDemoButton, "center, wrap");
		
		JLabel newWorkspaceLabel = new JLabel("");
		iconsPanel.add(newWorkspaceLabel);
		
		JLabel existingWorkspaceLabel = new JLabel(Messages.getString("WabitWelcomeScreen.openExisting"));
		iconsPanel.add(existingWorkspaceLabel);
		
		JLabel openDemoLabel = new JLabel(Messages.getString("WabitWelcomeScreen.openDemo"));
		iconsPanel.add(openDemoLabel);
		
		
		DefaultFormBuilder bottomPanelBuilder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref, 4dlu:grow, pref"));
		bottomPanelBuilder.setDefaultDialogBorder();
		JButton helpButton = new JButton(new HelpAction(context.getFrame()));
		bottomPanelBuilder.append(helpButton);
		
		JButton tutorialButton = new JButton(new AbstractAction("View Tutorials") {
			public void actionPerformed(ActionEvent e) {
				// TODO Link to a website with demos of Wabit.
			}
		});
//		bottomPanelBuilder.append(tutorialButton);
		//Temporary spacer label until tutorials exist on the website.
		bottomPanelBuilder.append(new JLabel());
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(iconsPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanelBuilder.getPanel(), BorderLayout.SOUTH);
		
	}
	
	public WabitPanel getPanel() {
	    return new WabitPanel(){
        
            public boolean hasUnsavedChanges() {
                return false;
            }
        
            public JComponent getPanel() {
                return mainPanel;
            }
        
            public void discardChanges() {
                //do nothing
            }
        
            public boolean applyChanges() {
                return true;
            }
        
            public void maximizeEditor() {
                //do nothing
            }

			public String getTitle() {
				return "Wabit " + WabitVersion.VERSION;
			}
        };
	}
	
}
