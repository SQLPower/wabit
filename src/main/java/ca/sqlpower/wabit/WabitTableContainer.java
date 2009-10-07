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

package ca.sqlpower.wabit;

import ca.sqlpower.query.Container;
import ca.sqlpower.query.Item;
import ca.sqlpower.query.TableContainer;

/**
 * This class distinguishes a WabitContainer as more specifically containing a
 * {@link TableContainer}.
 */
public class WabitTableContainer extends WabitContainer<WabitColumnItem> {

    public WabitTableContainer(Container delegate) {
        super(delegate);
    }

    @Override
    protected WabitColumnItem createWabitItemChild(Item item) {
        return new WabitColumnItem(item);
    }

    @Override
    protected Class<WabitColumnItem> getChildClass() {
        return WabitColumnItem.class;
    }
    
    @Override
    protected void addChildImpl(WabitObject child, int index) {
        throw new IllegalStateException("Cannot add children to a table that was " +
        		"loaded from a data source.");
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        throw new IllegalStateException("Cannot remove children from a table that " +
        		"was loaded from a data source.");
    }
    
}
