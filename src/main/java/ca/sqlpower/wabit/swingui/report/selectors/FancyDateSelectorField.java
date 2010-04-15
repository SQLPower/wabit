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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang.ObjectUtils;
import org.jdesktop.swingx.JXDatePicker;

import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.report.selectors.DateSelector;

public class FancyDateSelectorField extends JXDatePicker implements SelectorComponent {

	
	private PopupMenuListener popupMenuListener = new PopupMenuListener() {

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
			// no op
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			refreshEverything();
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
	};
	
	private void refreshEverything() {
		
		if (getDate() == null) {
			setDate((Date)selector.getDefaultValue());
		}
		
		if (ObjectUtils.equals(getDate(), selector.getDefaultValue())) {
			selector.setSelectedValue(null);
			setForeground(Color.GRAY);
			setFont(getFont().deriveFont(Font.ITALIC));
		} else {
			selector.setSelectedValue(getDate());
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
		SwingUtilities.invokeLater(refreshRoutine);
	}

	private final DateSelector selector;

	private final Runnable refreshRoutine;
	
	private SPListener spListener = new AbstractPoolingSPListener() {
		protected void propertyChangeImpl(final java.beans.PropertyChangeEvent evt) {
			
			if (evt.getPropertyName().equals("defaultValue") &&
					ObjectUtils.equals(getDate(), evt.getOldValue())) {
			
				setDate((Date)evt.getNewValue());
				
				if (ObjectUtils.equals(getDate(), selector.getDefaultValue())) {
					setForeground(Color.GRAY);
					setFont(getFont().deriveFont(Font.ITALIC));
				} else {
					setForeground(Color.BLACK);
					setFont(getFont().deriveFont(Font.PLAIN));
				}
				
				SwingUtilities.invokeLater(refreshRoutine);
			}
		};
	};
	
	
	public FancyDateSelectorField(final DateSelector selector, Runnable refreshRoutine) {
		
		this.selector = selector;
		this.refreshRoutine = refreshRoutine;
		//this.addPopupMenuListener(popupMenuListener);
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshEverything();
			}
		});
		this.selector.addSPListener(spListener);
		
		Date currentValue = selector.getCurrentValue()==null
				?null
				:(Date)selector.getCurrentValue();
		setDate(currentValue);
		
		if (ObjectUtils.equals(getDate(), selector.getDefaultValue())) {
			setForeground(Color.GRAY);
			setFont(getFont().deriveFont(Font.ITALIC));
		} else {
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
	}
	
	public void cleanup() {
		this.selector.removeSPListener(spListener);
	}
}
