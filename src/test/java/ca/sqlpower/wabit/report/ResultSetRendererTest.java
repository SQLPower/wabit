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
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.resultset.ResultSetCell;

public class ResultSetRendererTest extends AbstractWabitObjectTest {

    private ResultSetRenderer renderer;
    
    private SQLDatabase db;
    
    private Graphics graphics;
    
    private SQLDatabaseMapping stubMapping;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        stubMapping = new SQLDatabaseMapping() {
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return db;
            }
        };
        
        PlDotIni plini = new PlDotIni();
        plini.read(new File("src/test/java/pl.regression.ini"));
        JDBCDataSource ds = plini.getDataSource("regression_test", JDBCDataSource.class);
        db = new SQLDatabase(ds);
        renderer = new ResultSetRenderer(new QueryCache(stubMapping));
        
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        graphics = image.getGraphics();
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
            con = db.getConnection();
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
        
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        WabitWorkspace workspace = new WabitWorkspace();
        workspace.setSession(session);
        QueryCache cache = new QueryCache(stubMapping);
        workspace.addQuery(cache, session);
        cache.setDBMapping(stubMapping);
        cache.setDataSource(db.getDataSource());
        cache.getQuery().defineUserModifiedQuery("select * from subtotal_table");
        assertEquals(db, cache.getQuery().getDatabase());

        Report report = new Report("report");
        workspace.addReport(report);
        ContentBox cb = new ContentBox();
        report.getPage().addContentBox(cb);
        cb.setWidth(100);
        cb.setHeight(200);
        ResultSetRenderer renderer = new ResultSetRenderer(cache);
        renderer.setParent(cb);
        renderer.executeQuery();
        assertEquals(2, renderer.getColumnInfoList().size());
        renderer.getColumnInfoList().get(0).setWillGroupOrBreak(GroupAndBreak.GROUP);
        renderer.getColumnInfoList().get(1).setWillSubtotal(true);
        Font font = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[0];
        renderer.setHeaderFont(font);
        renderer.setBodyFont(font);
        renderer.createResultSetLayout((Graphics2D) graphics, cache.getCachedRowSet());
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
            con = db.getConnection();
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
        ResultSetRenderer rsRenderer = new ResultSetRenderer(new QueryCache(stubMapping), ciList);
        
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
        ResultSetRenderer rsRenderer = new ResultSetRenderer(new QueryCache(stubMapping), ciList);
        
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
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        WabitWorkspace workspace = new WabitWorkspace();
        workspace.setSession(session);
        QueryCache cache = new QueryCache(stubMapping);
        workspace.addQuery(cache, session);
        cache.setDBMapping(stubMapping);
        cache.setDataSource(db.getDataSource());
        
        Report report = new Report("report");
        workspace.addReport(report);
        ContentBox cb = new ContentBox();
        report.getPage().addContentBox(cb);
        ResultSetRenderer renderer = new ResultSetRenderer(cache);
        cb.setContentRenderer(renderer);
     
        assertNull(renderer.getExecuteException());
        
        renderer.executeQuery();
        
        System.out.println(renderer.getExecuteException());
        assertNull(renderer.getExecuteException());
    }
    
    /**
     * Tests calling renderSuccess with a null result set works without
     * throwing any exceptions.
     */
    public void testRenderSuccessWithEmptyRS() throws Exception {
        QueryCache cache = new QueryCache(stubMapping);
        cache.setDataSource(db.getDataSource());
        ResultSetRenderer renderer = new ResultSetRenderer(cache);
        
        Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        renderer.renderSuccess(g, new ContentBox(), 1, 1, false);
    }
}
