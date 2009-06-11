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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.WabitUtils;

public class CellSetTableHeaderComponent extends JComponent {

	/**
	 * This is the border we give to all hierarchy components we create. The
	 * drag and drop feedback mechanism will temporarily alter the borders of
	 * components when they're being dragged over, but it will set the borders
	 * back to this one when the DnD operation has completed.
	 */
	private static final Border DEFAULT_HIERARCHYCOMP_BORDER =
		BorderFactory.createEmptyBorder(1, 1, 1, 1);
	
	/**
	 * A rounded, dashed border to use for empty axes. We've seen precedent for
	 * using a rounded, dashed box to indicate areas where you can drag and drop
	 * stuff into.
	 */
	private static final Border ROUNDED_DASHED_BORDER = new AbstractBorder() {
		private final BasicStroke DASHED_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
	            BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 7.0f, 7.0f }, 0.0f);
		
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			Color oldColour = g.getColor();
			g.setColor(Color.GRAY);
			if (g instanceof Graphics2D) {
				((Graphics2D) g).setStroke(DASHED_STROKE);
			}
			g.drawRoundRect(x, y, width-1, height-1, 20, 20);
			g.setColor(oldColour);
		}
		
		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, 0);
		}
	};
	
    private static final Logger logger = Logger.getLogger(CellSetTableHeaderComponent.class);
    
    private final class CellSetTableHeaderDropTargetListener implements
			DropTargetListener {

		private JComponent borderedComponent;
		
		public void dragEnter(DropTargetDragEvent dtde) {
			// We don't care
		}

		public void dragExit(DropTargetEvent dte) {
			if (borderedComponent != null) {
				borderedComponent.setBorder(defaultBorder);
				borderedComponent = null;
			}
		}

		public void dragOver(DropTargetDragEvent dtde) {
			if (borderedComponent != null) {
				borderedComponent.setBorder(defaultBorder);
				borderedComponent = null;
			}
			
			Point point = dtde.getLocation();
			int insertIndex = calcDropInsertIndex(point);
			Border compoundBorder;
			if (insertIndex < getComponentCount()) {
				// draw line to left/top of component
				borderedComponent = (JComponent) getComponent(insertIndex);
				if (axis == Axis.ROWS) {
					Border leftLineBorder = BorderFactory.createMatteBorder(0, 5, 0, 0, Color.BLACK);
					Border clearFillerBorder = BorderFactory.createEmptyBorder(1, 0, 1, 1);
					compoundBorder = BorderFactory.createCompoundBorder(leftLineBorder, clearFillerBorder);
				} else if (axis == Axis.COLUMNS) {
					Border topLineBorder = BorderFactory.createMatteBorder(5, 0, 0, 0, Color.BLACK);
					Border clearFillerBorder = BorderFactory.createEmptyBorder(0, 1, 1, 1);
					compoundBorder = BorderFactory.createCompoundBorder(topLineBorder, clearFillerBorder);
				} else {
					throw new IllegalStateException("Can only deal with COLUMNS and ROWS axes, but got a " + axis);
				}
			} else {
				// draw line to right/bottom of last component
				borderedComponent = (JComponent) getComponent(getComponentCount() - 1);
				if (axis == Axis.ROWS) {
					Border rightLineBorder = BorderFactory.createMatteBorder(0, 0, 0, 5, Color.BLACK);
					Border clearFillerBorder = BorderFactory.createEmptyBorder(1, 1, 1, 0);
					compoundBorder = BorderFactory.createCompoundBorder(rightLineBorder, clearFillerBorder);
				} else if (axis == Axis.COLUMNS) {
					Border bottomLineBorder = BorderFactory.createMatteBorder(0, 0, 5, 0, Color.BLACK);
					Border clearFillerBorder = BorderFactory.createEmptyBorder(1, 1, 0, 1);
					compoundBorder = BorderFactory.createCompoundBorder(bottomLineBorder, clearFillerBorder);
				} else {
					throw new IllegalStateException("Can only deal with COLUMNS and ROWS axes, but got a " + axis);
				}
			}
			if (borderedComponent == null) {
				borderedComponent = CellSetTableHeaderComponent.this;
			}
			borderedComponent.setBorder(compoundBorder);
			if (canImport(CellSetTableHeaderComponent.this, dtde.getCurrentDataFlavors())) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}

		public void drop(DropTargetDropEvent dtde) {
			if (canImport(CellSetTableHeaderComponent.this, dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(dtde.getDropAction());
				boolean success = importData(
						CellSetTableHeaderComponent.this,
						dtde.getTransferable(),
						dtde.getLocation());
				dtde.dropComplete(success);
			}
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			// we don't care?
		}
		
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            logger.debug("canImport()");
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == OlapMetadataTransferable.LOCAL_OBJECT_FLAVOUR) {
                    return true;
                }
            }
            return false;
        }

        public boolean importData(JComponent comp, Transferable t, Point p) {
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

                    fireMemberDropped(calcDropInsertIndex(p), m);
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
     * This list of hierarchies is all of the hierarchy components in the header
     * in the order they appear.
     */
    private final List<HierarchyComponent> hierarchies = new ArrayList<HierarchyComponent>();

    /**
     * This graphics object will be null if the default graphic is to be
     * obtained. If a special graphics object is being used to render this
     * component, as in printing, then this graphic object will be set to the
     * desired graphics object instead.
     */
    private final Graphics graphic;

    /**
     * If this value is set this font should be used for sizing the headers
     * instead of the default font given by the graphics object.
     */
    private final Font headerFont;

    /**
     * If this value is set this font should be used in sizing the headers
     * with the size of the header font given by the graphics object.
     */
    private final Font bodyFont;

    /**
	 * The {@link DropTargetListener} used to handle drag-n-drop functionality
	 * in the OLAP editor
	 */
    private final CellSetTableHeaderDropTargetListener dropTargetListener = new CellSetTableHeaderDropTargetListener();

    /**
     * The default {@link Border} for this {@link CellSetTableHeaderComponent}.
     */
	private final Border defaultBorder;
    
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
        this(cellSet, axis, columnModel, null, null, null);
    }

    /**
     * Creates a component for viewing the given CellSet and Axis.
     * 
     * @param cellSet
     *            The {@link CellSet} that this header component is for
     * @param axis
     *            The {@link Axis} this component is the header for
     * @param columnModel
     *            The columnModel that will be used to determine column
     *            positions in the table. If the axis type is not
     *            {@link Axis#COLUMNS}, then this can be null.
     * @param g
     *            A graphics that is different from the default graphic used by
     *            the JComponent. This allows using the component to use the
     *            header in different graphics for things like printing.
     */
    public CellSetTableHeaderComponent(CellSet cellSet, Axis axis, TableColumnModel columnModel, Graphics g, Font headerFont, Font bodyFont) {
        this.axis = axis;
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
        setDropTarget(new DropTarget(this, dropTargetListener));
        graphic = g;
        
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
			for (int i = 0; i < hierarchyCount; i++) {
				HierarchyComponent hierarchyComponent =
					new HierarchyComponent(cellSetAxis, cellSetAxis.getAxisMetaData().getHierarchies().get(i), i, columnModel);
				hierarchyComponent.setBackground(
						ColourScheme.BACKGROUND_COLOURS[i % ColourScheme.BACKGROUND_COLOURS.length]);
				hierarchies.add(hierarchyComponent);
				add(hierarchyComponent);
			}
			defaultBorder = DEFAULT_HIERARCHYCOMP_BORDER;
		} else {
			// Currently defaultBorder needs to be set before calling
			// setLabelAsEmpty() because it uses defaultborder
			defaultBorder = ROUNDED_DASHED_BORDER;
		    setLabelAsEmpty();
		}
		setBorder(defaultBorder);
    }
    
    /**
     * This method will set the header component to have the default message if
     * there are no hierarchies in this axis.
     */
    private void setLabelAsEmpty() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Drop dimensions, hierarchies, or members here");
        label.setBackground(ColourScheme.BACKGROUND_COLOURS[0]);
        label.setOpaque(true);
        label.setBorder(defaultBorder);
        add(label, BorderLayout.CENTER);
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
    	graphic = null;
    	headerFont = null;
    	bodyFont = null;
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
    		// Currently defaultBorder needs to be set before calling
			// setLabelAsEmpty() because it uses defaultborder
    		defaultBorder = ROUNDED_DASHED_BORDER;
    	    setLabelAsEmpty();
    	} else {
	    	for (int i = 0; i < hierarchiesSize; i++) {
	    		Hierarchy hierarchy = hierarchies.get(i);
	    		HierarchyComponent hierarchyComponent = new HierarchyComponent(hierarchy, i);
				hierarchyComponent.setBackground(
						ColourScheme.BACKGROUND_COLOURS[i % ColourScheme.BACKGROUND_COLOURS.length]);
	    		add(hierarchyComponent);
	    	}
	    	defaultBorder = DEFAULT_HIERARCHYCOMP_BORDER;
    	}
        setDropTarget(new DropTarget(this, dropTargetListener));
        setBorder(defaultBorder);
	}

	private int calcDropInsertIndex(Point p) {
		if (!(getComponentAt(p) instanceof HierarchyComponent)) return 0;
		HierarchyComponent hc = (HierarchyComponent) getComponentAt(p);
		int indexOfHC = Arrays.asList(getComponents()).indexOf(hc);
		if (indexOfHC == -1) {
			return 0;
		} else {
			Point hcRelativePos = SwingUtilities.convertPoint(
					CellSetTableHeaderComponent.this, new Point(p), hc);
			boolean beforeMiddle;
			if (axis == Axis.ROWS) {
				beforeMiddle = hcRelativePos.x < (hc.getWidth() / 2);
			} else if (axis == Axis.COLUMNS) {
				beforeMiddle = hcRelativePos.y < (hc.getHeight() / 2);
			} else {
				throw new IllegalStateException(
						"I only know how to deal with ROWS and COLUMNS," +
						" but this component is for " + axis);
			}
			if (beforeMiddle) {
				return indexOfHC;
			} else {
				return indexOfHC + 1;
			}
		}
	}

	/**
     * Fires a member clicked event to all axis listeners currently registered
     * on this component.
     */
    private void fireMemberClicked(Member member) {
        final MemberEvent e = new MemberEvent(
                this, MemberEvent.Type.MEMBER_CLICKED, axis, member);
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(i).memberClicked(e);
        }
    }
    
    /**
     * Fires a member dropped event to all axis listeners currently registered
     * on this component.
     * @param p 
     */
    private void fireMemberDropped(int insertIndex, Member member) {
        final MemberDroppedEvent e = new MemberDroppedEvent(
                this, MemberEvent.Type.MEMBER_DROPPED, axis, insertIndex, member);
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(i).memberDropped(e);
        }
    }
    
    
    private void fireMemberRemoved(Member member) {
        final MemberEvent e = new MemberEvent(
                this, MemberEvent.Type.MEMBER_REMOVED, axis, member);
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(i).memberRemoved(e);
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
    public static class LayoutItem {
        
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
        
        public Rectangle2D getBounds() {
            return bounds;
        }
        
        public String getText() {
            return text;
        }
        
        public Member getMember() {
            return member;
        }
    }

    /**
     * A component that renders the current state of a single Olap4j hierarchy, as
     * described by the axis of a cell set. The table header component uses one of
     * these for each hierarchy on the axis in order fully describe the positions of
     * that axis of the cell set.
     */
    public class HierarchyComponent extends JPanel {

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
            		popUpMenu.add(new AbstractAction("Remove Hierarchy '" + hierarchy.getName() + "'") {
						public void actionPerformed(ActionEvent e) {
							Member member = null;
							try {
								member = hierarchy.getDefaultMember();
							} catch (OlapException ex) {
								throw new RuntimeException("OlapException while trying to retrieve " +
										"default member of Hierarchy '" + hierarchy.getName() + "'", ex);
							}
							
							fireMemberRemoved(member);
						}
            		});
            		popUpMenu.show(HierarchyComponent.this, e.getX(), e.getY());
            	} else if (selectedMember != null) {
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
        private int rowsRowHeight;
        
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
        public HierarchyComponent(CellSetAxis axis, Hierarchy hierarchy, final int hierarchyOrdinal, TableColumnModel columnModel) {
            this.axis = axis;
            this.hierarchyOrdinal = hierarchyOrdinal;
            this.columnModel = columnModel;
            this.hierarchy = hierarchy;
            setOpaque(true);
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        /**
         * A HierarchyComponent that only contains the hierarchy name and is
         * not drilldownable.
         * @param hierarchy
         * @param hierarchyOrdinal
         */
        public HierarchyComponent(Hierarchy hierarchy, int hierarchyOrdinal) {
        	this(null, hierarchy, hierarchyOrdinal, null);
        }

        /**
         * Creates the layout of the labels if it hasn't been created yet. This is
         * called at the beginning of both {@link #paintComponent(Graphics)} and
         * {@link #getPreferredSize()}. After the first call to this method, there
         * is no effect on subsequent calls (they just return immediately).
         */
        public void createLayout() {
            if (!getLayoutItems().isEmpty() && isValid()) return;
            layoutItems.clear();
            preferredSizes.clear();

            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.setFont(getBodyFont());
            FontMetrics bodyFM = g2.getFontMetrics();
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            rowsRowHeight = Math.max(fm.getHeight(), bodyFM.getHeight());
            int colsRowHeight = fm.getHeight();
            int y = 0;

            if (axis == null) {
            	LayoutItem li = new LayoutItem();
            	Rectangle2D stringBounds = fm.getStringBounds(hierarchy.getName(), g2);
            	li.bounds = new Rectangle2D.Double(0, 0, stringBounds.getWidth(), stringBounds.getHeight());
            	try {
            		li.member = hierarchy.getDefaultMember();
            	} catch (OlapException ex) {
            		throw new RuntimeException(ex);
            	}
            	li.text = hierarchy.getName();
            	preferredSizes.add(new Dimension((int)li.bounds.getWidth(), (int)li.bounds.getHeight()));
            	layoutItems.add(li);
            } else {
            
	            int[] columnPositions = getColumnPositions();
	            for (int i = 0; i < axis.getPositionCount(); i++) {
	            	preferredSizes.add(new Dimension(0, 0));
	            }
            	
	            int shallowestDepth = 0;
	            
	            if (axis.getPositionCount() > 0) {
					// Get the depth of the shallowest member (lowest depth) so
					// that it can be positioned at the left/top most position
					// (depending on the axis) (ex. if you drag in a member
					// beneath the default member)
	            	Member shallowestMember = axis.getPositions().get(0).getMembers().get(hierarchyOrdinal);
					shallowestDepth = shallowestMember.getDepth();
	            }
	            
	            for (Position position : axis) {
	                Member member = position.getMembers().get(hierarchyOrdinal);
	                int memberDepth = member.getDepth() - shallowestDepth;
	                LayoutItem li = new LayoutItem();
	                Rectangle2D stringBounds = fm.getStringBounds(member.getName(), g2);
	                if (axis.getAxisOrdinal() == Axis.ROWS) {
		                li.bounds = new Rectangle2D.Double(
		                        memberDepth * indentAmount, y,
		                        stringBounds.getWidth(), stringBounds.getHeight());
		                y += rowsRowHeight;
	                } else if (axis.getAxisOrdinal() == Axis.COLUMNS) {
	                	li.bounds = new Rectangle2D.Double(
		                        columnPositions[position.getOrdinal()], memberDepth * colsRowHeight,
		                        stringBounds.getWidth(), stringBounds.getHeight());
	                	y += colsRowHeight;
	                }
	                li.member = member;
	                li.text = member.getName();
	                Dimension d = preferredSizes.get(position.getOrdinal());
	                d.height = (int) Math.max(d.height, li.bounds.getHeight());
	                d.width = (int) Math.max(d.width, li.bounds.getWidth());
	                layoutItems.add(li);
	                
	            }
            }
            g2.dispose();
        }
        
        @Override
        public Graphics getGraphics() {
            if (graphic != null) return graphic;
            return super.getGraphics();
        }
        
        @Override
        public Font getFont() {
            if (headerFont != null) return headerFont;
            return super.getFont();
        }
        
        public Font getBodyFont() {
            if (bodyFont != null) return bodyFont;
            return getFont();
        }

		private int[] getColumnPositions() {
			if (columnModel == null) return new int[0];
			int[] columnPositions = new int[columnModel.getColumnCount()];
            int x = 0;
            for (int i = 0; i < columnModel.getColumnCount(); i++) {
            	columnPositions[i] = x;
            	x += columnModel.getColumn(i).getWidth();
            }
			return columnPositions;
		}
        
        public Member getMemberAtPoint(Point p) {
        	if (axis == null) return null;
        	if (axis.getAxisOrdinal() == Axis.ROWS) {
        		// This is a special-case optimization for members in the row axis
        		int rowNum = p.y / rowsRowHeight;
	            if (rowNum >= getLayoutItems().size()) return null;
	            if (rowNum < 0) return null;
	            return getLayoutItems().get(rowNum).member;
        	} else {
        		for (LayoutItem item: getLayoutItems()) {
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
            for (LayoutItem li : getLayoutItems()) {
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
            for (LayoutItem li : getLayoutItems()) {
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

        /**
         * Returns an unmodifiable list of all of the {@link LayoutItem}s in
         * this hierarchy component.
         */
        public List<LayoutItem> getLayoutItems() {
            return Collections.unmodifiableList(layoutItems);
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

    /**
     * Returns an unmodifiable list of all the hierarchy components in
     * this header in the order they appear.
     */
    public List<HierarchyComponent> getHierarchies() {
        return Collections.unmodifiableList(hierarchies);
    }
}
