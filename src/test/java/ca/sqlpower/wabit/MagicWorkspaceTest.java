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

import junit.framework.TestCase;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Guide.Axis;

/**
 * A collection of tests for different objects that ensure the correct events and actions
 * are done when magic is disabled.
 */
public class MagicWorkspaceTest extends TestCase {

	/**
	 * Tests that if there is a content box attached to a guide if you move the guide
	 * with magic disabled the content box will not be moved.
	 */
	public void testMovingGuidesWithBoxesIgnoresBoxes() throws Exception {
		WabitWorkspace workspace = new WabitWorkspace();
		
		Report report = new Report("Report");
		workspace.addReport(report);
		
		Guide guide = null;
		for (WabitObject o : report.getPage().getChildren()) {
			if (o instanceof Guide && ((Guide) o).getAxis().equals(Axis.VERTICAL)) {
				guide = (Guide) o;
			}
		}
		double initialOffset = guide.getOffset();
		
		ContentBox box = new ContentBox();
		box.setX(initialOffset);
		report.getPage().addContentBox(box);
		
		guide.setOffset(initialOffset + 10);
		
		assertEquals(initialOffset + 10, box.getX());
		
		workspace.setMagicDisabled(true);
		
		guide.setOffset(guide.getOffset() + 10);
		
		assertEquals(initialOffset + 10, box.getX());
	}
}
