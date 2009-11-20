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

package ca.sqlpower.wabit.rs.olap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.SortOrder;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wrapper class to an Olap4j Dimension. Used to load and save Olap4j Dimensions.
 */
public class WabitOlapAxis extends AbstractWabitObject {

	private static final Logger logger = Logger
			.getLogger(WabitOlapAxis.class);
	
	private QueryAxis queryAxis;
	
	private List<WabitOlapDimension> dimensions = new ArrayList<WabitOlapDimension>();
	
	private final Axis ordinal;
	
	private boolean nonEmpty;
	
	private String sortOrder;
	
	private String sortEvaluationLiteral;
	
	private boolean initialized = false;

	/**
	 * Copy Constructor. Creates a deep copy of the given WabitOlapAxis and its children.
	 */
	public WabitOlapAxis(WabitOlapAxis axis) {
		this(axis.ordinal);
		setNonEmpty(axis.nonEmpty);
		setSortOrder(axis.sortOrder);
		setSortEvaluationLiteral(axis.sortEvaluationLiteral);
		
		for (WabitOlapDimension dimension : axis.dimensions) {
			addDimension(new WabitOlapDimension(dimension));
		}
	}
	
	/**
	 * Creates a WabitOlapAxis to wrap the given {@link QueryAxis}.
	 */
	public WabitOlapAxis(QueryAxis queryAxis) {
		initialized = true;
		this.queryAxis = queryAxis;
		ordinal = getOrdinal();
		setName(ordinal.name());
		updateChildren();
	}
	
	/**
	 * Creates a WabitOlapDimension with the given name.  Note that
	 * this creates an uninitialized wrapper, that is, it has no wrapped class
	 * until it is initialized. Until then, any getters will return cached
	 * values.
	 */
	public WabitOlapAxis(Axis ordinal) {
		this.ordinal = ordinal;
		firePropertyChange("ordinal", null, ordinal);
		setName("unnamed Axis");
	}
	
	/**
	 * Initializes the WabitOlapAxis, and finds the wrapped Axis based
	 * on the given ordinal. Also recursively initializes its children.
	 */
	void init(OlapQuery query, Query mdxQuery) throws QueryInitializationException {
		if (!initialized) {
			logger.debug("Initializing Axis " + getName());

			queryAxis = mdxQuery.getAxes().get(ordinal);
			queryAxis.setNonEmpty(nonEmpty);

			if (sortOrder != null) {
				SortOrder order = SortOrder.valueOf(sortOrder);
				queryAxis.sort(order, sortEvaluationLiteral);
			}

			logger.debug("QueryAxis is " + queryAxis);

			initialized = true;
		}
		
		for (WabitOlapDimension dimension : dimensions) {
			dimension.init(query, mdxQuery);
			queryAxis.addDimension(dimension.getDimension());
		}
	}
	
	/**
	 * Updates lists of children based on children of the wrapped Dimension.
	 * Calling this is the only way to make sure this wrapper is synchronized
	 * with the wrapped Dimension, and should be called any time something
	 * modifies the query's selections.
	 */
	public void updateChildren(){
		if (!initialized) return;
		logger.debug("Updating children of Axis " + getName() + ". QueryAxis is " + queryAxis);
		
		fireTransactionStarted("Updating Children");
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
			WabitOlapDimension dimension = new WabitOlapDimension(queryDimensions.next());
			dimension.setParent(this);
			addDimension(dimension);
			dimension.updateChildren();
		}
		
