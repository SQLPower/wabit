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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.sqlpower.swingui.AboutPanel;
import ca.sqlpower.swingui.CommonCloseAction;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitVersion;

public class AboutAction extends AbstractAction {
	
	/**
	 * An icon for the aboutAction in the Help menu.
	 */
	private static final Icon ABOUT_ICON = new ImageIcon(AboutAction.class.getClassLoader().getResource("icons/wabit-16.png"));
	
	private final JFrame parentFrame;

	public AboutAction(JFrame parentFrame) {
		super("About Wabit", ABOUT_ICON);
		this.parentFrame = parentFrame;
	}
	
	public void actionPerformed(ActionEvent evt) {
		// This is one of the few JDIalogs that can not get replaced
		// with a call to DataEntryPanelBuilder, because an About
		// box must have only ONE button...
		final JDialog d = new JDialog(parentFrame,
									  "About Wabit");
		JPanel cp = new JPanel(new BorderLayout(12,12));
		cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		
		ImageIcon icon = SPSUtils.createIcon("wabit-128", "Wabit Logo");
		final AboutPanel aboutPanel = new AboutPanel(icon, "Wabit", "ca/sqlpower/wabit/wabit.properties", WabitVersion.VERSION.toString());
		cp.add(aboutPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
					aboutPanel.applyChanges();
					d.setVisible(false);
			}
		};
		okAction.putValue(Action.NAME, "OK");
		JDefaultButton okButton = new JDefaultButton(okAction);
		buttonPanel.add(okButton);

		cp.add(buttonPanel, BorderLayout.SOUTH);
		SPSUtils.makeJDialogCancellable(
				d, new CommonCloseAction(d));
		d.getRootPane().setDefaultButton(okButton);
		d.setContentPane(cp);
		d.pack();
		d.setLocationRelativeTo(parentFrame);
		d.setVisible(true);
	}
}
