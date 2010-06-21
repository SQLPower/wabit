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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.query.Query;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.olap4j.query.QueryDimension.HierarchizeMode;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Wrapper class to an Olap4j Dimension. Used to load and save Olap4j Dimensions.
 */
public class WabitOlapDimension extends AbstractWabitObject {
	
	private static final Logger logger = Logger
			.getLogger(WabitOlapDimension.class);

    /**
     * The hierarchy used by this dimension in the query.
     * <p> 
     * XXX This is currently null if the query was not initialized because it 
     * was created instead of loaded.
     */
	private Hierarchy hierarchy; // Can't make final, because it is set in init.
	
	private QueryDimension dimension;

	private List<WabitOlapInclusion> inclusions = new ArrayList<WabitOlapInclusion>();
	
	private List<WabitOlapExclusion> exclusions = new ArrayList<WabitOlapExclusion>();
	
	boolean initialized = false;

	/**
	 * FIXME This enum defines the {@link SPObject} child classes a
	 * {@link WabitOlapDimension} takes as well as the ordinal order of these
	 * child classes such that the class going before does not depend on the
	 * class that goes after. This is here temporarily, see bug 2327 for future
	 * enhancements. http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=2327
	 */
	public enum SPObjectOrder {
		WABIT_OLAP_INCLUSION(WabitOlapInclusion.class),
		WABIT_OLAP_EXCLUSION(WabitOlapExclusion.class);
		
		/**
		 * @see #getSuperChildClass()
		 */
		private final Class<? extends SPObject> superChildClass;
		
		/**
		 * @see #getChildClasses()
		 */
		private final Set<Class<? extends SPObject>> classes;

		/**
		 * Creates a new {@link SPObjectOrder},
		 * 
		 * @param superChildClass
		 *            The highest {@link SPObject} class that the
		 *            {@link SPObject#childPositionOffset(Class)} method looks
		 *            at to determine the index.
		 * @param classes
		 *            The list of child {@link SPObject} class varargs which
		 *            share the same ordering in the list of children. These
		 *            classes must be extending/implementing
		 *            {@link #superChildClass}.
		 */
		private SPObjectOrder(Class<? extends SPObject> superChildClass, Class<? extends SPObject>... classes) {
			this.superChildClass = superChildClass;
			this.classes = new HashSet<Class<? extends SPObject>>(Arrays.asList(classes));
		}

		/**
		 * Returns the highest {@link SPObject} class that the
		 * {@link SPObject#childPositionOffset(Class)} method looks at to
		 * determine the index.
		 */
		public Class<? extends SPObject> getSuperChildClass() {
			return superChildClass;
		}

		/**
		 * Returns the {@link Set} of {@link SPObject} classes that share the
		 * same ordering in the list of children. These classes must either
		 * extend/implement from the same class type given by
		 * {@link SPObjectOrder#getSuperChildClass()}.
		 */
		public Set<Class<? extends SPObject>> getChildClasses() {
			return Collections.unmodifiableSet(classes);
		}
		
		public static SPObjectOrder getOrderBySimpleClassName(String name) {
			for (SPObjectOrder order : values()) {
				if (order.getSuperChildClass().getSimpleName().equals(name)) {
					return order;
				} else {
					for (Class<? extends SPObject> childClass : order.getChildClasses()) {
						if (childClass.getSimpleName().equals(name)) {
							return order;
						}
					}
				}
			}
			throw new IllegalArgumentException("The " + SPObject.class.getSimpleName() + 
					" class \"" + name + "\" does not exist or is not a child type " +
							"of " + WabitOlapDimension.class.getSimpleName() + ".");
		}
		
	}
	
	/**
	 * Copy Constructor. Creates a deep copy of the given WabitOlapDimension and its children.
	 */
	public WabitOlapDimension(WabitOlapDimension dimension) {
		this(dimension.getName());
		
		for (WabitOlapInclusion inclusion : dimension.inclusions) {
			addInclusion(new WabitOlapInclusion(inclusion));
		}
		
		for (WabitOlapExclusion exclusion : dimension.exclusions) {
			addExclusion(new WabitOlapExclusion(exclusion));
		}
	}
	
