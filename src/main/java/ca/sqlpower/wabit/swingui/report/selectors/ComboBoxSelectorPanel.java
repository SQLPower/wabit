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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.object.InsertVariableAction;
import ca.sqlpower.swingui.object.VariableInserter;
import ca.sqlpower.wabit.report.selectors.ComboBoxSelector;
import ca.sqlpower.wabit.swingui.WabitIcons;

import com.lowagie.text.Font;

public class ComboBoxSelectorPanel implements DataEntryPanel {
	
	private final Component dialogOwner;
	private final ComboBoxSelector selector;
	private final SPObject context;	
	private boolean dirty = true;
	private final JPanel panel;
	
	private final JTextField genLabelField = new JTextField();
	private final JTextField valSpecField = new JTextField();
	private final JComboBox valTypeCombo = new JComboBox(Mode.values());
	private final JButton valSpecVarButton = new JButton();
	
	private final JLabel defValueLabel = new JLabel("Default value");
	private final JTextField defValueField = new JTextField();
	private final JLabel includeDefValueLabel = new JLabel("<html>Always include<br>default value</html>");
	private final JCheckBox includeDefValueBox = new JCheckBox("");
	
	private final JLabel staticHelpLabel = 
		new JLabel("<html>Please enter all the desired options as a semicolon delimited list. <br>&nbsp;&nbsp;&nbsp;&nbsp;ex: \"1999;2000;2001\"</html>");
	
	private enum Mode {
		Variable,
		Static
	}
	
	private Mode currentMode = Mode.Variable;
	
	private class InsertAction extends AbstractAction {
		public InsertAction() {
			super("Source...");
		}
		public void actionPerformed(ActionEvent e) {
			InsertVariableAction insertVariable = 
	    		new InsertVariableAction(
	    				"Source...",
	    				"Select a variable as a source",
	    				valSpecField.getText(),
	    				new SPVariableHelper(context), 
	    				null, 
	    				new VariableInserter() {
							@Override
							public void insert(String variable) {
								valSpecField.setText(variable);
							}
						}, 
	    				dialogOwner);
			insertVariable.actionPerformed(e);
		}
	};
	
	private InsertAction insertVariableAction = new InsertAction();
	
	
	
	
	public ComboBoxSelectorPanel(Component dialogOwner, SPObject context, ComboBoxSelector selector) {	
		this.dialogOwner = dialogOwner;
		this.context = context;
		this.selector = selector;
		
		if (selector.getStaticValues() != null) {
			this.currentMode = Mode.Static;
		} else {
			this.currentMode = Mode.Variable;
		}
		
		this.panel = new JPanel(new MigLayout("hidemode 1"));
		
		buildPanel();
		
		
		defValueField.setText(selector.getDefaultValue()==null?"":selector.getDefaultValue().toString());
		includeDefValueBox.setSelected(selector.isAlwaysIncludeDefaultValue());
		this.genLabelField.setText(selector.getName() == null ? "" : selector.getName());
		
		updateUi();
	}
	
	
	
	
	private void buildPanel() {
		
		JLabel logo = new JLabel(WabitIcons.PARAMETERS_COMBO_16);
		JLabel title = new JLabel("Drop down list");
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
        JLabel valTypeLabel = new JLabel("Source type");

        
        valTypeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!valTypeCombo.getSelectedItem().equals(currentMode)) {
					currentMode = (Mode)valTypeCombo.getSelectedItem();
					updateUi();
				}
			}
		});
        
        
        
        JLabel valSpecLabel = new JLabel("Source");
        
        
        
        
        
        this.valSpecVarButton.setAction(insertVariableAction);
        
        
    	valSpecField.addMouseListener(new MouseListener() {
    		public void mouseReleased(MouseEvent e) {
    			// Not interested.
    		}
    		public void mousePressed(MouseEvent e) {
    			// Not interested.
    		}
    		public void mouseExited(MouseEvent e) {
    			// Not interested.
    		}
    		public void mouseEntered(MouseEvent e) {
    			// Not interested.
    		}
    		public void mouseClicked(MouseEvent e) {
    			if (currentMode.equals(Mode.Variable)) {
    				insertVariableAction.actionPerformed(null);
    			}
    		}
    	});
        
        
        
        panel.add(titleBox, "span, wrap");
        panel.add(new JLabel(" "), "span, wrap");
        
        panel.add(generalLabel, "span, wrap");
        panel.add(genLabelLabel, "gapleft 15px");
        panel.add(genLabelField, "span, wrap, wmin 300px, wmax 300px");
        
        
        panel.add(new JLabel(" "), "span, wrap");
        
        
        panel.add(valuesLabel, "span, wrap");
        panel.add(valTypeLabel, "gapleft 15px");
        panel.add(valTypeCombo, "span, wrap, wmin 300px, wmax 300px");
        
        
        
        
        panel.add(valSpecLabel, "gapleft 15px");
        
        
        Box specBox = Box.createHorizontalBox();
        specBox.add(valSpecField, "growx, wmin 200px, wmax 300px");
        specBox.add(valSpecVarButton, "wrap, wmin 100px, wmax 100px");
        panel.add(specBox, "wrap, wmin 300px, wmax 300px");
        
        panel.add(defValueLabel, "gapleft 15px");
        panel.add(defValueField, "span, wrap, wmin 300px, wmax 300px");
        panel.add(includeDefValueLabel, "gapleft 15px");
        panel.add(includeDefValueBox, "span, wrap");
        
    	
        panel.add(staticHelpLabel, "gapleft 15px, span, wrap");
        
        panel.add(new JLabel(" "));
        
	}
	
	
	private void updateUi() {
		if (currentMode.equals(Mode.Variable)) {
			valSpecField.setEnabled(false);
        	this.staticHelpLabel.setVisible(false);
        	this.valSpecVarButton.setVisible(true);
        	this.defValueLabel.setVisible(true);
        	this.defValueField.setVisible(true);
        	this.includeDefValueLabel.setVisible(true);
        	this.includeDefValueBox.setVisible(true);
        	this.valTypeCombo.setSelectedItem(Mode.Variable);
        	valSpecField.setText(selector.getSourceKey());
        } else {
        	valSpecField.setEnabled(true);
        	this.staticHelpLabel.setVisible(true);
        	this.valSpecVarButton.setVisible(false);
        	this.defValueLabel.setVisible(false);
        	this.defValueField.setVisible(false);
        	this.includeDefValueLabel.setVisible(false);
        	this.includeDefValueBox.setVisible(false);
        	this.valTypeCombo.setSelectedItem(Mode.Static);
        	valSpecField.setText(selector.getStaticValues());
        }
		
		this.panel.repaint();
		
		Window window = SwingUtilities.getWindowAncestor(this.panel);
		if (window != null) {
			window.pack();
		}
	}

	public boolean applyChanges() {
		if (dirty) {
			
			if (currentMode.equals(Mode.Variable)) {
				selector.setStaticValues(null);
				selector.setAlwaysIncludeDefaultValue(this.includeDefValueBox.isSelected());
				selector.setDefaultValue(this.defValueField.getText().trim().length()==0? null:this.defValueField.getText().trim());
				selector.setSourceKey(valSpecField.getText());
			} else {
				selector.setSourceKey(null);
				selector.setDefaultValue(null);
				selector.setAlwaysIncludeDefaultValue(false);
				selector.setStaticValues(valSpecField.getText());
			}

			selector.setName(genLabelField.getText());
			
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
