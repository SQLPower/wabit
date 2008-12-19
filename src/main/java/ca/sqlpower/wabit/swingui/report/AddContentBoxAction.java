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

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

public class AddContentBoxAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(AddContentBoxAction.class);
    
    private final Layout report;
    private final PageNode addTo;

    private final WabitSwingSession session;

    public AddContentBoxAction(WabitSwingSession session, Layout report, PageNode addTo) {
        super("Add Content Box");
        this.session = session;
        this.report = report;
        this.addTo = addTo;
    }
    
    public void actionPerformed(ActionEvent e) {
        ContentBoxNode newCBNode = new ContentBoxNode(session.getFrame(), new ContentBox());
        newCBNode.setBounds(addTo.getWidth() / 2, addTo.getHeight() / 2, 30, 30); // XXX should be near mouse pointer
        addTo.addChild(newCBNode);
    }
}
