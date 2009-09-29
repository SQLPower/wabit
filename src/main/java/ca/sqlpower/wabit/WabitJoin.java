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

import ca.sqlpower.query.Item;
import ca.sqlpower.query.SQLJoin;

/**
 * Wraps a {@link SQLJoin} and converts events on the join to {@link WabitListener}
 * events.
 */
public class WabitJoin extends AbstractWabitObject {
    
    /**
     * The object that is listened to for join events.
     */
    private final SQLJoin delegate;
    
    /**
     * Returns the {@link WabitItem} wrapping the left side of this join.
     */
    private final WabitColumnItem leftItem;
    
    /**
     * Returns the {@link WabitItem} wrapping the right side of this join.
     */
    private final WabitColumnItem rightItem;

    /**
     * A change listener on the delegate join that re-fires events as
     * {@link WabitObject} events.
     */
    private final PropertyChangeListener changeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };

    private final QueryCache query;

    /**
     * Constructs a WabitJoin that converts {@link SQLJoin} events to
     * {@link WabitListener} events.
     * 
     * @param query
     *            The {@link QueryCache} this WabitJoin will be a part of. This
     *            is passed in to find the appropriate {@link WabitItem} objects
     *            for the left and right columns of the given {@link SQLJoin}.
     *            If new {@link WabitItem}s are created here their UUIDs would
     *            not match the {@link WabitItem}s in the QueryCache and be
     *            considered different object.
     * @param delegate
     *            The {@link SQLJoin} to listen to.
     */
    public WabitJoin(QueryCache query, SQLJoin delegate) {
        this.query = query;
        this.delegate = delegate;
        delegate.addJoinChangeListener(changeListener);
        leftItem = findWabitItemByDelegate(query, delegate.getLeftColumn());
        rightItem = findWabitItemByDelegate(query, delegate.getRightColumn());
        setName(leftItem.getName() + " (" + leftItem.getParent().getName() + ") to " 
                + rightItem.getName() + " (" + rightItem.getParent().getName() + ")");
    }
    
    @Override
    public CleanupExceptions cleanup() {
    	delegate.removeJoinChangeListener(changeListener);
    	return new CleanupExceptions();
    }
    
    /**
     * Helper method for the constructor that finds the correct {@link WabitItem}
     * wrapper in the {@link QueryCache} that wraps the given delegate item.
     */
    private WabitColumnItem findWabitItemByDelegate(QueryCache query, Item delegate) {
        for (WabitObject child : query.getChildren()) {
            if (child instanceof WabitTableContainer) {
            	for (WabitObject item : child.getChildren()) {
            		if (((WabitColumnItem) item).getDelegate().equals(delegate)) {
            			return (WabitColumnItem) item;
            		}
            	}
            }
        }
        throw new IllegalStateException("The QueryCache is missing a WabitColumnItem for " + 
                delegate.getName());
    }
    
    public SQLJoin getDelegate() {
        return delegate;
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public List<WabitObject> getDependencies() {
        List<WabitObject> dependency = new ArrayList<WabitObject>();
        dependency.add(leftItem);
        dependency.add(rightItem);
        return dependency;
    }

    public void removeDependency(WabitObject dependency) {
        if (dependency.equals(leftItem) || dependency.equals(rightItem)) {
            query.removeJoin(delegate);
        }
    }

}
