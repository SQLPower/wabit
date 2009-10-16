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

package ca.sqlpower.wabit.swingui.enterprise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.GroupMember;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitToolBarBuilder;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;

public class GroupPanel implements WabitPanel {

	private final Group group;
	
	private final JPanel panel = new JPanel(new MigLayout());
	private final JTextField nameTextField;
	
	private final JList currentUsersList;
	private final JScrollPane currentUsersScrollPane;
	private final UsersListModel currentUsersListModel;
	
	private final JList availableUsersList;
	private final JScrollPane availableUsersScrollPane;
	private final UsersListModel availableUsersListModel;
	
	private final JButton addButton;
	private final JButton removeButton;
	
	
	private final JLabel nameLabel;
	private final JLabel usersLabel;
	private final JLabel currentUsersLabel;
	private final JLabel availableUsersLabel;
	
	private final WabitToolBarBuilder toolbarBuilder = new WabitToolBarBuilder();

	private final WabitWorkspace workspace;
	
	
	public GroupPanel(Group baseGroup) {
		this.group = baseGroup;
		this.workspace = (WabitWorkspace)this.group.getParent();
		
		this.nameTextField = new JTextField();
		this.nameTextField.setText(group.getName());
		this.nameLabel = new JLabel("Group name");
		this.nameTextField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				group.setName(nameTextField.getText());
			}
			public void keyReleased(KeyEvent e) {
				// no-op
			}
			public void keyPressed(KeyEvent e) {
				// no-op
			}
		});
		
		
		this.availableUsersLabel = new JLabel("Available Users");
		this.availableUsersListModel = new UsersListModel(group, workspace, false);
		this.availableUsersList = new JList(this.availableUsersListModel);
		this.availableUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.availableUsersList.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
		this.availableUsersScrollPane = new JScrollPane(this.availableUsersList);
		
		
	
		this.currentUsersLabel = new JLabel("Current Groups");
		this.currentUsersListModel = new UsersListModel(group, workspace, true);
		this.currentUsersList = new JList(this.currentUsersListModel);
		this.currentUsersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.currentUsersList.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
		this.usersLabel = new JLabel("Edit user memberships");
		this.currentUsersScrollPane = new JScrollPane(this.currentUsersList);
	
		
		
		
		this.addButton = new JButton(">>");
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selection = availableUsersList.getSelectedValues();
				if (selection.length==0) {
					return;
				}
				for (Object object : selection) {
					group.addMember(new GroupMember((User)object));
				}
			}
		});
		
		
		
		this.removeButton = new JButton("<<");
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selection = currentUsersList.getSelectedValues();
				if (selection.length==0) {
					return;
				}
				List<GroupMember> toRemove = new ArrayList<GroupMember>();
				for (Object object : selection) {
					for (GroupMember membership : group.getMembers()) {
						if (membership.getUser().getUUID().equals(((User)object).getUUID())) {
							toRemove.add(membership);
						}
					}
				}
				for (GroupMember membership : toRemove) {
					group.removeMember(membership);
				}
			}
		});
		
		
		
		// Panel building time
		this.panel.add(this.nameLabel);
		this.panel.add(this.nameTextField, "span, wrap");
		
		
		this.panel.add(this.usersLabel, "span, wrap, gaptop 20");
		this.panel.add(this.availableUsersLabel);
		this.panel.add(this.currentUsersLabel, "wrap");
		
		this.panel.add(this.availableUsersScrollPane);
		this.panel.add(this.addButton);
		this.panel.add(this.removeButton);
		this.panel.add(this.currentUsersScrollPane);
		
	}
	
	
	
	public JComponent getSourceComponent() {
		return null;
	}

	public String getTitle() {
		return "Group editor - "+group.getName();
	}

	public JToolBar getToolbar() {
		return this.toolbarBuilder.getToolbar();
	}

	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
		// no op
	}

	public JComponent getPanel() {
		return this.panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}
	
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                	WabitWorkspace p = new WabitWorkspace();
                	p.setUUID("system");
                	
                	
                    // Add data sources to workspace
                    DataSourceCollection<SPDataSource> plini = new PlDotIni();
                    plini.read(new File(System.getProperty("user.home"), "pl.ini"));
                    List<SPDataSource> dataSources = plini.getConnections();
                    for (int i = 0; i < 10 && i < dataSources.size(); i++) {
                        p.addDataSource(new WabitDataSource(dataSources.get(i)));
                    }
                    
                    // Add layouts to workspace
                    Report layout = new Report("Example Layout");
                    p.addReport(layout);
                    Page page = layout.getPage();
                    page.addContentBox(new ContentBox());
                    page.addGuide(new Guide(Axis.HORIZONTAL, 123));
                    page.addContentBox(new ContentBox());
                    
                    // dd a report task
                    ReportTask task = new ReportTask();
                    task.setReport(layout);
                    p.addReportTask(task);
                    
                	
                    User user = new User("admin", "admin");
                    user.setParent(p);
                    Group group = new Group("Admins");
                    group.setParent(p);
                    group.addMember(new GroupMember(user));
                    
                    Group group2 = new Group("Other");
                    group2.setParent(p);
                    
                    p.addUser(user);
                	p.addGroup(group);
                	p.addGroup(group2);
                	
                	
                	
                	GroupPanel panel = new GroupPanel(group);
                	
                	GroupPanel panel2 = new GroupPanel(group);
                	
                	
                    JFrame f = new JFrame("TEST PANEL");
                    JPanel outerPanel = new JPanel(new BorderLayout());
                    outerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel.add(panel.getPanel(), BorderLayout.CENTER);
                    f.setContentPane(outerPanel);
                    f.pack();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                    
                    
                    JFrame f2 = new JFrame("TEST PANEL");
                    JPanel outerPanel2 = new JPanel(new BorderLayout());
                    outerPanel2.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel2.add(panel2.getPanel(), BorderLayout.CENTER);
                    f2.setContentPane(outerPanel2);
                    f2.pack();
                    f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f2.setVisible(true);
                    
                    
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

}
