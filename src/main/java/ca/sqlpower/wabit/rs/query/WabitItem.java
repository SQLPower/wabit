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

package ca.sqlpower.wabit.rs.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.SQLGroupFunction;
import ca.sqlpower.query.QueryImpl.OrderByArgument;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * This type of {@link Item} wraps any other type of item to let other classes
 * attach {@link SPListener}s to the delegate.
 */
public abstract class WabitItem extends AbstractWabitObject {
	
    /**
     * The delegate to send the method calls to.
     */
	private final Item delegate;
	
	/**
	 * A change listener on the delegate that will change the source of the
	 * events to this object and refire them.
	 */
	private final PropertyChangeListener delegateChangeListener = new PropertyChangeListener() {
    
        public void propertyChange(PropertyChangeEvent evt) {
        	if (evt.getPropertyName().equals("name")) {
        		setName((String) evt.getNewValue());
        	}
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
	public WabitItem(Item delegate) {
		super();
		this.delegate = delegate;
		delegate.addPropertyChangeListener(delegateChangeListener);
		setName(delegate.getName());
	}
	
	@Override
	public CleanupExceptions cleanup() {
		delegate.removePropertyChangeListener(delegateChangeListener);
		return new CleanupExceptions();
	}
	
	public Item getDelegate() {
	    return delegate;
	}

	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}

	public boolean allowsChildren() {
		return false;
	}
	
	public List<Class<? extends SPObject>> allowedChildTypes() {
		return Collections.emptyList();
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.emptyList();
	}

	public List<WabitObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(SPObject dependency) {
		// do nothing
	}
	
	//-----------------Item setters and getters--------------
	//TODO make this a proper delegate

	public void setGroupBy(SQLGroupFunction groupBy) {
		delegate.setGroupBy(groupBy);
    }

    public SQLGroupFunction getGroupBy() {
        return delegate.getGroupBy();
    }

    public void setHaving(String having) {
    	delegate.setHaving(having);
    }

    public String getHaving() {
        return delegate.getHaving();
    }

    public void setOrderBy(OrderByArgument orderBy) {
    	delegate.setOrderBy(orderBy);
    }

    public OrderByArgument getOrderBy() {
        return delegate.getOrderBy();
    }
    
	public Integer getSelected() {
		return delegate.getSelected();
	}
	
	public void setSelected(Integer selected) {
		delegate.setSelected(selected);
	}
	
	public void setOrderByOrdering(Integer ordering) {
		delegate.setOrderByOrdering(ordering);
	}
	
	public Integer getOrderByOrdering() {
	    return delegate.getOrderByOrdering();
	}
	
	public void setAlias(String alias) {
		delegate.setAlias(alias);
	}
	
	public String getAlias() {
		return delegate.getAlias();
	}
	
	public void setWhere(String where) {
		delegate.setWhere(where);
	}
	
	public String getWhere() {
		return delegate.getWhere();
	}
	
	public void setColumnWidth(Integer width) {
		delegate.setColumnWidth(width);
	}
	
	public Integer getColumnWidth() {
		return delegate.getColumnWidth();
	}
	
	//-----------------End Item setters and getters-----------
	

}
