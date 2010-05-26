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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ObjectUtils;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.object.SPVariableResolverProvider;
import ca.sqlpower.wabit.AbstractWabitObject;

abstract class AbstractSelector extends AbstractWabitObject implements Selector {

	/**
	 * Default value to include in the list of possible values.
	 */
	private Object defaultValue = null;
	
	private SPVariableResolver resolver;
	
	private List<SelectorListener> selectorListeners = new CopyOnWriteArrayList<SelectorListener>();
	
	
	
	
	public AbstractSelector() {
		super();
		this.setName("New Report Parameter");
	}

	
	
	
	
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		
		Object oldValue = this.defaultValue;
		this.defaultValue = defaultValue;
		
		if (!ObjectUtils.equals(oldValue, this.defaultValue)) {
		
			firePropertyChange("defaultValue", oldValue, this.defaultValue);
			
			if (ObjectUtils.equals(getCurrentValue(), oldValue)) {
				setSelectedValue(this.defaultValue);
			}
			
			this.fireSelectionChanged();
		}
	}
	
	
	
	
	/*
	 * Selector implementation
	 */
	public Object getCurrentValue() {
		
		if (resolver != null) {
		
			return resolver.resolve(getParent().getUUID() + SPVariableResolver.NAMESPACE_DELIMITER + getName(), getDefaultValue());
		
		} else {
		
			return getDefaultValue();
		
		}
	}
	
	
	
	public boolean setSelectedValue(Object newValue) {
		
		if ((newValue == null ||
				ObjectUtils.equals(getDefaultValue(), newValue)) &&
				resolver != null) {
			
			resolver.update(getName(), getDefaultValue());
		
		} else if (this.resolver != null && this.getName() != null) {
		
			resolver.update(getName(), newValue);
		
		}		
		
		fireSelectionChanged();
		
		return true;
	}
	
	
	
	
	
	/*
	 * SPObject overrides.
	 */
	
	@Override
	public void setName(String name) {
		if (resolver != null && this.getName() != null) {
			resolver.delete(getName());
		}
		super.setName(name);
		if (resolver != null && this.getName() != null) {
			resolver.update(getName(), getCurrentValue());
		}
	}
	
	@Override
	public void setParent(SPObject parent) {
		if (this.resolver != null && getName() != null) {
			this.resolver.delete(getName());
		}
		if (parent == null) {
			super.setParent(null);
			this.resolver = null;
		} else if (parent instanceof SPVariableResolverProvider) {
			super.setParent(parent);
			this.resolver = ((SPVariableResolverProvider)parent).getVariableResolver();
			if (this.resolver != null) {
				this.resolver.update(getName(), getDefaultValue());
			}
		} else {
			throw new AssertionError("Selectors can only be children of instances of " + SPVariableResolverProvider.class.getCanonicalName());
		}
	}
	
	@Override
	public boolean allowsChildType(Class<? extends SPObject> type) {
		return false;
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
		// No childs.
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

	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return Collections.emptyList();
	}

	public List<? extends SPObject> getChildren() {
		return Collections.emptyList();
	}

	public List<? extends SPObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(SPObject dependency) {
		// No op.
	}
	
	public void addSelectorListener(SelectorListener listener) {
		this.selectorListeners.add(listener);
	}
	
	public void removeSelectorListener(SelectorListener listener) {
		this.selectorListeners.remove(listener);
	}
	
	protected void fireSelectionChanged() {
		for (SelectorListener listener : this.selectorListeners) {
			listener.selectionChanged(this);
		}
	}
}
