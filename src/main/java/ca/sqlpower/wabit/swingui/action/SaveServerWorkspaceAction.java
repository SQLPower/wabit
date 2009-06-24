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

package ca.sqlpower.wabit.swingui.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.enterprise.client.WabitServerSessionContext;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Implements the "save as" feature for saving a project to a remote server.
 */
public class SaveServerWorkspaceAction extends AbstractAction {

    private final WabitServerInfo si;
    private final WabitServerSessionContext context;
    private final Component dialogOwner;
    private final WabitWorkspace project;

    public SaveServerWorkspaceAction(WabitServerInfo si, Component dialogOwner, WabitWorkspace project) throws IOException, SQLObjectException {
        super(WabitUtils.serviceInfoSummary(si) + "...");
        this.si = si;
        this.project = project;
        context = WabitServerSessionContext.getInstance(si);
        this.dialogOwner = dialogOwner;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            DataEntryPanel savePanel = new SaveOnServerPanel();
            Window owner = SPSUtils.getWindowInHierarchy(dialogOwner);
            JDialog saveDialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
                    savePanel, owner,
                    "Save on " + WabitUtils.serviceInfoSummary(si),
            "Save");
            saveDialog.setLocationRelativeTo(dialogOwner);
            saveDialog.setVisible(true);
        } catch (Exception ex) {
            SPSUtils.showExceptionDialogNoReport(dialogOwner, "Can't save to server", ex);
        }
    }
    
    private class SaveOnServerPanel implements DataEntryPanel {
        
        private final JPanel panel;
        private JTextField fileNameField;
        private JList existingFileList;

        SaveOnServerPanel() throws IOException, URISyntaxException {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow"));
            fileNameField = new JTextField(project.getName());
            existingFileList = new JList(context.getWorkspaceNames().toArray(new String[0]));
            existingFileList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    fileNameField.setText((String) existingFileList.getSelectedValue());
                }
            });
            builder.append("Existing workspaces on " + WabitUtils.serviceInfoSummary(si) + ":");
            builder.nextLine();
            builder.append(new JScrollPane(existingFileList));
            builder.nextLine();
            builder.append("Save as:");
            builder.nextLine();
            builder.append(fileNameField);
            
            panel = builder.getPanel();
        }
        
        public boolean applyChanges() {
            project.setName(fileNameField.getText());
            // TODO prompt about overwrite
            try {
                context.saveWorkspace(project);
                return true;
            } catch (Exception ex) {
                SPSUtils.showExceptionDialogNoReport(dialogOwner, "Save to server failed", ex);
                return false;
            }
        }

        public void discardChanges() {
            // nothing to do
        }

        public JComponent getPanel() {
            return panel;
        }

        public boolean hasUnsavedChanges() {
            return true;
        }
        
    };
}
