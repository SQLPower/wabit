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

package ca.sqlpower.wabit.swingui.event;

import java.awt.geom.Point2D;

import javax.swing.text.JTextComponent;

import ca.sqlpower.wabit.swingui.querypen.MouseState;
import ca.sqlpower.wabit.swingui.querypen.MouseState.MouseStates;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;

/**
 * Need to move the editing ability of the styled text editor to a mouse click
 * so we can either edit a column or drag a column.
 */
public class ExtendedStyledTextEventHandler extends PStyledTextEventHandler {

	/**
	 * Number of pixels the mouse is allowed to move between a mouse pressed and
	 * a mouse released event to be considered as a click instead of a drag.
	 */
	private static final int ALLOWED_MOVEMENT_ON_MOUSE_CLICK = 5;
	
	/**
	 * States of a mouse button.
	 */
	private enum State { MOUSE_UP, MOUSE_DOWN }
	
	/**
	 * The location where the mouse was last pressed at.
	 */
	private Point2D mousePressed;

	/**
	 * The mouse button state. This is for the left mouse button
	 * only.
	 */
	private State mouseState;
	
	/**
	 * The query pen that contains the component this handler is attached to.
	 * Used for getting the current state of the mouse.
	 */
	private MouseState pen;
	
	public ExtendedStyledTextEventHandler(MouseState pen, PCanvas canvas) {
		super(canvas);
		this.pen = pen;
		mouseState = State.MOUSE_UP;
	}
	
	public ExtendedStyledTextEventHandler(MouseState pen, PCanvas canvas, JTextComponent editor) {
		super(canvas, editor);
		this.pen = pen;
		mouseState = State.MOUSE_UP;
	}
	
	@Override
	public void mousePressed(PInputEvent e) {
		if (pen.getMouseState() == MouseStates.READY && mouseState == State.MOUSE_UP) {
			mouseState = State.MOUSE_DOWN;
			mousePressed = e.getPosition();
		}
	}
	
	@Override
	public void mouseReleased(PInputEvent e) {
		if (pen.getMouseState() == MouseStates.READY && mouseState == State.MOUSE_DOWN) {
			mouseState = State.MOUSE_UP;
			if (e.getPosition().distance(mousePressed) < ALLOWED_MOVEMENT_ON_MOUSE_CLICK) {
				super.mousePressed(e);
			}
		}
	}
}
