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
 * A class representing an individual persisted {@link WabitObject}.
 */
public class PersistedWabitObject {
	private final String parentUUID;
	private final String type;
	private final String uuid;
	private final int index;

	/**
	 * XXX If set to true this object has been loaded and does not need to
	 * be loaded again. It would be better if this was removed from the
	 * persisted object list but we will have to clean this up later.
	 */
	private boolean loaded = false;

	/**
	 * Constructor to persist a {@link WabitObject}.
	 * 
	 * @param parentUUID
	 *            The parent UUID of the {@link WabitObject} to persist
	 * @param type
	 *            The {@link WabitObject} class name
	 * @param uuid
	 *            The UUID of the {@link WabitObject} to persist
	 */
	public PersistedWabitObject(String parentUUID, String type,
			String uuid, int index) {
		this.parentUUID = parentUUID;
		this.type = type;
		this.uuid = uuid;
		this.index = index;
	}

	/**
	 * Accessor for the parent UUID field
	 * 
	 * @return The parent UUID of the object to persist
	 */
	public String getParentUUID() {
		return parentUUID;
	}

	/**
	 * Accessor for the {@link WabitObject} class name
	 * 
	 * @return The {@link WabitObject} class name
	 */
	public String getType() {
		return type;
	}

	/**
	 * Accessor for the UUID field
	 * 
	 * @return The UUID of the object to persist
	 */
	public String getUUID() {
		return uuid;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "PersistedWabitObject: uuid " + uuid + ", parent uuid "
				+ parentUUID + ", type " + type + ", index " + index + "\n";
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean isLoaded() {
		return loaded;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		
		PersistedWabitObject pwo = (PersistedWabitObject) obj;
		
		return getParentUUID().equals(pwo.getParentUUID()) 
				&& getType().equals(pwo.getType()) && getUUID().equals(pwo.getUUID()) 
				&& getIndex() == pwo.getIndex() && isLoaded() == pwo.isLoaded();
		
	}

}
