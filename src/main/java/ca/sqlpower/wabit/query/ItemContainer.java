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

package ca.sqlpower.wabit.query;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This container is used to hold a generic list of items in
 * the same section.
 */
public class ItemContainer implements Container {
	
	private static final Logger logger = Logger.getLogger(ItemContainer.class);

	/**
	 * The user visible name to this container.
	 */
	private String name;

	/**
	 * This section holds all of the Items containing the strings in this
	 * container.
	 */
	private Section section;
	
	private String alias;
	
	private Point2D position;
	
	/**
	 * A list of listeners that will tell other objects when the model changes.
	 */
	private final List<PropertyChangeListener> modelListeners;
	
	public ItemContainer(String name) {
		this.name = name;
		modelListeners = new ArrayList<PropertyChangeListener>();
		section = new ObjectSection();
		((ObjectSection)section).setParent(this);
		logger.debug("Container created.");
		position = new Point(0, 0);
	}
	
	public Object getContainedObject() {
		return section.getItems();
	}

	public Item getItem(Object item) {
		for (Item i : section.getItems()) {
			if (i.getItem().equals(item)) {
				return i;
			}
		}
		return null;
	}
	
	public void addItem(Item item) {
		section.addItem(item);
		for (PropertyChangeListener l : modelListeners) {
			l.propertyChange(new PropertyChangeEvent(ItemContainer.this, Container.CONTAINTER_ITEM_ADDED, null, item));
		}
	}
	
	public void removeItem(Item item) {
		section.removeItem(item);
		for (PropertyChangeListener l : modelListeners) {
			l.propertyChange(new PropertyChangeEvent(ItemContainer.this, Container.CONTAINER_ITEM_REMOVED, item, null));
		}
	}

	public String getName() {
		return name;
	}

	public List<Section> getSections() {
		return Collections.singletonList(section);
	}

	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the alias of the container. Null is not allowed.
	 */
	public void setAlias(String alias) {
		String oldAlias = this.alias;
		if (alias.equals(oldAlias)) {
			return;
		}
		this.alias = alias;
		for (PropertyChangeListener l : modelListeners) {
			l.propertyChange(new PropertyChangeEvent(this, CONTAINTER_ALIAS_CHANGED, oldAlias, alias));
		}
	}

	public void addChangeListener(PropertyChangeListener l) {
		modelListeners.add(l);		
	}

	public void removeChangeListener(PropertyChangeListener l) {
		modelListeners.remove(l);		
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D p) {
		position = p;
	}

}
