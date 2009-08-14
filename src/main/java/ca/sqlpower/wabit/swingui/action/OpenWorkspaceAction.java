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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import com.rc.retroweaver.runtime.Collections;

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
            defaultFile = context.getActiveSwingSession().getCurrentFile();
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

		try {
            loadFile(importFile, context);
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(context.getFrame(), "Cannot find file " + importFile.getName() + " to open.",
                    "Cannot Find File", JOptionPane.WARNING_MESSAGE);
        }
		
	}

	/**
	 * This will load a Wabit workspace file in a new session in the given context.
	 */
	@SuppressWarnings("unchecked")
    public static void loadFile(final File importFile, final WabitSwingSessionContext context) 
	    throws FileNotFoundException {
	    loadFiles(Collections.singletonList(importFile), context);
	}
	
	public static void loadFiles(final List<File> importFiles, final WabitSwingSessionContext context) 
	        throws FileNotFoundException {
	    final List<InputStream> ins = new ArrayList<InputStream>();
	    final Map<File, OpenWorkspaceXMLDAO> workspaceLoaders = new HashMap<File, OpenWorkspaceXMLDAO>();
	    for (File importFile : importFiles) {
	    	BufferedInputStream in = null;
	    	OpenWorkspaceXMLDAO workspaceLoader = null;
	    	try {
	    		in = new BufferedInputStream(new FileInputStream(importFile));
    			workspaceLoader = new OpenWorkspaceXMLDAO(context, in, (int) importFile.length());
	    	} catch (Exception e) {
	    		SPSUtils.showExceptionDialogNoReport(context.getFrame(), "Error occured while loading the " +
	    				"workspace located at: " + importFile.getAbsolutePath(), e);
	    		continue; //continue on but still show the user an error
	    	}
			ins.add(in);
	        workspaceLoaders.put(importFile, workspaceLoader);
	    }
		
		SPSwingWorker worker = new SPSwingWorker(context) {
		    
		    private OpenWorkspaceXMLDAO currentDAO;

            @Override
            public void doStuff() throws Exception {
                for (Map.Entry<File, OpenWorkspaceXMLDAO> entry : workspaceLoaders.entrySet()) {
                    currentDAO = entry.getValue();
                    entry.getValue().loadWorkspacesFromStream();
                }
            }
            
            @Override
            public void cleanup() throws Exception {
                if (getDoStuffException() != null) {
                    throw new RuntimeException(getDoStuffException());
                }

                if (!isCancelled()) {
                    for (Map.Entry<File, OpenWorkspaceXMLDAO> entry : workspaceLoaders.entrySet()) {
                        List<WabitSession> loadFile = null;
                        try {
                            loadFile = entry.getValue().addLoadedWorkspacesToContext();
                        } catch (Exception e) {
                            SPSUtils.showExceptionDialogNoReport(context.getFrame(), "Error occured while " +
                            		"loading the workspace located at: " + entry.getKey().getAbsolutePath(), e);
                            continue; //continue on but still show the user an error
                        }
                        context.setEditorPanel();
                        for (WabitSession session : loadFile) {
                            ((WabitSwingSession) session).setCurrentFile(entry.getKey());
                        }
                        context.putRecentFileName(entry.getKey().getAbsolutePath());
                    }
                }
                
                for (InputStream in : ins) {
                    
                    try {
                        in.close();
                    } catch (IOException e) {
                        // squishing exception to not hide other exceptions.
                    }
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
                if (currentDAO != null) { 
                    return currentDAO.getMessage();
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
            
            @Override
            protected boolean isFinishedImpl() {
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
	    SPSwingWorker worker = new SPSwingWorker(context) {

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
