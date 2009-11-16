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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionImpl;

/**
 * A Swing {@link Action} for creating new relational (SQL) queries in a Wabit
 * workspace.
 */
public class NewQueryAction extends AbstractAction {
	
	private static final Icon NEW_QUERY_ICON = new ImageIcon(WabitSwingSessionImpl.class.getClassLoader().getResource("icons/query-db-16.png"));

	private final WabitWorkspace workspace;
    private final WabitSwingSession session;
    private final JDBCDataSource ds;
    private final String newQueryName;

	/**
	 * Creates a new query with the first available datasource, or no datasource
	 * if one does not exist.
	 * 
	 * @param session
	 *            The {@link WabitSwingSession} containing the workspace that
	 *            the new query will be added to.
	 */
    public NewQueryAction(WabitSwingSession session) {
        this(session, getFirstAvailableDataSource(session));
    }

	/**
	 * Creates a new query and sets its datasource to the given datasource.
	 * 
	 * @param session
	 *            The {@link WabitSwingSession} containing the workspace that the
	 *            new query will be added to.
	 * @param ds
	 *            The datasource that the new query will use to start with
	 */
    public NewQueryAction(WabitSwingSession session, JDBCDataSource ds) {
    	super("New Relational Query" + ((ds == null)? "" : " on '" + ds.getName() + "'"), NEW_QUERY_ICON);
    	this.newQueryName = "New " + ((ds == null)? "Relational" : ds.getName()) + " Query";
    	this.workspace = session.getWorkspace();
    	this.session = session;
    	this.ds = ds;
    }
    
    public void actionPerformed(ActionEvent e) {
        QueryCache query = new QueryCache(session.getContext(), true, null, ds);
        query.setName(newQueryName);
		workspace.addQuery(query, session);
		JTree tree = session.getTree();
		int queryIndex = tree.getModel().getIndexOfChild(workspace, query);
		tree.setSelectionRow(queryIndex + 1);
    }
    
    /**
     * Returns the first available {@link JDBCDataSource} from a given {@link WabitSwingSession}
     * or null if one does not exist.
     */
    private static JDBCDataSource getFirstAvailableDataSource(WabitSwingSession session) {
    	List<JDBCDataSource> connections = session.getDataSources().getConnections(JDBCDataSource.class);
    	return connections.isEmpty()? null : connections.get(0);
    }
}
