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

package ca.sqlpower.object;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.StubWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class AbstractSPListenerTest extends TestCase {
    
    /**
     * This listener tracks the order events were handled in Wabit.
     */
    private static class ExecutionOrderWabitListener extends AbstractSPListener {

        /**
         * A list of events that were handled in the order they were handled by
         * this listener. Used to test that events are handled in the correct order
         * when in a transaction block.
         */
        private final List<Object> wabitEventsInOrder = new ArrayList<Object>();
        
        @Override
        protected void transactionStartedImpl(TransactionEvent e) {
            wabitEventsInOrder.add(e);
        }
        
        @Override
        protected void transactionEndedImpl(TransactionEvent e) {
            wabitEventsInOrder.add(e);
        }
        
        @Override
        protected void transactionRollbackImpl(TransactionEvent e) {
            wabitEventsInOrder.add(e);
        }
        
        @Override
        protected void propertyChangeImpl(PropertyChangeEvent evt) {
            wabitEventsInOrder.add(evt);
        }
        
        @Override
        protected void childAddedImpl(SPChildEvent e) {
            wabitEventsInOrder.add(e);
        }
        
        @Override
        protected void childRemovedImpl(SPChildEvent e) {
            wabitEventsInOrder.add(e);
        }
        
        public List<Object> getWabitEventsInOrder() {
            return Collections.unmodifiableList(wabitEventsInOrder);
        }
    }
    
    private final AbstractWabitObject wo = new AbstractWabitObject() {

        @Override
        protected boolean removeChildImpl(SPObject child) {
            return false;
        }

        public boolean allowsChildren() {
            return true;
        }

        public int childPositionOffset(
                Class<? extends SPObject> childType) {
            return 0;
        }

        public List<? extends WabitObject> getChildren() {
            return null;
        }

        public List<WabitObject> getDependencies() {
            return null;
        }

        public void removeDependency(SPObject dependency) {
            //do nothing
        }
        
        public List<Class<? extends SPObject>> getAllowedChildTypes() {
        	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
        	types.add(SPObject.class);
        	return types;
        }

    };

    /**
     * This tests that events fired.
     */
    public void testTransactionActsOnEvents() throws Exception {
        ExecutionOrderWabitListener listener = new ExecutionOrderWabitListener();
        wo.addSPListener(listener);
        SPChildEvent event1 = wo.fireChildAdded(WabitObject.class, new StubWabitObject(), 0);
        SPChildEvent event2 = wo.fireChildRemoved(WabitObject.class, new StubWabitObject(), 0);
        PropertyChangeEvent event3 = wo.firePropertyChange("dummyProperty", "oldValue", "newValue");
        
        List<Object> wabitEventsInOrder = listener.getWabitEventsInOrder();
        assertEquals(event1, wabitEventsInOrder.get(0));
        assertEquals(event2, wabitEventsInOrder.get(1));
        assertEquals(event3, wabitEventsInOrder.get(2));
    }
    
    /**
     * This tests that events are fired  while in a transaction are acted
     * on when the transaction completes successfully.
     */
    public void testTransactionEndActsOnEvents() throws Exception {
        ExecutionOrderWabitListener listener = new ExecutionOrderWabitListener();
        wo.addSPListener(listener);
        TransactionEvent event1 = wo.fireTransactionStarted("Start");
        PropertyChangeEvent event2 = wo.firePropertyChange("dummyProperty", "oldValue", "newValue");
        SPChildEvent event3 = wo.fireChildAdded(WabitObject.class, new StubWabitObject(), 0);
        SPChildEvent event4 = wo.fireChildRemoved(WabitObject.class, new StubWabitObject(), 0);
        PropertyChangeEvent event5 = wo.firePropertyChange("dummyProperty", "oldValue", "newValue");
        
        assertEquals(1, listener.getWabitEventsInOrder().size());
        
        TransactionEvent event6 = wo.fireTransactionEnded();
        
        List<Object> wabitEventsInOrder = listener.getWabitEventsInOrder();
        assertEquals(event1, wabitEventsInOrder.get(0));
        assertEquals(event2, wabitEventsInOrder.get(1));
        assertEquals(event3, wabitEventsInOrder.get(2));
        assertEquals(event4, wabitEventsInOrder.get(3));
        assertEquals(event5, wabitEventsInOrder.get(4));
        assertEquals(event6, wabitEventsInOrder.get(5));
    }
    
    public void testTransactionRollbackDoesNotActOnEvents() throws Exception {
        ExecutionOrderWabitListener listener = new ExecutionOrderWabitListener();
        wo.addSPListener(listener);
        TransactionEvent event1 = wo.fireTransactionStarted("Start");
        wo.firePropertyChange("dummyProperty", "oldValue", "newValue");
        wo.fireChildAdded(WabitObject.class, new StubWabitObject(), 0);
        wo.fireChildRemoved(WabitObject.class, new StubWabitObject(), 0);
        wo.firePropertyChange("dummyProperty", "oldValue", "newValue");
        
        assertEquals(1, listener.getWabitEventsInOrder().size());
        
        TransactionEvent event2 = wo.fireTransactionRollback("Rollback");
        
        assertEquals(2, listener.getWabitEventsInOrder().size());
        assertEquals(event1, listener.getWabitEventsInOrder().get(0));
        assertEquals(event2, listener.getWabitEventsInOrder().get(1));
    }
}
