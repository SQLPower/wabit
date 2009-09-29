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

package ca.sqlpower.wabit.olap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olap4j.Axis;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.SortOrder;

import com.rc.retroweaver.runtime.Collections;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wrapper class for an Olap4j Axis. Keeps important properties
 */
public class WabitOlapAxis extends AbstractWabitObject {

	private QueryAxis queryAxis;
	
	private List<WabitOlapDimension> dimensions = new ArrayList<WabitOlapDimension>();
	
	private int ordinal;
	
	private boolean nonEmpty;
	
	private String sortOrder;
	
	private String sortEvalLiteral;
	
	private boolean initialized = false;

	public WabitOlapAxis(WabitOlapAxis axis) {
		this(axis.ordinal);
		setNonEmpty(axis.nonEmpty);
		setSortOrder(axis.sortOrder);
		setSortEvaluationLiteral(axis.sortEvalLiteral);
		
		for (WabitOlapDimension dimension : axis.dimensions) {
			addDimension(new WabitOlapDimension(dimension));
		}
	}
	
	public WabitOlapAxis(QueryAxis queryAxis) {
		this.queryAxis = queryAxis;
		updateChildren();
	}
	
	public WabitOlapAxis(int ordinal) {
		this.setOrdinal(ordinal);
	}
	
	void init(OlapQuery query, Query mdxQuery) throws QueryInitializationException {
		Axis axis = Axis.Factory.forOrdinal(getOrdinal());
		queryAxis = mdxQuery.getAxes().get(axis);
		setNonEmpty(nonEmpty);
		
		if (sortOrder != null) {
			SortOrder order = SortOrder.valueOf(sortOrder);
			String sortEvaluationLiteral = sortEvalLiteral;
			queryAxis.sort(order, sortEvaluationLiteral);
		}
		
		for (WabitOlapDimension dimension : dimensions) {
			dimension.init(query, mdxQuery);
			queryAxis.addDimension(dimension.getDimension());
		}
		
		initialized = true;
	}
	
	public void updateChildren(){
		List<QueryDimension> olapDimensions = new ArrayList<QueryDimension>(queryAxis.getDimensions());
		Iterator<WabitOlapDimension> wabitDimensions = dimensions.iterator();
		for (int index = 0; wabitDimensions.hasNext(); index++) {
			WabitOlapDimension dimension = wabitDimensions.next();
			if (!olapDimensions.contains(dimension.getDimension())) {
				wabitDimensions.remove();
				fireChildRemoved(WabitOlapDimension.class, dimension, index);
			} else {
				olapDimensions.remove(dimension.getDimension());
				dimension.updateChildren();
			}
		}
		
		Iterator<QueryDimension> queryDimensions = olapDimensions.iterator();
		while (queryDimensions.hasNext()) {
			addDimension(new WabitOlapDimension(queryDimensions.next()));
		}
	}
	
	public void addDimension(WabitOlapDimension dimension) {
		dimensions.add(dimension);
		dimension.setParent(this);
		fireChildAdded(WabitOlapDimension.class, dimension, dimensions.size() - 1);
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
		dimensions.remove(child);
		return true;
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		if (childType == WabitOlapDimension.class){
			return 0;
		} else {
			throw new IllegalArgumentException("Objects of this type don't have children of type " + childType);
		}
	}

	public List<? extends WabitObject> getChildren() {
		return dimensions;
	}

	public List<WabitObject> getDependencies() {
		return null;
	}

	public void removeDependency(WabitObject dependency) {
		//no-op
	}
	
	public List<WabitOlapDimension> getDimensions() {
		return Collections.unmodifiableList(dimensions);
	}
	
	public void setNonEmpty(boolean nonEmpty) {
		boolean oldValue = this.nonEmpty;
		this.nonEmpty = nonEmpty;
		initialized = false;
		firePropertyChange("non-empy", oldValue, nonEmpty);
	}
	
	public boolean isNonEmpty() {
		if (initialized) {
			return queryAxis.isNonEmpty();
		} else {
			return nonEmpty;
		}
	}

	public void setSortOrder(String sortOrder) {
		String oldValue = this.sortOrder;
		this.sortOrder = sortOrder;
		initialized = false;
		firePropertyChange("sort-order", oldValue, sortOrder);
	}

	public String getSortOrder() {
		if (initialized) {
			return queryAxis.getSortOrder().name();
		} else {
			return sortOrder;
		}
	}

	public void setSortEvaluationLiteral(String sortEvalLiteral) {
		String oldValue = this.sortEvalLiteral;
		this.sortEvalLiteral = sortEvalLiteral;
		initialized = false;
		firePropertyChange("sort-evaluation-literal", oldValue, sortEvalLiteral);
	}

	public String getSortEvaluationLiteral() {
		if (initialized) {
			return queryAxis.getSortIdentifierNodeName();
		} else {
			return sortEvalLiteral;
		}
	}
	
	public void setOrdinal(int ordinal) {
		int oldValue = ordinal;
		this.ordinal = ordinal;
		initialized = false;
		firePropertyChange("ordinal", oldValue, ordinal);
	}

	public int getOrdinal() {
		if (initialized) {
			return queryAxis.getLocation().axisOrdinal();
		} else {
			return ordinal;
		}
	}

	QueryAxis getQueryAxis() {
		return queryAxis;
	}
}
