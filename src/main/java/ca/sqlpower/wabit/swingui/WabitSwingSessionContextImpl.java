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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.MemoryMonitor;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory;
import ca.sqlpower.swingui.action.ForumAction;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.ServerListListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.action.AboutAction;
import ca.sqlpower.wabit.swingui.action.HelpAction;
import ca.sqlpower.wabit.swingui.action.ImportWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.NewServerWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.NewWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.OpenWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveServerWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveWorkspaceAsAction;
import ca.sqlpower.wabit.swingui.olap.OlapQueryPanel;
import ca.sqlpower.wabit.swingui.report.ReportLayoutPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the swing version of the WabitSessionContext. Swing specific operations for
 * the context will be done in this implementation 
 */
public class WabitSwingSessionContextImpl implements WabitSwingSessionContext {

    private static final Logger logger = Logger.getLogger(WabitSwingSessionContextImpl.class);
    
    /**
     * This icon is at the top left of every frame.
     */
    public static final ImageIcon FRAME_ICON = new ImageIcon(
            WabitSwingSessionImpl.class.getResource("/icons/wabit-16.png"));
    
    /**
     * The icon for the "Open Demonstration Workspace" button.
     */
    private static final Icon OPEN_DEMO_ICON = new ImageIcon(
            WabitWelcomeScreen.class.getClassLoader().getResource("icons/wabit-16.png"));
    
    private static final int DEFAULT_DIVIDER_LOC = 50;
    
    /**
     * A constant for storing the location of the query dividers in prefs.
     */
    private static final String QUERY_DIVIDER_LOCATON = "QueryDividerLocaton";

    /**
     * A constant for storing the location of the divider for layouts in prefs.
     */
    private static final String LAYOUT_DIVIDER_LOCATION = "LayoutDividerLocation";
    
    /**
     * This listener is attached to the context's frame to call close
     * when the frame is going away. This way we can prompt to save
     * changes and cleanup.
     */
    private final WindowListener windowClosingListener = new WindowAdapter() {
        
        @Override
        public void windowClosing(WindowEvent e) {
            close();
        };
        
    };

    /**
     * The core session context that this swing session context delegates its
     * "core" operations to.
     */
    private final WabitSessionContext delegateContext;

    private final SwingUIUserPrompterFactory upf = new SwingUIUserPrompterFactory(null);
    
    /**
     * This is a preference that stores if the app should start up on the welcome screen
     * or it should start on the last loaded/saved workspace. 
     */
    private static final String PREFS_START_ON_WELCOME_SCREEN = "START_ON_WELCOME_SCREEN";
    
    public static final ForumAction FORUM_ACTION = new ForumAction(
            new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/wabit-24px.png")), 
            "Go to Wabit support forum");
    
	/**
	 * This is the prefs for the entire context.
	 */
	private final Preferences prefs = Preferences.userNodeForPackage(WabitSwingSessionContextImpl.class);
	
	/**
	 * This is the main frame of the context.
	 */
	private final JFrame frame;
	
	/**
	 * This is the main split pane that shows the tree on the left of the split
	 * and the current editor on the right of the split.
	 */
	private final JSplitPane wabitPane;
	
	/**
	 * This action will display an about dialog that is parented to the {@link #frame}.
	 */
	private AbstractAction aboutAction;
	
	/**
	 * This is the status label at the bottom of one of the windows.
	 */
	private final JLabel statusLabel;
	
	/**
     * This is the current panel to the right of the JTree showing the parts of the 
     * workspace. This will allow editing the currently selected element in the JTree.
     */
    private WabitPanel currentEditorPanel;
    
    /**
     * This tabbed pane contains all of the trees for each swing session in the context.
     */
    private JTabbedPane treeTabbedPane;
    
    /**
     * This is the current session that is being changed by the user.
     */
    private WabitSwingSession activeSession;
    
    /**
     * This is the limit of all result sets in Wabit. Changing this spinner
     * will cause cached result sets to be flushed.
     */
    private final JSpinner rowLimitSpinner;
    
