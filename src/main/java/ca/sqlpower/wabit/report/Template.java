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

package ca.sqlpower.wabit.report;

import ca.sqlpower.wabit.WabitSession;

public class Template extends Layout {
    public Template(String name) {
        this(name,null);
    }
    
    public Template(String name, String uuid) {
        super(uuid);
        setName(name);
        updateBuiltinVariables();
    }
    
    public Template(String name, String uuid, Page page) {
        super(uuid, page);
        setName(name);
        updateBuiltinVariables();
    }
    
    /**
     * Copy constructor
     * 
     * @param template
     * 		The layout to copy
     * @param session
     * 		The session to add the layout to
     */
    public Template(Template template, WabitSession session) {
    	super(new Page(template.getPage()));
    	setName(template.getName());
    	setZoomLevel(template.getZoomLevel());
	}
}
