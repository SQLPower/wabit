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
import javax.jmdns.ServiceInfo;

import ca.sqlpower.sql.DataSourceCollection;

public class StubWabitSessionContext implements WabitSessionContext {

	public void deregisterChildSession(WabitSession child) {
	}

	public DataSourceCollection getDataSources() {
		return null;
	}

	public boolean isMacOSX() {
		return false;
	}

	public void registerChildSession(WabitSession child) {
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
	
    public List<ServiceInfo> getEnterpriseServers() {
        return null;
    }

    public Preferences getPrefs() {
        return null;
    }

	public String getName() {
		return null;
	}

}
