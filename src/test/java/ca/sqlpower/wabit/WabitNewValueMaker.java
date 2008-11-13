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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.GenericNewValueMaker;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.Guide.Axis;

public class WabitNewValueMaker extends GenericNewValueMaker {

    @Override
    public Object makeNewValue(Class<?> valueType, Object oldVal, String propName) {
        Object newValue;
        
        if (valueType.equals(WabitObject.class)) {
            newValue = new StubWabitObject();
        } else if (valueType.equals(WabitDataSource.class)) {
            newValue = new JDBCDataSource(new SPDataSource(new PlDotIni()));
        } else if (valueType.equals(Query.class)) {
            newValue = new StubQuery();
        } else if (valueType.equals(Layout.class)) {
            newValue = new Layout("testing layout");
        } else if (valueType.equals(ContentBox.class)) {
        	newValue = new ContentBox();
        } else if (valueType.equals(ReportContentRenderer.class)) {
        	newValue = createMock(ReportContentRenderer.class);
        	replay(newValue);
        } else if (valueType.equals(Guide.class)) {
            newValue = new Guide(Axis.HORIZONTAL);
        } else {
            return super.makeNewValue(valueType, oldVal, propName);
        }
        
        return newValue;
    }
}
