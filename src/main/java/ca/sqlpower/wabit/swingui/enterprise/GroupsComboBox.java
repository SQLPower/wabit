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

import java.beans.PropertyChangeEvent;

import javax.swing.JComboBox;

import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.GroupMember;
import ca.sqlpower.wabit.enterprise.client.User;

public class GroupsComboBox extends JComboBox implements WabitListener {

	private final WabitWorkspace workspace;
	private final User user;

	public GroupsComboBox(WabitWorkspace workspace, User user) {
		super();
		this.workspace = workspace;
		this.user = user;
		updateList();
		this.workspace.addWabitListener(this);
		for (Group group : workspace.getGroups()) {
			group.addWabitListener(this);
		}
	}
	
	private void updateList() {
		removeAllItems();
		for (Group group : workspace.getGroups()) {
			boolean found = false;
			for (GroupMember member : group.getMembers()) {
				if (member.getUser().getUUID().equals(user.getUUID())) {
					found = true;
					break;
				}
			}
			if (!found)
				addItem(group);
		}
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
		if (e.getChild() instanceof GroupMember) {
			updateList();
		} else if (e.getChild() instanceof Group) {
			e.getChild().addWabitListener(this);
			updateList();
		}
	}

	public void wabitChildRemoved(WabitChildEvent e) {
		if (e.getChild() instanceof GroupMember) {
			updateList();
		} else if (e.getChild() instanceof Group) {
			e.getChild().removeWabitListener(this);
			updateList();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// no-op
	}

}
