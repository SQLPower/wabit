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
import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * This action creates a new WabitImage and adds it to the workspace
 */
public class NewImageAction extends AbstractAction {
    
	private static final Icon NEW_IMAGE_ICON = new ImageIcon(WabitSwingSessionImpl.class.getClassLoader().getResource("icons/image-16.png"));
	
    private final WabitSwingSession session;
    
    public NewImageAction(WabitSwingSession session) {
        super("New Image", NEW_IMAGE_ICON);
        this.session = session;
    }

    public void actionPerformed(ActionEvent e) {
        final WabitImage image = new WabitImage();
        image.setName("New Image");
        session.getWorkspace().addImage(image);
    }

}
