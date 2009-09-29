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
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.query.Query;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

import com.rc.retroweaver.runtime.Collections;

public class WabitOlapDimension extends AbstractWabitObject {
	
	private Hierarchy hierarchy;
	
	private QueryDimension dimension;

	private List<WabitOlapInclusion> inclusions = new ArrayList<WabitOlapInclusion>();
	
	private List<WabitOlapExclusion> exclusions = new ArrayList<WabitOlapExclusion>();
	
	private String name;
	
	boolean initialized = false;
	
	public WabitOlapDimension(WabitOlapDimension dimension) {
		this(dimension.name);
		
		for (WabitOlapInclusion inclusion : dimension.inclusions) {
			addInclusion(new WabitOlapInclusion(inclusion));
		}
		
		for (WabitOlapExclusion exclusion : dimension.exclusions) {
			addExclusion(exclusion);
		}
	}
	
	public WabitOlapDimension(QueryDimension dimension) {
		this.dimension = dimension;
		updateChildren();
	}
	
	public WabitOlapDimension(String name) {
		this.name = name;
		
	}

	void init(OlapQuery query, Query mdxQuery) throws QueryInitializationException {
		dimension = mdxQuery.getDimension(name);
		
		for (WabitOlapInclusion selection : inclusions) {
			selection.init(query);
			dimension.include(selection.getSelection().getOperator(), selection.getSelection().getMember());
		}
		for (WabitOlapInclusion exclusion : exclusions) {
			exclusion.init(query);
			dimension.exclude(exclusion.getSelection().getOperator(), (Member) exclusion.getSelection());
		}
		if (((WabitOlapAxis) getParent()).getQueryAxis().getLocation() != Axis.FILTER) {
			hierarchy = ((Member) inclusions.get(0)).getHierarchy();
		}
		initialized = true;
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
		if (child instanceof WabitOlapInclusion) {
			if (inclusions.contains(child)) {
				int index = inclusions.indexOf(child);
				fireChildRemoved(WabitOlapInclusion.class, child, index);
				return true;
			}
			if (exclusions.contains(child)) {
				int index = exclusions.indexOf(child) + inclusions.size();
				fireChildRemoved(WabitOlapInclusion.class, child, index);
				return true;
			}
		}
		return false;
	}
	
	public void updateChildren(){
		updateInclusions();
		updateExclusions();
	}
	
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
	
	private void updateExclusions() {
		List<Selection> olapExclusions = new ArrayList<Selection>(dimension.getExclusions());
		Iterator<WabitOlapExclusion> wabitExclusions = exclusions.iterator();
		for (int index = 0; wabitExclusions.hasNext(); index++) {
			WabitOlapInclusion exclusion = wabitExclusions.next();
			if (!olapExclusions.contains(exclusion.getSelection())) {
				wabitExclusions.remove();
				fireChildRemoved(WabitOlapInclusion.class, exclusion, index);
			} else {
				olapExclusions.remove(exclusion.getSelection());
			}
		}
		
		Iterator<Selection> exclusions = olapExclusions.iterator();
		while (exclusions.hasNext()) {
			addInclusion(new WabitOlapInclusion(exclusions.next()));
		}
	}
	
	public void addInclusion(WabitOlapInclusion inclusion) {
		inclusions.add(inclusion);
		inclusion.setParent(this);
		fireChildAdded(WabitOlapInclusion.class, inclusion, inclusions.size() - 1);
	}
	
	public void addExclusion(WabitOlapExclusion exclusion) {
		exclusions.add(exclusion);
		exclusion.setParent(this);
		fireChildAdded(WabitOlapExclusion.class, exclusion, exclusions.size() - 1);
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		List<WabitOlapInclusion> allChildren = new ArrayList<WabitOlapInclusion>();
		allChildren.addAll(inclusions);
		allChildren.addAll(exclusions);
		return allChildren;
	}
	
	@SuppressWarnings("unchecked")
	public List<WabitOlapInclusion> getInclusions() {
		return Collections.unmodifiableList(inclusions);
	}
	
	@SuppressWarnings("unchecked")
	public List<WabitOlapExclusion> getExclusions() {
		return Collections.unmodifiableList(exclusions);
	}

	public List<WabitObject> getDependencies() {
		return null;
	}

	public void removeDependency(WabitObject dependency) {
		//no-op
	}
	
	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		initialized = false;
		firePropertyChange("dimension-name", oldValue, name);
	}
	
	public String getName() {
		if (initialized) {
			return dimension.getName();
		} else {
			return name;
		}
	}
	
	QueryDimension getDimension() {
		return dimension;
	}

	void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	Hierarchy getHierarchy() {
		return hierarchy;
	}

}
