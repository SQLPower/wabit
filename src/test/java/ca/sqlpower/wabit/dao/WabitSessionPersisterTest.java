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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import ca.sqlpower.dao.SPPersister;
import ca.sqlpower.dao.SPPersister.DataType;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;

public class WabitSessionPersisterTest extends TestCase {

	private WabitSessionPersister wsp;

    private StubWabitSession session;

	public void setUp() {
		
		final PlDotIni defaultPlIni = new PlDotIni();
		
		try {
		
			defaultPlIni.read(new File("ca/sqlpower/sql/default_database_types.ini"));
			
			defaultPlIni.read(new File("ca/sqlpower/demodata/example_database.ini"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WabitSessionContext context = new StubWabitSessionContext() {
			@Override
			public UserPrompter createUserPrompter(String question,
					UserPromptType responseType, UserPromptOptions optionType,
					UserPromptResponse defaultResponseType,
					Object defaultResponse, String... buttonNames) {
				fail("Loading the example workspace should not prompt the user, it should just work."
						+ " Prompt was: " + question);
				throw new IllegalStateException();
			}

			@Override
			public DataSourceCollection<SPDataSource> getDataSources() {
				return defaultPlIni;
			}
		};

		session = new StubWabitSession(context);
        wsp = new WabitSessionPersister("testing persister", session, true);

	}
	
	public SPPersister getTestedPersister() {
		return this.wsp;
	}
	
	public WabitObject getRootObject() {
		return this.session.getWorkspace();
	}

	public void testForwardReferenceFollowedByNestedTransaction() throws Exception {
		wsp.begin();
		wsp.begin();
		wsp.persistObject("w6bbe2735-dfb4-496a-b1b5-32006e6ea3bd", "WabitDataSource", "w96184b5a-b847-4d44-9bc1-961a83d63dd0", 0);
		wsp.begin();
		wsp.commit();
	}
	
	public void testChangePropNullToNonNull() throws Exception {
	    WabitWorkspace workspace = session.getWorkspace();
	    workspace.setUUID(WabitWorkspace.SYSTEM_WORKSPACE_UUID);
        User user = new User("name", "pass");
	    workspace.addUser(user);
	    user.setEmail(null);
	    
	    assertNotNull(SQLPowerUtils.findByUuid(workspace, user.getUUID(), SPObject.class));
	    
        wsp.begin();
        wsp.persistProperty(
                user.getUUID(), "email", DataType.STRING, null, "new@email.com");
        wsp.commit();
        
        assertEquals("new@email.com", user.getEmail());
    }
	
    public void testChangePropNonNullToNull() throws Exception {
        WabitWorkspace workspace = session.getWorkspace();
        workspace.setUUID(WabitWorkspace.SYSTEM_WORKSPACE_UUID);
        User user = new User("name", "pass");
        workspace.addUser(user);
        user.setEmail("not@null");

        assertNotNull(SQLPowerUtils.findByUuid(workspace, user.getUUID(), SPObject.class));

        wsp.begin();
        wsp.persistProperty(
                user.getUUID(), "email", DataType.STRING, "not@null", null);
        wsp.commit();
        
        assertNull(user.getEmail());
    }

    public void testChangePropNullToNull() throws Exception {
        WabitWorkspace workspace = session.getWorkspace();
        workspace.setUUID(WabitWorkspace.SYSTEM_WORKSPACE_UUID);
        User user = new User("name", "pass");
        workspace.addUser(user);
        user.setEmail(null);

        assertNotNull(SQLPowerUtils.findByUuid(workspace, user.getUUID(), SPObject.class));

        wsp.begin();
        wsp.persistProperty(
                user.getUUID(), "email", DataType.STRING, null, null);
        wsp.commit();
        
        assertNull(user.getEmail());
    }
    
    /**
     * This test will start a transaction on an object, change a property multiple times,
     * and then commit the transaction. The end result should change the property on the
     * opposite side of the persister to the correct value on the object to start.
     */
    public void testMultiplePropertyChangesInOneTx() throws Exception {
    	WabitImage image = new WabitImage();
    	session.getWorkspace().addImage(image);
    	image.setName("name");

    	wsp.begin();
    	wsp.persistProperty(image.getUUID(), "name", DataType.STRING, image.getName(), "name1");
    	wsp.persistProperty(image.getUUID(), "name", DataType.STRING, "name1", "name2");
    	wsp.persistProperty(image.getUUID(), "name", DataType.STRING, "name2", "name3");
    	wsp.commit();
    	
    	assertEquals("name3", image.getName());
    }

}
