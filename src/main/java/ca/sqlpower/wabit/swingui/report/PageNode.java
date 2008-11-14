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

import java.awt.Color;

import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Guide.Axis;
import edu.umd.cs.piccolo.PNode;

public class PageNode extends PNode {

    private static final int DPI = 72;
    
    private final Page page;
    
    // XXX do these need to be special, or can they just be regular guides?
    // to make them regular guides, we'd need to let guides listen to their
    // parent node and be defined relative to the top, bottom, left, right,
    // vmiddle, or hmiddle of that node
    private final GuideNode leftMargin = new GuideNode(Axis.VERTICAL);
    private final GuideNode rightMargin = new GuideNode(Axis.VERTICAL);
    private final GuideNode topMargin = new GuideNode(Axis.HORIZONTAL);
    private final GuideNode bottomMargin = new GuideNode(Axis.HORIZONTAL);
    
    public PageNode(Page page) {
        this.page = page;
        addChild(leftMargin);
        addChild(rightMargin);
        addChild(topMargin);
        addChild(bottomMargin);
        setBounds(0, 0, page.getWidth(), page.getHeight());
        setPaint(Color.WHITE);
    }
    
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        boolean boundsSet = super.setBounds(x, y, width, height);
        if (boundsSet) {
            page.setWidth((int) width);
            page.setHeight((int) height);
            
            leftMargin.setGuideOffset(DPI);
            rightMargin.setGuideOffset((int) (width - DPI));
            topMargin.setGuideOffset(DPI);
            bottomMargin.setGuideOffset((int) (height - DPI));
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
        } else if (child instanceof GuideNode) {
            page.addGuide(((GuideNode) child).getGuide());
        }
        // There are other types of PNodes added that the model doesn't care about (like selection handles)
    }
}
