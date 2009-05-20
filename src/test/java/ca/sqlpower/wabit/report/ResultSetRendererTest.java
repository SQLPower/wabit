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

package ca.sqlpower.wabit.report;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;

public class ResultSetRendererTest extends AbstractWabitObjectTest {

    private ResultSetRenderer renderer;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        renderer = new ResultSetRenderer(new QueryCache(new SQLDatabaseMapping() {
            public SQLDatabase getDatabase(SPDataSource ds) {
                return null;
            }
        }));
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return renderer;
    }

    
}
