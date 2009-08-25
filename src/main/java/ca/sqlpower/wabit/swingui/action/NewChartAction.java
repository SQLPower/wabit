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

import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * An action creates a new Chart and adds it to the workspace. The new chart can
 * optionally have its data provider set to a predetermined value.
 */
public class NewChartAction extends AbstractAction {
    
	private static final Icon NEW_CHART_ICON = new ImageIcon(
	        WabitSwingSessionImpl.class.getClassLoader().getResource("icons/chart-16.png"));
	
    private final WabitSwingSession session;

    private final WabitObject dataProvider;

    /**
     * Creates an action which, when invoked, creates new chart with no default
     * query, and adds it to the workspace.
     * 
     * @param session
     *            The session whose workspace the new chart will belong to.
     * @param dataProvider
     *            The QueryCache or OlapQuery that will provide data to the new
     *            chart. Null means not to set an initial data provider.
     */
    public NewChartAction(WabitSwingSession session, WabitObject dataProvider) {
        super("New Chart", NEW_CHART_ICON);
        this.session = session;
        this.dataProvider = dataProvider;
    }

    /**
     * Creates an action which, when invoked, creates new chart with no default
     * query, and adds it to the workspace.
     * 
     * @param session
     */
    public NewChartAction(WabitSwingSession session) {
        this(session, null);
    }

    public void actionPerformed(ActionEvent e) {
        Chart chart = new Chart();
        chart.setName("New Chart");
        
        if (dataProvider != null) {
            try {
                chart.setName(dataProvider.getName() + " Chart");
                chart.defineQuery(dataProvider);
            } catch (SQLException ex) {
                SPSUtils.showExceptionDialogNoReport(
                        session.getTree(), "Failed to create chart", ex);
                return;
            }
        }
        
        session.getWorkspace().addChart(chart);
    }

}
