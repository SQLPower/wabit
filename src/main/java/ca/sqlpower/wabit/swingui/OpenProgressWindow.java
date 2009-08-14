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

package ca.sqlpower.wabit.swingui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * This class displays a modal dialog that shows the progress of a workspace
 * loading. After calling the one static method in this class the progress
 * window will appear and make the Wabit window unresponsive. When the worker
 * completes the dialog will be disposed of and the Wabit window will become
 * responsive again.
 */
public class OpenProgressWindow {

    /**
     * This method displays a modal dialog that shows the progress of a
     * workspace loading. The progress window will appear immediately and make
     * the Wabit window unresponsive. When the worker completes the dialog will
     * be disposed of and the Wabit window will become responsive again.
     * 
     * @param parent
     *            The frame to parent the dialog to. This frame will be
     *            unresponsive while the progress dialog is displayed.
     * @param worker
     *            The worker this progress window will monitor. When the worker
     *            finishes this dialog will go away.
     */
    public static void showProgressWindow(final JFrame parent, final SPSwingWorker worker) {
        
        final JDialog progressDialog = new JDialog(parent, "Loading");
        
        final JLabel messageLabel = new JLabel("Loading");
        
        final JProgressBar progressBar = new JProgressBar();
        
        final JButton cancelButton = new JButton(new AbstractAction("Cancel") {

            public void actionPerformed(ActionEvent e) {
                worker.setCancelled(true);
            }
            
        });
        
        // sticking the timer in an array so it can be accessed from inside the innerO class
        final Timer[] timerHandle = new Timer[1];
        timerHandle[0] = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (worker.isFinished() || worker.isCancelled()) {
                    timerHandle[0].stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    parent.setEnabled(true);
                }
            }
        });
        timerHandle[0].start(); 
                
        
        progressDialog.setLayout(new MigLayout("fillx"));
        progressDialog.add(messageLabel, "grow, wrap");
        progressDialog.add(progressBar, "grow, wrap");
        progressDialog.add(cancelButton, "align right");
    
        ProgressWatcher.watchProgress(progressBar, worker, messageLabel);
        
        messageLabel.setMinimumSize(new Dimension(300, (int) messageLabel.getPreferredSize().getHeight()));
        messageLabel.setPreferredSize(new Dimension(300, (int) messageLabel.getPreferredSize().getHeight()));
        progressDialog.pack();
        
        progressDialog.setLocationRelativeTo(parent);
        
        progressDialog.setVisible(true);
        
        parent.setEnabled(false);
    }
    
}
