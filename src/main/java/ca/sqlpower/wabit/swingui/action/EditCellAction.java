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
import javax.swing.JTree;

import ca.sqlpower.wabit.AbstractWabitObject;

/**
 * Allows editing of the name of the currently selected tree cell.
 * Only {@link AbstractWabitObject}s can have their name edited. 
 */
public class EditCellAction extends AbstractAction {
	
	private final JTree tree;

	public EditCellAction(JTree tree) {
		super("Edit name");
		this.tree = tree;
	}

	public void actionPerformed(ActionEvent e) {
		tree.startEditingAtPath(tree.getSelectionPath());
	}

}
