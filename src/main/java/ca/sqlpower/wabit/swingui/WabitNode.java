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

package ca.sqlpower.wabit.swingui;

import ca.sqlpower.wabit.WabitObject;

/**
 * An interface that all of our view objects. It serves two main purposes:
 * first, a standard way of getting the WabitObject each type of view component
 * represents; second, a standard way of telling the node that we're done with
 * it (so it can unlisten to the model and clean up any other stuff that needs
 * cleaning up).
 */
public interface WabitNode {
    
    /**
     * Causes this object to unlisten to its model object and anything else
     * that could impede garbage collection. Once you have cleaned up a
     * WabitNode, you can't use it again.
     */
    void cleanup();

    /**
     * Returns the WabitObject that this WabitNode visually represents.
     */
    WabitObject getModel();
}
