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

import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.wabit.dao.MessagePasser;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister;

/**
 * A {@link WabitPersister} implementation that serializes
 * {@link WabitPersister} method calls as {@link JSONObject}s and transmits them
 * to a destination using a {@link MessagePasser}. This allows these method
 * calls to be transmitted to other systems, typically (but not necessarily)
 * over a network connection.
 */
public class WabitJSONPersister implements WabitPersister {

	/**
	 * A count of transactions, mainly to keep track of nested transactions.
	 */
	private int transactionCount = 0;
	
	/**
	 * A MessagePasser object that is responsible for transmitting the
	 * JSONObject contents.
	 */
	private MessagePasser<JSONObject> messagePasser;

	/**
	 * Create a {@link WabitJSONPersister} that uses the given
	 * {@link MessagePasser} to transmit the JSON content
	 */
	public WabitJSONPersister(MessagePasser<JSONObject> messagePasser) {
		this.messagePasser = messagePasser;
	}
	
	@Override
	public void begin() throws WabitPersistenceException{
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "begin");
			// Need to put this in or anything calling get on the key "uuid"
			// will throw a JSONException
			jsonObject.put("uuid", JSONObject.NULL);
		} catch (JSONException e) {
			throw new WabitPersistenceException(null, e);
		}
		messagePasser.send(jsonObject);
		transactionCount++;
	}

	@Override
	public void commit() throws WabitPersistenceException {
		if (transactionCount <= 0) {
			throw new WabitPersistenceException(null, "Commit attempted while not in a transaction");
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "commit");
			// Need to put this in or anything calling get on the key "uuid"
			// will throw a JSONException
			jsonObject.put("uuid", JSONObject.NULL);
		} catch (JSONException e) {
			throw new WabitPersistenceException(null, e);
		}
		messagePasser.send(jsonObject);
		transactionCount--;
	}

	@Override
	public void persistObject(String parentUUID, String type, String uuid)
			throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "persistObject");
			jsonObject.put("parentUUID", parentUUID);
			jsonObject.put("type", type);
			jsonObject.put("uuid", uuid);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		messagePasser.send(jsonObject);
	}

	@Override
	public void persistProperty(String uuid, String propertyName, DataType type,
			Object oldValue, Object newValue) throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "persistProperty");
			jsonObject.put("propertyName", propertyName);
			jsonObject.put("uuid", uuid);
			jsonObject.put("oldValue", oldValue);
			jsonObject.put("newValue", newValue);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		messagePasser.send(jsonObject);
	}
	
	@Override
	public void persistProperty(String uuid, String propertyName, DataType type, Object newValue) throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "persistProperty");
			jsonObject.put("propertyName", propertyName);
			jsonObject.put("uuid", uuid);
			jsonObject.put("newValue", newValue);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		messagePasser.send(jsonObject);
	};
	
	@Override
	public void removeObject(String parentUUID, String uuid)
			throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "removeObject");
			jsonObject.put("parentUUID", parentUUID);
			jsonObject.put("uuid", uuid);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		messagePasser.send(jsonObject);
	}
	
	@Override
	public void rollback() throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", "rollback");
			// Need to put this in or anything calling get on the key "uuid"
			// will throw a JSONException
			jsonObject.put("uuid", JSONObject.NULL);
		} catch (JSONException e) {
			throw new WabitPersistenceException(null, e);
		}
		messagePasser.send(jsonObject);
	}
}
