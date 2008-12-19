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

package ca.sqlpower.wabit.swingui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.db.DefaultDataSourceDialogFactory;
import ca.sqlpower.swingui.db.DefaultDataSourceTypeDialogFactory;
import ca.sqlpower.swingui.db.JDBCDriverPanel;
import ca.sqlpower.wabit.JDBCDataSource;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.query.QueryCache;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This panel will display information about the project. It will
 * also allow the user to add and remove data sources.
 */
public class ProjectPanel implements DataEntryPanel {

	/**
	 * The main panel of this project.
	 */
	private final JPanel panel;
	private final WabitSwingSession session;
	private DatabaseConnectionManager dbConnectionManager;
	
	/**
	 * This action is used in the DB connection manager to add the selected db
	 * to the project.
	 */
	private final AbstractAction addDSToProjectAction = new AbstractAction("Add To Project") {
		public void actionPerformed(ActionEvent e) {
			SPDataSource ds = dbConnectionManager.getSelectedConnection();
			if (ds == null) {
				return;
			}
			boolean isDSAlreadyAdded = false;
			for (WabitDataSource wds : session.getProject().getDataSources()) {
				if (wds instanceof JDBCDataSource) {
					JDBCDataSource jdbc = (JDBCDataSource) wds;
					if (jdbc.getSPDataSource() == ds) {
						isDSAlreadyAdded = true;
					}
				}
			}
			if (!isDSAlreadyAdded) {
				session.getProject().addDataSource(ds);
			}
			Query query = new QueryCache();
			query.setName("New " + ds.getName() + " query");
			session.getProject().addQuery(query);
			query.setDataSource(ds);
			session.setEditorPanel(query);
		}
	}; 
	
	public ProjectPanel(WabitSwingSession session) {
		this.session = session;
		panel = new JPanel();
		buildUI();
	}
	
	private void buildUI() {
		Icon wabitIcon =
            new ImageIcon(ProjectPanel.class.getClassLoader().getResource("icons/wabit-400.png"));
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, fill:pref, pref:grow", "pref, pref, pref, pref"));
		CellConstraints cc = new CellConstraints();
		builder.add(new JLabel("<html><h2>Welcome to Wabit " + WabitVersion.VERSION, JLabel.CENTER), cc.xy(2, 1));
		builder.add(new JLabel(wabitIcon), cc.xy(2, 2));
		builder.add(new JLabel(
				"<html><br><br>" +
				"<p>Creating a report in Wabit involves three steps:" +
				"<ol>" +
				" <li> add a data source to your project" +
				" <li> formulate a query with the help of the query builder" +
				" <li> create a page layout for your query" +
				"</ol>" +
				"<br>" +
				"<p>Your page layout can be printed directly or saved to a PDF file." +
				"<p>To add a data source to your project, choose one from the list<br>" +
				"   below and press the <i>Add To Project</i> button."), cc.xy(2, 3));
		List<Action> actionList = new ArrayList<Action>();
		actionList.add(addDSToProjectAction);
		dbConnectionManager = new DatabaseConnectionManager(session.getContext().getDataSources(), 
				new DefaultDataSourceDialogFactory(), 
				new DefaultDataSourceTypeDialogFactory(session.getContext().getDataSources()),
				actionList, session.getFrame(), false);
		builder.add(dbConnectionManager.getPanel(), cc.xy(2, 4));
		panel.add(builder.getPanel());
	}
	
	public boolean applyChanges() {
		return true;
	}

	public void discardChanges() {
		//no changes to discard
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return false;
	}

}
