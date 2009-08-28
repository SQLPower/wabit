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

package ca.sqlpower.wabit.swingui;

import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.swingui.DataEntryPanel;

/**
 * A JPanel with a bunch of controls to allow the user to change global
 * application settings for the Wabit
 */
public class WabitApplicationPreferencesPanel implements DataEntryPanel {

	/**
	 * The panel that will display the controls to modify the preferences
	 */
	private JPanel panel;

	/**
	 * A checkbox to set if the user wants all queries in to be automatically
	 * executed when opened or modified.
	 */
	private JCheckBox autoExecuteQueries;

	/**
	 * The Preferences object representing the Wabit's global application preferences
	 */
	private final Preferences prefs;
	
	/**
	 * Create a global preferences panel using the preferences stored in the
	 * given prefs argument.
	 * 
	 * @param prefs
	 *            The {@link Preferences} object that contains the Wabit's
	 *            global application preferences
	 */
	public WabitApplicationPreferencesPanel(Preferences prefs) {
		this.prefs = prefs;
		prefs.getBoolean("", true);
		panel = new JPanel(new MigLayout());
		autoExecuteQueries = new JCheckBox("Automatically execute all queries when accessed", 
				prefs.getBoolean(WabitSwingSessionContextImpl.AUTOMATICALLY_EXECUTE_ALL_QUERIES, true));
		panel.add(autoExecuteQueries, "span");
	}
	
	public boolean applyChanges() {
		prefs.putBoolean(WabitSwingSessionContextImpl.AUTOMATICALLY_EXECUTE_ALL_QUERIES, autoExecuteQueries.isSelected());
		return true;
	}

	public void discardChanges() {
		autoExecuteQueries.setSelected(prefs.getBoolean(WabitSwingSessionContextImpl.AUTOMATICALLY_EXECUTE_ALL_QUERIES, true));
	}

	public JComponent getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		// return true so that 'Cancel' will always run discardChanges
		return true;
	}
}
