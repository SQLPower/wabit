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

package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.report.Layout;

public class WabitProject extends AbstractWabitObject {

    /**
     * The data sources that feed the queries for this project.
     */
    private final List<WabitDataSource> dataSources = new ArrayList<WabitDataSource>();
    
    /**
     * The queries that fetch result sets for this project.
     * <p>
     * TODO an SQL query is too specific; we should have a generic result set provider
     * class that could be anything (XPath, SQL query, gdata query, JavaScript that builds a table of data, ...)
     */
    private final List<Query> queries = new ArrayList<Query>();
    
    /**
     * The report layouts in this project.
     */
    private final List<Layout> layouts = new ArrayList<Layout>();

    public List<WabitObject> getChildren() {
        List<WabitObject> allChildren = new ArrayList<WabitObject>();
        allChildren.addAll(dataSources);
        allChildren.addAll(queries);
        allChildren.addAll(layouts);
        return allChildren;
    }
    
    public void addDataSource(WabitDataSource ds) {
        int index = dataSources.size();
        dataSources.add(index, ds);
        fireChildAdded(WabitDataSource.class, ds, index);
    }

    public void addQuery(Query dp) {
        int index = queries.size();
        queries.add(index, dp);
        fireChildAdded(Query.class, dp, index);
    }

    public void addLayout(Layout layout) {
        int index = layouts.size();
        layouts.add(index, layout);
        fireChildAdded(Layout.class, layout, index);
    }
    
    public int childPositionOffset(Class<? extends WabitObject> childType) {
        int offset = 0;

        // TODO consider whether this should be instaceOf or strict equality
        if (childType == SPDataSource.class) return offset;
        offset += dataSources.size();

        if (childType == Query.class) return offset;
        offset += queries.size();
        
        if (childType == Layout.class) return offset;
        
        throw new IllegalArgumentException("Objects of this type don't have children of type " + childType);
    }
}
