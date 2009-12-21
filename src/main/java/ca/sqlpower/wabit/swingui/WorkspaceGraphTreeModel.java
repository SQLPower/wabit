/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.WorkspaceGraphModel;

/**
 * Displays a WorkspaceGraphModel as a tree. Each node in the tree is wrapped in
 * a {@link WorkspaceGraphTreeNodeWrapper} object in case it needs to be placed
 * in the tree multiple times. This allows two different objects in the graph to
 * point to the same object. If there is a cycle in the graph the cycle will be
 * broken when an object in the cycle is reached that appears as a parent to it
 * in the tree.
 */
public class WorkspaceGraphTreeModel implements TreeModel {

    /**
     * Contains all of the current listeners on this model.
     */
    private final List<TreeModelListener> treeModelListeners = 
        new ArrayList<TreeModelListener>();
    
    /**
     * This is the root of the tree this tree model is representing.
     */
    private WorkspaceGraphTreeNodeWrapper rootTreeNode;

    /**
     * Constructs a tree model based on the graph given to it.
     */
    public WorkspaceGraphTreeModel(WorkspaceGraphModel model) {
        rootTreeNode = addNodeToTree(null, model.getGraphStartNode(), model);
    }

    /**
     * Helper method for the constructor to recursively build the tree from the
     * given graph.
     * 
     * @return The node returned is the tree node created by the call to this
     *         method. If the node cannot be made as it would make an infinite
     *         cycle in the tree null is returned.
     */
    private WorkspaceGraphTreeNodeWrapper addNodeToTree(
            WorkspaceGraphTreeNodeWrapper parent, 
            SPObject nodeToAdd, WorkspaceGraphModel graph) {
        
        //if it exists in its parent chain continue
        WorkspaceGraphTreeNodeWrapper ancestor = parent;
        while (ancestor != null) {
            if (ancestor.getWrappedObject().equals(nodeToAdd)) {
                return null;
            }
            ancestor = ancestor.getParent();
        }
        
        WorkspaceGraphTreeNodeWrapper newTreeNode = new WorkspaceGraphTreeNodeWrapper(nodeToAdd);
        if (parent != null) {
            parent.addChild(newTreeNode);
        }
        for (SPObject child : graph.getAdjacentNodes(nodeToAdd)) {
            addNodeToTree(newTreeNode, child, graph);
        }
        
        return newTreeNode;
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    public Object getChild(Object parent, int index) {
        return ((WorkspaceGraphTreeNodeWrapper) parent).getChild(index);
    }

    public int getChildCount(Object parent) {
        return ((WorkspaceGraphTreeNodeWrapper) parent).getChildren().size();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((WorkspaceGraphTreeNodeWrapper) parent).getIndexOfChild(
                (WorkspaceGraphTreeNodeWrapper) child);
    }

    public Object getRoot() {
        return rootTreeNode;
    }

    public boolean isLeaf(Object node) {
        return ((WorkspaceGraphTreeNodeWrapper) node).getChildren().isEmpty();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new IllegalStateException("This tree represents a graph and cannot be " +
        		"changed unless the underlying graph is changed.");
    }

}
