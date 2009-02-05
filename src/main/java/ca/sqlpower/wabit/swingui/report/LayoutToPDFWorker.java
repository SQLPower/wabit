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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ca.sqlpower.swingui.MonitorableWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Page.PageOrientation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This worker will write a layout as a PDF to a file.
 */
public class LayoutToPDFWorker extends MonitorableWorker {
	
    private boolean started;
    private boolean finished;
    private boolean cancelled;
    private int numPages;
    private int pageNum;
	private final File file;
	private final Layout layout;

	public LayoutToPDFWorker(SwingWorkerRegistry registry, File file, Layout layout) {
		super(registry);
		this.file = file;
		this.layout = layout;
	}

	@Override
	public void doStuff() throws Exception {
		writePDF(file, layout);
	}
	
	@Override
	public void cleanup() throws Exception {
		if (getDoStuffException() != null) {
			throw new RuntimeException(getDoStuffException());
		}
	}
	
	public void writePDF(File file, Layout layout)
    throws DocumentException, FileNotFoundException, PrinterException {
    	started = true;
    	finished = false;
    	cancelled = false;
    	pageNum = 0;
    	
    	try {
    		numPages = layout.getNumberOfPages();
    		Page page = layout.getPage();
    		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    		Rectangle pageSize;
    		if (page.getOrientation() == PageOrientation.PORTRAIT) {
    			pageSize = new Rectangle(page.getWidth(), page.getHeight());
    		} else {
    			pageSize = new Rectangle(page.getHeight(), page.getWidth());
    		}

    		Document pdfDoc = new Document(pageSize, 0f, 0f, 0f, 0f);

    		PdfWriter pdfOut = PdfWriter.getInstance(pdfDoc, out);
    		pdfDoc.open();
    		pdfDoc.addCreator("Wabit " + WabitVersion.VERSION);
    		PdfContentByte pdfContent = pdfOut.getDirectContent();
    		Graphics2D pdfGraphics = null;
    		try {
    			while(pageNum < numPages) {
    				pdfGraphics = pdfContent.createGraphics(pageSize.getWidth(), pageSize.getHeight());
    				int flag = layout.print(pdfGraphics, layout.getPageFormat(pageNum), pageNum);

    				pdfGraphics.dispose();
    				pdfGraphics = null;

    				if (flag == Printable.NO_SUCH_PAGE) break;

    				pdfDoc.newPage();

    				pageNum++;
    			}
    		} finally {
    			if (pdfGraphics != null) pdfGraphics.dispose();
    			if (pdfDoc != null) pdfDoc.close();
    		}
    	} finally {
    		finished = true;
    	}
    }
	
	public Integer getJobSize() {
		return numPages;
	}

	public String getMessage() {
		return "Exporting page " + pageNum + ".";
	}

	public int getProgress() {
		return pageNum;
	}

	public boolean hasStarted() {
		return started;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
