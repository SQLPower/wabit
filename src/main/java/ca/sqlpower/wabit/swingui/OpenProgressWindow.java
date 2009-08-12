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

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;

/**
 * This class displays a modal dialog that shows the progress of a workspace
 * loading. After creating an instance of this class starting the worker will
 * cause the dialog to appear. When the worker completes the dialog will be
 * disposed of.
 */
public class OpenProgressWindow {
    
    private static final Logger logger = Logger.getLogger(OpenProgressWindow.class);

    /**
     * The DAO that will be used to display the progress of the loading file.
     */
    private final SPSwingWorker worker;
    
    /**
     * This modal dialog will be displayed while the file is being loaded.
     */
    private final JDialog progressDialog;
    
    /**
     * This label will be used to display progress information.
     */
    private final JLabel messageLabel = new JLabel("Loading");
    
    private final JProgressBar progressBar = new JProgressBar();
    
    private final JButton cancelButton;
    
    private final JFrame parent;
    
    /**
     * Every time this timer ticks the worker will be checked to see if it has
     * started or finished it's execution.
     */
    private final Timer timer;

    public OpenProgressWindow(final JFrame parent, final SPSwingWorker worker) {
        this.parent = parent;
        this.worker = worker;
        
        cancelButton = new JButton(new AbstractAction("Cancel") {

            public void actionPerformed(ActionEvent e) {
                worker.setCancelled(true);
            }
            
        });
        
                
        timer = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (worker.isFinished() || worker.isCancelled()) {
                    timer.stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    parent.setEnabled(true);
                    logger.debug("Worker stopped");
                } else if (worker.hasStarted()) {
                    startWorker();
                }
            }
        });

        timer.start(); 
                
        progressDialog = new JDialog(parent, "Loading");
        
        buildUI();
    }
    
    private void buildUI() {
        progressDialog.setLayout(new MigLayout("fillx"));
        progressDialog.add(messageLabel, "grow, wrap");
        progressDialog.add(progressBar, "grow, wrap");
        progressDialog.add(cancelButton, "align right");
    }
    
    private void startWorker() {
        ProgressWatcher.watchProgress(progressBar, worker, messageLabel);
        
        messageLabel.setMinimumSize(new Dimension(300, (int) messageLabel.getPreferredSize().getHeight()));
        messageLabel.setPreferredSize(new Dimension(300, (int) messageLabel.getPreferredSize().getHeight()));
        progressDialog.pack();
        
        progressDialog.setLocationRelativeTo(parent);
        
        progressDialog.setVisible(true);
        
        parent.setEnabled(false);
    }
    
}
