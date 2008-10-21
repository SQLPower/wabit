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

package ca.sqlpower.wabit.swingui.querypen;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * This is a basic checkbox for Piccolo. This is created here as Piccolo does
 * not have a checkbox in its library.
 * 
 * Although this is not currently used in the QueryPen this is planned to be used 
 * in the print out as mixing vector graphics and pixel graphics looks odd.
 */
public class PCheckBox extends PComposite {

	/**
	 * Tracks if the checkbox is checked.
	 */
	private boolean checked;
	
	/**
	 * The box drawn around the checkbox.
	 */
	private PPath outerRect;
	
	/**
	 * The checkmark line for the checkbox.
	 */
	private PPath checkmark;
	
	PInputEventListener clickListener = new PBasicInputEventHandler() {
		@Override
		public void mouseReleased(PInputEvent event) {
			checked = !checked;
			updateCheckBox();
		}

	};
	
	public PCheckBox() {
		checked = false;
		outerRect = PPath.createRectangle(0, 0, 10, 10);
		checkmark = PPath.createPolyline(new float[] {3, 5, 10}, new float[] {3, 7, -2});
		addChild(outerRect);
		addChild(checkmark);
		addInputEventListener(clickListener);
		updateCheckBox();
	}

	/**
	 * Updates the checkbox to display or hide the check mark
	 * in the checkbox.
	 */
	private void updateCheckBox() {
		if (checked) {
			checkmark.setVisible(true);
		} else {
			checkmark.setVisible(false);
		}
	}
	
	public boolean isChecked() {
		return checked;
	}
	
}
