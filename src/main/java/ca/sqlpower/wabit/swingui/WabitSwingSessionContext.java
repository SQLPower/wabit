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

import java.awt.Component;

import javax.swing.JMenu;

import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.wabit.WabitSessionContext;

public interface WabitSwingSessionContext extends WabitSessionContext {

    /**
     * Creates a menu that keeps track of the last few opened and saved workspace
     * files.
     */
	RecentMenu createRecentMenu();

    /**
     * Creates a menu whose items change dynamically as enterprise servers
     * appear and disappear on the network.
     * 
     * @param dialogOwner
     *            The component that should own any dialogs that pop up as the
     *            result of executing menu actions under the server list menu.
     */
    JMenu createServerListMenu(Component dialogOwner, String name, ServerListMenuItemFactory itemFactory);

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
	 * if the most recent workspace in the recent menu should be loaded and displayed.
	 */
	boolean startOnWelcomeScreen();

}
