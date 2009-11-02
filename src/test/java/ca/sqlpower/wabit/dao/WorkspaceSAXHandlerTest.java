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
import java.util.concurrent.CancellationException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.util.DefaultUserPrompter;
import ca.sqlpower.util.DefaultUserPrompterFactory;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.Version;
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
    
    private class CountingPromptContext extends StubWabitSessionContext {
        private int timesUserWasPrompted = 0;
        
        @Override
        public UserPrompter createUserPrompter(String question,
                UserPromptType responseType, UserPromptOptions optionType,
                UserPromptResponse defaultResponseType, Object defaultResponse,
                String... buttonNames) {
            timesUserWasPrompted++;
            return new DefaultUserPrompterFactory().createUserPrompter(question, responseType, 
                    optionType, defaultResponseType, defaultResponse, buttonNames);
        }

        public int getTimesUserWasPrompted() {
            return timesUserWasPrompted;
        }
    }
    
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
		final WabitSessionContext beforeSaveContext = new WabitSessionContextImpl(false, false, plIni, "", false);
		final WabitSession session = beforeSaveContext.createSession();
		beforeSaveContext.registerChildSession(session);
		beforeSaveContext.setActiveSession(session);
		WabitWorkspace p = session.getWorkspace();
		p.setName("Workspace");
		p.addDataSource(newDS);

		QueryCache query = new QueryCache(beforeSaveContext);
		query.setName("name");
		p.addQuery(query, session);
		query.setDataSource(newDS);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WorkspaceXMLDAO saveDAO = new WorkspaceXMLDAO(out, beforeSaveContext);
		saveDAO.saveActiveWorkspace();
		System.out.println(out.toString("utf-8"));
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        
        final JDBCDataSource replacementDS = new JDBCDataSource(plIni); 
        replacementDS.setParentType(newDS.getParentType());
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
        
        OpenWorkspaceXMLDAO loadDAO =
            new OpenWorkspaceXMLDAO(context, in, OpenWorkspaceXMLDAO.UNKNOWN_STREAM_LENGTH);
	
        final List<WabitSession> loadedWorkspaces = loadDAO.openWorkspaces();
        assertEquals(1, loadedWorkspaces.size());
        WabitSession loadedSession = loadedWorkspaces.get(0);
        assertEquals(1, loadedSession.getWorkspace().getQueries().size());
        QueryCache loadedQuery = (QueryCache) loadedSession.getWorkspace().getQueries().get(0);
        assertEquals(replacementDS, loadedQuery.getDatabase().getDataSource());
	}
	
	/**
	 * If the version number is missing the user should be notified.
	 */
	public void testVersionNumberMissing() throws Exception {
	    CountingPromptContext context = new CountingPromptContext();
	    
	    String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
	        "<wabit wabit-app-version=\"0.9.9\">\n" +
	        "<project name=\"aaaaa\">\n" +
	        "</project>\n" +
	        "</wabit>";
	    
	    ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
	    
        OpenWorkspaceXMLDAO loadDAO = new OpenWorkspaceXMLDAO(context, in, 
                OpenWorkspaceXMLDAO.UNKNOWN_STREAM_LENGTH);
        
        loadDAO.openWorkspaces();
        
        assertEquals(1, context.getTimesUserWasPrompted());
    }
	
	/**
     * If the version number's minor version is less than the current version the 
     * user should be notified.
     */
    public void testVersionNumberOlderThanNow() throws Exception {
        CountingPromptContext context = new CountingPromptContext();
        
        Version currentVersion = WorkspaceXMLDAO.FILE_VERSION;
        StringBuffer buffer = new StringBuffer();
        buffer.append(currentVersion.getParts()[0]).append(".");
        buffer.append(((Integer) currentVersion.getParts()[1]) - 1);
        for (int i = 2; i < currentVersion.getParts().length; i++) {
            buffer.append(".").append(currentVersion.getParts()[i]);
        }
        String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<wabit export-format=\"" + buffer.toString() + "\" wabit-app-version=\"0.9.9\">\n" +
            "<project name=\"aaaaa\">\n" +
            "</project>\n" +
            "</wabit>";
        
        ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
        
        OpenWorkspaceXMLDAO loadDAO = new OpenWorkspaceXMLDAO(context, in, 
                OpenWorkspaceXMLDAO.UNKNOWN_STREAM_LENGTH);
        
        loadDAO.openWorkspaces();
        
        assertEquals(1, context.getTimesUserWasPrompted());
    }
    
    /**
     * If the version number's newer than the current file version that Wabit saves
     * the user should be notified.
     */
    public void testVersionNewerThanNow() throws Exception {
        CountingPromptContext context = new CountingPromptContext();
        
        Version currentVersion = WorkspaceXMLDAO.FILE_VERSION;
        StringBuffer buffer = new StringBuffer();
        buffer.append(currentVersion.getParts()[0]).append(".");
        buffer.append(((Integer) currentVersion.getParts()[1]) + 1);
        for (int i = 2; i < currentVersion.getParts().length; i++) {
            buffer.append(".").append(currentVersion.getParts()[i]);
        }
        String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<wabit export-format=\"" + buffer.toString() + "\" wabit-app-version=\"0.9.9\">\n" +
            "<project name=\"aaaaa\">\n" +
            "</project>\n" +
            "</wabit>";
        
        ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
        
        OpenWorkspaceXMLDAO loadDAO = new OpenWorkspaceXMLDAO(context, in, 
                OpenWorkspaceXMLDAO.UNKNOWN_STREAM_LENGTH);
        
        loadDAO.openWorkspaces();
        
        assertEquals(1, context.getTimesUserWasPrompted());
    }

    /**
     * If the file being loaded by the sax handler has a version number whose
     * major version is later than the current one it should cancel the loading
     * by throwing a CancellationException.
     */
    public void testCancelExceptionOnLaterMajorVersion() throws Exception {
        CountingPromptContext context = new CountingPromptContext();
        
        Version currentVersion = WorkspaceXMLDAO.FILE_VERSION;
        StringBuffer buffer = new StringBuffer();
        buffer.append(((Integer) currentVersion.getParts()[0]) + 1);
        for (int i = 1; i < currentVersion.getParts().length; i++) {
            buffer.append(".").append(currentVersion.getParts()[i]);
        }
        String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<wabit export-format=\"" + buffer.toString() + "\" wabit-app-version=\"0.9.9\">\n" +
            "<project name=\"aaaaa\">\n" +
            "</project>\n" +
            "</wabit>";
        
        ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
        
        SAXParser parser;
        WorkspaceSAXHandler saxHandler = new WorkspaceSAXHandler(context);

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(in, saxHandler);
            fail("The file should not load if the file has a later major or minor version" +
            		"than the currently supported version.");
        } catch (CancellationException e) {
            //do nothing on a cancellation
        }
    }
    
    /**
     * If the file being loaded by the sax handler has a version number whose
     * minor version is later than the current one it should cancel the loading
     * by throwing a CancellationException.
     */
    public void testCancelExceptionOnLaterMinorVersion() throws Exception {
        CountingPromptContext context = new CountingPromptContext();
        
        Version currentVersion = WorkspaceXMLDAO.FILE_VERSION;
        StringBuffer buffer = new StringBuffer();
        buffer.append(currentVersion.getParts()[0]).append(".");
        buffer.append(((Integer) currentVersion.getParts()[1]) + 1);
        for (int i = 2; i < currentVersion.getParts().length; i++) {
            buffer.append(".").append(currentVersion.getParts()[i]);
        }
        String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<wabit export-format=\"" + buffer.toString() + "\" wabit-app-version=\"0.9.9\">\n" +
            "<project name=\"aaaaa\">\n" +
            "</project>\n" +
            "</wabit>";
        
        ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
        
        SAXParser parser;
        WorkspaceSAXHandler saxHandler = new WorkspaceSAXHandler(context);

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(in, saxHandler);
            fail("The file should not load if the file has a later major or minor version" +
                    "than the currently supported version.");
        } catch (CancellationException e) {
            //do nothing on a cancellation
        }
    }
    
    /**
     * If the file being loaded by the sax handler has a version number whose
     * major version is before than the current one it should cancel the loading
     * by throwing a CancellationException.
     */
    public void testCancelExceptionOnEarlierMajorVersion() throws Exception {
        CountingPromptContext context = new CountingPromptContext();
        
        Version currentVersion = WorkspaceXMLDAO.FILE_VERSION;
        StringBuffer buffer = new StringBuffer();
        buffer.append(((Integer) currentVersion.getParts()[0]) - 1);
        for (int i = 1; i < currentVersion.getParts().length; i++) {
            buffer.append(".").append(currentVersion.getParts()[i]);
        }
        String noVersionProject = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<wabit export-format=\"" + buffer.toString() + "\" wabit-app-version=\"0.9.9\">\n" +
            "<project name=\"aaaaa\">\n" +
            "</project>\n" +
            "</wabit>";
        
        ByteArrayInputStream in = new ByteArrayInputStream(noVersionProject.getBytes());
        
        SAXParser parser;
        WorkspaceSAXHandler saxHandler = new WorkspaceSAXHandler(context);

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(in, saxHandler);
            fail("The file should not load if the file has a later major or minor version" +
                    "than the currently supported version.");
        } catch (CancellationException e) {
            //do nothing on a cancellation
        }
    }
}
