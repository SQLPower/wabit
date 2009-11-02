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

package ca.sqlpower.wabit;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides all the Java system properties as Wabit variables. Note that is is
 * technically possible (but unusual and not a recommended practice) to create
 * system properties whose keys are not an instance of String. These values will
 * not appear, and cannot be accessed, in this WabitVariables view of the system
 * properties.
 */
public class SystemPropertiesVariableContext implements VariableContext {

	public Set<String> getVariableNames() {
		HashSet<String> keys = new LinkedHashSet<String>();
		for (Object sysprop : System.getProperties().keySet()) {
			if (sysprop instanceof String) {
				keys.add((String) sysprop);
			}
		}
		return keys;
	}

	public Object getVariableValue(String name, Object defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

}
