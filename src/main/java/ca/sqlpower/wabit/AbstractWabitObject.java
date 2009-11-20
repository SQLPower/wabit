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

package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;

public abstract class AbstractWabitObject extends AbstractSPObject implements WabitObject {

    private static final Logger logger = Logger.getLogger(AbstractWabitObject.class);
    
    public AbstractWabitObject() {
    	super();
    }

	/**
	 * The uuid string passed in must be the toString representation of the UUID
	 * for this object. If the uuid string given is null then a new UUID will be
	 * automatically generated.
	 */
    public AbstractWabitObject(String uuid) {
    	super(uuid);
    }

    @Override
    public void generateNewUUID() {
    	uuid = WabitUtils.randomWabitUUID();
    }

	public WabitObject getParent() {
		return (WabitObject) super.getParent();
	}

	public final boolean removeChild(SPObject child)
	        throws ObjectDependentException {
	    if (!getChildren().contains(child)) 
	        throw new IllegalArgumentException("Child object " + child.getName() + " of type " + child.getClass()
	                + " is not a child of " + getName() + " of type " + getClass());
	    
	    WabitObject topAncestor = this;
	    while (topAncestor.getParent() != null) {
	        topAncestor = topAncestor.getParent();
	    }
	    WorkspaceGraphModel graph = new WorkspaceGraphModel(topAncestor, child, true, true);
	    for (SPObject graphNode : graph.getNodes()) {
	        List<SPObject> ancestors = new ArrayList<SPObject>();
	        SPObject ancestor = graphNode.getParent();
	        while (ancestor != null) {
	            ancestors.add(ancestor);
	            ancestor = ancestor.getParent();
	        }
	        if (!graphNode.equals(child) && !ancestors.contains(child)
	                && !graphNode.equals(topAncestor)) {
	            throw new ObjectDependentException("The child " + child.getName() + " being" +
	            		" removed from " + this.getName() + " is depended on by " + graphNode.getName() + " of type " + graphNode.getClass());
	        }
	    }
	    
	    
	    boolean removed = removeChildImpl(child);
	    if (removed) {
	    	child.setParent(null);
	    	return true;
	    } else {
	    	return false;
	    }
	}
	
	/**
	 * Default cleanup method that does nothing. Override and implement this
	 * method if cleanup is necessary.
	 */
	public CleanupExceptions cleanup() {
	    return new CleanupExceptions();
	}

    /**
     * Helper method to find the session of a WabitObject. This will walk up the
     * workspace tree to the WabitWorkspace and get its session. If the highest
     * ancestor is not a WabitWorkspace or the workspace is not attached to a
     * session this will throw a SessionNotFoundException.
     */
	public WabitSession getSession() {
	    return WabitUtils.getSession(this);
	}

    /**
     * Calls the runInBackground method on the session this object is attached
     * to if it exists. If this object is not attached to a session, which can
     * occur when loading, copying, or creating a new object, the runner will be
     * run on the current thread due to not being able to run elsewhere. Any
     * WabitObject that wants to run a runnable in the background should call to
     * this method instead of to the session.
     * 
     * @see WabitSession#runInBackground(Runnable)
     */
	protected void runInBackground(Runnable runner) {
	    try {
	        getSession().runInBackground(runner);
	    } catch (SessionNotFoundException e) {
	        runner.run();
	    }
	}

    /**
     * Calls the runInForeground method on the session this object is attached
     * to if it exists. If this object is not attached to a session, which can
     * occur when loading, copying, or creating a new object, the runner will be
     * run on the current thread due to not being able to run elsewhere. Any
     * WabitObject that wants to run a runnable in the foreground should call to
     * this method instead of to the session.
     * 
     * @see WabitSession#runInBackground(Runnable)
     */
	protected void runInForeground(Runnable runner) {
	    try {
	        getSession().runInForeground(runner);
	    } catch (SessionNotFoundException e) {
	        runner.run();
	    }
	}
	
	protected boolean isForegroundThread() {
		try {
			return getSession().isForegroundThread();
		} catch (SessionNotFoundException e) {
			return true;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WabitObject) {
			if (this.getUUID().equals(((WabitObject)obj).getUUID())) {
				return true;
			}
		}
		return false;
	}
	
	public void commit() {
		fireTransactionEnded();
	}
	
	public void rollback(String message) {
		fireTransactionRollback(message);
	}

	@Override
	public String toString() {
		return super.toString() + ", " + getName() + ":" + getUUID();
	}
}
