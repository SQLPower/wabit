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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.action.ReportFromTemplateAction;

/**
 * This class creates a dialog for choosing a template for a new report.
 * Clicking on a template will use the {@link ReportFromTemplateAction}'s
 * action to add a new report to the workspace.
 * <p>
 * TODO: This class does not need to extend JDialog. The panel it creates can
 * be placed in a new JDialog in the classes that call it.
 */
public class TemplateChooserDialog extends JDialog {

    /**
     * This template is a default one that will be added to the dialog if there
     * is no templates available. This gives the user at least one option to
     * choose from and will let them see where templates are used.
     */
    private static final Template DEFAULT_TEMPLATE = new Template("Blank");
    
    static {
        //Add a content box to the default template and have it fill up to the margins.
        final ContentBox centreContentBox = new ContentBox();
        final Page page = DEFAULT_TEMPLATE.getPage();
        page.addContentBox(centreContentBox);
        centreContentBox.setX(page.getLeftMarginOffset());
        centreContentBox.setY(page.getUpperMarginOffset());
        centreContentBox.setWidth(page.getRightMarginOffset() - page.getLeftMarginOffset());
        centreContentBox.setHeight(page.getLowerMarginOffset() - page.getUpperMarginOffset());
    }
    
	private final JPanel panel = new JPanel(new MigLayout("", "[][][]"));
	
	public TemplateChooserDialog(final WabitSwingSession session) {
		setTitle("Template Chooser");

		int i = 0;
		List<Template> templates = new ArrayList<Template>();
		templates.add(DEFAULT_TEMPLATE);
		templates.addAll(session.getWorkspace().getTemplates());
		for (final Template t : templates) {
			JPanel previewPanel = new JPanel(new MigLayout("", "[center]"));
			i++;
			//TODO refactor the necessary steps from the actionPerformed method
			//and place them in a static method in ReportFromTemplateAction instead.
			//OR if the button does not need its visiblity changed replace the abstract
			//action with the ReportFromTemplateAction.
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
			if (i % 3 == 0) {
				panel.add(previewPanel, "align 50% top, wrap");
			} else {
				panel.add(previewPanel, "align 50% top");
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
