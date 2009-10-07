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
	 * in JSON format. It expects the following key-value pairs:
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
		try {
			JSONObject jsonObject = new JSONObject(message);
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
				parentUUID = jsonObject.getString("parentUUID");
				propertyName = jsonObject.getString("propertyName");
				propertyType = DataType.valueOf(jsonObject.getString("type"));
				newValue = jsonObject.get("newValue");
				Object oldValue = jsonObject.get("oldValue");
				persister.persistProperty(parentUUID, propertyName,
						propertyType, oldValue, newValue);
				break;
			case persistProperty:
				parentUUID = jsonObject.getString("parentUUID");
				propertyName = jsonObject.getString("propertyName");
				propertyType = DataType.valueOf(jsonObject.getString("type"));
				newValue = jsonObject.get("newValue");
				persister.persistProperty(parentUUID, propertyName,
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
		} catch (JSONException e) {
			throw new WabitPersistenceException(uuid, e);
		}
	}

}
