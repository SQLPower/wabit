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

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.image.WabitImage;

public class CopyImageAction extends AbstractAction {
	private WabitSession session;
	private WabitImage image;
	
	public CopyImageAction(WabitSession session, WabitImage image) {
		super("Copy Image");
		this.session = session;
		this.image = image;
	}
	
	public void actionPerformed(ActionEvent e) {
		WabitImage newImage = new WabitImage(image);
		newImage.setName(image.getName() +  " Copy");
		session.getWorkspace().addImage(newImage);
	}

}
