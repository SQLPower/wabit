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
import ca.sqlpower.wabit.swingui.querypen.UnmodifiableItemPNode;
import ca.sqlpower.wabit.swingui.querypen.QueryPen;

public class QueryCacheTest extends TestCase {
	
	private QueryCache queryCache;
	private QueryPen pen;
	
	protected void setUp() throws Exception {
		super.setUp();
		pen = new QueryPen(new StubWabitSwingSession());
		queryCache = new QueryCache(pen);
	}
	
	public void testSelectListener() throws Exception {
		Item item = new StubItem();
		UnmodifiableItemPNode node = new UnmodifiableItemPNode(pen, pen.getCanvas(), item);
		queryCache.selectionChanged(node, true);
		assertTrue(queryCache.getSelectedColumns().contains(item));
		queryCache.selectionChanged(node, false);
		assertTrue(!queryCache.getSelectedColumns().contains(item));
	}
	
	public void testAliasListener() throws Exception {
		Item item = new StubItem();
		UnmodifiableItemPNode node = new UnmodifiableItemPNode(pen, pen.getCanvas(), item);
		String newAlias = "Alias test.";
		node.setAlias(newAlias);
		queryCache.aliasChanged(node);
		assertTrue(queryCache.getAliasList().get(item).equals(newAlias));
		node.setAlias("");
		queryCache.aliasChanged(node);
		assertNull(queryCache.getAliasList().get(item));
	}

}
