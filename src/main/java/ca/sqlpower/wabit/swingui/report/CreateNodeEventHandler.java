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

import javax.swing.JFrame;

import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.WabitLabel;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Guide.Axis;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.MouseState.MouseStates;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

public class CreateNodeEventHandler extends PBasicInputEventHandler {

	private final WabitSwingSession session;

	private final JFrame parentFrame;
	
	private final LayoutPanel panel;

	public CreateNodeEventHandler(WabitSwingSession session, LayoutPanel panel) {
		this.session = session;
		parentFrame = ((WabitSwingSessionContext) session.getContext()).getFrame();
		this.panel = panel;
	}

	
	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);
		if (event.isRightMouseButton()) {
			panel.setMouseState(MouseStates.READY);
			panel.getCursorManager().placeModeFinished();
		} else {
			
			if (panel.getMouseState().equals(MouseStates.CREATE_LABEL) || panel.getMouseState().equals(MouseStates.CREATE_BOX)) {
				
				ContentBox contentBox = new ContentBox();
				
				if (panel.getMouseState().equals(MouseStates.CREATE_LABEL)) {
					contentBox.setContentRenderer(new WabitLabel("New Content Box"));
				}
				
				ContentBoxNode newCBNode = 
						new ContentBoxNode(
								panel.getLayer(), 
								session, 
								parentFrame, 
								session.getWorkspace(), 
								panel, 
								contentBox);
				
				Page page = panel.getLayout().getPage();
				newCBNode.setBounds(event.getPosition().getX(), event.getPosition().getY(), (page.getRightMarginOffset() - page.getLeftMarginOffset()) / 2, panel.getPageNode().getHeight() / 10);
				panel.getPageNode().addChild(newCBNode);
			} else if (panel.getMouseState().equals(MouseStates.CREATE_HORIZONTAL_GUIDE)) {
				Guide tmpGuide = new Guide(Axis.HORIZONTAL, (int)event.getPosition().getY());
				panel.getPageNode().getModel().addGuide(tmpGuide);
				panel.getPageNode().addChild(new GuideNode(tmpGuide));
			}  else if (panel.getMouseState().equals(MouseStates.CREATE_VERTICAL_GUIDE)) {
				Guide tmpGuide = new Guide(Axis.VERTICAL, (int)event.getPosition().getX());
				panel.getPageNode().getModel().addGuide(tmpGuide);
				panel.getPageNode().addChild(new GuideNode(tmpGuide));
			}
			panel.setMouseState(MouseStates.READY);
			panel.getCursorManager().placeModeFinished();
		}
	}
}
