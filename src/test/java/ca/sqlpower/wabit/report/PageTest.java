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

import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Page.StandardPageSizes;

public class PageTest extends AbstractWabitObjectTest {

    private Page page;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        page = new Page("test page", StandardPageSizes.US_LETTER);
    }
    
    @Override
    public Set<String> getPropertiesToIgnoreForEvents() {
        Set<String> ignore = super.getPropertiesToIgnoreForEvents();
        ignore.add("fontMetrics"); // this just depends on the font
        return ignore;
    }
    @Override
    public WabitObject getObjectUnderTest() {
        return page;
    }
}
