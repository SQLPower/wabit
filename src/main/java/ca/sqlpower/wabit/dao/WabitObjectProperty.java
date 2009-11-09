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
import ca.sqlpower.wabit.dao.WabitPersister.DataType;

/**
 * A class representing an individual persisted {@link WabitObject}
 * property.
 */
public class WabitObjectProperty {

	private final String uuid;
	private final String propertyName;
	private final DataType dataType;
	private final Object oldValue;
	private final Object newValue;
	private final boolean unconditional;

	/**
	 * Constructor to persist a {@link WabitObject} property, keeping track
	 * of all the parameters of the persistProperty(...) method call. These
	 * fields will be necessary for when commit() is called.
	 * 
	 * @param propertyName
	 *            The name of the property to persist
	 * @param newValue
	 *            The property value to persist
	 * @param unconditional
	 *            Whether or not to validate if oldValue matches the actual
	 *            property value before persisting
	 */
	public WabitObjectProperty(String uuid, String propertyName,
			DataType dataType, Object oldValue, Object newValue,
			boolean unconditional) {
		this.uuid = uuid;
		this.propertyName = propertyName;
		this.newValue = newValue;
		this.oldValue = oldValue;
		this.unconditional = unconditional;
		this.dataType = dataType;
	}

	/**
	 * Accessor for the property name field
	 * 
	 * @return The property name to persist upon
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Accessor for the property value to persist
	 * 
	 * @return The property value to persist
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * Accessor for the unconditional persist determinant
	 * 
	 * @return The unconditional field
	 */
	public boolean isUnconditional() {
		return unconditional;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public String getUUID() {
		return uuid;
	}

	@Override
	public String toString() {
		return "WabitObjectProperty [uuid=" + uuid + ", propertyName="
				+ propertyName + ", dataType=" + dataType + ", oldValue="
				+ oldValue + ", newValue=" + newValue + ", unconditional="
				+ unconditional + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		
		WabitObjectProperty wop = (WabitObjectProperty) obj;
		
		if (!isUnconditional() && !wop.isUnconditional() 
				&& !getOldValue().equals(wop.getOldValue())) {
			return false;
		}
		
		return isUnconditional() == wop.isUnconditional()
				&& getUUID().equals(wop.getUUID()) 
				&& getPropertyName().equals(wop.getPropertyName()) 
				&& getDataType().equals(wop.getDataType()) 
				&& getNewValue().equals(wop.getNewValue());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		
		result = prime * result + (unconditional ? 1 : 0);
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
		result = prime * result + uuid.hashCode();
		result = prime * result + propertyName.hashCode();
		result = prime * result + dataType.hashCode();
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		
		return result;
	}
	
}
