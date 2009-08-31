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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A lot of the JToolBars that were being added to Wabit were coded with a lot
 * of boilerplate code. In particular, buttons were having their text set to be
 * at the bottom and centred, and having a client property set to that they
 * would render without button borders on OS X (Leopard and later (hopefully))
 * Also, each toolbar was being placed with a 'Wabit Logo' button on the right
 * that opens to the support forum. This class is intended to centralize most of
 * the boilerplate code so that making a JToolBar that looks like all the other
 * JToolBars in the Wabit application doesn't require so much repeated code.
 */
public class WabitToolBarBuilder {
	private JToolBar toolBar;
	private JToolBar wabitToolBar;
	private JToolBar buttonBar;
	
	public WabitToolBarBuilder() {
		toolBar = new JToolBar();
		toolBar.setLayout(new BorderLayout());
		toolBar.setFloatable(false);
		wabitToolBar = new JToolBar();
		wabitToolBar.setFloatable(false);
        JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		wabitToolBar.add(forumButton);
		buttonBar = new JToolBar();
		buttonBar.setFloatable(false);
		toolBar.add(wabitToolBar, BorderLayout.EAST);
		toolBar.add(buttonBar, BorderLayout.WEST);
	}
	
	/**
	 * Get the final JToolBar component. Typically you would call this once
	 * you're done adding all the buttons and components you want.
	 * 
	 * @return An instance of JToolBar with specially formatted JButtons and a
	 *         Wabit button at the end.
	 */
	public JToolBar getToolbar() {
		return toolBar;
	}
	
	/**
	 * Add an Action that will be added as a button to the toolbar.
	 * 
	 * @param a
	 *            The Action which to add to the ToolBar
	 * @param text
	 *            The text to set as the button's label
	 */
	public void add(Action a, String text) {
		JButton button = new JButton(a);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		// Remove button borders in OS X Leopard (and hopefully future OS X releases)
		button.putClientProperty("JButton.buttonType", "toolbar");
		button.setText(text);
		buttonBar.add(button);
	}
	
	/**
	 * Add a component to the toolbar. 
	 */
	public void add(Component component) {
		buttonBar.add(component);
	}
	
	/**
	 * Add a separatot to the toolbar.
	 */
	public void addSeparator() {
		buttonBar.addSeparator();
	}
}
