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
import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.Nullable;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.enterprise.client.Watermarker;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.LayoutToPDF;

/**
 * This worker will write a layout as a PDF to a file.
 */
public class LayoutToPDFWorker extends SPSwingWorker {
	
	private final LayoutToPDF pdfMaker;
	private final Component dialogOwner;

	/**
	 * Creates a PDF worker which does not watermark its output.
	 * 
	 * @param registry
	 *            The swing worker registry to register with (usually the
	 *            WabitSession)
	 * @param file
	 *            The file to save to
	 * @param layout
	 *            The layout to transform into a PDF
	 * @param dialogOwner
	 *            The Component that should own all GUI dialogs generated while
	 *            making the PDF
	 */
    public LayoutToPDFWorker(SwingWorkerRegistry registry, File file, Layout layout, Component dialogOwner) {
    	this(registry, file, layout, dialogOwner, null);
    }

	/**
	 * Creates a PDF worker which watermarks its output using the given
	 * watermarker.
	 * 
	 * @param registry
	 *            The swing worker registry to register with (usually the
	 *            WabitSession)
	 * @param file
	 *            The file to save to
	 * @param layout
	 *            The layout to transform into a PDF
	 * @param dialogOwner
	 *            The Component that should own all GUI dialogs generated while
	 *            making the PDF
	 * @param watermarker
	 *            The watermarker to use. null means do not watermark.
	 */
	public LayoutToPDFWorker(SwingWorkerRegistry registry, File file, Layout layout, Component dialogOwner,
			@Nullable Watermarker watermarker) {
		super(registry);
		this.dialogOwner = dialogOwner;
		try {
			pdfMaker = new LayoutToPDF(file, layout, watermarker);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doStuff() throws Exception {
		pdfMaker.writePDF();
	}
	
	@Override
	public void cleanup() throws Exception {
		if (getDoStuffException() != null) {
			SPSUtils.showExceptionDialogNoReport(dialogOwner, "PDF Export Failed", getDoStuffException());
		}
	}

	@Override
	protected Integer getJobSizeImpl() {
		return pdfMaker.getJobSize();
	}

	@Override
	protected String getMessageImpl() {
		return pdfMaker.getMessage();
	}

	@Override
	protected int getProgressImpl() {
		return pdfMaker.getProgress();
	}

	@Override
	protected boolean hasStartedImpl() {
		return pdfMaker.hasStarted();
	}

	@Override
	protected boolean isFinishedImpl() {
		return pdfMaker.isFinished();
	}
	
}
