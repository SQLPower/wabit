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
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.ObjectDependentException;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Grant;
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
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;

public class GrantPanel implements DataEntryPanel {

	private boolean dirty = false;
	private final String objectType;
	private final String objectUuid;
	private final WabitWorkspace workspace;
	private final WabitWorkspace systemWorkspace;
	
	private final JList list;
	private final JScrollPane scrollPane;
	
	private final JCheckBox createCheckBox;
	private final JCheckBox modifyCheckBox;
	private final JCheckBox deleteCheckBox;
	private final JCheckBox executeCheckBox;
	private final JCheckBox grantCheckBox;
	
	
	
	private final UsersAndGroupsListModel listModel;
	private final Map<String,Grant> grants = new HashMap<String, Grant>();
	
	private final JLabel topLabel;
	private final JLabel bottomLabel;
	private final JPanel labelPanel = new JPanel(new MigLayout());
	
	private final JPanel panel = new JPanel(new MigLayout());
	private final JPanel checkboxPanel = new JPanel(new MigLayout());
	private final boolean systemMode;
	
	private final JLabel icon;
	

	public GrantPanel(
			@Nonnull WabitWorkspace workspace, 
			@Nonnull WabitWorkspace systemWorkspace, 
			@Nonnull String objectType, 
			@Nullable String objectUuid, 
			@Nonnull String label) {
		
		if (objectUuid == null && objectType != null) {
			this.systemMode = true;
			this.topLabel = new JLabel("Server Wide Security Settings");
			this.bottomLabel = new JLabel("Please configure system level permissions for " + label);
			this.icon = new JLabel(WabitIcons.SERVER_ICON_32);
		} else if (objectUuid != null && objectType != null) {
			this.systemMode = false;
			this.topLabel = new JLabel("Sharing and Security Settings");
			this.bottomLabel = new JLabel("Please configure who has access to " + label);
			this.icon = new JLabel(WabitIcons.SECURITY_ICON_32);
		} else {
			throw new RuntimeException ("You must either supply objectType or both objectType and objectUuid parameters.");
		}
		
		this.workspace = workspace;
		this.systemWorkspace = systemWorkspace;
		this.objectType = objectType;
		this.objectUuid = objectUuid;
		
		
		
		
		this.listModel = new UsersAndGroupsListModel(this.systemWorkspace, this);
		this.list = new JList(this.listModel);
		this.list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		this.list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateGrantSettings();
			}
		});
		this.list.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
		this.scrollPane = new JScrollPane(this.list);
		
		
		
		String suffix = "";
		if (this.systemMode) {
			suffix = " any";
		}
		
		this.createCheckBox = new JCheckBox("Create"+suffix);
		this.createCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uuid = ((WabitObject)list.getSelectedValue()).getUUID();
				grants.get(uuid).setCreatePrivilege(createCheckBox.isSelected());
				grants.get(uuid).setDirty(true);
				dirty = true;
			}
		});
		this.modifyCheckBox = new JCheckBox("Modify"+suffix);
		this.modifyCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uuid = ((WabitObject)list.getSelectedValue()).getUUID();
				grants.get(uuid).setModifyPrivilege(modifyCheckBox.isSelected());
				grants.get(uuid).setDirty(true);
				dirty = true;
			}
		});
		this.deleteCheckBox = new JCheckBox("Delete"+suffix);
		this.deleteCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uuid = ((WabitObject)list.getSelectedValue()).getUUID();
				grants.get(uuid).setDeletePrivilege(deleteCheckBox.isSelected());
				grants.get(uuid).setDirty(true);
				dirty = true;
			}
		});
		this.executeCheckBox = new JCheckBox("Execute"+suffix);
		this.executeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uuid = ((WabitObject)list.getSelectedValue()).getUUID();
				grants.get(uuid).setExecutePrivilege(executeCheckBox.isSelected());
				grants.get(uuid).setDirty(true);
				dirty = true;
			}
		});
		this.grantCheckBox = new JCheckBox("Grant"+suffix);
		this.grantCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String uuid = ((WabitObject)list.getSelectedValue()).getUUID();
				grants.get(uuid).setGrantPrivilege(grantCheckBox.isSelected());
				grants.get(uuid).setDirty(true);
				dirty = true;
			}
		});
		
		this.checkboxPanel.add(this.createCheckBox, "wrap");
		this.checkboxPanel.add(this.modifyCheckBox, "wrap");
		this.checkboxPanel.add(this.deleteCheckBox, "wrap");
		this.checkboxPanel.add(this.executeCheckBox, "wrap");
		this.checkboxPanel.add(this.grantCheckBox, "wrap");
		
		this.topLabel.setFont(this.topLabel.getFont().deriveFont(new Float(this.topLabel.getFont().getSize()+8)));
		this.labelPanel.add(this.icon, "spany 2, gapright 10");
		this.labelPanel.add(this.topLabel, "wrap");
		this.labelPanel.add(this.bottomLabel);
		panel.add(labelPanel, "north, gapbottom 20, gaptop 10");
		
		
		panel.add(scrollPane, "west, wmin 500, hmin 400");
		panel.add(this.checkboxPanel, "east");
		
		updateGrantsList();
	}
	
	void updateGrantsList() {
		// We need to inspect each group and each user and select
		// from the list those that have a grant on the required type
		for (int i = 0; i < this.list.getModel().getSize(); i++) {
			if (this.list.getModel().getElementAt(i) instanceof Group) {
				Group group = (Group)this.list.getModel().getElementAt(i);
				for (Grant grant : group.getGrants()) {
					if (this.systemMode && grant.getType().equals(this.objectType)) {
						this.grants.put(group.getUUID(), grant);
					} else if (!this.systemMode && this.objectUuid.equals(grant.getSubject())) {
						this.grants.put(group.getUUID(), grant);
					}
				}
			} else {
				User user = (User)this.list.getModel().getElementAt(i);
				for (Grant grant : user.getGrants()) {
					if (this.systemMode && grant.getType().equals(this.objectType)) {
						this.grants.put(user.getUUID(), grant);
					} else if (!this.systemMode && this.objectUuid.equals(grant.getSubject())) {
						this.grants.put(user.getUUID(), grant);
					}
				}
			}
		}
	}
	
	void updateGrantSettings() {
		
		WabitObject currentSelection = (WabitObject)this.list.getSelectedValue();
		Grant grant = this.grants.get(currentSelection.getUUID());
		
		// if grant is null, this means there was no grant given so far.
		if(grant == null && systemMode) {
			grant = new Grant(null, this.objectType, false,false,false,false,false);
			grant.setDirty(true);
		} else if(grant == null && !systemMode) {
			grant = new Grant(this.objectUuid, this.workspace.findByUuid(this.objectUuid, WabitObject.class).getClass().getSimpleName(), false,false,false,false,false);
			grant.setDirty(true);
		}
		
		this.createCheckBox.setSelected(grant.isCreatePrivilege());
		this.modifyCheckBox.setSelected(grant.isModifyPrivilege());
		this.deleteCheckBox.setSelected(grant.isDeletePrivilege());
		this.executeCheckBox.setSelected(grant.isExecutePrivilege());
		this.grantCheckBox.setSelected(grant.isGrantPrivilege());
		
	}

	public boolean applyChanges() {
		if (!this.dirty) {
			return true;
		}
		this.systemWorkspace.beginTransaction("Updating grants...");
		try {
			for (Entry<String, Grant> entry : this.grants.entrySet()) {
				if (entry.getValue().isDirty()) {
					Grant grant = entry.getValue();
					if (!grant.isCreatePrivilege() && !grant.isDeletePrivilege() &&
							!grant.isExecutePrivilege() && !grant.isGrantPrivilege() &&
							!grant.isModifyPrivilege()) {
						// Grant is empty. we remove it.
						this.systemWorkspace.findByUuid(entry.getKey(), WabitObject.class).removeChild(grant);
					} else if (grant.isDirty()) {
						// This means we need to save or update the grant
						Grant persistedGrant = this.systemWorkspace.findByUuid(grant.getUUID(), Grant.class);
						if (persistedGrant!=null) {
							persistedGrant.setCreatePrivilege(grant.isCreatePrivilege());
							persistedGrant.setModifyPrivilege(grant.isModifyPrivilege());
							persistedGrant.setDeletePrivilege(grant.isDeletePrivilege());
							persistedGrant.setExecutePrivilege(grant.isExecutePrivilege());
							persistedGrant.setGrantPrivilege(grant.isGrantPrivilege());
						} else {
							// We're dealing with a new grant
							this.systemWorkspace.findByUuid(entry.getKey(), WabitObject.class).addChild(grant,0);
						}
					}
				}
			}
			this.systemWorkspace.commitTransaction();
			return true;
		} catch (IllegalArgumentException e) {
			this.systemWorkspace.rollbackTransaction();
			throw new RuntimeException(e);
		} catch (ObjectDependentException e) {
			this.systemWorkspace.rollbackTransaction();
			throw new RuntimeException(e);
		}
	}

	public void discardChanges() {
		// no-op
	}

	public JComponent getPanel() {
		return this.panel;
	}

	public boolean hasUnsavedChanges() {
		return this.dirty;
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
        			GroupMember member = new GroupMember(user);
        			Group group = new Group("Server Administrators");
        			group.addMember(member);
        			Grant workspaceGrant = new Grant(null, WabitWorkspace.class.getSimpleName(), true,true,true,true,true);
        			group.addGrant(workspaceGrant);
        			Grant userGrant = new Grant(null, User.class.getSimpleName(), true,true,true,true,true);
        			group.addGrant(userGrant);
        			Grant groupGrant = new Grant(null, Group.class.getCanonicalName(), true,true,true,true,true);
        			group.addGrant(groupGrant);
        			Grant grantGrant = new Grant(null, Grant.class.getCanonicalName(), true,true,true,true,true);
        			group.addGrant(grantGrant);
                    
                    Group group2 = new Group("Other Group");
                    group2.setParent(p);
                    
                    p.addUser(user);
                	p.addGroup(group);
                	p.addGroup(group2);
                	
                	
                	
                	GrantPanel panel = new GrantPanel(p, p, User.class.getSimpleName(), null, "Label");
                	
                    JFrame f = new JFrame("TEST PANEL");
                    JPanel outerPanel = new JPanel(new BorderLayout());
                    outerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel.add(panel.getPanel(), BorderLayout.CENTER);
                    f.setContentPane(outerPanel);
                    f.pack();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                    
                    
                    GrantPanel panel2 = new GrantPanel(p, p, User.class.getSimpleName(), null, "label");
                    JFrame f2 = new JFrame("TEST PANEL");
                    JPanel outerPanel2 = new JPanel(new BorderLayout());
                    outerPanel2.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel2.add(panel2.getPanel(), BorderLayout.CENTER);
                    f2.setContentPane(outerPanel2);
                    f2.pack();
                    f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f2.setVisible(true);
//                    
                    
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

}
