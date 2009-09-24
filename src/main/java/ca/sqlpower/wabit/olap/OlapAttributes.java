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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * Provides a number of attributes for an Olap4j query. Each part of an Olap
 * Query (eg. Axis, Dimension) can be represented by an OlapAttributes instance.
 * The attributes store important informaton on how to create or save each part
 * of an Olap query.
 * 
 */
public class OlapAttributes {

	private String name;

	/**
	 * A Map of property names to values that can be used by the OlapQuery class
	 * to build a part of a valid MDX query
	 */
	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * The parent of the part of the Olap query represented by this
	 * OlapAttributes instance. The top level objects, in this case the axes, do
	 * not have parents.
	 */
	private OlapAttributes parent;

	/**
	 * A Set of OlapAttributes representing children of this part of the Olap
	 * query. Each contains more information about how to build or save the
	 * query
	 */
	private Set<OlapAttributes> children = new HashSet<OlapAttributes>();
	
	public OlapAttributes(String name){
		this.name = name;
	}

	public void setParent(OlapAttributes parent) {
		this.parent = parent;
	}

	public OlapAttributes getParent() {
		return parent;
	}
	
	public void addAttribute(String name, String value){
		attributes.put(name, value);
	}
	
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
	
	public void addChild(OlapAttributes child){
		children.add(child);
		child.setParent(this);
	}
	
	public Set<OlapAttributes> getChildren() {
		return Collections.unmodifiableSet(children);
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Returns the String representing the name of the value that can represent
	 * this attribute uniquely. Returns <code>null</code> if this type attribute
	 * is always unique
	 */
	public String getUniqueTag(){
		if (name.equals("olap4j-axis")) {
			return "ordinal";
		} else if (name.equals("olap4j-dimension")) {
			return "dimension-name";
		} else if (name.equals("olap4j-selection") || name.equals("olap4j-exclusion")) {
			return "unique-member-name";
		} else {
			return null;
		}
	}
	
}
