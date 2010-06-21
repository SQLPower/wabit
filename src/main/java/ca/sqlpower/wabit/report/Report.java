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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.report.selectors.DateSelector;
import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;

/**
 * Represents a report layout in the Wabit.
 */
public class Report extends Layout {

	/**
	 * FIXME This enum defines the {@link SPObject} child classes a
	 * {@link Report} takes as well as the ordinal order of these child classes
	 * such that the class going before does not depend on the class that goes
	 * after. This is here temporarily, see bug 2327 for future enhancements.
	 * http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=2327
	 */
	public enum SPObjectOrder {
		PAGE(Page.class),
		SELECTOR(Selector.class, ComboBoxSelector.class, DateSelector.class, TextBoxSelector.class);
		
		/**
		 * @see #getSuperChildClass()
		 */
		private final Class<? extends SPObject> superChildClass;
		
		/**
		 * @see #getChildClasses()
		 */
		private final Set<Class<? extends SPObject>> classes;

		/**
		 * Creates a new {@link SPObjectOrder},
		 * 
		 * @param superChildClass
		 *            The highest {@link SPObject} class that the
		 *            {@link SPObject#childPositionOffset(Class)} method looks
		 *            at to determine the index.
		 * @param classes
		 *            The list of child {@link SPObject} class varargs which
		 *            share the same ordering in the list of children. These
		 *            classes must be extending/implementing
		 *            {@link #superChildClass}.
		 */
		private SPObjectOrder(Class<? extends SPObject> superChildClass, Class<? extends SPObject>... classes) {
			this.superChildClass = superChildClass;
			this.classes = new HashSet<Class<? extends SPObject>>(Arrays.asList(classes));
		}

		/**
		 * Returns the highest {@link SPObject} class that the
		 * {@link SPObject#childPositionOffset(Class)} method looks at to
		 * determine the index.
		 */
		public Class<? extends SPObject> getSuperChildClass() {
			return superChildClass;
		}

		/**
		 * Returns the {@link Set} of {@link SPObject} classes that share the
		 * same ordering in the list of children. These classes must either
		 * extend/implement from the same class type given by
		 * {@link SPObjectOrder#getSuperChildClass()}.
		 */
		public Set<Class<? extends SPObject>> getChildClasses() {
			return Collections.unmodifiableSet(classes);
		}
		
		public static SPObjectOrder getOrderBySimpleClassName(String name) {
			for (SPObjectOrder order : values()) {
				if (order.getSuperChildClass().getSimpleName().equals(name)) {
					return order;
				} else {
					for (Class<? extends SPObject> childClass : order.getChildClasses()) {
						if (childClass.getSimpleName().equals(name)) {
							return order;
						}
					}
				}
			}
			throw new IllegalArgumentException("The " + SPObject.class.getSimpleName() + 
					" class \"" + name + "\" does not exist or is not a child type " +
							"of " + Report.class.getSimpleName() + ".");
		}
		
	}
	
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
        	int indexOf = getChildren().indexOf(child);
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
        	fireChildAdded(Selector.class, child, getChildren().indexOf(child));
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
