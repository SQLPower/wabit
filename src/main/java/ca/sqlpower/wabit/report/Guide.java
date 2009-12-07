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

package ca.sqlpower.wabit.report;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;

public class Guide extends AbstractWabitObject {

    public static enum Axis {
        VERTICAL("Vertical"),
        HORIZONTAL("Horizontal");
        
        private final String humanName;
        
        private Axis(String humanName) {
            this.humanName = humanName;
        }
        
        @Override
        public String toString() {
            return humanName;
        }
    }

    private final Axis axis;

    /**
     * The X or Y coordinate of this guide. For a horizontal guide, this is the Y coordinate;
     * for a vertical guide it's the X coordinate.
     */
    private double offset;

    public Guide(Axis axis, double offset) {
        this.axis = axis;
		setName(axis + " copy");
        setOffset(offset);
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        throw new UnsupportedOperationException("Guides don't have child nodes.");
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double guideOffset) {
        double oldOffset = this.offset;
        this.offset = guideOffset;
        firePropertyChange("offset", oldOffset, guideOffset);
        WabitWorkspace workspace = WabitUtils.getWorkspace(this);
        if (workspace != null && !workspace.isMagicDisabled()) {
        	dragSnappedEdges(oldOffset, guideOffset);
        }
    }

    public Axis getAxis() {
        return axis;
    }

    @Override
    public Page getParent() {
        return (Page) super.getParent();
    }

    /**
     * Moves all the box edges that were snapped to this guide at the old offset
     * to the given new offset.
     * <p>
     * XXX: this could be done by having snapped boxes listen to the guides they
     * were snapped to if the snapping behaviour was moved into the model (it's
     * currently in the view's GuideAwareSelection...thing)
     */
    private void dragSnappedEdges(double oldOffset, double newOffset) {
        if (getParent() == null) return;
        for (ContentBox cb : getParent().getContentBoxes()) {
            if (axis == Axis.HORIZONTAL) {
                if (oldOffset == cb.getY()) {
                    cb.setY(newOffset);
                    cb.setHeight(cb.getHeight() + (oldOffset - newOffset));
                } else if (oldOffset == (cb.getY() + cb.getHeight())) {
                    cb.setHeight(cb.getHeight() + (newOffset - oldOffset));
                }
            } else {
                if (oldOffset == cb.getX()) {
                    cb.setX(newOffset);
                    cb.setWidth(cb.getWidth() + (oldOffset - newOffset));
                } else if (oldOffset == (cb.getX() + cb.getWidth())) {
                    cb.setWidth(cb.getWidth() + (newOffset - oldOffset));
                }
            }
        }
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        //do nothing
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }    
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }

}
