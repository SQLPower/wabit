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

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.wabit.report.Page;

/**
 * An action that invokes a native page format dialog which allows the
 * user to modify the page size and rotation of a given page.
 */
public class PageFormatAction extends AbstractAction {

    public static final Icon ICON = new ImageIcon(PageFormatAction.class.getResource("/icons/32x32/settings.png"));
    
    private final Page page;

    public PageFormatAction(Page page) {
        super("Page Format...", ICON);
        putValue(SHORT_DESCRIPTION, "Change paper size and orientation");
        this.page = page;
    }
    
    public void actionPerformed(ActionEvent e) {
        PageFormat pageFormat = PrinterJob.getPrinterJob().pageDialog(page.getPageFormat());
        page.applyPageFormat(pageFormat);
    }
}
