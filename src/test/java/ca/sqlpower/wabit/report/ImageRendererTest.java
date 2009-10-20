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

package ca.sqlpower.wabit.report;

import java.util.Set;

import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;

public class ImageRendererTest extends AbstractWabitObjectTest {
    
    private ImageRenderer renderer;
    private WabitImage image;
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignored = super.getPropertiesToNotPersistOnObjectPersist();
    	ignored.add("backgroundColour");
    	return ignored;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        image = new WabitImage();
        image.setName("Main image");
        renderer = new ImageRenderer();
        renderer.setName("Main renderer");
        renderer.setImage(image);
        
        ContentBox contentBox = new ContentBox();
        contentBox.setName("contentbox");
        contentBox.setContentRenderer(renderer);
        Report report = new Report("report");
        report.getPage().addContentBox(contentBox);
        
        getWorkspace().addReport(report);
    }

    @Override
    public WabitObject getObjectUnderTest() {
        return renderer;
    }

}
