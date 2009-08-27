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

package ca.sqlpower.wabit;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.olap4j.OlapConnection;

import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLDatabaseMapping;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.Report;

public class WabitWorkspaceTest extends AbstractWabitObjectTest {

    private WabitWorkspace workspace;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workspace = new WabitWorkspace();
    }
    
    @Override
    public Set<String> getPropertiesToIgnoreForEvents() {
    	Set<String> ignore = new HashSet<String>();
        ignore.add("dataSourceTypes");
        ignore.add("serverBaseURI");
        ignore.add("session");
    	return ignore;
    }
    
    @Override
    public WabitObject getObjectUnderTest() {
        return workspace;
    }
    
    /**
     * Regression test for bug 1976. If a query cache is selected and is
     * removed the wabit object being edited should be changed.
     */
    public void testRemovingSelectedQueryCacheChangesSelection() throws Exception {
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        QueryCache query = new QueryCache(new SQLDatabaseMapping() {
            public SQLDatabase getDatabase(JDBCDataSource ds) {
                return null;
            }
        });
        workspace.addQuery(query, session);
        workspace.setEditorPanelModel(query);
        assertEquals(query, workspace.getEditorPanelModel());
        
        workspace.removeQuery(query, session);
        assertNotSame(query, workspace.getEditorPanelModel());
    }
    
    /**
     * Regression test for bug 1976. If an OLAP query is selected and is
     * removed the wabit object being edited should be changed.
     */
    public void testRemovingSelectedOlapQueryChangesSelection() throws Exception {
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        OlapQuery query = new OlapQuery(new OlapConnectionMapping() {
            public OlapConnection createConnection(Olap4jDataSource dataSource)
                    throws SQLException, ClassNotFoundException, NamingException {
                return null;
            }
        });
        workspace.addOlapQuery(query);
        workspace.setEditorPanelModel(query);
        assertEquals(query, workspace.getEditorPanelModel());
        
        workspace.removeOlapQuery(query);
        assertNotSame(query, workspace.getEditorPanelModel());
    }
    
    /**
     * Regression test for bug 1976. If a layout is selected and is
     * removed the wabit object being edited should be changed.
     */
    public void testRemovingSelectedLayoutChangesSelection() throws Exception {
        WabitSession session = new StubWabitSession(new StubWabitSessionContext());
        Report layout = new Report("Layout");
        workspace.addReport(layout);
        workspace.setEditorPanelModel(layout);
        assertEquals(layout, workspace.getEditorPanelModel());
        
        workspace.removeReport(layout);
        assertNotSame(layout, workspace.getEditorPanelModel());
    }

}
