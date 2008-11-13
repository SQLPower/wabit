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

import junit.framework.TestCase;
import ca.sqlpower.testutil.CountingPropertyChangeListener;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.StringItem;

public class QueryCacheTest extends TestCase {
	
	private QueryCache queryCache;
	
	protected void setUp() throws Exception {
		super.setUp();
		queryCache = new QueryCache();
	}
	
	public void testSelectListener() throws Exception {
		Item item = new StringItem("ItemName");
		queryCache.selectionChanged(item, true);
		assertTrue(queryCache.getSelectedColumns().contains(item));
		queryCache.selectionChanged(item, false);
		assertTrue(!queryCache.getSelectedColumns().contains(item));
	}
	
	public void testAliasListener() throws Exception {
		Item item = new StringItem("ItemName");
		queryCache.addItem(item);
		item.setSelected(true);
		CountingPropertyChangeListener listener = new CountingPropertyChangeListener();
		queryCache.addPropertyChangeListener(listener);
		String newAlias = "Alias test.";
		item.setAlias(newAlias);
		assertEquals(1, listener.getPropertyChangeCount());
	}

}
