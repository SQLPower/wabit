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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.WabitObject;

public abstract class CopyAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(CopyAction.class);
	
	protected WabitObject target;
	
	private Window dialogOwner;
	
	private JLabel nameLabel = new JLabel("Name of Copy:  ");
	
	private JTextField nameField;
	
	private DataEntryPanel namePanel = new DataEntryPanel(){

		public boolean applyChanges() {
			logger.debug("Copying " + target.getName());
			copy(nameField.getText());
			return true;
		}

		public void discardChanges() {
			logger.debug("Cancelling Copy");
			//no-op
		}

		public JComponent getPanel() {
			JPanel propertiesPanel = new JPanel();
			propertiesPanel.setLayout(new BorderLayout());
			propertiesPanel.add(nameLabel, BorderLayout.WEST);
			propertiesPanel.add(nameField, BorderLayout.CENTER);
			return propertiesPanel;
		}

		public boolean hasUnsavedChanges() {
			return true;
		}
		
	};
	
	public CopyAction(WabitObject target, Window dialogOwner){
		super("Copy " + target.getName());
		this.target = target;
		this.dialogOwner = dialogOwner;
		nameField = new JTextField("Copy of " + target.getName());
		nameField.setPreferredSize(new Dimension(300, 30));
	}
	
	public void actionPerformed(ActionEvent e){
		JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                namePanel, dialogOwner, "Copying " + target.getName(), "OK");
        d.setVisible(true);
        logger.debug("Showing Copy Dialog");
	}
	
	public abstract void copy(String name);
	
}
