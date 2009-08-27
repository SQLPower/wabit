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
import ca.sqlpower.wabit.report.Template;

public class CopyTemplateAction extends AbstractAction {
	private Template layout;
	private WabitSession session;
	
	public CopyTemplateAction(Template layout, WabitSession session) {
		super("Copy Template");
		this.layout = layout;
		this.session = session;
	}
	
	public void actionPerformed(ActionEvent e) {
		Template layoutCopy = new Template(layout, session);
		layoutCopy.setParent(layout.getParent());
		layoutCopy.setName(layout.getName() + " Copy");
		session.getWorkspace().addTemplate(layoutCopy);
	}
}
