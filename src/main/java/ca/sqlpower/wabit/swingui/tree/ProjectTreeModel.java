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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.WabitProject;

/**
 * Provides a tree with the project at the root. The project contains data
 * sources, queries, and layouts in that order.
 */
public class ProjectTreeModel implements TreeModel {

    private static final Logger logger = Logger
            .getLogger(ProjectTreeModel.class);
    private final WabitProject project;

    public ProjectTreeModel(WabitProject project) {
        this.project = project;
//     TODO   WabitUtils.listenToHierarchy(project, pcl, wcl);
    }
    
    public Object getRoot() {
        // TODO Auto-generated method stub
        return null;
    }


    public Object getChild(Object parent, int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getChildCount(Object parent) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isLeaf(Object node) {
        // TODO Auto-generated method stub
        return false;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // TODO Auto-generated method stub

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

}
