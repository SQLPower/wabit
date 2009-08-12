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

package ca.sqlpower.wabit.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.Layout;

import com.rc.retroweaver.runtime.Collections;

/**
 * This DAO will load workspaces to a context from a given input stream. Each
 * time a new input stream is to be loaded a new instance of this class should
 * be created.
 */
public class OpenWorkspaceXMLDAO implements Monitorable {

    private static final Logger logger = Logger.getLogger(OpenWorkspaceXMLDAO.class);
    
    /**
     * This input stream will count the number of bytes read from the stream.
     * This allows the workspace to tell how far it is in loading the file.
     */
    private static class CountingInputStream extends InputStream {

        /**
         * The input stream to delegate to.
         */
        private final InputStream delegateStream;

        /**
         * The number of bytes already read.
         */
        private int byteCount; 
        
        public CountingInputStream(InputStream in) {
            delegateStream = in;
            byteCount = 0;
        }
        
        @Override
        public int read() throws IOException {
            byteCount++;
            return delegateStream.read();
        }
        
        public int getByteCount() {
            return byteCount;
        }
    }

	/**
	 * This context will have new sessions added to it for each workspace
	 * loaded.
	 */
	private final WabitSessionContext context;
	
	/**
	 * This is the input stream we are loading workspaces from.
	 */
	private final CountingInputStream in;
	
	/**
	 * The sax handler used to load workspaces.
	 */
	private WorkspaceSAXHandler saxHandler;
	   
    /**
     * Describes if this SAXHandler has started parsing an input stream.
     */
    private AtomicBoolean started = new AtomicBoolean(false);
    
    /**
     * Describes if this SAXHandler has finished parsing an input stream.
     */
    private AtomicBoolean finished = new AtomicBoolean(false);
    
    /**
     * Tracks if this DAO has been cancelled before the saxHandler could be
     * set to be used as a delegate.
     */
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    
    /**
     * The number of bytes in the stream. For use in the monitorable methods.
     */
    private final int bytesInStream;
	
	public OpenWorkspaceXMLDAO(WabitSessionContext context, InputStream in, int bytesInStream) {
		this.context = context;
        this.bytesInStream = bytesInStream;
		this.in = new CountingInputStream(in);
	}

    /**
     * Call this method to load the workspaces in the given stream into the
     * {@link #saxHandler}. This is the first step to loading or importing a
     * file into Wabit. This needs to be done before any other parts of loading
     * if it is being done in multiple parts.
     * <p>
     * If loading is done on multiple threads this operation can be done on a
     * separate thread. This method should only be called once for each DAO.
     */
	public void loadWorkspacesFromStream() {
	    if (started.get()) throw new IllegalStateException("Loading already started. A new instance " +
	        "of this class should be created instead of calling this method again.");
	    started.set(true);
	    SAXParser parser;
	    saxHandler = new WorkspaceSAXHandler(context);

	    try {
	        parser = SAXParserFactory.newInstance().newSAXParser();
	        parser.parse(in, saxHandler);
	    } catch (CancellationException e) {
	        //do nothing on a cancellation
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
	        context.setLoading(false);
	    }
	    finished.set(true);
	}

    /**
     * Call this method to register the loaded workspaces with the context in
     * this DAO. This is the last step to loading a file into Wabit.
     * <p>
     * If loading is done on multiple threads this operation must be done on the
     * event dispatch thread. This should only be called once for each DAO or
     * the same workspace will be added to the context multiple times.
     * 
     * @return The sessions that contain the workspaces that have been added to
     *         Wabit.
     */
	@SuppressWarnings("unchecked")
    public List<WabitSession> addLoadedWorkspacesToContext() {
	       if (cancelled.get()) return Collections.emptyList();
	        
	        for (WabitSession session : saxHandler.getSessions()) {
	            context.registerChildSession(session);
	        }
	        
	        return saxHandler.getSessions();
	}
	
    /**
     * Call this method to add the {@link WabitObject}s in the loaded workspaces
     * to the given session . This is the last step to importing a file into
     * Wabit workspace.
     * <p>
     * If importing is done on multiple threads this operation must be done on
     * the event dispatch thread. This should only be called once for each DAO
     * or the same workspace will be added to the context multiple times.
     * 
     * @param session
     *            The session that will have {@link WabitObject}s added to it.
     *            Null should not be passed into this method.
     */
    public void addImportedWorkspaceContentToWorkspace(WabitSession session) {
        if (session == null) {
            context.createUserPrompter("Select a workspace to import into.", UserPromptType.MESSAGE, 
                    UserPromptOptions.OK, UserPromptResponse.OK, null);
            return;
        }
        
        if (cancelled.get()) return;

        try {
            context.setLoading(true);
            final WabitWorkspace workspace = session.getWorkspace();
            int importObjectCount = 0;
            for (WabitSession importingSession : saxHandler.getSessions()) {
                for (WabitObject importObject : importingSession.getWorkspace().getChildren()) {
                    if (importObject instanceof WabitDataSource) {
                        if (!session.getWorkspace().dsAlreadyAdded(((WabitDataSource) importObject).getSPDataSource())) {
                            workspace.addDataSource((WabitDataSource) importObject);
                        }
                    } else if (importObject instanceof QueryCache) {
                        workspace.addQuery((QueryCache) importObject, session);
                    } else if (importObject instanceof OlapQuery) {
                        workspace.addOlapQuery((OlapQuery) importObject);
                    } else if (importObject instanceof WabitImage) {
                        workspace.addImage((WabitImage) importObject);
                    } else if (importObject instanceof Layout) {
                        workspace.addLayout((Layout) importObject);
                    } else {
                        throw new IllegalStateException("Cannot import the WabitObject type " + importObject.getClass());
                    }
                    importObjectCount++;
                }
            }
            logger.debug("Imported " + importObjectCount + " objects into " + session.getWorkspace().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            context.setLoading(false);
        }
    }
	
    /**
     * Calling this method will load all of the workspaces from the input stream
     * into the context that was given to this class when its constructor was
     * called. This is a complete version of loading a file into Wabit. If this
     * method is used no other methods need to be called.
     * <p>
     * Use this method to load a file into Wabit if there is no concerns about
     * multi threading.
     */
    public List<WabitSession> openWorkspaces() {
        loadWorkspacesFromStream();
        return addLoadedWorkspacesToContext();
    }

    /**
     * Calling this method will import all of the workspaces from the input
     * stream into the session given. This is a complete version of importing a
     * file into a workspace in Wabit. If this method is used no other methods
     * need to be called.
     * <p>
     * Use this method to import a file into Wabit if there is no concerns about
     * multi threading.
     * 
     * @param session
     *            The session to import {@link WabitObject}s into. This should
     *            not be null.
     */
    public void importWorkspaces(WabitSession session) {
        loadWorkspacesFromStream();
        importWorkspaces(session);
    }

    public Integer getJobSize() {
        return bytesInStream;
    }

    public String getMessage() {
        if (saxHandler == null) return "";
        return saxHandler.getMessage();
    }

    public int getProgress() {
        return in.getByteCount();
    }

    public boolean hasStarted() {
        return started.get();
    }

    public boolean isFinished() {
        return finished.get();
    }
    
    public boolean isCancelled() {
        if (saxHandler == null) {
            return cancelled.get(); 
        }
        return saxHandler.isCancelled();
    }

    public void setCancelled(boolean cancelled) {
        if (saxHandler == null) {
            this.cancelled.set(cancelled);
        } else {
            saxHandler.setCancelled(cancelled);
        }
    }

}
