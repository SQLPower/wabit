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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.query.Query;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.olap4j.query.QueryDimension.HierarchizeMode;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wrapper class to an Olap4j Dimension. Used to load and save Olap4j Dimensions.
 */
public class WabitOlapDimension extends AbstractWabitObject {
	
	private static final Logger logger = Logger
			.getLogger(WabitOlapDimension.class);
	
	private Hierarchy hierarchy; // Can't make final, because it is set in init.
	
	private QueryDimension dimension;

	private List<WabitOlapInclusion> inclusions = new ArrayList<WabitOlapInclusion>();
	
	private List<WabitOlapExclusion> exclusions = new ArrayList<WabitOlapExclusion>();
	
	boolean initialized = false;
	
	/**
	 * Copy Constructor. Creates a deep copy of the given WabitOlapDimension and its children.
	 */
	public WabitOlapDimension(WabitOlapDimension dimension) {
		this(dimension.getName());
		
		for (WabitOlapInclusion inclusion : dimension.inclusions) {
			addInclusion(new WabitOlapInclusion(inclusion));
		}
		
		for (WabitOlapExclusion exclusion : dimension.exclusions) {
			addExclusion(exclusion);
		}
	}
	
	/**
	 * Creates a WabitOlapDimension to wrap the given {@link Dimension}.
	 */
	public WabitOlapDimension(QueryDimension dimension) {
		this.dimension = dimension;
		setName(dimension.getName());
		initialized = true;
	}
	
	/**
	 * Creates a WabitOlapDimension with the given name.  Note that
	 * this creates an uninitialized wrapper, that is, it has no wrapped class
	 * until it is initialized. Until then, any getters will return cached
	 * values.
	 */
	public WabitOlapDimension(String name) {
		setName(name);
	}

