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

package ca.sqlpower.wabit.swingui.query;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.swingui.QueryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This panel will let a user modify properties of a {@link QueryCache} object
 * that are not displayed in the {@link QueryPanel}. These properties are less
 * often changed.
 */
public class QueryPropertiesPanel implements DataEntryPanel {
	
	/**
	 * The query this panel will modify.
	 */
	private final QueryCache query;
	
	/**
	 * The main editor panel.
	 */
	private final JPanel panel = new JPanel();
	
	/**
	 * Displays the row limit for streaming queries. Users can change
	 * the spinner's value to modify how many rows will be stored in
	 * a streaming query.
	 */
	private final JSpinner streamingRowLimitField = new JSpinner();

    /**
     * This check box displays if the user will always be prompted when the
     * query contains a cross join.
     */
    private final JCheckBox promptForCrossJoinsCB;
	
	public QueryPropertiesPanel(QueryCache query) {
		this.query = query;
		streamingRowLimitField.setValue(Integer.valueOf(query.getStreamingRowLimit()));
		streamingRowLimitField.setToolTipText("The number of rows to retain while streaming. " +
				"Old rows will be removed for new ones.");
		
		promptForCrossJoinsCB = new JCheckBox("Always prompt if query contains cross joins", 
		        query.getPromptForCrossJoins());
		
		buildUI();
	}
	
	private void buildUI() {
		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 5dlu, pref:grow"), panel);
		
		builder.append(promptForCrossJoinsCB, 3);
		
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
	    query.setPromptForCrossJoins(promptForCrossJoinsCB.isSelected());
	    query.setStreamingRowLimit((Integer) streamingRowLimitField.getValue());
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
