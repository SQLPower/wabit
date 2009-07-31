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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.viewer.categoryexplorer.TreeModelAdapter;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.SwingUIUserPrompterFactory;
import ca.sqlpower.swingui.db.DatabaseConnectionManager;
import ca.sqlpower.swingui.db.DefaultDataSourceDialogFactory;
import ca.sqlpower.swingui.db.DefaultDataSourceTypeDialogFactory;
import ca.sqlpower.swingui.event.SessionLifecycleListener;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompterFactory;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.UserPrompterFactory.UserPromptType;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionImpl;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSession;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellEditor;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel;


/**
 * The Main Window for the Wabit Application.
 */
public class WabitSwingSessionImpl implements WabitSwingSession {
    
    private static final Icon DB_ICON = new ImageIcon(WabitSwingSessionImpl.class.getClassLoader().getResource("icons/dataSources-db.png"));
	
	private static Logger logger = Logger.getLogger(WabitSwingSessionImpl.class);
	
	private final WabitSwingSessionContext sessionContext;
	
	private final JTree workspaceTree;
	
	private final List<SessionLifecycleListener<WabitSession>> lifecycleListeners =
		new ArrayList<SessionLifecycleListener<WabitSession>>();

	/**
	 * This DB connection manager will allow editing the db connections in the
	 * pl.ini file. This DB connection manager can be used anywhere needed in 
	 * wabit. 
	 */
	private DatabaseConnectionManager dbConnectionManager;
	
	/**
	 * A {@link UserPrompterFactory} that will create a dialog for users to choose an existing
	 * DB or create a new one if they load a workspace with a DB not in their pl.ini.
	 */
	private UserPrompterFactory upfMissingLoadedDB;
	
	/**
	 * All of the session specific operations should be delegated to this session.
	 * This class is mainly used to tie Swing objects to a core session.
	 */
	private WabitSession delegateSession;
	
	/**
	 * The model behind the workspace tree on the left side of Wabit.
	 */
	private WorkspaceTreeModel workspaceTreeModel;
	
