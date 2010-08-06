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
import java.util.Collection;

import junit.framework.TestCase;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.WorkspaceGraphModel;
import ca.sqlpower.object.WorkspaceGraphModelEdge;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.WabitLabel;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.rs.olap.OlapConnectionPool;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.query.QueryCache;

public class WorkspaceGraphModelTest extends TestCase {
    
	private StubWabitSession session;
	private StubWabitSessionContext context;
	private WabitWorkspace workspace;

	protected void setUp() throws Exception {
		super.setUp();
		
		final PlDotIni plIni = new PlDotIni();
    	plIni.read(new File("src/test/resources/pl.regression.ini"));
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
     * This test confirms that getNodes in the graph model of the workspaceXMLDAO
     * returns a correct list of nodes.
     * @throws Exception
     */
    public void testGetGraphNodes() throws Exception {
        QueryCache cache = new QueryCache(context);
        cache.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(cache, session);
        
        OlapQuery query = new OlapQuery(context);
        workspace.addOlapQuery(query);
        
        Chart chart = new Chart();
        workspace.addChart(chart);
        chart.setQuery(cache);
        
        Report report = new Report("name");
        workspace.addReport(report);
        
        WorkspaceGraphModel graphModel = new WorkspaceGraphModel(workspace, workspace, false, false);
        Collection<SPObject> nodes = graphModel.getNodes();
        assertEquals(16, nodes.size());
        assertTrue(nodes.contains(workspace));
        assertTrue(nodes.contains(cache));
        assertTrue(nodes.contains(query));
        assertTrue(nodes.contains(chart));
        assertTrue(nodes.contains(report));
        assertTrue(nodes.contains(report.getPage()));
        assertEquals(4, report.getPage().getChildren().size());
        assertTrue(nodes.containsAll(report.getPage().getChildren()));
    }

    /**
     * This test confirms that getNodes given a chart will return its query as
     * well.
     * 
     * @throws Exception
     */
    public void testGetGraphNodesForChart() throws Exception {
        QueryCache cache = new QueryCache(context);
        cache.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(cache, session);
        
        Chart chart = new Chart();
        workspace.addChart(chart);
        chart.setQuery(cache);
        
        WorkspaceGraphModel graphModel = new WorkspaceGraphModel(workspace, chart, false, false);
        Collection<SPObject> nodes = graphModel.getNodes();
        assertEquals(8, nodes.size());
        assertTrue(nodes.contains(cache));
        assertTrue(nodes.contains(chart));
    }

    /**
     * This test confirms that getOutboundEdges given a chart will return its
     * edge to its query.
     * 
     * @throws Exception
     */
    public void testGetOutboundEdgesForChart() throws Exception {
        QueryCache cache = new QueryCache(context);
        cache.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(cache, session);
        
        Chart chart = new Chart();
        workspace.addChart(chart);
        chart.setQuery(cache);
        
        WorkspaceGraphModel graphModel = new WorkspaceGraphModel(workspace, chart, false, false);
        Collection<WorkspaceGraphModelEdge> outboundEdges = 
             graphModel.getOutboundEdges(chart);
        assertEquals(1, outboundEdges.size());
        WorkspaceGraphModelEdge edge = (WorkspaceGraphModelEdge) outboundEdges.toArray()[0];
        assertEquals(chart, edge.getParent());
        assertEquals(cache, edge.getChild());
    }
    
    public void testReportChildrenInGraph() throws Exception {
        Report report = new Report("Report");
        workspace.addReport(report);
        ContentBox cb1 = new ContentBox();
        ContentBox cb2 = new ContentBox();
        ContentBox cb3 = new ContentBox();
        report.getPage().addContentBox(cb1);
        report.getPage().addContentBox(cb2);
        report.getPage().addContentBox(cb3);
        
        WabitLabel label = new WabitLabel();
        cb1.setContentRenderer(label);
        
        final QueryCache query = new QueryCache(context);
        ResultSetRenderer rsr = new ResultSetRenderer(query);
        cb2.setContentRenderer(rsr);
        
        WorkspaceGraphModel graph = new WorkspaceGraphModel(workspace, report, false, false);
        assertEquals(17, graph.getNodes().size());
        Collection<WorkspaceGraphModelEdge> outboundEdgesReport = graph.getOutboundEdges(report);
        assertEquals(1, outboundEdgesReport.size());
        assertEquals(report.getPage(), ((WorkspaceGraphModelEdge) outboundEdgesReport.toArray()[0]).getChild());
        Collection<WorkspaceGraphModelEdge> outboundEdgesPage = graph.getOutboundEdges(report.getPage());
        assertEquals(7, outboundEdgesPage.size());
        Collection<WorkspaceGraphModelEdge> inboundEdgesCB1 = graph.getInboundEdges(cb1);
        assertEquals(1, inboundEdgesCB1.size());
        assertEquals(report.getPage(), ((WorkspaceGraphModelEdge) inboundEdgesCB1.toArray()[0]).getParent());
        Collection<WorkspaceGraphModelEdge> inboundEdgesCB2 = graph.getInboundEdges(cb2);
        assertEquals(1, inboundEdgesCB2.size());
        assertEquals(report.getPage(), ((WorkspaceGraphModelEdge) inboundEdgesCB2.toArray()[0]).getParent());
        Collection<WorkspaceGraphModelEdge> inboundEdgesCB3 = graph.getInboundEdges(cb3);
        assertEquals(1, inboundEdgesCB3.size());
        assertEquals(report.getPage(), ((WorkspaceGraphModelEdge) inboundEdgesCB3.toArray()[0]).getParent());
        Collection<WorkspaceGraphModelEdge> outboundEdgesCB1 = graph.getOutboundEdges(cb1);
        assertEquals(1, outboundEdgesCB1.size());
        assertEquals(label, ((WorkspaceGraphModelEdge) outboundEdgesCB1.toArray()[0]).getChild());
        Collection<WorkspaceGraphModelEdge> outboundEdgesCB2 = graph.getOutboundEdges(cb2);
        assertEquals(1, outboundEdgesCB2.size());
        assertEquals(rsr, ((WorkspaceGraphModelEdge) outboundEdgesCB2.toArray()[0]).getChild());
        Collection<WorkspaceGraphModelEdge> outboundEdgesCB3 = graph.getOutboundEdges(cb3);
        assertEquals(0, outboundEdgesCB3.size());
    }
    
    /**
     * Tests a basic dependency graph can be created.
     */
    public void testGetDependencyGraph() throws Exception {
        QueryCache cache = new QueryCache(context);
        cache.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(cache, session);
        Chart chart = new Chart();
        chart.setName("chart");
        workspace.addChart(chart);
        Report report = new Report("Report");
        workspace.addReport(report);
        ContentBox box = new ContentBox();
        box.setContentRenderer(new ChartRenderer(chart));
        report.getPage().addContentBox(box);
        
        WorkspaceGraphModel graphModel = new WorkspaceGraphModel(workspace, report, true, false);
        Collection<SPObject> nodes = graphModel.getNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(report));
        assertTrue(nodes.contains(chart));
        final Collection<WorkspaceGraphModelEdge> outboundEdges = graphModel.getOutboundEdges(report);
        assertEquals(1, outboundEdges.size());
        assertEquals(chart, outboundEdges.toArray(new WorkspaceGraphModelEdge[1])[0].getChild());
        
        chart.setQuery(cache);
        graphModel = new WorkspaceGraphModel(workspace, report, true, false);
        nodes = graphModel.getNodes();
        assertEquals(4, nodes.size());
        assertTrue(nodes.contains(report));
        assertTrue(nodes.contains(chart));
        assertTrue(nodes.contains(cache));
    }
    
