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

package ca.sqlpower.wabit.swingui.olap.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.olap4j.OlapException;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;

/**
 * An abstract base action meant to be extended by actions that modify
 * the query.
 * <p>
 * There's a specialized subclass, {@link MemberAction}, for actions that
 * work with a specific member of a query. 
 */
public abstract class OlapQueryAction extends AbstractAction {

    /**
     * The {@link OlapQuery} object that this action modifies.
     */
    private final OlapQuery query;
    
    protected OlapQueryAction(OlapQuery query, String name) {
        super(name);
        this.query = query;
        setEnabled(query != null);
    }
    
    /**
     * Returns the {@link OlapQuery} object that this action modifies.
     */
    public OlapQuery getQuery() {
        return query;
    }
    
    public final void actionPerformed(ActionEvent e) {
    	try {
    		performOlapQueryAction(query);
    	} catch (Exception ex) {
    		throw new RuntimeException(ex);
    	}
    }
    
    protected abstract void performOlapQueryAction(OlapQuery query) throws OlapException, QueryInitializationException;
}
