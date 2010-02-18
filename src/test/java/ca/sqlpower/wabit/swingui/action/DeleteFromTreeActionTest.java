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

package ca.sqlpower.wabit.swingui.action;

import java.io.File;

import junit.framework.TestCase;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.WorkspaceGraphModel;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.query.QueryCache;

public class DeleteFromTreeActionTest extends TestCase {

	private StubWabitSessionContext context;
	private StubWabitSession session;
	private WabitWorkspace workspace;

	protected void setUp() throws Exception {
		super.setUp();
		final PlDotIni plIni = new PlDotIni();
    	plIni.read(new File("src/test/java/pl.regression.ini"));
        final Olap4jDataSource olapDS = plIni.getDataSource("World Facts OLAP Connection", 
        		Olap4jDataSource.class);
        if (olapDS == null) throw new IllegalStateException("Cannot find 'World Facts OLAP Connection'");
        final OlapConnectionPool connectionPool = new OlapConnectionPool(olapDS, 
        		new SQLDatabaseMapping() {
        	private final SQLDatabase sqlDB = new SQLDatabase(olapDS.getDataSource());
        	public SQLDatabase getDatabase(JDBCDataSource ds) {
        		return sqlDB;
        	}
        });
    	
    	
    	this.context = new StubWabitSessionContext() {
    		public org.olap4j.OlapConnection createConnection(Olap4jDataSource dataSource) 
    			throws java.sql.SQLException ,ClassNotFoundException ,javax.naming.NamingException {
    				return connectionPool.getConnection();
    		};
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return plIni;
    		}
    	};
    	
    	this.workspace = new WabitWorkspace();
    	
    	this.session = new StubWabitSession(context) {

    		@Override
    		public DataSourceCollection<SPDataSource> getDataSources() {
    			return getContext().getDataSources();
    		}
    		
    		@Override
    		public WabitWorkspace getWorkspace() {
    			return workspace;
    		}
    	};
    	
    	this.workspace.setSession(session);
	}
	
    /**
     * This is a basic test that checks objects can be removed
     * from a workspace.
     */
    public void testRemoveNodes() throws Exception {
        
        QueryCache query = new QueryCache(context);
        query.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(query, session);
        Chart chart = new Chart();
        chart.setName("chart");
        workspace.addChart(chart);
        chart.setQuery(query);
        Report report = new Report("report");
        workspace.addReport(report);
        ContentBox chartContentBox = new ContentBox();
        chartContentBox.setContentRenderer(new ChartRenderer(chart));
        report.getPage().addContentBox(chartContentBox);
        ContentBox queryContentBox = new ContentBox();
        queryContentBox.setContentRenderer(new ResultSetRenderer(query));
        report.getPage().addContentBox(queryContentBox);
        
        OlapQuery olapQuery = new OlapQuery(context);
        olapQuery.setName("olap query");
        workspace.addOlapQuery(olapQuery);
        ContentBox olapQueryContentBox = new ContentBox();
        olapQueryContentBox.setContentRenderer(new CellSetRenderer(olapQuery));
        report.getPage().addContentBox(olapQueryContentBox);
        
        assertEquals(4, workspace.getChildren().size());
        assertEquals(3, report.getPage().getContentBoxes().size());
        
        WorkspaceGraphModel graph = new WorkspaceGraphModel(workspace, query, true, true);
        
        DeleteFromTreeAction deleteAction = new DeleteFromTreeAction(workspace, query, null, 
                new DefaultUserPrompterFactory());
        
        deleteAction.removeNode(query, graph);
        
        assertEquals(2, workspace.getChildren().size());
        assertTrue(workspace.getChildren().contains(olapQuery));
        assertTrue(workspace.getChildren().contains(report));
        
        assertEquals(3, report.getPage().getContentBoxes().size());
        assertTrue(report.getPage().getContentBoxes().contains(queryContentBox));
        assertTrue(report.getPage().getContentBoxes().contains(chartContentBox));
        assertTrue(report.getPage().getContentBoxes().contains(olapQueryContentBox));
        assertNull(queryContentBox.getContentRenderer());
        assertNull(chartContentBox.getContentRenderer());
        assertNotNull(olapQueryContentBox.getContentRenderer());
        
    }
    
    /**
     * This is a test to assert a correct exception is thrown when an object
     * is attempted to be deleted but has a dependency that was not handled
     * in the graph.
     */
    public void testRemoveNodesWithAddedDependencies() throws Exception {
        
        QueryCache query = new QueryCache(context);
        query.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(query, session);
        Chart chart = new Chart();
        chart.setName("chart");
        chart.setQuery(query);
        workspace.addChart(chart);
        Report report = new Report("report");
        workspace.addReport(report);
        ContentBox chartContentBox = new ContentBox();
        chartContentBox.setContentRenderer(new ChartRenderer(chart));
        report.getPage().addContentBox(chartContentBox);
        ContentBox queryContentBox = new ContentBox();
        queryContentBox.setContentRenderer(new ResultSetRenderer(query));
        report.getPage().addContentBox(queryContentBox);
        
        OlapQuery olapQuery = new OlapQuery(context);
        olapQuery.setName("olap query");
        workspace.addOlapQuery(olapQuery);
        ContentBox olapQueryContentBox = new ContentBox();
        olapQueryContentBox.setContentRenderer(new CellSetRenderer(olapQuery));
        report.getPage().addContentBox(olapQueryContentBox);
        
        assertEquals(4, workspace.getChildren().size());
        assertEquals(3, report.getPage().getContentBoxes().size());
        
        WorkspaceGraphModel graph = new WorkspaceGraphModel(workspace, query, true, true);
        
        //Adding an additional edge to the graph here to cause a
        //dependency problem. This should cause removeNode to throw
        //an exception.
        Chart chart2 = new Chart();
        chart2.setName("chart2");
        chart2.setQuery(query);
        workspace.addChart(chart2);
        
        DeleteFromTreeAction deleteAction = new DeleteFromTreeAction(workspace, query, null, 
                new DefaultUserPrompterFactory());
        
        try {
            deleteAction.removeNode(query, graph);
            fail("The graph does not include the dependency on the second chart. " +
            		"This should throw an exception when the object is attempted to be removed.");
        } catch (ObjectDependentException e) {
            //successfully caught the exception that there are additional
            //dependencies that the graph did not include.
        }
    }
    
}
