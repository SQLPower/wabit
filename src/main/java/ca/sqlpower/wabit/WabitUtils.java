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

public class WabitUtils {

    /**
     * Adds the given listeners to the hierarchy of Wabit objects rooted at
     * <code>root</code>.
     * 
     * @param root
     *            The object at the top of the subtree to listen to. Must not be
     *            null.
     * @param pcl
     *            The property change listener to add to root and all its
     *            WabitObject descendants. If you do not want property change
     *            events, you can provide null for this parameter.
     * @param wcl
     *            The Wabit child listener to add to root and all its
     *            WabitObject descendants. If you do not want Wabit child
     *            events, you can provide null for this parameter.
     */
    public static void listenToHierarchy(WabitObject root, PropertyChangeListener pcl, WabitChildListener wcl) {
        root.addPropertyChangeListener(pcl);
        root.addChildListener(wcl);
        for (WabitObject wob : root.getChildren()) {
            listenToHierarchy(wob, pcl, wcl);
        }
    }

    /**
     * Removes the given listeners from the hierarchy of Wabit objects rooted at
     * <code>root</code>.
     * 
     * @param root
     *            The object at the top of the subtree to unlisten to. Must not
     *            be null.
     * @param pcl
     *            The property change listener to remove from root and all its
     *            WabitObject descendants. If you do not want to unlisten to
     *            property change events, you can provide null for this
     *            parameter.
     * @param wcl
     *            The Wabit child listener to remove from root and all its
     *            WabitObject descendants. If you do not want to unlisten to
     *            Wabit child events, you can provide null for this parameter.
     */
    public static void unlistenToHierarchy(WabitObject root, PropertyChangeListener pcl, WabitChildListener wcl) {
        root.removePropertyChangeListener(pcl);
        root.removeChildListener(wcl);
        for (WabitObject wob : root.getChildren()) {
            unlistenToHierarchy(wob, pcl, wcl);
        }
    }

}
