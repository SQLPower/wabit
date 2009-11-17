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

/**
 * This class represents an individual removed {@link WabitObject} in a {@link SPPersister}. 
 */
public class RemovedWabitObject {
	
	private final String parentUUID;
	private final String uuid;
	
	public RemovedWabitObject(String parentUUID, String uuid) {
		this.parentUUID = parentUUID;
		this.uuid = uuid;
	}
	
	public String getParentUUID() {
		return parentUUID;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	@Override
	public String toString() {
		return "RemovedWabitObject: parentUUID " + getParentUUID() + ", uuid " + getUUID() + "\n";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		
		RemovedWabitObject pwo = (RemovedWabitObject) obj;
		
		return getParentUUID().equals(pwo.getParentUUID()) 
				&& getUUID().equals(pwo.getUUID()); 
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		
		result = prime * result + parentUUID.hashCode();
		result = prime * result + uuid.hashCode();
		
		return result;
	}
}
