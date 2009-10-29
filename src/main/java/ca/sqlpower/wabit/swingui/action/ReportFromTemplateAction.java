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

import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * Creates a new report on a template.
 */
public class ReportFromTemplateAction extends AbstractAction {
	private final WabitSwingSession session;
	private final Template template;

    public ReportFromTemplateAction(WabitSwingSession session, Template template) {
        super("New Report on " + template.getName(), CreateLayoutFromQueryAction.ADD_LAYOUT_ICON);
		this.session = session;
		this.template = template;
    }

    public void actionPerformed(ActionEvent e) {
		Report newReport = new Report(template, session);
		newReport.setName(template.getName() + " Report");
		final WabitWorkspace workspace = session.getWorkspace();
		synchronized (workspace) {
			workspace.begin(null);
			workspace.addReport(newReport);
			workspace.commit();
		}
    }
}
