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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.jmdns.JmDNS;
import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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
import ca.sqlpower.swingui.Search;
import ca.sqlpower.swingui.SearchTextField;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.swingui.action.ForumAction;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.ServerListListener;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.dao.OpenWorkspaceXMLDAO;
import ca.sqlpower.wabit.dao.WorkspaceXMLDAO;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.swingui.action.AboutAction;
import ca.sqlpower.wabit.swingui.action.CloseWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.HelpAction;
import ca.sqlpower.wabit.swingui.action.ImportWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.NewImageAction;
import ca.sqlpower.wabit.swingui.action.NewLayoutAction;
import ca.sqlpower.wabit.swingui.action.NewOLAPQueryAction;
import ca.sqlpower.wabit.swingui.action.NewQueryAction;
import ca.sqlpower.wabit.swingui.action.NewServerWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.NewWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.OpenWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveServerWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveWorkspaceAction;
import ca.sqlpower.wabit.swingui.action.SaveWorkspaceAsAction;
import ca.sqlpower.wabit.swingui.olap.OlapQueryPanel;
import ca.sqlpower.wabit.swingui.report.ReportLayoutPanel;
import ca.sqlpower.wabit.swingui.tree.SmartLeftTreeTransferable;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the swing version of the WabitSessionContext. Swing specific operations for
 * the context will be done in this implementation 
 */
public class WabitSwingSessionContextImpl implements WabitSwingSessionContext {

    private static final String SEARCH_TAB = "Select Search Tab";

	private static final Logger logger = Logger.getLogger(WabitSwingSessionContextImpl.class);
    
    public static final String EXAMPLE_WORKSPACE_URL = "/ca/sqlpower/wabit/example_workspace.wabit";
    
    /**
     * The regex to split all of the workspaces on when they are saved to prefs
     */
	private static final String WORKSPACE_PREFS_REGEX = ";";
    
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
    
    public static final Icon OPEN_WABIT_ICON = new ImageIcon(
            WabitSwingSessionContextImpl.class.getClassLoader().getResource("icons/workspace-16.png"));
    
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
     * This is a simple {@link SwingWorkerRegistry} implementation for the
     * context to track workers involved with loading files. It would be useful
     * if the {@link OpenWorkspaceXMLDAO} created the session(s) to load into
     * before creating the thread to do the loading. Then the loading thread
     * could register with the session and we could remove this implementation.
     */
    private static class LoadingSwingWorkerRegistry implements SwingWorkerRegistry {

        private final List<SPSwingWorker> activeWorkers = new ArrayList<SPSwingWorker>();
        
        public void registerSwingWorker(SPSwingWorker worker) {
            activeWorkers.add(worker);
        }

        public void removeSwingWorker(SPSwingWorker worker) {
            activeWorkers.remove(worker);
        }
        
        public void close() {
            for (SPSwingWorker worker : activeWorkers) {
                worker.kill();
            }
        }
        
    }
    
