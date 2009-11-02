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

package ca.sqlpower.wabit;

import junit.framework.TestCase;

public class SystemPropertiesVariableContextTest extends TestCase {

	private VariableContext varContext;
	
	@Override
	protected void setUp() throws Exception {
		varContext = new SystemPropertiesVariableContext();
	}
	
	public void testExistingProperty() throws Exception {
		String userHome = System.getProperty("user.home");
		assertNotNull("This test assumes user.home is defined, but apparently it is not!", userHome);
		assertEquals(userHome, varContext.getVariableValue("user.home", null));
	}
	
	public void testNonExistingProperty() throws Exception {
		Object defaultVal = new Object();
		assertSame(defaultVal, varContext.getVariableValue("ca.sqlpower.wabit.NON_EXISTANT", defaultVal));
	}
}
