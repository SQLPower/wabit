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

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.olap4j.metadata.Datatype;

import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.wabit.dao.MessageDecoder;
import ca.sqlpower.wabit.dao.WabitPersister;
import ca.sqlpower.wabit.dao.WabitPersister.WabitPersistMethod;

public class WabitJSONMessageDecoderTest extends TestCase {

	public void testDecodeBegin() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call begin() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call begin() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call begin() but instead called persistProperty()");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call begin() but instead called persistProperty()");
				
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call begin() but instead called persistObject()");
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call begin() but instead called commit()");				
			}
			
			public void begin() throws SPPersistenceException {
				// We expect this method to get called.
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.begin);
		json.put("uuid", JSONObject.NULL);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
	
	public void testDecodeCommit() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call commit() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call commit() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call commit() but instead called persistProperty()");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call commit() but instead called persistProperty()");
				
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call commit() but instead called persistObject()");
			}
			
			public void commit() throws SPPersistenceException {
				// We expect this method to get called.			
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call commit() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.commit);
		json.put("uuid", JSONObject.NULL);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}

	public void testDecodePersistObject() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call persistObject() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call persistObject() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call persistObject() but instead called persistProperty()");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call persistObject() but instead called persistProperty()");
				
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				// We expect this method to get called.			
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call persistObject() but instead called commit()");
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call persistObject() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.persistObject);
		json.put("uuid", "uuid");
		json.put("parentUUID", "parent");
		json.put("type", "type");
		json.put("index", 0);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
	
	public void testDecodeChangeProperty() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call persistProperty() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() with oldValue but instead called persistProperty() without oldValue");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				// We expect this method to get called.			
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called persistObject()");
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called commit()");
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.changeProperty);
		json.put("uuid", "uuid");
		json.put("type", Datatype.BOOLEAN);
		json.put("propertyName", "property");
		json.put("newValue", true);
		json.put("oldValue", false);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
	
	public void testDecodePersistProperty() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call persistProperty() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				// We expect this method to get called.			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() without oldValue but instead called persistProperty() with oldValue");
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called persistObject()");
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called commit()");
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call persistProperty() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.persistProperty);
		json.put("uuid", "uuid");
		json.put("type", Datatype.BOOLEAN);
		json.put("propertyName", "property");
		json.put("newValue", true);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
	
	public void testDecodeRemoveObject() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				fail("Expected to call removeObject() but instead called rollback()");
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				// We expect this method to get called.			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call removeObject() but instead called persistProperty()");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call removeObject() but instead called persistProperty()");
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call removeObject() but instead called removeObject()");			
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call removeObject() but instead called commit()");
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call removeObject() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.removeObject);
		json.put("uuid", "uuid");
		json.put("parentUUID", "parent");
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
	
	public void testDecodeRollback() throws Exception {
		WabitPersister dummyPersister = new WabitPersister() {
			public void rollback() {
				// We expect this method to get called.
			}
			
			public void removeObject(String parentUUID, String uuid)
					throws SPPersistenceException {
				fail("Expected to call rollback() but instead called removeObject()");			
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call rollback() but instead called persistProperty()");
			}
			
			public void persistProperty(String uuid, String propertyName,
					DataType propertyType, Object oldValue, Object newValue)
					throws SPPersistenceException {
				fail("Expected to call rollback() but instead called persistProperty()");
				
			}
			
			public void persistObject(String parentUUID, String type, String uuid, int index)
					throws SPPersistenceException {
				fail("Expected to call rollback() but instead called persistObject()");
			}
			
			public void commit() throws SPPersistenceException {
				fail("Expected to call rollback() but instead called commit()");				
			}
			
			public void begin() throws SPPersistenceException {
				fail("Expected to call rollback() but instead called begin()");
			}
		};

		JSONObject json = new JSONObject();
		json.put("method", WabitPersistMethod.rollback);
		json.put("uuid", JSONObject.NULL);
		JSONArray messages = new JSONArray();
		messages.put(json);
		
		MessageDecoder<String> decoder = new WabitJSONMessageDecoder(dummyPersister);
		decoder.decode(messages.toString());
	}
}
