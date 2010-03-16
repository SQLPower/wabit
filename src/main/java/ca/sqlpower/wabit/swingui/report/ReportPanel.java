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

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.report.selectors.SelectorsPanel;

public class ReportPanel extends LayoutPanel {

	private SelectorsPanel dashboardPanel;
	final private JSplitPane splitPane;
	private final Report report;
	
	
	
	
	
	private final Runnable reportRefresher = new Runnable() {
		public void run() {
			refreshDataAction.actionPerformed(null);
		}
	};
	
	public ReportPanel(WabitSwingSession session, final Report report) {
		
		super(session, report);
		this.report = report;
		
		// build the dashboard controls
		this.dashboardPanel = new SelectorsPanel(report, reportRefresher);
		
		// Fuse the dashboard panel with the source list.
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setTopComponent(super.getSourceComponent());
		this.splitPane.setBottomComponent(dashboardPanel);
		this.splitPane.setOneTouchExpandable(true);
		
		// Setting a divider location needs the divider
		// to be visible, so we wrap this in a runnable.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				splitPane.setDividerLocation(0.5d);
			}
		});
		
	}
	
	
	
	
	
	@Override
	protected void cleanup() {
		super.cleanup();
		this.dashboardPanel.cleanup();
	}

	/**
	 * We override this component because we want to add a more complex component
	 * that will display the source stuff but also the dashboard controls.
	 */
	public JComponent getSourceComponent() {
		return this.splitPane;
	}
}
