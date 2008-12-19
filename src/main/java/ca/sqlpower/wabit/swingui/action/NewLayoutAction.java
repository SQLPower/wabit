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

import javax.swing.AbstractAction;

import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.report.Layout;

/**
 * An action that adds a new empty layout to a particular project every time it
 * is invoked.
 */
public class NewLayoutAction extends AbstractAction {

    private final WabitProject project;

    public NewLayoutAction(WabitProject project) {
        super("New Layout");
        this.project = project;
    }

    public void actionPerformed(ActionEvent e) {
        project.addLayout(new Layout("New Layout"));
    }
}