	/**
	 * Initializes the WabitOlapDimension, and finds the wrapped Dimension based
	 * on the given name. Also recursively initializes its children.
	 */
	void init(OlapQuery query, Query mdxQuery) throws QueryInitializationException {
		logger.debug("Initializing Dimension " + getName());
		
		dimension = mdxQuery.getDimension(getName());
		dimension.setHierarchizeMode(HierarchizeMode.PRE);
		
		for (WabitOlapInclusion inclusion : inclusions) {
			dimension.include(inclusion.getOperator(), query.findMember(inclusion.getUniqueMemberName()));
			inclusion.init(query);
		}
		for (WabitOlapExclusion exclusion : exclusions) {
			dimension.exclude(exclusion.getOperator(), query.findMember(exclusion.getUniqueMemberName()));
			exclusion.init(query);
		}
		if (((WabitOlapAxis) getParent()).getQueryAxis().getLocation() != Axis.FILTER) {
			hierarchy = inclusions.get(0).getSelection().getMember().getHierarchy();
		}
		initialized = true;
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
		if (child instanceof WabitOlapSelection) {
			if (inclusions.contains(child)) {
				int index = inclusions.indexOf(child);
				dimension.getInclusions().remove(((WabitOlapInclusion) child).getSelection());
				fireTransactionStarted("Removing Child");
				inclusions.remove(child);
				fireTransactionEnded();
				fireChildRemoved(WabitOlapInclusion.class, child, index);
				return true;
			}
			if (exclusions.contains(child)) {
				int index = exclusions.indexOf(child);
				dimension.getExclusions().remove(((WabitOlapExclusion) child).getSelection());
				fireTransactionStarted("Removing Child");
				exclusions.remove(child);
				fireTransactionEnded();
				fireChildRemoved(WabitOlapExclusion.class, child, index);
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates lists of children based on children of the wrapped Dimension.
	 * Calling this is the only way to make sure this wrapper is synchronized
	 * with the wrapped Dimension, and should be called any time something
	 * modifies the query's selections.
	 */
	public void updateChildren(){
		if (!initialized) return;
		fireTransactionStarted("Updating Children");
		updateInclusions();
		updateExclusions();
		fireTransactionEnded();
	}
	
	/**
	 * Updates the list of inclusions based on children of the wrapped Dimension.
	 */
	private void updateInclusions() {
		List<Selection> olapInclusions = new ArrayList<Selection>(dimension.getInclusions());
		Iterator<WabitOlapInclusion> wabitInclusions = inclusions.iterator();
		for (int index = 0; wabitInclusions.hasNext(); index++) {
			WabitOlapInclusion inclusion = wabitInclusions.next();
			if (!olapInclusions.contains(inclusion.getSelection())) {
				wabitInclusions.remove();
				fireChildRemoved(WabitOlapInclusion.class, inclusion, index);
			} else {
				olapInclusions.remove(inclusion.getSelection());
			}
		}
		
		Iterator<Selection> inclusions = olapInclusions.iterator();
		while (inclusions.hasNext()) {
			addInclusion(new WabitOlapInclusion(inclusions.next()));
		}
	}
	
	/**
	 * Updates the list of exclusions based on children of the wrapped Dimension.
	 */
	private void updateExclusions() {
		List<Selection> olapExclusions = new ArrayList<Selection>(dimension.getExclusions());
		Iterator<WabitOlapExclusion> wabitExclusions = exclusions.iterator();
		for (int index = 0; wabitExclusions.hasNext(); index++) {
			WabitOlapExclusion exclusion = wabitExclusions.next();
			if (!olapExclusions.contains(exclusion.getSelection())) {
				wabitExclusions.remove();
				fireChildRemoved(WabitOlapInclusion.class, exclusion, index);
			} else {
				olapExclusions.remove(exclusion.getSelection());
			}
		}
		
		Iterator<Selection> exclusions = olapExclusions.iterator();
		while (exclusions.hasNext()) {
			addExclusion(new WabitOlapExclusion(exclusions.next()));
		}
	}
	
	/**
	 * Adds an inclusion to this dimension. Note that this will not affect the
	 * wrapped {@link Dimension}.
	 */
	public void addInclusion(WabitOlapInclusion inclusion) {
		inclusions.add(inclusion);
		inclusion.setParent(this);
		fireChildAdded(WabitOlapInclusion.class, inclusion,
				inclusions.size() - 1);
	}

	/**
	 * Adds an exclusion to this dimension. Note that this will not affect the
	 * wrapped {@link Dimension}.
	 */
	public void addExclusion(WabitOlapExclusion exclusion) {
		exclusions.add(exclusion);
		exclusion.setParent(this);
		fireChildAdded(WabitOlapExclusion.class, exclusion, exclusions.size() - 1);
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		if (childType.equals(WabitOlapInclusion.class)) {
			return 0;
		} else if (childType.equals(WabitOlapExclusion.class)){
			return inclusions.size();
		} else {
			throw new IllegalArgumentException("WabitOlapDimension has no children of type " + childType);
		}
	}

	public List<WabitOlapSelection> getChildren() {
		List<WabitOlapSelection> allChildren = new ArrayList<WabitOlapSelection>();
		allChildren.addAll(inclusions);
		allChildren.addAll(exclusions);
		return allChildren;
	}
	
	/**
	 * Returns the list of inclusions.
	 */
	public List<WabitOlapInclusion> getInclusions() {
		return Collections.unmodifiableList(inclusions);
	}
	
	/**
	 * Returns the list of exclusions. 
	 */
	public List<WabitOlapExclusion> getExclusions() {
		return Collections.unmodifiableList(exclusions);
	}

	/**
	 * Olap wrapper classes only depend on the wrapped Olap4j objects.
	 */
	@SuppressWarnings("unchecked")
	public List<WabitObject> getDependencies() {
		return Collections.EMPTY_LIST;
	}

	public void removeDependency(WabitObject dependency) {
		//no-op
	}
	
	@Override
	public String getName() {
		if (initialized) {
			return dimension.getName();
		} else {
			return super.getName();
		}
	}
	
	/**
	 * Returns the Dimension wrapped by this object. This method is package
	 * private to avoid leaking the Olap4j object wrapped inside, and to allow
	 * other OLAP specific classes access.
	 */
	QueryDimension getDimension() {
		return dimension;
	}

	Hierarchy getHierarchy() {
		return hierarchy;
	}

}
