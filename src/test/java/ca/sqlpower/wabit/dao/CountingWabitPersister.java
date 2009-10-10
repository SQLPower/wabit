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

import ca.sqlpower.wabit.dao.WabitSessionPersister.PersistedWabitObject;
import ca.sqlpower.wabit.dao.WabitSessionPersister.WabitObjectProperty;

public class CountingWabitPersister implements WabitPersister {
	
	private int persistObjectCount = 0;
	private int persistPropertyCount = 0;
	private int persistPropertyUnconditionallyCount = 0;
	private int removeObjectCount = 0;
	
	private final List<WabitObjectProperty> propertiesPersisted = new ArrayList<WabitObjectProperty>();
	
	private PersistedWabitObject lastPersistObject;  

	public void begin() throws WabitPersistenceException {
		// TODO Auto-generated method stub

	}

	public void commit() throws WabitPersistenceException {
		// TODO Auto-generated method stub

	}

	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws WabitPersistenceException {
		persistObjectCount++;
		lastPersistObject = new PersistedWabitObject(parentUUID, type, uuid, index);
	}


	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws WabitPersistenceException {
		persistPropertyCount++;
		propertiesPersisted.add(new WabitObjectProperty(uuid, propertyName, propertyType, oldValue, 
				newValue, false));
	}

	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws WabitPersistenceException {
		persistPropertyUnconditionallyCount++;
		propertiesPersisted.add(new WabitObjectProperty(uuid, propertyName, propertyType, null, 
				newValue, true));
	}

	public void removeObject(String parentUUID, String uuid)
			throws WabitPersistenceException {
		removeObjectCount++;
	}

	public void rollback() throws WabitPersistenceException {
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
	
	public PersistedWabitObject getLastPersistObject() {
		return lastPersistObject;
	}
	
	public List<WabitObjectProperty> getAllPropertyChanges() {
		return Collections.unmodifiableList(propertiesPersisted);
	}
}