    /**
     * This tracks the old row limit for firing an appropriate event when the row
     * limit spinner changes.
     */
    private int oldRowLimitValue;
    
    /**
     * This object will fire property changes for the context when values change.
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * This is the most recent file loaded in this context or the last
     * file that the context was saved to. This will be null if no file has
     * been loaded or the workspaces has not been saved yet.
     */
    private File currentFile = null;
    
    /**
     * The list of all currently-registered background tasks.
     */
    private final List<SPSwingWorker> activeWorkers =
        Collections.synchronizedList(new ArrayList<SPSwingWorker>());
    
    /**
     * This welcome screen's panel will be displayed when there is no active session
     * available.
     */
    private final WabitWelcomeScreen welcomeScreen = new WabitWelcomeScreen(this);
    
    /**
     * This action will close all of the open sessions and, if successful, close the app.
     */
    private final Action exitAction = new AbstractAction("Exit") {
        public void actionPerformed(ActionEvent e) {
            close();
        }
    };

	/**
	 * @param terminateWhenLastSessionCloses
	 *            Set to true if the context should stop the app when the last
	 *            session is closed. If false the app will have to be closed in
	 *            a way other than closing all of the sessions.
	 * @param headless
	 *            Set to true to not create any GUI objects when the context
	 *            starts. This stops the welcome screen from being created.
	 */
	public WabitSwingSessionContextImpl(WabitSessionContext delegateContext, boolean headless)
			throws IOException, SQLObjectException {
		this.delegateContext = delegateContext;
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
        frame = new JFrame("Wabit " + WabitVersion.VERSION + " - " + getName());
        wabitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        statusLabel= new JLabel();

        rowLimitSpinner = new JSpinner();
        final JSpinner.NumberEditor rowLimitEditor = new JSpinner.NumberEditor(getRowLimitSpinner());
        getRowLimitSpinner().setEditor(rowLimitEditor);
        getRowLimitSpinner().setValue(1000);
        rowLimitSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pcs.firePropertyChange(QueryCache.ROW_LIMIT, oldRowLimitValue, 
                        ((Integer) rowLimitSpinner.getValue()).intValue());
                oldRowLimitValue = (Integer) rowLimitSpinner.getValue();
            }
        });
        
        if (!headless) {
            buildUI();
            macOSXRegistration();
        }
	}
	
	public WabitSession createSession() {
	    final WabitSwingSessionImpl session = new WabitSwingSessionImpl(this);
	    registerChildSession(session);
        return session;
	}
	
	public WabitSession createServerSession(WabitServerInfo serverInfo) {
        final WabitSwingSessionImpl session = new WabitSwingSessionImpl(serverInfo, this);
        registerChildSession(session);
        return session;
    }
	
	public RecentMenu createRecentMenu() {
		RecentMenu menu = new RecentMenu(this.getClass()) {
			
			@Override
			public void loadFile(String fileName) throws IOException {
				File file = new File(fileName);
				OpenWorkspaceAction.loadFile(file, WabitSwingSessionContextImpl.this);
			}
		};
		
		menu.setText("Open Recent Workspace");
		
		return menu;
	}
