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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitChildEvent.EventType;

/**
 * Extend this class to add the behaviour to not respond to events when in a
 * transaction. Instead of responding to events while in a transaction the
 * listener will collect the events and then at the end of the transaction act
 * on each event in the order it was received. If a rollback event was received
 * the events will be discarded instead.
 */
public abstract class AbstractWabitListener implements WabitListener {

    /**
     * Tracks the objects this listener is attached to that are in the middle of
     * a transaction. If the map stores a number greater than 0 for a given
     * object it is in a transaction state. If the set does not contain the
     * object it is not in a transaction. This is a map as one instance of this
     * object could be attached to several objects as a hierarchy listener.
     */
    private final Map<WabitObject, Integer> inTransactionMap = 
        new HashMap<WabitObject, Integer>();

    /**
     * Tracks the e that occur while an object is in a transaction state. These
     * events will be acted on when the transaction ends or are removed when the
     * transaction rolls back. The events can be {@link PropertyChangeEvent}s or
     * {@link WabitChildEvent}s.
     */
    private final Map<WabitObject, List<Object>> eventMap = 
        new HashMap<WabitObject, List<Object>>();

    public final void transactionEnded(TransactionEvent e) {
        if (inTransactionMap.get(e.getSource()) == null) {
            throw new IllegalStateException("An end transaction for object " + e.getSource() 
                    + " of type " + e.getSource().getClass() + " was called while it was " +
                    		"not in a transaction.");
        }
        Integer nestedTransactionCount = inTransactionMap.get(e.getSource()) - 1;
        if (nestedTransactionCount < 0) {
            throw new IllegalStateException("The transaction count was not removed properly.");
        } else if (nestedTransactionCount > 0) {
            inTransactionMap.put((WabitObject) e.getSource(), nestedTransactionCount);
        } else {
            inTransactionMap.remove(e.getSource());
            if (eventMap.get(e.getSource()) != null) {
                for (Object evt : eventMap.get(e.getSource())) {
                    if (evt instanceof PropertyChangeEvent) {
                        propertyChangeImpl((PropertyChangeEvent) evt);
                    } else if (evt instanceof WabitChildEvent) {
                        WabitChildEvent childEvent = (WabitChildEvent) evt;
                        if (childEvent.getType().equals(EventType.ADDED)) {
                            wabitChildAddedImpl(childEvent);
                        } else if (childEvent.getType().equals(EventType.REMOVED)) {
                            wabitChildRemovedImpl(childEvent);
                        } else {
                            throw new IllegalStateException("Unknown wabit child event of type " + childEvent.getType());
                        }
                    } else {
                        throw new IllegalStateException("Unknown event type " + evt.getClass());
                    }
                }
            }
            
            eventMap.remove(e.getSource());
        }
        transactionEndedImpl(e);
    }
    
    /**
     * Override this method if an action is required when a transaction ends.
     * This will be called when any transactionEnded event is fired, even if
     * it is the end of a transaction that is contained in another transaction.
     */
    protected void transactionEndedImpl(TransactionEvent e) {
        //meant to be overridden by classes extending this listener
    }

    public final void transactionRollback(TransactionEvent e) {
        inTransactionMap.remove(e.getSource());
        eventMap.remove(e.getSource());
        transactionRollbackImpl(e);
    }
    
    /**
     * Override this method if an action is required when a transaction rolls back.
     */
    protected void transactionRollbackImpl(TransactionEvent e) {
        //meant to be overridden by classes extending this listener
    }

    public final void transactionStarted(TransactionEvent e) {
        Integer transactionCount = inTransactionMap.get(e.getSource());
        if (transactionCount == null) {
            inTransactionMap.put((WabitObject) e.getSource(), 1);
        } else {
            inTransactionMap.put((WabitObject) e.getSource(), transactionCount++);
        }
        transactionStartedImpl(e);
    }
    
    /**
     * Override this method if an action is required when a transaction starts.
     * This will be called when any transactionStarted event is fired, even if
     * it is the start of a transaction that is contained in another transaction.
     */
    protected void transactionStartedImpl(TransactionEvent e) {
        //meant to be overridden by classes extending this listener
    }

    public final void wabitChildAdded(WabitChildEvent e) {
        if (inTransactionMap.get(e.getSource()) != null 
                && inTransactionMap.get(e.getSource()) > 0) {
            List<Object> events = eventMap.get(e.getSource());
            if (events == null) {
                events = new ArrayList<Object>();
                eventMap.put(e.getSource(), events);
            }
            events.add(e);
        } else {
            wabitChildAddedImpl(e);
        }
    }
    
    /**
     * Override this method if an action is required when a child added event is
     * acted upon.
     */
    protected void wabitChildAddedImpl(WabitChildEvent e) {
        //meant to be overridden by classes extending this listener
    }

    public final void wabitChildRemoved(WabitChildEvent e) {
        if (inTransactionMap.get(e.getSource()) != null 
                && inTransactionMap.get(e.getSource()) > 0) {
            List<Object> events = eventMap.get(e.getSource());
            if (events == null) {
                events = new ArrayList<Object>();
                eventMap.put(e.getSource(), events);
            }
            events.add(e);
        } else {
            wabitChildRemovedImpl(e);
        }
    }
    
    /**
     * Override this method if an action is required when a child removed event is
     * acted upon.
     */
    protected void wabitChildRemovedImpl(WabitChildEvent e) {
        //meant to be overridden by classes extending this listener
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        if (inTransactionMap.get(evt.getSource()) != null 
                && inTransactionMap.get(evt.getSource()) > 0) {
            List<Object> events = eventMap.get(evt.getSource());
            if (events == null) {
                events = new ArrayList<Object>();
                eventMap.put((WabitObject) evt.getSource(), events);
            }
            events.add(evt);
        } else {
            propertyChangeImpl(evt);
        }
    }
    
    /**
     * Override this method if an action is required when a property change event is
     * acted upon.
     */
    protected void propertyChangeImpl(PropertyChangeEvent evt) {
        //meant to be overridden by classes extending this listener
    }

}
