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

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.architect.SQLObject;

/**
 * A model for the {@link ContainerPane}. This will store objects of a defined type and
 * can be grouped when adding the items to the model.
 *
 * @param <C> The type of object this model will store.
 */
public class ContainerModel<C extends SQLObject> {

	private List<List<C>> containers;
	
	private String name;
	
	public ContainerModel() {
		containers = new ArrayList<List<C>>();
		name = "";
	}
	
	public void addContainer() {
		containers.add(new ArrayList<C>());
	}
	
	public void addItem(int containerIndex, C item) {
		containers.get(containerIndex).add(item);		
	}
	
	public C getContents(int containerIndex, int containerLocation) {
		return containers.get(containerIndex).get(containerLocation);
	}
	
	public int getContainerCount() {
		return containers.size();
	}
	
	public int getContainerSize(int containerIndex) {
		return containers.get(containerIndex).size();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
