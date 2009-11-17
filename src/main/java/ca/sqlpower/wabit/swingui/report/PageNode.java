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
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.Watermarker;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.swingui.WabitNode;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

public class PageNode extends PNode implements WabitNode {

    private static final Logger logger = Logger.getLogger(PageNode.class);
    
    private final Page page;

    /**
     * Updates this page's bounds based on changes in the model.
     */
    private final SPListener pageChangeHandler = new AbstractSPListener() {
        public void propertyChangeImpl(PropertyChangeEvent evt) {
            logger.debug("Page " + evt.getSource() + ": " + evt.getPropertyName() +
                    " changed " + evt.getOldValue() + " -> " + evt.getNewValue(), new Exception());
            updateBoundsFromPage();
        }
    };
    
    public PageNode(WabitSwingSession session, LayoutPanel parentPanel, Page page) {
        this.page = page;
        page.addSPListener(pageChangeHandler);
        for (WabitObject pageChild : page.getChildren()) {
            if (pageChild instanceof Guide) {
                addChild(new GuideNode((Guide) pageChild));
            } else if (pageChild instanceof ContentBox) {
                logger.debug("Adding content box node for " + pageChild);
                ContentBox contentBox = (ContentBox) pageChild;
				addChild(new ContentBoxNode(session, ((WabitSwingSessionContext) session.getContext()).getFrame(), 
                        session.getWorkspace(), parentPanel, contentBox));
            } else {
                throw new UnsupportedOperationException(
                        "Don't know what view class to use for page child: " + pageChild);
            }
        }

        updateBoundsFromPage();
        setPaint(Color.WHITE);
    }
    
    @Override
    protected void paint(PPaintContext pc) {
    	super.paint(pc);
        Watermarker watermarker = 
        	new Watermarker(
        			WabitUtils.getWorkspace(page).getSession());
        watermarker.maybeWatermark(
        		pc.getGraphics(),
        		new Rectangle(page.getWidth(), page.getHeight()));
    }
    
    private void updateBoundsFromPage() {
        super.setBounds(0, 0, page.getWidth(), page.getHeight());
    }
    
    /**
     * This method sets the bounds of the page object itself. The page will
     * then fire a change event, which will cause us to update this PNode's
     * bounds to match.
     */
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
        page.setWidth((int) width);
        page.setHeight((int) height);
        return true;
    }

    /**
     * Adds the given node to this page node, and if it's a content box node,
     * also adds that node's underlying content box to this page node's
     * underlying page object.
     */
    @Override
    public void addChild(int index, PNode child) {
        super.addChild(index, child);
        if (child instanceof WabitNode && !page.getChildren().contains(((WabitNode) child).getModel())) {
            if (child instanceof ContentBoxNode) {
                page.addContentBox(((ContentBoxNode) child).getModel());
            } else if (child instanceof GuideNode) {
                page.addGuide(((GuideNode) child).getModel());
            }
            // There are other types of PNodes added that the model doesn't care about (like selection handles)
        }
    }

    public void cleanup() {
        page.removeSPListener(pageChangeHandler);
    }

    public Page getModel() {
        return page;
    }
}
