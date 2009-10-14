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

package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This WabitObject can take all objects as children. This is useful for testing to group
 * multiple {@link WabitObject}s into the same tree when they may have wildly different
 * parent chains in a normal workspace.
 */
public class AllObjectContainer extends AbstractWabitObject {
	
	private final List<WabitObject> allChildren = new ArrayList<WabitObject>();

	@Override
	protected boolean removeChildImpl(WabitObject child) {
		return allChildren.remove(child);
	}

	public boolean allowsChildren() {
		return true;
	}
	
	@Override
	protected void addChildImpl(WabitObject child, int index) {
		allChildren.add(index, child);
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.unmodifiableList(allChildren);
	}

	public List<WabitObject> getDependencies() {
		return Collections.emptyList();
	}

	public void removeDependency(WabitObject dependency) {
		//do nothing
	}

}