//
//	public JMenu createServerListMenu(Component dialogOwner) {
//	    return new ServerListMenu(this, "Open On Server", dialogOwner);
//	}
	
	public void deregisterChildSession(WabitSession child) {
	    delegateContext.deregisterChildSession(child);
	    
	    treeTabbedPane.removeTabAt(getSessions().indexOf(child));
	    // TODO: Re-enabled this fix for the open most recent project
	    // after the bug where the wrong recent project is opened is fixed.
//		if (getSessionCount() == 0 && !closing) {
	    if (getSessionCount() == 0) {
			logger.debug("Wrote true in deregisterChildSession() to " + PREFS_START_ON_WELCOME_SCREEN + " preference");
			getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, true);
		}
	}
	
	/**
     * Registers this application in Mac OS X if we're running on that platform.
     *
     * <p>This code came from Apple's "OS X Java Adapter" example.
     */
    private void macOSXRegistration() {

        Action prefAction = new AbstractAction() {
		
			public void actionPerformed(ActionEvent e) {
				// TODO Implement prefs in Mac
			}
		};
		
		Action exitAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		};

        // Whether or not this is OS X, the three actions we're referencing must have been initialized by now.
        if (exitAction == null) throw new IllegalStateException("Exit action has not been initialized"); //$NON-NLS-1$
        if (prefAction == null) throw new IllegalStateException("Prefs action has not been initialized"); //$NON-NLS-1$
        if (aboutAction == null) throw new IllegalStateException("About action has not been initialized"); //$NON-NLS-1$

        if (isMacOSX()) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter"); //$NON-NLS-1$

                // The main registration method.  Takes quitAction, prefsAction, aboutAction.
                Class[] defArgs = { Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs); //$NON-NLS-1$
                Object[] args = { exitAction, prefAction, aboutAction };
                registerMethod.invoke(osxAdapter, args);

                // The enable prefs method.  Takes a boolean.
                defArgs = new Class[] { boolean.class };
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs); //$NON-NLS-1$
                args = new Object[] {Boolean.TRUE};
                prefsEnableMethod.invoke(osxAdapter, args);
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  " +
                		"Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  " +
                		"Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }
    
    /**
     *  Builds the GUI
     * @throws SQLObjectException 
     */
    public void buildUI() throws SQLObjectException {
        frame.setIconImage(FRAME_ICON.getImage());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(windowClosingListener);
        aboutAction = new AboutAction(frame);
        
        // this will be the frame's content pane
        JPanel cp = new JPanel(new BorderLayout());
        
        treeTabbedPane = new JTabbedPane();
        for (WabitSession session : getSessions()) {
            treeTabbedPane.addTab(session.getWorkspace().getName(), ((WabitSwingSession) session).getTree());
        }
        final ChangeListener tabChangeListener = new ChangeListener() {
        
            public void stateChanged(ChangeEvent e) {
                final int selectedIndex = treeTabbedPane.getSelectedIndex();
                if (selectedIndex >= 0) {
                    setActiveSession((WabitSwingSession) getSessions().get(selectedIndex));
                    setEditorPanel();
                }
            }
        };
        treeTabbedPane.addChangeListener(tabChangeListener);
        
        wabitPane.add(treeTabbedPane, JSplitPane.LEFT);
        
        //prefs
        if(prefs.get("MainDividerLocaton", null) != null) {
            String[] dividerLocations = prefs.get("MainDividerLocaton", null).split(",");
            wabitPane.setDividerLocation(Integer.parseInt(dividerLocations[0]));
        }
        
        DefaultFormBuilder statusBarBuilder = new DefaultFormBuilder(
                new FormLayout("pref:grow, 4dlu, pref, 2dlu, max(50dlu; pref), 4dlu, pref"));
        statusBarBuilder.append(statusLabel);
        
        statusBarBuilder.append("Row Limit", getRowLimitSpinner());
        
        MemoryMonitor memoryMonitor = new MemoryMonitor();
        memoryMonitor.start();
        JLabel memoryLabel = memoryMonitor.getLabel();
        memoryLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
        statusBarBuilder.append(memoryLabel);
        
        cp.add(wabitPane, BorderLayout.CENTER);
        cp.add(statusBarBuilder.getPanel(), BorderLayout.SOUTH);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        menuBar.add(fileMenu);
        fileMenu.add(new NewWorkspaceAction(this));
        fileMenu.add(new OpenWorkspaceAction(this));
        fileMenu.add(createRecentMenu());
        
        fileMenu.addSeparator();
        JMenuItem openDemoMenuItem = new JMenuItem(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                OpenWorkspaceAction.loadFile(WabitWelcomeScreen.class.getResourceAsStream(
                        "/ca/sqlpower/wabit/example_workspace.wabit"), WabitSwingSessionContextImpl.this);
            }
        });
        
        fileMenu.add(createServerListMenu(frame, "New Server Workspace", new ServerListMenuItemFactory() {
            public JMenuItem createMenuEntry(WabitServerInfo serviceInfo, Component dialogOwner) {
                return new JMenuItem(new NewServerWorkspaceAction(dialogOwner, serviceInfo, WabitSwingSessionContextImpl.this));
            }
        }));
        fileMenu.add(createServerListMenu(frame, "Open Server Workspace", new ServerListMenuItemFactory() {
            public JMenuItem createMenuEntry(WabitServerInfo serviceInfo, Component dialogOwner) {
                return new OpenOnServerMenu(dialogOwner, serviceInfo, WabitSwingSessionContextImpl.this);
            }
        }));
        
        fileMenu.addSeparator();
        openDemoMenuItem.setText("Open Demo Workspace");
        openDemoMenuItem.setIcon(OPEN_DEMO_ICON);
        fileMenu.add(openDemoMenuItem);
        
        fileMenu.addSeparator();
        fileMenu.add(new ImportWorkspaceAction(this));
        
        fileMenu.addSeparator();
        fileMenu.add(new SaveWorkspaceAction(this));
        fileMenu.add(new SaveWorkspaceAsAction(this));
        fileMenu.add(createServerListMenu(frame, "Save Workspace on Server", new ServerListMenuItemFactory() {
            public JMenuItem createMenuEntry(WabitServerInfo serviceInfo, Component dialogOwner) {
                try {
                    return new JMenuItem(new SaveServerWorkspaceAction(serviceInfo, dialogOwner, activeSession.getWorkspace(), WabitSwingSessionContextImpl.this));
                } catch (Exception e) {
                    JMenuItem menuItem = new JMenuItem(e.toString());
                    menuItem.setEnabled(false);
                    // TODO it would be nice to have ASUtils.createExceptionMenuItem(Throwable)
                    return menuItem;
                }
            }
        }));
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Close Workspace") {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        fileMenu.addSeparator();
        JMenuItem databaseConnectionManager = new JMenuItem(new AbstractAction("Database Connection Manager...") {
            public void actionPerformed(ActionEvent e) {
                 ((WabitSwingSession) activeSession).getDbConnectionManager().showDialog(getFrame());
            }
        });
        fileMenu.add(databaseConnectionManager);

        
        if (!isMacOSX()) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }
        
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('v');
        menuBar.add(viewMenu);
        JMenuItem maxEditor = new JMenuItem(new AbstractAction("Maximize Editor") {
            public void actionPerformed(ActionEvent e) {
                if (currentEditorPanel != null) {
                    currentEditorPanel.maximizeEditor();
                }
            }
        });
        maxEditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));
        viewMenu.add(maxEditor);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        menuBar.add(helpMenu);
        if (!isMacOSX()) {
            helpMenu.add(aboutAction);
            helpMenu.addSeparator();
        }
        helpMenu.add(SPSUtils.forumAction);
        helpMenu.add(new HelpAction(frame));
    
        frame.setJMenuBar(menuBar);
        frame.setContentPane(cp);
        
        //prefs
        if (prefs.get("frameBounds", null) != null) {
            String[] frameBounds = prefs.get("frameBounds", null).split(",");
            if (frameBounds.length == 4) {
                logger.debug("Frame bounds are " + Integer.parseInt(frameBounds[0]) + ", " 
                        + Integer.parseInt(frameBounds[1]) + ", " +
                        Integer.parseInt(frameBounds[2]) + ", " + Integer.parseInt(frameBounds[3]));
                frame.setBounds(
                        Integer.parseInt(frameBounds[0]),
                        Integer.parseInt(frameBounds[1]),
                        Integer.parseInt(frameBounds[2]),
                        Integer.parseInt(frameBounds[3]));
            }
        } else {
            frame.setSize(1050, 750);
            frame.setLocation(200, 100);
        }

        frame.setVisible(true);
        
        logger.debug("UI is built.");
    }
    
    public boolean setEditorPanel() {
        if (isLoading()) return false;
        if (!removeEditorPanel()) {
            return false;
        }
        int dividerLoc;
        if (currentEditorPanel != null) {
            dividerLoc = wabitPane.getDividerLocation();
        } else {
            if(prefs.get("MainDividerLocaton", null) != null) {
                String[] dividerLocations = prefs.get("MainDividerLocaton", null).split(",");
                dividerLoc = Integer.parseInt(dividerLocations[0]);
            } else {
                dividerLoc = DEFAULT_DIVIDER_LOC;
            }
        }
        
        if (currentEditorPanel != null) {
            wabitPane.remove(currentEditorPanel.getPanel());
        }
        
        WabitObject entryPanelModel = null;
        if (activeSession != null) {
            entryPanelModel = activeSession.getWorkspace().getEditorPanelModel();
        }
        
        currentEditorPanel = createEditorPanel(entryPanelModel);
        
        wabitPane.add(currentEditorPanel.getPanel(), JSplitPane.RIGHT);
        wabitPane.setDividerLocation(dividerLoc);
        // The execute query currently needs to be done after the panel is added
        // to the split pane, because it requires a Graphics2D object to get a
        // FontMetrics to use to calculate optimal column widths in the
        // CellSetViewer. If done before, the Graphics2D object is null.
        if (currentEditorPanel instanceof OlapQueryPanel) {
            try {
                ((OlapQueryPanel) currentEditorPanel).updateCellSet(((OlapQuery) entryPanelModel).execute());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // TODO Select the proper panel in the wabit tree
        return true;
    }

    /**
     * This is a helper method for {@link #setEditorPanel()} that will create
     * the panel to edit the model object given.
     */
    private WabitPanel createEditorPanel(WabitObject entryPanelModel) {
        if (activeSession == null) {
            currentEditorPanel = welcomeScreen.getPanel();
        } else if (entryPanelModel instanceof QueryCache) {
            QueryPanel queryPanel = new QueryPanel(activeSession, (QueryCache) entryPanelModel);
            if (prefs.get(QUERY_DIVIDER_LOCATON, null) != null) {
                String[] dividerLocations = prefs.get(QUERY_DIVIDER_LOCATON, null).split(",");
                queryPanel.getTopRightSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[0]));
                queryPanel.getFullSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[1]));
            }
            currentEditorPanel = queryPanel;
        } else if (entryPanelModel instanceof OlapQuery) {
            OlapQueryPanel panel = new OlapQueryPanel(activeSession, wabitPane, (OlapQuery) entryPanelModel);
            currentEditorPanel = panel;
        } else if (entryPanelModel instanceof Layout) {
            ReportLayoutPanel rlPanel = new ReportLayoutPanel(activeSession, (Layout) entryPanelModel);
            if (prefs.get(LAYOUT_DIVIDER_LOCATION, null) != null) {
                rlPanel.getSplitPane().setDividerLocation(Integer.parseInt(prefs.get(LAYOUT_DIVIDER_LOCATION, null)));
            }
            currentEditorPanel = rlPanel;
        } else if (entryPanelModel instanceof WabitWorkspace) {
            currentEditorPanel = new WorkspacePanel(activeSession);
        } else {
            if (entryPanelModel instanceof WabitObject && ((WabitObject) entryPanelModel).getParent() != null) {
                currentEditorPanel = createEditorPanel(((WabitObject) entryPanelModel).getParent()); 
            } else {
                throw new IllegalStateException("Unknown model for the defined types of entry panels. " +
                        "The type is " + entryPanelModel.getClass());
            }
        }
        return currentEditorPanel;
    }
    
    /**
     * This will close the editor panel the user is currently modifying if 
     * the user has no changes or discards their changes. This will return true
     * if the panel was properly closed or false if it was not closed (ie: due to
     * unsaved changes).
     */
    private boolean removeEditorPanel() {
        if (currentEditorPanel != null && currentEditorPanel.hasUnsavedChanges()) {
            int retval = JOptionPane.showConfirmDialog(frame, "There are unsaved changes. Discard?", 
                    "Discard changes", JOptionPane.YES_NO_OPTION);
            if (retval == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        if (currentEditorPanel != null) {
            if (currentEditorPanel instanceof QueryPanel) {
                QueryPanel query = (QueryPanel)currentEditorPanel;
                prefs.put(QUERY_DIVIDER_LOCATON, String.format("%d,%d", 
                        query.getTopRightSplitPane().getDividerLocation(), 
                        query.getFullSplitPane().getDividerLocation()));
            } else if (currentEditorPanel instanceof ReportLayoutPanel) {
                prefs.put(LAYOUT_DIVIDER_LOCATION, String.format("%d", 
                        ((ReportLayoutPanel) currentEditorPanel).getSplitPane().getDividerLocation()));
            }
            currentEditorPanel.discardChanges();
        }
        return true;
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public List<WabitSession> getSessions() {
        return delegateContext.getSessions();
    }

	public void putRecentFileName(String fileName) {
		createRecentMenu().putRecentFileName(fileName);
		getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
		logger.debug("Wrote false in putRecentFileName() to " + PREFS_START_ON_WELCOME_SCREEN + " preference");
	}

	public boolean startOnWelcomeScreen() {
		logger.debug("Got " + (getSessionCount() == 0) + " from " + PREFS_START_ON_WELCOME_SCREEN + " preference");
		return getPrefs().getBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
	}

    public void close() {
        if (!removeEditorPanel()) {
            return;
        }
        
        try {
            prefs.put("MainDividerLocaton", String.format("%d", wabitPane.getDividerLocation()));
            prefs.put("frameBounds", String.format("%d,%d,%d,%d", frame.getX(), frame.getY(),
                    frame.getWidth(), frame.getHeight()));
            prefs.flush();
        } catch (BackingStoreException ex) {
            logger.log(Level.WARN,"Failed to flush preferences", ex);
        }
        
        if (hasUnsavedChanges()) {
            int response = JOptionPane.showOptionDialog(frame,
                    "You have unsaved changes. Do you want to save?", "Unsaved Changes", //$NON-NLS-1$ //$NON-NLS-2$
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] {"Don't Save", "Cancel", "Save"}, "Save"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (response == 0) {
                //we are closing
            } else if (response == JOptionPane.CLOSED_OPTION || response == 1) {
                setEditorPanel();
                return;
            } else {
                boolean isClosing = true;
                for (WabitSession session : delegateContext.getSessions()) {
                    if (!SaveWorkspaceAction.save(WabitSwingSessionContextImpl.this)) {
                        isClosing = false;
                    }
                }
                if (!isClosing) return;
            }
        }

        for (WabitSession session : delegateContext.getSessions()) {
            session.close();
        }

        frame.dispose();

        getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, getSessionCount() == 0);
        logger.debug("Wrote " + (getSessionCount() == 0) + " in close() to " 
                + PREFS_START_ON_WELCOME_SCREEN + " preference");
        delegateContext.close();
        
    }
    
    /* docs inherited from interface */
    public void registerSwingWorker(SPSwingWorker worker) {
        activeWorkers.add(worker);
    }

    /* docs inherited from interface */
    public void removeSwingWorker(SPSwingWorker worker) {
        activeWorkers.remove(worker);
    }
    
    public JSpinner getRowLimitSpinner() {
        return rowLimitSpinner;
    }

    public int getRowLimit() {
        //XXX This limit should be saved in the delegate context.
        return (Integer) rowLimitSpinner.getValue();
    }
    
    /**
     * sets the StatusMessage
     */
    public void setStatusMessage (String msg) {
        statusLabel.setText(msg);   
    }
    
    private boolean hasUnsavedChanges() {
        // FIXME: this does not work obviously. Need to implement this with
        //a dirty/clean flag if we are still going to prompt to save on close
        //of the context.
        
        return true;
    }

    public DataSourceCollection<SPDataSource> getDataSources() {
        return delegateContext.getDataSources();
    }

    public List<WabitServerInfo> getEnterpriseServers(boolean includeDiscoveredServers) {
        return delegateContext.getEnterpriseServers(includeDiscoveredServers);
    }

    public void addServer(WabitServerInfo serverInfo) {
        delegateContext.addServer(serverInfo);
    }

    public void removeServer(WabitServerInfo si) {
        delegateContext.removeServer(si);
    }

    public JmDNS getJmDNS() {
        return delegateContext.getJmDNS();
    }

    public int getSessionCount() {
        return delegateContext.getSessionCount();
    }

    public boolean isMacOSX() {
        return delegateContext.isMacOSX();
    }

    public void registerChildSession(WabitSession child) {
        delegateContext.registerChildSession(child);
        treeTabbedPane.addTab(child.getWorkspace().getName(), ((WabitSwingSession) child).getTree());
        treeTabbedPane.setSelectedIndex(treeTabbedPane.getTabCount() - 1);
    }

    public Preferences getPrefs() {
        return delegateContext.getPrefs();
    }

    public JMenu createServerListMenu(Component dialogOwner, String name,
            ServerListMenuItemFactory itemFactory) {
        return new ServerListMenu(this, name, dialogOwner, itemFactory);
    }

	public String getName() {
		return delegateContext.getName();
	}

	public UserPrompter createDatabaseUserPrompter(String question,
			List<Class<? extends SPDataSource>> dsTypes,
			UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			DataSourceCollection<SPDataSource> dsCollection,
			String... buttonNames) {
		return upf.createDatabaseUserPrompter(question, dsTypes, optionType, defaultResponseType,
				defaultResponse, dsCollection, buttonNames);
	}

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File file) {
        File oldDirectory = currentFile;
        currentFile = file;       
        pcs.firePropertyChange("currentDirectory", oldDirectory, currentFile);
    }

    public WabitSwingSession getActiveSession() {
        return activeSession;
    }

    public void setActiveSession(WabitSwingSession activeSession) {
        WabitSwingSession oldSession = this.activeSession;
        this.activeSession = activeSession;
        pcs.firePropertyChange("activeSession", oldSession, activeSession);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public Connection borrowConnection(JDBCDataSource dataSource)
            throws SQLObjectException {
        return delegateContext.borrowConnection(dataSource);
    }

    public boolean isLoading() {
        return delegateContext.isLoading();
    }

    public void setLoading(boolean loading) {
        delegateContext.setLoading(loading);
    }

    public SQLDatabase getDatabase(JDBCDataSource ds) {
        return delegateContext.getDatabase(ds);
    }

    public OlapConnection createConnection(Olap4jDataSource dataSource)
            throws SQLException, ClassNotFoundException, NamingException {
        return delegateContext.createConnection(dataSource);
    }
 
    /**
     * Launches the Wabit application by loading the configuration and
     * displaying the GUI.
     * 
     * @throws Exception if startup fails
     */
    public static void main(final String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error("Unable to set native look and feel. Continuing with default.", e);
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    WabitSessionContextImpl coreContext = new WabitSessionContextImpl(false, true);
                    WabitSwingSessionContext context = new WabitSwingSessionContextImpl(coreContext, false);
                    
                	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wabit");
                	System.setProperty("apple.laf.useScreenMenuBar", "true");
                    final File importFile;
                    if (args.length > 0) {
                        importFile = new File(args[0]);
                    } else if (context.startOnWelcomeScreen()) {
                        importFile = null;
                    } else {
                        importFile = context.createRecentMenu().getMostRecentFile();
                    }
                    
                    
                    if (importFile != null) {
                    	OpenWorkspaceAction.loadFile(importFile, context);
                    }
                    context.setEditorPanel();
                } catch (Exception ex) {
                     ex.printStackTrace();
                    // We wish we had a parent component to direct the dialog but this is being invoked, so
                    // everything else blew up.
                    SPSUtils.showExceptionDialogNoReport("An unexpected error occured while launching Wabit",ex);
                }
            }
        });
        
    }
    
	public UserPrompter createUserPrompter(String question,
			UserPromptType responseType, UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			String... buttonNames) {
		return upf.createUserPrompter(question, responseType, optionType, defaultResponseType,
				defaultResponse, buttonNames);
	}

	public void addServerListListener(ServerListListener l) {
		delegateContext.addServerListListener(l);
	}

	public void removeServerListListener(ServerListListener l) {
		delegateContext.removeServerListListener(l);
	}

	
}
