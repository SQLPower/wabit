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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class GroupMember extends AbstractWabitObject {

    private final User user;
    
    public GroupMember(User user) {
        this.user = user;
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return null;
    }

    public List<WabitObject> getDependencies() {
        return Collections.singletonList((WabitObject)this.user);
    }

    public void removeDependency(WabitObject dependency) {
        if (dependency.equals(this.user)) {
            ((Group)getParent()).removeMember(this);
        }
    }

	public User getUser() {
		return user;
	}

    
}
