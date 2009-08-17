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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.swingui.query.QueryPropertiesPanel;

/**
 * This action will create and show the properties panel of a query object.
 */
public class ShowQueryPropertiesAction extends AbstractAction {
	
	private static final Icon QUERY_PROPERTIES_ICON = new ImageIcon(CreateLayoutFromQueryAction.class.getResource("/icons/32x32/settings.png"));

	/**
	 * The window to parent the dialog to.
	 */
	private final Window parent;

	/**
	 * The query this listener will show a properties panel for.
	 */
	private final QueryCache query;

	public ShowQueryPropertiesAction(QueryCache query, Window parent) {
		super("Query Properties...", QUERY_PROPERTIES_ICON);
		this.query = query;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(new QueryPropertiesPanel(query),
		        parent, query.getName() + " Properties panel", "OK");
		dialog.setVisible(true);
	}
	
}
