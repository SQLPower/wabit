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

import ca.sqlpower.wabit.WabitWorkspace;

/**
 * This query list model will contain a list of all the queries in the project.
 * This is for dragging in queries to a layout.
 */
public class QueryListModel implements ListModel {

	private final WabitWorkspace project;
	private final List<ListDataListener> listListeners = new ArrayList<ListDataListener>();

	public QueryListModel(WabitWorkspace project) {
		this.project = project;
	}

	public void addListDataListener(ListDataListener l) {
		listListeners.add(l);
	}

	public Object getElementAt(int index) {
	    if (index < project.getQueries().size()) {
	        return project.getQueries().get(index);
	    } else {
	        return project.getOlapQueries().get(index - project.getQueries().size());
	    }
	}

	public int getSize() {
		return project.getQueries().size() + project.getOlapQueries().size();
	}

	public void removeListDataListener(ListDataListener l) {
		listListeners.remove(l);
	}

}
