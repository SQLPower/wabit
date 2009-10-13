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

package ca.sqlpower.wabit.enterprise.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Report;

public class ReportTask extends AbstractWabitObject {

	private Report report = null;
	private String email = null;
	private String triggerType = null; 
	private int triggerHourParam = -1;
	private int triggerMinuteParam = -1;
	private int triggerDayOfWeekParam = -1;
	private int triggerDayOfMonthParam = -1;
	private int triggerIntervalParam = -1;
	
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
		return Collections.emptyList();
	}

	public List<WabitObject> getDependencies() {
		if (this.report!=null) {
			List<WabitObject> list = new ArrayList<WabitObject>();
			list.add(this.report);
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	public void removeDependency(WabitObject dependency) {
		this.report = null;
	}

	@Override
	public String getName() {
		if (this.report==null) {
			return "Report Task";
		} else {
			return "Report Task (" + report.getName() + ")";
		}
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public int getTriggerHourParam() {
		return triggerHourParam;
	}

	public void setTriggerHourParam(int triggerHourParam) {
		this.triggerHourParam = triggerHourParam;
	}

	public int getTriggerMinuteParam() {
		return triggerMinuteParam;
	}

	public void setTriggerMinuteParam(int triggerMinuteParam) {
		this.triggerMinuteParam = triggerMinuteParam;
	}

	public int getTriggerDayOfWeekParam() {
		return triggerDayOfWeekParam;
	}

	public void setTriggerDayOfWeekParam(int triggerDayOfWeekParam) {
		this.triggerDayOfWeekParam = triggerDayOfWeekParam;
	}

	public int getTriggerDayOfMonthParam() {
		return triggerDayOfMonthParam;
	}

	public void setTriggerDayOfMonthParam(int triggerDayOfMonthParam) {
		this.triggerDayOfMonthParam = triggerDayOfMonthParam;
	}

	public int getTriggerIntervalParam() {
		return triggerIntervalParam;
	}

	public void setTriggerIntervalParam(int triggerIntervalParam) {
		this.triggerIntervalParam = triggerIntervalParam;
	}
}
