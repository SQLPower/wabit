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

import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.enterprise.GrantPanel;


public class SystemLevelSecurityAction extends AbstractAction {

	private final String objectUuid;
	private final String objectType;
	private final String label;
	private final WabitWorkspace workspace;
	private final WabitWorkspace systemWorkspace;
	
	public SystemLevelSecurityAction(WabitWorkspace workspace, WabitWorkspace systemWorkspace, String objectUuid, String objectType, String label) {
		super("Manage ".concat(label).concat(" permissions"), WabitIcons.SECURITY_ICON_16);
		this.workspace = workspace;
		this.systemWorkspace = systemWorkspace;
		this.objectUuid = objectUuid;
		this.objectType = objectType;
		this.label = label;
	}
	
	public void actionPerformed(ActionEvent e) {
		GrantPanel panel = new GrantPanel(this.workspace, this.systemWorkspace, objectType, objectUuid, label);
		DataEntryPanelBuilder.createDataEntryPanelDialog(
				panel,
				((WabitSwingSessionContext)workspace.getSession().getContext()).getFrame(), 
				"Server permissions for "+label, 
				"Apply changes").setVisible(true);
	}
}
