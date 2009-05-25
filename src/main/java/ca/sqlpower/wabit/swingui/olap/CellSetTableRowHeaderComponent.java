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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.WabitUtils;

public class CellSetTableRowHeaderComponent extends JComponent {

    private static final Logger logger = Logger.getLogger(CellSetTableRowHeaderComponent.class);
    
    private class HeaderComponentTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            logger.debug("canImport()");
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            logger.debug("importData("+t+")");
            if (t.isDataFlavorSupported(OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR)) {
                try {
                    
                    Object transferData = t.getTransferData(OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR);
                    Member m;
                    if (transferData instanceof org.olap4j.metadata.Dimension) {
                        org.olap4j.metadata.Dimension d = (org.olap4j.metadata.Dimension) transferData;
                        Hierarchy h = d.getDefaultHierarchy();
                        m = h.getDefaultMember();
                    } else if (transferData instanceof Hierarchy) {
                        Hierarchy h = (Hierarchy) transferData;
                        m = h.getDefaultMember();
                    } else if (transferData instanceof Member) {
                        m = (Member) transferData;
                    } else {
                        return false;
                    }

                    // TODO figure out which index the drop happened at and include it in the event
                    fireMemberDropped(m);
                    logger.debug("  -- import complete");
                    return true;

                } catch (Exception e) {
                    logger.info("Error processing drop", e);
                    // note: exceptions thrown here get eaten by the DnD system
                    return false;
                }
            }
            logger.debug("  -- import failed");
            return false;
        }
    }

    private final List<AxisListener> axisListeners = new ArrayList<AxisListener>();
    
    /**
     * Which axis this component represents. Should be either ROWS or COLUMNS.
     */
    private final Axis axis;

    /**
     * Creates a 'empty' CellSetTableRowHeaderComponent without a given CellSet.
     * This is mainly for providing an empty table row header for the user to
     * drop Members, Hierarchies, or Dimensions into.
     * 
     * @param axis The {@link Axis} this component is the header for
     */
    public CellSetTableRowHeaderComponent(Axis axis) {
    	this.axis = axis;
    	setLayout(new BorderLayout());
        JLabel label = new JLabel("Drop dimensions, hierarchies, or members here");
        label.setBackground(ColourScheme.BACKGROUND_COLOURS[0]);
        label.setOpaque(true);
		add(label, BorderLayout.CENTER);
        setTransferHandler(new HeaderComponentTransferHandler());
        
    }
    
    /**
     * Creates a CellSetTableRowHeaderComponent for viewing the given CellSet
     * and Axis.
     * 
     * @param cellSet The {@link CellSet} that this header component is for
     * @param axis The {@link Axis} this component is the header for
     */
    public CellSetTableRowHeaderComponent(CellSet cellSet, Axis axis) {
    	this(axis);
    	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        CellSetAxis rowAxis = cellSet.getAxes().get(axis.axisOrdinal());
        CellSetAxisMetaData axisMetaData = rowAxis.getAxisMetaData();
        int hierarchyCount = axisMetaData.getHierarchies().size();
        if (hierarchyCount > 0) {
        	removeAll();
        	for (int i = 0; i < hierarchyCount; i++) {
                HierarchyComponent hierarchyComponent =
                    new HierarchyComponent(rowAxis, i);
                hierarchyComponent.setBackground(
                        ColourScheme.BACKGROUND_COLOURS[i % ColourScheme.BACKGROUND_COLOURS.length]);
                add(hierarchyComponent);
            }
        }
    }

    /**
     * Fires a member clicked event to all axis listeners currently registered
     * on this component.
     */
    private void fireMemberClicked(Member member) {
        final MemberClickEvent e = new MemberClickEvent(
                this, MemberClickEvent.Type.MEMBER_CLICKED, axis, member);
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(i).memberClicked(e);
        }
    }
    
    /**
     * Fires a member dropped event to all axis listeners currently registered
     * on this component.
     */
    private void fireMemberDropped(Member member) {
        final MemberClickEvent e = new MemberClickEvent(
                this, MemberClickEvent.Type.MEMBER_DROPPED, axis, member);
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(i).memberDropped(e);
        }
    }
    
    /**
     * Adds the given axis listener to this component.
     * 
     * @param l The listener to add. Must not be null.
     */
    public void addAxisListener(AxisListener l) {
        if (l == null) throw new NullPointerException("Null listener not allowed");
        axisListeners.add(l);
    }
    
    public void removeAxisLisener(AxisListener l) {
        axisListeners.remove(l);
    }

    /**
     * Container for information relating to the layout of a hierarchy.
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

    /**
     * A component that renders the current state of a single Olap4j hierarchy, as
     * described by the axis of a cell set. The table header component uses one of
     * these for each hierarchy on the axis in order fully describe the positions of
     * that axis of the cell set.
     */
    private class HierarchyComponent extends JPanel {

        private class MouseHandler implements MouseListener, MouseMotionListener {

            public void mouseMoved(MouseEvent e) {
                setSelectedMember(getMemberAtPoint(e.getPoint()));
            }

            public void mouseClicked(MouseEvent e) {
                // hey you: don't implement "click" behaviour here. Use mousePressed() or mouseReleased().
            }

            public void mouseEntered(MouseEvent e) {
                // don't care
            }

            public void mouseExited(MouseEvent e) {
                setSelectedMember(null);
            }

            public void mousePressed(MouseEvent e) {
                if (selectedMember != null) {
                    fireMemberClicked(selectedMember);
                }
            }

            public void mouseReleased(MouseEvent e) {
                // don't care
            }

            public void mouseDragged(MouseEvent e) {
                // don't care
            }
        };
        
        private final MouseHandler mouseHandler = new MouseHandler();
        
        private final CellSetAxis axis;
        private final Hierarchy hierarchy;
        private final int hierarchyOrdinal;
        private final List<LayoutItem> layoutItems = new ArrayList<LayoutItem>();

        /**
         * The current "selected" member.
         */
        private Member selectedMember;
        
        /**
         * The height of each row, in pixels. This is the same as the font height at
         * the time {@link #createLayout()} was invoked.
         */
        private int rowHeight;
        
        /**
         * Number of pixels to indent per level of member nesting.
         */
        private double indentAmount = 15;
        
        public HierarchyComponent(CellSetAxis axis, int hierarchyOrdinal) {
            this.axis = axis;
            this.hierarchyOrdinal = hierarchyOrdinal;
            hierarchy = axis.getAxisMetaData().getHierarchies().get(hierarchyOrdinal);
            setOpaque(true);
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
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
            rowHeight = fm.getHeight();
            int y = 0;
            
            for (Position position : axis) {
                Member member = position.getMembers().get(hierarchyOrdinal);
                int memberDepth = member.getDepth();
                LayoutItem li = new LayoutItem();
                Rectangle2D stringBounds = fm.getStringBounds(member.getName(), g2);
                li.bounds = new Rectangle2D.Double(
                        memberDepth * indentAmount, y,
                        stringBounds.getWidth(), stringBounds.getHeight());
                li.member = member;
                li.text = member.getName();
                layoutItems.add(li);
                
                y += rowHeight;
            }
            
            g2.dispose();
        }
        
        public Member getMemberAtPoint(Point p) {
            int rowNum = p.y / rowHeight;
            if (rowNum >= layoutItems.size()) return null;
            if (rowNum < 0) return null;
            return layoutItems.get(rowNum).member;
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
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);
            createLayout();
            
            FontMetrics fm = g2.getFontMetrics();
            int ascent = fm.getAscent();
            
            String previousLabel = null;
            for (LayoutItem li : layoutItems) {
                if (!WabitUtils.nullSafeEquals(previousLabel, li.text)) {
                    if (li.member == selectedMember) {
                        g2.setColor(Color.BLUE);
                    }
                    
                    g2.drawString(li.text, (int) li.bounds.getX(), ((int) li.bounds.getY()) + ascent);
                    
                    if (li.member == selectedMember) {
                        g2.setColor(getForeground());
                    }
                }
                previousLabel = li.text;
                
            }
        }

        /**
         * Currently used to indicate the mouse is hovering over the given member.
         * The name "selected" isn't quite right for this property.
         */
        public void setSelectedMember(Member selectedMember) {
            this.selectedMember = selectedMember;
            repaint();
        }

    }
}
