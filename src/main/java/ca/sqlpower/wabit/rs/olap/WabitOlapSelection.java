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

package ca.sqlpower.wabit.rs.olap;

import java.util.Collections;
import java.util.List;

import org.olap4j.query.Selection;
import org.olap4j.query.Selection.Operator;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wrapper class to wrap an Olap4j Selection, that is, an Inclusion or
 * Exclusion.
 */
public abstract class WabitOlapSelection extends AbstractWabitObject {

	/**
	 * The {@link Selection} this class wraps. Should not be leaked out of the
	 * wabit.olap package.
	 */
	protected Selection selection;

	protected final Operator operator;

	protected final String uniqueMemberName;

	protected boolean initialized = false;

	/**
	 * Copy Constructor. Creates a new WabitOlapSelection with the same
	 * properties as the parameter.
	 */
	public WabitOlapSelection(WabitOlapSelection selection) {
		this(selection.operator, selection.uniqueMemberName);
	}
	
	/**
	 * Creates a new WabitOlapSelection to wrap the given {@link Selection}.
	 */
	public WabitOlapSelection(Selection selection) {
		this.selection = selection;
		this.operator = selection.getOperator();
		this.uniqueMemberName = selection.getMember().getUniqueName();
		initialized = true;
		//XXX The object is not constructed so what is listening to it?
		firePropertyChange("operator", null, operator);
		firePropertyChange("uniqueMemberName", null, uniqueMemberName);
		
		setName(getUniqueMemberName());
	}

	/**
	 * Creates a new WabitOlapSelection with the given properties. Note that
	 * this creates an uninitialized wrapper, that is, it has no wrapped class
	 * until it is initialized. Until then, any getters will return cached
	 * values.
	 */
	public WabitOlapSelection(Operator operator, String uniqueMemberName) {
		this.operator = operator;
		this.uniqueMemberName = uniqueMemberName;
		//XXX The object is not constructed so what is listening to it?
		firePropertyChange("operator", null, operator);
		firePropertyChange("uniqueMemberName", null, uniqueMemberName);
		setName(getUniqueMemberName());
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		return 0;
	}

	/**
	 * Selections are leaf nodes.
	 */
	public List<? extends WabitObject> getChildren() {
		return Collections.emptyList();
	}

	/**
	 * Olap wrapper classes only depend on the wrapped Olap4j objects
	 */
	@SuppressWarnings("unchecked")
	public List<WabitObject> getDependencies() {
		return Collections.EMPTY_LIST;
	}

	public void removeDependency(SPObject dependency) {
		//no-op
	}

	/**
	 * Returns the {@link Operator} associated with the wrapped Selection, or
	 * the cached Operator if this object hasn't been initialized.
	 */
	public Operator getOperator() {
		if (initialized) {
			return selection.getOperator();
		} else {
			return operator;
		}
	}

	/**
	 * Returns the unique member name associated with the wrapped Selection, or
	 * the cached Operator if this object hasn't been initialized.
	 */
	public String getUniqueMemberName() {
		if (initialized) {
			return selection.getMember().getUniqueName();
		} else {
			return uniqueMemberName;
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Returns the Selection wrapped by this object. This method is package
	 * private to avoid leaking the Olap4j object wrapped inside, and to allow
	 * other OLAP specific classes access.
	 */
	Selection getSelection() {
		if (!initialized) {
			throw new IllegalStateException("Olap Selection is not initialized");
		}
		return selection;
	}
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }
	
}
