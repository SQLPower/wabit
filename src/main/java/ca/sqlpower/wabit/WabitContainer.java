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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.ContainerChildEvent;
import ca.sqlpower.query.ContainerChildListener;
import ca.sqlpower.query.Item;

/**
 * This container wraps any other kind of Container to allow attaching
 * {@link WabitListener}s that will be notified appropriately when events happen
 * on the container.
 */
public class WabitContainer extends AbstractWabitObject {

    /**
     * The container that is delegated to by this object.
     */
	private final Container delegate;
	
	/**
	 * This is a list of WabitItems that mimics the items in the delegate.
	 * Items should be added to and removed from this list as they are added
	 * to and removed from the delegate. These children will also be returned
	 * instead of the delegate's children when they are requested. The children
	 * of the delegate contain a parent pointer to the delegate and the WabitItems
	 * point to this container instead.
	 */
	private final List<WabitItem> children = new ArrayList<WabitItem>();

    /**
     * A listener on the delegate container which refires the child events with
     * this object as its source.
     */
	private final ContainerChildListener containerChildListener = new ContainerChildListener() {
    
        public void containerChildRemoved(ContainerChildEvent evt) {
            Item removedItem = evt.getChild();
            WabitItem childToRemove = findWabitItemWrapper(removedItem);
            children.remove(childToRemove);
            fireChildRemoved(WabitItem.class, childToRemove, evt.getIndex());
        }

        public void containerChildAdded(ContainerChildEvent evt) {
            WabitItem child = new WabitItem(evt.getChild());
            child.setParent(WabitContainer.this);
            children.add(child);
            fireChildAdded(WabitItem.class, child, evt.getIndex());
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
        private WabitItem findWabitItemWrapper(Item removedItem) {
            WabitItem childToRemove = null;
            for (WabitItem child : children) {
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
		super();
		this.delegate = delegate;
		delegate.addChildListener(containerChildListener);
		delegate.addPropertyChangeListener(changeListener);
		for (Item i : delegate.getItems()) {
		    WabitItem child = new WabitItem(i);
		    child.setParent(this);
			children.add(child);
		}
		setName(delegate.getName());
	}
	
	@Override
	public CleanupExceptions cleanup() {
		delegate.removePropertyChangeListener(changeListener);
		return new CleanupExceptions();
	}
	
	public Container getDelegate() {
	    return delegate;
	}

	@Override
	protected boolean removeChildImpl(WabitObject child) {
	    Item item = ((WabitItem) child).getDelegate();
	    delegate.removeItem(item);
	    return true;
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		if (!childType.equals(WabitItem.class)) throw new IllegalArgumentException("Only children of " + WabitItem.class + " are allowed in this class.");
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public List<WabitObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(WabitObject dependency) {
	    //do nothing, no dependencies
	}

}
