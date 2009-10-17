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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This worker will write a layout as a PDF to a file.
 */
public class LayoutToPDFWorker extends SPSwingWorker {
	
	public interface PDFWatermarker {
		public void watermarkPDF(Graphics g, Rectangle size);
	}
	
    private boolean cancelled;
    private int numPages;
    private int pageNum;
	private final File file;
	private final Layout layout;
	private final Component dialogOwner;
	private final PDFWatermarker watermarker;
	
	/**
	 * Tracks if this PDF worker actually was able to start writing to PDF. The
	 * PDF worker may not be able to write a layout to PDF if it is already currently
	 * printing.
	 */
    private boolean startedWriting;

    public LayoutToPDFWorker(SwingWorkerRegistry registry, File file, Layout layout, Component dialogOwner) {
    	this(registry, file, layout, dialogOwner, new PDFWatermarker(){
    		public void watermarkPDF(Graphics g, Rectangle size) {
    	        // no-op
    		}
    	});
    }
    
	public LayoutToPDFWorker(SwingWorkerRegistry registry, File file, Layout layout, Component dialogOwner, PDFWatermarker watermarker) {
		super(registry);
		this.file = file;
		this.layout = layout;
		this.dialogOwner = dialogOwner;
		this.watermarker = watermarker;
	}

	@Override
	public void doStuff() throws Exception {
		if (layout.compareAndSetCurrentlyPrinting(false, true)) {
		    startedWriting = true;
			writePDF(file, layout);
		} else {
		    startedWriting = false;
		    throw new IllegalStateException("Could not export to PDF. The layout is currently being exported. Please try again later.");
		}
	}
	
	@Override
	public void cleanup() throws Exception {
		if (startedWriting) {
			layout.compareAndSetCurrentlyPrinting(true, false);
		}
		if (getDoStuffException() != null) {
			throw new RuntimeException(getDoStuffException());
		}
	}
	
	public void writePDF(File file, Layout layout)
    throws DocumentException, FileNotFoundException, PrinterException {
    	cancelled = false;
    	pageNum = 0;

    	numPages = layout.getNumberOfPages();
    	Page page = layout.getPage();
    	OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    	Rectangle pageSize;
    	pageSize = new Rectangle(page.getWidth(), page.getHeight());

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

    	        watermarker.watermarkPDF(pdfGraphics, pageSize);
    	        
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
	}
	
	@Override 
	protected Integer getJobSizeImpl() {
		return numPages;
	}

	@Override
	protected String getMessageImpl() {
		return "Exporting page " + pageNum + ".";
	}

	@Override
	protected int getProgressImpl() {
		return pageNum;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
