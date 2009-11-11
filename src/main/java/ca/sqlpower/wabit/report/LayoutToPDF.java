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

package ca.sqlpower.wabit.report;

import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.annotation.Nullable;

import ca.sqlpower.util.Monitorable;
import ca.sqlpower.util.MonitorableImpl;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.enterprise.client.Watermarker;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class LayoutToPDF implements Monitorable {

	private final File file;
	private final Layout layout;
	private final Watermarker watermarker;

	private final MonitorableImpl monitorableHelper = new MonitorableImpl();
	
	
	/**
	 * Creates a PDF maker which does not watermark its output.
	 * 
	 * @param file
	 *            The file to save to
	 * @param layout
	 *            The layout to transform into a PDF
	 * @param watermarker
	 *            The watermarker to use. null means do not watermark.
	 */
	public LayoutToPDF(File file, Layout layout, @Nullable Watermarker watermarker) {
		super();
		this.file = file;
		this.layout = layout;
		this.watermarker = watermarker;
	}

	public void writePDF()
    throws DocumentException, FileNotFoundException, PrinterException {
    	monitorableHelper.setStarted(true);
		int pageNum = 0;

    	int numPages = layout.getNumberOfPages();
    	monitorableHelper.setJobSize(numPages);
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
    	    	monitorableHelper.checkCancelled();
    	    	monitorableHelper.setProgress(pageNum);
    	        pdfGraphics = pdfContent.createGraphics(pageSize.getWidth(), pageSize.getHeight());
    	        int flag = layout.print(pdfGraphics, layout.getPageFormat(pageNum), pageNum);

    	        if (watermarker != null) {
    	        	java.awt.Rectangle watermarkSize = new java.awt.Rectangle();
    	        	watermarkSize.setSize(
    	        			Math.round(pageSize.getWidth()),
    	        			Math.round(pageSize.getHeight()));
    	        	watermarker.watermark(pdfGraphics, watermarkSize);
    	        }
    	        
    	        pdfGraphics.dispose();
    	        pdfGraphics = null;

    	        if (flag == Printable.NO_SUCH_PAGE) break;

    	        pdfDoc.newPage();

    	        pageNum++;
    	    }
    	} finally {
    	    if (pdfGraphics != null) pdfGraphics.dispose();
    	    if (pdfDoc != null) pdfDoc.close();
    	    monitorableHelper.setFinished(true);
    	}
	}


	public Integer getJobSize() {
		return monitorableHelper.getJobSize();
	}


	public String getMessage() {
		return monitorableHelper.getMessage();
	}


	public int getProgress() {
		return monitorableHelper.getProgress();
	}


	public boolean hasStarted() {
		return monitorableHelper.hasStarted();
	}


	public boolean isCancelled() {
		return monitorableHelper.isCancelled();
	}


	public boolean isFinished() {
		return monitorableHelper.isFinished();
	}


	public void setCancelled(boolean cancelled) {
		monitorableHelper.setCancelled(cancelled);
	}
	
	
}
