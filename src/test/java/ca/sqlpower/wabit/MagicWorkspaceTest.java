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

import java.io.File;

import junit.framework.TestCase;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.rs.query.QueryCache;

/**
 * A collection of tests for different objects that ensure the correct events and actions
 * are done when magic is disabled.
 */
public class MagicWorkspaceTest extends TestCase {
	
    private PlDotIni plIni;

	@Override
    protected void setUp() throws Exception {
    	super.setUp();
    	plIni = new PlDotIni();
    	plIni.read(new File("src/test/java/pl.regression.ini"));
    }

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
	
	/**
	 * Setting the data source of a {@link QueryCache} changes the streaming
	 * flag normally. If magic is disabled the streaming flag should not change.
	 */
	public void testSettingDSDoesNotChangeStreaming() throws Exception {
		WabitSessionContext context = new WabitSessionContextImpl(
				true, false, plIni, null, false); 
		WabitSession session = new WabitSessionImpl(context);
		WabitWorkspace workspace = new WabitWorkspace();
		
		workspace.setMagicDisabled(true);
		
		QueryCache query = new QueryCache(context);
		workspace.addQuery(query, session);
		
		JDBCDataSource ds = plIni.getDataSource("regression_test", JDBCDataSource.class);
		boolean streaming = ds.getParentType().getSupportsStreamQueries();
		query.setStreaming(!streaming);
		
		query.setDataSource(ds);
		
		assertEquals(!streaming, query.isStreaming());
	}
	
	/**
	 * If magic is disabled and the content renderer of a content box is changed
	 * the name of the content box should remain the same as before.
	 */
	public void testSettingRendererDoesNotChangeName() throws Exception {
		WabitSessionContext context = new WabitSessionContextImpl(
				true, false, plIni, null, false); 
		WabitSession session = new WabitSessionImpl(context);
		WabitWorkspace workspace = session.getWorkspace();
		
		Report report = new Report("report");
		workspace.addReport(report);
		
		Page page = report.getPage();
		
		ContentBox box = new ContentBox();
		page.addContentBox(box);
		ImageRenderer renderer1 = new ImageRenderer();
		renderer1.setName("renderer1");
		box.setContentRenderer(renderer1);
		
		ImageRenderer renderer2 = new ImageRenderer();
		renderer2.setName("renderer2");
		String boxName = box.getName();
		
		workspace.setMagicDisabled(true);
		box.setContentRenderer(renderer2);
		
		assertEquals(boxName, box.getName());
		
	}
}
