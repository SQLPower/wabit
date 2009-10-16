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

package ca.sqlpower.wabit.enterprise.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.GrantedAuthority;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class Group extends AbstractWabitObject implements GrantedAuthority {

    private final List<Grant> grants = new ArrayList<Grant>();
    private final List<GroupMember> members = new ArrayList<GroupMember>();

    public Group(String name) {
    	setName(name);
    }
    
    @Override
    protected boolean removeChildImpl(WabitObject child) {
        if (child instanceof Grant) {
            return this.grants.remove((Grant)child);
        } else if (child instanceof User) {
            return this.members.remove((User)child);
        } else {
            return false;
        }
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        if (this.members.size() > 0) {
            return this.members.size() - 1;
        } else {
            return 0;
        }
    }

    public List<WabitObject> getChildren() {
        List<WabitObject> children = new ArrayList<WabitObject>();
        children.addAll(this.members);
        children.addAll(this.grants);
        return children;
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(WabitObject dependency) {
        // no-op
    }

    /**
     * Mutable list!! beware.
     * @return
     */
    public List<Grant> getGrants() {
        return grants;
    }

    public List<GroupMember> getMembers() {
        return members;
    }
    public void addGrant(Grant grant) {
        this.grants.add(grant);
        fireChildAdded(Grant.class, grant, this.grants.indexOf(grant));
    }
    
    public void removeGrant(Grant grant) {
        if (this.grants.contains(grant)) {
            int index = this.grants.indexOf(grant);
            this.grants.remove(grant);
            fireChildRemoved(Grant.class, grant, index);
        }
    }
    
    public void addMember(GroupMember member) {
        this.members.add(member);
        fireChildAdded(GroupMember.class, member, this.members.indexOf(member));
    }
    
    public void removeMember(GroupMember member) {
        if (this.members.contains(member)) {
            int index = this.members.indexOf(member);
            this.members.remove(member);
            fireChildRemoved(GroupMember.class, member, index);
        }
    }

	public String getAuthority() {
		return super.getName();
	}

	public int compareTo(Object o) {
		assert o instanceof GrantedAuthority;
		return ((GrantedAuthority)o).getAuthority().compareTo(this.getAuthority());
	}
	
	@Override
	public String toString() {
		return super.getName();
	}
}
