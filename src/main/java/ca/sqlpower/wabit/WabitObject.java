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

import java.util.List;

import javax.annotation.Nonnull;

public interface WabitObject {

    /**
     * Adds a listener that will be notified when children are added to or
     * removed from this object, when properties change, and when a transaction
     * starts and ends. The events will always be fired in the foreground.
     * 
     * @param l
     *            The listener to add.
     * @see WabitListener
     * @see WabitSession#runInForeground(Runnable)
     */
    void addWabitListener(WabitListener l);

    /**
     * Removes a listener that was previously attached to this wabit object.
     * 
     * @param l
     *            The listener to remove.
     */
    void removeWabitListener(WabitListener l);

    /**
     * Returns the parent of this WabitObject. This will be null when the object
     * is first created until it is added as a child to another object. If this
     * object is never added as a child to another object this will remain null
     * and the object may be treated as the root node of a {@link WabitObject}
     * tree.
     * 
     * @return The parent of this object.
     */
    WabitObject getParent();

    /**
     * Sets the parent of this object to the given object. This should only be
     * done when this object is being added as a child to another object.
     * 
     * @param parent
     *            The new parent of this object.
     */
    void setParent(WabitObject parent);
    
    /**
     * Returns an unmodifiable list of the children in this WabitObject. If there
     * are no children in this WabitObject an empty list should be returned.
     */
    List<? extends WabitObject> getChildren();

    /**
     * Returns true if this object may contain children. Not all types of
     * WabitObjects can be a child to any WabitObject.
     * 
     * @see WabitObject#childPositionOffset(Class)
     */
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
    
    void setUUID(String uuid);
    
    /**
     * Sets the UUID of this object to a newly generated UUID. This is necessary
     * if the object is being cloned or copied to a new workspace.
     */
    void generateNewUUID();
    
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

    /**
     * Disconnects this object from any other objects it is listening to, closes
     * any open connections, and performs any other necessary operations to
     * ensure that this object can be discarded. Once this object has been
     * cleaned up none of its methods should be called. This method will only
     * cleanup this object and not its descendants. To clean up this object and
     * its descendants see {@link WabitUtils#cleanupWabitObject(WabitObject)}.
     * <p>
     * Calling cleanup does not mean the object must be disconnected from the
     * workspace as all objects will be cleaned up when the session is closing.
     * The object can also still have other objects dependent on it unlike
     * {@link #removeChild(WabitObject)}.
     * 
     * @return A collection of exceptions and errors that occurred during
     *         cleanup if any occurred.
     */
    CleanupExceptions cleanup();
    
}
