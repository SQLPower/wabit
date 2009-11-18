package ca.sqlpower.wabit;

import ca.sqlpower.object.SPObject;


/**
 * Each edge is made up of a parent {@link WabitObject} and a child
 * {@link WabitObject}. The edge goes in the direction from the parent to
 * the child.
 */
public class WorkspaceGraphModelEdge {
    
    private final SPObject parent;
    private final SPObject child;

    public WorkspaceGraphModelEdge(SPObject parent, SPObject child) {
        this.parent = parent;
        this.child = child;
    }
    
    public SPObject getParent() {
        return parent;
    }
    
    public SPObject getChild() {
        return child;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkspaceGraphModelEdge) {
            WorkspaceGraphModelEdge wabitObject = (WorkspaceGraphModelEdge) obj;
            return getParent().equals(wabitObject.getParent()) && getChild().equals(wabitObject.getChild());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + getParent().hashCode();
        result = 37 * result + getChild().hashCode();
        return result;
    }
}