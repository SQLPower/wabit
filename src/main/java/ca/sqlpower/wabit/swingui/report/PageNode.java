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

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.report.Page;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

public class PageNode extends PNode {

    private Color marginColour = new Color(0xdddddd);
    private BasicStroke marginStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4f, new float[] { 12f, 12f }, 0f);
    private final Page page;
    
    public PageNode(Page page) {
        this.page = page;
        setBounds(0, 0, page.getWidth(), page.getHeight());
        setPaint(Color.WHITE);
    }
    
    @Override
    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
        PCamera camera = paintContext.getCamera();
        Graphics2D g2 = paintContext.getGraphics();
        
        g2.setColor(marginColour);
        g2.setStroke(SPSUtils.getAdjustedStroke(marginStroke, camera.getViewScale()));
        g2.drawLine(page.getLeftMargin(), 0, page.getLeftMargin(), page.getHeight());
        g2.drawLine(page.getWidth() - page.getRightMargin(), 0, page.getWidth() - page.getRightMargin(), page.getHeight());
        g2.drawLine(0, page.getTopMargin(), page.getWidth(), page.getTopMargin());
        g2.drawLine(0, page.getHeight() - page.getBottomMargin(), page.getWidth(), page.getHeight() - page.getBottomMargin());
        // TODO create guide node class and make margins guides
        
    }
    
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        boolean boundsSet = super.setBounds(x, y, width, height);
        if (boundsSet) {
            page.setWidth((int) width);
            page.setHeight((int) height);
        }
        return boundsSet;
    }

    /**
     * Adds the given node to this page node, and if it's a content box node,
     * also adds that node's underlying content box to this page node's
     * underlying page object.
     */
    @Override
    public void addChild(int index, PNode child) {
        super.addChild(index, child);
        if (child instanceof ContentBoxNode) {
            page.addContentBox(((ContentBoxNode) child).getContentBox());
        }
    }
}
