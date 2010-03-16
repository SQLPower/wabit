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

package ca.sqlpower.wabit.swingui.report.selectors;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.ObjectUtils;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.report.selectors.Selector;
import ca.sqlpower.wabit.report.selectors.SelectorListener;

public class FancyComboBoxSelector extends JComboBox implements
		SelectorComponent {

	private final ComboBoxSelector selector;
	private final Runnable refreshRoutine;
	private AtomicBoolean ignoreEvents = new AtomicBoolean(false);

	private SPListener selectorListener = new AbstractSPListener() {
		protected void propertyChangeImpl(java.beans.PropertyChangeEvent evt) {
			if (!evt.getPropertyName().equals("parent"))
				refresh();
		};
	};
	
	private final SelectorListener selectionListener = new SelectorListener() {
		public void selectionChanged(Selector source) {
			
			Object currentSelection = selector.getCurrentValue();
			
			if (currentSelection != null
					&& !ObjectUtils.equals(getSelectedItem(), currentSelection)) {
				setSelectedItem(currentSelection);
			}
			
			SwingUtilities.invokeLater(refreshRoutine);
		}
	};
	
	public FancyComboBoxSelector(ComboBoxSelector selector, Runnable refreshRoutine) {
		
		this.selector = selector;
		this.refreshRoutine = refreshRoutine;
		
		this.selector.addSPListener(selectorListener);
		this.selector.addSelectorListener(selectionListener);
		
		addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (ignoreEvents.get()) return;
				if (!ObjectUtils.equals(getSelectedItem(), FancyComboBoxSelector.this.selector.getCurrentValue())) {
					FancyComboBoxSelector.this.selector.setSelectedValue(getSelectedItem());
				}
			}
		});
		
		refresh();
	}
	
	private void refresh() {
		ignoreEvents.set(true);
		try {
			removeAllItems();
			
			Collection<Object> values = selector.getPossibleValues();
			
			for (Object value : values) {
				addItem(value);
			}
			
			if (values.size() > 0) 
			{
				setSelectedItem(selector.getCurrentValue());	
			}
			
			SwingUtilities.invokeLater(refreshRoutine);
		} finally {
			ignoreEvents.set(false);
		}
	}

	public void cleanup() {
		this.selector.removeSPListener(selectorListener);
		this.selector.removeSelectorListener(selectionListener);
	}

}
