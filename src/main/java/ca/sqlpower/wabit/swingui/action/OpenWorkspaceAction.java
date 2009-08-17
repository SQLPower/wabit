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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;
import ca.sqlpower.wabit.swingui.OpenProgressWindow;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;

/**
 * This action will load in workspaces from a user selected file to a given
 * context.
 */
public class OpenWorkspaceAction extends AbstractAction {
    
    private static final Logger logger = Logger.getLogger(OpenWorkspaceAction.class);
    
	/**
	 * This is the context within Wabit that will have the workspaces
	 * loaded into.
	 */
	private final WabitSwingSessionContext context;
	
	public OpenWorkspaceAction(WabitSwingSessionContext context) {
		super("Open Workspace...", WabitSwingSessionContextImpl.OPEN_WABIT_ICON);
		this.context = context;
	}

	public void actionPerformed(ActionEvent e) {
	    File defaultFile = null;
        if (context.getActiveSession() != null) {
            defaultFile = context.getActiveSwingSession().getCurrentURIAsFile();
        }
		JFileChooser fc = new JFileChooser(defaultFile);
		fc.setDialogTitle("Select the file to load from.");
		fc.addChoosableFileFilter(SPSUtils.WABIT_FILE_FILTER);
		
		File importFile = null;
		int fcChoice = fc.showOpenDialog(context.getFrame());

		if (fcChoice != JFileChooser.APPROVE_OPTION) {
		    return;
		}
		importFile = fc.getSelectedFile();
		loadFiles(context, importFile.toURI());
	}

    /**
     * Attempts to read the workspaces at the given URIs, adding them to the
     * given context only after every workspace has been loaded successfully.
     * Any URIs that could not be resolved are discarded after warning the user
     * that the URI(s) was/were unrecognized. The work of loading the workspaces
     * itself is done on a separate worker thread <i>after</i> this method
     * returns; the results of loading are integrated into the context on the
     * Swing Event Dispatch Thread after the worker thread has terminated.
     * <p>
     * The progress of the worker is made visible by use of a dialog with a
     * progress bar and a reasonably fine-grained status message. While that
     * dialog is visible, the session's frame is made unresponsive to mouse and
     * keyboard input.
     * <p>
     * If any of the workspaces whose URIs were deemed valid could not be opened
     * (probably due to IO errors or file corruption), a message to that effect
     * will be displayed to the user. No sessions will be added to the context,
     * even the ones that were loaded successfully before the IO error was
     * encountered.
     * 
     * @param context
     *            The context to open new workspaces into
     * @param importFiles
     *            The URIs to read workspace data from (Wabit XML format)
     */
	public static void loadFiles(final WabitSwingSessionContext context, final URI ... importFiles) {
	    final List<InputStream> ins = new ArrayList<InputStream>();
	    final Map<URI, OpenWorkspaceXMLDAO> workspaceLoaders = new HashMap<URI, OpenWorkspaceXMLDAO>();
	    List<URI> invalidURIs = new ArrayList<URI>();
	    for (URI importFile : importFiles) {
	    	BufferedInputStream in = null;
	    	OpenWorkspaceXMLDAO workspaceLoader = null;
	    	try {
	    	    URL importURL = importFile.toURL();
	    	    URLConnection urlConnection = importURL.openConnection();
	    		in = new BufferedInputStream(urlConnection.getInputStream());
	    		ins.add(in);
    			workspaceLoader = new OpenWorkspaceXMLDAO(context, in, urlConnection.getContentLength());
    			workspaceLoaders.put(importFile, workspaceLoader);
	    	} catch (Exception e) {
	    	    logger.info("Can't deal with URI " + importFile, e);
	    	    invalidURIs.add(importFile);
	    	}
	    }
		
	    if (!invalidURIs.isEmpty()) {
	        StringBuilder message = new StringBuilder();
	        message.append("The following workspace locations will not be opened:");
	        for (URI badURI : invalidURIs) {
	            message.append("\n").append(badURI);
	        }
	        JOptionPane.showMessageDialog(
	                context.getFrame(),
	                message.toString(),
	                "Some workspaces not opened",
	                JOptionPane.WARNING_MESSAGE);
	    }
	        
	    SPSwingWorker worker = new SPSwingWorker(context.getLoadingRegistry()) {
		    
		    /**
		     * Used for communicating the current message of this monitorable worker.
		     * Marked as volatile because it is used from multiple threads concurrently.
		     */
		    private volatile OpenWorkspaceXMLDAO currentDAO;

            @Override
            public void doStuff() throws Exception {
                for (OpenWorkspaceXMLDAO dao : workspaceLoaders.values()) {
                    currentDAO = dao;
                    dao.loadWorkspacesFromStream();
                }
            }
            
            @Override
            public void cleanup() throws Exception {
                for (InputStream in : ins) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.warn(
                                "Failed to close a workspace input stream. " +
                                "Squishing this exception: " + e);
                    }
                }

                if (getDoStuffException() != null) {
                    SPSUtils.showExceptionDialogNoReport(
                            context.getFrame(),
                            "Wabit had trouble opening your workspace",
                            getDoStuffException());
                    return;
                }

                if (!isCancelled()) {
                    for (Map.Entry<URI, OpenWorkspaceXMLDAO> entry : workspaceLoaders.entrySet()) {
                        List<WabitSession> registeredSession = null;
                        try {
                            registeredSession = entry.getValue().addLoadedWorkspacesToContext();
                            for (WabitSession session : registeredSession) {
                                ((WabitSwingSession) session).setCurrentURI(entry.getKey());
                            }
                            
                            // TODO convert recent file menu to recent URI menu
                            try {
                                context.putRecentFileName(new File(entry.getKey()).getAbsolutePath());
                            } catch (IllegalArgumentException ignored) { /* yikes! */ }
                            
                        } catch (Exception e) {
                            SPSUtils.showExceptionDialogNoReport(context.getFrame(),
                                    "Wabit had trouble after opening the workspace located at " +
                                    entry.getKey(), e);
                        }
                    }
                    context.setEditorPanel();
                }
                
            }
            
