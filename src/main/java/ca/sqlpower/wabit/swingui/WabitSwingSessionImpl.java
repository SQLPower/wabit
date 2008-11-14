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
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectRoot;
import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.architect.swingui.dbtree.DBTreeModel;
import ca.sqlpower.architect.swingui.dbtree.DnDTreePathTransferable;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.DocumentAppender;
import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.event.SessionLifecycleEvent;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.LogAction;


/**
 * The Main Window for the Wabit Application; contains a main() method that is
 * the conventional way to start the application running.
 */
public class WabitSwingSessionImpl implements WabitSwingSession {
	
	private static Logger logger = Logger.getLogger(WabitSwingSessionImpl.class);
	
	private final WabitSessionContext sessionContext;
	
	private JTree projectTree;
	private JFrame frame;
	private static JLabel statusLabel;
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

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSessionImpl(WabitSessionContext context) {
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
    	
    	JSplitPane wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    	
    	rootNode = new SQLObjectRoot();
        for (SPDataSource ds : sessionContext.getDataSources().getConnections()) {
            rootNode.addChild(new SQLDatabase(ds));
        }
    	final DBTreeModel treeModel = new DBTreeModel(rootNode);
		projectTree = new JTree(treeModel);
		projectTree.addMouseListener(new PopUpMenuListener());
    	projectTree.setCellRenderer(new DBTreeCellRenderer());
    	DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(projectTree, DnDConstants.ACTION_COPY, new DragGestureListener() {
			
			public void dragGestureRecognized(DragGestureEvent dge) {
				
				if(projectTree.getSelectionPaths() == null) {
					return;
				}
				ArrayList<int[]> list = new ArrayList<int[]>();
				for (TreePath path : projectTree.getSelectionPaths()) {
					Object selectedNode = path.getLastPathComponent();
					if (!(selectedNode instanceof SQLObject)) {
						throw new IllegalStateException("DBTrees are not allowed to contain non SQLObjects. This tree contains a " + selectedNode.getClass());
					}
					int[] dndPathToNode = DnDTreePathTransferable.getDnDPathToNode((SQLObject)selectedNode, rootNode);
					list.add(dndPathToNode);
				}
					
				Object firstSelectedObject = projectTree.getSelectionPath().getLastPathComponent();
				String name;
				if (firstSelectedObject instanceof SQLObject) {
					name = ((SQLObject) firstSelectedObject).getName();
				} else {
					name = firstSelectedObject.toString();
				}
				
				Transferable dndTransferable = new DnDTreePathTransferable(list, name);
				dge.getDragSource().startDrag(dge, null, dndTransferable, new DragSourceListener() {
					public void dropActionChanged(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragOver(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragExit(DragSourceEvent dse) {
						//do nothing
					}
					public void dragEnter(DragSourceDragEvent dsde) {
						//do nothing
					}
					public void dragDropEnd(DragSourceDropEvent dsde) {
						//do nothing
					}
				});
			}
		});

        wabitPane.add(new JScrollPane(projectTree), JSplitPane.LEFT);
        wabitPane.add(new QueryPanel(this).getSplitPane(), JSplitPane.RIGHT);
        
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
        frame.setSize(800, 500);
        frame.setLocation(500, 400);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
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

	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();

	private SQLObjectRoot rootNode;
	
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

	public SQLObjectRoot getRootNode() {
		return rootNode;
	}
	
	public WabitSessionContext getContext() {
		return sessionContext;
	}
	
	/**
	 * A PopUpMenuListener which is current used for the ProjectTree.
	 * It will Display a List of options once you right click on the ProjectTree.
	 *
	 */
	private class PopUpMenuListener extends MouseAdapter {

		JPopupMenu menu;
		DatabaseConnectionManager dbConnectionManager;

		PopUpMenuListener() {
			menu = new JPopupMenu();
			dbConnectionManager = new DatabaseConnectionManager(sessionContext.getDataSources());
			menu.add(new AbstractAction("Database ConnectionManager..."){

				public void actionPerformed(ActionEvent e) {
					 dbConnectionManager.showDialog(frame);
				}});

		}

		public void mouseClicked(MouseEvent e) {
			
			if (e.getButton() == MouseEvent.BUTTON3) {
				menu.show(e.getComponent(), e.getX(), e.getY());
			} else {
				menu.setVisible(false);
			}

		}
	}

	public Logger getUserInformationLogger() {
		return userInformationLogger;
	}
	
}
