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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * A Swing {@link Action} for creating new relational (SQL) queries in a Wabit
 * project.
 */
public class NewQueryAction extends AbstractAction {
	
    private final WabitWorkspace project;
    private final WabitSwingSession session;
    private final JDBCDataSource ds;
    private final String newQueryName;

	/**
	 * Creates a new query with no datasource. Typically, the user would then
	 * set the datasource of this Query afterwards.
	 * 
	 * @param session
	 *            The {@link WabitSwingSession} containing the project that the
	 *            new query will be added to.
	 */
    public NewQueryAction(WabitSwingSession session) {
        super("New Relational Query");
        this.newQueryName = "New Relational Query";
        this.project = session.getWorkspace();
        this.session = session;
        this.ds = null;
    }

	/**
	 * Creates a new query and sets its datasource to the given datasource.
	 * 
	 * @param session
	 *            The {@link WabitSwingSession} containing the project that the
	 *            new query will be added to.
	 * @param ds
	 *            The datasource that the new query will use to start with
	 */
    public NewQueryAction(WabitSwingSession session, JDBCDataSource ds) {
    	super("New Relational Query on '" + ds.getName() + "'");
    	this.newQueryName = "New " + ds.getName() + " query";
    	this.project = session.getWorkspace();
    	this.session = session;
    	this.ds = ds;
    }
    
    public void actionPerformed(ActionEvent e) {
        QueryCache query = new QueryCache(session);
        query.setName(newQueryName);
        if (ds != null) {
        	query.setDataSource(ds);
        }
		project.addQuery(query, session);
		JTree tree = session.getTree();
		int queryIndex = tree.getModel().getIndexOfChild(project, query);
		tree.setSelectionRow(queryIndex + 1);
    }
}
