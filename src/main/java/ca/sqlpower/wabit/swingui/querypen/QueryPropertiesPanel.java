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

package ca.sqlpower.wabit.swingui.querypen;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.Query;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This panel will let a user modify properties of a {@link Query} object that
 * do not fit well on the {@link Query}'s editor panel. 
 */
public class QueryPropertiesPanel implements DataEntryPanel {
	
	/**
	 * The query this panel will modify.
	 */
	private final Query query;
	
	/**
	 * The main editor panel.
	 */
	private final JPanel panel = new JPanel();
	
	private final JTextField streamingRowLimitField = new JTextField();
	
	public QueryPropertiesPanel(Query query) {
		this.query = query;
		streamingRowLimitField.setText(new Integer(query.getStreamingRowLimit()).toString());
		streamingRowLimitField.setToolTipText("The number of rows to retain while streaming. Old rows will be removed for new ones.");
		
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu, pref:grow"), panel);
		String connectionStyle;
		if (query.isStreaming()) {
			connectionStyle = "Streaming";
		} else {
			connectionStyle = "Non-streaming";
		}
		builder.append("Connection Style", new JLabel(connectionStyle));
		builder.nextLine();
		if (query.isStreaming()) {
			builder.append("Row Limit", streamingRowLimitField);
		} else {
			//TODO:add non streaming properties here.
		}
	}

	public boolean applyChanges() {
		try {
			int streamingRowLimit = Integer.parseInt(streamingRowLimitField.getText());
			query.setStreamingRowLimit(streamingRowLimit);
		} catch (NumberFormatException e) {
			//If the user entered an invalid streaming row limit don't change the previous value.
		}
		return true;
	}

	public void discardChanges() {
		//Do nothing
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return true;
	}

}