		if (sortOrder != null) {
			SortOrder order = SortOrder.valueOf(sortOrder);
			queryAxis.sort(order, sortEvaluationLiteral);
		}
		fireTransactionEnded();
	}
	
	/**
	 * Adds an exclusion to this axis. Note that this will not affect the
	 * wrapped {@link QueryAxis}.
	 */
	public void addDimension(WabitOlapDimension dimension) {
	    addDimension(dimension, dimensions.size());
	}

    /**
     * Adds an exclusion to this axis at the given dimension. Note that this
     * will not affect the wrapped {@link QueryAxis}.
     */
	public void addDimension(WabitOlapDimension dimension, int index) {
		logger.debug("Adding dimension " + dimension.getName() + " to Axis " + getName() + " at " + ordinal);
		
		dimensions.add(index, dimension);
		dimension.setParent(this);
		fireChildAdded(WabitOlapDimension.class, dimension, index);
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
	    if (initialized) {
	        WabitOlapDimension dimension = (WabitOlapDimension) child;
	        
	        if (!queryAxis.getDimensions().contains(dimension.getDimension())) return false;
	        
	        try {
	            fireTransactionStarted("Removing " + child.getName() + " from axis " + getName());
                getParent().removeDimension(dimension.getDimension().getDimension(), 
                        queryAxis.getLocation());
                fireTransactionEnded();
                return true;
            } catch (QueryInitializationException e) {
                fireTransactionRollback(e.getMessage());
                throw new IllegalStateException("The query's axis " + ordinal + " has been initialized " +
                		"but the query just had an error during initialization.", e);
            } catch (RuntimeException e) {
                fireTransactionRollback(e.getMessage());
                throw e;
            }
	    } else {
	        int index = dimensions.indexOf(child);
	        boolean success = dimensions.remove(child);
	        if (success) {
	            fireChildRemoved(WabitOlapDimension.class, child, index);
	        }
	        return success;
	    }
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
	    WabitOlapDimension dimension = (WabitOlapDimension) child;
	    if (initialized) {
	        try {
	            fireTransactionStarted("Adding " + child.getName() + " to axis " + 
	                    getName() + " at index " + index);
	            addDimension(dimension, index);
                getParent().addDimensionToAxis(index, ordinal, dimension.getDimension());
                fireTransactionEnded();
            } catch (QueryInitializationException e) {
                fireTransactionRollback(e.getMessage());
                throw new IllegalStateException("The axis " + ordinal + " has been initialized " +
                		"but the query just had an error during initialization.", e);
            } catch (RuntimeException e) {
                fireTransactionRollback(e.getMessage());
                throw e;
            }
	    } else {
	        addDimension(dimension, index);
	    }
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		if (childType == WabitOlapDimension.class){
			return 0;
		} else {
			throw new IllegalArgumentException("A WabitOlapAxis doesn't have children of type " + childType);
		}
	}

	public List<? extends WabitObject> getChildren() {
		return dimensions;
	}

	/**
	 * Olap wrapper classes only depend on the wrapped Olap4j objects
	 */
	@SuppressWarnings("unchecked")
	public List<WabitObject> getDependencies() {
		return Collections.EMPTY_LIST;
	}

	public void removeDependency(SPObject dependency) {
		//no-op
	}
	
	/**
	 * Returns the list of dimensions 
	 */
	public List<WabitOlapDimension> getDimensions() {
		return Collections.unmodifiableList(dimensions);
	}
	
	public void setNonEmpty(boolean nonEmpty) {
		boolean oldValue = this.nonEmpty;
		this.nonEmpty = nonEmpty;
		initialized = false;
		firePropertyChange("nonEmpty", oldValue, nonEmpty);
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
		firePropertyChange("sortOrder", oldValue, sortOrder);
	}

	public String getSortOrder() {
		if (initialized && queryAxis.getSortOrder() != null) {
			return queryAxis.getSortOrder().name();
		} else {
			return sortOrder;
		}
	}

	public void setSortEvaluationLiteral(String sortEvaluationLiteral) {
		String oldValue = this.sortEvaluationLiteral;
		this.sortEvaluationLiteral = sortEvaluationLiteral;
		initialized = false;
		firePropertyChange("sortEvaluationLiteral", oldValue, sortEvaluationLiteral);
	}

	public String getSortEvaluationLiteral() {
		if (initialized) {
			return queryAxis.getSortIdentifierNodeName();
		} else {
			return sortEvaluationLiteral;
		}
	}
	
	public Axis getOrdinal() {
		logger.debug("Query Axis is " + queryAxis);
		if (initialized) {
			return queryAxis.getLocation();
		} else {
			return ordinal;
		}
	}

	/**
	 * Returns the QueryAxis wrapped by this object. This method is package
	 * private to avoid leaking the Olap4j object wrapped inside, and to allow
	 * other OLAP specific classes access.
	 */
	QueryAxis getQueryAxis() {
		return queryAxis;
	}
	
	@Override
	public OlapQuery getParent() {
	    return (OlapQuery) super.getParent();
	}
}
