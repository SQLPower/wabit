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

package ca.sqlpower.wabit.dao;

import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;

/**
 * An interface for objects that persist {@link WabitObject}s. The medium into
 * which they are persisted is entirely up to the implementation.
 */
public interface WabitPersister {

	/**
	 * Persists a property into a WabitObject. It may throw an {@link Exception}
	 * if the actual previous value in persistent storage does not match the
	 * expected previous value as an indication to the object using this
	 * WabitPersister that their cached copy of the {@link WabitObject} may be
	 * out of sync with the persistent storage.
	 * 
	 * @param object
	 *            The {@link WabitObject} in which to set the property
	 * @param propertyName
	 *            The name of the property to persist
	 * @param oldValue
	 *            The expected previous value of the property
	 * @param newValue
	 *            The value to set the property to
	 */
	public void persistProperty(WabitObject object, String propertyName, Object oldValue, Object newValue);

	/**
	 * Adds a {@link WabitObject} into the persistent storage. If the
	 * WabitObject has children, then they will also be persisted. If the
	 * WabitObject already exists in persistent storage, then it will update the
	 * existing WabitObject instead of creating a new one.
	 * 
	 * @param parent
	 *            The parent {@link WabitObject} of the object to persist. If
	 *            the WabitObject has no parent (as is the case for
	 *            {@link WabitWorkspace}), then it can be set to null.
	 * @param child
	 *            The object to actually persist
	 */
	public void persistObject(WabitObject parent, WabitObject child);
	
	/**
	 * Removes a WabitObject from persistent storage
	 * 
	 * @param parent
	 *            The parent {@link WabitObject} of the object to remove
	 * @param child
	 *            The {@link WabitObject} to remove
	 */
	public void removeObject(WabitObject parent, WabitObject child);
	
	/**
	 * Indicates the start of an atomic transaction of persisting multiple
	 * {@link WabitObject}s
	 */
	public void begin();
	
	/**
	 * Causes a current {@link WabitObject} persistence transaction to commit
	 * its results.
	 */
	public void commit();
}
