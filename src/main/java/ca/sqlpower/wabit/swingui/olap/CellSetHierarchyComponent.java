/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.wabit.swingui.olap;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

/**
 * A component that renders the current state of a single Olap4j hierarchy, as
 * described by the axis of a cell set. The table header component uses one of
 * these for each hierarchy on the axis in order fully describe the positions of
 * that axis of the cell set.
 */
public class CellSetHierarchyComponent extends JPanel {

    /**
     * Container for information relating to the layout of this hierarchy.
     * Instances of this class are created in createLayout().
     */
    private static class LayoutItem {
        
        /**
         * The bounds of the label, given this component's font.
         */
        private Rectangle2D bounds;
        
        /**
         * The text of the label to render.
         */
        private String text;
        
        /**
         * The Olap4j Member this label represents.
         */
        private Member member;
    }
    
    private final CellSetAxis axis;
    private final Hierarchy hierarchy;
    private final int hierarchyOrdinal;
    private final List<LayoutItem> layoutItems = new ArrayList<LayoutItem>();
    
    /**
     * Number of pixels to indent per level of member nesting.
     */
    private double indentAmount = 15;
    
    public CellSetHierarchyComponent(CellSetAxis axis, int hierarchyOrdinal) {
        this.axis = axis;
        this.hierarchyOrdinal = hierarchyOrdinal;
        hierarchy = axis.getAxisMetaData().getHierarchies().get(hierarchyOrdinal);
        setOpaque(true);
    }

    /**
     * Creates the layout of the labels if it hasn't been created yet. This is
     * called at the beginning of both {@link #paintComponent(Graphics)} and
     * {@link #getPreferredSize()}. After the first call to this method, there
     * is no effect on subsequent calls (they just return immediately).
     */
    private void createLayout() {
        
        if (!layoutItems.isEmpty()) return;
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        double y = fm.getAscent();
        
        for (Position position : axis) {
            Member member = position.getMembers().get(hierarchyOrdinal);
            int memberDepth = 0;
            Member ancestor = member;
            do {
                memberDepth++;
                ancestor = ancestor.getParentMember();
            } while (ancestor != null);
            LayoutItem li = new LayoutItem();
            Rectangle2D stringBounds = fm.getStringBounds(member.getName(), g2);
            li.bounds = new Rectangle2D.Double(
                    memberDepth * indentAmount, y,
                    stringBounds.getWidth(), stringBounds.getHeight());
            li.member = member;
            li.text = member.getName();
            layoutItems.add(li);
            
            y += fm.getHeight();
        }
        
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        createLayout();
        Dimension ps = new Dimension();
        for (LayoutItem li : layoutItems) {
            ps.width = (int) Math.max(li.bounds.getX() + li.bounds.getWidth(), ps.width);
            ps.height = (int) Math.max(li.bounds.getY() + li.bounds.getHeight(), ps.height);
        }
        return ps;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        createLayout();
        for (LayoutItem li : layoutItems) {
            g.drawString(li.text, (int) li.bounds.getX(), (int) li.bounds.getY());
        }
    }
}
