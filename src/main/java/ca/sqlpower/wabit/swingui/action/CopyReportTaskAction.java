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

import java.awt.Window;

import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.enterprise.client.ReportTask;

public class CopyReportTaskAction extends CopyAction {
	private ReportTask task;
	private WabitSession session;
	
	public CopyReportTaskAction(ReportTask task, WabitSession session, Window dialogOwner) {
		super(task, dialogOwner);
		this.task = task;
		this.session = session;
	}
	
	public void copy(String name) {
		ReportTask taskCopy = new ReportTask(task);
		taskCopy.setParent(task.getParent());
		taskCopy.setName(name);
		taskCopy.setNoob(true);
		session.getWorkspace().addReportTask(taskCopy);
	}
}
