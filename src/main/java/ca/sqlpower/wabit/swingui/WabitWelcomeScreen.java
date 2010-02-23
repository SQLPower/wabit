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
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.swingui.WorkspacePanel.FurryPanel;
import ca.sqlpower.wabit.swingui.WorkspacePanel.LogoLayout;
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
	 * The icon for the "Connect to a Wabit Server" button.
	 */
	private static final Icon OPEN_SERVER_ICON = new ImageIcon(
			WabitWelcomeScreen.class.getClassLoader().getResource("icons/workspace-server-logo.png"));
	
	/**
	 * The icon for the "Open Demonstration Workspace" button.
	 */
	private static final Icon OPEN_DEMO_ICON = new ImageIcon(WabitWelcomeScreen.class.getClassLoader().getResource("icons/welcome-demo.png"));
	
	private final WabitSwingSessionContext context;
	
	/**
	 * This panel contains all of the welcome screen buttons and images.
	 */
    private JPanel mainPanel;
    private JScrollPane mainScrollPane;
	
	public WabitWelcomeScreen(WabitSwingSessionContext context) {
		this.context = context;
		buildUI();
	}
	
	private JPanel buildButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout("ins n 0 n 0",
				"[right] para [] para [] para [left]"));
		
		//new workspace
		JButton newWorkspaceButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				new NewWorkspaceScreen(context).showFrame();
			}
		});
		JPanel newWorkspacePanel = new JPanel(new MigLayout("ins 0", "[center]"));
		newWorkspaceButton.setIcon(NEW_WORKSPACE_ICON);
		newWorkspacePanel.setOpaque(false);
		newWorkspacePanel.add(newWorkspaceButton, "wrap");
		newWorkspacePanel.add(new JLabel("Create a New"), "wrap, gapbottom 1");
		newWorkspacePanel.add(new JLabel("Workspace"), "wrap");
		buttonsPanel.add(newWorkspacePanel, "");
		
		//existing workspace
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
		
		
		JPanel existingWorkspacePanel = new JPanel(new MigLayout("ins 0", "[center]"));
		existingWorkspacePanel.setOpaque(false);
		openExistingButton.setIcon(OPEN_EXISTING_ICON);
		existingWorkspacePanel.add(openExistingButton, "wrap");
		existingWorkspacePanel.add(new JLabel("Open an Existing"), "wrap, gapbottom 1");
		existingWorkspacePanel.add(new JLabel("Workspace"), "wrap");
		buttonsPanel.add(existingWorkspacePanel, "");
		
		final JButton serverConnectionButton = new JButton();
		serverConnectionButton.setAction(new AbstractAction() {
		
			public void actionPerformed(ActionEvent evt) {
		        JPopupMenu serverPopup = ServerListMenu.createPopupInstance(context, mainPanel);
		        serverPopup.show(serverConnectionButton, 0, serverConnectionButton.getHeight());
			}
		});
		
		JPanel serverConnectionPanel = new JPanel(new MigLayout("ins 0", "[center]"));
		serverConnectionPanel.setOpaque(false);
		serverConnectionButton.setIcon(OPEN_SERVER_ICON);
		serverConnectionPanel.add(serverConnectionButton, "wrap");
		serverConnectionPanel.add(new JLabel("Connect to a"), "wrap, gapbottom 1");
		serverConnectionPanel.add(new JLabel("Wabit Server"), "wrap");
		buttonsPanel.add(serverConnectionPanel, "");
		
		//open demo
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
		
		
		JPanel demoWorkspacePanel = new JPanel(new MigLayout("ins 0", "[center]"));
		demoWorkspacePanel.setOpaque(false);
		openDemoButton.setIcon(OPEN_DEMO_ICON);
		demoWorkspacePanel.add(openDemoButton, "wrap");
		demoWorkspacePanel.add(new JLabel("Try the Demo"), "wrap, gapbottom 1");
		demoWorkspacePanel.add(new JLabel("Workspace"), "wrap");
		buttonsPanel.add(demoWorkspacePanel, "");
		
		return buttonsPanel;
	}
	
	private void buildUI() {
		//This panel is only here to center the icons panel in the middle of the dialog
		
		JPanel iconsPanel = new JPanel(new MigLayout("ins n 0 n 0", "[center, grow]"));
		iconsPanel.setOpaque(false);
		
		JPanel generateLogoPanel = LogoLayout.generateLogoPanel();
		generateLogoPanel.setOpaque(false);
		iconsPanel.add(generateLogoPanel, "wrap, alignx center");

		iconsPanel.add(buildButtonsPanel(), "");
		
		new JButton(new AbstractAction("View Tutorials") {
			public void actionPerformed(ActionEvent e) {
				// TODO Link to a website with demos of Wabit.
			}
		});
//		bottomPanelBuilder.append(tutorialButton);
		//Temporary spacer label until tutorials exist on the website.
		
		
		DefaultFormBuilder bottomPanelBuilder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref, 4dlu:grow, pref"));
		bottomPanelBuilder.setDefaultDialogBorder();
		JButton helpButton = new JButton(new HelpAction(context.getFrame()));
		bottomPanelBuilder.append(helpButton);
		
		mainPanel = new FurryPanel(new BorderLayout());
		mainPanel.add(iconsPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanelBuilder.getPanel(), BorderLayout.SOUTH);
		
		mainScrollPane = new JScrollPane(mainPanel);
	}
	
	public WabitPanel getPanel() {
	    return new WabitPanel(){
        
            public boolean hasUnsavedChanges() {
                return false;
            }
        
            public JComponent getPanel() {
                return mainScrollPane;
            }
        
            public void discardChanges() {
                //do nothing
            }
        
            public boolean applyChanges() {
                return true;
            }

            public JComponent getSourceComponent() {
                return null;
            }

			public String getTitle() {
				return "Wabit " + WabitVersion.VERSION;
			}

            public JToolBar getToolbar() {
                return null;
            }
        };
	}
	
}
