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

package ca.sqlpower.wabit;

import java.util.Set;

/**
 * An interface for providing variable names and their associated values.
 */
public interface VariableContext {

    /**
     * Returns the set of all defined variable names in this context.
     */
    Set<String> getVariableNames();

    /**
     * Returns the value of the named variable, or the given default value if
     * the variable is not defined. If you want a reliable way to find out if
     * a variable is defined, look it up in the set returned by
     * {@link #getVariableNames()}.
     * 
     * @param name
     *            The name of the variable
     * @param defaultValue
     *            The value to return if the variable is not defined
     * @return The value of the variable.
     */
    Object getVariableValue(String name, Object defaultValue);
}
