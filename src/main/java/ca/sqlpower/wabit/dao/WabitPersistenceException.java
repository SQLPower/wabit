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
 * A general exception for any exceptions that occur during an attempt to
 * persist a {@link WabitObject} and/or its properties by any class that
 * implements {@link WabitPersister}. This exception could be subclassed to
 * provide more detail for certain exceptions, such an exception caused by a
 * WabitObject having a different old property value than expected.
 */
public class WabitPersistenceException extends Exception {

	/**
	 * The UUID of the WabitObject that was being persisted.
	 */
	private String uuid;

	/**
	 * Constructs a {@link WabitPersistenceException} with the given UUID for
	 * the {@link WabitObject} that was being persisted.
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} that was being persisted.
	 *            If there is no particular WabitObject related to this
	 *            Exception, null may be passed in instead.
	 */
	public WabitPersistenceException(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Constructs a {@link WabitPersistenceException} with a given UUID and
	 * detailed message
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} that was being persisted.
	 *            If there is no particular WabitObject related to this
	 *            Exception, null may be passed in instead.
	 * @param message
	 *            A detailed error message. Can be retrieved with
	 *            {@link #getMessage()}.
	 */
	public WabitPersistenceException(String uuid, String message) {
		super(message);
		this.uuid = uuid;
	}

	/**
	 * Constructs a {@link WabitPersistenceException} with a given UUID and a
	 * cause {@link Throwable}. This can be used to wrap a more specific
	 * Exception that caused this WabitPersistenceException
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} that was being persisted.
	 *            If there is no particular WabitObject related to this
	 *            Exception, null may be passed in instead.
	 * @param cause
	 *            A {@link Throwable} that is the specific cause
	 */
	public WabitPersistenceException(String uuid, Throwable cause) {
		super(cause);
		this.uuid = uuid;
	}

	/**
	 * Constructs a {@link WabitPersistenceException} with a given UUID, a
	 * detailed message, and a cause {@link Throwable}. This can be used to wrap
	 * a more specific Exception that caused this WabitPersistenceException
	 * 
	 * @param uuid
	 *            The UUID of the {@link WabitObject} that was being persisted.
	 *            If there is no particular WabitObject related to this
	 *            Exception, null may be passed in instead.
	 * @param message
	 *            A detailed error message. Can be retrieved with
	 *            {@link #getMessage()}.
	 * @param cause
	 *            A {@link Throwable} that is the specific cause
	 */
	public WabitPersistenceException(String uuid, String message, Throwable cause) {
		super(message, cause);
		this.uuid = uuid;
	}

	/**
	 * Returns the UUID of the {@link WabitObject} that was being persisted when
	 * this Exception occured
	 * 
	 * @return The UUID of the {@link WabitObject} that was being persisted
	 */
	public String getUUID() {
		return uuid;
	}
}
