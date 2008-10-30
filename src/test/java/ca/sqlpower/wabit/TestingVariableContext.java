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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple variable context implementation for use in test cases.
 */
public class TestingVariableContext implements VariableContext {

    private final Map<String, Object> vars = new HashMap<String, Object>();

    public Set<String> getVariableNames() {
        return vars.keySet();
    }

    public <T> T getVariableValue(String name, T defaultValue) {
        if (vars.containsKey(name)) {
            Class<T> valueClass = (Class<T>) defaultValue.getClass();
            return valueClass.cast(vars.get(name));
        } else { 
            return defaultValue;
        }
    }
    
    public void setVariable(String name, Object value) {
        vars.put(name, value);
    }
}
