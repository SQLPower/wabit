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

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * A Swing Action for creating a new OLAP Query in a workspace.
 */
public class NewOLAPQueryAction extends AbstractAction {

	private static final Icon NEW_OLAP_QUERY_ICON = new ImageIcon(WabitSwingSessionImpl.class.getClassLoader().getResource("icons/query-olap-16.png"));
	
	private WabitSwingSession session;
	private Olap4jDataSource ds;
	private String newQueryName;
	
	public NewOLAPQueryAction(WabitSwingSession session) {
		super("New OLAP Query", NEW_OLAP_QUERY_ICON);
		this.session = session;
		this.ds = null;
		this.newQueryName = "New OLAP query";
	}
	
	// TODO: Ideally, we would be able to choose the datasource from the OLAP
	// query editor like with relational queries rather than having to specify
	// it here, for flexibility and consistency.
	public NewOLAPQueryAction(WabitSwingSession session, Olap4jDataSource ds) {
		super("New OLAP Query on '" + ds.getName() + "'", NEW_OLAP_QUERY_ICON);
        this.session = session;
        this.ds = ds;
        this.newQueryName = "New " + ds.getName() + " query";
	}
	
	public void actionPerformed(ActionEvent e) {
		OlapQuery newQuery = new OlapQuery(session.getContext());
	    newQuery.setOlapDataSource(ds);
	    newQuery.setName(newQueryName);
	    session.getWorkspace().addOlapQuery(newQuery);
		JTree tree = session.getTree();
		int queryIndex = tree.getModel().getIndexOfChild(session.getWorkspace(), newQuery);
		tree.setSelectionRow(queryIndex + 1);
	}

}
