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

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jfree.data.category.CategoryDataset;

import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.wabit.report.ChartRenderer.DataTypeSeries;
import ca.sqlpower.wabit.report.chart.ColumnIdentifier;
import ca.sqlpower.wabit.report.chart.ColumnNameColumnIdentifier;

//TODO change this to extend AbstractWabitObjectTest
public class GraphRendererTest extends TestCase {
    
    private PlDotIni plIni;
    private JDBCDataSource ds;
    private ChartRenderer renderer;

    @Override
    protected void setUp() throws Exception {
        plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        ds = plIni.getDataSource("regression_test", JDBCDataSource.class);
        ContentBox contentBox = new ContentBox();
        renderer = new ChartRenderer();
        contentBox.setHeight(100);
        contentBox.setWidth(100);
    }

    /**
     * This tests the createCategoryDataset version that takes a result set with
     * a very simple result set. This test will pass if a basic
     * {@link CategoryDataset} can be created.
     */
    public void testCreateCategoryDatasetForResultSet() throws Exception {
        Connection con = ds.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("create table graph_test (category varchar(50), series integer)");
        stmt.execute("insert into graph_test (category, series) values ('a', 10)");
        stmt.execute("insert into graph_test (category, series) values ('b', 20)");
        stmt.execute("insert into graph_test (category, series) values ('c', 30)");
        CachedRowSet rs = new CachedRowSet();
        rs.populate(stmt.executeQuery("select category, series from graph_test"));
        stmt.execute("drop table graph_test");
        stmt.close();
        con.close();
        
        
        List<ColumnIdentifier> columnNamesInOrder = new ArrayList<ColumnIdentifier>(); 
        final ColumnIdentifier categoryIdentifier = new ColumnNameColumnIdentifier("category");
        final ColumnIdentifier seriesIdentifier = new ColumnNameColumnIdentifier("series");
        columnNamesInOrder.add(categoryIdentifier);
        columnNamesInOrder.add(seriesIdentifier);
        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes = new HashMap<ColumnIdentifier, DataTypeSeries>();
        columnsToDataTypes.put(categoryIdentifier, DataTypeSeries.CATEGORY);
        columnsToDataTypes.put(seriesIdentifier, DataTypeSeries.SERIES);
        List<ColumnIdentifier> categoryColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        categoryColumnIdentifiers.add(categoryIdentifier);
        CategoryDataset dataset = ChartRenderer.createCategoryDataset(columnNamesInOrder, columnsToDataTypes, rs, categoryColumnIdentifiers);
        
        assertEquals(3, dataset.getColumnCount());
        System.out.println(dataset.getColumnKeys());
        System.out.println(dataset.getRowKeys());
        assertTrue(dataset.getColumnKeys().contains("a"));
        assertTrue(dataset.getColumnKeys().contains("b"));
        assertTrue(dataset.getColumnKeys().contains("c"));
        assertEquals(1, dataset.getRowCount());
        assertTrue(dataset.getRowKeys().contains("series"));
        assertEquals(10, dataset.getValue("series", "a").intValue());
        assertEquals(20, dataset.getValue("series", "b").intValue());
        assertEquals(30, dataset.getValue("series", "c").intValue());
    }
    
    /**
     * This tests if two columns can be used as categories in a chart.
     */
    public void testCreateCategoryDatasetForResultSetTwoCategories() throws Exception {
        Connection con = ds.createConnection();
        Statement stmt = con.createStatement();
        stmt.execute("create table graph_test (category varchar(50), category2 varchar(50), series integer)");
        stmt.execute("insert into graph_test (category, category2, series) values ('a', 'd', 10)");
        stmt.execute("insert into graph_test (category, category2, series) values ('b', 'e', 20)");
        stmt.execute("insert into graph_test (category, category2, series) values ('c', 'f', 30)");
        CachedRowSet rs = new CachedRowSet();
        rs.populate(stmt.executeQuery("select category, category2, series from graph_test"));
        stmt.execute("drop table graph_test");
        stmt.close();
        con.close();
        
        
        List<ColumnIdentifier> columnNamesInOrder = new ArrayList<ColumnIdentifier>(); 
        final ColumnIdentifier categoryIdentifier = new ColumnNameColumnIdentifier("category");
        final ColumnIdentifier category2Identifier = new ColumnNameColumnIdentifier("category2");
        final ColumnIdentifier seriesIdentifier = new ColumnNameColumnIdentifier("series");
        columnNamesInOrder.add(categoryIdentifier);
        columnNamesInOrder.add(seriesIdentifier);
        Map<ColumnIdentifier, DataTypeSeries> columnsToDataTypes = new HashMap<ColumnIdentifier, DataTypeSeries>();
        columnsToDataTypes.put(categoryIdentifier, DataTypeSeries.CATEGORY);
        columnsToDataTypes.put(category2Identifier, DataTypeSeries.CATEGORY);
        columnsToDataTypes.put(seriesIdentifier, DataTypeSeries.SERIES);
        List<ColumnIdentifier> categoryColumnIdentifiers = new ArrayList<ColumnIdentifier>();
        categoryColumnIdentifiers.add(categoryIdentifier);
        categoryColumnIdentifiers.add(category2Identifier);
        CategoryDataset dataset = ChartRenderer.createCategoryDataset(columnNamesInOrder, columnsToDataTypes, rs, categoryColumnIdentifiers);
        
        assertEquals(3, dataset.getColumnCount());
        System.out.println(dataset.getColumnKeys());
        System.out.println(dataset.getRowKeys());
        String row1ColKey = ChartRenderer.createCategoryName(Arrays.asList(new String[]{"a", "d"}));
        String row2ColKey = ChartRenderer.createCategoryName(Arrays.asList(new String[]{"b", "e"}));
        String row3ColKey = ChartRenderer.createCategoryName(Arrays.asList(new String[]{"c", "f"}));
        assertTrue(dataset.getColumnKeys().contains(row1ColKey));
        assertTrue(dataset.getColumnKeys().contains(row2ColKey));
        assertTrue(dataset.getColumnKeys().contains(row3ColKey));
        assertEquals(1, dataset.getRowCount());
        assertTrue(dataset.getRowKeys().contains("series"));
        assertEquals(10, dataset.getValue("series", row1ColKey).intValue());
        assertEquals(20, dataset.getValue("series", row2ColKey).intValue());
        assertEquals(30, dataset.getValue("series", row3ColKey).intValue());
    }
    
}
