/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;

public class PrintAction extends AbstractAction {
	
	public class PrintWorker extends SPSwingWorker {
		
		private final PrinterJob job;
		private final Layout printingLayout;
		private int progress;
		private final SPVariableHelper variableHelper;

		public PrintWorker(SwingWorkerRegistry registry, PrinterJob job, Layout layout) {
			super(registry);
			printingLayout = layout;
			setJobSize(layout.getNumberOfPages());
			setMessage(null);
			progress = 0;
			this.job = job;
			this.variableHelper = new SPVariableHelper(layout);
		}

		@Override
		public void doStuff() throws Exception {
            job.print();
		}
		
		@Override
		public void cleanup() throws Exception {
			printingLayout.compareAndSetCurrentlyPrinting(true, false);
			if (getDoStuffException() != null) {
				throw new RuntimeException(getDoStuffException());
			}
		}

		@Override
		protected int getProgressImpl() {
			Object progressObject = this.variableHelper.resolve(Report.PAGE_NUMBER, progress);
			if (progressObject instanceof Integer) {
				progress = ((Integer) progressObject).intValue();
			}
			return progress;
		}

	}

    public static final Icon ICON = new ImageIcon(PageFormatAction.class.getResource("/icons/32x32/print.png"));
    private final Layout layout;
    private final Component dialogOwner;
	private final WabitSwingSession session;
	private final JFrame parentFrame;

    public PrintAction(Layout layout, Component dialogOwner, WabitSwingSession session) {
        super("Print...", ICON);
		this.session = session;
		parentFrame = ((WabitSwingSessionContext) session.getContext()).getFrame();
        putValue(SHORT_DESCRIPTION, "Print Report");
        this.layout = layout;
        this.dialogOwner = dialogOwner;
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (!layout.compareAndSetCurrentlyPrinting(false, true)) {
    		JOptionPane.showMessageDialog(dialogOwner, "The layout is currently exporting. Please try again after it completes.", "In Use", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(layout);
        boolean ok = job.printDialog();
        if (ok) {
        	
        	final JPanel glassPane = new JPanel();
        	parentFrame.setGlassPane(glassPane);
            glassPane.setVisible(true);
            glassPane.setFocusable(true);
            glassPane.setOpaque(false);
            glassPane.addFocusListener(new FocusListener() {
			
				public void focusLost(FocusEvent e) {
					if (glassPane.isVisible()) {
						glassPane.requestFocus();
					}
				}
				public void focusGained(FocusEvent e) {
					//Do nothing on focus gained
				}
			});
			
            glassPane.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {
					e.consume();
				}
				public void mousePressed(MouseEvent e) {
					e.consume();			
				}
				public void mouseExited(MouseEvent e) {
					e.consume();			
				}
				public void mouseEntered(MouseEvent e) {
					e.consume();			
				}
				public void mouseClicked(MouseEvent e) {
					e.consume();			
				}
			});
            
            glassPane.addMouseMotionListener(new MouseMotionListener() {
				public void mouseMoved(MouseEvent e) {
					e.consume();
				}
				public void mouseDragged(MouseEvent e) {
					e.consume();
				}
			});
            
			ProgressMonitor monitor = new ProgressMonitor(dialogOwner, "Printing " + layout.getName(), "", 0, 1);
        	final PrintWorker worker = new PrintWorker(session, job, layout);
            monitor.setMillisToPopup(0);
			ProgressWatcher watcher = new ProgressWatcher(monitor, worker) {
				@Override
				public void actionPerformed(ActionEvent evt) {
					super.actionPerformed(evt);
					if (worker.isFinished() || worker.isCancelled()) {
						JPanel glassPane = new JPanel();
						glassPane.setOpaque(false);
						parentFrame.setGlassPane(glassPane);
					}
				}
			};
			watcher.start();
			new Thread(worker).start();
        } else {
        	layout.compareAndSetCurrentlyPrinting(true, false);
        }
    }
}
