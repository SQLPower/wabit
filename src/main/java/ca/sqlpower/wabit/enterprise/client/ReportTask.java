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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Report;

/**
 * <p>ReportTask objects are used to parameter a server side report production
 * and distribution via email. The mandatory parameters are :
 * 
 * <ul>
 * <li>report</li>
 * <li>email</li>
 * <li>triggerType</li>
 * </ul>
 * 
 * <p>The triggerType has to be one of the following values.
 * 
 * <dl>
 * <dt>minute</dt>
 * <dd>Launches a job every minute.</dd>
 * <dt>hour</dt>
 * <dd>Launches a job every hour.</dd>
 * <dt>day</dt>
 * <dd>Launches a job every day.</dd>
 * <dt>week</dt>
 * <dd>Launches a job every week</dd>
 * <dt>month</dt>
 * <dd>Launches a job every month</dd>
 * </dl>
 * 
 * <p>For a minute triggerType, you need to define those parameters
 * 
 * <dl>
 * <dt>triggerIntervalParam</dt>
 * <dd>Defines how many minutes between each execution. Default 1.</dd>
 * </dl>
 * 
 * <p>For a hour triggerType, you need to define those parameters
 * 
 * <dl>
 * <dt>triggerIntervalParam</dt>
 * <dd>Defines how many hours between each execution. Default 1.</dd>
 * </dl>
 * 
 * <p>For a day triggerType, you need to define those parameters
 * 
 * <dl>
 * <dt>triggerHourParam</dt>
 * <dd>Defines at which hour of the day to launch the execution. Default 0 (midnight).</dd>
 * <dt>triggerMinuteParam</dt>
 * <dd>Defines at which minute of the hour to launch the execution. Default 0.</dd>
 * </dl>
 * 
 * <p>For a week triggerType, you need to define those parameters
 * 
 * <dl>
 * <dt>triggerDayOfWeekParam</dt>
 * <dd>Defines at which day of the week to launch the execution. Default 1.</dd>
 * <dt>triggerHourParam</dt>
 * <dd>Defines at which hour of the day to launch the execution. Default 0 (midnight).</dd>
 * <dt>triggerMinuteParam</dt>
 * <dd>Defines at which minute of the hour to launch the execution. Default 0.</dd>
 * </dl>
 * 
 * <p>For a month triggerType, you need to define those parameters
 * 
 * <dl>
 * <dt>triggerDayOfMonthParam</dt>
 * <dd>Defines at which day of the month to launch the execution. Default 1</dd>
 * <dt>triggerHourParam</dt>
 * <dd>Defines at which hour of the day to launch the execution. Default 0 (midnight).</dd>
 * <dt>triggerMinuteParam</dt>
 * <dd>Defines at which minute of the hour to launch the execution. Default 0.</dd>
 * </dl>
 * 
 * @author luc
 *
 */
public class ReportTask extends AbstractWabitObject {

	private Report report = null;
	private String email = null;
	private String triggerType = null; 
	private int triggerHourParam = 0;
	private int triggerMinuteParam = 0;
	private int triggerDayOfWeekParam = 0;
	private int triggerDayOfMonthParam = 1;
	private int triggerIntervalParam = 1;
	private transient boolean noob = false;
	
	public ReportTask() {
		super();
	}
	
	public ReportTask(Report report) {
		super();
		this.report = report;
	}
	
	public ReportTask(ReportTask task) {
		super();
		this.report = task.getReport();
		this.email = task.getEmail();
		this.triggerType = task.getTriggerType();
		this.triggerDayOfMonthParam = task.getTriggerDayOfMonthParam();
		this.triggerDayOfWeekParam = task.getTriggerDayOfWeekParam();
		this.triggerHourParam = task.getTriggerHourParam();
		this.triggerIntervalParam = task.getTriggerIntervalParam();
		this.triggerMinuteParam = task.getTriggerMinuteParam();
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
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

	public void removeDependency(SPObject dependency) {
		this.report = null;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		Report oldVal = this.report;
		this.report = report;
		firePropertyChange("report", oldVal, report);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		String oldEmail = this.email;
		this.email = email;
		firePropertyChange("email", oldEmail, email);
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		String oldTriggerType = this.triggerType;
		this.triggerType = triggerType;
		firePropertyChange("triggerType", oldTriggerType, triggerType);
	}

	public int getTriggerHourParam() {
		return triggerHourParam;
	}

	public void setTriggerHourParam(int triggerHourParam) {
		int oldVal = this.triggerHourParam;
		this.triggerHourParam = triggerHourParam;
		firePropertyChange("triggerHourParam", oldVal, triggerHourParam);
	}

	public int getTriggerMinuteParam() {
		return triggerMinuteParam;
	}

	public void setTriggerMinuteParam(int triggerMinuteParam) {
		int oldVal = this.triggerMinuteParam;
		this.triggerMinuteParam = triggerMinuteParam;
		firePropertyChange("triggerMinuteParam", oldVal, triggerMinuteParam);
	}

	public int getTriggerDayOfWeekParam() {
		return triggerDayOfWeekParam;
	}

	public void setTriggerDayOfWeekParam(int triggerDayOfWeekParam) {
		int oldVal = this.triggerDayOfWeekParam;
		this.triggerDayOfWeekParam = triggerDayOfWeekParam;
		firePropertyChange("triggerDayOfWeekParam", oldVal, triggerDayOfWeekParam);
	}

	public int getTriggerDayOfMonthParam() {
		return triggerDayOfMonthParam;
	}

	public void setTriggerDayOfMonthParam(int triggerDayOfMonthParam) {
		int oldVal = this.triggerDayOfMonthParam;
		this.triggerDayOfMonthParam = triggerDayOfMonthParam;
		firePropertyChange("triggerDayOfMonthParam", oldVal, triggerDayOfMonthParam);
	}

	public int getTriggerIntervalParam() {
		return triggerIntervalParam;
	}

	public void setTriggerIntervalParam(int triggerIntervalParam) {
		int oldVal = this.triggerIntervalParam;
		this.triggerIntervalParam = triggerIntervalParam;
		firePropertyChange("triggerIntervalParam", oldVal, triggerIntervalParam);
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public boolean isNoob() {
		return noob;
	}

	public void setNoob(boolean noob) {
		this.noob = noob;
	}

	public List<Class<? extends SPObject>> allowedChildTypes() {
		return Collections.emptyList();
	}
}
