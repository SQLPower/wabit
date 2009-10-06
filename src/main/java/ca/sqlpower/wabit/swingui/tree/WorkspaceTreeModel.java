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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.olap4j.OlapConnection;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitQuerySelectedItem;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.WabitOlapAxis;
import ca.sqlpower.wabit.olap.WabitOlapDimension;
import ca.sqlpower.wabit.olap.WabitOlapInclusion;
import ca.sqlpower.wabit.olap.WabitOlapSelection;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.swingui.olap.Olap4jTreeModel;
import ca.sqlpower.wabit.swingui.tree.FolderNode.FolderType;

/**
 * Provides a tree with the workspace at the root. The workspace contains data
 * sources, queries, and layouts in that order.
 */
public class WorkspaceTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(WorkspaceTreeModel.class);
    
    /**
     * This is the root node of the tree
     */
    private final WabitWorkspace workspace;
    
    /**
     * This is the list of folders in the tree
     */
    private final List<FolderNode> folderList;

    /**
     * This is the listener which listens for property change events on the tree.
     */
    private WabitTreeModelEventAdapter listener;
    
    /**
     * This is the tree model which contains a workspace
     * 
     * @param workspace
     * 		This is the root node of the tree 
     */
    public WorkspaceTreeModel(WabitWorkspace workspace) {
        this.workspace = workspace;
        this.folderList = new ArrayList<FolderNode>();
        
        folderList.add(new FolderNode(workspace, FolderType.CONNECTIONS));
        folderList.add(new FolderNode(workspace, FolderType.QUERIES));
        folderList.add(new FolderNode(workspace, FolderType.CHARTS));
        folderList.add(new FolderNode(workspace, FolderType.IMAGES));
        folderList.add(new FolderNode(workspace, FolderType.TEMPLATES));
        folderList.add(new FolderNode(workspace, FolderType.REPORTS));
        listener = new WabitTreeModelEventAdapter();
        WabitUtils.listenToHierarchy(workspace, listener);
    }
    
    /**
     * Returns true if the given object should appear in the tree as a node.
     * This is determined by the object's type.
     * 
     * @param o The object to test
     * @return True if o should appear; false if it should not.
     */
    private boolean appearsInTree(WabitObject o) {
        if (o instanceof WabitWorkspace) return true;
        if (o instanceof WabitDataSource) return true;
        if (o instanceof QueryCache) return true;
        if (o instanceof OlapQuery) return true;
        if (o instanceof FolderNode) return true;
        if (o instanceof Template) return true;
        if (o instanceof Report) return true;
        if (o instanceof Chart) return true;
        if (o instanceof ChartColumn) return true;
        if (o instanceof ContentBox) return true;
        if (o instanceof WabitImage) return true;
        if (o instanceof WabitQuerySelectedItem) return true;
        if (o instanceof WabitOlapAxis) return true;
        if (o instanceof WabitOlapDimension) return true;
        if (o instanceof WabitOlapSelection) return true;
        return false;
    }
    
    public Object getRoot() {
        return workspace;
    }
    
    public Object getChild(Object parentObject, int index) {
		if (parentObject instanceof WabitWorkspace) {
			return folderList.get(index);
    	} else if (parentObject instanceof FolderNode) {
    		return ((FolderNode) parentObject).getChildren().get(index);
    	} else if (parentObject instanceof Layout) {
    		return  getLayoutsChildren((Layout) parentObject).get(index);
    	} else if (parentObject instanceof ContentBox) {
    		return new ArrayList<Object>();
    	} else if (parentObject instanceof WabitDataSource) {
    		List<Object> children = getWabitDatasourceChildren((WabitDataSource) parentObject);
    		return children.get(index);
    	} else if (parentObject instanceof QueryCache) {
    	    return ((QueryCache) parentObject).getSelectedWabitColumns().get(index);
    	} else if (parentObject instanceof SQLObject) {
    		try {
				return ((SQLObject) parentObject).getChild(index);
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
    	} else if (parentObject instanceof Olap4jTreeObject) {
    		Olap4jTreeObject parentTreeNode = (Olap4jTreeObject) parentObject;
    		Olap4jTreeModel model = getOlapTreeModelFromNode(parentTreeNode);
    		Object olap4jObject = model.getChild(parentTreeNode.getOlapObject(), index);
			Olap4jTreeObject newTreeNode = new Olap4jTreeObject(olap4jObject);
			newTreeNode.setParent(parentTreeNode);
			return newTreeNode;
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
    	} else if (parent instanceof Layout) {
    		return getLayoutsChildren((Layout) parent).size();
    	} else if (parent instanceof ContentBox) {
    		return 0;
    	} else if (parent instanceof WabitDataSource) {
    		WabitDataSource wds = (WabitDataSource) parent;
			List<Object> children = getWabitDatasourceChildren(wds);
    		return children.size();
    	} else if (parent instanceof QueryCache) {
    	    return ((QueryCache) parent).getSelectedWabitColumns().size();
    	} else if (parent instanceof SQLObject) {
    		try {
				return ((SQLObject) parent).getChildCount();
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
    	} else if (parent instanceof Olap4jTreeObject) {
    		Olap4jTreeObject treeNode = (Olap4jTreeObject) parent;
    		Olap4jTreeModel model = getOlapTreeModelFromNode(treeNode);
    		return model.getChildCount(treeNode.getOlapObject());
    	} else {
    		return ((WabitObject) parent).getChildren().size(); // XXX would be more efficient if we could ask for a child count
    	}
    }

    /**
     * This is a hash map of all the {@link OlapConnection}'s associated with each delegate {@link Olap4jTreeModel}.
     * For an explaination of why this is necessary see {@link Olap4jTreeObject#Olap4jTreeObject(Object)}.
     */
    private Map<OlapConnection, Olap4jTreeModel> treeModelMap = new HashMap<OlapConnection, Olap4jTreeModel>();
    
    /**
     * This method will return all the children of any given layout.
     */
	private List<WabitObject> getLayoutsChildren(Layout parent) {
		List<WabitObject> layoutChildren = new ArrayList<WabitObject>();
		List<Page> page = ((Layout) parent).getChildren();
		for (WabitObject wo : ((Page) page.get(0)).getChildren()) {
			if (wo instanceof ContentBox) {
				layoutChildren.add(wo);
			}
		}
		return layoutChildren;
	}
	
	@SuppressWarnings("unchecked") //everything in here will be an object, no warning is needed
	private List<Object> getWabitDatasourceChildren(WabitDataSource parent) {
		List<Object> children = new ArrayList<Object>();
		SPDataSource spDS = parent.getSPDataSource();
		WabitSessionContext context = workspace.getSession().getContext();
		if (spDS instanceof JDBCDataSource) {
			JDBCDataSource jdbcDS = (JDBCDataSource) spDS;
			try {
				children.addAll(context.getDatabase(jdbcDS).getChildren());
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
		} else if (spDS instanceof Olap4jDataSource) {
			Olap4jDataSource olapDS = (Olap4jDataSource) spDS;
			Olap4jTreeModel olapTreeModel;
			OlapConnection connection;
			try {
				connection = context.createConnection(olapDS);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (treeModelMap.containsKey(connection)) {
				olapTreeModel = treeModelMap.get(connection);
			} else {
				olapTreeModel = new Olap4jTreeModel(Collections.singletonList(connection));
				treeModelMap.put(connection, olapTreeModel);
			}
			Object root = olapTreeModel.getRoot();
			for (int i = 0; i < olapTreeModel.getChildCount(root); i++) {
				Olap4jTreeObject node = new Olap4jTreeObject(olapTreeModel.getChild(root, i));
				node.setParent(connection);
				children.add(node);
			}
		} else {
			throw new UnsupportedOperationException("Datasource of type " + spDS.getClass().toString() + " is not yet" +
					"supported in the tree on the LHS");
		}
		return children;
	}
	
	/**
	 * When passed an {@link Olap4jTreeObject} this method will return the delegate {@link Olap4jTreeModel}
	 * associated with it.
	 */
	private Olap4jTreeModel getOlapTreeModelFromNode(Olap4jTreeObject treeObject) {
		Object nodeData = treeObject.getOlapObject();
		while (!(nodeData instanceof OlapConnection)) {
			treeObject = (Olap4jTreeObject) treeObject.getParent();
			nodeData = treeObject.getOlapObject();
		}
		OlapConnection olapConnection = (OlapConnection) nodeData;
		if (treeModelMap.containsKey(olapConnection)) {
			return treeModelMap.get(olapConnection);
		} else {
			throw new IllegalStateException("OlapConnection should have been in the map already because " +
					"the user should have had to go through a WabitDataSource which would have added the connection" +
					" to the map.");
		}
	}
    
    public int getIndexOfChild(Object parent, Object child) {
    	if (parent instanceof WabitWorkspace) {
    		return folderList.indexOf(child);
    	} else if (parent instanceof FolderNode) {
    		return ((FolderNode) parent).getChildren().indexOf(child);
    	} else if (parent instanceof WabitDataSource) {
    		WabitDataSource wds = (WabitDataSource) parent;
			List<Object> children = getWabitDatasourceChildren(wds);
    		return children.indexOf(child);
    	} else if (parent instanceof SQLObject) {
    		try {
				return ((SQLObject) parent).getChildren().indexOf(child);
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
    	} else if (parent instanceof QueryCache) {
    	    return ((QueryCache) parent).getSelectedWabitColumns().indexOf(child);
    	} else if (parent instanceof WabitObject) {
	        WabitObject wo = (WabitObject) parent;
	        List<? extends WabitObject> children = wo.getChildren();
	        return children.indexOf(child);
    	} else if (parent instanceof Olap4jTreeObject){
    		Olap4jTreeObject treeNode = (Olap4jTreeObject) parent;
			Olap4jTreeModel model = getOlapTreeModelFromNode(treeNode);
    		Olap4jTreeObject treeNodeChild = (Olap4jTreeObject) child;
			return model.getIndexOfChild(treeNode.getOlapObject(), treeNodeChild.getOlapObject());
    	} else {
    		throw new UnsupportedOperationException("Object of type " + parent.getClass().toString() + " " +
    				"not yet supported in Left Hand tree model");
    	}
    }

    public boolean isLeaf(Object node) {
    	boolean retval;
    	if (node instanceof FolderNode) {
    		retval = !(((FolderNode) node).allowsChildren());
    	} else if (node instanceof ContentBox) {
    		retval = true;
    	} else if (node instanceof WabitDataSource) {
    		retval = false;
    	} else if (node instanceof SQLDatabase){
    		retval = false;
    	} else if (node instanceof SQLObject) {
    		retval = !((SQLObject) node).allowsChildren();
    	} else if (node instanceof WabitObject) {
    		retval = !((WabitObject) node).allowsChildren();
    	} else {
    		Olap4jTreeObject treeNode = (Olap4jTreeObject) node;
    		Olap4jTreeModel model = getOlapTreeModelFromNode(treeNode);
    		retval = model.isLeaf(treeNode.getOlapObject());
    	}
		return retval;
    }
    
    /**
     * This is a class which wraps around every tree object
     */
    public class Olap4jTreeObject {
    	private Object olapObject;
    	private Object parent;
    	
        /**
         * This is a class which wraps around an Olap4jObject in the tree, since all Olap4j objects
         * don't decend from a particular interface it is tedious and in some cases impossible to get
         * the {@link OlapConnection} object from just a general Olap4j object (and impossible to do 
         * so when dealing with certain query objects, ex. Dimension, Measures). The reason we need 
         * this {@link OlapConnection} is because the {@link Olap4jTreeModel} is a complicated entity so
         * we want to just delegate to it, we do this by having a map of {@link OlapConnection}s to
         * {@link Olap4jTreeModel}'s. 
         * 
         * @param olapObject
         * 		The {@link Olap4jTreeObject} being wrapped around.
         */
    	public Olap4jTreeObject(Object olapObject) {
    		this.olapObject = olapObject;
		}
    	
    	public Object getParent() {
			return parent;
		}
    	
    	public void setParent(Object parent) {
			this.parent = parent;
		}
    	
    	public Object getOlapObject() {
			return olapObject;
		}
    	
    	@Override
    	public String toString() {
    		return olapObject.toString();
    	}
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

    protected void fireTreeNodesInserted(final TreeModelEvent e) {
        SPSUtils.runOnSwingThread(new Runnable() {
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("Firing treeNodesInserted event: " + e);
                }
                for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
                    treeModelListeners.get(i).treeNodesInserted(e);
                }
            }
        });
    }

    protected void fireTreeNodesRemoved(final TreeModelEvent e) {
        SPSUtils.runOnSwingThread(new Runnable() {
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("Firing treeNodesRemoved event " + e);
                }
                for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
                    treeModelListeners.get(i).treeNodesRemoved(e);
                }
            }
        });
    }

    protected void fireTreeNodesChanged(final TreeModelEvent e) {
        SPSUtils.runOnSwingThread(new Runnable() {
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("firing TreeNodesChanged. source=" + e.getSource());
                }
                for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
                    treeModelListeners.get(i).treeNodesChanged(e);
                }
            }
        });
    }

    protected void fireTreeStructureChanged(final TreeModelEvent e) {
        SPSUtils.runOnSwingThread(new Runnable() {
            public void run() {
                if (logger.isDebugEnabled()) {
                    logger.debug("firing TreeStructuredChanged. source=" + e.getSource());
                }
                for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
                    treeModelListeners.get(i).treeStructureChanged(e);
                }
            }
        });
    }
    
    /**
	 * A private event handler that listens for {@link PropertyChangeEvent} and
	 * {@link WabitChildEvent} from the business model and 'translates' them
	 * into {@link TreeModelEvent} for the WorkspaceTreeModel.
	 */
    private class WabitTreeModelEventAdapter implements WabitListener {
        
		public void propertyChange(PropertyChangeEvent evt) {
			WabitObject node = (WabitObject) evt.getSource();
			if (!appearsInTree(node)) {
			    return;
			}
			TreeModelEvent e;
			if (node == getRoot()) {
				// special case for root node
				e = new TreeModelEvent(this, new Object[] { getRoot() }, null,
						null);
			} else {
                TreePath treePath = createTreePathForObject(node);
                treePath = treePath.getParentPath();
                int actualIndex = node.getParent().getChildren().indexOf(node);
                if (actualIndex < 0) {
                    throw new IllegalStateException(
                            "Got an event from a WabitObject that isn't one " +
                            "of its own parent's children! Parent : " + node.getParent().getName());
                }
	            int treeIndex = getCorrectIndex(node, actualIndex);
				e = new TreeModelEvent(this, treePath,
						new int[] { treeIndex }, new Object[] { node });
				
				if (logger.isDebugEnabled()) {
				    logger.debug("Created change event for tree path " + treePath);
				    logger.debug("actualIndex = " + actualIndex);
				    logger.debug("treeIndex = " + treeIndex);
				    logger.debug("node = " + node);
				}
			}
			fireTreeNodesChanged(e);
		}

		public void wabitChildAdded(WabitChildEvent e) {
		    WabitUtils.listenToHierarchy(e.getChild(), this);
		    if (!appearsInTree(e.getChild())) {
		        return;
		    }
		    TreePath treePath = createTreePathForObject(e.getChild());
		    
			int index = getCorrectIndex(e);
			
		    TreeModelEvent treeEvent = new TreeModelEvent(this, treePath.getParentPath(), 
		    		new int[] {index},
		    		new Object[] {e.getChild()});
			fireTreeNodesInserted(treeEvent);
		}

		public void wabitChildRemoved(WabitChildEvent e) {
            WabitUtils.unlistenToHierarchy(e.getChild(), this);
            if (!appearsInTree(e.getChild())) {
                return;
            }
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
         * For each child type, rebases the child index of Workspace children to
         * 0 because this tree model puts them in folders. The indices of other
         * children are returned as-is.
         * 
         * @param e
         *            A WabitEvent for either an added or removed child of any
         *            WabitObject.
         */
		private int getCorrectIndex(WabitChildEvent e) {
		    return getCorrectIndex(e.getChild(), e.getIndex());
		}
		
		private int getCorrectIndex(WabitObject wabitObject, final int actualIndex) {
		    
		    if (wabitObject instanceof WabitQuerySelectedItem) {
	            //only displaying the selected items of a QueryCache
	            return actualIndex - wabitObject.getParent().childPositionOffset(WabitQuerySelectedItem.class);
	        }

			// Unfortunately, can't use WabitObject.childPositionOffset because
			// MDX and SQL query objects are mixed in the same folder and therefore
			// need the same offset.
			
			int index = actualIndex;
			if (wabitObject instanceof WabitDataSource) return index;
			index -= (workspace.getConnections().size());
			
			if (wabitObject instanceof OlapQuery || wabitObject instanceof QueryCache) return index;
			index -= (workspace.getQueries().size()) + (workspace.getOlapQueries().size());
			
            if (wabitObject instanceof Chart) return index;
            index -= (workspace.getCharts().size());

            if (wabitObject instanceof WabitImage) return index;
            index -= (workspace.getImages().size());
            
            if (wabitObject instanceof Template) return index;
            index -= (workspace.getTemplates().size());
			
			if (wabitObject instanceof Report) return index;
			
			return actualIndex;
		}

        public void transactionEnded(TransactionEvent e) {
            //do nothing
            
        }

        public void transactionRollback(TransactionEvent e) {
            //do nothing            
        }

        public void transactionStarted(TransactionEvent e) {
            //do nothing            
        }

    }

    /**
     * Returns the correct tree path to the given Wabit Object, or null if the
     * given object shouldn't appear in this tree model.
     * 
     * @param obj The WabitObject to calculate a tree path for
     * @return The tree path to the given object, or null.
     */
    public TreePath createTreePathForObject(WabitObject obj) {
        if (!appearsInTree(obj)) {
            return null;
        }
    	List<Object> path = new ArrayList<Object>();
    	path.add(0, obj);
    	while (obj.getParent() != null && obj != getRoot()) {
    		if (obj.getParent() == getRoot()) {
            	//this will be where the folders are
            	FolderType folderType = FolderNode.getProperFolderParent(obj);
            	for (FolderNode folder : folderList) {
            		if (folderType == folder.getFolderType()) {
            			path.add(0, folder);
            		}
            	}
            }
            obj = obj.getParent();
            if (!(obj instanceof Page || obj instanceof ReportContentRenderer)) {
            	path.add(0, obj);
            }
    	}
    	if (path.get(0) != getRoot()) {
    	    throw new IllegalArgumentException(
    	            "The given object cannot be found in this tree. " +
    	            "Its apparent root is " + path.get(0) + 
    	            " but this tree's root is " + getRoot());
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
                    Report layout = new Report("Example Layout");
                    p.addReport(layout);
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
