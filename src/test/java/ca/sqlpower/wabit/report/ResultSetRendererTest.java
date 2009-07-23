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
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.resultset.Section;

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
        } finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
        
        QueryCache cache = new QueryCache(stubMapping);
        cache.setDataSource(db.getDataSource());
        cache.getQuery().defineUserModifiedQuery("select * from subtotal_table");
        
        ContentBox cb = new ContentBox();
        cb.setWidth(100);
        cb.setHeight(200);
        ResultSetRenderer renderer = new ResultSetRenderer(cache);
        renderer.setParent(cb);
        renderer.executeQuery();
        assertEquals(2, renderer.getColumnInfoList().size());
        renderer.getColumnInfoList().get(0).setWillBreak(true);
        renderer.getColumnInfoList().get(1).setWillSubtotal(true);
        Font font = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[0];
        renderer.setHeaderFont(font);
        renderer.setBodyFont(font);
        renderer.createResultSetLayout((Graphics2D) graphics, cache.getCachedRowSet());
        List<Section> sections = renderer.findSections();
        assertEquals(3, sections.size());
        
        Map<List<Object>, BigDecimal> sectionKeyToSubTotal = new HashMap<List<Object>, BigDecimal>();
        List<Object> sectionKey = new ArrayList<Object>();
        sectionKey.add("a");
        sectionKey.add(null);
        BigDecimal subtotal = BigDecimal.valueOf(60);
        sectionKeyToSubTotal.put(sectionKey, subtotal);
        sectionKey = new ArrayList<Object>();
        sectionKey.add("b");
        sectionKey.add(null);
        subtotal = BigDecimal.valueOf(36);
        sectionKeyToSubTotal.put(sectionKey, subtotal);
        sectionKey = new ArrayList<Object>();
        sectionKey.add("fib");
        sectionKey.add(null);
        subtotal = BigDecimal.valueOf(20);
        sectionKeyToSubTotal.put(sectionKey, subtotal);
        for (Section section : sections) {
            System.out.println(section.getSectionHeader());
            BigDecimal sectionSubtotal = sectionKeyToSubTotal.get(section.getSectionHeader());
            assertEquals(sectionSubtotal, section.getTotals().get(1));
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

}
