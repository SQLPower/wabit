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
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.swingui.querypen.QueryPen;

/**
 * This is the controller between the QueryCache and the QueryPen.
 * It will pass relevant events from the QueryPen to the QueryCache
 * like Containers being added and removed.
 */
public class QueryController {
	
	private static final Logger logger = Logger.getLogger(QueryController.class);

	private final PropertyChangeListener fromChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(Container.PROPERTY_TABLE_ADDED)) {
				queryCache.addTable((Container)evt.getNewValue());
			} else if (evt.getPropertyName().equals(Container.PROPERTY_TABLE_REMOVED)) {
				queryCache.removeTable((Container)evt.getOldValue());
			}
		}
	};
	
	private final PropertyChangeListener tableItemListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(Container.CONTAINTER_ITEM_ADDED)) {
				Item item = (Item)evt.getNewValue();
				queryCache.addItem(item);
				logger.debug("Item " + item + " added to the query cache");
			} else if (evt.getPropertyName().equals(Container.CONTAINER_ITEM_REMOVED)) {
				Item item = (Item)evt.getOldValue();
				queryCache.removeItem(item);
			}
		}
	};

	private final PropertyChangeListener joinChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(SQLJoin.PROPERTY_JOIN_ADDED)) {
				queryCache.addJoin((SQLJoin)e.getNewValue());
			} else if (e.getPropertyName().equals(SQLJoin.PROPERTY_JOIN_REMOVED)) {
				SQLJoin joinLine = (SQLJoin) e.getOldValue();
				queryCache.removeJoin(joinLine);
			}
		}
	};
	
	private final PropertyChangeListener whereListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals(Container.PROPERTY_WHERE_MODIFIED)) {
				queryCache.setGlobalWhereClause((String)e.getNewValue());
			}
		}
	};
	
	private final QueryCache queryCache;
	
	public QueryController(QueryCache cache, QueryPen pen) {
		queryCache = cache;
		pen.addQueryListener(fromChangeListener);
		pen.addQueryListener(joinChangeListener);
		pen.addQueryListener(whereListener);
		pen.addQueryListener(tableItemListener);
	}
	
}
