/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.util.List;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

public class StubWabitSessionContext implements WabitSessionContext {

	public void deregisterChildSession(WabitSession child) {
	    // no op
	}

	public DataSourceCollection getDataSources() {
		return null;
	}

	public boolean isMacOSX() {
		return false;
	}

	public void registerChildSession(WabitSession child) {
	    // no op
	}

	public WabitSession createSession() {
		return new StubWabitSession(this);
	}

	public int getSessionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public JmDNS getJmDNS() {
	    return null;
	}

    public Preferences getPrefs() {
        return null;
    }

	public String getName() {
		return null;
	}

    public void addServer(WabitServerInfo serverInfo) {
        // TODO Auto-generated method stub
        
    }

    public List<WabitServerInfo> getEnterpriseServers(
            boolean includeDiscoveredServers) {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeServer(WabitServerInfo si) {
        // TODO Auto-generated method stub
        
    }

    public UserPrompter createUserPrompter(String question,
            UserPromptType responseType, UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse,
            String... buttonNames) {
        return null;
    }

}
