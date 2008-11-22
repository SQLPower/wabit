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
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Page.PageOrientation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class PDFAction extends AbstractAction {

    public static final Icon ICON = new ImageIcon(PageFormatAction.class.getResource("/icons/page_white_acrobat.png"));
    
    /**
     * Controls whether or not this action should prompt when the user attempts
     * to overwrite an existing file. The OS X file dialog does this
     * automatically, so this flag causes the overwrite prompt to be suppressed
     * on that platform.
     */
    private static final boolean PROMPT_ON_OVERWRITE = System.getProperty("mrj.version") != null;

    private final Layout layout;
    private final Component dialogOwner;

    public PDFAction(Component dialogOwner, Layout layout) {
        super("Create PDF...", ICON);
        putValue(SHORT_DESCRIPTION, "Export report as PDF");
        this.dialogOwner = dialogOwner;
        this.layout = layout;
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            FileDialog fd;
            Window owner;
            if (dialogOwner instanceof Window) {
                owner = (Window) dialogOwner;
            } else {
                owner = SwingUtilities.getWindowAncestor(dialogOwner);
            }
            if (owner instanceof Frame) {
                fd = new FileDialog((Frame) owner, "Save PDF as", FileDialog.SAVE);
            } else {
                fd = new FileDialog((Dialog) owner, "Save PDF as", FileDialog.SAVE);
            }
            File targetFile = null;
            boolean promptAgain;
            do {
                promptAgain = false;
                fd.setVisible(true);
                String dir = fd.getDirectory();
                String fileName = fd.getFile();
                if (fileName == null) return;
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName += ".pdf";
                }
                targetFile = new File(dir, fileName);
                if (PROMPT_ON_OVERWRITE && targetFile.exists()) {
                    int choice = JOptionPane.showOptionDialog(
                            owner, "The file " + targetFile + " exists.\nDo you want to replace it?",
                            "File exists", -1, JOptionPane.WARNING_MESSAGE, null,
                            new String[] { "Replace", "Cancel" }, "Replace");
                    if (choice == 0) {
                        promptAgain = false;
                    } else if (choice == 1) {
                        promptAgain = true;
                    } else if (choice == -1) {
                        return;
                    } else {
                        throw new RuntimeException("Unrecognized choice: " + choice);
                    }
                }
            } while (promptAgain);
            
            writePDF(targetFile, layout);
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void writePDF(File file, Layout layout)
    throws DocumentException, FileNotFoundException, PrinterException {
        
    	int numPages = layout.getNumberOfPages();
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
            int pageNum = 0;
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
    }
}
