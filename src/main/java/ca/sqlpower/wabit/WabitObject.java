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

import javax.annotation.Nonnull;

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
     * Removes the given child object from this object. If the given child is
     * not an actual child of this object an illegal argument exception will be
     * thrown. If the child has dependencies and cannot be removed an object
     * dependent exception will be thrown.
     * 
     * @param child
     *            The object to remove as a child of this object.
     * @return True if the child was successfully removed. False if the child
     *         was not removed from this object.
     */
    boolean removeChild(WabitObject child) throws ObjectDependentException, IllegalArgumentException;
    
    /**
     * Returns the short name for this object.
     */
    String getName();
    
    /**
     * Sets the name for this object 
     */
    void setName(String name);
    
    String getUUID();
    
    /**
     * Removes the given object as a dependency of this object. For this object
     * to no longer be dependent on the given dependency all of its children
     * must also not be dependent on the given dependency when this method
     * returns. This may remove this object from its parent if necessary.
     */
    void removeDependency(@Nonnull WabitObject dependency);

    /**
     * Returns a list of all {@link WabitObject}s that this Wabit object is
     * dependent on. Children of a WabitObject are not dependencies and will not
     * be returned in this list. If there are no objects this Wabit object is
     * dependent on an empty list should be returned. These are only the
     * immediate dependencies of this object. If you want to find the
     * dependencies of this object's dependencies as well it may be useful to
     * look at {@link WorkspaceGraphModel} to make a full graph of all of the
     * dependencies.
     */
    List<WabitObject> getDependencies();
    
}
