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

public interface WabitObject {

    void addChildListener(WabitChildListener l);
    void removeChildListener(WabitChildListener l);
    void addPropertyChangeListener(PropertyChangeListener l);
    void removePropertyChangeListener(PropertyChangeListener l);
    WabitObject getParent();
    void setParent(WabitObject parent);
    List<? extends WabitObject> getChildren();
    boolean allowsChildren();
    
    /**
     * Returns the position in the list that would be returned by getChildren()
     * that the first object of type childClass is, or where it would be if
     * there were any children of that type.
     *
     * @throws IllegalArgumentException if the given child class is not valid for
     * this OLAPObject.
     */
    int childPositionOffset(Class<? extends WabitObject> childType);
    
    /**
     * Returns the short name for this object.
     */
    String getName();
    
    /**
     * Sets the name for this object 
     */
    void setName(String name);
    
    UUID getUUID();
    
}
