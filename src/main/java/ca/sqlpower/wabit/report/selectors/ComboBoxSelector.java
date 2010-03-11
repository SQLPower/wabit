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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import ca.sqlpower.object.SPVariableHelper;

/**
 * Class representing a combo box parameter selector for a dashboard.
 * 
 */
public class ComboBoxSelector extends AbstractSelector {
	
	public static final String STATIC_LIST_DELIMITER = ";";
	
	private final static Logger logger  = Logger.getLogger(ComboBoxSelector.class);

	/**
	 * The variable key from which to populate the combo box 
	 * possible values. May be null if static values are specified.
	 */
	private String sourceKey = null;
	
	/**
	 * List of static values, separated by {@link ComboBoxSelector#STATIC_LIST_DELIMITER}.
	 * May be null if a source key is specified.
	 */
	private String staticValues = null;
	
	
	/**
	 * Wether or not to always include the default value
	 * in the list of possible values.
	 */
	private boolean alwaysIncludeDefaultValue = false;
	
	
	
	@Override
	public Object getDefaultValue() {
		
		if (sourceKey != null) {
			return super.getDefaultValue();
		}
		
		Collection<Object> values = getPossibleValues();
		if (values.size()>0) {
			return values.iterator().next();
		} else {
			return super.getDefaultValue();
		}
		
	}
	
	
	
	public Collection<Object> getPossibleValues() {
		
		if (this.getParent() == null) {
			throw new RuntimeException("Program error. Cannot set values as long as the parent is not set.");
		}
		if (sourceKey == null && staticValues == null) {
			logger.warn("Tried to resolve a selector possible values without having specified a source key nor a static list of values.");
			return Collections.emptyList();
		}
		
		List<Object> values = new ArrayList<Object>();
		
		
		
		if (sourceKey != null) {
			
			// Maybe add the defaultValue.
			if (alwaysIncludeDefaultValue && getDefaultValue() != null) {
				values.add(getDefaultValue());
			}
			
			SPVariableHelper helper = new SPVariableHelper(this);
			values.addAll(helper.resolveCollection(sourceKey));
			
			if (values.size() == 0 && getDefaultValue() != null) {
				values.add(getDefaultValue());
			}
			
		} else {
			
			StringTokenizer st = new StringTokenizer(this.staticValues, ";");
			while (st.hasMoreTokens()) {
				values.add(st.nextToken());
			}
			
		}
		
		
		
		return values;
	}
	
	public void setSourceKey(String sourceKey) {
		
		String oldSourceKey = this.sourceKey;
		
		if (sourceKey != null) {
			this.sourceKey = sourceKey.replaceFirst("\\$", "").replaceFirst("\\{", "").replaceFirst("\\}", "");			
		} else {
			this.sourceKey = null;
		}
		
		if (!ObjectUtils.equals(this.sourceKey, oldSourceKey)) {
			
			Collection<Object> values = getPossibleValues();
			
			if (values.size()>0) {
				this.setSelectedValue(values.iterator().next());				
			} else {
				this.setSelectedValue(null);
			}
			
			firePropertyChange("sourceKey", oldSourceKey, this.sourceKey);
			fireSelectionChanged();
		}
	}
	
	public String getSourceKey() {
		return sourceKey;
	}
	
	public String getStaticValues() {
		return staticValues;
	}
	
	public void setStaticValues(String staticValues) {
		
		String oldStaticValues = this.staticValues;
		this.staticValues = staticValues;
		
		if (!ObjectUtils.equals(this.staticValues, oldStaticValues)) {
			
			Collection<Object> values = getPossibleValues();
			
			if (values.size()>0) {
				this.setSelectedValue(values.iterator().next());				
			} else {
				this.setSelectedValue(null);
			}
			
			firePropertyChange("staticValues", oldStaticValues, this.staticValues);
		}
	}
	
	public boolean isAlwaysIncludeDefaultValue() {
		return alwaysIncludeDefaultValue;
	}
	
	public void setAlwaysIncludeDefaultValue(boolean alwaysIncludeDefaultValue) {
		boolean oldValue = this.alwaysIncludeDefaultValue;
		this.alwaysIncludeDefaultValue = alwaysIncludeDefaultValue;
		if (oldValue != this.alwaysIncludeDefaultValue) {
			firePropertyChange("alwaysIncludeDefaultValue", oldValue, this.alwaysIncludeDefaultValue);
		}
	}
}