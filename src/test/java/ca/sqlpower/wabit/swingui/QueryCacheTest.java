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

package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;

import junit.framework.TestCase;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryChangeAdapter;
import ca.sqlpower.query.StringItem;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.StubWabitSessionContext;

public class QueryCacheTest extends TestCase {
	
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
	}
	
	public void testAliasListener() throws Exception {
		Item item = new StringItem("ItemName");
		queryCache.getQuery().addItem(item);
		item.setSelected(true);
		CountingQueryChangeListener listener = new CountingQueryChangeListener();
		queryCache.getQuery().addQueryChangeListener(listener);
		String newAlias = "Alias test.";
		item.setAlias(newAlias);
		assertEquals(1, listener.getChangeCount());
	}
	
}
