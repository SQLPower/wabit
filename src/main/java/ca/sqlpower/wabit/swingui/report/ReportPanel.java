/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.action.EditReportParameterAction;

public class ReportPanel extends LayoutPanel {

	private final JPanel dashboardPanel;
	private final JSplitPane splitPane;
	
	public ReportPanel(WabitSwingSession session, Report report) {
		
		super(session, report);
		
		// build the dashboard controls
		this.dashboardPanel = new JPanel(new MigLayout("fillx", "[][grow][][]"));
		this.dashboardPanel.add(new Label("Parameters"), "spanx 3");
		this.dashboardPanel.add(new JButton("+"), "wrap");
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		this.dashboardPanel.add(sep, "span, wrap, growx, gapbottom 10px");
		
		// Insert dashboard controls
		this.dashboardPanel.add(new Label("Option 1"));
		this.dashboardPanel.add(new JComboBox(new Object[] {"value 1", "value 2"}), "growx");
		this.dashboardPanel.add(new JButton(new EditReportParameterAction(this.dashboardPanel)));
		this.dashboardPanel.add(new JButton(WabitIcons.CLOSE_WORKSPACE), "wrap");
		
		this.dashboardPanel.add(new Label("Option 2"));
		this.dashboardPanel.add(new JComboBox(new Object[] {"value 3", "value 4"}), "growx");
		this.dashboardPanel.add(new JButton(new EditReportParameterAction(this.dashboardPanel)));
		this.dashboardPanel.add(new JButton(WabitIcons.CLOSE_WORKSPACE), "wrap");
		
		// Fuse the dashboard panel with the source list.
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setTopComponent(super.getSourceComponent());
		this.splitPane.setBottomComponent(dashboardPanel);
		this.splitPane.setOneTouchExpandable(true);

	}

	/**
	 * We override this component because we want to add a more complex component
	 * that will display the source stuff but also the dashboard controls.
	 */
	public JComponent getSourceComponent() {
		return this.splitPane;
	}
}
