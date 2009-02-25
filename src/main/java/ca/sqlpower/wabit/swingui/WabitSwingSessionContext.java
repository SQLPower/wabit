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

package ca.sqlpower.wabit.swingui;

import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.wabit.WabitSessionContext;

public interface WabitSwingSessionContext extends WabitSessionContext {

	RecentMenu createRecentMenu();
	
	WabitWelcomeScreen getWelcomeScreen();

	/**
	 * Sets the most recent file to be saved or loaded into the session. The
	 * session tracks recent files for uses like a recent menu and to decide if
	 * Wabit should start with loading a saved file.
	 * 
	 * @param fileName
	 *            The absolute path to the file with the file name.
	 */
	void putRecentFileName(String fileName);
	
	/**
	 * Returns true if Wabit should open on the welcome screen. Returns false
	 * if the most recent project in the recent menu should be loaded and displayed.
	 */
	boolean startOnWelcomeScreen();

}
