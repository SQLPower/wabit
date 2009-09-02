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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

/**
 * This method will close the active workspace in the given context. This will
 * also prompt to save the closing workspace if changes exist.
 */
public class CloseWorkspaceAction extends AbstractAction {

    private final WabitSwingSessionContext context;

    public CloseWorkspaceAction(WabitSwingSessionContext context) {
        super("Close Workspace");
        this.context = context;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (context.getActiveSession() == null) return;
        checkUnsavedChanges(context);
        closeActiveWorkspace(context);
    }

    public static void checkUnsavedChanges(WabitSwingSessionContext context) {
    	if (context.getActiveSwingSession().hasUnsavedChanges()) {
            int response = JOptionPane.showOptionDialog(context.getFrame(),
                    "You have unsaved changes. Do you want to save?", "Unsaved Changes", //$NON-NLS-1$ //$NON-NLS-2$
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] {"Don't Save", "Cancel", "Save"}, "Save"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (response == 0) {
                //we are closing
            } else if (response == JOptionPane.CLOSED_OPTION || response == 1) {
                context.setEditorPanel();
                return;
            } else {
                boolean isClosing = true;
                if (!SaveWorkspaceAction.save(context, context.getActiveSwingSession())) {
                    isClosing = false;
                }
                if (!isClosing) return;
            }
        }
    }
    
    public static void closeActiveWorkspace(WabitSwingSessionContext context) {
        context.deregisterChildSession(context.getActiveSession());
        context.getActiveSession().close();
        context.setActiveSession(null);
    }
}
