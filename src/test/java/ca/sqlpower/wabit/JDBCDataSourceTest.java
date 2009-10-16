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

import java.io.File;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;

public class JDBCDataSourceTest extends AbstractWabitObjectTest {

    private WabitDataSource ds;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PlDotIni plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        JDBCDataSource spds = plIni.getDataSource("regression_test", JDBCDataSource.class);
        ds = new WabitDataSource(spds);
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return ds;
    }
}
