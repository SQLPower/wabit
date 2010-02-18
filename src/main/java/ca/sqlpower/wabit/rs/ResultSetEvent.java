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

package ca.sqlpower.wabit.rs;

import java.sql.ResultSet;

import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;
import ca.sqlpower.sql.CachedRowSet;

/**
 * Event object that describes the outcome of a result set producer execution.
 */
@Immutable
public class ResultSetEvent {
	
	public enum EventType {
		NEW_DATA,
		EXECUTION_COMPLETED
	}

    private final ResultSetHandle source;
	private final EventType eventType;
	private final Object[] row;
	private final int rowNumber;

    public static ResultSetEvent getNewDataEvent(
    		ResultSetHandle source,
            Object[] row,
            int rowNumber) 
    {
    	return new ResultSetEvent(EventType.NEW_DATA, source, row, rowNumber);
    }
    
    public static ResultSetEvent getExecutionCompleteEvent(ResultSetHandle source) 
    {
    	return new ResultSetEvent(EventType.NEW_DATA, source, null, 0);
    }
    
    ResultSetEvent(
    		EventType eventType,
            ResultSetHandle source,
            Object[] row,
            int rowNumber) 
    {
		this.eventType = eventType;
        this.source = source;
        this.row = row;
        this.rowNumber = rowNumber;
    }

    /**
     * Returns a copy of the collection of result sets. The copy has a
     * collection of independent row cursor and other state (such as wasNull())
     * but shares the actual underlying data with (at least) all other invokers
     * of this method.
     * 
     * @return A copy containing mostly-independent copies of the new result
     *         sets, or null if an execution was attempted while the result set
     *         producer was not in a state where it could produce a result set.
     * @see CachedRowSet#createShared()
     */
    @Nullable
    public ResultSet getResults() {
        return this.source.getResultSet();
    }
    
    /**
     * Pagkage protected accessor
     */
    public ResultSetHandle getSourceHandle() {
    	return this.source;
    }
    
    /**
     * Returns the type of this event.
     * @return One value of the {@link EventType} enumeration.
     */
    public EventType getEventType() {
		return eventType;
	}
    
    public Object[] getRow() {
		return row;
	}

	public int getRowNumber() {
		return rowNumber;
	}
}
