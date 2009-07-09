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

import java.io.InputStream;

import junit.framework.TestCase;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.swingui.WabitWelcomeScreen;

/**
 * This test class is for testing problems with Wabit and the demo project.
 */
public class DemoDatabaseTest extends TestCase {

    public void testVersionNumberUpToDate() throws Exception {
        
        WabitSessionContext context = new WabitSessionContextImpl(true, false) {
            @Override
            public UserPrompter createUserPrompter(String question,
                    UserPromptType responseType, UserPromptOptions optionType,
                    UserPromptResponse defaultResponseType,
                    Object defaultResponse, String... buttonNames) {
                fail("Loading the example workspace should not prompt the user, it should just work");
                throw new IllegalStateException();
            }
        };
        
        InputStream in = WabitWelcomeScreen.class.getResourceAsStream("/ca/sqlpower/wabit/example_workspace.wabit");
        OpenWorkspaceXMLDAO workspaceDAO = new OpenWorkspaceXMLDAO(context, in);
        workspaceDAO.openWorkspaces();
    }
    
}
