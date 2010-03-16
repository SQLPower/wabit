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

import java.awt.Component;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.selectors.TextBoxSelector;
import ca.sqlpower.wabit.swingui.WabitIcons;

import com.lowagie.text.Font;

public class TextBoxSelectorPanel implements DataEntryPanel {
	
	private final Component dialogOwner;
	private final TextBoxSelector selector;
	private boolean dirty = true;
	private final JPanel panel;
	
	private final JTextField genLabelField = new JTextField();
	
	private final JLabel defValueLabel = new JLabel("Default value");
	private final JTextField defValueField = new JTextField();
	
	
	public TextBoxSelectorPanel(Component dialogOwner, TextBoxSelector selector) {	
		this.dialogOwner = dialogOwner;
		this.selector = selector;
		
		this.panel = new JPanel(new MigLayout("hidemode 1"));
		
		buildPanel();
		updateUi();
	}
	
	
	
	
	private void buildPanel() {
		
		
		JLabel logo = new JLabel(WabitIcons.PARAMETERS_TEXT_16);
		JLabel title = new JLabel("Free form text box");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setFont(title.getFont().deriveFont(12f));
		Box titleBox = Box.createHorizontalBox();
		titleBox.add(logo);
		titleBox.add(new JLabel(" "));
		titleBox.add(title);
		
        // Build the 'general' section
        
        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(generalLabel.getFont().deriveFont(Font.BOLD));
        JLabel genLabelLabel = new JLabel("Name");
        JLabel valuesLabel = new JLabel("Values");
        valuesLabel.setFont(valuesLabel.getFont().deriveFont(Font.BOLD));

        
        
        panel.add(titleBox, "span, wrap");
        panel.add(new JLabel(" "), "span, wrap");
        
        panel.add(generalLabel, "span, wrap");
        panel.add(genLabelLabel, "gapleft 15px");
        panel.add(genLabelField, "span, wrap, wmin 300px, wmax 300px");
        
        
        panel.add(new JLabel(" "), "span, wrap");
        
        
        panel.add(valuesLabel, "span, wrap");        
        panel.add(defValueLabel, "gapleft 15px");
        panel.add(defValueField, "span, wrap, wmin 300px, wmax 300px");
        
        panel.add(new JLabel(" "));
        
	}
	
	
	private void updateUi() {
		defValueField.setText(selector.getDefaultValue()==null?"":selector.getDefaultValue().toString());
		this.genLabelField.setText(selector.getName());
		this.panel.repaint();
		Window window = SwingUtilities.getWindowAncestor(this.panel);
		if (window != null) {
			window.pack();
		}
	}

	public boolean applyChanges() {
		if (dirty) {
			selector.setDefaultValue(defValueField.getText().trim());
			selector.setName(genLabelField.getText().trim());
			dirty = false;
			this.dialogOwner.repaint();
			return true;
		}
		return false;
	}

	public void discardChanges() {
		dirty = false;
	}

	public JComponent getPanel() {
		return this.panel;
	}

	public boolean hasUnsavedChanges() {
		return dirty;
	}
}
