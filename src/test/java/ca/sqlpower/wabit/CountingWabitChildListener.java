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

/**
 * A listener implementation that's useful for building unit tests.
 */
public class CountingWabitChildListener implements WabitChildListener {

    private int addedCount;
    private int removedCount;
    
    private WabitChildEvent lastEvent;
    
    /**
     * Counts this added event and keeps a reference to the event object.
     */
    public void wabitChildAdded(WabitChildEvent e) {
        addedCount++;
        lastEvent = e;
    }

    /**
     * Counts this removed event and keeps a reference to the event object.
     */
    public void wabitChildRemoved(WabitChildEvent e) {
        removedCount++;
        lastEvent = e;
    }

    /**
     * Returns the most recent add or remove event received by this listener.
     */
    public WabitChildEvent getLastEvent() {
        return lastEvent;
    }
    
    /**
     * Returns the number of times {@link #wabitChildAdded(WabitChildEvent)}
     * has been called on this listener.
     */
    public int getAddedCount() {
        return addedCount;
    }
    
    /**
     * Returns the number of times {@link #wabitChildRemoved(WabitChildEvent)}
     * has been called on this listener.
     */
    public int getRemovedCount() {
        return removedCount;
    }
}
