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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.report.selectors.Selector;

/**
 * Represents a report layout in the Wabit.
 */
public class Report extends Layout {
	
	private List<Selector> selectors = new ArrayList<Selector>();
	
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
    	setZoomLevel(layout.getZoomLevel());
	}
    
    public int childPositionOffset(Class<? extends SPObject> childType) {
        if (childType == Page.class) {
            return 0;
        } else if (Selector.class.isAssignableFrom(childType)) {
            return super.getChildren().size();
        } else {
        	throw new IllegalArgumentException("Layouts don't have children of type " + childType);
        }
    }

    @Override
    public List<WabitObject> getChildren() {
    	List<WabitObject> children = new ArrayList<WabitObject>();
    	children.addAll(super.getChildren());
    	children.addAll(this.selectors);
        return children;
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child instanceof Selector) {
        	int indexOf = this.selectors.indexOf(child);
        	boolean removed = this.selectors.remove(child);
        	if (removed) {
        		fireChildRemoved(Selector.class, child, indexOf);
        	}
        	return removed;
        } else {
        	return false;
        }
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (child instanceof Selector) {
        	this.selectors.add(index, (Selector)child);
        	fireChildAdded(Selector.class, child, index);
        } else {
        	throw new IllegalArgumentException("Only Selectors are possible children of reports.");
        }
    }
    
    public boolean allowsChildren() {
    	return true;
    }
    
    @Override
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.addAll(super.getAllowedChildTypes());
    	types.add(Selector.class);
    	return types;
    }
    
    public List<Selector> getSelectors() {
		return Collections.unmodifiableList(this.selectors);
	}
}
