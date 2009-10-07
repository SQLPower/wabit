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

package ca.sqlpower.wabit.query;

import java.beans.PropertyChangeEvent;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.ItemContainer;
import ca.sqlpower.query.QueryChangeAdapter;
import ca.sqlpower.query.SQLJoin;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.CountingWabitListener;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitConstantsContainer;
import ca.sqlpower.wabit.WabitContainer;
import ca.sqlpower.wabit.WabitItem;
import ca.sqlpower.wabit.WabitJoin;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitTableContainer;

public class QueryCacheTest extends AbstractWabitObjectTest {
	
	private class CountingQueryChangeListener extends QueryChangeAdapter {
	    
	    private int changeCount = 0;
	    
	    @Override
	    public void itemPropertyChangeEvent(PropertyChangeEvent evt) {
	        changeCount++;
	    }
	    
	    public int getChangeCount() {
            return changeCount;
        }
	}
	
	private QueryCache queryCache;
	
	protected void setUp() throws Exception {
		super.setUp();
		queryCache = new QueryCache(new StubWabitSessionContext());
		queryCache.setName("Main query");
	}
	
	@Override
	public WabitObject getObjectUnderTest() {
	    return queryCache;
	}
	
	public void testAliasListener() throws Exception {
		Item item = new StringItem("ItemName");
		queryCache.addItem(item);
		queryCache.selectItem(item);
		CountingQueryChangeListener listener = new CountingQueryChangeListener();
		queryCache.addQueryChangeListener(listener);
		String newAlias = "Alias test.";
		item.setAlias(newAlias);
		assertEquals(1, listener.getChangeCount());
	}

	/**
	 * Tests adding a {@link Container} to a query cache adds a corresponding
	 * {@link WabitContainer} and its contained {@link Item}s as
	 * {@link WabitItem}s.
	 */
	public void testWabitContainerAdded() throws Exception {
		Item item = new StringItem("ItemName");
		Container container = new ItemContainer("Container");
		container.addItem(item);
		item.setSelected(null);
		
		CountingWabitListener listener = new CountingWabitListener();
		queryCache.addWabitListener(listener);
		queryCache.addTable(container);
		
		assertEquals(1, listener.getAddedCount());
		assertEquals(2, queryCache.getChildren().size());
		WabitObject queryChild = queryCache.getChildren().get(1);
		assertEquals(WabitTableContainer.class, queryChild.getClass());
		WabitChildEvent evt = listener.getLastEvent();
		assertEquals(queryCache, evt.getSource());
		assertEquals(queryChild, evt.getChild());
		assertEquals(1, evt.getIndex());
		assertEquals(container, ((WabitTableContainer) queryChild).getDelegate());
	}
	
	/**
	 * Tests that when a container is remove to a query an appropriate wabit object
	 * is removed from the query and an event is fired appropriately.
	 */
	public void testContainerRemovedFiresEvents() throws Exception {
	    Item item = new StringItem("ItemName");
        Container container = new ItemContainer("Container");
        container.addItem(item);
        item.setSelected(null);
        
        CountingWabitListener listener = new CountingWabitListener();
        queryCache.addWabitListener(listener);
        queryCache.addTable(container);
        
        assertEquals(1, listener.getAddedCount());
        assertEquals(2, queryCache.getChildren().size());
        WabitObject queryChild = queryCache.getChildren().get(1);
        assertEquals(WabitTableContainer.class, queryChild.getClass());
        
        queryCache.removeTable(container);
        
        assertEquals(1, listener.getRemovedCount());
        assertEquals(1, queryCache.getChildren().size());
        WabitChildEvent evt = listener.getLastEvent();
        assertEquals(queryCache, evt.getSource());
        assertEquals(container, ((WabitTableContainer) evt.getChild()).getDelegate());
        assertEquals(1, evt.getIndex());
    }
	
	
    /**
     * Tests the addChild and removeChild methods on an OlapQuery can
     * add and remove a container successfully.
     */
    public void testAddAndRemoveContainer() throws Exception {
        Container tableContainer = new ItemContainer("container");
        Item item1 = new StringItem("item1");
        Item item2 = new StringItem("item2");
        tableContainer.addItem(item1);
        tableContainer.addItem(item2);
        WabitTableContainer container = new WabitTableContainer(tableContainer);
        
        queryCache.addChild(container, queryCache.childPositionOffset(container.getClass()));
        
        assertTrue(queryCache.getFromTableList().contains(container.getDelegate()));
        
        queryCache.removeChild(container);
        
        assertFalse(queryCache.getFromTableList().contains(container.getDelegate()));
    }
    
    public void testAddAndRemoveJoin() throws Exception {
        Container tableContainer = new ItemContainer("container");
        Item item1 = new StringItem("item1");
        Item item2 = new StringItem("item2");
        tableContainer.addItem(item1);
        tableContainer.addItem(item2);
        queryCache.addTable(tableContainer);
        Container tableContainer2 = new ItemContainer("container2");
        Item item3 = new StringItem("item3");
        Item item4 = new StringItem("item4");
        tableContainer2.addItem(item3);
        tableContainer2.addItem(item4);
        queryCache.addTable(tableContainer2);
        SQLJoin join = new SQLJoin(item1, item3);
        
        WabitJoin wabitJoin = new WabitJoin(queryCache, join);
        
        assertFalse(queryCache.getJoins().contains(join));
        
        queryCache.addChild(wabitJoin, queryCache.childPositionOffset(wabitJoin.getClass()));
        
        assertTrue(queryCache.getJoins().contains(join));
        
        queryCache.removeChild(wabitJoin);
        
        assertFalse(queryCache.getJoins().contains(join));
        
    }

}
