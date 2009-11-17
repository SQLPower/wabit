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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JSpinner;

import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;

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
	 * Returns the JFrame that displays the context.
	 */
	JFrame getFrame();

    /**
     * Sets the panel that allows editing of the current selection in the tree.
     * A new panel will be created based on the type of model the active
     * session's currently editing if the user has no changes or wants to
     * discard the current changes.
     * 
     * @return False if the previous editor was not correctly saved and the old
     *         component needs to be displayed. True otherwise.
     */
    boolean setEditorPanel();

    /**
     * Returns the row limit component that affects all cached result sets.
     * @return
     */
    JSpinner getRowLimitSpinner();
    
    /**
     * Sets the status message at the bottom of the window.
     * @param msg
     */
    public void setStatusMessage (String msg);
    
    public WabitSwingSession getActiveSwingSession();

    /**
     * Returns a {@link SwingWorkerRegistry} for the context to track workers
     * that are involved with loading files. It would be useful if the
     * {@link OpenWorkspaceXMLDAO} created the session(s) to load into before
     * creating the thread to do the loading. Then the loading thread could
     * register with the session and we could remove this implementation.
     */
    SwingWorkerRegistry getLoadingRegistry();
    /**
     * Creates a new server-based session for the given server. The new session
     * will belong to this context.
     */
    WabitSwingSession createServerSession(SPServerInfo serverInfo);

    /**
     * Creates a new local session that belongs to this context.
     */
    WabitSwingSession createSession();
}
