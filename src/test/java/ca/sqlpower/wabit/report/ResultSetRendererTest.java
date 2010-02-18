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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.resultset.ResultSetCell;
import ca.sqlpower.wabit.rs.query.QueryCache;

public class ResultSetRendererTest extends AbstractWabitObjectTest {

    private ResultSetRenderer renderer;
    
    private Graphics graphics;
    
    private ContentBox parentCB;

	private QueryCache query;
    
    @Override
    public Class<? extends WabitObject> getParentClass() {
    	return ContentBox.class;
    }
    
    @Override
    public Set<String> getPropertiesToNotPersistOnObjectPersist() {
    	Set<String> ignorable = super.getPropertiesToNotPersistOnObjectPersist();
    	ignorable.add("colBeingDragged");
    	ignorable.add("columnInfoList");
    	return ignorable;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        this.query = new QueryCache(getContext());
        getWorkspace().addQuery(query, getSession());
        query.setDataSource((JDBCDataSource)getSession().getDataSources().getDataSource("regression_test"));
        
        getWorkspace().addChild(query, 0);
		renderer = new ResultSetRenderer(query);
        parentCB = new ContentBox();
        parentCB.setContentRenderer(renderer);
        
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        graphics = image.getGraphics();
        
        Report report = new Report("report");
        report.getPage().addContentBox(parentCB);
        getWorkspace().addReport(report);
    }
    
    @Override
    protected void tearDown() throws Exception {
        graphics.dispose();
        super.tearDown();
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return renderer;
    }
    
