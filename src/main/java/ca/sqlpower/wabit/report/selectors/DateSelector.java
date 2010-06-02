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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a report parameter that takes date value.
 */
public class DateSelector extends AbstractSelector {

	@Override
	public boolean setSelectedValue(Object newValue) {
		
		final Date value; 
		if (newValue instanceof String) {
			
			DateFormat dateFormatter = 
					new SimpleDateFormat("yy-mm-dd");
			try {
				value = dateFormatter.parse((String)newValue);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Invalid date format. Please use yy-mm-dd");
			}
			
		} else if (newValue instanceof Date) {
			
			value = (Date)newValue;
		
		} else {
			return false;
		}
		
		return super.setSelectedValue(value);
	}
	
}
