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
import java.util.Collections;
import java.util.List;

public class StubWabitObject implements WabitObject {

    public void addWabitListener(WabitListener l) {
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
        return Collections.emptyList();
    }

    public String getName() {
        return null;
    }
    
	public void setName(String name) {
		
	}

    public WabitObject getParent() {
        return null;
    }

    public void removeWabitListener(WabitListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    public void setParent(WabitObject parent) {
    }

	public String getUUID() {
		return "stubby";
	}
	
	public void setUUID(String uuid){
	}

    public List<WabitObject> getDependencies() {
        return null;
    }

    public void removeDependency(WabitObject dependency) {
        //do nothing
    }

    public boolean removeChild(WabitObject child)
            throws ObjectDependentException, IllegalArgumentException {
        return false;
    }

    public void generateNewUUID() {
        // TODO Auto-generated method stub
        
    }

    public CleanupExceptions cleanup() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addChild(WabitObject child, int index)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
    }

	public void begin(String message) {
		// TODO Auto-generated method stub
		
	}

	public void commit() {
		// TODO Auto-generated method stub
		
	}

	public void rollback(String message) {
		// TODO Auto-generated method stub
		
	}

}
