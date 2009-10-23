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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.MessageSender;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister;

/**
 * A {@link WabitPersister} implementation that serializes
 * {@link WabitPersister} method calls as {@link JSONObject}s and transmits them
 * to a destination using a {@link MessageSender}. This allows these method
 * calls to be transmitted to other systems, typically (but not necessarily)
 * over a network connection.
 */
public class WabitJSONPersister implements WabitPersister {

	private static final Logger logger = Logger
			.getLogger(WabitJSONPersister.class);
	
	/**
	 * A count of transactions, mainly to keep track of nested transactions.
	 */
	private int transactionCount = 0;
	
	/**
	 * A MessagePasser object that is responsible for transmitting the
	 * JSONObject contents.
	 */
	private final MessageSender<JSONObject> messageSender;

	private final List<JSONObject> messageBuffer;
	
	/**
	 * Create a {@link WabitJSONPersister} that uses the given
	 * {@link MessageSender} to transmit the JSON content
	 */
	public WabitJSONPersister(MessageSender<JSONObject> messageSender) {
		this.messageSender = messageSender;
		this.messageBuffer = new ArrayList<JSONObject>();
	}
	
	public void begin() throws WabitPersistenceException{
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.begin);
			// Need to put this in or anything calling get on the key "uuid"
			// will throw a JSONException
			jsonObject.put("uuid", JSONObject.NULL);
		} catch (JSONException e) {
			throw new WabitPersistenceException(null, e);
		}
		logger.debug(jsonObject);
		messageBuffer.add(jsonObject);
		transactionCount++;
	}

	public void commit() throws WabitPersistenceException {
		if (transactionCount <= 0) {
			throw new WabitPersistenceException(null, "Commit attempted while not in a transaction");
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.commit);
			// Need to put this in or anything calling get on the key "uuid"
			// will throw a JSONException
			jsonObject.put("uuid", JSONObject.NULL);
		} catch (JSONException e) {
			throw new WabitPersistenceException(null, e);
		}
		logger.debug(jsonObject);
		messageBuffer.add(jsonObject);
		transactionCount--;
		if (transactionCount == 0) {
			for (JSONObject obj: messageBuffer) {
				messageSender.send(obj);
			}
			messageSender.flush();
		}
	}

	public void persistObject(String parentUUID, String type, String uuid, int index)
			throws WabitPersistenceException {
		if (! WabitWorkspace.class.getSimpleName().equals(type) && parentUUID == null) {
			throw new NullPointerException("Child is not a WabitWorkspace, but has a null parent ID. Child's ID is " + uuid);
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.persistObject.toString());
			jsonObject.put("parentUUID", parentUUID);
			jsonObject.put("type", type);
			jsonObject.put("uuid", uuid);
			jsonObject.put("index", index);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		logger.debug(jsonObject);
		if (transactionCount == 0) {
			messageSender.send(jsonObject);
			messageSender.flush();
		} else {
			messageBuffer.add(jsonObject);
		}
	}

	public void persistProperty(String uuid, String propertyName, DataType type,
			Object oldValue, Object newValue) throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.changeProperty);
			jsonObject.put("uuid", uuid);
			jsonObject.put("propertyName", propertyName);
			jsonObject.put("type", type.toString());
			jsonObject.put("oldValue", oldValue == null ? JSONObject.NULL : oldValue);
			jsonObject.put("newValue", newValue == null ? JSONObject.NULL : newValue);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		logger.debug(jsonObject);
		if (transactionCount == 0) {
			messageSender.send(jsonObject);
			messageSender.flush();
		} else {
			messageBuffer.add(jsonObject);
		}
	}
	
	public void persistProperty(String uuid, String propertyName, DataType type, Object newValue) throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.persistProperty);
			jsonObject.put("uuid", uuid);
			jsonObject.put("propertyName", propertyName);
			jsonObject.put("type", type.toString());
			if (type == DataType.PNG_IMG && newValue != null) {
				ByteArrayInputStream in = (ByteArrayInputStream) newValue;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				SQLPowerUtils.copyStream(in, out);
				byte[] bytes = out.toByteArray();
				byte[] base64Bytes = Base64.encodeBase64(bytes);
				jsonObject.put("newValue", base64Bytes);
			} else {
				jsonObject.put("newValue", newValue == null ? JSONObject.NULL : newValue);
			}
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		} catch (IOException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		logger.debug(jsonObject);
		if (transactionCount == 0) {
			messageSender.send(jsonObject);
			messageSender.flush();
		} else {
			messageBuffer.add(jsonObject);
		}
	};
	
	public void removeObject(String parentUUID, String uuid)
			throws WabitPersistenceException {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", WabitPersistMethod.removeObject);
			jsonObject.put("parentUUID", parentUUID);
			jsonObject.put("uuid", uuid);
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
		logger.debug(jsonObject);
		if (transactionCount == 0) {
			messageSender.send(jsonObject);
			messageSender.flush();
		} else {
			messageBuffer.add(jsonObject);
		}
	}
	
	public void rollback() {
		// Since this persister does not send messages in a transaction until
		// commit gets called, rollback doesn't need to send anything. It just
		// clears the messageBuffer.
		if (transactionCount <= 0) {
			throw new RuntimeException("Rollback attempted while not in a transaction");
		}
		messageBuffer.clear();
	}
	
	public MessageSender<JSONObject> getMessageSender() {
		return messageSender;
	}
}
