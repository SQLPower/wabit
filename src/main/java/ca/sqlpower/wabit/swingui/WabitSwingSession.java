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

import javax.swing.JTree;

import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel;

/**
 * This defines UI components that are needed for each session. These
 * UI pieces can be used to display or modify information in the session
 * and its workspace.
 */
public interface WabitSwingSession extends WabitSession {

    /**
     * Returns the JTree that describes the given session.
     */
    JTree getTree();

    /**
     * Returns the tree model that is used to define the JTree in
     * {@link #getTree()}.
     */
    WorkspaceTreeModel getWorkspaceTreeModel();
    
    /**
     * Returns a connection manager that will allow users to alter
     * the connections in this session.
     */
    DatabaseConnectionManager getDbConnectionManager();
    
}
