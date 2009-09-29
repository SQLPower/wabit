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

import java.util.Collections;
import java.util.List;

import org.olap4j.query.Selection;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class WabitOlapSelection extends AbstractWabitObject {

	private Selection selection;
	
	private String operator;
	
	private String uniqueMemberName;
	
	private boolean initialized = false;

	public WabitOlapSelection(WabitOlapSelection selection) {
		this(selection.operator, selection.uniqueMemberName);
	}
	
	public WabitOlapSelection(Selection selection) {
		this.selection = selection;
	}
	
	public WabitOlapSelection(String operator, String uniqueMemberName) {
		this.setOperator(operator);
		this.setUniqueMemberName(uniqueMemberName);
	}

	void init(OlapQuery query) {
		selection = (Selection) query.findMember(uniqueMemberName);
	}
	
	@Override
	protected boolean removeChildImpl(WabitObject child) {
		return false;
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<WabitObject> getDependencies() {
		return Collections.EMPTY_LIST;
	}

	public void removeDependency(WabitObject dependency) {
		//no-op
	}

	public String getOperator() {
		if (initialized) {
			return selection.getOperator().toString();
		} else {
			return operator;
		}
	}

	public String getUniqueMemberName() {
		if (initialized) {
			return selection.getMember().getUniqueName();
		} else {
			return uniqueMemberName;
		}
	}
	
	public void setOperator(String operator) {
		String oldValue = this.operator;
		this.operator = operator;
		initialized = false;
		firePropertyChange("operator", oldValue, operator);
	}

	public void setUniqueMemberName(String uniqueMemberName) {
		String oldValue = this.uniqueMemberName;
		this.uniqueMemberName = uniqueMemberName;
		initialized = false;
		firePropertyChange("unique-member-name", oldValue, uniqueMemberName);
	}
	
	Selection getSelection() {
		return selection;
	}

}
