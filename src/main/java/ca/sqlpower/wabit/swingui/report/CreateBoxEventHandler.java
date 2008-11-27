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

package ca.sqlpower.wabit.swingui.report;

import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.querypen.MouseState.MouseStates;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

public class CreateBoxEventHandler extends PBasicInputEventHandler {

	private final WabitSwingSession session;

	private final ReportLayoutPanel panel;

	public CreateBoxEventHandler(WabitSwingSession session, ReportLayoutPanel panel) {
		this.session = session;
		this.panel = panel;
	}

	
	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);
		if (event.isRightMouseButton()) {
			panel.setMouseState(MouseStates.READY);
			panel.getCursorManager().placeModeFinished();
		} else if (panel.getMouseState().equals(MouseStates.CREATE_BOX)) {
			ContentBox contentBox = new ContentBox();
			Label label = new Label(panel.getReport(), "New Content Box");
			contentBox.setContentRenderer(label);
			ContentBoxNode newCBNode = new ContentBoxNode(session.getFrame(),
					contentBox);
			newCBNode.setBounds(event.getPosition().getX(), event.getPosition()
					.getY(), (panel.getReport().getPage()
					.getRightMarginOffset() - panel.getReport().getPage()
					.getLeftMarginOffset()) / 2, panel.getPageNode()
					.getHeight() / 10);
			panel.getPageNode().addChild(newCBNode);
			panel.setMouseState(MouseStates.READY);
			panel.getCursorManager().placeModeFinished();
		}
	}
}
