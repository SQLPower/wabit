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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.wabit.swingui.WabitIcons;

public class CopyUuidToClipboardAction extends AbstractAction implements ClipboardOwner {
	
	private final String uuid;

	public CopyUuidToClipboardAction(String uuid) {
		super("Copy unique ID to clipboard", WabitIcons.COPY_16);
		this.uuid = uuid;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(new StringSelection(uuid), this );
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// no op
	}
}
