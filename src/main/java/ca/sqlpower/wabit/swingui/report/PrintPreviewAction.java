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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Report;

/**
 * This is the action which pops up a print preview dialog which is
 * just basically a dialog with controls to change the page and a 
 * JComponent which prints all of the data on the page.
 */
public class PrintPreviewAction extends AbstractAction {
	private JDialog printPreviewDialog;
	private JTextArea pageNumberTextArea;
	private ReportPrintPreviewPanel reportPrintPreviewPanel;
	private JFrame parentFrame;
	private Layout layout;
	
	/**
	 * This listens to the page index and updates the page accordingly
	 */
	private PropertyChangeListener pageIndexListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("pageIndex")) {
				pageNumberTextArea.setText(String.valueOf(reportPrintPreviewPanel.getPageIndex()));
				pageNumberTextArea.invalidate();
				reportPrintPreviewPanel.repaint();
			}
		}
	};
	
	/**
	 * Listens to the enter key on the text area so we can update the page
	 * once a user presses enter.
	 */
	private KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			//do nothing
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				int currentPageIndex;
				try {
					currentPageIndex = Integer.parseInt(pageNumberTextArea.getText().trim());
				} catch (Exception e1) {
					//we dont care, just return
					return;
				}
				int newPageIndex;
				if (currentPageIndex < 1) {
					newPageIndex = 1;
				} else if (currentPageIndex > reportPrintPreviewPanel.getPageCount()) {
					newPageIndex = reportPrintPreviewPanel.getPageCount();
				} else {
					newPageIndex = currentPageIndex;
				}
				reportPrintPreviewPanel.setPageIndex(newPageIndex);
			}
		}

		public void keyTyped(KeyEvent e) {
			//Do nothing
		}
	
	};
	
	/**
	 * This is the action which pops up a print preview dialog which is
	 * just basically a dialog with controls to change the page and a 
	 * JComponent which prints all of the data on the page.
	 */
	public PrintPreviewAction(JFrame parentFrame, Layout layout) {
		super("Print Preview", new ImageIcon(PrintPreviewAction.class.getClassLoader().getResource("icons/32x32/printPreview.png")));
		putValue(SHORT_DESCRIPTION, "Print Preview");
		this.parentFrame = parentFrame;
		this.layout = layout;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		printPreviewDialog = new JDialog(parentFrame);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		//Main panel
		reportPrintPreviewPanel = new ReportPrintPreviewPanel(layout);
		reportPrintPreviewPanel.setPreferredSize(new Dimension(layout.getPage().getWidth(), layout.getPage().getHeight()));
		reportPrintPreviewPanel.setBackground(Color.WHITE);
		JScrollPane scrollPane = new JScrollPane();
		
		JPanel scrollPaneView = new JPanel();
		scrollPaneView.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 20));
		scrollPaneView.setBackground(Color.WHITE);
		scrollPaneView.add(reportPrintPreviewPanel);
		
		scrollPane.setViewportView(scrollPaneView);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		
		//Toolbar up top
		JToolBar pageSelectToolBar = new JToolBar();
		pageSelectToolBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		pageSelectToolBar.setFloatable(false);
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		
		JButton previousPage = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int pageIndex = reportPrintPreviewPanel.getPageIndex();
				if (pageIndex <= 1) return;
				reportPrintPreviewPanel.setPageIndex(pageIndex - 1);
			}
		});
		previousPage.setText("Previous Page");
		toolBarPanel.add(previousPage);
		
		pageNumberTextArea = new JTextArea();
		pageNumberTextArea.addKeyListener(keyListener);
		pageNumberTextArea.setText(String.valueOf(reportPrintPreviewPanel.getPageIndex()));
		pageNumberTextArea.setEditable(true);
		pageNumberTextArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		pageNumberTextArea.setPreferredSize(new Dimension(20, pageNumberTextArea.getPreferredSize().height));
		toolBarPanel.add(pageNumberTextArea);
		
		toolBarPanel.add(new JLabel(" of " + reportPrintPreviewPanel.getPageCount()));
		
		JButton nextPage = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int pageIndex = reportPrintPreviewPanel.getPageIndex();
				if (pageIndex > (reportPrintPreviewPanel.getPageCount() - 1)) return;
				reportPrintPreviewPanel.setPageIndex(pageIndex + 1);
			}
		});
		nextPage.setText("Next Page");
		toolBarPanel.add(nextPage);
		
		pageSelectToolBar.add(toolBarPanel);
		mainPanel.add(pageSelectToolBar, BorderLayout.NORTH);
		printPreviewDialog.add(mainPanel);
		
		reportPrintPreviewPanel.addPropertyChangeListener(pageIndexListener);
		printPreviewDialog.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int w = printPreviewDialog.getSize().width;
		int h = printPreviewDialog.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		printPreviewDialog.setLocation(x, y);
		printPreviewDialog.setVisible(true);
		printPreviewDialog.setTitle("Print Preview - " + layout.getName());

		printPreviewDialog.addWindowListener(closeListener);
	}
	
	private void cleanup() {
		reportPrintPreviewPanel.removePropertyChangeListener(pageIndexListener);
		pageNumberTextArea.removeKeyListener(keyListener);
		printPreviewDialog.removeWindowListener(closeListener);
		printPreviewDialog.dispose();
	}
	
	private WindowListener closeListener = new WindowListener() {
		public void windowActivated(WindowEvent e) {
			//dont care
		}

		public void windowClosed(WindowEvent e) {
			cleanup();
		}

		public void windowClosing(WindowEvent e) {
			//do nothing
		}

		public void windowDeactivated(WindowEvent e) {
			//dont care
		}

		public void windowDeiconified(WindowEvent e) {
			//dont care
		}

		public void windowIconified(WindowEvent e) {
			//dont care
		}

		public void windowOpened(WindowEvent e) {
			//dont care
		}
		
	};
}
