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

package ca.sqlpower.wabit.swingui.report;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.Font;

public class ReportParameterPanel implements DataEntryPanel {
	
	private final JPanel panel;
	
	public ReportParameterPanel() {
		
		FormLayout layout = new FormLayout("15dlu, 4dlu, pref, 4dlu, pref:grow");
        final DefaultFormBuilder fb = new DefaultFormBuilder(layout);
		
        
        
        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(generalLabel.getFont().deriveFont(Font.BOLD));
   
        JLabel genLabelLabel = new JLabel("Name");
        JTextField genLabelField = new JTextField();
        
        JLabel genTypeLabel = new JLabel("Control type");
        JComboBox genTypeCombo = new JComboBox(new String[] {"Combo box", "Text field"});
        
        
        
        
        
        JLabel valuesLabel = new JLabel("Values provider");
        valuesLabel.setFont(valuesLabel.getFont().deriveFont(Font.BOLD));
        
        JLabel valTypeLabel = new JLabel("Source");
        JComboBox valTypeCombo = new JComboBox(new String[] {"Static list", "Variable"});
        
        JLabel valSpecLabel = new JLabel("Values");
        JTextField valSpecField = new JTextField("${user.name}");
        valSpecField.setEnabled(false);
        JLabel staticHelpLabel = new JLabel("<html>Please enter all the desired options as a semicolon delimited list. <br>&nbsp;&nbsp;&nbsp;&nbsp;ex: \"1999;2000;2001\"</html>");
        
        
        
        JButton valSpecVarButton = new JButton("Variables");
        
        
        
        fb.append(generalLabel, 5);
        fb.nextLine();
        
        
        fb.append("");
        fb.append(genLabelLabel);
        fb.append(genLabelField);
        fb.nextLine();
        
        
        
        fb.append("");
        fb.append(genTypeLabel);
        fb.append(genTypeCombo);
        fb.nextLine();
        
        

        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        
        
        fb.append(valuesLabel, 5);
        fb.nextLine();
        
        
        
        fb.append("");
        fb.append(valTypeLabel);
        fb.append(valTypeCombo);
        fb.nextLine();
        
        
        
        
        
        
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
//        fb.append("");
//        fb.append(staticHelpLabel, 3);
//        fb.nextLine();
        
        fb.append("");
        fb.append(valSpecLabel);
        //fb.append(valSpecField);
        Box varControls = Box.createHorizontalBox();
        varControls.add(valSpecField);
        varControls.add(valSpecVarButton);
        fb.append(varControls);
        fb.nextLine();
        
        
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        
        this.panel = fb.getPanel();
	}

	public boolean applyChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	public void discardChanges() {
		// TODO Auto-generated method stub

	}

	public JComponent getPanel() {
		return this.panel;
	}

	public boolean hasUnsavedChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	public static void main(String[] args) {
		ReportParameterPanel rpp = new ReportParameterPanel();
		JFrame f = new JFrame("TEST PANEL");
	    JPanel outerPanel = new JPanel(new BorderLayout());
	    outerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
	    outerPanel.add(rpp.getPanel(), BorderLayout.CENTER);
		  f.setContentPane(outerPanel);
		  f.pack();
		  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  f.setVisible(true);
	}
}