    /**
     * Tests a basic inverted dependency graph can be created.
     */
    public void testGetInvertedDependencyGraph() throws Exception {
        QueryCache cache = new QueryCache(context);
        cache.setDataSource((JDBCDataSource)session.getDataSources().getDataSource("regression_test"));
        workspace.addQuery(cache, session);
        Chart chart = new Chart();
        chart.setName("chart");
        workspace.addChart(chart);
        Report report = new Report("Report");
        workspace.addReport(report);
        ContentBox box = new ContentBox();
        final ChartRenderer chartRenderer = new ChartRenderer(chart);
        box.setContentRenderer(chartRenderer);
        report.getPage().addContentBox(box);
        
        WorkspaceGraphModel graphModel = new WorkspaceGraphModel(workspace, chart, true, true);
        Collection<SPObject> nodes = graphModel.getNodes();
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(chartRenderer));
        assertTrue(nodes.contains(chart));
        final Collection<WorkspaceGraphModelEdge> outboundEdges = graphModel.getOutboundEdges(chart);
        assertEquals(1, outboundEdges.size());
        assertEquals(chartRenderer, outboundEdges.toArray(new WorkspaceGraphModelEdge[1])[0].getChild());
        
        chart.setQuery(cache);
        graphModel = new WorkspaceGraphModel(workspace, cache, true, true);
        nodes = graphModel.getNodes();
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains(chartRenderer));
        assertTrue(nodes.contains(chart));
        assertTrue(nodes.contains(cache));
    }
    
}
