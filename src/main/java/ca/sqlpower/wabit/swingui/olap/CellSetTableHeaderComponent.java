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
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.query.Selection;
import org.olap4j.query.SortOrder;

import ca.sqlpower.swingui.ColoredIcon;
import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.olap.action.ClearExclusionsAction;
import ca.sqlpower.wabit.swingui.olap.action.ClearSortFromAxisAction;
import ca.sqlpower.wabit.swingui.olap.action.DrillReplaceAction;
import ca.sqlpower.wabit.swingui.olap.action.DrillUpAction;
import ca.sqlpower.wabit.swingui.olap.action.ExcludeMemberAction;
import ca.sqlpower.wabit.swingui.olap.action.RemoveHierarchyAction;
import ca.sqlpower.wabit.swingui.olap.action.SortByMeasureAction;

/**
 * A Component to be used as the header component in the CellSetViewer.
 */
public class CellSetTableHeaderComponent extends JComponent {

	public class CellSetTableCornerComponent extends JComponent {
		
		private List<Dimension> preferredSizes = new ArrayList<Dimension>();
		
		/**
		 * Only the containing CellSetTableHeaderComponent instance will
		 * instantiate the corner component. Use
		 * {@link CellSetTableHeaderComponent#getCornerComponent()} to get the
		 * corner component reference
		 */
		private CellSetTableCornerComponent() {
			//do nothing
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
	        super.paintComponent(g2);
	        
	        FontMetrics fm = g2.getFontMetrics();
	        Font oldFont = g2.getFont();
	        Font italicFont = oldFont.deriveFont(Font.ITALIC);
	        Color oldColor = g2.getColor();
	        
	        g2.setColor(ColourScheme.HEADER_COLOURS[3]);
	        g2.fillRect(0, 0, getWidth(), getHeight());
	        
	        try {
	        	g2.setFont(italicFont);
		        int x = 0;
				ImageIcon dimensionIcon = OlapIcons.DIMENSION_ICON;
		        
				for (HierarchyComponent h: hierarchies) {
					String hierarchyName = h.getHierarchy().getDimension().getName();
					g2.setColor(h.getBackground());
					g2.fillRect(x, getHeight() - Math.max(fm.getHeight(), dimensionIcon.getIconHeight()), 
							Math.max(h.getWidth(), dimensionIcon.getIconWidth() + (int) fm.getStringBounds(hierarchyName, g2).getWidth()), Math.max(fm.getHeight(), dimensionIcon.getIconHeight()));
					g2.setColor(oldColor);
					dimensionIcon.paintIcon(this, g2, x, getHeight() - dimensionIcon.getIconHeight());
	        		g2.drawString(hierarchyName, x + dimensionIcon.getIconWidth(), getHeight() - fm.getDescent());
	        		x += Math.max(h.getWidth(), dimensionIcon.getIconWidth() + (int) fm.getStringBounds(hierarchyName, g2).getWidth());
				}
	        } finally {
	        	g2.setFont(oldFont);
	        	g2.setColor(oldColor);
	        }
		}
	}
	
	private CellSetTableCornerComponent cornerComponent;
	
