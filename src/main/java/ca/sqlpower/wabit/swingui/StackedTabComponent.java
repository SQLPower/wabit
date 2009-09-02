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

package ca.sqlpower.wabit.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * A custom JComponent to implement the idea of a 'stack' of tabs. The idea is
 * similar to the JTabbedPane, except the tabs are stacked on top of each other
 * vertically. When a 'tab' is selected, it's component is painted in between
 * the tab and the next tab (or at the every bottom of it's the last tab). The
 * API is only a subset of the {@link JTabbedPane} API, so it's not yet a full
 * drop-in replacement for JTabbedPane. It currently only implements the subset
 * of menthods that are called from {@link WabitSwingSessionContextImpl}, so
 * that it can be used in the Wabit.
 */
public class StackedTabComponent extends JComponent {
	
	/**
	 * Set of gradient colours for a selected tab
	 */
	private static final Color SELECTED_TAB_GRADIENT_TOP = new Color(255, 204, 66);
	private static final Color SELECTED_TAB_GRADIENT_BOTTOM = new Color(255, 99, 00);
	
	/**
	 * Set of gradient colours for an unselected tab
	 */
	private static final Color UNSELECTED_TAB_RADIENT_TOP = new Color(221, 221, 221);
	private static final Color UNSELECTED_TAB_GRADIENT_BOTTOM = new Color(204, 204, 204);
	
	/**
	 * A list of tabs that this component currently contains
	 */
	private List<StackedTab> tabs = new ArrayList<StackedTab>();
	
	/**
	 * The tab that is currently 'selected' 
	 */
	private StackedTab selectedTab;
	
	/**
	 * A list of change listeners that receive state change events from the
	 * {@link StackedTabComponent}. This is primarily when the selected tab has
	 * changed.
	 */
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	
	public StackedTabComponent() {
		setLayout(new MigLayout("flowy, hidemode 3, fill, ins 0, gap 0 0", "", ""));
	}
	
	/**
	 * Represents an individual 'tab'. It consists of a label component, which
	 * is the tab itself, and a subcomponent which is the component contained by
	 * the tab.
	 */
	private class StackedTab {
		/**
		 * The title of this tab
		 */
		private String title;
		
		/**
		 * A JLabel which represents the tab itself
		 */
		private JLabel label;
		
		/**
		 * The {@link Component} contained by this tab
		 */
		private final Component subComponent;
		
		/**
		 * Creates a new StackedTab with the given title and containing the
		 * given {@link Component}
		 * 
		 * @param title
		 *            The title to give to this tab
		 * @param component
		 *            The {@link Component} that this tab will contain
		 */
		public StackedTab(String title, Component component) {
			this.title = title;
			label = new JLabel(title) {
				// Override paintComponent to give it a gradient background
				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D)g;
					Color topColor = (StackedTab.this == selectedTab) ? SELECTED_TAB_GRADIENT_TOP : UNSELECTED_TAB_RADIENT_TOP; 
					Color bottomColor = (StackedTab.this == selectedTab) ? SELECTED_TAB_GRADIENT_BOTTOM : UNSELECTED_TAB_GRADIENT_BOTTOM;
					GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
					g2.setPaint(gp);
					g2.fillRect(0, 0, StackedTabComponent.this.getWidth(), getHeight());
					super.paintComponent(g);
				}
			};
			
			label.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					// Use mousePressed or mouseReleased events instead
				}

				public void mouseEntered(MouseEvent e) {
					// TODO: provide visual feedback?
				}

				public void mouseExited(MouseEvent e) {
					// TODO: provide visual feedback?				
				}

				public void mousePressed(MouseEvent e) {
					setSelectedIndex(tabs.indexOf(StackedTab.this));
				}

				public void mouseReleased(MouseEvent e) {
					// do nothing?
				}
				
			});
			this.subComponent = component;
		}
	}
	
	public void addTab(String title, Component comp) {
		final StackedTab tab = new StackedTab(title, comp);
		tab.subComponent.setVisible(false);
		tabs.add(tab);
		add(tab.label, "grow 100 0, push 100 0");
		add(tab.subComponent, "grow 100 100, push 100 100");
	}
	
	/**
	 * Set the selected tab to the one at the given index. If the index is
	 * outside the range of the current list of tabs, then selected tab is set
	 * to null.
	 */
	public void setSelectedIndex(int i) {
		StackedTab oldTab = selectedTab;
		if (oldTab != null){
			oldTab.subComponent.setVisible(false);
		}
		if (i < 0 || i >= tabs.size()) {
			selectedTab = null;
		} else {
			selectedTab = tabs.get(i);
			selectedTab.subComponent.setVisible(true);
		}
		StackedTabComponent.this.repaint();
		if (oldTab != selectedTab) {
			fireStateChanged();
		}
	}

	/**
	 * Notifies the list of {@link ChangeListener}s of a state change, usually
	 * the selected tab being changed
	 */
	private void fireStateChanged() {
		for (ChangeListener listener : changeListeners) {
			ChangeEvent e = new ChangeEvent(this);
			listener.stateChanged(e);
		}
	}

	/**
	 * Returns the current number of tabs
	 */
	public int getTabCount() {
		return tabs.size();
	}

	/**
	 * Sets the title of a given tab.
	 * 
	 * @param index
	 *            The index of the tab that we're changing the title for
	 * @param newValue
	 *            The new title for tab at the given index
	 */
	public void setTitleAt(int index, String newValue) {
		final StackedTab tab = tabs.get(index);	
		if (tab != null) {
			tab.title = newValue;
		}
	}

	/**
	 * Add a {@link ChangeListener} to listen for state changes to the tab
	 */
	public void addChangeListener(ChangeListener tabChangeListener) {
		changeListeners.add(tabChangeListener);
	}

	/**
	 * Returns the index of the first tab which has a title that matches the
	 * given string
	 * 
	 * @param string
	 *            A string to match with a tab's title
	 * @return The index of the first tab that has a title that matches the
	 *         given string. If no such tab exists, returns -1.
	 */
	public int indexOfTab(String string) {
		for (int i = 0; i < tabs.size(); i++){
			StackedTab tab = tabs.get(i);
			if (tab.title.equals(string)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the current selected tab. If no tab is selected,
	 * then returns -1
	 */
	public int getSelectedIndex() {
		return tabs.indexOf(selectedTab);
	}

	/**
	 * Removes the tab at the given index
	 * 
	 * @param i
	 *            The index of the tab to remove
	 * @throws IndexOutOfBoundsException
	 *             if i < 0 or i >= to tabs.size()
	 */
	public void removeTabAt(int i) {
		StackedTab removedTab = tabs.get(i);
		if (removedTab != null) {
			remove(removedTab.label);
			remove(removedTab.subComponent);
		}
		tabs.remove(i);
	}

	/**
	 * Returns the index of the tab that contains the given set of co-ordinates
	 * 
	 * @param x
	 *            The x co-ordinate to check
	 * @param y
	 *            The y co-ordinate to check
	 * @return The index of the tab containing the x and y co-ordinates
	 */
	public int indexAtLocation(int x, int y) {
		for (int i = 0; i < tabs.size(); i++) {
			StackedTab tab = tabs.get(i);
			if (tab.label.contains(x, y) || tab.subComponent.contains(x, y)) {
				return i;
			}
		}
		return -1;
	}
}
