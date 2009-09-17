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

import java.io.File;
import java.net.URI;

import javax.swing.JTree;

import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel;

/**
 * This defines UI components that are needed for each session. These
 * UI pieces can be used to display or modify information in the session
 * and its workspace.
 */
public interface WabitSwingSession extends SwingWorkerRegistry, WabitSession {

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
    
    /**
     * Sets the URI that this session was most recently loaded from or saved to,
     * and resets the unsaved changes flag for this session.
     */
    void setCurrentURI(URI uri);
    
    /**
     * Returns the URI that this session was most recently loaded from or saved to.
     */
    URI getCurrentURI();

    /**
     * Returns the current URI as a File object, if the current URI is not null
     * and it actually represents a file on a locally-accessible filesystem.
     * Otherwise, returns null (for example, if the current URI is an HTTP URI
     * or represents a Java system resource).
     * 
     * @see #getCurrentURI()
     */
    File getCurrentURIAsFile();
    
    /**
     * Reports whether or not any changes have been detected in this workspace
     * since it was opened or last saved.
     * 
     * @see #setCurrentURI(URI)
     */
    boolean hasUnsavedChanges();

    // override narrows return type
    public WabitSwingSessionContext getContext();
    
    /**
     * The runner will be executed on the event dispatch thread. This will block
     * the UI from responding as long as the runner is running. If a process is
     * running in the background and we need to fire events or do other work on
     * the event dispatch thread it can be wrapped in a runnable and passed to here.
     * The background thread will not block and wait for the runner to complete.
     */
    public void runInForeground(Runnable runner);

    /**
     * Starts this runner on a worker thread that is registered with this swing
     * session. This helps enforce large running processes occur on a separate
     * thread than the current one if the current thread is the event dispatch
     * thread.
     */
    public void runInBackground(Runnable runner);
}