	/**
	 * A constant for the size of padding to be added to the left and right
	 * sides of a layout item, and in particular between a layout item's text
	 * and the edge of a HierarchyComponent.
	 */
	private static final int LAYOUT_ITEM_PADDING = 5;
	
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
	public static final Border ROUNDED_DASHED_BORDER = new AbstractBorder() {
		private final BasicStroke DASHED_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
	            BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 7.0f, 7.0f }, 0.0f);
		
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			Color oldColour = g.getColor();
			g.setColor(new Color(221, 221, 221));
			Stroke oldStroke = null;
			if (g instanceof Graphics2D) {
				oldStroke = ((Graphics2D) g).getStroke();
				((Graphics2D) g).setStroke(DASHED_STROKE);
			}
			g.drawRoundRect(x + 5 , y + 5 , width-11, height-11, 20, 20);
			if (g instanceof Graphics2D) {
				((Graphics2D) g).setStroke(oldStroke);
			}
			g.setColor(oldColour);
		}
		
		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, 0);
		}
	};
	
	/**
	 * The UIDefault value for the expanded JTree icon. Used to denote expanded Members
	 */
	private static final Icon EXPANDED_TREE_ICON = UIManager.getDefaults().getIcon("Tree.expandedIcon");
	
	/**
	 * The UIDefault value for the collapsed JTree icon. Used to denote collapsed Members
	 */
	private static final Icon COLLAPSED_TREE_ICON = UIManager.getDefaults().getIcon("Tree.collapsedIcon");
	
    private static final Logger logger = Logger.getLogger(CellSetTableHeaderComponent.class);
    
    private final class CellSetTableHeaderDropTargetListener implements
			DropTargetListener {

		private JComponent borderedComponent;
		
		public void dragEnter(DropTargetDragEvent dtde) {
			// We don't care
		}

		public void dragExit(DropTargetEvent dte) {
			resetUIAfterDrag();
		}

		private void resetUIAfterDrag() {
			if (borderedComponent != null) {
				borderedComponent.setBorder(defaultBorder);
				borderedComponent = null;
			}
		}

		public void dragOver(DropTargetDragEvent dtde) {
			resetUIAfterDrag();
			if (canImport(CellSetTableHeaderComponent.this, dtde.getCurrentDataFlavors())) {
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
				return;
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
		}

		public void drop(DropTargetDropEvent dtde) {
			if (canImport(CellSetTableHeaderComponent.this, dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(dtde.getDropAction());
				boolean success = importData(
						CellSetTableHeaderComponent.this,
						dtde.getTransferable(),
						dtde.getLocation());
				if (!success) {
					resetUIAfterDrag();
				}
				dtde.dropComplete(success);
			}
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			// we don't care?
		}
		
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            logger.debug("canImport()");
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR) {
                    return true;
                }
            }
            return false;
        }

        public boolean importData(JComponent comp, Transferable t, Point p) {
            logger.debug("importData("+t+")");
            if (t.isDataFlavorSupported(OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR)) {
                try {
                    
                    Object[] transferDataArray = (Object[]) t.getTransferData(OlapMetadataTransferable.OLAP_ARRAY_FLAVOUR);
                    for (Object transferData : transferDataArray) {
                    	Member m;
                    	if (transferData instanceof org.olap4j.metadata.Dimension) {
                    		org.olap4j.metadata.Dimension d = (org.olap4j.metadata.Dimension) transferData;
                    		Hierarchy h = d.getDefaultHierarchy();
                    		m = h.getDefaultMember();
                    	} else if (transferData instanceof Hierarchy) {
                    		Hierarchy h = (Hierarchy) transferData;
                    		m = h.getDefaultMember();
                    	} else if (transferData instanceof Member) {
                    		m =(Member) transferData;
                    	} else if (transferData instanceof Level) {
                    		Level level = (Level) transferData;
                    		for (Member member : level.getMembers()) {
                    			query.addToAxis(calcDropInsertIndex(p), member, axis);
                    		}
                    		continue;
                    	} else {
                    		return false;
                    	}

                    	query.addToAxis(calcDropInsertIndex(p), m, axis);
                    }
                    logger.debug("  -- import complete");
                    execute(query);
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
     * This is the amount of vertical space that will be used up by each row label.
     * Users of this component are responsible for synchronizing this value with
     * whatever row height the body of the table has.
     */
    private float rowHeight;

    /**
	 * The {@link DropTargetListener} used to handle drag-n-drop functionality
	 * in the OLAP editor
	 */
    private final CellSetTableHeaderDropTargetListener dropTargetListener = new CellSetTableHeaderDropTargetListener();

    /**
     * The default {@link Border} for this {@link CellSetTableHeaderComponent}.
     */
	private final Border defaultBorder;

    private final OlapQuery query;

	/**
	 * Creates a CellSetTableRowHeaderComponent for viewing the given CellSet
	 * and Axis.
	 * 
     * @param session
     *            the WabitSwingSession this component belongs to. Must not be
     *            null.
	 * @param query
	 *            The query that generated the cell set. This query will be
	 *            manipulated by the various drill down, up, replace,
	 *            across, through, over, under across the woods to
	 *            grandmother's house we go!
	 *            <p>
	 *            Can be null (for example, if the cell set was obtained by
	 *            direct execution of an MDX statement), but in that case no
	 *            query manipulations will be offered to the person viewing
	 *            the cell set.
	 * @param cellSet
	 *            The {@link CellSet} that this header component is for
	 * @param axis
	 *            The {@link Axis} this component is the header for
	 * @param table
	 *            The table this row header is for. We will attach a listener to
	 *            this table so we can track its row height. The table's columnModel
	 *            will be used to determine column positions in the table.
	 */
    public CellSetTableHeaderComponent(OlapQuery query, CellSet cellSet, Axis axis, JTable table) {
        this(query, cellSet, axis, table, null, null);
    }

    /**
     * Creates a component for viewing the given CellSet and Axis.
     * 
     * @param session
     *            the WabitSwingSession this component belongs to. Must not be
     *            null.
     * @param query
     *            The query that generated the cell set. This query will be
     *            manipulated by the various drill down, up, replace,
     *            across, through, over, under across the woods to
     *            grandmother's house we go!
     *            <p>
     *            Can be null (for example, if the cell set was obtained by
     *            direct execution of an MDX statement), but in that case no
     *            query manipulations will be offered to the person viewing
     *            the cell set.
     * @param cellSet
     *            The {@link CellSet} that this header component is for
     * @param axis
     *            The {@link Axis} this component is the header for
	 * @param table
	 *            The table this row header is for. We will attach a listener to
	 *            this table so we can track its row height. The table's columnModel
	 *            will be used to determine column positions in the table.
     * @param g
     *            A graphics that is different from the default graphic used by
     *            the JComponent. This allows using the component to use the
     *            header in different graphics for things like printing.
     */
    public CellSetTableHeaderComponent(OlapQuery query, CellSet cellSet, Axis axis, final JTable table, Graphics g, Font headerFont) {
        this.query = query;
        this.axis = axis;
        setRowHeight(table.getRowHeight());
        
        PropertyChangeListener rowHeightSyncher = new PropertyChangeListener() {
        	public void propertyChange(PropertyChangeEvent evt) {
        		setRowHeight(table.getRowHeight());
        	}
        };
        
        table.addPropertyChangeListener("rowHeight", rowHeightSyncher);
        
        if (headerFont != null) {
        	setFont(headerFont);
        }
        setDropTarget(new DropTarget(this, dropTargetListener));
        graphic = g;
        
    	CellSetAxis cellSetAxis = cellSet.getAxes().get(axis.axisOrdinal());
    	CellSetAxisMetaData axisMetaData = cellSetAxis.getAxisMetaData();
    	int hierarchyCount = axisMetaData.getHierarchies().size();

    	if (axis == Axis.ROWS) {
    		cornerComponent = new CellSetTableCornerComponent();
    		for (int i = 0; i < hierarchyCount; i++) {
    			cornerComponent.preferredSizes.add(new Dimension(0, 0));
    		}
    		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	} else if (axis == Axis.COLUMNS) {
    		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	} else {
    		throw new IllegalArgumentException(
    				"Only rows and columns axes are supported, but I got " + axis);
    	}

		if (hierarchyCount > 0 && cellSetAxis.getPositionCount() > 0) {
			for (int i = 0; i < hierarchyCount; i++) {
				Hierarchy hierarchy = cellSetAxis.getAxisMetaData().getHierarchies().get(i);
				HierarchyComponent hierarchyComponent =
					new HierarchyComponent(
							cellSetAxis, hierarchy,
							i, table.getColumnModel());
				hierarchyComponent.setBackground(
						ColourScheme.HEADER_COLOURS[i % ColourScheme.HEADER_COLOURS.length]);
				hierarchies.add(hierarchyComponent);
				add(hierarchyComponent);
			}
			defaultBorder = DEFAULT_HIERARCHYCOMP_BORDER;
		} else if (cellSetAxis.getPositionCount() == 0) {
			defaultBorder = ROUNDED_DASHED_BORDER;
		    setLabelAsEmptyNoPositions(cellSetAxis.getAxisMetaData().getHierarchies());
		} else {
			// Currently defaultBorder needs to be set before calling
			// setLabelAsEmpty() because it uses defaultborder
			defaultBorder = ROUNDED_DASHED_BORDER;
		    setLabelAsEmptyNoHierarchies();
		}
		setBorder(defaultBorder);
    }
    
    /**
     * This method will set the header component to have the default message if
     * there are no hierarchies in this axis.
     */
    private void setLabelAsEmptyNoHierarchies() {
    	setLayout(new BorderLayout());
    	JPanel panel = new JPanel(new MigLayout("flowy, align 50% 50%, ins 20", "align center", ""));
    	panel.setBackground(Color.WHITE);
    	String axisString = null;
    	if (axis == Axis.COLUMNS) {
    		axisString = "Columns Axis:";
    	} else if (axis == Axis.ROWS) {
    		axisString = "Rows Axis:";
    	}
    	JLabel axisLabel = new JLabel(axisString);
    	axisLabel.setFont(axisLabel.getFont().deriveFont(Font.BOLD));
    	panel.add(axisLabel, SwingConstants.CENTER);
    	JLabel label = new JLabel("Drag Dimensions, Hierarchies,", SwingConstants.CENTER);
    	panel.add(label);
    	label = new JLabel("Measures, and Members here", SwingConstants.CENTER);
    	panel.add(label);
    	
    	addGreyedButtonsToPanel(panel);
    	
    	panel.setBorder(defaultBorder);
    	add(panel, BorderLayout.CENTER);
    }
    
	/**
	 * This method will set the header component to have a default message if
	 * the axis contains no positions (due to all members being omitted or
	 * excluded)
	 * 
	 * @param hierarchies
	 *            A list of Hierarchies used to clear all exclusions if necessary
	 */
    private void setLabelAsEmptyNoPositions(final List<Hierarchy> hierarchies) {
    	setLayout(new BorderLayout());
    	JPanel panel = new JPanel(new MigLayout("flowy, align 50% 50%, ins 20", "align center", ""));
    	panel.setBackground(Color.WHITE);
    	
    	JLabel label = new JLabel("No members in this axis");
    	panel.add(label);
    	label = new JLabel("Try unchecking 'Omit Empty Rows' or clicking ");
    	panel.add(label);
    	label = new JLabel("<html><a href=\"\">here to Clear All Exceptions</a></html>");
    	label.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mousePressed(MouseEvent e) {
    			for (Hierarchy h: hierarchies) {
    				try {
    					query.clearExclusions(h);
    					execute(query);
    				} catch (QueryInitializationException ex) {
    					throw new RuntimeException("Error while clearing all exclusions", ex);
    				} catch (OlapException ex) {
    					throw new RuntimeException("Error while clearing all exclusions", ex);
    				} catch (SQLException ex) {
    				    throw new RuntimeException("Error while clearing all exclusions", ex);
    				} catch (InterruptedException ex) {
    				    logger.info("OlapQuery execution was canceled before it took place", ex);
                    }
    			}
    		}
    	});
    	panel.add(label);
    	
    	panel.setBorder(defaultBorder);
    	add(panel, BorderLayout.CENTER);
    }

	public static void addGreyedButtonsToPanel(JPanel panel) {
		JPanel iconPanel = new JPanel();
    	iconPanel.setOpaque(false);
    	iconPanel.add(new JLabel(new ColoredIcon(OlapIcons.DIMENSION_ICON, Color.LIGHT_GRAY, 0.9f)));
    	iconPanel.add(new JLabel(new ColoredIcon(OlapIcons.HIERARCHY_ICON, Color.LIGHT_GRAY, 0.9f)));
    	iconPanel.add(new JLabel(new ColoredIcon(OlapIcons.MEASURE_ICON, Color.LIGHT_GRAY, 0.9f)));
    	panel.add(iconPanel);
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
     * @param session
     *            the WabitSwingSession this component belongs to. Must not be
     *            null.
	 * @param query
	 *            The query that things dropped on the axis component should be
	 *            added to.
	 * @param axis
	 *            The {@link Axis} this component is the header for
	 */
    public CellSetTableHeaderComponent(OlapQuery query, Axis axis) throws QueryInitializationException {
        this.query = query;
    	this.axis = axis;
    	graphic = null;
    	int hierarchiesSize = 0;
    	
    	List<Hierarchy> hierarchies = null;
    	
    	if (axis == Axis.ROWS) {
    	    hierarchies = query.getRowHierarchies();
    		if (hierarchies != null) {
    			hierarchiesSize = hierarchies.size();
    			setLayout(new GridLayout(1, Math.max(1, hierarchiesSize)));
    		}
    	} else if (axis == Axis.COLUMNS) {
    	    hierarchies = query.getColumnHierarchies();
    		if (hierarchies != null) {
    			hierarchiesSize = hierarchies.size();
    			setLayout(new GridLayout(Math.max(1, hierarchiesSize), 1));
    		}
    	} else {
    		throw new IllegalArgumentException(
    				"Only rows and columns axes are supported, but I got " + axis);
    	}

    	if (hierarchiesSize == 0) {
    		// Currently defaultBorder needs to be set before calling
			// setLabelAsEmpty() because it uses defaultborder
    		defaultBorder = ROUNDED_DASHED_BORDER;
    	    setLabelAsEmptyNoHierarchies();
    	} else {
	    	for (int i = 0; i < hierarchiesSize; i++) {
	    		Hierarchy hierarchy = hierarchies.get(i);
	    		HierarchyComponent hierarchyComponent = new HierarchyComponent(hierarchy, i);
				hierarchyComponent.setBackground(
						ColourScheme.HEADER_COLOURS[i % ColourScheme.HEADER_COLOURS.length]);
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
            	maybeShowPopup(e, selectedMember);
            }

            public void mouseEntered(MouseEvent e) {
                // don't care
            }

            public void mouseExited(MouseEvent e) {
                setSelectedMember(null);
            }

            public void mousePressed(MouseEvent e) {
            	boolean showedPopup = maybeShowPopup(e, selectedMember);
            	if (!showedPopup && selectedMember != null && e.getButton() == MouseEvent.BUTTON1) {
            		try {
            			if (selectedMember instanceof Measure) {
            				Axis sortAxis = axis.getAxisOrdinal() == Axis.ROWS ? Axis.COLUMNS : Axis.ROWS;
							SortOrder order = query.getSortOrder(sortAxis);
            				SortOrder newOrder;
            				if (order == SortOrder.ASC) {
            					newOrder = SortOrder.DESC;
            				} else {
            					newOrder = SortOrder.ASC;
            				}
            				query.sortBy(sortAxis, newOrder, (Measure) selectedMember);
            			} else {
            				query.toggleMember(selectedMember);
            			}
            			OlapGuiUtil.asyncExecute(query, evilDigUpSession());
            		} catch (Exception ex) {
	            		throw new RuntimeException("Database error while trying to execute the OLAP query", ex);
            		}
            	}
            }
            
            public void mouseReleased(MouseEvent e) {
            	maybeShowPopup(e, selectedMember);
            }

			/**
			 * Shows a pop-up menu if the MouseEvent is a pop-up trigger for the
			 * running platform.
			 * 
			 * @param e
			 *            The MouseEvent
			 * @param clickedOnMember
			 *            The Member being clicked on
			 * @return True if MouseEvent was a pop-up trigger. False if not.
			 */
			private boolean maybeShowPopup(MouseEvent e, final Member clickedOnMember) {
				if (e.isPopupTrigger()) {
			        WabitSwingSession session = evilDigUpSession();
			        if (session == null) {
			            throw new IllegalStateException(
                                "Got a popup trigger event, but the current query " +
                                "apparently doesn't belong to a Swing session!");
			        }
					JPopupMenu popUpMenu = new JPopupMenu();
					popUpMenu.add(new RemoveHierarchyAction(session, query, hierarchy, CellSetTableHeaderComponent.this.axis));
				    popUpMenu.add(new ClearExclusionsAction(session, query, hierarchy));
				    popUpMenu.add(new ClearSortFromAxisAction(session, query, axis.getAxisOrdinal()));
				    if (clickedOnMember != null && 
							!(clickedOnMember instanceof Measure)) {
					    popUpMenu.addSeparator();
					    popUpMenu.add(new ExcludeMemberAction(
					            session,
					            query,
					            clickedOnMember,
					            Selection.Operator.MEMBER));
					    popUpMenu.add(new ExcludeMemberAction(
					            session,
					            query,
					            clickedOnMember,
					            Selection.Operator.CHILDREN));
					    popUpMenu.addSeparator();
						popUpMenu.add(new DrillReplaceAction(session, query, clickedOnMember));
						Member parentMember = clickedOnMember.getParentMember();
						try {
							if (parentMember != null && !query.isIncluded(parentMember)) {
								popUpMenu.add(new DrillUpAction(session, query, clickedOnMember, parentMember));
								try {
									Member rootMember = hierarchy.getRootMembers().get(0);
									if (!parentMember.equals(rootMember)  && !query.isIncluded(rootMember)) {
										popUpMenu.add(new DrillUpAction(session, query, clickedOnMember, rootMember));
									}
								} catch (OlapException ex) {
									throw new RuntimeException(
											"OLAP error occured while trying to get Root Member of hierarchy " 
											+ hierarchy.getName(), ex);
								}
							}
						} catch (QueryInitializationException ex) {
							throw new RuntimeException(
									"OLAP error occured while initializing the OLAP query", ex);
						}
					} else if (clickedOnMember != null && clickedOnMember instanceof Measure) {
						 popUpMenu.addSeparator();
						 Axis sortAxis = axis.getAxisOrdinal() == Axis.ROWS ? Axis.COLUMNS : Axis.ROWS;
						 JMenuItem sortAsc = new JMenuItem(new SortByMeasureAction(session, "Sort Ascending", query, 
								 clickedOnMember, sortAxis, SortOrder.ASC));
						 JMenuItem sortDesc = new JMenuItem(new SortByMeasureAction(session, "Sort Descending", query, 
								 clickedOnMember, sortAxis, SortOrder.DESC));
						 JMenuItem sortBAsc = new JMenuItem(new SortByMeasureAction(session, "Sort Ascending (Break Hiearachy)", query, 
								 clickedOnMember, sortAxis, SortOrder.BASC));
						 JMenuItem sortBDesc = new JMenuItem(new SortByMeasureAction(session, "Sort Descending (Break Hierarchy)", query, 
								 clickedOnMember, sortAxis, SortOrder.BDESC));
						 popUpMenu.add(sortAsc);
						 popUpMenu.add(sortDesc);
						 popUpMenu.add(sortBAsc);
						 popUpMenu.add(sortBDesc);
					}
					popUpMenu.show(HierarchyComponent.this, e.getX(), e.getY());
					return true;
				}
				return false;
			}

            public void mouseDragged(MouseEvent e) {
            	maybeShowPopup(e, selectedMember);
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
         * The {@link TableColumnModel} that we'll use to determine the positions of
         * the columns in the table.
         */
        private TableColumnModel columnModel;
        
        /**
         * Number of pixels to indent per level of member nesting.
         */
        private double indentAmount = Math.max(15, Math.max(EXPANDED_TREE_ICON.getIconWidth(), EXPANDED_TREE_ICON.getIconWidth()));

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
         * @param hierarchy
         *            The hierarchy that this Component represents. Must not be
         *            null.
         * @param hierarchyOrdinal
         *            The index of <tt>hierarchy</tt> within <tt>axis</tt>.
         * @param columnModel
         *            The columnModel that will be used to determine column
         *            positions in the table. If the axis type is not
         *            {@link Axis#COLUMNS}, then this can be null.
         */
        public HierarchyComponent(CellSetAxis axis, Hierarchy hierarchy, final int hierarchyOrdinal, TableColumnModel columnModel) {
			
            // HierarchyComponent may exceed the default maximum size of
			// Short.MAX_VALUE x Short.MAX_VALUE, so we have to increase it or
			// it won't display properly.
        	this.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        	
        	this.axis = axis;
            this.hierarchyOrdinal = hierarchyOrdinal;
            this.columnModel = columnModel;
            this.hierarchy = hierarchy;
            if (hierarchy == null) {
                throw new NullPointerException("Null hierarchy not allowed");
            }
            setOpaque(true);
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }
        
        /**
         * Creates a HierarchyComponent that only displays the hierarchy name and is
         * not drilldownable.
         * 
         * @param hierarchy
         *            The hierarchy that this Component represents. Must not be null.
         * @param hierarchyOrdinal
         *            The index of <tt>hierarchy</tt> within <tt>axis</tt>.
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
            Graphics2D g2 = (Graphics2D) getGraphics();
            if (g2 == null || ((!getLayoutItems().isEmpty()) && isValid())) return;
            layoutItems.clear();
            preferredSizes.clear();

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int colsRowHeight = Math.max(fm.getHeight(), EXPANDED_TREE_ICON.getIconHeight());
            int y = 0;

            if (axis == null) {
            	LayoutItem li = new LayoutItem();
            	Rectangle2D stringBounds = fm.getStringBounds(hierarchy.getName(), g2);
            	li.bounds = new Rectangle2D.Double(0, 0, stringBounds.getWidth() + OlapIcons.HIERARCHY_ICON.getIconWidth() + LAYOUT_ITEM_PADDING, stringBounds.getHeight());
            	try {
            		li.member = hierarchy.getDefaultMember();
            	} catch (OlapException ex) {
            		throw new RuntimeException(ex);
            	}
            	li.text = hierarchy.getName();
            	preferredSizes.add(new Dimension((int)li.bounds.getWidth(), (int)li.bounds.getHeight()));
            	layoutItems.add(li);
            } else {
            	if (axis.getAxisOrdinal() == Axis.ROWS) {
            		Rectangle2D stringBounds = fm.getStringBounds(hierarchy.getName(), g2);
            		Dimension d = cornerComponent.preferredSizes.get(hierarchyOrdinal);
            		d.setSize(OlapIcons.HIERARCHY_ICON.getIconWidth() + (int)stringBounds.getWidth() 
            				+ CellSetTableHeaderComponent.LAYOUT_ITEM_PADDING, 
            				(int)stringBounds.getHeight());
            	}
            	
	            int[] columnPositions = getColumnPositions();
	            for (int i = 0; i < axis.getPositionCount(); i++) {
	            	preferredSizes.add(new Dimension(0, 0));
	            }
            	
	            Member shallowestMember = null;
	            if (axis.getPositionCount() > 0) {
					// Get the depth of the shallowest member (lowest depth) so
					// that it can be positioned at the left/top most position
					// (depending on the axis) (ex. if you drag in a member
					// beneath the default member)
	            	for (Position p: axis.getPositions()) {
	            		Member member = p.getMembers().get(hierarchyOrdinal);
	            		if (shallowestMember == null || member.getDepth() < shallowestMember.getDepth()) {
	            			shallowestMember = member;
	            		}
	            	}
	            }
	            
	            for (Position position : axis) {
	                Member member = position.getMembers().get(hierarchyOrdinal);
	                int memberDepth = member.getDepth() - (shallowestMember == null ? 0 : shallowestMember.getDepth());
	                LayoutItem li = new LayoutItem();
	                Rectangle2D stringBounds = fm.getStringBounds(member.getName(), g2);
	                if (axis.getAxisOrdinal() == Axis.ROWS) {
	                	if (member.getChildMemberCount() > 0) {
	                		double height = Math.max(stringBounds.getHeight(), EXPANDED_TREE_ICON.getIconHeight());
							double width = stringBounds.getWidth() + Math.max(height, EXPANDED_TREE_ICON.getIconWidth()) + LAYOUT_ITEM_PADDING;
							li.bounds = new Rectangle2D.Double(
			                        memberDepth * indentAmount, y,
			                        width,
			                        height);
	                	} else {
			                li.bounds = new Rectangle2D.Double(
			                        memberDepth * indentAmount, y,
			                        stringBounds.getWidth() + (LAYOUT_ITEM_PADDING * 2), stringBounds.getHeight());
	                	}
		                y += rowHeight;
	                } else if (axis.getAxisOrdinal() == Axis.COLUMNS) {
	                	if (member.getChildMemberCount() > 0) {
	                		double height = Math.max(stringBounds.getHeight(), EXPANDED_TREE_ICON.getIconHeight());
							double width = stringBounds.getWidth() + Math.max(height, EXPANDED_TREE_ICON.getIconWidth());
	                		li.bounds = new Rectangle2D.Double(
			                        columnPositions[position.getOrdinal()], (memberDepth + 1) * colsRowHeight,
			                        width, 
			                        height);
	                	} else {
		                	li.bounds = new Rectangle2D.Double(
			                        columnPositions[position.getOrdinal()], (memberDepth + 1) * colsRowHeight,
			                        stringBounds.getWidth(), stringBounds.getHeight());
	                	}
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
        		int rowNum = (int) (p.y / rowHeight);
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
            if (axis != null && axis.getAxisOrdinal() == Axis.ROWS) {
            	ps.width = cornerComponent.preferredSizes.get(hierarchyOrdinal).width;
            }
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
            
            if (axis != null && axis.getAxisOrdinal() == Axis.COLUMNS) {
            	OlapIcons.DIMENSION_ICON.paintIcon(this, g2, 0, 0);
            	Font oldFont = g2.getFont();
            	Font italicFont = oldFont.deriveFont(Font.ITALIC);
            	Color oldColor = g2.getColor();
            	try {
            		g2.setFont(italicFont);
            		g2.drawString(getHierarchy().getDimension().getName(), OlapIcons.DIMENSION_ICON.getIconWidth(), ascent);
            		g2.setColor(Color.WHITE);
            		g2.drawLine(0, fm.getHeight(), getWidth(), fm.getHeight());
            	} finally {
            		g2.setFont(oldFont);
            		g2.setColor(oldColor);
            	}
            }
            
            LayoutItem previousLabel = null;
            
            List<LayoutItem> layoutItems = getLayoutItems();
            for (int i = 0 ; i < layoutItems.size() ; i++) {
            	LayoutItem li = layoutItems.get(i);
            	if (previousLabel == null || !WabitUtils.nullSafeEquals(previousLabel.text, li.text) || 
            			!previousLabel.member.getLevel().equals(li.member.getLevel())) {
            		if (li.member == selectedMember) {
            			g2.setColor(Color.BLUE);
            		}
            		if (axis != null && li.member.getChildMemberCount() > 0) {
            			Icon icon;
						// in a crossjoin, the next layout item may contain the
						// same member, so we have to check the next after.
            			LayoutItem next = null;
            			int j = i ;
            			while (j + 1 < layoutItems.size() && (next == null || next.member.equals(li.member))) {
            				j++;
            				next = layoutItems.get(j);
            			}	
        				if (next != null && next.member.getParentMember() != null && next.member.getParentMember().equals(li.member)) {
            				icon = EXPANDED_TREE_ICON;
            			} else {
            				icon = COLLAPSED_TREE_ICON;
            			}
            			int x, y;
            			if (icon.getIconHeight() < li.bounds.getHeight()) {
            				y = (int) li.bounds.getCenterY() - icon.getIconHeight()/2;
            			} else {
            				y = (int) li.bounds.getY();
            			}
            			
            			if (icon.getIconWidth() < li.bounds.getHeight()) {
            				x = (int) (li.bounds.getX() + (li.bounds.getHeight() - icon.getIconHeight())/2);
            			} else {
            				x = (int) li.bounds.getX();
            			}
            			icon.paintIcon(this, g2, x, y);
            			g2.drawString(li.text, (int) (li.bounds.getX() + Math.max(icon.getIconWidth(), li.getBounds().getHeight())), ((int) li.bounds.getY()) + ascent);
            		} else {
            			g2.drawString(li.text, (int) li.bounds.getX() + LAYOUT_ITEM_PADDING, ((int) li.bounds.getY()) + ascent);
            		}
            		
            		if (li.member == selectedMember) {
            			g2.setColor(getForeground());
            		}
            	}
            	previousLabel = li;
            }
            logger.debug("maximum size = " + this.getMaximumSize());
            logger.debug("x = " + this.getX() + ", " +
            			 "y = " + this.getY() + ", " +
            			 "location = " + this.getLocation() + ", " +
            			 "width = " + this.getWidth() + ", " +
            			 "height = " + this.getHeight());
        }

        /**
         * Currently used to indicate the mouse is hovering over the given member.
         * The name "selected" isn't quite right for this property.
         */
        public void setSelectedMember(Member selectedMember) {
        	logger.debug("selectedMember set to " + selectedMember);
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
        
        /**
		 * The Hierarchy this component represents.
         */
		public Hierarchy getHierarchy() {
			return hierarchy;
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
    
    public float getRowHeight() {
		return rowHeight;
	}
    
    public void setRowHeight(float rowHeight) {
		this.rowHeight = rowHeight;
		revalidate();
	}

	public CellSetTableCornerComponent getCornerComponent() {
		if (cornerComponent == null) {
			cornerComponent = new CellSetTableCornerComponent();
		}
		return cornerComponent;
	}

    /**
     * Executes the given query in the background if it belongs to a
     * WabitSwingSession (which is a worker registry). Otherwise, just executes
     * the query on the current thread.
     * 
     * @param query
     *            The query to execute
     * @throws InterruptedException
     *             if the current thread is interrupted while waiting for its
     *             turn to execute the given OlapQuery.
     * @throws SQLException
     *             If there is a problem iterating over the results when
     *             converting the CellSet to a ResultSet.
     */
    private void execute(OlapQuery query) throws QueryInitializationException, InterruptedException, SQLException {
        WabitSwingSession session = evilDigUpSession();
        if (session != null) {
            OlapGuiUtil.asyncExecute(query, session);
        } else {
            query.executeOlapQuery();
        }
    }

    /**
     * The following code is evil, and intended to be temporary. It would be
     * much better if CellSetTableHeaderComponent was given a WabitSwingSession
     * in its constructor, but due to early design decisions, this is not
     * currently feasible.
     * 
     * @return the WabitSwingSession the current query belongs to, or null if
     *         the query belongs to some other kind of session (or no session at
     *         all).
     */
    private WabitSwingSession evilDigUpSession() {
        WabitSwingSession session = null;
        WabitObject wo = query;
        while (wo.getParent() != null) {
            wo = wo.getParent();
        }
        if (wo instanceof WabitWorkspace) {
            WabitSession owningSession = ((WabitWorkspace) wo).getSession();
            if (owningSession instanceof WabitSwingSession) {
                session = (WabitSwingSession) owningSession;
            }
        }
        return session;
    }
}
