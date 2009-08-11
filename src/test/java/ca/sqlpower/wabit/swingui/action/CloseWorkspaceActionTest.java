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

package ca.sqlpower.wabit.swingui.action;

import junit.framework.TestCase;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;

public class CloseWorkspaceActionTest extends TestCase {

    public void testClose() throws Exception {
        WabitSwingSessionContext context = new WabitSwingSessionContextImpl(
                new WabitSessionContextImpl(false, false), true);
        WabitSession session = context.createSession();
        context.registerChildSession(session);
        context.setActiveSession(session);
        
        CloseWorkspaceAction.closeActiveWorkspace(context);
        
        assertEquals(null, context.getActiveSession());
        assertTrue(context.getSessions().isEmpty());
    }
}
