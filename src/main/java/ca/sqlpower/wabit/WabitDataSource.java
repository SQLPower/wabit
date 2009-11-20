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
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.SPDataSource;

/**
 * An implementation of {@link WabitObject} that wraps a data sources.
 * This data source can be any implementation of {@link SPDataSource}.
 */
public class WabitDataSource extends AbstractWabitObject {

    private static final String UUID_KEY_NAME = "ca.sqlpower.wabit.WabitDataSource.UUID";
    
	/**
	 * Underlying {@link SPDataSource} object that actually contains all the
	 * database connection info.
	 */
	private final SPDataSource dataSource;

	/**
	 * Keeps this wrapper's name in sync with the data source it's wrapping.
	 */
	private final PropertyChangeListener dataSourceChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("name")) {
                setName(dataSource.getName());
            }
        }
    };

    /**
     * Creates a WabitDataSource wrapper for the given data source. The newly
     * created WabitDataSource's name tracks changes to the underlying data
     * source's name.
     * <p>
     * It is vitally important to call {@link #cleanup()} on a WabitDataSource
     * when you are done with it. If you attach the WabitDataSource to a
     * workspace, cleanup will be done automatically the session is closed, but
     * if the object you create does not get attached to a session, cleanup is
     * your own responsibility.
     * 
     * @param ds The data source to wrap. Must not be null.
     */
	public WabitDataSource(@Nonnull SPDataSource ds) {
	    this.dataSource = ds;
	    setName(ds.getName());
	    
	    String uuid = dataSource.get(UUID_KEY_NAME);
	    if (uuid == null){
	        generateNewUUID();
	        uuid = super.getUUID();
	        dataSource.put(UUID_KEY_NAME, uuid);
	    }
	    
	    ds.addPropertyChangeListener(dataSourceChangeListener);
	}

	@Override
	public CleanupExceptions cleanup() {
	    dataSource.removePropertyChangeListener(dataSourceChangeListener);
	    return super.cleanup();
	}
	
    /**
     * Gets the UUID associated with this instance's data source. This class
     * does not store its own UUID, as in implementation, these instances are
     * instantiated and removed often, but the data source they are wrapping
     * stays constant.
     * 
     * @return The UUID, a unique identifying string associated with this instance's
     *         data source.
     */
	@Override
	public String getUUID(){
	    String uuid = dataSource.get(UUID_KEY_NAME);
	    return uuid;
	}
	
	/**
	 * Sets the UUID associated with this instance's data source. This class
     * does not store its own UUID, as in implementation, these instances are
     * instantiated and removed often, but the data source they are wrapping
     * stays constant.
     * 
     * @param  The UUID, a unique identifying string associated with this instance's
     *         data source.
	 */
	public void setUUID(String uuid){
	    String oldUUID = dataSource.get(UUID_KEY_NAME);
	    dataSource.put(UUID_KEY_NAME, uuid);
	    firePropertyChange("UUID", oldUUID, uuid);
	}
	
	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		throw new UnsupportedOperationException("This object doesn't have children at all");
	}

	public List<WabitObject> getChildren() {
		return Collections.emptyList();
	}
	
	public SPDataSource getSPDataSource() {
		return dataSource;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WabitDataSource) {
			return this.dataSource.equals(((WabitDataSource) obj).getSPDataSource());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.dataSource.hashCode();
	}
	
	public void removeDependency(WabitObject dependency) {
	    //do nothing
	}

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

	public void removeDependency(SPObject dependency) {
		//no-op
	}
}
