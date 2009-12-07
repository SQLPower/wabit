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

package ca.sqlpower.wabit.rs.query;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.Query;
import ca.sqlpower.query.StringItem;

/**
 * This class represents a specific implementation of {@link WabitContainer} that
 * wraps the constants container in a {@link Query}.
 */
public class WabitConstantsContainer extends WabitContainer<WabitConstantItem> {

    public WabitConstantsContainer(Container delegate) {
        super(delegate);
    }
    
    public WabitConstantsContainer(Container delegate, boolean createItemWrappers) {
    	super(delegate, createItemWrappers);
    }

    @Override
    protected WabitConstantItem createWabitItemChild(Item item) {
    	if (!(item instanceof StringItem)) {
    		throw new IllegalArgumentException("The item to add to " + getClass() + " with" +
    				" name \"" + getName() + "\" and UUID + \"" + getUUID() + "\" must" +
    						" be of type " + StringItem.class.getName() + ", not " + 
    						item.getClass() + ".");
    	}
        return new WabitConstantItem((StringItem) item);
    }

    @Override
    protected Class<WabitConstantItem> getChildClass() {
        return WabitConstantItem.class;
    }

	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
		types.add(WabitConstantItem.class);
		return types;
	}

}
