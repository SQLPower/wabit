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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.wabit.WabitChildEvent.EventType;

public abstract class AbstractWabitObject implements WabitObject {

    private static final Logger logger = Logger.getLogger(AbstractWabitObject.class);
    
    @GuardedBy("childListeners")
    private final List<WabitListener> listeners = 
        Collections.synchronizedList(new ArrayList<WabitListener>());
    
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
    
    public void setUUID(String uuid) {
        String oldUUID = this.uuid;
        if (uuid == null){
            generateNewUUID();
        } else {
            this.uuid = uuid;
        }
        firePropertyChange("UUID", oldUUID, uuid);
    }
    
    public void generateNewUUID() {
        uuid = WabitUtils.randomWabitUUID();
    }
    
    public void addWabitListener(WabitListener l) {
    	if (l == null) {
    		throw new NullPointerException("Cannot add child listeners that are null.");
    	}
    	synchronized (listeners) {
    	    listeners.add(l);
    	}
    }

    public void removeWabitListener(WabitListener l) {
        synchronized (listeners) {
            listeners.remove(l);
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
     * @return The child event that was fired or null if no event was fired, for
     *         testing purposes.
     */
    protected WabitChildEvent fireChildAdded(Class<? extends WabitObject> type, WabitObject child, int index) {
    	logger.debug("Child Added: " + type + " notifying " + listeners.size() + " listeners");
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for adding the child " + child.getName() + 
    				" must fired on the foreground thread.");
    	}
        synchronized(listeners) {
            if (listeners.isEmpty()) return null;
        }
        final WabitChildEvent e = new WabitChildEvent(this, type, child, index, EventType.ADDED);
        synchronized(listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		final WabitListener listener = listeners.get(i);
        		listener.wabitChildAdded(e);
        	}
        }
        return e;
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
     * @return The child event that was fired or null if no event was fired, for
     *         testing purposes.
     */
    protected WabitChildEvent fireChildRemoved(Class<? extends WabitObject> type, WabitObject child, int index) {
    	logger.debug("Child Removed: " + type + " notifying " + listeners.size() + " listeners: " + listeners);
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for removing the child " + child.getName() + 
    				" must fired on the foreground thread.");
    	}
        synchronized(listeners) {
            if (listeners.isEmpty()) return null;
        }
        final WabitChildEvent e = new WabitChildEvent(this, type, child, index, EventType.REMOVED);
        synchronized(listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		final WabitListener listener = listeners.get(i);
        		listener.wabitChildRemoved(e);
        	}
        }
        return e;
    }

    /**
     * Fires a property change on the foreground thread as defined by the
     * current session being used.
     * 
     * @return The property change event that was fired or null if no event was
     *         fired, for testing purposes.
     */
    protected PropertyChangeEvent firePropertyChange(final String propertyName, final boolean oldValue, 
            final boolean newValue) {
    	if (oldValue == newValue) return null;
    	
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for property change " + propertyName + 
    				" must fired on the foreground thread.");
    	}
        synchronized(listeners) {
            if (listeners.size() == 0) return null;
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        synchronized(listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).propertyChange(evt);
        	}
        }
        return evt;
    }

    /**
     * Fires a property change on the foreground thread as defined by the
     * current session being used.
     * 
     * @return The property change event that was fired or null if no event was
     *         fired, for testing purposes.
     */
    protected PropertyChangeEvent firePropertyChange(final String propertyName, final int oldValue, 
            final int newValue) {
    	if (oldValue == newValue) return null;
    	
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for property change " + propertyName + 
    				" must fired on the foreground thread.");
    	}
        synchronized(listeners) {
            if (listeners.size() == 0) return null;
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        synchronized(listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).propertyChange(evt);
        	}
        }
        return evt;
    }

    /**
     * Fires a property change on the foreground thread as defined by the
     * current session being used.
     * 
     * @return The property change event that was fired or null if no event was
     *         fired, for testing purposes.
     */
    protected PropertyChangeEvent firePropertyChange(final String propertyName, final Object oldValue, 
            final Object newValue) {
    	if ((oldValue == null && newValue == null)
    			|| (oldValue != null && oldValue.equals(newValue))) return null; 
    	
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for property change " + propertyName + 
    				" must fired on the foreground thread.");
    	}
        synchronized(listeners) {
            if (listeners.size() == 0) return null;
            if (logger.isDebugEnabled()) {
                logger.debug("Firing property change \"" + propertyName
                        + "\" to " + listeners.size() + " listeners: "
                        + listeners);
            }
        }
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        synchronized(listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).propertyChange(evt);
        	}
        }
        return evt;
    }
    
    /**
     * Fires a transaction started event with a message indicating the
     * reason/type of the transaction.
     * 
     * @return The event that was fired or null if no event was fired, for
     *         testing purposes.
     */
    protected TransactionEvent fireTransactionStarted(final String message) {
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for a transaction start" + 
    				" must fired on the foreground thread.");
    	}
        synchronized (listeners) {
            if (listeners.size() == 0) return null;            
        }
        final TransactionEvent evt = TransactionEvent.createStartTransactionEvent(this, message);
        synchronized (listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).transactionStarted(evt);
        	}
        }
        return evt;
    }

    /**
     * Fires a transaction ended event.
     * 
     * @return The event that was fired or null if no event was fired, for
     *         testing purposes.
     */
    protected TransactionEvent fireTransactionEnded() {
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for a transaction end" + 
    				" must fired on the foreground thread.");
    	}
        synchronized (listeners) {
            if (listeners.size() == 0) return null;            
        }
        final TransactionEvent evt = TransactionEvent.createEndTransactionEvent(this);
        synchronized (listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).transactionEnded(evt);
        	}
        }
        return evt;
    }

    /**
     * Fires a transaction rollback event with a message indicating the
     * reason/type of the rollback.
     * 
     * @return The event that was fired or null if no event was fired, for
     *         testing purposes.
     */
    protected TransactionEvent fireTransactionRollback(final String message) {
    	if (!isForegroundThread()) {
    		throw new IllegalStateException("Event for a transaction rollback" + 
    				" must fired on the foreground thread.");
    	}
        synchronized (listeners) {
            if (listeners.size() == 0) return null;            
        }
        final TransactionEvent evt = TransactionEvent.createRollbackTransactionEvent(this, message);
        synchronized (listeners) {
        	for (int i = listeners.size() - 1; i >= 0; i--) {
        		listeners.get(i).transactionRollback(evt);
        	}
        }
        return evt;
    }
    
    public <T extends WabitObject> List<T> getChildren(Class<T> type) {
    	List<T> children = new ArrayList<T>();
    	for (WabitObject child : getChildren()) {
    		if (type.isAssignableFrom(child.getClass())) {
    			children.add(type.cast(child));
    		}
    	}
    	return children;
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
	        throw new IllegalArgumentException("Child object " + child.getName() + " of type " + child.getClass()
	                + " is not a child of " + getName() + " of type " + getClass());
	    
	    WabitObject topAncestor = this;
	    while (topAncestor.getParent() != null) {
	        topAncestor = topAncestor.getParent();
	    }
	    WorkspaceGraphModel graph = new WorkspaceGraphModel(topAncestor, child, true, true);
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
     * This is the object specific implementation of removeChild. There are
     * checks in the removeChild method to ensure the child being removed has no
     * dependencies and is a child of this object.
     * 
     * @see #removeChild(WabitObject)
     */
	protected abstract boolean removeChildImpl(WabitObject child);
	
	public final void addChild(WabitObject child, int index) throws IllegalArgumentException {
	    //Throws an IllegalStateException if the child is not a valid child of this class.
	    childPositionOffset(child.getClass());
	    child.setParent(this); // TODO: check that the given child doesn't already have a parent
	    addChildImpl(child, index);
	}

    /**
     * This is the object specific implementation of
     * {@link #addChild(WabitObject)}. There are checks in the
     * {@link #addChild(WabitObject)} method to ensure that the object given
     * here is a valid child type of this object.
     * <p>
     * This method should be overwritten if children are allowed.
     * 
     * @param child
     *            The child to add to this object.
     * @param index
     *            The index to add the child at.
     */
	protected void addChildImpl(WabitObject child, int index) {
		throw new UnsupportedOperationException("This WabitObject cannot have children. " +
				"This class is " + getClass() + " and trying to add " + child.getName() + 
				" of type " + child.getClass());
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
	
	public void begin(String message) {
		fireTransactionStarted(message);
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
