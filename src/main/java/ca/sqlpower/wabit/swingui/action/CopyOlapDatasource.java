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

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitSession;

public class CopyOlapDatasource extends AbstractAction {
	private WabitSession session;
	private WabitDataSource wds;
	
	public CopyOlapDatasource(WabitSession session, WabitDataSource wds) {
		super("Copy Datasource");
		this.session = session;
		this.wds = wds;
	}
	
	public void actionPerformed(ActionEvent e) {
		SPDataSource ds = wds.getSPDataSource();
		SPDataSource newDS;
		if (ds instanceof JDBCDataSource) {
			newDS = (SPDataSource) new JDBCDataSource(session.getWorkspace());
			newDS.copyFrom(ds);
		} else if (ds instanceof Olap4jDataSource) {
			newDS = new Olap4jDataSource(session.getWorkspace());
			newDS.copyFrom(ds);
		} else {
			throw new UnsupportedOperationException("Datasource of type " + 
					ds.getClass().getName() + " is not yet supported to be copied to another workspace.");
		}
		newDS.setDisplayName(wds.getSPDataSource().getDisplayName() + " Copy");
		WabitDataSource newWO = new WabitDataSource(newDS);
		newWO.setName(wds.getName() + " Copy");
		session.getDataSources().addDataSource(newDS);
		session.getWorkspace().addDataSource((WabitDataSource) newWO);
	}
}
