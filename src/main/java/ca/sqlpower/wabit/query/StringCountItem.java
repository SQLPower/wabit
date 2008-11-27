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
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

/**
 * This a special class that does similar things as {@link StringItem}. It
 * listens to the Tables that are being added to the query and creates a
 * constant Item for doing count(*). This item should also be removed when no
 * tables exist in the query.
 */
public class StringCountItem extends StringItem implements Item {
	
	private static Logger logger = Logger.getLogger(StringCountItem.class);

	private final QueryCache queryCache;
	
	public StringCountItem(QueryCache query) {
		super("");
		this.queryCache = query;
		setName("COUNT(*)");
		
		queryCache.addPropertyChangeListener(new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(QueryCache.PROPERTY_TABLE_REMOVED)) {
					logger.debug(" Table has been removed, removing constant item");
					if(queryCache.getFromTableList().isEmpty()) {
						setName("");
					}
				} else if(evt.getPropertyName().equals(QueryCache.GROUPING_CHANGED)) {
					logger.debug("Grouping has changed to "+ evt.getNewValue());
					if(evt.getNewValue().equals(false) && isSelected()) {
						logger.debug("grouping is false, setting model and view's selected to false");
						setSelected(false);
						firePropertyChange(QueryCache.GROUPING_CHANGED,evt.getOldValue(), evt.getNewValue());
					}
				}
			}
		});
	}
	public boolean isGroupingEnabled() {
		return queryCache.isGroupingEnabled();
	}

}
