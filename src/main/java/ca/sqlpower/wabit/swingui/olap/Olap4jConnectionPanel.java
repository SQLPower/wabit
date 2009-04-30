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

package ca.sqlpower.wabit.swingui.olap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.olap.Olap4jDataSource;
import ca.sqlpower.wabit.olap.Olap4jDataSource.Type;

/**
 * Provides a GUI for modifying the properties of an Olap4jDataSource.
 */
public class Olap4jConnectionPanel implements DataEntryPanel {

    private final Olap4jDataSource olapDataSource;
    
    private JPanel panel;
    private JTextField schemaFileField;
    private JComboBox dataSourceBox;

    private JRadioButton xmlaType;

    private JRadioButton inProcessType;

    private JTextField xmlaUriField;

    public Olap4jConnectionPanel(Olap4jDataSource olapDataSource, DataSourceCollection dsCollection) {
        this.olapDataSource = olapDataSource;
        panel = new JPanel(new MigLayout("", "[][grow][]", ""));

        ButtonGroup connectionTypeGroup = new ButtonGroup();
        inProcessType = new JRadioButton("In-process Mondrian Server");
        connectionTypeGroup.add(inProcessType);
        panel.add(inProcessType, "span 2,wrap");
        
        panel.add(new JLabel("Database Connection"), "gapbefore 25px");
        dataSourceBox = new JComboBox(new ConnectionComboBoxModel(dsCollection));
        panel.add(dataSourceBox, "grow,wrap");
        
        panel.add(new JLabel("Mondrian Schema"), "gapbefore 25px");
        schemaFileField = new JTextField();
        panel.add(schemaFileField, "growx");
        JButton fileChooserButton = new JButton("...");
        panel.add(fileChooserButton, "wrap paragraph");
        fileChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(schemaFileField.getText());
                fc.setFileFilter(SPSUtils.XML_FILE_FILTER);
                int choice = fc.showOpenDialog(panel);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    schemaFileField.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });
        
        xmlaType = new JRadioButton("Remote XML/A Server");
        connectionTypeGroup.add(xmlaType);
        panel.add(xmlaType, "span 2,wrap");
        panel.add(new JLabel("XML/A Server URL"), "gapbefore 25px");
        URI xmlaServerURI = olapDataSource.getXmlaServer();
        xmlaUriField = new JTextField(xmlaServerURI == null ? "" : xmlaServerURI.toString());
        panel.add(xmlaUriField, "growx");
        
        Type type = olapDataSource.getType();
        if (type == null || type == Type.IN_PROCESS) {
            // default type
            inProcessType.setSelected(true);
        } else if (type == Type.XMLA) {
            xmlaType.setSelected(true);
        } else {
            throw new IllegalStateException("Unknown olap4j connection type: " + type);
        }
    }
    
    public boolean applyChanges() {
        if (inProcessType.isSelected()) {
            olapDataSource.setType(Type.IN_PROCESS);
        } else if (xmlaType.isSelected()) {
            olapDataSource.setType(Type.XMLA);
        } else {
            throw new IllegalStateException(
                    "Someone added a new connection type but forgot to" +
                    " put in the code for storing it");
        }
        
        olapDataSource.setDataSource((SPDataSource) dataSourceBox.getSelectedItem());
        olapDataSource.setMondrianSchema(new File(schemaFileField.getText()).toURI());
        try {
            olapDataSource.setXmlaServer(new URI(xmlaUriField.getText()));
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(panel, "XML/A Server URI is not valid.");
            return false;
        }
        
        return true;
    }

    public void discardChanges() {
        // no op
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return true;
    }
}
