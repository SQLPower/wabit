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

import javax.swing.AbstractAction;

import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Report;

public class AddContentBoxAction extends AbstractAction {

    private final Report report;
    private final PageNode addTo;

    public AddContentBoxAction(Report report, PageNode addTo) {
        super("Add Content Box");
        this.report = report;
        this.addTo = addTo;
    }
    
    public void actionPerformed(ActionEvent e) {
        ContentBoxNode newCBNode = new ContentBoxNode(new ContentBox());
        newCBNode.setBounds(addTo.getWidth() / 2, addTo.getHeight() / 2, 30, 30); // XXX should be near mouse pointer
        addTo.addChild(newCBNode);
        
        // XXX temporary
        Label l = new Label(report, "This is a label");
        newCBNode.getContentBox().setContentRenderer(l);
    }
}
