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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.action.ReportFromTemplateAction;

public class TemplateChooserDialog extends JDialog {
	private final JPanel panel = new JPanel(new MigLayout("", "[][][]"));
	
	public TemplateChooserDialog(final WabitSwingSession session) {
		setTitle("Template Chooser");

		int i = 0;
		for (final Template t : session.getWorkspace().getTemplates()) {
			JPanel previewPanel = new JPanel(new MigLayout("", "[center]"));
			i++;
			JButton previewButton = new JButton(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					AbstractAction action = new ReportFromTemplateAction(session, t);
					action.actionPerformed(e);
				}
			});
			TemplatePreviewIcon previewIcon = new TemplatePreviewIcon(t);
			previewButton.setIcon(previewIcon);
			previewPanel.add(previewButton, "wrap");
			previewPanel.add(new JLabel(t.getName()), "");
			if (i == 3) {
				panel.add(previewPanel, "wrap");
			} else {
				panel.add(previewPanel);
			}
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setMinimumSize(new Dimension(500, 400));
		add(scrollPane);
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

}
