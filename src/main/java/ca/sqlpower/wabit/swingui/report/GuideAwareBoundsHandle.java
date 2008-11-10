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

import java.util.Collection;

import javax.swing.SwingConstants;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class GuideAwareBoundsHandle extends PBoundsHandle {

    /**
     * When the dragged edge(s) of the node come within this distance (in global
     * pixels) of a guide, that edge will snap to the guide.
     */
    private final int threshold;
    private final Collection<GuideNode> guides;

    public GuideAwareBoundsHandle(PBoundsLocator locator, int threshold, Collection<GuideNode> guides) {
        super(locator);
        this.threshold = threshold;
        this.guides = guides;
    }

    @Override
    public void dragHandle(PDimension localDimension, PInputEvent event) {
        super.dragHandle(localDimension, event);
        
        PBoundsLocator l = (PBoundsLocator) getLocator();
        int side = l.getSide();
        
        PNode n = l.getNode();
        PBounds b = n.getGlobalBounds();

        for (GuideNode guide : guides) {
            PBounds guideBounds = guide.getGlobalBounds();
            
            //left
            if (side == SwingConstants.NORTH_WEST || side == SwingConstants.WEST || side == SwingConstants.SOUTH_WEST) {
                int leftDistance = (int) Math.abs(b.getX() - guideBounds.getX());
                if (leftDistance < threshold) {
                    b.width += b.getX() - guideBounds.getX();
                    b.x = guideBounds.getX();
                }
            }

            // right
            if (side == SwingConstants.NORTH_EAST || side == SwingConstants.EAST || side == SwingConstants.SOUTH_EAST) {
                int rightDistance = (int) Math.abs(b.getX() + b.getWidth() - guideBounds.getX());
                if (rightDistance < threshold) {
                    b.width = guideBounds.getX() - b.getX();
                }
            }

            // top
            if (side == SwingConstants.NORTH_WEST || side == SwingConstants.NORTH || side == SwingConstants.NORTH_EAST) {
                int topDistance = (int) Math.abs(b.getY() - guideBounds.getY());
                if (topDistance < threshold) {
                    b.height += b.getY() - guideBounds.getY();
                    b.y = guideBounds.getY();
                }
            }

            // bottom
            if (side == SwingConstants.SOUTH_WEST || side == SwingConstants.SOUTH || side == SwingConstants.SOUTH_EAST) {
                int bottomDistance = (int) Math.abs(b.getY() + b.getHeight() - guideBounds.getY());
                if (bottomDistance < threshold) {
                    b.height = guideBounds.getY() - b.getY();
                }
            }
        }
        
        n.globalToLocal(b);
        n.setBounds(b);
    }
}
