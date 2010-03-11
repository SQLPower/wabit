/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.wabit.report.selectors;

import java.util.HashSet;
import java.util.Set;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObjectTest;
import ca.sqlpower.wabit.report.Report;

public class TextBoxSelectorTest extends AbstractWabitObjectTest {

	private TextBoxSelector selector;
	
	@Override
	public Set<String> getPropertiesToIgnoreForPersisting() {
		Set<String> ignorables = new HashSet<String>();
		ignorables.addAll(super.getPropertiesToIgnoreForPersisting());
		ignorables.add("currentValue");
		return ignorables;
	}
	
	@Override
	public Set<String> getPropertiesToNotPersistOnObjectPersist() {
		Set<String> ignorables = new HashSet<String>();
		ignorables.addAll(super.getPropertiesToNotPersistOnObjectPersist());
		ignorables.add("currentValue");
		return ignorables;
	}
	
	@Override
	public Set<String> getPropertiesToIgnoreForEvents() {
		Set<String> ignorables = new HashSet<String>();
		ignorables.addAll(super.getPropertiesToIgnoreForEvents());
		ignorables.add("currentValue");
		return ignorables;
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.selector = new TextBoxSelector();

		Report report = new Report("Test report");
		report.addChild(selector, report.getSelectors().size());
		
		getWorkspace().addChild(report, 0);
	}
	
	@Override
	public SPObject getObjectUnderTest() {
		return selector;
	}
}
