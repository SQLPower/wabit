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

import java.beans.PropertyChangeListener;
import java.sql.Connection;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;

public class StubWabitSession implements WabitSession {
	
	private final WabitSessionContext context;
	private WabitProject project;

	public StubWabitSession(WabitSessionContext context) {
		this.context = context;
		project = new WabitProject();
	}

	public void addSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		// TODO Auto-generated method stub

	}

	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	public WabitSessionContext getContext() {
		return context;
	}

	public WabitProject getProject() {
		return project;
	}

	public void removeSessionLifecycleListener(
			SessionLifecycleListener<WabitSession> l) {
		// TODO Auto-generated method stub

	}

	public UserPrompter createUserPrompter(String question,
			UserPromptType responseType, UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			String... buttonNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public int getRowLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		// TODO Auto-generated method stub
		
	}

	public void setRowLimit(int newLimit) {
		// TODO Auto-generated method stub
		
	}

    public boolean isLoading() {
        return false;
    }

    public void setLoading(boolean loading) {
        // no op
    }

    public Connection borrowConnection(SPDataSource dataSource) {
        // TODO Auto-generated method stub
        return null;
    }

    public SQLDatabase getSqlDatabase(SPDataSource dataSource) {
        // TODO Auto-generated method stub
        return null;
    }

}
