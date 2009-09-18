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
import java.util.UUID;

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
	private SPDataSource dataSource;

	public WabitDataSource(SPDataSource ds) {
	    this.dataSource = ds;
	    setName(ds.getName());
	    ds.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("name")) {
					setName(dataSource.getName());
				}
			}
		});
	    // TODO listen for changes in DS and rebroadcast the appropriate ones
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
	    if (uuid == null){
	        generateNewUUID();
	        uuid = super.getUUID();
	        dataSource.put(UUID_KEY_NAME, uuid);
	    }
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
	    dataSource.put(UUID_KEY_NAME, uuid);
	}
	
	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
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
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }
}
