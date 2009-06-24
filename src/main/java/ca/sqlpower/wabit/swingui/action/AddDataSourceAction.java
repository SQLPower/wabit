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

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.WabitWorkspace;

/**
 * An action that adds a certain data source to a certain project when it is
 * invoked. The main purpose for this action is to act as an "add data source"
 * menu item.
 */
public class AddDataSourceAction extends AbstractAction {

    private final WabitWorkspace project;
    private final SPDataSource dataSource;

    public AddDataSourceAction(WabitWorkspace project, SPDataSource dataSource) {
        super(dataSource.getName());
        this.project = project;
        this.dataSource = dataSource;
    }
    
    public void actionPerformed(ActionEvent e) {
        project.addDataSource(dataSource);
    }
}
