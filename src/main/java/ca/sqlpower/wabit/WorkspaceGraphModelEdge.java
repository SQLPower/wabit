package ca.sqlpower.wabit;


/**
 * Each edge is made up of a parent {@link WabitObject} and a child
 * {@link WabitObject}. The edge goes in the direction from the parent to
 * the child.
 */
public class WorkspaceGraphModelEdge {
    
    private final WabitObject parent;
    private final WabitObject child;

    public WorkspaceGraphModelEdge(WabitObject parent, WabitObject child) {
        this.parent = parent;
        this.child = child;
    }
    
    public WabitObject getParent() {
        return parent;
    }
    
    public WabitObject getChild() {
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