    /**
     * This is the {@link SwingWorkerRegistry} responsible for tracking
     * the threads used in loading sessions that don't have a session yet.
     */
    private final LoadingSwingWorkerRegistry loadingRegistry = new LoadingSwingWorkerRegistry();
    
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
     * This is a preference that stores the absolute file location of each file
     * that should be started when Wabit starts up. Each file name is separated
     * by the {@link #WORKSPACE_PREFS_REGEX}. To get the files that need to be
     * opened when Wabit starts get the string stored in prefs with this key and
     * split the string by the regex. Each file stored in the prefs should be
     * loaded and if an exception occurs stop loading. If no files are listed
     * the welcome screen should appear.
     */
    private static final String PREFS_OPEN_WORKSPACES = "OPEN_WORKSPACES";
    
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
    private final JTabbedPane treeTabbedPane;
    
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

    public static final Icon NEW_ICON = new ImageIcon(
            WabitSwingSessionContextImpl.class.getClassLoader().getResource("icons/32x32/new.png"));
    
	/**
	 * Creates a popup menu with all the possible 'New <insert Wabit object
	 * here>' options
	 */
    private final Action newAction = new AbstractAction("New", NEW_ICON) {
    	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() instanceof JButton) {
    			JButton source = (JButton) e.getSource();
    			JPopupMenu popupMenu = new JPopupMenu();
    			popupMenu.add(new NewWorkspaceAction(WabitSwingSessionContextImpl.this));
    			WabitSwingSession activeSession = getActiveSwingSession();
    			if (activeSession != null) {
    				popupMenu.add(new NewQueryAction(activeSession));
    				popupMenu.add(new NewOLAPQueryAction(activeSession));
    				popupMenu.add(new NewImageAction(activeSession));
    				popupMenu.add(new NewLayoutAction(activeSession));
    			}
    			popupMenu.show(source, 0, source.getHeight());
    		}
    	}
    };
    
    public static final Icon OPEN_ICON = new ImageIcon(
    	WabitSwingSessionContextImpl.class.getClassLoader().getResource("icons/32x32/open.png"));

    /**
     * An action that creates a popup with an open and import option
     */
    private final Action openAction = new AbstractAction("Open", OPEN_ICON) {
    	public void actionPerformed(ActionEvent e) {
    		if (e.getSource() instanceof JButton) {
    			JButton source = (JButton) e.getSource();
    			JPopupMenu popupMenu = new JPopupMenu();
    			popupMenu.add(new OpenWorkspaceAction(WabitSwingSessionContextImpl.this));
    			WabitSwingSession activeSession = getActiveSwingSession();
    			if (activeSession != null) {
    				popupMenu.add(new ImportWorkspaceAction(WabitSwingSessionContextImpl.this));
    			}
    			popupMenu.show(source, 0, source.getHeight());
    		}
    	}
    };
    
    public static final Icon SAVE_ICON = new ImageIcon(
        	WabitSwingSessionContextImpl.class.getClassLoader().getResource("icons/32x32/save.png"));

    /**
     * An action that saves the current active workspace.
     */
    private final Action saveAction = new AbstractAction("Save", SAVE_ICON) {
    	public void actionPerformed(ActionEvent e) {
			SaveWorkspaceAction.saveAllSessions(WabitSwingSessionContextImpl.this);
    	}
    };
    
    /**
     * This is the model of the search JTree, it is just a default tree model
     * that we add {@link DefaultMutableTreeNode}s to.
     */
    private DefaultTreeModel searchTreeModel;
    
    /**
     * This is the root of our searchTree, it is never visible and it is just a
     * {@link DefaultMutableTreeNode} with null in it.
     */
    private DefaultMutableTreeNode searchTreeRoot;
    
    /**
     * This is the text area that a user types into to search.
     */
    private final SearchTextField searchTextArea = new SearchTextField(new SearchWabitTree(), 0);
    
    /**
     * This is the tree that displays a user's search results.
     */
    private final JTree searchTree = new JTree();
    
    /**
     * This is the cell renderer in the search tree, it basically just gets
     * the object out of the {@link DefaultMutableTreeNode} class that is in
     * the tree and passes that object (which will be something that the
     * {@link WorkspaceTreeCellRenderer} knows how to deal with) to the
     * {@link WorkspaceTreeCellRenderer} so the search tree looks exactly 
     * like the Workspace tree. 
     */
    private class SearchTreeCellRenderer extends WorkspaceTreeCellRenderer {
    	@Override
    	public Component getTreeCellRendererComponent(JTree tree, Object value,
    			boolean sel, boolean expanded, boolean leaf, int row,
    			boolean hasFocus) {
    		Object objectToRender = ((DefaultMutableTreeNode) value).getUserObject();
    		if (objectToRender == null) {
    			//this means its the root node, it doesn't matter if we pass the cell
    			//renderer something that it can't really render because the root node
    			//is hidden
    			objectToRender = value;
    		}
			return super.getTreeCellRendererComponent(tree, objectToRender, sel, expanded, leaf,
    				row, hasFocus);
    	}
    }
    
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
        treeTabbedPane = new JTabbedPane();
        treeTabbedPane.setDropTarget(new DropTarget(treeTabbedPane, treeTabDropTargetListener));

        Action selectSearchTabAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent arg0) {
        		// Choose the search tab. This, of course, assumes that the
				// Search Tab is always at index 0.
        		treeTabbedPane.setSelectedIndex(0);
        		// This seems to improve the chances of requestFocusInWindow succeeding...
        		SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						searchTextArea.getTextField().requestFocusInWindow();
					}
        		});
        	}
        };
        InputMap inputMap = treeTabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), SEARCH_TAB);
        treeTabbedPane.getActionMap().put(SEARCH_TAB, selectSearchTabAction);
        
        searchTreeRoot = new DefaultMutableTreeNode();
        searchTreeModel = new DefaultTreeModel(searchTreeRoot);
		searchTree.setModel(searchTreeModel);
		searchTree.setCellRenderer(new SearchTreeCellRenderer());
		searchTree.setRootVisible(false);
		searchTree.setShowsRootHandles(true);
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(searchTextArea.getPanel(), BorderLayout.NORTH);
		searchPanel.add(new JScrollPane(searchTree), BorderLayout.CENTER);
		treeTabbedPane.addTab("Search", searchPanel);
		TreeSelectionListener searchTreeSelectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath pathToSelection = e.getNewLeadSelectionPath();
				if (pathToSelection == null) return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathToSelection.getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof WabitObject) {
					WabitObject wo = (WabitObject) userObject;
					while (wo.getParent() != null) {
						wo = wo.getParent();
					}
					WabitWorkspace workspace = (WabitWorkspace) wo; 
					setActiveSession(workspace.getSession());
					JTree tree = ((WabitSwingSessionImpl) workspace.getSession()).getTree();
					TreePath path = ((WorkspaceTreeModel) tree.getModel()).createTreePathForObject((WabitObject) userObject);
					tree.expandPath(path);
					tree.setSelectionPath(path);
					workspace.setEditorPanelModel((WabitObject) userObject);
					setEditorPanel();
				}
			}
		};
		searchTree.addTreeSelectionListener(searchTreeSelectionListener);
		
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
	
	/**
	 * This is the class that actually does the search on the search tree.
	 * It implements 'Search' which is something used in the library to have
	 * a generic search text box that looks nice and has regular expressions. 
	 */
	private class SearchWabitTree implements Search {
		public void doSearch(Pattern p, boolean matchExactly) {
			String searchString = searchTextArea.getText().trim();
			
			//clear the tree and start over
			searchTreeRoot = new DefaultMutableTreeNode();
			searchTreeModel = new DefaultTreeModel(searchTreeRoot);
			searchTree.setModel(searchTreeModel);
			
			//this just makes sure that we don't see an all workspaces tree when there is
			//no text showing we are not sure if we actually want this
			if (searchString.equals("")) return;

			//get all the tree models we can search
			List<TreeModel> searchableModels = new ArrayList<TreeModel>();
			for (WabitSession session : getSessions()) {
				if (!(session instanceof WabitSwingSession)) {
					throw new IllegalStateException("Found non swing session in swing session context!");
				}
				JTree tree = ((WabitSwingSession) session).getTree();
				searchableModels.add(tree.getModel());
			}
			
			for (TreeModel originalModel : searchableModels) {
				WorkspaceTreeModel model = (WorkspaceTreeModel) originalModel;
				
				//search the tree
				ArrayList<Object> rootTreePath = new ArrayList<Object>();
				rootTreePath.add(model.getRoot());
				List<List<Object>> matchedTreePaths = searchTree(rootTreePath, model, p, matchExactly);
				
				//add everything into the tree if it's not already there
				for (List<Object> treePath : matchedTreePaths) {
					DefaultMutableTreeNode lastObject = (DefaultMutableTreeNode) searchTreeModel.getRoot();
					for (Object object : treePath) {
						int indexOfChild = -1;
						for (int i = 0; i < lastObject.getChildCount(); i++) {
							DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) lastObject.getChildAt(i);
							if (childNode.getUserObject().equals(object)) {
								indexOfChild = i;
								break;
							}
						}
						if (indexOfChild == -1) {
							DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(object);
							searchTreeModel.insertNodeInto(currentNode, lastObject, lastObject.getChildCount());
							lastObject = currentNode;
						} else {
							if (treePath.indexOf(object) != (treePath.size() - 1)) {
								lastObject = (DefaultMutableTreeNode) searchTreeModel.getChild(lastObject, indexOfChild);
							}
						}
					}
				}

				//Show everything in the tree, we would do this as we go but it doesn't wan't to work.
				searchTree.expandPath(new TreePath(searchTreeModel.getRoot()));
				for (int i = 0; i < searchTree.getRowCount(); i++) {
					searchTree.expandRow(i); 
				}
				
			}
			
		}
		
	}
	
	/**
	 * Recursive function which searches all of the objects' names in a tree
	 * model for a given string.
	 * 
	 * @param searchString
	 * 		The string to search all the names of objects in the treemodel for
	 * @param currentTreePath
	 * 		The current path in the tree (since the function is recursive)
	 * @param model
	 * 		The tree model being searched
	 * @return
	 * 		Returns a list of paths to all the tree objects in a model which have
	 * 		a name which contains the searchString
	 */
	private List<List<Object>> searchTree(List<Object> currentTreePath, 
			WorkspaceTreeModel model, Pattern p, boolean matchExactly) {
		
		Object currentObject = currentTreePath.get(currentTreePath.size() - 1);

		ArrayList<List<Object>> returnList = new ArrayList<List<Object>>();

		for (int i = 0; i < model.getChildCount(currentObject); i++) {
			List<Object> childPath = new ArrayList<Object>(currentTreePath);
			childPath.add(model.getChild(currentObject, i));
			returnList.addAll(searchTree(childPath, model, p, matchExactly));
		}
		if (currentObject instanceof WabitObject) { //It could be a FolderNode...
			WabitObject currentWO = (WabitObject) currentObject;
			String name = currentWO.getName();
			if (currentWO instanceof ContentBox) {
				ReportContentRenderer content = ((ContentBox) currentWO).getContentRenderer();
				if (content instanceof CellSetRenderer) {
					name = ((CellSetRenderer) content).getOlapQuery().getName();
				} else if (content instanceof ResultSetRenderer) {
					name = ((ResultSetRenderer) content).getQuery().getName();
				} else if (content instanceof Label) {
					name = ((Label) content).getText();
				} else if (content instanceof ChartRenderer) {
					name = ((ChartRenderer) content).getQuery().getName();
				} else if (content instanceof ImageRenderer) {
					name = ((ImageRenderer) content).getImage().getName();
				}
			}
            if (matchExactly && p.matcher(name).matches()) {
				returnList.add(currentTreePath);
            } else if (!matchExactly && p.matcher(name).find()) {
				returnList.add(currentTreePath);
            }
		}
		return returnList;
	}

	public WabitSwingSession createSession() {
	    final WabitSwingSessionImpl session = new WabitSwingSessionImpl(this, delegateContext.createSession());
        return session;
    }

	private TreeTabDropTargetListener treeTabDropTargetListener = new TreeTabDropTargetListener();
	
	/**
	 * This is the droplistener on the tabbed pane which controls importing
	 * and exporting between workspaces 
	 */
	public class TreeTabDropTargetListener implements DropTargetListener {

		public void dragEnter(DropTargetDragEvent dtde) {
			//don't care
		}

		public void dragExit(DropTargetEvent dte) {
			//don't care
		}

		public void dragOver(DropTargetDragEvent dtde) {
			if (canImport(dtde.getCurrentDataFlavors())) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}
		
        public boolean canImport(DataFlavor[] transferFlavors) {
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == SmartLeftTreeTransferable.WABIT_OBJECT_FLAVOUR_TO_EXPORT) {
                    return true;
                }
            }
            return false;
        }

		public void drop(DropTargetDropEvent dtde) {
			Point mouseLocation = dtde.getLocation();
			int tabIndex = treeTabbedPane.indexAtLocation(mouseLocation.x, mouseLocation.y);
			if (tabIndex == -1 || tabIndex == 0) return; //The search tab should always have an index of 0
			treeTabbedPane.setSelectedIndex(tabIndex);
			
			ByteArrayOutputStream byteOut;
			Transferable transferable = dtde.getTransferable();
			DataFlavor dataFlavor = SmartLeftTreeTransferable.WABIT_OBJECT_FLAVOUR_TO_EXPORT;
			Object[] transferData;
			try {
				transferData = (Object[]) transferable.getTransferData(dataFlavor);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
			List<WabitObject> wabitObjectsToExport = new ArrayList<WabitObject>();
			String wabitDataSourcesBeingExported = "";
			for (int i = 0; i < transferData.length; i++) {
				if (transferData[i] instanceof WabitDataSource) {
					wabitDataSourcesBeingExported += (((WabitDataSource) transferData[i]).getName() + "\n");
				} else {
					WabitObject wo = (WabitObject) transferData[i];
					wabitObjectsToExport.add(wo);
					wabitDataSourcesBeingExported += getDatasourceDependencies(wo);
				}
			}
			boolean shouldContinue = false;
			if (wabitDataSourcesBeingExported != "") {
				wabitDataSourcesBeingExported = wabitDataSourcesBeingExported.substring(0, wabitDataSourcesBeingExported.lastIndexOf("\n"));
				UserPrompter up = upf.createUserPrompter("WARNING: By performing the following export you are exposing your database\n" +
						" credentials to all users who have access to the workspace being dragged into. This \n" +
						"is safe if you are meerly transferring the data to another local workspace but please use\n" +
						" caution before transferring these over to a server. The following connections are being transferred: \n" +
						wabitDataSourcesBeingExported + ".",
						UserPromptType.BOOLEAN, UserPromptOptions.OK_CANCEL, UserPromptResponse.OK, true,
						"Continue", "Cancel");
				UserPromptResponse response = up.promptUser();
				shouldContinue = response.equals(UserPromptResponse.OK);
			} else {
				shouldContinue = true;
			}
			if (!shouldContinue) return;
			byteOut = new ByteArrayOutputStream();
			WorkspaceXMLDAO dao = new WorkspaceXMLDAO(byteOut, delegateContext);
			dao.save(wabitObjectsToExport);
			try {
				byteOut.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			byte[] outByteArray = byteOut.toByteArray();
			ByteArrayInputStream input = new ByteArrayInputStream(outByteArray);

			OpenWorkspaceXMLDAO open = new OpenWorkspaceXMLDAO(delegateContext, input, outByteArray.length);
			open.importWorkspaces(getActiveSession());
		}
		
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//don't care
		}
	}
	
	/**
	 * This is a recursive function which returns a \n delimited string of all of the
	 * {@link WabitDataSource}s that the given {@link WabitObject} is dependant on. 
	 */
	private String getDatasourceDependencies(WabitObject wo) {
		String wabitDatasources = "";
		for (WabitObject dependency : wo.getDependencies()) {
			if (dependency instanceof WabitDataSource) {
				wabitDatasources += (((WabitDataSource) dependency).getName() + "\n");
			} else {
				wabitDatasources += getDatasourceDependencies(dependency);
			}
		}
		return wabitDatasources;
	}
	
	public WabitSwingSession createServerSession(WabitServerInfo serverInfo) {
        final WabitSwingSessionImpl session = new WabitSwingSessionImpl(this, delegateContext.createServerSession(serverInfo));
        return session;
    }
	
	public RecentMenu createRecentMenu() {
		RecentMenu menu = new RecentMenu(this.getClass()) {
			
			@Override
			public void loadFile(String fileName) throws IOException {
				File file = new File(fileName);
				OpenWorkspaceAction.loadFiles(WabitSwingSessionContextImpl.this, file.toURI());
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
	    treeTabbedPane.removeTabAt(getSessions().indexOf(child) + 1);
	    delegateContext.deregisterChildSession(child);
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
        
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        
        JButton newButton = new JButton(newAction);
        newButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        newButton.setHorizontalTextPosition(SwingConstants.CENTER);
        
        JButton openButton = new JButton(openAction);
        openButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        openButton.setHorizontalTextPosition(SwingConstants.CENTER);
        
        JButton saveButton = new JButton(saveAction);
        saveButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        saveButton.setHorizontalTextPosition(SwingConstants.CENTER);
        
        // OS X specific client properties to modify the button appearance.
        // This only seems to affect OS X 10.5 Leopard's buttons.
        newButton.putClientProperty("JButton.buttonType", "toolbar");
        openButton.putClientProperty("JButton.buttonType", "toolbar");
        saveButton.putClientProperty("JButton.buttonType", "toolbar");

        toolBar.add(newButton);
        toolBar.add(openButton);
		toolBar.add(saveButton);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        for (WabitSession session : getSessions()) {
            JPanel brandedTree = SPSUtils.getBrandedTreePanel(((WabitSwingSession) session).getTree());
			treeTabbedPane.addTab(session.getWorkspace().getName(), new JScrollPane(brandedTree));
        }
        final ChangeListener tabChangeListener = new ChangeListener() {
        
            public void stateChanged(ChangeEvent e) {
                final int selectedIndex = treeTabbedPane.getSelectedIndex();
                if (treeTabbedPane.indexOfTab("Search") == selectedIndex) return;
                if (selectedIndex >= 0) {
                    setActiveSession((WabitSwingSession) getSessions().get(selectedIndex - 1));
                    setEditorPanel();
                }
            }
        };
        treeTabbedPane.addChangeListener(tabChangeListener);
        
        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(treeTabbedPane, BorderLayout.CENTER);
        
        wabitPane.add(leftPanel, JSplitPane.LEFT);
        
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
                try {
                    final URI resource = WabitWelcomeScreen.class.getResource(
                            EXAMPLE_WORKSPACE_URL).toURI();
                    OpenWorkspaceAction.loadFiles(WabitSwingSessionContextImpl.this, resource);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
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
        fileMenu.add(new AbstractAction("Save All") {
            public void actionPerformed(ActionEvent e) {
                SaveWorkspaceAction.saveAllSessions(WabitSwingSessionContextImpl.this);
            }
        });
        fileMenu.add(createServerListMenu(frame, "Save Workspace on Server", new ServerListMenuItemFactory() {
            public JMenuItem createMenuEntry(WabitServerInfo serviceInfo, Component dialogOwner) {
                try {
                    return new JMenuItem(new SaveServerWorkspaceAction(serviceInfo, dialogOwner, getActiveSession().getWorkspace(), WabitSwingSessionContextImpl.this));
                } catch (Exception e) {
                    JMenuItem menuItem = new JMenuItem(e.toString());
                    menuItem.setEnabled(false);
                    // TODO it would be nice to have ASUtils.createExceptionMenuItem(Throwable)
                    return menuItem;
                }
            }
        }));
        fileMenu.addSeparator();
        
        JMenuItem closeMenuItem = new JMenuItem(new CloseWorkspaceAction(this));
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK));
        fileMenu.add(closeMenuItem);
        fileMenu.addSeparator();
        
        JMenuItem databaseConnectionManager = new JMenuItem(new AbstractAction("Database Connection Manager...") {
            public void actionPerformed(ActionEvent e) {
                getActiveSwingSession().getDbConnectionManager().showDialog(getFrame());
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
        if (getActiveSession() != null) {
            entryPanelModel = getActiveSession().getWorkspace().getEditorPanelModel();
        }
        
        currentEditorPanel = createEditorPanel(entryPanelModel);
        
        wabitPane.add(currentEditorPanel.getPanel(), JSplitPane.RIGHT);
        wabitPane.setDividerLocation(dividerLoc);
        frame.setTitle(currentEditorPanel.getTitle());
        
        // The execute query currently needs to be done after the panel is added
        // to the split pane, because it requires a Graphics2D object to get a
        // FontMetrics to use to calculate optimal column widths in the
        // CellSetViewer. If done before, the Graphics2D object is null.
        if (currentEditorPanel instanceof OlapQueryPanel) {
            try {
                ((OlapQuery) entryPanelModel).execute();
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
        if (getActiveSession() == null) {
            currentEditorPanel = welcomeScreen.getPanel();
        } else if (entryPanelModel instanceof QueryCache) {
            QueryPanel queryPanel = new QueryPanel(getActiveSwingSession(), (QueryCache) entryPanelModel);
            if (prefs.get(QUERY_DIVIDER_LOCATON, null) != null) {
                String[] dividerLocations = prefs.get(QUERY_DIVIDER_LOCATON, null).split(",");
                queryPanel.getTopRightSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[0]));
                queryPanel.getFullSplitPane().setDividerLocation(Integer.parseInt(dividerLocations[1]));
            } else {
                //Setting the lower half of the split initially to 1/4 of the screen
                //height or else the results won't be visible and the user won't see
                //them update
                queryPanel.getFullSplitPane().setDividerLocation(
                        (int) (wabitPane.getHeight() * 3 / 4));
            }
            currentEditorPanel = queryPanel;
        } else if (entryPanelModel instanceof OlapQuery) {
            OlapQueryPanel panel = new OlapQueryPanel(getActiveSwingSession(), wabitPane, (OlapQuery) entryPanelModel);
            currentEditorPanel = panel;
        } else if (entryPanelModel instanceof WabitImage) {
            WabitImagePanel panel = new WabitImagePanel((WabitImage) entryPanelModel, this);
            currentEditorPanel = panel;
        } else if (entryPanelModel instanceof Layout) {
            ReportLayoutPanel rlPanel = new ReportLayoutPanel(getActiveSwingSession(), (Layout) entryPanelModel);
            if (prefs.get(LAYOUT_DIVIDER_LOCATION, null) != null) {
                rlPanel.getSplitPane().setDividerLocation(Integer.parseInt(prefs.get(LAYOUT_DIVIDER_LOCATION, null)));
            }
            currentEditorPanel = rlPanel;
        } else if (entryPanelModel instanceof WabitWorkspace) {
            currentEditorPanel = new WorkspacePanel(getActiveSwingSession());
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
                if (!SaveWorkspaceAction.saveAllSessions(WabitSwingSessionContextImpl.this)) {
                	isClosing = false;
                }
                if (!isClosing) return;
            }
        }

        for (WabitSession session : delegateContext.getSessions()) {
            session.close();
        }

        frame.dispose();

        getPrefs().remove(PREFS_OPEN_WORKSPACES);
        for (int i = 0; i < getSessionCount(); i++) {
            File currentFile = ((WabitSwingSession) getSessions().get(i)).getCurrentURIAsFile();
            if (currentFile == null) continue;
            String currentWorkspaces;
            String workspaces = getPrefs().get(PREFS_OPEN_WORKSPACES, null);
			if (workspaces == null) {
            	currentWorkspaces = "";
            } else {
            	currentWorkspaces = workspaces + WORKSPACE_PREFS_REGEX;
            }
			String saveOutString = currentWorkspaces + currentFile.getAbsolutePath();
			getPrefs().put(PREFS_OPEN_WORKSPACES, saveOutString);
        }
        
        loadingRegistry.close();
        
        delegateContext.close();
        
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
    
    /**
     * Returns true if any of this context's sessions have unsaved changes.
     */
    private boolean hasUnsavedChanges() {
        for (WabitSession session : getSessions()) {
            if (session instanceof WabitSwingSession) {
                WabitSwingSession swingSession = (WabitSwingSession) session;
                if (swingSession.hasUnsavedChanges()) {
                    return true;
                }
            }
        }
        return false;
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
        JPanel brandedTree = SPSUtils.getBrandedTreePanel(((WabitSwingSession) child).getTree());
		treeTabbedPane.addTab(child.getWorkspace().getName(), new JScrollPane(brandedTree));
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

    public WabitSession getActiveSession() {
        return delegateContext.getActiveSession();
    }
    
    public WabitSwingSession getActiveSwingSession() {
        return (WabitSwingSession) delegateContext.getActiveSession();
    }

    public void setActiveSession(WabitSession activeSession) {
        WabitSession oldSession = delegateContext.getActiveSession();
        delegateContext.setActiveSession(activeSession);
        treeTabbedPane.setSelectedIndex(getSessions().indexOf(activeSession) + 1);
        if (oldSession != activeSession) {
            setEditorPanel();
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        delegateContext.addPropertyChangeListener(l);
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        delegateContext.removePropertyChangeListener(l);
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
                	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Wabit");
                	System.setProperty("apple.laf.useScreenMenuBar", "true");

                	WabitSessionContextImpl coreContext = new WabitSessionContextImpl(false, true);
                    WabitSwingSessionContext context = new WabitSwingSessionContextImpl(coreContext, false);
                    context.setEditorPanel();
                    
                    final List<File> importFile = new ArrayList<File>();
                    if (args.length > 0) {
                        importFile.add(new File(args[0]));
                    } else {
                        String workspacesToLoad = context.getPrefs().get(PREFS_OPEN_WORKSPACES, null);
                        if (workspacesToLoad != null) {
							for (String workspaceLocation: workspacesToLoad.split(WORKSPACE_PREFS_REGEX)) {
                        		File newFile = new File(workspaceLocation);
                        		importFile.add(newFile);
                        	}
                        }
                    }
                    
                    List<URI> startupURIs = new ArrayList<URI>();
                    for (File file : importFile) {
                        if (file != null) {
                            startupURIs.add(file.toURI());
                        }
                    }
                    
                    OpenWorkspaceAction.loadFiles(context, startupURIs.toArray(new URI[startupURIs.size()]));
                    
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

	public SwingWorkerRegistry getLoadingRegistry() {
        return loadingRegistry;
    }
}
