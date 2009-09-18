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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

public abstract class AbstractWabitObject implements WabitObject {

    private static final Logger logger = Logger.getLogger(AbstractWabitObject.class);
    
    @GuardedBy("childListeners")
    private final List<WabitChildListener> childListeners = 
        Collections.synchronizedList(new ArrayList<WabitChildListener>());
    
    @GuardedBy("pcs")
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private WabitObject parent;
    private String name;
    
    /**
     * This UUID is for saving and loading to allow saved files to be diff friendly.
     */
    private String uuid;
    
    public AbstractWabitObject() {
        generateNewUUID();
    }

	/**
	 * The uuid string passed in must be the toString representation of the UUID
	 * for this object. If the uuid string given is null then a new UUID will be
	 * automatically generated.
	 */
    public AbstractWabitObject(String uuid) {
    	if (uuid == null) {
    	    generateNewUUID();
    	} else {
    		this.uuid = uuid;
    	}
    }
    
    public void generateNewUUID() {
        uuid = "w" + UUID.randomUUID().toString();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (pcs) {
            pcs.addPropertyChangeListener(l);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        synchronized (pcs) {
            pcs.removePropertyChangeListener(l);
        }
    }
    
    public void addChildListener(WabitChildListener l) {
    	if (l == null) {
    		throw new NullPointerException("Cannot add child listeners that are null.");
    	}
    	synchronized (childListeners) {
    	    childListeners.add(l);
    	}
    }

    public void removeChildListener(WabitChildListener l) {
        synchronized (childListeners) {
            childListeners.remove(l);
        }
    }

    /**
     * Fires a child added event to all child listeners. The child should have
     * been added by the calling code already. The event will be fired on the
     * foreground thread defined by the session being used.
     * 
     * @param type
     *            The canonical type of the child being added
     * @param child
     *            The child object that was added
     * @param index
     *            The index of the added child within its own child list (this
     *            will be converted to the overall child position before the
     *            event object is constructed).
     */
    protected void fireChildAdded(Class<? extends WabitObject> type, WabitObject child, int index) {
        synchronized(childListeners) {
            if (childListeners.isEmpty()) return;
        }
        index += childPositionOffset(type);
        final WabitChildEvent e = new WabitChildEvent(this, type, child, index);
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(childListeners) {
                    for (int i = childListeners.size() - 1; i >= 0; i--) {
                        final WabitChildListener listener = childListeners.get(i);
                        listener.wabitChildAdded(e);
                    }
                }
            }
        };
        runInForeground(runner);
    }

    /**
     * Fires a child removed event to all child listeners. The child should have
     * been removed by the calling code. The event will be fired on the
     * foreground thread defined by the session being used.
     * 
     * @param type
     *            The canonical type of the child being removed
     * @param child
     *            The child object that was removed
     * @param index
     *            The index that the removed child was at within its own child
     *            list (this will be converted to the overall child position
     *            before the event object is constructed).
     */
    protected void fireChildRemoved(Class<? extends WabitObject> type, WabitObject child, int index) {
        synchronized(childListeners) {
            if (childListeners.isEmpty()) return;
        }
        index += childPositionOffset(type);
        final WabitChildEvent e = new WabitChildEvent(this, type, child, index);
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(childListeners) {
                    for (int i = childListeners.size() - 1; i >= 0; i--) {
                        final WabitChildListener listener = childListeners.get(i);
                        listener.wabitChildRemoved(e);
                    }
                }
            }
        };
        runInForeground(runner);
    }

    /**
     * Fires a property change on the foreground thread as defined by the current
     * session being used.
     */
    protected void firePropertyChange(final String propertyName, final boolean oldValue, 
            final boolean newValue) {
        synchronized(pcs) {
            if (pcs.getPropertyChangeListeners().length == 0) return;
        }
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(pcs) {
                    pcs.firePropertyChange(propertyName, oldValue, newValue);
                }
            }
        };
        runInForeground(runner);
    }

    /**
     * Fires a property change on the foreground thread as defined by the current
     * session being used.
     */
    protected void firePropertyChange(final String propertyName, final int oldValue, 
            final int newValue) {
        synchronized(pcs) {
            if (pcs.getPropertyChangeListeners().length == 0) return;
        }
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(pcs) {
                    pcs.firePropertyChange(propertyName, oldValue, newValue);
                }
            }
        };
        runInForeground(runner);
    }

    /**
     * Fires a property change on the foreground thread as defined by the current
     * session being used.
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, 
            final Object newValue) {
        synchronized(pcs) {
            if (pcs.getPropertyChangeListeners().length == 0) return;
            if (logger.isDebugEnabled()) {
                logger.debug("Firing property change \"" + propertyName + "\" to " +
                        pcs.getPropertyChangeListeners().length +
                        " listeners: " + Arrays.toString(pcs.getPropertyChangeListeners()));
            }
        }
        Runnable runner = new Runnable() {
            public void run() {
                synchronized(pcs) {
                    pcs.firePropertyChange(propertyName, oldValue, newValue);
                }
            }
        };
        runInForeground(runner);
    }
    
	public WabitObject getParent() {
		return parent;
	}

	public void setParent(WabitObject parent) {
	    WabitObject oldParent = this.parent;
		this.parent = parent;
		if(parent != null) {
			firePropertyChange("parent", oldParent, parent);
		}
	}
	
	public String getName() {
	    return name;
	}
	
	public void setName(String name) {
	    if (name == null) {
	        throw new NullPointerException("Null name not allowed");
	    }
	    String oldName = this.name;
        this.name = name;
        firePropertyChange("name", oldName, name);
    }
	
	public String getUUID() {
		return uuid;
	}
	
	public final boolean removeChild(WabitObject child)
	        throws ObjectDependentException {
	    if (!getChildren().contains(child)) 
	        throw new IllegalArgumentException("Child object " + child.getName() 
	                + " is not a child of " + getName());
	    
	    WabitObject topAncestor = this;
	    while (topAncestor.getParent() != null) {
	        topAncestor = topAncestor.getParent();
	    }
	    WorkspaceGraphModel graph = new WorkspaceGraphModel(topAncestor, child, false, true);
	    for (WabitObject graphNode : graph.getNodes()) {
	        List<WabitObject> ancestors = new ArrayList<WabitObject>();
	        WabitObject ancestor = graphNode.getParent();
	        while (ancestor != null) {
	            ancestors.add(ancestor);
	            ancestor = ancestor.getParent();
	        }
	        if (!graphNode.equals(child) && !ancestors.contains(child)
	                && !graphNode.equals(topAncestor)) {
	            throw new ObjectDependentException("The child " + child.getName() + " being" +
	            		" removed from " + this.getName() + " is depended on by " + graphNode.getName());
	        }
	    }
	    
	    return removeChildImpl(child);
	}

    /**
     * This is the object specific implementation of removeChild. There are
     * checks in the removeChild method to ensure the child being removed has no
     * dependencies and is a child of this object.
     * 
     * @see #removeChild(WabitObject)
     */
	protected abstract boolean removeChildImpl(WabitObject child);
	
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
	protected WabitSession getSession() {
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
}
