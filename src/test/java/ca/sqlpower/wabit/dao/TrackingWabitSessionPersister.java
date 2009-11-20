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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.wabit.WabitSession;

/**
 * This {@link SPPersister} tracks each method call made to it for the sole
 * purpose of being used in tests.
 */
public class TrackingWabitSessionPersister extends WabitSessionPersister {
	
	private static final Logger logger = Logger.getLogger(TrackingWabitSessionPersister.class);
	
	private CountDownLatch latch;
	private final List<Object> persisterCalls = new ArrayList<Object>();
	
	private int beginCount = 0;
	private int commitCount = 0;
	private int rollbackCount = 0;
	private int persistObjectCount = 0;
	private int persistPropertyCount = 0;
	private int removeObjectCount = 0;
	private List<PersistedWabitObject> savedPersistedObjects;
	private Map<String, String> savedObjectsToRemove;
	
	public TrackingWabitSessionPersister(WabitSession session, CountDownLatch latch) {
		super("TrackingWabitSessionPersister", session);
		this.latch = latch;
	}

	/**
	 * Increments the begin counter and adds this begin call to the list of
	 * persister calls.
	 */
	@Override
	public void begin() {
		beginCount++;
		persisterCalls.add(SPPersistMethod.begin);
		super.begin();
	}

	/**
	 * Increments the commit counter and adds this commit call to the list of
	 * persister calls.
	 */
	@Override
	public void commit() throws SPPersistenceException {
		commitCount++;
		persisterCalls.add(SPPersistMethod.commit);
		
		if (beginCount == commitCount) {
			Collections.sort(persistedObjects, persistedObjectComparator);
			
			savedPersistedObjects = new ArrayList<PersistedWabitObject>();
			for (PersistedWabitObject pwo : persistedObjects) {
				savedPersistedObjects.add(new PersistedWabitObject(
						pwo.getParentUUID(), pwo.getType(), pwo.getUUID(), pwo.getIndex()));
			}
			
			savedObjectsToRemove = new TreeMap<String, String>(removedObjectComparator);
			savedObjectsToRemove.putAll(objectsToRemove);
			
			super.commit();
			latch.countDown();
		} else {
			super.commit();
		}
		
	}
	
	/**
	 * Increments the rollback counter and adds this rollback call to the list of
	 * persister calls.
	 */
	@Override
	public void rollback() {
		rollbackCount++;
		persisterCalls.add(SPPersistMethod.rollback);
		super.rollback();
	}

	/**
	 * Increments the persistObject counter and adds this persistObject call to the list
	 * of persister calls.
	 */
	@Override
	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws SPPersistenceException {
		persistObjectCount++;
		persisterCalls.add(new PersistedWabitObject(parentUUID, type, uuid, index));
		super.persistObject(parentUUID, type, uuid, index);
	}

	/**
	 * Increments the persistProperty counter and adds this persistProperty call to the
	 * list of persister calls.
	 */
	@Override
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object oldValue, Object newValue)
			throws SPPersistenceException {
		persistPropertyCount++;
		persisterCalls.add(new WabitObjectProperty(uuid, propertyName, propertyType, oldValue, newValue, false));
		super.persistProperty(uuid, propertyName, propertyType, oldValue, newValue);
	}

	/**
	 * Increments the persistProperty counter and adds this persistProperty call to the
	 * list of persister calls.
	 */
	@Override
	public void persistProperty(String uuid, String propertyName,
			DataType propertyType, Object newValue)
			throws SPPersistenceException {
		persistPropertyCount++;
		persisterCalls.add(new WabitObjectProperty(uuid, propertyName, propertyType, null, newValue, true));
		super.persistProperty(uuid, propertyName, propertyType, newValue);
	}

	/**
	 * Increments the removeObject counter and adds this removeObject call to the
	 * list of persister calls.
	 */
	@Override
	public void removeObject(String parentUUID, String uuid)
			throws SPPersistenceException {
		removeObjectCount++;
		persisterCalls.add(new RemovedWabitObject(parentUUID, uuid));
		super.removeObject(parentUUID, uuid);
	}
	
	/**
	 * Returns the number of begin method calls this persister has received.
	 */
	public int getBeginCount() {
		return beginCount;
	}
	
	/**
	 * Returns the number of commit method calls this persister has received.
	 */
	public int getCommitCount() {
		return commitCount;
	}

	/**
	 * Returns the number of rollback method calls this persister has received.
	 */
	public int getRollbackCount() {
		return rollbackCount;
	}
	
	/**
	 * Returns the number of persistObject method calls this persister has received. 
	 */
	public int getPersistObjectCount() {
		return persistObjectCount;
	}
	
	/**
	 * Returns the number of persistProperty method calls this persister has received. 
	 */
	public int getPersistPropertyCount() {
		return persistPropertyCount;
	}
	
	/**
	 * Returns the number of persistProperty method calls this persister has received.
	 */
	public int getRemoveObjectCount() {
		return removeObjectCount;
	}
	
	/**
	 * Returns an unmodifiable {@link List} of stored persister calls.  
	 */
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
		savedPersistedObjects.clear();
		savedObjectsToRemove.clear();
		latch = new CountDownLatch(1);
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}
	
	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	public List<PersistedWabitObject> getPersistObjectCalls() {
		return savedPersistedObjects;
	}
	
	public Map<String, String> getRemoveObjectCalls() {
		return savedObjectsToRemove;
	}

}