    /**
     * This listener is attached to the active session's workspace and will
     * update the current editor displayed when the object being edited changes
     * in the workspace.
     */
    private final PropertyChangeListener workspaceEditorModelListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (sessionContext.isLoading()) return;
            if (evt.getPropertyName().equals("editorPanelModel")) {
                sessionContext.setActiveSession(WabitSwingSessionImpl.this);
                if (!sessionContext.setEditorPanel()) {
                    getWorkspace().setEditorPanelModel((WabitObject) evt.getOldValue());
                    return;
                }
                if (evt.getNewValue() != null) {
                    final TreePath createTreePathForObject = 
                        getWorkspaceTreeModel().createTreePathForObject((WabitObject) evt.getNewValue());
                    logger.debug("Tree path being set to " + createTreePathForObject 
                            + " as editor panel being set to " + ((WabitObject) evt.getNewValue()).getName());
                    getTree().setSelectionPath(createTreePathForObject);
                }
            }
        }
    };

	/**
	 * Creates a new session 
	 * 
	 * @param context
	 */
	public WabitSwingSessionImpl(WabitSwingSessionContext context) {
	    delegateSession = new WabitSessionImpl(context);
		sessionContext = context;
		
		workspaceTreeModel = new WorkspaceTreeModel(delegateSession.getWorkspace());
		workspaceTree = new JTree(workspaceTreeModel);
		workspaceTree.setToggleClickCount(0);
		
        //Temporary upfMissingLoadedDB factory that is not parented in case there is no frame at current.
        //This should be replaced in the buildUI with a properly parented prompter factory.
        upfMissingLoadedDB = new SwingUIUserPrompterFactory(null);
		
        buildUIComponents();
        getWorkspace().addPropertyChangeListener(workspaceEditorModelListener);
	}
	
	public WabitSwingSessionImpl(WabitServerInfo serverInfo,
            WabitSwingSessionContext context) {
        this(context);
        delegateSession = new WabitServerSession(serverInfo, context);
    }

    /**
	 *  Builds the GUI pieces that belong to this session.
	 */
    public void buildUIComponents() {
		
		List<Class<? extends SPDataSource>> newDSTypes = new ArrayList<Class<? extends SPDataSource>>();
        newDSTypes.add(JDBCDataSource.class);
        newDSTypes.add(Olap4jDataSource.class);
        dbConnectionManager = new DatabaseConnectionManager(getDataSources(), 
				new DefaultDataSourceDialogFactory(), 
				new DefaultDataSourceTypeDialogFactory(getDataSources()),
				new ArrayList<Action>(), new ArrayList<JComponent>(), sessionContext.getFrame(), false, newDSTypes);
		dbConnectionManager.setDbIcon(DB_ICON);
		
		upfMissingLoadedDB = new SwingUIUserPrompterFactory(sessionContext.getFrame());
        
		final WorkspaceTreeCellRenderer renderer = new WorkspaceTreeCellRenderer();
		workspaceTree.setCellRenderer(renderer);
		workspaceTree.setCellEditor(new WorkspaceTreeCellEditor(workspaceTree, renderer));
		
		for (final QueryCache queryCache : getWorkspace().getQueries()) {
		    //Repaints the tree when the worker thread's timer fires. This will allow the tree node
		    //to paint a throbber badge on the query node.
		    queryCache.addTimerListener(new PropertyChangeListener() {
		        public void propertyChange(PropertyChangeEvent evt) {
		            renderer.updateTimer(queryCache, (Integer) evt.getNewValue());
		            workspaceTree.repaint(workspaceTree.getPathBounds(new TreePath(new WabitObject[]{getWorkspace(), queryCache})));
		        }
		    });

		    //This removes the timer from the query if the query has stopped running.
		    queryCache.addPropertyChangeListener(new PropertyChangeListener() {
		        public void propertyChange(PropertyChangeEvent evt) {
		            if (evt.getPropertyName().equals(QueryCache.RUNNING) && !((Boolean) evt.getNewValue())) {
		                renderer.removeTimer(queryCache);
		                workspaceTree.repaint(workspaceTree.getPathBounds(
		                        new TreePath(new WabitObject[]{delegateSession.getWorkspace(), queryCache})));
		            }
		        }
		    });
		}
		
		//XXX Pull this out into a final variable
		//This is a listener on each query cache that forwards timer events to
		//the renderer to update a throbber badge on the query's image when the
		//query is running.
		workspaceTree.getModel().addTreeModelListener(new TreeModelAdapter() {
			@Override
			public void treeNodesInserted(final TreeModelEvent e) {
				for (int i = 0; i < e.getChildren().length; i++) {
					if (e.getChildren()[i] instanceof QueryCache) {
						final QueryCache queryCache = (QueryCache) e.getChildren()[i];
						final TreePath treePath = e.getTreePath();
						queryCache.addTimerListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								renderer.updateTimer(queryCache, (Integer) evt.getNewValue());
								workspaceTree.repaint(workspaceTree.getPathBounds(
								        new TreePath(new WabitObject[]{getWorkspace(), queryCache})));
							}
						});
						queryCache.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("running") && !((Boolean) evt.getNewValue())) {
									renderer.removeTimer(queryCache);
									workspaceTree.repaint(workspaceTree.getPathBounds(
									        new TreePath(new WabitObject[]{getWorkspace(), queryCache})));
								}
							}
						});
					}
				}
			}
		});
		workspaceTree.addMouseListener(new WorkspaceTreeListener(this));
    	workspaceTree.setEditable(true);

    	//Sets the editor panel if it is not set when creating the UI 
    	//components. This may need to move elsewhere.
        if (getWorkspace().getEditorPanelModel() == null) {
        	getWorkspace().setEditorPanelModel(getWorkspace());
        }
    	
    }
    
    public DataSourceCollection<SPDataSource> getDataSources() {
        return delegateSession.getDataSources();
    }

    public JTree getTree() {
    	return workspaceTree;
    }

	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.add(l);
	}

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l) {
		lifecycleListeners.remove(l);
	}
	
	public boolean close() {
	    getWorkspace().removePropertyChangeListener(workspaceEditorModelListener);
	    return delegateSession.close();
	}

    public WabitWorkspace getWorkspace() {
        return delegateSession.getWorkspace();
    }
    
	public WabitSwingSessionContext getContext() {
		return sessionContext;
	}
	
    /**
     * Returns a {@link DatabaseConnectionManager} that allows modifying
     * the data source collection of the given session.
     */
    public DatabaseConnectionManager getDbConnectionManager() {
        return dbConnectionManager;
    }
	
	public UserPrompter createUserPrompter(String question, UserPromptType responseType, UserPromptOptions optionType, UserPromptResponse defaultResponseType, Object defaultResponse, String ...buttonNames) {
		return upfMissingLoadedDB.createUserPrompter(question, responseType, optionType, defaultResponseType, defaultResponse, buttonNames);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
        delegateSession.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
	    delegateSession.removePropertyChangeListener(l);
	}
	
    public WorkspaceTreeModel getWorkspaceTreeModel() {
        return workspaceTreeModel;
    }


	public UserPrompter createDatabaseUserPrompter(String question,
			List<Class<? extends SPDataSource>> dsTypes,
			UserPromptOptions optionType,
			UserPromptResponse defaultResponseType, Object defaultResponse,
			DataSourceCollection<SPDataSource> dsCollection,
			String... buttonNames) {
		return upfMissingLoadedDB.createDatabaseUserPrompter(question, dsTypes, optionType, defaultResponseType,
				defaultResponse, dsCollection, buttonNames);
	}
}
