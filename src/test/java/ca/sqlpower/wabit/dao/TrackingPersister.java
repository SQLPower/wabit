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

import org.apache.log4j.Logger;

/**
 * This {@link WabitPersister} tracks each method call made to it for the sole
 * purpose of being used in tests.
 */
public class TrackingPersister implements WabitPersister {
	
	private static final Logger logger = Logger.getLogger(TrackingPersister.class);
	
	private final List<Object> persisterCalls = new ArrayList<Object>();
	
	private int beginCount = 0;
	private int commitCount = 0;
	private int rollbackCount = 0;
	private int persistObjectCount = 0;
	private int persistPropertyCount = 0;
	private int removeObjectCount = 0;

	public void begin() throws WabitPersistenceException {
		beginCount++;
		persisterCalls.add(WabitPersistMethod.begin);
	}

	public void commit() throws WabitPersistenceException {
		commitCount++;
		persisterCalls.add(WabitPersistMethod.commit);
	}
	
	public void rollback() {
		rollbackCount++;
		persisterCalls.add(WabitPersistMethod.rollback);
	}

	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws WabitPersistenceException {
		persistObjectCount++;
		persisterCalls.add(new PersistedWabitObject(parentUUID, type, uuid, index));
	}

	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws WabitPersistenceException {
		persistPropertyCount++;
		persisterCalls.add(new WabitObjectProperty(uuid, propertyName, propertyType, oldValue, newValue, false));
	}

	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws WabitPersistenceException {
		persistPropertyCount++;
		persisterCalls.add(new WabitObjectProperty(uuid, propertyName, propertyType, null, newValue, true));
	}

	public void removeObject(String parentUUID, String uuid)
			throws WabitPersistenceException {
		removeObjectCount++;
		persisterCalls.add(new RemovedWabitObject(parentUUID, uuid));
	}
	
	public int getBeginCount() {
		return beginCount;
	}
	
	public int getCommitCount() {
		return commitCount;
	}
	
	public int getRollbackCount() {
		return rollbackCount;
	}
	
	public int getPersistObjectCount() {
		return persistObjectCount;
	}
	
	public int getPersistPropertyCount() {
		return persistPropertyCount;
	}
	
	public int getRemoveObjectCount() {
		return removeObjectCount;
	}
	
	public List<Object> getPersisterCalls() {
		return Collections.unmodifiableList(persisterCalls);
	}
	
	/**
	 * Resets all the counters and persister method calls tracked by this persister.
	 */
	public void reset() {
		beginCount = 0;
		commitCount = 0;
		persistObjectCount = 0;
		persistPropertyCount = 0;
		removeObjectCount = 0;
		rollbackCount = 0;
		persisterCalls.clear();
	}

}
