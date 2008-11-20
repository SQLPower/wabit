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

package ca.sqlpower.wabit.swingui.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ca.sqlpower.wabit.report.Layout;

public class WabitPrintDialog {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Layout layout = new Layout("Test print");
                JFrame f = new JFrame("Hello World Printer");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JButton printButton = new JButton("Print Report");
                printButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPrintable(layout);
                        boolean ok = job.printDialog();
                        if (ok) {
                            try {
                                job.print();
                            } catch (PrinterException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                });
                f.add("Center", printButton);
                f.pack();
                f.setVisible(true);
            }
        });

    }
}
