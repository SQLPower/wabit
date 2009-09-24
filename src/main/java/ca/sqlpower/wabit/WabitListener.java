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

import java.beans.PropertyChangeListener;

import ca.sqlpower.util.TransactionEvent;

/**
 * This listener can be added to any {@link WabitObject}s and is notified of
 * property and child changes.
 */
public interface WabitListener extends PropertyChangeListener {

    /**
     * Called when a child is added to an object this listener is attached to.
     * 
     * @param e
     *            An event describing the child added.
     */
    void wabitChildAdded(WabitChildEvent e);
    /**
     * Called when a child is removed from an object this listener is attached to.
     * 
     * @param e
     *            An event describing the child removed.
     */
    void wabitChildRemoved(WabitChildEvent e);

    /**
     * Called when a transaction has started. The events fired after the
     * transaction started until the transaction has ended or rolled back should
     * be considered one atomic operation.
     * 
     * @param e
     *            Contains what object started a transaction and a message
     *            describing the transaction.
     */
    void transactionStarted(TransactionEvent e);

    /**
     * Signals a transaction has finished. The events that occurred during the
     * transaction should be considered one atomic operation. By the time the
     * transaction ends in this fashion or immediately after the atomic
     * operation should be acted upon. Some transactions may be nested inside of
     * other transactions. In this case the transaction is finished only when
     * the outer most transaction has completed.
     * 
     * @param e
     *            Contains the object that has finished a transaction.
     */
    void transactionEnded(TransactionEvent e);

    /**
     * Signals a transaction has finished and the events that occurred during
     * the transaction need to be reversed or not acted upon. If a transaction
     * inside of another transaction rolls back both the inner and outer
     * transaction will be rolled back.
     * 
     * @param e
     *            Contains the object that was rolled back and a message
     *            describing why the transaction was rolled back.
     */
    void transactionRollback(TransactionEvent e);
    
}
