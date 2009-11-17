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

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.ContainerChildEvent;
import ca.sqlpower.query.ContainerChildListener;
import ca.sqlpower.query.Item;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * This container wraps any other kind of Container to allow attaching
 * {@link SPListener}s that will be notified appropriately when events happen
 * on the container.
 */
public abstract class WabitContainer<T extends WabitItem> extends AbstractWabitObject {

    /**
     * The container that is delegated to by this object.
     */
	private final Container delegate;
	
	/**
	 * This is a list of {@link WabitItem}s that mimics the items in the delegate.
	 * Items should be added to and removed from this list as they are added
	 * to and removed from the delegate. These children will also be returned
	 * instead of the delegate's children when they are requested. The children
	 * of the delegate contain a parent pointer to the delegate and the {@link WabitItem}s
	 * point to this container instead.
	 */
	protected final List<T> children = new ArrayList<T>();

    /**
     * A listener on the delegate container which refires the child events with
     * this object as its source.
     */
	private final ContainerChildListener containerChildListener = new ContainerChildListener() {
    
        public void containerChildRemoved(ContainerChildEvent evt) {
            Item removedItem = evt.getChild();
            T childToRemove = findWabitItemWrapper(removedItem);
            children.remove(childToRemove);
            fireChildRemoved(getChildClass(), childToRemove, evt.getIndex());
        }

        public void containerChildAdded(ContainerChildEvent evt) {
            if (children.size() > evt.getIndex() && 
                    children.get(evt.getIndex()).getDelegate().equals(evt.getChild())) {
                //a child object was added for this event already from addChildImpl
                //and doesn't need to be added again.
                return;
            }
            T child = createWabitItemChild(evt.getChild());
            child.setParent(WabitContainer.this);
            children.add(child);
            fireChildAdded(getChildClass(), child, evt.getIndex());
        }

        /**
         * Finds the WabitItem that delegates to the the given item. If there is
         * no WabitItem that delegates to this item an
         * {@link IllegalStateException} is thrown as the WabitContainer is no
         * longer in sync with the container it is delegating to.
         * 
         * @param removedItem
         *            The item that has been removed from this object's
         *            delegate.
         * @return The {@link WabitItem} that delegates to the removed item.
         * @throws IllegalStateException
         *             if there is no {@link WabitItem} that wraps the given
         *             item.
         */
        private T findWabitItemWrapper(Item removedItem) {
            T childToRemove = null;
            for (T child : children) {
                if (child.getDelegate().equals(removedItem)) {
                    childToRemove = child;
                    break;
                }
            }
            if (childToRemove == null) 
                throw new IllegalStateException("The WabitContainer " + getName() + 
                        " has become out of sync with its delegate container " + 
                        delegate.getName());
            return childToRemove;
        }
    };
    
    /**
     * A listener on the delegate container that refires the property change
     * events with this object as its source.
     */
    private final PropertyChangeListener changeListener = new PropertyChangeListener() {
    
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
    public WabitContainer(Container delegate) {
    	this(delegate, true);
    }

	/**
	 * Creates a WabitObject that wraps any {@link Container} object.
	 * 
	 * @param delegate
	 *            The {@link Container} to wrap.
	 * @param createItemWrappers
	 *            If true the children of the container will be wrapped to
	 *            start. If false there will be no {@link WabitObject} children
	 *            to this container.
	 */
	public WabitContainer(Container delegate, boolean createItemWrappers) {
		super();
		this.delegate = delegate;
		delegate.addChildListener(containerChildListener);
		delegate.addPropertyChangeListener(changeListener);
		if (createItemWrappers) {
			for (Item i : delegate.getItems()) {
				T child = createWabitItemChild(i);
				child.setParent(this);
				children.add(child);
			}
		}
		setName(delegate.getName());
	}
	
	/**
	 * Creates a child of the type this class contains
	 * @param item The item to wrap in a WabitItem.
	 * @return A new WabitItem that is the correct sub-type for this class.
	 */
	protected abstract T createWabitItemChild(Item item);
	
	/**
	 * Returns the class type of the children this container contains.
	 */
	protected abstract Class<T> getChildClass();
	
	@Override
	public CleanupExceptions cleanup() {
		delegate.removePropertyChangeListener(changeListener);
		return new CleanupExceptions();
	}
	
	public Container getDelegate() {
	    return delegate;
	}

	@Override
	protected boolean removeChildImpl(SPObject child) {
	    Item item = ((WabitItem) child).getDelegate();
	    delegate.removeItem(item);
	    return true;
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
	    final WabitItem wabitItem = (WabitItem) child;
	    children.add(index, (T) child);
	    child.setParent(this);
	    fireChildAdded(child.getClass(), child, index);
	    
        Item item = wabitItem.getDelegate();
	    delegate.addItem(item, index);
	}

	public boolean allowsChildren() {
		return true;
	}
	
	public List<Class<? extends SPObject>> allowedChildTypes() {
		List<Class<? extends SPObject>> childTypes = new ArrayList<Class<? extends SPObject>>();
		childTypes.add(getChildClass());
		return childTypes;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		if (!childType.equals(getChildClass())) throw new IllegalArgumentException("Only children of " + WabitItem.class + " are allowed in this class.");
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public List<WabitObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(SPObject dependency) {
	    //do nothing, no dependencies
	}
	
	//------------Container getters and setters-------------
	//TODO We will want to make this a correct delegate when we have time. For now we 
	//are just adding the getters and setters that fire events.

	public String getAlias() {
		return delegate.getAlias();
	}
	
	public void setAlias(String alias) {
		delegate.setAlias(alias);
	}
	
	public Point2D getPosition() {
		return delegate.getPosition();
	}
	
	public void setPosition(Point2D position) {
		delegate.setPosition(position);
	}
	
	//-----------End Container getters and setters----------
}