	/**
	 * Creates a WabitOlapDimension to wrap the given {@link QueryDimension}.
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
		//XXX what if there are no inclusions? possibly occurrs when a query is saved
		//with an empty axis?
		if (((WabitOlapAxis) getParent()).getQueryAxis().getLocation() != Axis.FILTER) {
			hierarchy = inclusions.get(0).getSelection().getMember().getHierarchy();
		}
		initialized = true;
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
	    if (initialized) {
	        final OlapQuery query = getParent().getParent();
	        if (child instanceof WabitOlapInclusion) {
	            WabitOlapInclusion inclusion = (WabitOlapInclusion) child;
	            try {
	            	fireTransactionStarted("Including member " + child.getName() + " on " + getName());
	            	if (inclusion.isInitialized()) {
	            		query.addToAxis(0, inclusion.getSelection().getMember(), 
	            				inclusion.getOperator(), getParent().getOrdinal());
	            	} else {
	            		query.addToAxis(0, query.findMember(inclusion.getUniqueMemberName()), inclusion.getOperator(), getParent().getOrdinal());
	        			inclusion.init(query);
	            	}
                    fireTransactionEnded();
                } catch (OlapException e) {
                    fireTransactionRollback(e.getMessage());
                    throw new RuntimeException(e);
                } catch (QueryInitializationException e) {
                    fireTransactionRollback(e.getMessage());
                    throw new IllegalStateException("The dimension " + getName() + 
                            " was initialized but the parent query " + query.getName() + 
                            " was not initialized");
                }
	        } else if (child instanceof WabitOlapExclusion) {
	            WabitOlapExclusion exclusion = (WabitOlapExclusion) child;
	            try {
	                fireTransactionStarted("Excluding member " + child.getName() + " on " + getName());
	                if (exclusion.isInitialized()) {
	                	query.excludeMember(getDimension().getName(), exclusion.getSelection().getMember(), 
	                			exclusion.getOperator());
	                } else {
	                	query.excludeMember(getDimension().getName(), query.findMember(exclusion.getUniqueMemberName()), exclusion.getOperator());
	        			exclusion.init(query);
	                }
                    fireTransactionEnded();
                } catch (QueryInitializationException e) {
                    fireTransactionRollback(e.getMessage());
                    throw new IllegalStateException("The dimension " + getName() + 
                            " was initialized but the parent query " + query.getName() + 
                            " was not");
                }
	        }
	    } else {
	        if (child instanceof WabitOlapInclusion) {
	            WabitOlapInclusion inclusion = (WabitOlapInclusion) child;
	            inclusions.add(index, inclusion);
	            fireChildAdded(inclusion.getClass(), inclusion, index);
	        } else if (child instanceof WabitOlapExclusion) {
	            WabitOlapExclusion exclusion = (WabitOlapExclusion) child;
                exclusions.add(index, exclusion);
                fireChildAdded(exclusion.getClass(), exclusion, index);
	        }
	    }
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
	    if (initialized) {
	        if (child instanceof WabitOlapInclusion) {
	            WabitOlapInclusion inclusion = (WabitOlapInclusion) child;
	            final OlapQuery query = getParent().getParent();
	            try {
	                fireTransactionStarted("Removing " + child.getName() + " from inclusions on " + getName());
                    boolean success = query.removeIncludedMember(inclusion.getSelection().getMember(), 
                            inclusion.getOperator());
                    fireTransactionEnded();
                    return success;
                } catch (QueryInitializationException e) {
                    fireTransactionRollback(e.getMessage());
                    throw new IllegalStateException("The dimension " + getName() + " has been initialized " +
                    		"but the query " + query.getName() + " was still initializing.", e);
                } catch (RuntimeException e) {
                    fireTransactionRollback(e.getMessage());
                    throw e;
                }
	        } else if (child instanceof WabitOlapExclusion) {
	            WabitOlapExclusion exclusion = (WabitOlapExclusion) child;
                final OlapQuery query = getParent().getParent();
                try {
                    fireTransactionStarted("Removing " + child.getName() + " from exclusions on " + getName());
                    boolean success = query.removeExcludedMember(exclusion.getSelection().getMember(), 
                            exclusion.getOperator());
                    fireTransactionEnded();
                    return success;
                } catch (QueryInitializationException e) {
                    fireTransactionRollback(e.getMessage());
                    throw new IllegalStateException("The dimension " + getName() + " has been initialized " +
                            "but the query " + query.getName() + " was still initializing.", e);
                } catch (RuntimeException e) {
                    fireTransactionRollback(e.getMessage());
                    throw e;
                }
	        } else {
	            throw new AssertionError("The child " + child.getName() + " of type " + 
	                    child.getClass() + " is not a valid child for " + WabitOlapDimension.class + 
	                    " and should have been caught sooner.");
	        }
	    } else {
			if (inclusions.contains(child)) {
				int index = inclusions.indexOf(child);
				inclusions.remove(child);
				fireChildRemoved(WabitOlapInclusion.class, child, index);
				return true;
			} else if (exclusions.contains(child)) {
				int index = exclusions.indexOf(child);
				exclusions.remove(child);
				fireChildRemoved(WabitOlapExclusion.class, child, index);
				return true;
			} else {
			    return false;
			}
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
	 * wrapped {@link QueryDimension}.
	 */
	public void addInclusion(WabitOlapInclusion inclusion) {
		inclusions.add(inclusion);
		inclusion.setParent(this);
		fireChildAdded(WabitOlapInclusion.class, inclusion,
				inclusions.size() - 1);
	}

	/**
	 * Adds an exclusion to this dimension. Note that this will not affect the
	 * wrapped {@link QueryDimension}.
	 */
	public void addExclusion(WabitOlapExclusion exclusion) {
		exclusions.add(exclusion);
		exclusion.setParent(this);
		fireChildAdded(WabitOlapExclusion.class, exclusion, exclusions.size() - 1);
	}

	public boolean allowsChildren() {
		return true;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
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

	public void removeDependency(SPObject dependency) {
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

	/**
	 * Warning, if the dimension was made in any way other than loading so that
	 * init was not called this will always be null as it is only set in init.
	 */
	Hierarchy getHierarchy() {
		return hierarchy;
	}
	
	@Override
	public WabitOlapAxis getParent() {
	    return (WabitOlapAxis) super.getParent();
	}
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(WabitOlapInclusion.class);
    	types.add(WabitOlapExclusion.class);
    	return types;
    }

}
