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

import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.wabit.dao.MessageSender;
import ca.sqlpower.wabit.dao.WabitPersistenceException;
import ca.sqlpower.wabit.dao.WabitPersister.DataType;
import ca.sqlpower.wabit.dao.WabitPersister.WabitPersistMethod;

public class WabitJSONPersisterTest extends TestCase {

	private WabitJSONPersister persister;
	
	public void testBegin() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					assertEquals(content.getString("method"), WabitPersistMethod.begin.toString());
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
	}

	public void testCommit() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					String method = content.getString("method");
					if (!method.equals(WabitPersistMethod.begin.toString())) {
						assertEquals(method, WabitPersistMethod.commit.toString());
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		
		try {
			persister.commit();
			fail("Expected WabitPersistenceException to be thrown");
		} catch (WabitPersistenceException e) {
			if (!e.getMessage().equals("Commit attempted while not in a transaction")) {
				throw e;
			}
		}
		
		persister.begin();
		persister.commit();
	}

	public void testPersistObject()  throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					assertEquals(content.getString("method"), WabitPersistMethod.persistObject.toString());
					assertEquals(content.getString("parentUUID"), "parent");
					assertEquals(content.getString("uuid"), "uuid");
					assertEquals(content.getString("type"), "type");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.persistObject("parent", "type", "uuid");
	}

	public void testChangeProperty() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					assertEquals(content.getString("method"), WabitPersistMethod.changeProperty.toString());
					assertEquals(content.getString("propertyName"), "property");
					assertEquals(content.getString("type"), DataType.STRING.name());
					assertEquals(content.getString("oldValue"), "old");
					assertEquals(content.getString("newValue"), "new");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.persistProperty("uuid", "property", DataType.STRING, "old", "new");
	}

	public void testPersistProperty() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					assertEquals(content.getString("method"), WabitPersistMethod.persistProperty.toString());
					assertEquals(content.getString("propertyName"), "property");
					assertEquals(content.getString("type"), DataType.STRING.name());
					assertEquals(content.getString("newValue"), "new");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.persistProperty("uuid", "property", DataType.STRING, "new");
	}

	public void testRemoveObject() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					assertEquals(content.getString("method"), WabitPersistMethod.removeObject.toString());
					assertEquals(content.getString("parentUUID"), "parent");
					assertEquals(content.getString("uuid"), "uuid");
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.removeObject("parent", "uuid");
	}

	public void testRollback() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					String method = content.getString("method");
					if (!method.equals(WabitPersistMethod.begin.toString())) {
						assertEquals(method, WabitPersistMethod.rollback.toString());
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		
		try {
			persister.rollback();
			fail("Expected WabitPersistenceException to be thrown");
		} catch (WabitPersistenceException e) {
			if (!e.getMessage().equals("Rollback attempted while not in a transaction")) {
				throw e;
			}
		}
		
		persister.begin();
		persister.rollback();
	}

}
