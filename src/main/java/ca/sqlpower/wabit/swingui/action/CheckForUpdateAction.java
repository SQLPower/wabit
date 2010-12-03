/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitVersion;

/**
 * This action will check for a newer version of this product so a user knows
 * they can/should update.
 */
public class CheckForUpdateAction extends AbstractAction {

	private final static String UPDATER_URL = 
			"http://wabit.googlecode.com/svn/trunk/doc/currentVersion.xml";
	
	private final JFrame owner;
	
	public CheckForUpdateAction(JFrame owner) {
		this.owner = owner;
	}
	
	public CheckForUpdateAction(String caption, JFrame owner) {
		super(caption);
		this.owner = owner;
	}
	
	public void actionPerformed(ActionEvent event) {
		SPSUtils.checkForUpdate(owner, "SQL Power Wabit", WabitVersion.VERSION, UPDATER_URL, false, true, null);
	}
	
	public static void checkForUpdate(JFrame owner) {
		SPSUtils.checkForUpdate(owner, "SQL Power Wabit", WabitVersion.VERSION, UPDATER_URL, true, true, null);
	}
	
}
