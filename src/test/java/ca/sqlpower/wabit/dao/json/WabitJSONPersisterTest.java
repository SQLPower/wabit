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

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.dao.SPPersister.SPPersistMethod;

public class WabitJSONPersisterTest extends TestCase {

	private WabitJSONPersister persister;
	
	public void testBegin() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					assertEquals(content.getString("method"), SPPersistMethod.begin.toString());
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}

			public void flush() throws SPPersistenceException {
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
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					String method = content.getString("method");
					if (!method.equals(SPPersistMethod.begin.toString())) {
						assertEquals(method, SPPersistMethod.commit.toString());
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		
		try {
			persister.commit();
			fail("Expected SPPersistenceException to be thrown");
		} catch (SPPersistenceException e) {
			if (!e.getMessage().equals("Commit attempted while not in a transaction")) {
				throw e;
			}
		}
		
		persister.begin();
		persister.commit();
	}

	public void testPersistObject()  throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.persistObject.toString())) {						
						assertEquals(content.getString("parentUUID"), "parent");
						assertEquals(content.getString("uuid"), "uuid");
						assertEquals(content.getString("type"), "type");
						assertEquals(content.getInt("index"), 0);
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
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
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.changeProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.STRING.name());
						assertEquals(content.getString("oldValue"), "old");
						assertEquals(content.getString("newValue"), "new");
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
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

	public void testPersistImagePropertyConditional() throws Exception {
		final String binaryDataBase64 = "AQIDBAUGBwgJCg==";
		final byte[] binaryData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.changeProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.PNG_IMG.name());
						assertSame(content.get("oldValue"), JSONObject.NULL);
						assertEquals(content.getString("newValue"), binaryDataBase64);
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail("Unexpected method \""+content.getString("method")+"\"");
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.persistProperty("uuid", "property", DataType.PNG_IMG, null, new ByteArrayInputStream(binaryData));
		persister.commit();
	}
	
	public void testPersistImagePropertyUnconditional() throws Exception {
		final String binaryDataBase64 = "AQIDBAUGBwgJCg==";
		final byte[] binaryData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.persistProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.PNG_IMG.name());
						assertEquals(content.getString("newValue"), binaryDataBase64);
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail("Unexpected method \""+content.getString("method")+"\"");
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
				// no-op
			}
			public void clear() {
				// no op
			}
		};
		
		persister = new WabitJSONPersister(messagePasser);
		persister.begin();
		persister.persistProperty("uuid", "property", DataType.PNG_IMG, new ByteArrayInputStream(binaryData));
		persister.commit();
	}
	
	public void testPersistProperty() throws Exception {
		MessageSender<JSONObject> messagePasser = new MessageSender<JSONObject>() {
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.persistProperty.toString())) {
						assertEquals(content.getString("propertyName"), "property");
						assertEquals(content.getString("type"), DataType.STRING.name());
						assertEquals(content.getString("newValue"), "new");
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
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
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					if (content.getString("method").equals(SPPersistMethod.removeObject.toString())) {
						assertEquals(content.getString("parentUUID"), "parent");
						assertEquals(content.getString("uuid"), "uuid");
					} else if (!content.getString("method").equals(SPPersistMethod.commit.toString()) && !content.getString("method").equals(SPPersistMethod.begin.toString())) {
						fail();
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
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
			public void send(JSONObject content) throws SPPersistenceException {
				try {
					String method = content.getString("method");
					if (!method.equals(SPPersistMethod.begin.toString())) {
						assertEquals(method, SPPersistMethod.rollback.toString());
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void flush() throws SPPersistenceException {
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
			fail("Expected SPPersistenceException to be thrown");
		} catch (SPPersistenceException e) {
			if (!e.getMessage().equals("Commit attempted while not in a transaction")) {
				throw e;
			}
		}
	}

	
	private class TestingMessageSender implements MessageSender<JSONObject> {
		private boolean commitCalled = false;
		
		public void send(JSONObject content) throws SPPersistenceException {
			try {
				String method = content.getString("method");
				if (!method.equals(SPPersistMethod.commit.toString())) {
					assertEquals("WabitJSONPersister sent calls to the message sender in a transaction before commit got called!", true, commitCalled);
				} else {
					commitCalled = true;
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		
		public void flush() throws SPPersistenceException {
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
