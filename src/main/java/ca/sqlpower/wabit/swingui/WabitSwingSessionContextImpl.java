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

package ca.sqlpower.wabit.swingui;

import java.io.File;
import java.io.IOException;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.LoadProjectsAction;

/**
 * This is the swing version of the WabitSessionContext. Swing specific operations for
 * the context will be done in this implementation 
 */
public class WabitSwingSessionContextImpl extends WabitSessionContextImpl implements WabitSwingSessionContext {

	private RecentMenu recentMenu;

	public WabitSwingSessionContextImpl(boolean terminateWhenLastSessionCloses)
			throws IOException, SQLObjectException {
		super(terminateWhenLastSessionCloses);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		recentMenu = new RecentMenu(this.getClass()) {
			
			@Override
			public void loadFile(String fileName) throws IOException {
				File file = new File(fileName);
				LoadProjectsAction.loadFile(file, WabitSwingSessionContextImpl.this);
			}
		};
	}
	
	@Override
	public WabitSession createSession() {
		WabitSwingSession session = new WabitSwingSessionImpl(this);
		return session;
	}
	
	public RecentMenu getRecentMenu() {
		return recentMenu;
	}

}
