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

package ca.sqlpower.wabit.report;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.NoRowidException;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.report.Page.StandardPageSizes;

/**
 * Represents a report layout in the Wabit.
 */
public class Layout extends AbstractWabitObject implements Runnable, Callable<Void>, VariableContext {

    private static final Logger logger = Logger.getLogger(Layout.class);
    
    /**
     * The page size and margin info.
     */
    private Page page = new Page(StandardPageSizes.US_LETTER);
    
    /**
     * The variables defined for this report.
     */
    private final Map<String, Object> vars = new HashMap<String, Object>();

    /**
     * A wrapper for {@link #call()} that achieves two purposes: firstly, it allows Report
     * to implement Runnable; secondly it conveniently wraps any checked exceptions
     * declared by call() in a RuntimeException when/if they are thrown. 
     */
    public void run() {
        try {
            call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates the report.
     */
    public Void call() throws SQLException, IOException, NoRowidException {
        // TODO render output
        return null;
    }
    
    public Page getPage() {
        return page;
    }
    
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

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<WabitObject> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean allowsChildren() {
    	return true;
    }
}
