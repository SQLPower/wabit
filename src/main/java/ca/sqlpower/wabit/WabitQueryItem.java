/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.query.QueryItem;

/**
 * This wraps a {@link QueryItem} to allow it to be used in Wabit events.
 */
public abstract class WabitQueryItem extends AbstractWabitObject {
	
	private final QueryItem item;
	
	private final PropertyChangeListener itemListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("name")) {
				setName((String) evt.getNewValue());
			}
		}
	};

	public WabitQueryItem(QueryItem item) {
		this.item = item;
		setName(item.getDelegate().getName());
		item.getDelegate().addPropertyChangeListener(itemListener);
	}

	@Override
	public CleanupExceptions cleanup() {
		item.getDelegate().removePropertyChangeListener(itemListener);
		return new CleanupExceptions();
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
		return false;
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

	public List<WabitObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(WabitObject dependency) {
		//do nothing
	}

	public QueryItem getItem() {
		return item;
	}

}
