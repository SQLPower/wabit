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

package ca.sqlpower.wabit.swingui.tree;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Page;

/**
 * This tree cell editor allows the user to set the name of objects in the tree.
 */
public class WorkspaceTreeCellEditor extends DefaultTreeCellEditor {
	
	private final KeyListener keyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {
			//Do nothing
		}
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				stopCellEditing();
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (currentlyEditingObject != null) {
					textField.setText(currentlyEditingObject.getName());
				}
				cancelCellEditing();
			}
		}
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (currentlyEditingObject != null) {
					textField.setText(currentlyEditingObject.getName());
				}
			}
		}
	};
	
	private final FocusListener focusListener = new FocusListener() {
		public void focusLost(FocusEvent e) {
			stopCellEditing();
		}
		public void focusGained(FocusEvent e) {
			//Do nothing
		}
	};

	public WorkspaceTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
		
	}
	
	/**
	 * This is the wabit object being currently edited by this editor. If this
	 * is null then no wabit object is being edited.
	 */
	private AbstractSPObject currentlyEditingObject;

	/**
	 * This JTextField allows editing of the name of WabitObjects.
	 */
	private JTextField textField;
	
	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component component = super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
				leaf, row);
		if (value instanceof AbstractSPObject) {
			currentlyEditingObject = (AbstractSPObject) value;
			textField = new JTextField(((AbstractSPObject) value).getName());
			textField.addFocusListener(focusListener);
			textField.addKeyListener(keyListener);
			return textField;
		} else {
			currentlyEditingObject = null;
		}
		return component;
	}
	
	@Override
	public boolean stopCellEditing() {
		boolean isStopping = super.stopCellEditing();
		if (isStopping && currentlyEditingObject != null) {
			if (currentlyEditingObject.getParent() instanceof Page) {
				((Page) currentlyEditingObject.getParent()).setUniqueName(
						(WabitObject) currentlyEditingObject, textField
								.getText());
			} else {
				currentlyEditingObject.setName(textField.getText());
			}
			textField.removeFocusListener(focusListener);
			textField.addKeyListener(keyListener);
		}
		return isStopping;
	}
	
}
