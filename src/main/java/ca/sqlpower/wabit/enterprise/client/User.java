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
import org.springframework.security.userdetails.UserDetails;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class User extends AbstractWabitObject implements UserDetails {

    private final List<Grant> grants;
    private String password;
    private GrantedAuthority[] authorities = null;

    public User(String username, String password) {
    	super();
        assert username != null;
        this.grants = new ArrayList<Grant>();
        this.password = password;
        super.setName(username);
    }

    protected boolean removeChildImpl(WabitObject child) {
        assert child instanceof Grant;
        return grants.remove((Grant)child);
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return this.grants;
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(WabitObject dependency) {
        // no-op
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        String oldPassword = this.password;
        this.password = password;
        firePropertyChange("password", oldPassword, password);
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
    
    /**
     * The returned list is mutable. Beware.
     */
    public List<Grant> getGrants() {
		return grants;
	}

	public GrantedAuthority[] getAuthorities() {
		if (this.authorities==null) {
			throw new RuntimeException("Programmatic error. The user manager has to fill in this user's groups before passing back to the security framework.");
		} else {
			return this.authorities;
		}
	}
	
	public void setAuthorities(GrantedAuthority[] authorities) {
		this.authorities = authorities;
	}

	public String getUsername() {
		return super.getName();
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}
}
