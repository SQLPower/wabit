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
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.validation.swingui.StatusComponent;

/**
 * This action will open a browser to the SQL forums.
 */
public class ForumAction extends AbstractAction {
	
	public ForumAction() {
		super("", new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/wabit-24px.png")));
		putValue(SHORT_DESCRIPTION, "Go to Wabit support forum");		
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			BrowserUtil.launch(SPSUtils.FORUM_URL);
		} catch (IOException e1) {
			throw new RuntimeException("Unexpected error in launch", e1); //$NON-NLS-1$
		}
	}

}
