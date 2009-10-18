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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.wabit.dao.MessageDecoder;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.dao.WabitPersister.WabitPersistMethod;

/**
 * An implementation of {@link MessageDecoder} that takes in a String that is
 * intended to be a JSON-formatted message, and constructs a JSONObject from it.
 * It then expects JSONObject key values that map to {@link WabitPersister}
 * methods and their expected parameters. It then extracts this information from
 * the JSONObject and makes the appropriate method calls to a
 * {@link WabitPersister} provided in the constructor.
 */
public class WabitJSONMessageDecoder implements MessageDecoder<String> {

	private static final Logger logger = Logger
			.getLogger(WabitJSONMessageDecoder.class);
	
	/**
	 * A {@link WabitPersister} that the decoder will make method calls on
	 */
	private WabitPersister persister;

	/**
	 * Creates a WabitMessageDecoder with the given {@link WabitPersister}. The
	 * messages that this class decodes will contain WabitPersister method calls
	 * with their parameters. This decoder will use the messages to make method
	 * calls to the given WabitPersister.
	 * 
	 * @param persister
	 *            The {@link WabitPersister} that this decoder will make method
	 *            calls to
	 */
	public WabitJSONMessageDecoder(WabitPersister persister) {
		this.persister = persister;
	}

	/**
	 * Takes in a message in the form of String. The message is expected to be
	 * in JSON format.
	 * 
	 * The JSON message is expected to be a JSONArray of JSONObjects. Each
	 * JSONObject contains details for making a WabitPersister method call.
	 * 
	 * It expects the following key-value pairs in each JSONObject message:
	 * <ul>
	 * <li>method - The String value of a {@link WabitPersistMethod}. This is
	 * used to determine which {@link WabitPersister} method to call.</li>
	 * <li>uuid - The UUID of the WabitObject, if there is one, that the persist
	 * method call will act on. If there is none, it expects
	 * {@link JSONObject#NULL}</li>
	 * </ul>
	 * Other possible key-value pairs (depending on the intended method call)
	 * include:
	 * <ul>
	 * <li>parentUUID</li>
	 * <li>type</li>
	 * <li>newValue</li>
	 * <li>oldValue</li>
	 * <li>propertyName</li>
	 * </ul>
	 * See the method documentation of {@link WabitPersister} for full details
	 * on the expected values
	 */
	public void decode(String message) throws WabitPersistenceException {
		String uuid = null;
		JSONObject jsonObject = null;
		try {
			JSONArray messageArray = new JSONArray(message);
			for (int i=0; i < messageArray.length(); i++) {
				logger.debug("Decoding Message: " + jsonObject);
				jsonObject = messageArray.getJSONObject(i);
				uuid = jsonObject.getString("uuid");
				WabitPersistMethod method = WabitPersistMethod.valueOf(jsonObject.getString("method"));
				String parentUUID;
				String propertyName;
				DataType propertyType;
				Object newValue;
				switch (method) {
				case begin:
					persister.begin();
					break;
				case commit:
					persister.commit();
					break;
				case persistObject:
					parentUUID = jsonObject.getString("parentUUID");
					String type = jsonObject.getString("type");
					int index = jsonObject.getInt("index");
					persister.persistObject(parentUUID, type, uuid, index);
					break;
				case changeProperty:
					propertyName = jsonObject.getString("propertyName");
					propertyType = DataType.valueOf(jsonObject.getString("type"));
					newValue = getWithType(jsonObject, propertyType, "newValue");
					Object oldValue = getWithType(jsonObject, propertyType, "oldValue");
					persister.persistProperty(uuid, propertyName,
							propertyType, oldValue, newValue);
					break;
				case persistProperty:
					propertyName = jsonObject.getString("propertyName");
					propertyType = DataType.valueOf(jsonObject.getString("type"));
					newValue = getWithType(jsonObject, propertyType, "newValue");
					if (newValue == null) logger.debug("newValue was null for propertyName " + propertyName);
					persister.persistProperty(uuid, propertyName,
							propertyType, newValue);
					break;
				case removeObject:
					parentUUID = jsonObject.getString("parentUUID");
					persister.removeObject(parentUUID, uuid);
					break;
				case rollback:
					persister.rollback();
					break;
				default:
					throw new WabitPersistenceException(uuid,
							"Does not support Wabit persistence method " + method);
				}
			}
		} catch (JSONException e) {
			if (jsonObject != null) {
				logger.error("Error decoding JSONObject " + jsonObject);
			}
			throw new WabitPersistenceException(uuid, e);
		}
	}

	private static Object getNullable(JSONObject jo, DataType type, String propName) throws JSONException {
		final Object value = jo.get(propName);
		if (value == JSONObject.NULL) {
			return null;
		} else {
			return value;
		}
	}
	
	private static Object getWithType(JSONObject jo, DataType type, String propName) throws JSONException {
		if (getNullable(jo, type, propName) == null) return null;
		
		switch (type) {
		case BOOLEAN:
			return Boolean.valueOf(jo.getBoolean(propName));
		case DOUBLE:
			return Double.valueOf(jo.getDouble(propName));
		case INTEGER:	
			return Integer.valueOf(jo.getInt(propName));
		case NULL:
		case PNG_IMG:
		case STRING:
		case REFERENCE:
		default:
			return getNullable(jo, type, propName);
		}
	}
}