            @Override
            protected Integer getJobSizeImpl() {
                int jobSize = 0;
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    jobSize += workspaceDAO.getJobSize();
                }
                
                return jobSize;
            }
            
            @Override
            protected String getMessageImpl() {
                OpenWorkspaceXMLDAO myCurrentDAO = currentDAO;
                if (myCurrentDAO != null) { 
                    return myCurrentDAO.getMessage();
                } else {
                    return "";
                }
            }
            
            @Override
            protected int getProgressImpl() {
                int progress = 0;
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    progress += workspaceDAO.getProgress();
                }
                
                return progress;
            }
            
            @Override
            protected boolean hasStartedImpl() {
                boolean started = false;
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    started = started || workspaceDAO.hasStarted();
                }
                
                return started;
            }
            
            /**
             * This worker is finished if all of the workspace loaders are finished,
             * OR if doStuff() has thrown an exception.
             */
            @Override
            protected boolean isFinishedImpl() {
                if (getDoStuffException() != null) {
                    return true;
                }
                
                boolean finished = true;
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    finished = finished && workspaceDAO.isFinished();
                }
                return finished;
            }
            
            @Override
            public synchronized boolean isCancelled() {
                boolean cancelled = false;
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    cancelled = cancelled || workspaceDAO.isCancelled();
                }
                
                return cancelled;
            }
            
            @Override
            public synchronized void setCancelled(boolean cancelled) {
                for (OpenWorkspaceXMLDAO workspaceDAO : workspaceLoaders.values()) {
                    workspaceDAO.setCancelled(cancelled);
                }
            }
		    
		};
		
		OpenProgressWindow.showProgressWindow(context.getFrame(), worker);
		new Thread(worker).start();
	}

    /**
     * This will load a Wabit workspace file in a new
     * session in the given context through an input stream. This is slightly
     * different from loading from a file as no default file to save to will be
     * specified and nothing will be added to the recent files menu.
     */
	public static void loadFile(InputStream input, final WabitSwingSessionContext context, int bytesInStream) {
	    
	    final BufferedInputStream in = new BufferedInputStream(input);
	    final OpenWorkspaceXMLDAO workspaceLoader = new OpenWorkspaceXMLDAO(context, in, bytesInStream);
	    SPSwingWorker worker = new SPSwingWorker(context.getLoadingRegistry()) {

	        @Override
	        public void doStuff() throws Exception {
	            workspaceLoader.loadWorkspacesFromStream();
	        }
	        
	        @Override
	        public void cleanup() throws Exception {
	            if (getDoStuffException()!= null) {
	                throw new RuntimeException(getDoStuffException());
	            }
	            if (!isCancelled()) {
	                workspaceLoader.addLoadedWorkspacesToContext();
	                context.setEditorPanel();
	            }
	            try {
	                in.close();
	            } catch (IOException e) {
	                logger.error(e);
	            }
	        }
	        
	        @Override
	        protected Integer getJobSizeImpl() {
	            return workspaceLoader.getJobSize();
	        }
	        
	        @Override
	        protected String getMessageImpl() {
	            return workspaceLoader.getMessage();
	        }
	        
	        @Override
	        protected int getProgressImpl() {
	            return workspaceLoader.getProgress();
	        }
	        
	        @Override
	        protected boolean hasStartedImpl() {
	            return workspaceLoader.hasStarted();
	        }
	        
	        @Override
	        protected boolean isFinishedImpl() {
	            return workspaceLoader.isFinished();
	        }
	        
	        @Override
            public synchronized boolean isCancelled() {
                return workspaceLoader.isCancelled();
            }
            
            @Override
            public synchronized void setCancelled(boolean cancelled) {
                workspaceLoader.setCancelled(cancelled);
            }
	        
	    };
	    
	    OpenProgressWindow.showProgressWindow(context.getFrame(), worker);
	    new Thread(worker).start();
	}

}
