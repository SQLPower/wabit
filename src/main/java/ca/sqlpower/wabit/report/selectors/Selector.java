/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.wabit.report.selectors;

import ca.sqlpower.wabit.WabitObject;

public interface Selector extends WabitObject {

	/**
	 * Sets the currently selected value of this selector.
	 * The objects supported are selector-specific.
	 * @param newValue
	 * @return True if it is set. False otherwise.
	 */
	boolean setSelectedValue(Object newValue);
	
	/**
	 * Returns the current value of this selector,
	 * or it's default value if none have been set
	 * already. The default could be null as well...
	 * @return
	 */
	Object getCurrentValue();

	/**
	 * Registers a selector listener
	 * @param listener
	 */
	public void addSelectorListener(SelectorListener listener);

	/**
	 * De-Registers a listener
	 * @param listener
	 */
	public void removeSelectorListener(SelectorListener listener);
}