    /**
     * This is a test to confirm that subtotalling columns for breaks works.
     */
    public void testSubtotals() throws Exception {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getContext().createConnection((JDBCDataSource)getSession().getDataSources().getDataSource("regression_test"));
            stmt = con.createStatement();
            stmt.execute("Create table subtotal_table (break_col varchar(50), subtotal_values integer)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('a', 10)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('a', 20)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('a', 30)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('b', 12)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('b', 24)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 1)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 1)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 2)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 3)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 5)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 8)");
            stmt.execute("insert into subtotal_table (break_col, subtotal_values) values ('fib', 13)");
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        
        query.setUserModifiedQuery("select * from subtotal_table");

        Report report = new Report("report");
        getWorkspace().addReport(report);
        
        ContentBox cb = new ContentBox();
        report.getPage().addContentBox(cb);
        cb.setWidth(100);
        cb.setHeight(200);
        ResultSetRenderer renderer = new ResultSetRenderer(query);
        renderer.setParent(cb);
        renderer.refresh();
        
        Graphics2D contentGraphics = (Graphics2D) graphics.create(
                (int) cb.getX(), (int) cb.getY(),
                (int) cb.getWidth(), (int) cb.getHeight());
        
        renderer.renderReportContent(contentGraphics, cb, 1, 0, false, new SPVariableHelper(renderer));
        
        assertEquals(2, renderer.getColumnInfoList().size());
        renderer.getColumnInfoList().get(0).setWillGroupOrBreak(GroupAndBreak.GROUP);
        renderer.getColumnInfoList().get(1).setWillSubtotal(true);
        
        Font font = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[0];
        renderer.setHeaderFont(font);
        renderer.setBodyFont(font);
        
        renderer.renderReportContent(contentGraphics, cb, 1, 0, false, new SPVariableHelper(renderer));
        
        List<List<ResultSetCell>> layoutCells = renderer.findCells();
        
        boolean foundATotal = false;
        boolean foundBTotal = false;
        boolean foundFibTotal = false;
        for (List<ResultSetCell> cells : layoutCells) {
            for (ResultSetCell cell : cells) {
                if (cell.getText().equals("60")) {
                    foundATotal = true;
                } else if (cell.getText().equals("36")) {
                    foundBTotal = true;
                } else if (cell.getText().equals("33")) {
                    foundFibTotal = true;
                }
            }
        }
        if (!foundATotal) {
            fail("Could not find the correct subtotal cell for the A column. The cell should contain the value 60");
        } 
        if (!foundBTotal) {
            fail("Could not find the correct subtotal cell for the B column. The cell should contain the value 36");
        } 
        if (!foundFibTotal) {
            fail("Could not find the correct subtotal cell for the Fib column. The cell should contain the value 33");
        }
        
        con = null;
        stmt = null;
        try {
            con = getContext().createConnection((JDBCDataSource)getSession().getDataSources().getDataSource("regression_test"));
            stmt = con.createStatement();
            stmt.execute("drop table subtotal_table");
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        
    }

    /**
     * This is a test to confirm that the correct column is being selected to
     * have it's width changed when dragging.
     */
    public void testColSelectedForDrag() throws Exception {
        List<ColumnInfo> ciList = new ArrayList<ColumnInfo>();
        ColumnInfo ci1 = new ColumnInfo("Col1");
        ci1.setWidth(50);
        ciList.add(ci1);
        ColumnInfo ci2 = new ColumnInfo("Col2");
        ci2.setWidth(50);
        ciList.add(ci2);
        ColumnInfo ci3 = new ColumnInfo("Col3");
        ci3.setWidth(50);
        ciList.add(ci3);
        ResultSetRenderer rsRenderer = new ResultSetRenderer(query, ciList);
        
        assertNull(rsRenderer.getColBeingDragged());
        
        rsRenderer.defineColumnBeingDragged(50);
        assertEquals(ci1, rsRenderer.getColBeingDragged());
        
        rsRenderer.defineColumnBeingDragged(99); //check that you don't have to be 100% accurate.
        assertEquals(ci2, rsRenderer.getColBeingDragged());
        
        rsRenderer.defineColumnBeingDragged(75);
        assertNull(rsRenderer.getColBeingDragged());
    }
    
    /**
     * Test to confirm that a column can be dragged.
     */
    public void testColCanBeDragged() throws Exception {
        List<ColumnInfo> ciList = new ArrayList<ColumnInfo>();
        ColumnInfo ci1 = new ColumnInfo("Col1");
        ci1.setWidth(50);
        ciList.add(ci1);
        ColumnInfo ci2 = new ColumnInfo("Col2");
        ci2.setWidth(50);
        ciList.add(ci2);
        ColumnInfo ci3 = new ColumnInfo("Col3");
        ci3.setWidth(50);
        ciList.add(ci3);
        ResultSetRenderer rsRenderer = new ResultSetRenderer(query, ciList);
        
        rsRenderer.defineColumnBeingDragged(50);
        rsRenderer.moveColumnBeingDragged(25);
        assertEquals(75, ci1.getWidth());
        
        rsRenderer.defineColumnBeingDragged(125);
        assertEquals(ci2, rsRenderer.getColBeingDragged());
        
        rsRenderer.moveColumnBeingDragged(-25);
        assertEquals(25, ci2.getWidth());
        
        rsRenderer.defineColumnBeingDragged(150);
        assertEquals(ci3, rsRenderer.getColBeingDragged());
        
        rsRenderer.moveColumnBeingDragged(-100);
        assertEquals(0, ci3.getWidth());
    }

    /**
     * This tests that calling execute on an empty query will not throw
     * an NPE.
     */
    public void testExecuteEmptyQueryWithoutException() throws Exception {
        
        Report report = new Report("report");
        getWorkspace().addReport(report);
        ContentBox cb = new ContentBox();
        report.getPage().addContentBox(cb);
        ResultSetRenderer renderer = new ResultSetRenderer(query);
        cb.setContentRenderer(renderer);
     
        assertNull(renderer.getExecuteException());
        
        renderer.refresh();
        
        System.out.println(renderer.getExecuteException());
        assertNull(renderer.getExecuteException());
    }
    
    /**
     * Tests calling renderSuccess with an empty result set works without
     * throwing any exceptions.
     */
    public void testRenderSuccessWithEmptyRS() throws Exception {
    	
        
        ResultSetRenderer renderer = new ResultSetRenderer(query);
        renderer.refresh();
        
        Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        renderer.renderReportContent(g, new ContentBox(), 1, 1, false, null);
    }

    /**
     * Tests that children can be added to and removed from a result set
     * renderer. Although the children of a {@link ResultSetRenderer} are
     * maintained internally some classes, like the undo manager, need to be
     * able to modify the children of this renderer.
     */
    public void testAddAndRemoveChild() throws Exception {
        ColumnInfo ci = new ColumnInfo("label");
        
        assertEquals(0, renderer.getChildren().size());
        
        renderer.addChild(ci, 0);
        
        assertEquals(ci, renderer.getChildren().get(0));
        
        renderer.removeChild(ci);
        
        assertEquals(0, renderer.getChildren().size());
    }
}
