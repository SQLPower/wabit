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
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitChildListener;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Guide.Axis;

/**
 * Provides a tree with the project at the root. The project contains data
 * sources, queries, and layouts in that order.
 */
public class ProjectTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(ProjectTreeModel.class);
    
    private final WabitProject project;

    private WabitTreeModelEventAdapter listener;
    
    public ProjectTreeModel(WabitProject project) {
        this.project = project;
        listener = new WabitTreeModelEventAdapter();
        WabitUtils.listenToHierarchy(project, listener, listener);
    }
    
    public Object getRoot() {
        return project;
    }

    public Object getChild(Object parent, int index) {
        return ((WabitObject) parent).getChildren().get(index);
    }

    public int getChildCount(Object parent) {
        return ((WabitObject) parent).getChildren().size(); // XXX would be more efficient if we could ask for a child count
    }

    public int getIndexOfChild(Object parent, Object child) {
        WabitObject wo = (WabitObject) parent;
        List<? extends WabitObject> children = wo.getChildren();
        return children.indexOf(child);
    }

    public boolean isLeaf(Object node) {
    	boolean retval = !((WabitObject) node).allowsChildren();
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
	 * into {@link TreeModelEvent} for the ProjectTreeModel
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
				e = new TreeModelEvent(this, pathToNode(parent),
						new int[] { indexOfChild }, new Object[] { node });
			}
			fireTreeNodesChanged(e);
		}

		public void wabitChildAdded(WabitChildEvent e) {
		    WabitUtils.listenToHierarchy(e.getChild(), this, this);
			TreeModelEvent treeEvent = new TreeModelEvent(this, pathToNode(e
					.getSource()), new int[] { e.getIndex() }, new Object[] { e
					.getChild() });
			fireTreeNodesInserted(treeEvent);
		}

		public void wabitChildRemoved(WabitChildEvent e) {
            WabitUtils.unlistenToHierarchy(e.getChild(), this, this);
			TreeModelEvent treeEvent = new TreeModelEvent(this, pathToNode(e
					.getSource()), new int[] { e.getIndex() }, new Object[] { e
					.getChild() });
			fireTreeNodesRemoved(treeEvent);
		}
    	
	    private TreePath pathToNode(WabitObject o) {
	        List<WabitObject> path = new ArrayList<WabitObject>();
	        while (o != null) {
	            path.add(0, o);
	            if (o == getRoot()) break;
	            o = o.getParent();
	        }
	        if (path.get(0) != getRoot()) {
	            // note: if you get this exception, it's probably because the item
	            // at the beginning of the path needs its parent reference set, but
	            // it might also be because one of the other items in the path points
	            // to a node that's in a different tree. So check into both possibilities!
	            throw new IllegalStateException("Parent pointer is missing at " + path.get(0));
	        }
	        return new TreePath(path.toArray());
	    }
    }
    
    public TreePath createTreePathForObject(WabitObject obj) {
    	WabitObject pathObject = obj;
    	List<WabitObject> path = new ArrayList<WabitObject>();
    	while (pathObject.getParent() != null) {
    		path.add(0, pathObject);
    		pathObject = pathObject.getParent();
    	}
    	path.add(0, pathObject);
    	return new TreePath(path.toArray());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    WabitProject p = new WabitProject();
                    
                    // Add data sources to project
                    DataSourceCollection<SPDataSource> plini = new PlDotIni();
                    plini.read(new File(System.getProperty("user.home"), "pl.ini"));
                    List<SPDataSource> dataSources = plini.getConnections();
                    for (int i = 0; i < 10 && i < dataSources.size(); i++) {
                        p.addDataSource(new WabitDataSource(dataSources.get(i)));
                    }
                    
                    // TODO: Add queries to project
                    
                    // Add layouts to project
                    Layout layout = new Layout("Example Layout");
                    p.addLayout(layout);
                    Page page = layout.getPage();
                    page.addContentBox(new ContentBox());
                    page.addGuide(new Guide(Axis.HORIZONTAL, 123));
                    page.addContentBox(new ContentBox());
                    
                    // Show project tree in a frame
                    ProjectTreeModel tm = new ProjectTreeModel(p);
                    JTree tree = new JTree(tm);
                    tree.setCellRenderer(new ProjectTreeCellRenderer());
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
