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

package ca.sqlpower.wabit.swingui.enterprise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Creates a panel for setting the properties of a WabitServerInfo. Since
 * instances of WabitServerInfo are not mutable, calling applyChanges() will not
 * modify the original WabitServerInfo object provided in the constructor. You
 * must obtain a new WabitServerInfo object by calling getServerInfo().
 */
public class ServerInfoPanel implements DataEntryPanel {

    private final Component dialogOwner;

    private final JPanel panel;

    private JTextField name;
    private JTextField host;
    private JTextField port;
    private JTextField path;
    private JTextField username;
    private JPasswordField password;

    
    public ServerInfoPanel(Component dialogOwner, WabitServerInfo defaultSettings) {
        this.dialogOwner = dialogOwner;
        panel = buildUI(defaultSettings);
    }

    public ServerInfoPanel(JComponent dialogOwner) {
        this(dialogOwner, new WabitServerInfo("", "", 8080, "/wabit-enterprise/", "", ""));
    }

    private JPanel buildUI(WabitServerInfo si) {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, max(100dlu; pref):grow"));
        
        builder.append("Display Name", name = new JTextField(si.getName()));
        builder.append("Host", host = new JTextField(si.getServerAddress()));
        builder.append("Port", port = new JTextField(String.valueOf(si.getPort())));
        builder.append("Path", path = new JTextField(si.getPath()));
        builder.append("Username", username = new JTextField(si.getUsername()));
        builder.append("Password", password = new JPasswordField(si.getPassword()));
        
        return builder.getPanel();
    }

    /**
     * Returns a new WabitServerInfo object which has been configured based on the
     * settings currently in this panel's fields.
     */
    public WabitServerInfo getServerInfo() {
        int port = Integer.parseInt(this.port.getText());
        WabitServerInfo si = new WabitServerInfo(
                name.getText(), host.getText(), port, path.getText(), 
                username.getText(), new String(password.getPassword()));
        return si;
    }
    public JComponent getPanel() {
        return panel;
    }

    /**
     * Checks fields for validity, but does not modify the WabitServerInfo given in
     * the constructor (this is not possible because it's immutable). If any of
     * the fields contain inappropriate entries, the user will be told so in a
     * dialog.
     * 
     * @return true if all the fields contain valid values; false if there are
     *         invalid fields.
     */
    public boolean applyChanges() {
    	
    	if (this.name.getText()==null||this.name.getText().equals("")) {
    		JOptionPane.showMessageDialog(
    				dialogOwner, "Please give this conenction a name for future reference.",
    				"Name Required", JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    	
    	String port = this.port.getText();
    	try {
    		Integer.parseInt(port);
    	} catch (NumberFormatException ex) {
    		JOptionPane.showMessageDialog(
    				dialogOwner, "The server port must be a numeric value. It is usually either 80 or 8080. In doubt, contact your system administrator.",
    				"Invalid Server Port Number", JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    	
    	if (!this.path.getText().startsWith("/")) {
    		this.path.setText("/".concat(this.path.getText()==null?"":this.path.getText()));
    	}
    	String path = this.path.getText();
    	if (path == null || path.length() < 2) {
    		JOptionPane.showMessageDialog(
    				dialogOwner, "Path must begin with /",
    				"Invalid Setting", JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    	
    	if (this.host.getText().startsWith("http://")) {
    		this.host.setText(this.host.getText().replace("http://", ""));
    	}
    	String host = this.host.getText();
    	try {
    		new URI("http", null, host, Integer.parseInt(port), path, null, null);
    	} catch (URISyntaxException e) {
    		JOptionPane.showMessageDialog(
    				dialogOwner, "There seems to be a problem with the host name you provided. It can be a web URL (you can omit the http:// part) or a IP adress. Please verify the values provided are correct.",
    				"", JOptionPane.ERROR_MESSAGE);
    		return false;
    	}
    	
        
        
        return true;
    }

    public void discardChanges() {
        // nothing to do
    }

    public boolean hasUnsavedChanges() {
        return true;
    }
    

	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                	ServerInfoPanel panel = new ServerInfoPanel(null);
                	
                    JFrame f = new JFrame("TEST PANEL");
                    JPanel outerPanel = new JPanel(new BorderLayout());
                    outerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    outerPanel.add(panel.getPanel(), BorderLayout.CENTER);
                    f.setContentPane(outerPanel);
                    f.pack();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setVisible(true);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
