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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.SPSUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A guide is an object visible only at design time which has a particular
 * (movable) position that other nodes can snap to and be positioned relative
 * to.
 */
public class GuideNode extends PNode {

    private static final Logger logger = Logger.getLogger(GuideNode.class);
    
    public static enum Axis { VERTICAL, HORIZONTAL }
    
    private final Axis axis;
    
    private BasicStroke marginStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4f, new float[] { 12f, 12f }, 0f);

    /**
     * The X or Y coordinate of this guide. For a horizontal guide, this is the Y coordinate;
     * for a vertical guide it's the X coordinate.
     */
    private int guideOffset;
    
    /**
     * Creates a new guide node oriented as specified. The orientation of
     * a guide node can't be changed, although its position can.
     * 
     * @param axis Whether this new guide is oriented vertically or horizontally 
     */
    public GuideNode(Axis axis) {
        this.axis = axis;
        setPaint(new Color(0xdddddd));
        setPickable(false);
    }
    
    @Override
    protected void paint(PPaintContext paintContext) {
        PCamera camera = paintContext.getCamera();
        Graphics2D g2 = paintContext.getGraphics();
        
        g2.setStroke(SPSUtils.getAdjustedStroke(marginStroke, camera.getViewScale()));
        g2.setPaint(getPaint());
        g2.drawLine((int) getX(), (int) getY(), (int) (getX() + getWidth()), (int) (getY() + getHeight()));
    }
    
    public void setGuideOffset(int guideOffset) {
        this.guideOffset = guideOffset;
        adjustBoundsForParent();
    }
    
    public int getGuideOffset() {
        return guideOffset;
    }

    @Override
    public void setParent(PNode newParent) {
        logger.debug("Changing parent to " + newParent);
        if (getParent() != null) {
            getParent().removePropertyChangeListener(parentChangeHandler);
        }
        super.setParent(newParent);
        if (newParent != null) {
            adjustBoundsForParent();
            newParent.addPropertyChangeListener(parentChangeHandler);
        }
    }
    
    private void adjustBoundsForParent() {
        PNode parent = getParent();
        if (axis == Axis.HORIZONTAL) {
            setBounds((int) parent.getX(), guideOffset, (int) parent.getWidth(), 1);
        } else if (axis == Axis.VERTICAL) {
            setBounds(guideOffset, (int) parent.getY(), 1, (int) parent.getHeight());
        } else {
            throw new IllegalStateException("Unknown axis: " + axis);
        }
    }
    
    private final PropertyChangeListener parentChangeHandler = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            logger.debug("Parent change event: " + evt.getPropertyName());
            if (evt.getPropertyName().contains("bounds")) {
                adjustBoundsForParent();
            }
        }
        
    };
}
