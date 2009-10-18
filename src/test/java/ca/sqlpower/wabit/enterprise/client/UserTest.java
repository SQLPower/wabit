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

package ca.sqlpower.wabit.enterprise.client;

import java.util.Set;

import org.springframework.security.GrantedAuthority;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;

public class UserTest extends AbstractWabitObjectTest {

	private User user;
	
	@Override
	public Set<String> getPropertiesToNotPersistOnObjectPersist() {
		Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
		ignored.add("accountNonExpired");
		ignored.add("accountNonLocked");
		ignored.add("credentialsNonExpired");
		ignored.add("enabled");
		ignored.add("authorities");
		ignored.add("grants");
		ignored.add("username");
		return ignored;
	}
	
	@Override
	public Set<String> getPropertiesToIgnoreForEvents() {
		Set<String> ignored = super.getPropertiesToIgnoreForEvents();
		ignored.add("authorities");
		return ignored;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		user = new User("name", "password");
		user.setAuthorities(new GrantedAuthority[0]);

		getWorkspace().setUUID(WabitWorkspace.SYSTEM_WORKSPACE_UUID);
		getWorkspace().addUser(user);
	}
	
	@Override
	public WabitObject getObjectUnderTest() {
		return user;
	}
	
	@Override
	public Class<? extends WabitObject> getParentClass() {
		return WabitWorkspace.class;
	}

}
