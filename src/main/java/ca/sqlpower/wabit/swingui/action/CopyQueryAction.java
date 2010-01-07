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

import java.awt.Window;

import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.query.QueryCache;

/**
 * This method will copy a given query and add it to the same
 * workspace the first copy was in.
 */
public class CopyQueryAction extends CopyAction {

    private final WabitObject query;
    private final WabitSession session;

    public CopyQueryAction(WabitObject query, WabitSession session, Window dialogOwner) {
        super(query, dialogOwner);
        this.session = session;
        if (query instanceof QueryCache || query instanceof OlapQuery) {
        	this.query = query;
        } else {
        	throw new UnsupportedOperationException("Copy query action only works for items of type " + 
        			QueryCache.class.getName() + " and " + OlapQuery.class.getName() + " and not for " +
        			"items of type " + query.getClass().getName() + ".");
        }
    }
    
    public void copy(String name) {
    	if (query instanceof QueryCache) {
	        QueryCache newQuery = new QueryCache((QueryCache) query, true);
	        newQuery.setName(name);
	        session.getWorkspace().addQuery(newQuery, session);
    	} else if (query instanceof OlapQuery) {
    		OlapQuery olapQuery;
			try {
				olapQuery = OlapQuery.copyOlapQuery((OlapQuery) query);
			} catch (Exception e1) {
				throw new RuntimeException("Error copying query", e1);
			}
			olapQuery.setName(name);
    		session.getWorkspace().addOlapQuery(olapQuery);

    	}
    }

}
