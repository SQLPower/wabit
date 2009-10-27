/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Grant;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.User;

public class UsersAndGroupsListModel implements ListModel, WabitListener {

	private final List<WabitObject> items;
	private final WabitWorkspace workspace;
	private final Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	private final GrantPanel grantPanel;
	
	public UsersAndGroupsListModel(WabitWorkspace workspace, GrantPanel grantPanel) {
		this.grantPanel = grantPanel;
		items = new ArrayList<WabitObject>();
		this.workspace = workspace;
		updateList();
		this.workspace.addWabitListener(this);
		for (Group group : this.workspace.getGroups()) {
			group.addWabitListener(this);
		}
		for (User user : this.workspace.getUsers()) {
			user.addWabitListener(this);
		}
	}
	
	private void updateList() {
		this.items.clear();
		List<Group> groups = new LinkedList<Group>();
		groups.addAll(this.workspace.getGroups());
		Collections.sort(groups, new Comparator<Group>() {
			public int compare(Group o1, Group o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		this.items.addAll(groups);
		List<User> users = new LinkedList<User>();
		users.addAll(this.workspace.getUsers());
		Collections.sort(users, new Comparator<User>() {
			public int compare(User o1, User o2) {
				return o2.getName().compareTo(o1.getName());
			}
		});
		this.items.addAll(users);
	}
	
	private void fireChange() {
		for (ListDataListener l : this.listeners) {
			l.contentsChanged(
				new ListDataEvent(
					this,
					ListDataEvent.CONTENTS_CHANGED,
					0,
					items.size()));
		}
	}

	public void addListDataListener(ListDataListener l) {
	    this.listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
	    this.listeners.remove(l);
    }

	public Object getElementAt(int index) {
	    if (index < 0 || index >= items.size()) {
	        // Swing library code requires this questionable behaviour
	        return null;
	    } else {
	        return items.get(index);
	    }
	}

	public int getSize() {
		return items.size();
	}

	public void transactionEnded(TransactionEvent e) {
		// no-op
	}

	public void transactionRollback(TransactionEvent e) {
		// no-op
	}

	public void transactionStarted(TransactionEvent e) {
		// no-op
	}

	public void wabitChildAdded(WabitChildEvent e) {
		if (e.getChild() instanceof Group ||
				e.getChild() instanceof Group) {
			e.getChild().addWabitListener(this);
			updateList();
			fireChange();
		} else if ((e.getSource() instanceof Group ||
				e.getSource() instanceof User) &&
				e.getChild() instanceof Grant) {
			this.grantPanel.updateGrantsList();
			this.grantPanel.updateGrantSettings();	
		}
	}

	public void wabitChildRemoved(WabitChildEvent e) {
		if (e.getChild() instanceof Group ||
				e.getChild() instanceof Group) {
			e.getChild().removeWabitListener(this);
			updateList();
			fireChange();
		} else if ((e.getSource() instanceof Group ||
				e.getSource() instanceof User) &&
				e.getChild() instanceof Grant) {
			this.grantPanel.updateGrantsList();
			this.grantPanel.updateGrantSettings();	
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// no op
	}
}
