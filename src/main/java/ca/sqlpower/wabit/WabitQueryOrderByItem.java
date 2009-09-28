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

import ca.sqlpower.query.Item;
import ca.sqlpower.query.QueryItem;

/**
 * This class is the exact same as {@link WabitQueryItem} but is subclassed to represent
 * wrapped {@link Item}s that are in the order by list of a {@link QueryCache}. This
 * lets the {@link QueryCache} give a different starting index for these children separate
 * from the {@link WabitQueryItem}.
 */
public class WabitQueryOrderByItem extends WabitQueryItem {

    public WabitQueryOrderByItem(QueryItem item) {
        super(item);
    }

}
