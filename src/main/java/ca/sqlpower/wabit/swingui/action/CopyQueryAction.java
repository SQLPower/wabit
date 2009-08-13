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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.olap.OlapQuery;

/**
 * This method will copy a given query and add it to the same
 * workspace the first copy was in.
 */
public class CopyQueryAction extends AbstractAction {

    private final WabitObject query;
    private final WabitSession session;

    public CopyQueryAction(WabitSession session, WabitObject query) {
        super("Copy Query");
        this.session = session;
        if (query instanceof QueryCache || query instanceof OlapQuery) {
        	this.query = query;
        } else {
        	throw new UnsupportedOperationException("Copy query action only works for items of type " + 
        			QueryCache.class.getName() + " and " + OlapQuery.class.getName() + " and not for " +
        			"items of type " + query.getClass().getName() + ".");
        }
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (query instanceof QueryCache) {
	        QueryCache newQuery = new QueryCache((QueryCache) query, true);
	        newQuery.setName(newQuery.getName() + " Copy");
	        session.getWorkspace().addQuery(newQuery, session);
    	} else if (query instanceof OlapQuery) {
    		OlapQuery olapQuery;
			try {
				olapQuery = new OlapQuery((OlapQuery) query);
			} catch (Exception e1) {
				throw new RuntimeException("Error copying query", e1);
			}
			olapQuery.setName(query.getName() + " Copy");
    		session.getWorkspace().addOlapQuery(olapQuery);
    	}
    }

}
