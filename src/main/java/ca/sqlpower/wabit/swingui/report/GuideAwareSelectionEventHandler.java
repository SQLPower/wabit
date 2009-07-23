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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.util.PBoundsLocator;

/**
 * An extension of the standard Piccolo selection handler that snaps object to guides
 * while they are being dragged.
 */
public class GuideAwareSelectionEventHandler extends PSelectionEventHandler {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(GuideAwareSelectionEventHandler.class);
    
    private double snapThreshold = 7;
    
    public GuideAwareSelectionEventHandler(PNode marqueeParent, PNode selectableParent) {
        super(marqueeParent, selectableParent);
    }

    /**
     * Overrides the normal drag handler to implement snap-to-guide behaviour.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void dragStandardSelection(PInputEvent e) {
        // There was a press node, so drag selection
        PDimension d = e.getCanvasDelta();
        e.getTopCamera().localToView(d);

        Collection<GuideNode> guides = getGuides();
        
        PDimension gDist = new PDimension();
        for (PNode node : (Collection<PNode>) getSelection()) {

            gDist.setSize(d);
            node.getParent().globalToLocal(gDist);
            node.offset(gDist.getWidth(), gDist.getHeight());
            
            for (GuideNode guide : guides) {
                guide.snap(node, snapThreshold);
            }
        }
    }
    
    @Override
    public void decorateSelectedNode(PNode node) {
        Collection<GuideNode> guides = getGuides();
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createEastLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createWestLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createNorthLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createSouthLocator(node), snapThreshold, guides));
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createNorthEastLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createNorthWestLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createSouthEastLocator(node), snapThreshold, guides)); 
        node.addChild(new GuideAwareBoundsHandle(PBoundsLocator.createSouthWestLocator(node), snapThreshold, guides));    
    }
    
    /**
     * Overrides the default behaviour by not selecting guides.
     */
    @Override
    public void select(PNode node) {
        if (! (node instanceof GuideNode)) {
            super.select(node);
        }
    }
    
    /**
     * Overrides the default behaviour by not selecting guides.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void select(Collection items) {
        List<Object> nonGuideItems = new ArrayList<Object>();
        for (Object o : items) {
            if ( ! (o instanceof GuideNode) ) {
                nonGuideItems.add(o);
            }
        }
        super.select(nonGuideItems);
    }
    
    /**
     * Returns all the guides that are direct children of the selectable parents
     * of this selection handler.
     */
    @SuppressWarnings("unchecked")
    public Collection<GuideNode> getGuides() {
        List<GuideNode> guides = new ArrayList<GuideNode>();
        for (PNode selectableParent : (Collection<PNode>) getSelectableParents()) {
            for (PNode child : (Collection<PNode>) selectableParent.getChildrenReference()) {
                if (child instanceof GuideNode) {
                    guides.add((GuideNode) child);
                }
            }
        }
        return guides;
    }
}
