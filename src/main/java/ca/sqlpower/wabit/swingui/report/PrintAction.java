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
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ProgressMonitor;

import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.report.Layout;

public class PrintAction extends AbstractAction {
	
	public class PrintWorker extends MonitorableWorker {
		
		private boolean started;
		private boolean finished;
		private final PrinterJob job;
		private final int jobSize;
		private final Layout printingLayout;
		private int progress;

		public PrintWorker(SwingWorkerRegistry registry, PrinterJob job, Layout layout) {
			super(registry);
			printingLayout = layout;
			this.jobSize = layout.getNumberOfPages();
			started = false;
			finished = false;
			progress = 0;
			this.job = job;
		}

		@Override
		public void doStuff() throws Exception {
			started = true;
            job.print();
		}
		
		@Override
		public void cleanup() throws Exception {
			finished = true;
			if (getDoStuffException() != null) {
				throw new RuntimeException(getDoStuffException());
			}
		}

		public Integer getJobSize() {
			return jobSize;
		}

		public String getMessage() {
			return null;
		}

		public int getProgress() {
			Object progressObject = printingLayout.getVariableValue(Layout.PAGE_NUMBER, progress);
			if (progressObject instanceof Integer) {
				progress = ((Integer) progressObject).intValue();
			}
			return progress;
		}

		public boolean hasStarted() {
			return started;
		}

		public boolean isFinished() {
			return finished;
		}
		
	}

    public static final Icon ICON = new ImageIcon(PageFormatAction.class.getResource("/icons/printer.png"));
    private final Layout layout;
    private final Component dialogOwner;
	private final SwingWorkerRegistry registry;

    public PrintAction(Layout layout, Component dialogOwner, SwingWorkerRegistry registry) {
        super("Print...", ICON);
		this.registry = registry;
        putValue(SHORT_DESCRIPTION, "Print Report");
        this.layout = layout;
        this.dialogOwner = dialogOwner;
    }
    
    public void actionPerformed(ActionEvent e) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPageable(layout);
        boolean ok = job.printDialog();
        if (ok) {
        	int jobSize = layout.getNumberOfPages();
			ProgressMonitor monitor = new ProgressMonitor(dialogOwner, "Printing " + layout.getName(), "", 0, jobSize);
        	PrintWorker worker = new PrintWorker(registry, job, layout);
			ProgressWatcher.watchProgress(monitor, worker);
			new Thread(worker).start();
        }
    }
}
