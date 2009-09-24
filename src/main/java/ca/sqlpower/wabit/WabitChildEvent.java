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

/**
 * An event that is passed to listeners when a child is added to or removed from
 * its parent.
 */
public class WabitChildEvent {

    /**
     * The type of event that signals if a child is being added or removed.
     */
    public enum EventType {
        /**
         * Defines this event to be adding a child to the parent object.
         */
        ADDED,
        
        /**
         * Defines this event to be removing a child from the parent object.
         */
        REMOVED
    }

    /**
     * The parent that gained or lost a child.
     */
    private final WabitObject source;
    
    /**
     * The child type for which the parent gained or lost a child (Wabit Objects support
     * multiple child types).
     */
    private final Class<? extends WabitObject> childType;
    
    /**
     * The child that was added or removed.
     */
    private final WabitObject child;

    /**
     * The index of the child that was added or removed. This index is the
     * overall position in the list returned by
     * <code>source.getChildren()</code>, not just the position within the
     * separate list of just these children). For example, if the source is a
     * Schema, and the added child is a Cube called newCube, this is the same as
     * <code>schema.getChildren().indexOf(newCube)</code>, not
     * <code>schema.getCubes().indexOf(newCube)</code>.
     */
    private final int index;

    /**
     * Defines if this event is adding a child to its parent or removing the
     * child from its parent.
     */
    private final EventType type;

    /**
     * Creates a new event object that describes adding or removing a single
     * child of the given type to/from a parent.
     * 
     * @param source
     *            The parent that gained or lost a child.
     * @param childType
     *            The child type for which the parent gained or lost a child
     *            (Wabit Objects support multiple child types).
     * @param child
     *            The child that was added or removed.
     * @param index
     *            The index of the child that was added or removed (this is the
     *            overall index in the parent, not the index within one child type).
     */
    public WabitChildEvent(WabitObject source, Class<? extends WabitObject> childType, 
            WabitObject child, int index, EventType type) {
        this.source = source;
        this.childType = childType;
        this.child = child;
        this.index = index;
        this.type = type;
    }

    public WabitObject getSource() {
        return source;
    }

    public Class<? extends WabitObject> getChildType() {
        return childType;
    }

    public WabitObject getChild() {
        return child;
    }

    public int getIndex() {
        return index;
    }
    
    public EventType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "Parent: " + source + "; child: " + child + "; index " + index;
    }

}
