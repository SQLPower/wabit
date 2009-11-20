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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Join;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wraps a {@link SQLJoin} and converts events on the join to {@link SPListener}
 * events.
 */
public class WabitJoin extends AbstractWabitObject implements Join {
    
	private static final Logger logger = Logger.getLogger(WabitJoin.class);
	
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
	 * These are the property change listeners that listen directly to the
	 * {@link SQLJoin} this object delegates to. The events that get fired to
	 * this listener should have this object as the source instead of the
	 * delegate.
	 */
    private final List<PropertyChangeListener> joinListeners = new ArrayList<PropertyChangeListener>();

    /**
     * A change listener on the delegate join that re-fires events as
     * {@link WabitObject} events.
     */
    private final PropertyChangeListener changeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	firePropertyChangeEvent(evt);
        }
    };

    private final QueryCache query;

    /**
     * Constructs a WabitJoin that converts {@link SQLJoin} events to
     * {@link SPListener} events.
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
    
    protected void firePropertyChangeEvent(final PropertyChangeEvent evt) {
    	final PropertyChangeEvent newEvent = new PropertyChangeEvent(this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        runInForeground(new Runnable() {
            public void run() {
            	synchronized (joinListeners) {
            		for (int i = joinListeners.size()-1; i >= 0; i--) {
            			joinListeners.get(i).propertyChange(newEvent);
            		}
				}
            }
        });
        super.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	}

	@Override
    public CleanupExceptions cleanup() {
    	delegate.removeJoinChangeListener(changeListener);
    	return new CleanupExceptions();
    }
    
    /**
     * XXX The query may be equivalent to the parent. If this is the case the query
     * variable should be merged with the parent.
     */
    public QueryCache getQuery() {
		return query;
	}
    
    /**
     * Helper method for the constructor that finds the correct {@link WabitItem}
     * wrapper in the {@link QueryCache} that wraps the given delegate item.
     */
    private WabitColumnItem findWabitItemByDelegate(QueryCache query, Item delegate) {
        for (WabitObject child : query.getChildren()) {
            if (child instanceof WabitTableContainer) {
            	for (WabitColumnItem item : child.getChildren(WabitColumnItem.class)) {
            		if (item.getDelegate().equals(delegate)) {
            			return item;
            		}
            	}
            }
        }
        throw new IllegalStateException("The QueryCache is missing a WabitColumnItem for " + 
                delegate.getName());
    }

	/**
	 * Returns the {@link SQLJoin} delegate of this object.
	 * XXX This method should be removed in the future to make its delegate a proper delegate.
	 */
    public SQLJoin getDelegate() {
        return delegate;
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

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public List<WabitObject> getDependencies() {
        List<WabitObject> dependency = new ArrayList<WabitObject>();
        dependency.add(leftItem);
        dependency.add(rightItem);
        return dependency;
    }

    public void removeDependency(SPObject dependency) {
        if (dependency.equals(leftItem) || dependency.equals(rightItem)) {
            query.removeJoin(delegate);
        }
    }

    //-----------SQLJoin getters and setters----------------
    //TODO make this a full delegator when we have spare time.
    
    public void setLeftColumnOuterJoin(boolean isOuterJoin) {
    	delegate.setLeftColumnOuterJoin(isOuterJoin);
    }
    
    public boolean isLeftColumnOuterJoin() {
    	return delegate.isLeftColumnOuterJoin();
    }
    
    public void setRightColumnOuterJoin(boolean isOuterJoin) {
    	delegate.setRightColumnOuterJoin(isOuterJoin);
    }
    
    public boolean isRightColumnOuterJoin() {
    	return delegate.isRightColumnOuterJoin();
    }
    
    public void setComparator(String comparator) {
    	delegate.setComparator(comparator);
    }
    
    public String getComparator() {
    	return delegate.getComparator();
    }

	public void addJoinChangeListener(PropertyChangeListener l) {
		synchronized(joinListeners) {
			joinListeners.add(l);
		}
	}

	public Item getLeftColumn() {
		return delegate.getLeftColumn();
	}

	public Item getRightColumn() {
		return delegate.getRightColumn();
	}

    /**
     * This remove all the listeners inside this object's delegate to ensure that
     * listeners do not remain attached to the join when it is removed. Used for
     * deleting a join.
     * <p>
     * XXX This should be removed and objects adding listeners to a join should
     * remove the joins appropriately when they are not needed.
     */
	public void removeAllListeners() {
		delegate.removeAllListeners();
		joinListeners.clear();
	}

	public void removeJoinChangeListener(PropertyChangeListener l) {
		synchronized(joinListeners) {
			joinListeners.remove(l);
		}
	}
    
    //-----------End SQLJoin getters and setters -----------
}
