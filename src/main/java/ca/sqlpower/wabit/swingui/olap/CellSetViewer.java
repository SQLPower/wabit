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

package ca.sqlpower.wabit.swingui.olap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.olap4j.CellSet;
import org.olap4j.query.RectangularCellSetFormatter;

public class CellSetViewer {

    private static final Logger logger = Logger
            .getLogger(CellSetViewer.class);
    
    private final JPanel viewerComponent = new JPanel(new BorderLayout());
    private final JTable table;
    private final JScrollPane scrollPane;
    private final JLabel messageLabel = new JLabel("", JLabel.CENTER);
    
    public CellSetViewer() {
        viewerComponent.setPreferredSize(new Dimension(640, 480));
        table = new JTable();
        scrollPane = new JScrollPane(table);
        showMessage("No query defined");
    }

    public void showCellSet(CellSet cellSet) {
        if (logger.isDebugEnabled()) {
            logger.debug("Showing cell set:");
            RectangularCellSetFormatter f = new RectangularCellSetFormatter(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
            f.format(cellSet, pw);
            pw.flush();
        }
        table.setModel(new CellSetTableModel(cellSet));
        JComponent rowHeader = new CellSetTableRowHeaderComponent(cellSet);
        scrollPane.setRowHeaderView(rowHeader);
        
        if (scrollPane.getParent() == null) {
            viewerComponent.removeAll();
            viewerComponent.add(scrollPane, BorderLayout.CENTER);
            viewerComponent.revalidate();
        }
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
        if (messageLabel.getParent() == null) {
            viewerComponent.removeAll();
            viewerComponent.add(messageLabel, BorderLayout.CENTER);
            viewerComponent.revalidate();
        }
    }
    
    public JComponent getViewComponent() {
        return viewerComponent;
    }
}
