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

import javax.swing.JComponent;

import ca.sqlpower.swingui.DataEntryPanel;

/**
 * All panels that are to be displayed in Wabit's main split pane 
 * to the left of the workspace tree should implement this interface.
 * This interface defines basic behaviour required of all panels in
 * Wabit including maximizing the editor.
 */
public interface WabitPanel extends DataEntryPanel {

	/**
	 * Returns a suitable string that can be used in the title of a containing
	 * window or frame. Usually describes what the panel contains. (ex.
	 * "Query Editor" or "Report Editor")
	 */
	String getTitle();

    /**
     * Returns the component (often a JTree or JList, but can be anything) that
     * contains all the workspace items that can be dragged into this editor.
     * The component will <i>not</i> be wrapped in a scroll pane; you get to do
     * this yourself if your component needs one.
     * 
     * @return The component as described above, or null if nothing can be
     *         dropped on this panel.
     */
	JComponent getSourceComponent();
}
