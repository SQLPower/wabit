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

package ca.sqlpower.wabit;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.TransactionEvent;

/**
 * A listener implementation that's useful for building unit tests.
 */
public class CountingWabitListener implements SPListener {
	
	private static final Logger logger = Logger.getLogger(CountingWabitListener.class);

    private int addedCount;
    private int removedCount;
    private int propertyChangeCount;
    private int transactionStartCount;
    private int transactionEndCount;
    private int transactionRollbackCount;
    
    private SPChildEvent lastEvent;
    private PropertyChangeEvent lastPropertyEvent;
    private TransactionEvent lastTransactionEvent;
    
    /**
     * Counts this added event and keeps a reference to the event object.
     */
    public void childAdded(SPChildEvent e) {
        addedCount++;
        lastEvent = e;
    }

    /**
     * Counts this removed event and keeps a reference to the event object.
     */
    public void childRemoved(SPChildEvent e) {
        removedCount++;
        lastEvent = e;
    }

    /**
     * Returns the most recent add or remove event received by this listener.
     */
    public SPChildEvent getLastEvent() {
        return lastEvent;
    }
    
    /**
     * Returns the number of times {@link #childAdded(WabitChildEvent)}
     * has been called on this listener.
     */
    public int getAddedCount() {
        return addedCount;
    }
    
    /**
     * Returns the number of times {@link #childRemoved(WabitChildEvent)}
     * has been called on this listener.
     */
    public int getRemovedCount() {
        return removedCount;
    }

    public void transactionEnded(TransactionEvent e) {
        transactionEndCount++;
        lastTransactionEvent = e;
    }

    public void transactionStarted(TransactionEvent e) {
        transactionStartCount++;
        lastTransactionEvent = e;
    }

    public void propertyChanged(PropertyChangeEvent evt) {
    	logger.debug("Received event for " + evt.getPropertyName() + " on " + 
    			evt.getSource() + " to value " + evt.getNewValue());
        propertyChangeCount++;
        lastPropertyEvent = evt;
    }
    
    public int getPropertyChangeCount() {
        return propertyChangeCount;
    }
    
    public int getTransactionEndCount() {
        return transactionEndCount;
    }
    
    public int getTransactionStartCount() {
        return transactionStartCount;
    }
    
    public PropertyChangeEvent getLastPropertyEvent() {
        return lastPropertyEvent;
    }
    
    public TransactionEvent getLastTransactionEvent() {
        return lastTransactionEvent;
    }

    public void transactionRollback(TransactionEvent e) {
        transactionRollbackCount++;
        lastTransactionEvent = e;
    }
    
    public int getTransactionRollbackCount() {
        return transactionRollbackCount;
    }
}
