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

			public void flush() throws WabitPersistenceException {
				// no-op
			}
			
			public void clear() {
				// no op
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
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
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
					if (content.getString("method").equals(WabitPersistMethod.persistObject.toString())) {						
						assertEquals(content.getString("parentUUID"), "parent");
						assertEquals(content.getString("uuid"), "uuid");
						assertEquals(content.getString("type"), "type");
						assertEquals(content.getInt("index"), 0);
					} else if (!content.getString("method").equals(WabitPersistMethod.commit.toString()) && !content.getString("method").equals(WabitPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.persistObject("parent", "type", "uuid", 0);
		persister.commit();
	}

	public void testChangeProperty() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					if (content.getString("method").equals(WabitPersistMethod.changeProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.STRING.name());
						assertEquals(content.getString("oldValue"), "old");
						assertEquals(content.getString("newValue"), "new");
					} else if (!content.getString("method").equals(WabitPersistMethod.commit.toString()) && !content.getString("method").equals(WabitPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.persistProperty("uuid", "property", DataType.STRING, "old", "new");
		persister.commit();
	}

	public void testPersistProperty() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					if (content.getString("method").equals(WabitPersistMethod.persistProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.STRING.name());
						assertEquals(content.getString("newValue"), "new");
					} else if (!content.getString("method").equals(WabitPersistMethod.commit.toString()) && !content.getString("method").equals(WabitPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.persistProperty("uuid", "property", DataType.STRING, "new");
		persister.commit();
	}

	public void testRemoveObject() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws WabitPersistenceException {
				try {
					if (content.getString("method").equals(WabitPersistMethod.removeObject.toString())) {
						assertEquals(content.getString("parentUUID"), "parent");
						assertEquals(content.getString("uuid"), "uuid");
					} else if (!content.getString("method").equals(WabitPersistMethod.commit.toString()) && !content.getString("method").equals(WabitPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.removeObject("parent", "uuid");
		persister.commit();
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
			
			public void flush() throws WabitPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.rollback();
		
		try {
			persister.commit();
			fail("Expected WabitPersistenceException to be thrown");
		} catch (WabitPersistenceException e) {
			if (!e.getMessage().equals("Commit attempted while not in a transaction")) {
				throw e;
			}
		}
	}

	
	private class TestingMessageSender implements MessageSender<JSONObject> {
		private boolean commitCalled = false;
		
		public void send(JSONObject content) throws WabitPersistenceException {
			try {
				String method = content.getString("method");
				if (!method.equals(WabitPersistMethod.commit.toString())) {
					assertEquals("WabitJSONPersister sent calls to the message sender in a transaction before commit got called!", true, commitCalled);
				} else {
					commitCalled = true;
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		
		public void flush() throws WabitPersistenceException {
			// no-op
		}
		public void clear() {
			// no op
		}
	}

	public void testTransactionOnlyCommitSendsMessages() throws Exception {
		TestingMessageSender messageSender = new TestingMessageSender();
		persister = new WabitJSONPersister(messageSender);
		
		persister.begin();
		persister.persistObject("parentUUID", "type", "uuid", 0);
		persister.persistProperty("uuid", "propertyName", DataType.STRING, "old");
		persister.persistProperty("uuid", "propertyName", DataType.STRING, "old", "new");
		persister.removeObject("parentUUID", "uuid");
		messageSender.commitCalled = true;
		persister.commit();
	}
}
