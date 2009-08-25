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

package ca.sqlpower.wabit.swingui.report;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;

/**
 * This query list model will contain a list of all the queries in the
 * workspace. This is for dragging in queries, images, charts, and so on to a
 * layout.
 * <p>
 * Takes a snapshot of the eligible workspace items when it's constructed. This
 * behaviour replaces a previous version which tried to be dynamic, but didn't
 * watch the session for changes (and never fired a ListDataEvent). In practice,
 * we make one of these every time we show an editor, so the fact that it doesn't
 * update is not (currently) a problem. If that changes, we can make this fancier.
 */
public class DraggableWabitObjectListModel implements ListModel {

	private final List<WabitObject> items;
	
	public DraggableWabitObjectListModel(WabitWorkspace workspace) {
		items = new ArrayList<WabitObject>();
		items.addAll(workspace.getQueries());
		items.addAll(workspace.getOlapQueries());
		items.addAll(workspace.getCharts());
		items.addAll(workspace.getImages());
	}

	public void addListDataListener(ListDataListener l) {
	    // no op
	}

	public void removeListDataListener(ListDataListener l) {
	    // no op
    }

	public Object getElementAt(int index) {
	    if (index < 0 || index >= items.size()) {
	        // Swing library code requires this questionable behaviour
	        return null;
	    } else {
	        return items.get(index);
	    }
	}

	public int getSize() {
		return items.size();
	}
}
