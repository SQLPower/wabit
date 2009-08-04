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

package ca.sqlpower.wabit.swingui.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitChildListener;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Guide.Axis;

/**
 * Provides a tree with the workspace at the root. The workspace contains data
 * sources, queries, and layouts in that order.
 */
public class WorkspaceTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(WorkspaceTreeModel.class);
    
    private static enum FolderType {
    	CONNECTIONS,
    	QUERIES,
    	REPORTS
    	//TODO implement images and charts folders.... maybe olap cubes
    }
    
    public static FolderType getProperFolderParent(WabitObject object) {
    	if (object instanceof WabitDataSource) {
    		return FolderType.CONNECTIONS;
    	} else if (object instanceof QueryCache || object instanceof OlapQuery) {
    		return FolderType.QUERIES; 
    	} else if (object instanceof Layout) {
    		return FolderType.REPORTS;
    	}
    	throw new UnsupportedOperationException("Trying to find the parent folder of object of type: " + object.getChildren().toString());
    }
    
    public class FolderNode {
    	private WabitWorkspace parent;
    	private FolderType folderType;
    	
    	
    	public FolderNode(WabitWorkspace parent, FolderType folderType) {
    		this.parent = parent;
    		this.folderType = folderType;
		}
    	
    	public WabitWorkspace getParent() {
			return parent;
		}
    	
		public FolderType getFolderType() {
			return folderType;
		}

		public boolean allowsChildren() {
			return true;
		}


		public List<? extends WabitObject> getChildren() {
			List<WabitObject> childList = new ArrayList<WabitObject>();
			switch (folderType) {
			case CONNECTIONS:
				childList.addAll(workspace.getDataSources());
				break;
			case QUERIES:
				childList.addAll(workspace.getQueries());
				childList.addAll(workspace.getOlapQueries());
				break;
			case REPORTS:
				childList.addAll(workspace.getLayouts());
				break;
			}
			return childList;
		}
		
		@Override
		public String toString() {
			String name = null;
			switch (folderType) {
			case CONNECTIONS:
				name = "Connections";
				break;
			case QUERIES:
				name = "Queries";
				break;
			case REPORTS:
				name = "Reports";
				break;
			}
			return name;
		}

		public int childPositionOffset(Class<? extends WabitObject> childType) {
			return 0;
		}
    }
    
    private final WabitWorkspace workspace;
    
    private final List<FolderNode> folderList;

    private WabitTreeModelEventAdapter listener;
    
    public WorkspaceTreeModel(WabitWorkspace workspace) {
        this.workspace = workspace;
        this.folderList = new ArrayList<FolderNode>();
        folderList.add(new FolderNode(workspace, FolderType.CONNECTIONS));
        folderList.add(new FolderNode(workspace, FolderType.QUERIES));
        folderList.add(new FolderNode(workspace, FolderType.REPORTS));
        listener = new WabitTreeModelEventAdapter();
        WabitUtils.listenToHierarchy(workspace, listener, listener);
    }
    
    public Object getRoot() {
        return workspace;
    }
    
    public Object getChild(Object parentObject, int index) {
		if (parentObject instanceof WabitWorkspace) {
			return folderList.get(index);
    	}  else if (parentObject instanceof FolderNode) {
    		return ((FolderNode) parentObject).getChildren().get(index);
    	} else {
			WabitObject wabitObject = (WabitObject) parentObject;
			return wabitObject.getChildren().get(index);
    	}
    }
    
    public int getChildCount(Object parent) {
    	if (parent instanceof WabitWorkspace) {
    		return folderList.size();
    	} else if (parent instanceof FolderNode) {
    		return ((FolderNode) parent).getChildren().size();
    	} else {
    		return ((WabitObject) parent).getChildren().size(); // XXX would be more efficient if we could ask for a child count
    	}
    }
    
    public int getIndexOfChild(Object parent, Object child) {
    	if (parent instanceof WabitWorkspace) {
    		return folderList.indexOf(child);
    	} else if (parent instanceof FolderNode) {
    		return ((FolderNode) parent).getChildren().indexOf(child);
    	} else {
	        WabitObject wo = (WabitObject) parent;
	        List<? extends WabitObject> children = wo.getChildren();
	        return children.indexOf(child);
    	}
    }

    public boolean isLeaf(Object node) {
    	boolean retval;
    	if (node instanceof FolderNode) {
    		retval = !(((FolderNode) node).allowsChildren());
    	} else {
    		retval = !((WabitObject) node).allowsChildren();
    	}
		return retval;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    	fireTreeStructureChanged(new TreeModelEvent(newValue, path));
    }

    // -------------- treeModel event source support -----------------
    private final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    protected void fireTreeNodesInserted(TreeModelEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("Firing treeNodesInserted event: " + e);
        }
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesInserted(e);
        }
    }

    protected void fireTreeNodesRemoved(TreeModelEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("Firing treeNodesRemoved event " + e);
        }
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesRemoved(e);
        }
    }

    protected void fireTreeNodesChanged(TreeModelEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("firing TreeNodesChanged. source=" + e.getSource());
        }
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesChanged(e);
        }
    }

    protected void fireTreeStructureChanged(TreeModelEvent e) {
        if (logger.isDebugEnabled()) {
            logger.debug("firing TreeStructuredChanged. source=" + e.getSource());
        }
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeStructureChanged(e);
        }
    }
    
    /**
	 * A private event handler that listens for {@link PropertyChangeEvent} and
	 * {@link WabitChildEvent} from the business model and 'translates' them
	 * into {@link TreeModelEvent} for the WorkspaceTreeModel.
	 */
    private class WabitTreeModelEventAdapter implements PropertyChangeListener, WabitChildListener {

		public void propertyChange(PropertyChangeEvent evt) {
			WabitObject node = (WabitObject) evt.getSource();

			TreeModelEvent e;
			if (node == getRoot()) {
				// special case for root node
				e = new TreeModelEvent(this, new Object[] { getRoot() }, null,
						null);
			} else {
				WabitObject parent = node.getParent();
				if (parent == null) {
				    throw new NullPointerException("Parent of non-root node " + node + " was null!");
				}
				int indexOfChild = getIndexOfChild(parent, node);
				e = new TreeModelEvent(this, createTreePathForObject(node),
						new int[] { indexOfChild }, new Object[] { node });
			}
			fireTreeNodesChanged(e);
		}

		public void wabitChildAdded(WabitChildEvent e) {
		    WabitUtils.listenToHierarchy(e.getChild(), this, this);
		    TreePath treePath = createTreePathForObject(e.getChild());
		    
			int index = getCorrectIndex(e);
			
		    TreeModelEvent treeEvent = new TreeModelEvent(this, treePath.getParentPath(), 
		    		new int[] {index},
		    		new Object[] {e.getChild()});
			fireTreeNodesInserted(treeEvent);
		}



		public void wabitChildRemoved(WabitChildEvent e) {
            WabitUtils.unlistenToHierarchy(e.getChild(), this, this);
		    TreePath treePath = createTreePathForObject(e.getChild());
		    
			int index = getCorrectIndex(e);
//			if (treePath.getParentPath() != null) {
//				treePath = treePath.getParentPath();
//			}
			TreeModelEvent treeEvent = new TreeModelEvent(this, treePath.getParentPath(),
					new int[] { index }, new Object[] { e.getChild() });
			fireTreeNodesRemoved(treeEvent);
		}

		/**
		 * This method will get the correct 
		 * @param e
		 * 		This is the 
		 * @param backupIndex
		 * @return
		 */
		private int getCorrectIndex(WabitChildEvent e) {
			int index = 0;
			WabitObject wabitObject = e.getChild();

			index += e.getIndex();
			if (wabitObject instanceof WabitDataSource) return index;
			
			index -= (workspace.getConnections().size());
			
			if (wabitObject instanceof OlapQuery || wabitObject instanceof QueryCache) return index;
			
			index -= (workspace.getQueries().size()) + (workspace.getOlapQueries().size());
			
			if (wabitObject instanceof Layout) return index;
			
			return e.getIndex();
		}
    }
    
    public TreePath createTreePathForObject(WabitObject obj) {
    	List<Object> path = new ArrayList<Object>();
    	path.add(0, obj);
    	while (obj.getParent() != null && obj != getRoot()) {
    		if (obj.getParent() == getRoot()) {
            	//this will be where the folders are
            	FolderType folderType = getProperFolderParent(obj);
            	for (FolderNode folder : folderList) {
            		if (folderType == folder.getFolderType()) {
            			path.add(0, folder);
            		}
            	}
            }
            obj = obj.getParent();
            path.add(0, obj);
    	}
    	return new TreePath(path.toArray());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    WabitWorkspace p = new WabitWorkspace();
                    
                    // Add data sources to workspace
                    DataSourceCollection<SPDataSource> plini = new PlDotIni();
                    plini.read(new File(System.getProperty("user.home"), "pl.ini"));
                    List<SPDataSource> dataSources = plini.getConnections();
                    for (int i = 0; i < 10 && i < dataSources.size(); i++) {
                        p.addDataSource(new WabitDataSource(dataSources.get(i)));
                    }
                    
                    // TODO: Add queries to workspace
                    
                    // Add layouts to workspace
                    Layout layout = new Layout("Example Layout");
                    p.addLayout(layout);
                    Page page = layout.getPage();
                    page.addContentBox(new ContentBox());
                    page.addGuide(new Guide(Axis.HORIZONTAL, 123));
                    page.addContentBox(new ContentBox());
                    
                    // Show workspace tree in a frame
                    WorkspaceTreeModel tm = new WorkspaceTreeModel(p);
                    JTree tree = new JTree(tm);
                    tree.setCellRenderer(new WorkspaceTreeCellRenderer());
                    JFrame f = new JFrame("Tree!");
                    f.setContentPane(new JScrollPane(tree));
                    f.pack();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
