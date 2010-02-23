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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.Watermarker;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;

/**
 * This is a JComponent which keeps track of what page you are on
 * and prints out all the data to the screen
 */
public class ReportPrintPreviewPanel extends JComponent {
	private Layout layout;
	private int pageIndex;
	private final int pageCount;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public ReportPrintPreviewPanel(Layout layout) {
		this.layout = layout;
		pageIndex = 1;
		pageCount = layout.getNumberOfPages();
	}
	
	public void setPageIndex(int pageIndex) {
		int oldValue = this.pageIndex;
		this.pageIndex = pageIndex;
		pcs.firePropertyChange("pageIndex", oldValue, pageIndex);
	}
	
	public int getPageIndex() {
		return pageIndex;
	}
	
	public int getPageCount() {
		return pageCount;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/**
	 * This is the method which is overridden to paint the correct
	 * data to the page.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		try {
			layout.print(g, layout.getPageFormat(pageIndex - 1), pageIndex - 1);
			Watermarker watermarker = new Watermarker(WabitUtils.getWorkspace(layout).getSession());
			Page page = layout.getPage();
	    	Rectangle pageSize = new Rectangle(page.getWidth(), page.getHeight());
	        watermarker.maybeWatermark(g, pageSize);
		} catch (Exception e) {
			throw new RuntimeException(e); //TODO make this nicer for the user
		}
	}

}
