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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SQLPowerUtils;

public class WabitUtils {

	/**
	 * This method returns a list of all of the ancestors of the given
	 * {@link WabitObject}. The order of the ancestors is such that the highest
	 * ancestor is at the start of the list and the parent of the object itself
	 * is at the end of the list.
	 * 
	 * @see SQLPowerUtils#getAncestorList(SPObject)
	 */
    public static List<WabitObject> getAncestorList(WabitObject o) {
    	List<WabitObject> ancestors = new ArrayList<WabitObject>();
    	for (SPObject spo : SQLPowerUtils.getAncestorList(o)) {
    		ancestors.add((WabitObject) spo);
    	}
    	return ancestors;
    }

	/**
	 * Walks up the parent chain of {@link SPObject}s and returns the
	 * {@link WabitWorkspace} that these objects belong to or null if the
	 * workspace does not exist.
	 * 
	 * @param o
	 *            The object to follow the parent chain.
	 * @return A {@link WabitWorkspace} that contains the given SPObject and all
	 *         of its children or null if the given object is not in a
	 *         workspace.
	 */
    public static WabitWorkspace getWorkspace(SPObject o) {
        SPObject ancestor = o;
        while (ancestor.getParent() != null) {
            ancestor = ancestor.getParent();
        }
        if (ancestor instanceof WabitWorkspace) {
            return ((WabitWorkspace) ancestor);
        } else {
        	return null;
        }
    }

	/**
	 * Walks up the parent chain of {@link SPObject}s and returns the
	 * {@link WabitSession} that these objects belong to. This can throw a
	 * {@link SessionNotFoundException} if the object is not attached to a
	 * session.
	 * 
	 * @param o
	 *            The object to follow the parent chain.
	 * @return A WabitSession that contains the given SPObject and all of its
	 *         children.
	 */
    public static WabitSession getSession(SPObject o) {
    	WabitWorkspace workspace = getWorkspace(o);
    	if (workspace != null  && workspace.getSession() != null) 
    		return workspace.getSession();
        throw new SessionNotFoundException("No session exists for " + o.getName() + " of type " +
                o.getClass());
    }

	/**
	 * Generates a new UUID in the format suitable for use with any
	 * WabitObject's UUID property.
	 */
	public static String randomWabitUUID() {
		return "w" + UUID.randomUUID().toString();
	}

}
