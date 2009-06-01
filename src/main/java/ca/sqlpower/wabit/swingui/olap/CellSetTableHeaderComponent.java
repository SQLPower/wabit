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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.Position;
import org.olap4j.Axis.Standard;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.WabitUtils;

public class CellSetTableHeaderComponent extends JComponent {

    private static final Logger logger = Logger.getLogger(CellSetTableHeaderComponent.class);
    
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
	 * Creates a CellSetTableRowHeaderComponent for viewing the given CellSet
	 * and Axis.
	 * 
	 * @param cellSet
	 *            The {@link CellSet} that this header component is for
	 * @param axis
	 *            The {@link Axis} this component is the header for
	 * @param columnModel
	 *            The columnModel that will be used to determine column
	 *            positions in the table. If the axis type is not
	 *            {@link Axis#COLUMNS}, then this can be null.
	 */
    public CellSetTableHeaderComponent(CellSet cellSet, Axis axis, TableColumnModel columnModel) {
    	this(axis, null);
    	
    	CellSetAxis cellSetAxis = cellSet.getAxes().get(axis.axisOrdinal());
    	CellSetAxisMetaData axisMetaData = cellSetAxis.getAxisMetaData();
    	int hierarchyCount = axisMetaData.getHierarchies().size();

    	if (axis == Axis.ROWS) {
    		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	} else if (axis == Axis.COLUMNS) {
    		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	} else {
    		throw new IllegalArgumentException(
    				"Only rows and columns axes are supported, but I got " + axis);
    	}

		if (hierarchyCount > 0) {
			removeAll();
			for (int i = 0; i < hierarchyCount; i++) {
				HierarchyComponent hierarchyComponent =
					new HierarchyComponent(cellSetAxis, i, columnModel);
				hierarchyComponent.setBackground(
						ColourScheme.BACKGROUND_COLOURS[i % ColourScheme.BACKGROUND_COLOURS.length]);
				add(hierarchyComponent);
			}
		}
    }

	/**
	 * Creates a CellSetTableRowHeaderComponent without a given CellSet. This is
	 * mainly for providing an table row header for the user to drop Members,
	 * Hierarchies, or Dimensions into. If the user has already dropped in a
	 * Member, Hierarchy, or Dimension into one header, but not the other (and
	 * thus does not yet have a complete query), then the header with members in
	 * it can display its existing hierarchies so that the user can tell what
	 * they have added already.
	 * 
	 * @param axis
	 *            The {@link Axis} this component is the header for
	 * @param hierarchies
	 *            A list of Hierarchies that the user has already dropped into
	 *            the header. If the list is empty, then it will print a
	 *            label asking the user to drop a Member, Hierarchy, or
	 *            Dimension into it.
	 */
    public CellSetTableHeaderComponent(Axis axis, List<Hierarchy> hierarchies) {
    	this.axis = axis;
    	int hierarchiesSize = 0;
    	
    	if (hierarchies != null) {
    		hierarchiesSize = hierarchies.size();
    	}
    	
    	if (axis == Axis.ROWS) {
    		setLayout(new GridLayout(1, Math.max(1, hierarchiesSize)));
    	} else if (axis == Axis.COLUMNS) {
    		setLayout(new GridLayout(Math.max(1, hierarchiesSize), 1));
    	} else {
    		throw new IllegalArgumentException(
    				"Only rows and columns axes are supported, but I got " + axis);
    	}

    	if (hierarchiesSize == 0) {
    		JLabel label = new JLabel("Drop dimensions, hierarchies, or members here");
    		label.setBackground(ColourScheme.BACKGROUND_COLOURS[0]);
    		label.setOpaque(true);
    		add(label);
    	} else {
	    	for (int i = 0; i < hierarchiesSize; i++) {
	    		Hierarchy h = hierarchies.get(i);
	    		JLabel label = new JLabel(h.getName());
	    		label.setVerticalAlignment(JLabel.TOP);
	    		label.setBackground(ColourScheme.BACKGROUND_COLOURS[i]);
	    		label.setOpaque(true);
	    		add(label);
	    	}
    	}
    	
        setTransferHandler(new HeaderComponentTransferHandler());
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
            	if (e.getButton() == MouseEvent.BUTTON3) {
            		JPopupMenu popUpMenu = new JPopupMenu();
            		popUpMenu.add(new AbstractAction("Remove Dimension") {
						public void actionPerformed(ActionEvent e) {
							Object o = e.getSource();
							if (e.getSource() instanceof HierarchyComponent) {
								HierarchyComponent h = (HierarchyComponent) e.getSource();
							}
						}
            		});
            		popUpMenu.show(HierarchyComponent.this, e.getX(), e.getY());
            	}
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
         * The {@link TableColumnModel} that we'll use to determine the positions of
         * the columns in the table.
         */
        private TableColumnModel columnModel;
        
        /**
         * Number of pixels to indent per level of member nesting.
         */
        private double indentAmount = 15;

        private List<Dimension> preferredSizes = new ArrayList<Dimension>();
        
        /**
		 * Returns a {@link Dimension} that specifies the preferred size of the
		 * Member at the provided position in this HierarchyComponent.
		 * <p>
		 * Note that modifying the Dimension object returned from this method
		 * will change the sizes of the Hierarchy components. So be sure you
		 * know what you're doing if you're modifying the Dimension.
		 * 
		 * @param position
		 * @return
		 */
		public Dimension getPreferredSizeAtPosition(int position) {
			return preferredSizes.get(position);
		}
		
		/**
		 * @param axis
		 *            The {@link CellSetAxis} that this HierachyComponent is in.
		 * @param hierarchyOrdinal
		 *            The ordinal for the Hierarchy this component is for
		 * @param columnModel
		 *            The columnModel that will be used to determine column
		 *            positions in the table. If the axis type is not
		 *            {@link Axis#COLUMNS}, then this can be null.
		 */
        public HierarchyComponent(CellSetAxis axis, int hierarchyOrdinal, TableColumnModel columnModel) {
            this.axis = axis;
            this.hierarchyOrdinal = hierarchyOrdinal;
            this.columnModel = columnModel;
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
            if (!layoutItems.isEmpty() && isValid()) return;
            layoutItems.clear();
            preferredSizes.clear();
        	
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            rowHeight = fm.getHeight();
            int y = 0;
            
            int[] columnPositions = getColumnPositions();
            for (int i = 0; i < axis.getPositionCount(); i++) {
            	preferredSizes.add(new Dimension(0, 0));
            }
            	
            for (Position position : axis) {
                Member member = position.getMembers().get(hierarchyOrdinal);
                int memberDepth = member.getDepth();
                LayoutItem li = new LayoutItem();
                Rectangle2D stringBounds = fm.getStringBounds(member.getName(), g2);
                if (axis.getAxisOrdinal() == Axis.ROWS) {
	                li.bounds = new Rectangle2D.Double(
	                        memberDepth * indentAmount, y,
	                        stringBounds.getWidth(), stringBounds.getHeight());
                } else if (axis.getAxisOrdinal() == Axis.COLUMNS) {
                	li.bounds = new Rectangle2D.Double(
	                        columnPositions[position.getOrdinal()], memberDepth * rowHeight,
	                        stringBounds.getWidth(), stringBounds.getHeight());
                }
                li.member = member;
                li.text = member.getName();
                Dimension d = preferredSizes.get(position.getOrdinal());
                d.height = (int) Math.max(d.height, li.bounds.getHeight());
                d.width = (int) Math.max(d.width, li.bounds.getWidth());
                layoutItems.add(li);
                
                y += rowHeight;
            }
            
            g2.dispose();
        }

		private int[] getColumnPositions() {
			int[] columnPositions = new int[columnModel.getColumnCount()];
            int x = 0;
            for (int i = 0; i < columnModel.getColumnCount(); i++) {
            	columnPositions[i] = x;
            	x += columnModel.getColumn(i).getWidth();
            }
			return columnPositions;
		}
        
        public Member getMemberAtPoint(Point p) {
        	if (axis.getAxisOrdinal() == Axis.ROWS) {
        		// This is a special-case optimization for members in the row axis
        		int rowNum = p.y / rowHeight;
	            if (rowNum >= layoutItems.size()) return null;
	            if (rowNum < 0) return null;
	            return layoutItems.get(rowNum).member;
        	} else {
        		for (LayoutItem item: layoutItems) {
        			if (item.bounds.contains(p)) {
        				return item.member;
        			}
        		}
        		return null;
        	}
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
    
    public Dimension getMemberSize(int columnIndex) {
    	Dimension d = new Dimension(0, 0);
    	for (int i = 0; i < getComponentCount(); i++) {
    		Component component = getComponent(i);
    		if (component instanceof HierarchyComponent) {
				HierarchyComponent hierarchyComponent = (HierarchyComponent) component;
				hierarchyComponent.createLayout();
				Dimension d2 = hierarchyComponent.getPreferredSizeAtPosition(columnIndex);
				d.height = Math.max(d.height, d2.height);
				d.width = Math.max(d.width, d2.width);
			}
    	}
    	return d;
    }
}
