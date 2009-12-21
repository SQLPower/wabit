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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.dao.PersistedSPOProperty;
import ca.sqlpower.dao.PersistedSPObject;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;

public class CountingWabitPersister implements SPPersister {
	
	private int persistObjectCount = 0;
	private int persistPropertyCount = 0;
	private int persistPropertyUnconditionallyCount = 0;
	private int removeObjectCount = 0;
	
	private final List<PersistedSPOProperty> propertiesPersisted = new ArrayList<PersistedSPOProperty>();
	
	private final List<PersistedSPObject> persistedObjects = new ArrayList<PersistedSPObject>();  

	public void begin() throws SPPersistenceException {
		// TODO Auto-generated method stub

	}

	public void commit() throws SPPersistenceException {
		// TODO Auto-generated method stub

	}

	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws SPPersistenceException {
		persistObjectCount++;
		persistedObjects.add(new PersistedSPObject(parentUUID, type, uuid, index));
	}


	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws SPPersistenceException {
		persistPropertyCount++;
		propertiesPersisted.add(new PersistedSPOProperty(uuid, propertyName, propertyType, oldValue, 
				newValue, false));
	}

	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws SPPersistenceException {
		persistPropertyUnconditionallyCount++;
		propertiesPersisted.add(new PersistedSPOProperty(uuid, propertyName, propertyType, null, 
				newValue, true));
	}

	public void removeObject(String parentUUID, String uuid)
			throws SPPersistenceException {
		removeObjectCount++;
	}

	public void rollback() {
		// TODO Auto-generated method stub

	}

	public int getPersistObjectCount() {
		return persistObjectCount;
	}
	
	public int getPersistPropertyCount() {
		return persistPropertyCount;
	}
	
	public int getPersistPropertyUnconditionallyCount() {
		return persistPropertyUnconditionallyCount;
	}
	
	public int getRemoveObjectCount() {
		return removeObjectCount;
	}

	public String getLastUUID() {
		return propertiesPersisted.get(propertiesPersisted.size() - 1).getUUID();
	}

	public String getLastPropertyName() {
		return propertiesPersisted.get(propertiesPersisted.size() - 1).getPropertyName();
	}

	public DataType getLastDataType() {
		return propertiesPersisted.get(propertiesPersisted.size() - 1).getDataType();
	}
	
	public Object getLastOldValue() {
		return propertiesPersisted.get(propertiesPersisted.size() - 1).getOldValue();
	}
	
	public Object getLastNewValue() {
		return propertiesPersisted.get(propertiesPersisted.size() - 1).getNewValue();
	}
	
	public PersistedSPObject getLastPersistObject() {
		return persistedObjects.get(persistedObjects.size() - 1);
	}
	
	public List<PersistedSPObject> getAllPersistedObjects() {
		return Collections.unmodifiableList(persistedObjects);
	}
	
	public List<PersistedSPOProperty> getAllPropertyChanges() {
		return Collections.unmodifiableList(propertiesPersisted);
	}

	/**
	 * Removes all of the property change events that were stored to track what
	 * objects were persisted. This does not change the count of how many times
	 * the persist property method was called.
	 */
	public void clearAllPropertyChanges() {
		propertiesPersisted.clear();
	}
}
