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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;

public class StubWabitObject implements WabitObject {

    public void addChildListener(WabitChildListener l) {
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
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

    public String getName() {
        return null;
    }
    
	public void setName(String name) {
		
	}

    public WabitObject getParent() {
        return null;
    }

    public void removeChildListener(WabitChildListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    public void setParent(WabitObject parent) {
    }

	public String getUUID() {
		return null;
	}

    public List<WabitObject> getDependencies() {
        return null;
    }

}
