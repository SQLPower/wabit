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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.olap4j.impl.ArrayMap;

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
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitToolBarBuilder;
import ca.sqlpower.wabit.swingui.action.DeleteFromTreeAction;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;

public class UserPanel implements WabitPanel {

	private final User user;
	
	private final JPanel panel = new JPanel(new MigLayout());
	private final JTextField loginTextField;
	private final JPasswordField passwordTextField;
	private final JTextField fullNameTextField;
	private final JTextField emailTextField;
	
	private final JList currentGroupsList;
	private final JScrollPane currentGroupsScrollPane;
	private final GroupsListModel currentGroupsListModel;
	
	private final JList availableGroupsList;
	private final JScrollPane availableGroupsScrollPane;
	private final GroupsListModel availableGroupsListModel;
	
	private final JButton addButton;
	private final JButton removeButton;
	
	
	private final JLabel loginLabel;
	private final JLabel passwordLabel;
	private final JLabel fullNameLabel;
	private final JLabel emailLabel;
	private final JLabel groupsLabel;
	private final JLabel currentGroupsLabel;
	private final JLabel availableGroupsLabel;
	
	private final WabitToolBarBuilder toolbarBuilder = new WabitToolBarBuilder();

	private final WabitWorkspace workspace;
	
	
	public UserPanel(User baseUser) {
		this.user = baseUser;
		this.workspace = (WabitWorkspace)this.user.getParent();
		
		this.loginTextField = new JTextField();
		this.loginTextField.setText(user.getName());
		this.loginLabel = new JLabel("User name");
		this.loginTextField.getDocument().addDocumentListener(new DocumentListener() {

			public void textChanged(DocumentEvent e) {
				user.setName(loginTextField.getText());
			}
			
			public void changedUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void insertUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void removeUpdate(DocumentEvent e) {
				textChanged(e);
			}
		});
		
		
		this.passwordTextField = new JPasswordField();
		this.passwordTextField.setText(user.getPassword());
		this.passwordLabel = new JLabel("Password");
		this.passwordTextField.getDocument().addDocumentListener(new DocumentListener() {

			public void textChanged(DocumentEvent e) {
				user.setPassword(loginTextField.getText());
			}
			
			public void changedUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void insertUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void removeUpdate(DocumentEvent e) {
				textChanged(e);
			}
		});
		
		
		
		this.fullNameTextField = new JTextField();
		this.fullNameTextField.setText(user.getFullName());
		this.fullNameLabel = new JLabel("Full name");
		this.fullNameTextField.getDocument().addDocumentListener(new DocumentListener() {

			public void textChanged(DocumentEvent e) {
				user.setName(fullNameTextField.getText());
			}
			
			public void changedUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void insertUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void removeUpdate(DocumentEvent e) {
				textChanged(e);
			}
		});
		
		
		
		this.emailTextField = new JTextField();
		this.emailTextField.setText(user.getEmail());
		this.emailLabel = new JLabel("Email");
		this.emailTextField.getDocument().addDocumentListener(new DocumentListener() {

			public void textChanged(DocumentEvent e) {
				user.setName(emailTextField.getText());
			}
			
			public void changedUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void insertUpdate(DocumentEvent e) {
				textChanged(e);
			}
			public void removeUpdate(DocumentEvent e) {
				textChanged(e);
			}
		});
		
		
		this.availableGroupsLabel = new JLabel("Available Groups");
		this.availableGroupsListModel = new GroupsListModel(user, workspace, false);
		this.availableGroupsList = new JList(this.availableGroupsListModel);
		this.availableGroupsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.availableGroupsList.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
		this.availableGroupsScrollPane = new JScrollPane(this.availableGroupsList);
		
		
	
		this.currentGroupsLabel = new JLabel("Current Memberships");
		this.currentGroupsListModel = new GroupsListModel(user, workspace, true);
		this.currentGroupsList = new JList(this.currentGroupsListModel);
		this.currentGroupsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.currentGroupsList.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
		this.groupsLabel = new JLabel("Edit user memberships");
		this.currentGroupsScrollPane = new JScrollPane(this.currentGroupsList);
	
		
		
		
		this.addButton = new JButton(">>");
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selection = availableGroupsList.getSelectedValues();
				if (selection.length==0) {
					return;
				}
				try {
					workspace.begin("Add user to groups");
					for (Object object : selection) {
						((Group)object).addMember(new GroupMember(user));
					}
				} finally {
					workspace.commit();
				}
			}
		});
		
		
		
		this.removeButton = new JButton("<<");
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] selection = currentGroupsList.getSelectedValues();
				if (selection.length==0) {
					return;
				}
				try {
					workspace.begin("Remove user from groups");
					Map<Group,GroupMember> toRemove = new ArrayMap<Group, GroupMember>();
					for (Object object : selection) {
						for (GroupMember membership : ((Group)object).getMembers()) {
							if (membership.getUser().getUUID().equals(user.getUUID())) {
								toRemove.put((Group)object, membership);
							}
						}
					}
					for (Entry<Group, GroupMember> entry : toRemove.entrySet()) {
						entry.getKey().removeMember(entry.getValue());
					}
				} finally {
					workspace.commit();
				}
			}
		});
		
		
		Action deleteAction = new DeleteFromTreeAction(
				this.workspace, 
				this.user,
				this.panel,
				this.workspace.getSession().getContext());
		this.toolbarBuilder.add(
				deleteAction,
				"Delete this user",
				WabitIcons.DELETE_ICON_32);
		
		
		// Panel building time
		JPanel namePassPanel = new JPanel(new MigLayout());
		namePassPanel.add(this.loginLabel, "align right, gaptop 20");
		namePassPanel.add(this.loginTextField, "span, wrap, wmin 600");
		namePassPanel.add(this.passwordLabel, "align right");
		namePassPanel.add(this.passwordTextField, "span, wrap, wmin 600");
		namePassPanel.add(this.fullNameLabel, "align right, gaptop 20");
		namePassPanel.add(this.fullNameTextField, "span, wrap, wmin 600");
		namePassPanel.add(this.emailLabel, "align right");
		namePassPanel.add(this.emailTextField, "span, wrap, wmin 600");
		this.panel.add(namePassPanel, "north");
		
		this.panel.add(this.groupsLabel, "span, wrap, gaptop 20, align center");
		
		JPanel buttonsPanel = new JPanel(new MigLayout());
		buttonsPanel.add(this.addButton, "wrap");
		buttonsPanel.add(this.removeButton);
		JPanel availablePanel = new JPanel(new MigLayout());
		availablePanel.add(this.availableGroupsLabel, "wrap, align center");
		availablePanel.add(this.availableGroupsScrollPane, "wmin 300");
		JPanel currentPanel = new JPanel(new MigLayout());
		currentPanel.add(this.currentGroupsLabel, "wrap, align center");
		currentPanel.add(this.currentGroupsScrollPane, "wmin 300");
		this.panel.add(availablePanel);
		this.panel.add(buttonsPanel, "shrink, span 1 2");
		this.panel.add(currentPanel);
	}
	
	
	
	public JComponent getSourceComponent() {
		return null;
	}

	public String getTitle() {
		return "User editor - "+user.getName();
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
                    
                    Group group2 = new Group("Other Group");
                    group2.setParent(p);
                    
                    p.addUser(user);
                	p.addGroup(group);
                	p.addGroup(group2);
                	
                	
                	
                	UserPanel panel = new UserPanel(user);
                	
                	UserPanel panel2 = new UserPanel(user);
                	
                	
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
