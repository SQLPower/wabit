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

package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;



/**
 * A simple SQL object that joins two columns together in a select
 * statement. This will also store how the two columns are being
 * compared. 
 */
public class SQLJoin {
	
	/**
	 * This property indicates a change to the join in relation to the object
	 * connected by the left part of this join. The left side is not the physical 
	 * side shown in the GUI but the object stored in the leftColumn.
	 */
	public static final String LEFT_JOIN_CHANGED = "LEFT_JOIN_CHANGED";
	
	/**
	 * This property indicates a change to the join in relation to the object
	 * connected by the right part of this join. The right side is not the physical 
	 * side shown in the GUI but the object stored in the rightColumn.
	 */
	public static final String RIGHT_JOIN_CHANGED = "RIGHT_JOIN_CHANGED";
	
	/**
	 * This property indicates a change to the Comparable relation to the object
	 * connected by the left part of this join. 
	 */
	public static final String COMPARATOR_CHANGED = "COMPARATOR_CHANGED";
	
	/**
	 * The left column of this join.
	 */
	private final Item leftColumn;

	/**
	 * The right column in the join.
	 */
	private final Item rightColumn;
	
	/**
	 * True if the left column should be an outer join. False otherwise.
	 * If this and isRightColumnOuterJoin is true then it should be a full
	 * outer join.
	 */
	private boolean isLeftColumnOuterJoin;
	
	/**
	 * True if the right column should be an outer join. False otherwise.
	 * If this and isLeftColumnOuterJoin is true then it should be a full
	 * outer join.
	 */
	private boolean isRightColumnOuterJoin;
	
	/**
	 * Listeners listening for changes to the join.
	 */
	private final List<PropertyChangeListener> joinChangeListeners;
	
	/**
	 * it is one of "<", ">", "=", "<>", ">=", "<=", "BETWEEN", "LIKE", "IN", "NOT".
	 */
	private String comparator;

	public static final String PROPERTY_JOIN_REMOVED = "JOIN_REMOVED";

	public static final String PROPERTY_JOIN_ADDED = "JOIN_ADDED";

	public SQLJoin(Item leftColumn, Item rightColumn) {
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
		this.comparator = "=";
		isLeftColumnOuterJoin = false;
		isRightColumnOuterJoin = false;
		joinChangeListeners = new ArrayList<PropertyChangeListener>();
	}

	public Item getLeftColumn() {
		return leftColumn;
	}
	
	public Item getRightColumn() {
		return rightColumn;
	}

	/**
	 * This will return the comparator between the two columns.
	 * 
	 * XXX At current this is always = but later it could be
	 * things like 'LIKE'.
	 * @return
	 */
	public String getComparator() {
		return comparator;
	}

	public boolean isLeftColumnOuterJoin() {
		return isLeftColumnOuterJoin;
	}

	public void setLeftColumnOuterJoin(boolean isLeftColumnOuterJoin) {
		if (this.isLeftColumnOuterJoin != isLeftColumnOuterJoin) {
			this.isLeftColumnOuterJoin = isLeftColumnOuterJoin;
			for (PropertyChangeListener l : joinChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(this, LEFT_JOIN_CHANGED, !this.isLeftColumnOuterJoin, this.isLeftColumnOuterJoin));
			}
		}
	}
	
	public void setComparator(String newComparator) {
		String oldComparator = comparator;
		comparator = newComparator;
		for (PropertyChangeListener l : joinChangeListeners) {
			l.propertyChange(new PropertyChangeEvent(this, COMPARATOR_CHANGED, oldComparator, newComparator));
		}
	}

	public boolean isRightColumnOuterJoin() {
		return isRightColumnOuterJoin;
	}

	public void setRightColumnOuterJoin(boolean isRightColumnOuterJoin) {
		if (this.isRightColumnOuterJoin != isRightColumnOuterJoin) {
			this.isRightColumnOuterJoin = isRightColumnOuterJoin;
			for (PropertyChangeListener l : joinChangeListeners) {
				l.propertyChange(new PropertyChangeEvent(this, RIGHT_JOIN_CHANGED, !this.isRightColumnOuterJoin, this.isRightColumnOuterJoin));
			}
		}
	}
	
	public void addJoinChangeListener(PropertyChangeListener l) {
		joinChangeListeners.add(l);
	}
	
	public void removeJoinChangeListener(PropertyChangeListener l) {
		joinChangeListeners.remove(l);
	}
	
	/**
	 * This can be called to remove all the listeners. Used for deleting
	 * a join.
	 */
	public void removeAllListeners() {
		joinChangeListeners.clear();
	}
}
