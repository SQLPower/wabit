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

import ca.sqlpower.wabit.WabitSession;

/**
 * Represents a report layout in the Wabit.
 */
public class Report extends Layout {
    public Report(String name) {
        this(name,null);
    }
    
    public Report(String name, String uuid) {
        super(uuid);
        setName(name);
        updateBuiltinVariables();
    }
    
    public Report(String name, String uuid, Page page) {
    	super(uuid, page);
    	setName(name);
    	updateBuiltinVariables();
    }
    
    /**
     * Copy constructor
     * 
     * @param layout
     * 		The layout to copy
     * @param session
     * 		The session to add the layout to
     */
    public Report(Layout layout, WabitSession session) {
    	super(new Page(layout.getPage()));
    	setName(layout.getName());
    	for (String variableName : layout.getVarContext().getVariableNames()) {
    		setVariable(variableName, layout.getVarContext().getVariableValue(variableName, null));
    	}
    	setZoomLevel(layout.getZoomLevel());
    	
	}
}
