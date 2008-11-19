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

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DocumentAppender;
import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.action.LogAction;
import ca.sqlpower.wabit.swingui.report.ReportLayoutPanel;
import ca.sqlpower.wabit.swingui.tree.ProjectTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.ProjectTreeModel;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSessionImpl implements WabitSwingSession {
	
	/**
	 * A constant for storing the location of the query dividers in prefs.
	 */
	private static final String QUERY_DIVIDER_LOCATON = "QueryDividerLocaton";

	private static Logger logger = Logger.getLogger(WabitSwingSessionImpl.class);
	
	private final WabitSessionContext sessionContext;
	
	private final WabitProject project;
	
	private JTree projectTree;
	private JSplitPane wabitPane;
	private JFrame frame;
	private static JLabel statusLabel;
	
	private final Preferences prefs = Preferences.userNodeForPackage(WabitSwingSessionImpl.class);
	
	/**
	 * All information useful to the user in a log format should be logged here.
	 * The user can get access to the contents of this log from the window's menu.
	 */
	private final Logger userInformationLogger = Logger.getLogger("User Info Log");

	/**
	 * The list of all currently-registered background tasks.
	 */
	private final List<SPSwingWorker> activeWorkers =
		Collections.synchronizedList(new ArrayList<SPSwingWorker>());
	
	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();

	/**
	 * This is the current panel to the right of the JTree showing the parts of the 
	 * project. This will allow editing the currently selected element in the JTree.
	 */
	private DataEntryPanel currentEditorPanel;

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSessionImpl(WabitSessionContext context) {
	    project = new WabitProject();
		sessionContext = context;
		sessionContext.registerChildSession(this);
		
		statusLabel= new JLabel();
		
	}
	/**
	 * sets the StatusMessage
	 */
	public static void setStatusMessage (String msg) {
		statusLabel.setText(msg);	
	}
	
	/**
	 *  Builds the GUI
	 * @throws ArchitectException 
	 */
    public void buildUI() throws ArchitectException {
        frame = new JFrame("Power*Wabit");
        
        // this will be the frame's content pane
		JPanel cp = new JPanel(new BorderLayout());
    	
    	wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	
		projectTree = new JTree(new ProjectTreeModel(project));
		projectTree.addMouseListener(new ProjectTreeListener(this));
    	projectTree.setCellRenderer(new ProjectTreeCellRenderer());

        wabitPane.add(new JScrollPane(projectTree), JSplitPane.LEFT);
		setEditorPanel(new QueryCache());
    	
		//prefs
    	if(prefs.get("MainDividerLocaton", null) != null) {
            String[] dividerLocations = prefs.get("MainDividerLocaton", null).split(",");
            wabitPane.setDividerLocation(Integer.parseInt(dividerLocations[0]));
        }
        
        JPanel statusPane = new JPanel(new BorderLayout());
        statusPane.add(statusLabel, BorderLayout.CENTER);
		
		MemoryMonitor memoryMonitor = new MemoryMonitor();
		memoryMonitor.start();
		JLabel memoryLabel = memoryMonitor.getLabel();
		memoryLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
		statusPane.add(memoryLabel, BorderLayout.EAST);
		
		cp.add(wabitPane, BorderLayout.CENTER);
        cp.add(statusPane, BorderLayout.SOUTH);
        
        JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
        
		JMenu windowMenu = new JMenu("Window");
		fileMenu.setMnemonic('w');
		menuBar.add(windowMenu);
		JTextArea logTextArea = new JTextArea();
		DocumentAppender docAppender = new DocumentAppender(logTextArea.getDocument());
		userInformationLogger.addAppender(docAppender);
		JMenuItem logMenuItem = new JMenuItem(new LogAction(frame, logTextArea ));
		windowMenu.add(logMenuItem);
		
		frame.setJMenuBar(menuBar);
        frame.setContentPane(cp);
        
        //prefs
        if (prefs.get("frameBounds", null) != null) {
            String[] frameBounds = prefs.get("frameBounds", null).split(",");
            if (frameBounds.length == 4) {
                frame.setBounds(
                        Integer.parseInt(frameBounds[0]),
                        Integer.parseInt(frameBounds[1]),
                        Integer.parseInt(frameBounds[2]),
                        Integer.parseInt(frameBounds[3]));
            }
        } else {
        	frame.setSize(950, 550);
        	frame.setLocation(300, 200);
        }

        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
        	
        	@Override
			public void windowClosing(WindowEvent e) {
                try {
                	prefs.put("MainDividerLocaton", String.format("%d", wabitPane.getDividerLocation()));
                    prefs.put("frameBounds", String.format("%d,%d,%d,%d", frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight()));
                    prefs.flush();
                } catch (BackingStoreException ex) {
                    logger.log(Level.WARN,"Failed to flush preferences", ex);
                }

				close();
			}});

        logger.debug("UI is built.");
    }
    
    public JTree getTree() {
    	return projectTree;
    }

    /* docs inherited from interface */
	public void registerSwingWorker(SPSwingWorker worker) {
		activeWorkers.add(worker);
	}

    /* docs inherited from interface */
	public void removeSwingWorker(SPSwingWorker worker) {
		activeWorkers.remove(worker);
	}

	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.add(l);
	}

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.remove(l);
	}

	/**
	 * Ends this session, disposing its frame and releasing any system
	 * resources that were obtained explicitly by this session. Also
	 * fires a sessionClosing lifecycle event, so any resources used up
	 * by subsystems dependent on this session can be freed by the appropriate
	 * parties.
	 */
    public void close() {
    	
    	if (!removeEditorPanel()) {
    		return;
    	}
    	
    	SessionLifecycleEvent<WabitSession> e =
    		new SessionLifecycleEvent<WabitSession>(this);
    	for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
    		lifecycleListeners.get(i).sessionClosing(e);
    	}
    	frame.dispose();
    	sessionContext.deregisterChildSession(this);
    }
    
    /**
     * Launches the Wabit application by loading the configuration and
     * displaying the GUI.
     * 
     * @throws Exception if startup fails
     */
    public static void  main(String[] args) throws Exception {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wabit");
    	System.setProperty("apple.laf.useScreenMenuBar", "true");
    	WabitSessionContext context = new WabitSessionContextImpl(true);
        WabitSwingSessionImpl wss = new WabitSwingSessionImpl(context);
        wss.buildUI();
    }

    public WabitProject getProject() {
        return project;
    }
    
	public WabitSessionContext getContext() {
		return sessionContext;
	}
	
	public Logger getUserInformationLogger() {
		return userInformationLogger;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public void setEditorPanel(Object entryPanelModel) {
		if (!removeEditorPanel()) {
			return;
		}
		if (entryPanelModel instanceof QueryCache) {
			QueryPanel queryPanel = new QueryPanel(this, (QueryCache)entryPanelModel);
		   	if (prefs.get(QUERY_DIVIDER_LOCATON, null) != null) {
	            String[] dividerLocations = prefs.get(QUERY_DIVIDER_LOCATON, null).split(",");
	            queryPanel.getTopRightSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[0]));
	            queryPanel.getFullSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[1]));
		   	}
		   	currentEditorPanel = queryPanel;
		} else if (entryPanelModel instanceof Layout) {
			currentEditorPanel = new ReportLayoutPanel(this, (Layout) entryPanelModel);
		} else {
			throw new IllegalStateException("Unknown model for the defined types of entry panels. The type is " + entryPanelModel.getClass());
		}
		wabitPane.add(currentEditorPanel.getPanel(), JSplitPane.RIGHT);
	}
	
	/**
	 * This will close the editor panel the user is currently modifying if 
	 * the user has no changes or discards their changes. This will return true
	 * if the panel was properly closed or false if it was not closed (ie: due to
	 * unsaved changes).
	 */
	private boolean removeEditorPanel() {
		if (currentEditorPanel != null && currentEditorPanel.hasUnsavedChanges()) {
			int retval = JOptionPane.showConfirmDialog(frame, "There are unsaved changes. Discard?", "Discard changes", JOptionPane.YES_NO_OPTION);
			if (retval == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		if (currentEditorPanel != null) {
			if (currentEditorPanel instanceof QueryPanel) {
				QueryPanel query = (QueryPanel)currentEditorPanel;
				prefs.put(QUERY_DIVIDER_LOCATON, String.format("%d,%d", query.getTopRightSplitPane().getDividerLocation(), query.getFullSplitPane().getDividerLocation()));
			}
			currentEditorPanel.discardChanges();
			wabitPane.remove(currentEditorPanel.getPanel());
		}
		return true;
	}
	
}
