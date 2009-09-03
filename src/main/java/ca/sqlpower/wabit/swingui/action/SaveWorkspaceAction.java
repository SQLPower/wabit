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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.SaveWorkspaceAsAction.SaveException;

/**
 * This will save the active session in the context to the file where the user
 * last loaded or saved the session. If the user has not yet saved or loaded
 * the sessions it will prompt the user for a file location.
 */
public class SaveWorkspaceAction extends AbstractAction {
    
    private final WabitSwingSessionContext context;

    /**
     * This will save the active session in the context to the file where the
     * user last loaded or saved the session. If the user has not yet saved or
     * loaded the sessions it will prompt the user for a file location.
     * 
     * @param context
     *            The context to use to get the active session from.
     */
    public SaveWorkspaceAction(WabitSwingSessionContext context) {
        super("Save", WabitIcons.SAVE_ICON_16);
        this.context = context;
    }

    public void actionPerformed(ActionEvent e) {
        save(context, context.getActiveSwingSession());
    }

    /**
     * Saves the given workspace to the given file or if the file does not exist
     * the save as method will be called to let the user choose a file to save
     * the workspace to.
     * 
     * @param context
     *            The context that contains the workspace. Used to update the
     *            the recent menu in the context.
     * @param session
     *            The session whose workspace will be saved to the most recent
     *            file it was saved to or loaded from. If the session is new and
     *            has no recent file the user will be prompted for it. If this
     *            is null false will be returned.
     * @return If the save was successful or not
     */
    public static boolean save(WabitSwingSessionContext context, WabitSwingSession session) {
        if (session.getWorkspace() == null) return false;
        
        // TODO this will move into the session impl, and a corresponding interface method will appear on WabitSession
        // see bug 2092 for details
        File targetFile = session.getCurrentURIAsFile();
        if (targetFile == null || (
                session.hasUnsavedChanges() &&
                session.getCurrentURI().toString().contains(
                        WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL))) {
            return SaveWorkspaceAsAction.save(context, session);
        } else {
            if (session.getCurrentURI().toString().contains(
                        WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL)) return true;
            try {
				SaveWorkspaceAsAction.saveSessionToFile(context, session, session.getCurrentURIAsFile());
			} catch (SaveException e) {
                JOptionPane.showMessageDialog(context.getFrame(),
                		e.getMessage(), "Error on Saving",
                		JOptionPane.ERROR_MESSAGE);
                context.setStatusMessage(e.getMessage());
                return false;
			}
            context.setStatusMessage("Saved " + session.getWorkspace().getName() + " to " +
                    targetFile.getName());
            return true;
        }
    }

    /**
     * This method will save all of the sessions in the given context to their
     * recent files. If a session does not have a valid file that it was
     * recently saved to or loaded from the user will be prompted to select a
     * file location.
     * 
     * @param context
     *            The context to save its sessions to files
     * @return True if all of the sessions were saved successfully. False
     *         otherwise.
     */
    public static boolean saveAllSessions(WabitSwingSessionContext context) {
        StringBuffer statusMessage = new StringBuffer("Saved ");
        for (WabitSession session : context.getSessions()) {
            //If the example workspace has unsaved changes so it needs to be saved
            //the user should be prompted above for a file location.
            final WabitSwingSession swingSession = (WabitSwingSession) session;
            if (swingSession.getCurrentURIAsFile() == null ||
            		(swingSession.getCurrentURI() != null &&
            		swingSession.hasUnsavedChanges() &&
        			swingSession.getCurrentURI().toString().contains(
        			WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL))) {
                SaveWorkspaceAsAction.save(context, swingSession);
            } else {
            	if (swingSession.getCurrentURI().toString().contains(
            			WabitSwingSessionContextImpl.EXAMPLE_WORKSPACE_URL)) continue;

            	try {
					SaveWorkspaceAsAction.saveSessionToFile(context, swingSession, swingSession.getCurrentURIAsFile());
				} catch (SaveException e) {
                    JOptionPane.showMessageDialog(context.getFrame(),
                    		e.getMessage(), "Error on Saving",
                    		JOptionPane.ERROR_MESSAGE);
                    context.setStatusMessage(e.getMessage());
                    return false;
				}
            	statusMessage.append(session.getWorkspace().getName() + " to " + 
            			swingSession.getCurrentURIAsFile().getName() + " ");
            }
        }
        context.setStatusMessage(statusMessage.toString());
        return true;
    }

}
