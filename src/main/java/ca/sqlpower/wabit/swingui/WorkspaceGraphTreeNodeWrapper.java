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

import ca.sqlpower.object.SPObject;

import com.rc.retroweaver.runtime.Collections;

/**
 * Wraps a WabitObject that is a node in a WorkspaceGraphModel so it can
 * represent a node in a tree. Each node has one parent, which is null 
 * for the root object, and any number of children.
 */
public class WorkspaceGraphTreeNodeWrapper {

    private final SPObject wrappedObject;
    
    /**
     * A list of children of this node.
     */
    private final List<WorkspaceGraphTreeNodeWrapper> children = 
        new ArrayList<WorkspaceGraphTreeNodeWrapper>();
    
    /**
     * The parent of this node in the tree. This will only be null
     * if this node is the root node of a tree.
     */
    private WorkspaceGraphTreeNodeWrapper parent;

    public WorkspaceGraphTreeNodeWrapper(SPObject wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public SPObject getWrappedObject() {
        return wrappedObject;
    }

    public void setParent(WorkspaceGraphTreeNodeWrapper parent) {
        this.parent = parent;
    }

    public WorkspaceGraphTreeNodeWrapper getParent() {
        return parent;
    }
    
    public void addChild(WorkspaceGraphTreeNodeWrapper child) {
        children.add(child);
        child.setParent(this);
    }
    
    public void removeChild(WorkspaceGraphTreeNodeWrapper child) {
        children.remove(child);
        child.setParent(null);
    }
    
    /**
     * Returns an unmodifiable list of the children of this node.
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<WorkspaceGraphTreeNodeWrapper> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public int getIndexOfChild(WorkspaceGraphTreeNodeWrapper child) {
        return children.indexOf(child);
    }
    
    public WorkspaceGraphTreeNodeWrapper getChild(int index) {
        return children.get(index);
    }
    
    public String getName() {
        StringBuffer buffer = new StringBuffer();
        SPObject ancestor = wrappedObject.getParent();
        while (ancestor != null) {
            if (buffer.length() > 0) {
                buffer.insert(0, "/");
            }
            buffer.insert(0, ancestor.getName());
            ancestor = ancestor.getParent();
        }
        
        return wrappedObject.getName() + "   (" + buffer.toString() + ")";
    }
}
