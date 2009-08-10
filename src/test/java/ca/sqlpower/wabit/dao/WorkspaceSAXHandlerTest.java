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

package ca.sqlpower.wabit.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.util.DefaultUserPrompter;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.StubWabitSession;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.WabitWorkspace;

public class WorkspaceSAXHandlerTest extends TestCase {

	/**
	 * This is a fake database to be used in testing.
	 */
	private SQLDatabase db;
	private PlDotIni plIni;
	
	@Override
	protected void setUp() throws Exception {
		plIni = new PlDotIni();
        plIni.read(new File("src/test/java/pl.regression.ini"));
        JDBCDataSource ds = plIni.getDataSource("regression_test", JDBCDataSource.class);

        db = new SQLDatabase(ds);
	}
	
	/**
	 * Tests loading a workspace with a data source that no longer exists in
	 * the list of data sources can be replaced by a new data source.
	 */
	public void testMissingDSIsReplaced() throws Exception {
		JDBCDataSource newDS = new JDBCDataSource(db.getDataSource());
		newDS.setName("Missing DS is replaced");
		final WabitSessionContext beforeSaveContext = new WabitSessionContextImpl(false, false);
		final WabitSession session = beforeSaveContext.createSession();
		beforeSaveContext.setActiveSession(session);
		WabitWorkspace p = session.getWorkspace();
		p.setName("Workspace");
		p.addDataSource(newDS);

		QueryCache query = new QueryCache(beforeSaveContext);
		p.addQuery(query, session);
		query.setDataSource(newDS);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WorkspaceXMLDAO saveDAO = new WorkspaceXMLDAO(out, beforeSaveContext);
		saveDAO.saveActiveWorkspace();
		System.out.println(out.toString("utf-8"));
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        final JDBCDataSource replacementDS = new JDBCDataSource(plIni); 
        replacementDS.setName("Replacement DS");
        WabitSessionContext context = new StubWabitSessionContext() {
            @Override
            public DataSourceCollection getDataSources() {
                return plIni;
            }
        	
        	@Override
        	public WabitSession createSession() {
        		return new StubWabitSession(this);
        	}

        	@Override
        	public UserPrompter createUserPrompter(String question,
        	        UserPromptType responseType, UserPromptOptions optionType,
        	        UserPromptResponse defaultResponseType,
        	        Object defaultResponse, String... buttonNames) {
                    return new DefaultUserPrompter(optionType, defaultResponseType,
                            defaultResponse);
        	}
        	
        	@Override
        	public SQLDatabase getDatabase(JDBCDataSource ds) {
        	    if (ds == db.getDataSource()) return db;
        	    if (ds == replacementDS) return new SQLDatabase(replacementDS);
        	    return null;
        	}
        	
        	@Override
        	public UserPrompter createDatabaseUserPrompter(String question,
        			List<Class<? extends SPDataSource>> dsTypes,
        			UserPromptOptions optionType,
        			UserPromptResponse defaultResponseType,
        			Object defaultResponse,
        			DataSourceCollection<SPDataSource> dsCollection,
        			String... buttonNames) {
        		return new DefaultUserPrompter(
                        optionType, UserPromptResponse.NEW, replacementDS);
        	}
        };
        
        OpenWorkspaceXMLDAO loadDAO = new OpenWorkspaceXMLDAO(context, in, 0);
	
        final List<WabitSession> loadedWorkspaces = loadDAO.openWorkspaces();
        assertEquals(1, loadedWorkspaces.size());
        WabitSession loadedSession = loadedWorkspaces.get(0);
        assertEquals(1, loadedSession.getWorkspace().getQueries().size());
        QueryCache loadedQuery = (QueryCache) loadedSession.getWorkspace().getQueries().get(0);
        assertEquals(replacementDS, loadedQuery.getQuery().getDatabase().getDataSource());
	}
}
