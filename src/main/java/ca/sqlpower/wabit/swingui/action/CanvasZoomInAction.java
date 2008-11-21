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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;

public class CanvasZoomInAction extends AbstractAction {
	
	protected static final double ZOOM_CONSTANT = 0.1;
	private final PCanvas canvas;
	
	
	public CanvasZoomInAction(PCanvas canvas) {
		this.canvas = canvas;
	}

	public void actionPerformed(ActionEvent e) {
		PCamera camera = canvas.getCamera();
		camera.setViewScale(camera.getViewScale() + ZOOM_CONSTANT);
	}

}
