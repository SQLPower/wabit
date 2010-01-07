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

package ca.sqlpower.wabit.dao.json;

import org.json.JSONObject;

import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.dao.json.SPJSONPersister;
import ca.sqlpower.wabit.WabitWorkspace;

/**
 * A {@link SPPersister} implementation that serializes
 * {@link SPPersister} method calls as {@link JSONObject}s and transmits them
 * to a destination using a {@link MessageSender}. This allows these method
 * calls to be transmitted to other systems, typically (but not necessarily)
 * over a network connection.
 */
public class WabitJSONPersister extends SPJSONPersister {
	
	/**
	 * Create a {@link WabitJSONPersister} that uses the given
	 * {@link MessageSender} to transmit the JSON content
	 */
	public WabitJSONPersister(MessageSender<JSONObject> messageSender) {
		super(messageSender);
	}
	
	@Override
	public void persistObject(String parentUUID, String type, String uuid,
			int index) throws SPPersistenceException {
		if (! WabitWorkspace.class.getSimpleName().equals(type) && parentUUID == null) {
			throw new NullPointerException("Child is not a WabitWorkspace, but has a null parent ID. Child's ID is " + uuid);
		}
		super.persistObject(parentUUID, type, uuid, index);
	}
	
}
