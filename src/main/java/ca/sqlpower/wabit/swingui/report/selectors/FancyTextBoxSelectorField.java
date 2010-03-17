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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.ObjectUtils;

import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;

public class FancyTextBoxSelectorField extends JTextField implements SelectorComponent {

	
	private FocusListener focusListener = new FocusListener() {

		public void focusLost(FocusEvent e) {
			
			String newText = getText().trim();
			
			if (newText.length() == 0
					|| ObjectUtils.equals(newText, selector.getDefaultValue())) {
				selector.setSelectedValue(null);
				setText(String.valueOf(selector.getDefaultValue()));
				setForeground(Color.GRAY);
				setFont(getFont().deriveFont(Font.ITALIC));
			} else {
				selector.setSelectedValue(newText);
				setForeground(Color.BLACK);
				setFont(getFont().deriveFont(Font.PLAIN));
			}
			SwingUtilities.invokeLater(refreshRoutine);
		}
		
		public void focusGained(FocusEvent e) {
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
			if (ObjectUtils.equals(getText(), selector.getDefaultValue())) {
				setText("");
			}
		}
	};
	

	private final TextBoxSelector selector;

	private final Runnable refreshRoutine;
	
	private SPListener spListener = new AbstractPoolingSPListener() {
		protected void propertyChangeImpl(final java.beans.PropertyChangeEvent evt) {
			
			String currentText = getText().trim();
			
			if (currentText.length() == 0
					|| ObjectUtils.equals(currentText, evt.getOldValue()==null?"":evt.getOldValue())) {
			
				setText(evt.getNewValue()==null?"":evt.getNewValue().toString());
				setForeground(Color.GRAY);
				setFont(getFont().deriveFont(Font.ITALIC));
				SwingUtilities.invokeLater(refreshRoutine);
			
			}
			
		};
	};


	private KeyListener enterKeyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				FancyTextBoxSelectorField.this.transferFocus();
			}
		}
		public void keyReleased(KeyEvent e) {
			// not interested
		}
		public void keyPressed(KeyEvent e) {
			// not interested
		}
	};
	
	
	public FancyTextBoxSelectorField(final TextBoxSelector selector, Runnable refreshRoutine) {
		
		this.selector = selector;
		this.refreshRoutine = refreshRoutine;
		this.addFocusListener(focusListener);
		this.addKeyListener(this.enterKeyListener);
		this.selector.addSPListener(spListener);
		
		setText(selector.getCurrentValue()==null?"":selector.getCurrentValue().toString());
		
		if (getText().length() == 0
				|| ObjectUtils.equals(getText(), selector.getDefaultValue())) {
			selector.setSelectedValue(null);
			setText(String.valueOf(selector.getDefaultValue()));
			setForeground(Color.GRAY);
			setFont(getFont().deriveFont(Font.ITALIC));
		} else {
			selector.setSelectedValue(getText());
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
	}
	
	public void cleanup() {
		this.selector.removeSPListener(spListener);
	}
}
