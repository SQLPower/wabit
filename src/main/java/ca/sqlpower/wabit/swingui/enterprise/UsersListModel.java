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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitWorkspace;

public class UsersListModel implements ListModel, SPListener {

	private final List<User> items;
	private final WabitWorkspace workspace;
	private final Group group;
	private final Set<ListDataListener> listeners = new HashSet<ListDataListener>();
	private final boolean currentMode;
	
	public UsersListModel(Group group, WabitWorkspace workspace, boolean currentMode) {
		this.group = group;
		this.currentMode = currentMode;
		items = new ArrayList<User>();
		this.workspace = workspace;
		updateList();
		this.workspace.addSPListener(this);
		this.group.addSPListener(this);
	}
	
	private void updateList() {
		items.clear();
		if (currentMode) { // Means we want the current users in this list
			for (GroupMember membership : group.getChildren(GroupMember.class)) {
				items.add(membership.getUser());
			}
		} else {
			List<User> tmpList = new ArrayList<User>();
			for (GroupMember membership : group.getChildren(GroupMember.class)) {
				tmpList.add(membership.getUser());
			}
			items.addAll(workspace.getUsers());
			items.removeAll(tmpList);
		}
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

	public void childAdded(SPChildEvent e) {
		if (e.getChild() instanceof User) {
			updateList();
			fireChange();
		} else if (e.getSource().getUUID().equals(this.group.getUUID()) &&
				e.getChild() instanceof GroupMember) {
			updateList();
			fireChange();
		}
	}

	public void childRemoved(SPChildEvent e) {
		if (e.getSource() instanceof WabitWorkspace &&
				e.getChild() instanceof User) {
			updateList();
			fireChange();
		} else if (e.getSource().getUUID().equals(this.group.getUUID()) &&
				e.getChild() instanceof GroupMember) {
			updateList();
			fireChange();
		}
	}

	public void propertyChanged(PropertyChangeEvent evt) {
		// no op
	}
}
