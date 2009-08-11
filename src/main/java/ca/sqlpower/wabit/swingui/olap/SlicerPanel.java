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

import java.awt.Color;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;

/**
 * This is the slicer panel which shows at the bottom of the cell set viewer
 */
public class SlicerPanel extends JPanel {
	/**
	 * This listens for drops on the panel and handles them
	 */
	private final SlicerPanelDropTargetListener slicerPanelDropTargetListener = new SlicerPanelDropTargetListener();
	
	/**
	 * Mouse handler for the panel, lets the user right click
	 */
	private final MouseHandler mouseHandler = new MouseHandler();

	/**
	 * This is the query being sliced
	 */
	private final OlapQuery olapQuery;
	
	private final static String SLICER_TEXT = "Drag Dimensions, Hierarchies, Measures, and Members here";
	
	/**
	 * This is the panel within the main bottom component of the {@link CellSetViewer}
	 * it shows the user which member is in the filter
	 */
	private JPanel slicerDisplay;
	
	private static final Border DEFAULT_BORDER = BorderFactory.createEtchedBorder();
	private static final Border DRAG_OVER_BORDER = BorderFactory.createLineBorder(Color.BLACK, 5);
	
	/**
	 * This panel is the slicer panel which shows at the bottom of the cell set viewer.
	 */
	public SlicerPanel(OlapQuery olapQuery) {
		super();
		this.olapQuery = olapQuery;
		updatePanel();
		repaint();
		setDropTarget(new DropTarget(this, slicerPanelDropTargetListener));
		this.addMouseListener(mouseHandler);
	}
	
	/**
	 * This method updates the view and sets up the panel
	 */
	private void updatePanel() {
		removeAll();
		Member slicerMember = olapQuery.getSlicerMember();
		if (slicerMember != null) {
			slicerDisplay = new JPanel();
			
			JLabel axisTitle = new JLabel("Filter By: ");
			slicerDisplay.add(axisTitle);
			if (slicerMember instanceof Measure) {

				if (slicerMember instanceof Measure) {
					slicerDisplay.add(new JLabel(OlapIcons.MEASURE_ICON));
				}
				slicerDisplay.add(new JLabel(slicerMember.getName()));

			} else {
				String point = " > ";
				slicerDisplay.add(new JLabel(OlapIcons.DIMENSION_ICON));
				slicerDisplay.add(new JLabel(slicerMember.getDimension().getName() + point));

				slicerDisplay.add(new JLabel(OlapIcons.HIERARCHY_ICON));
				slicerDisplay.add(new JLabel(slicerMember.getHierarchy().getName() + point));

				slicerDisplay.add(new JLabel(OlapIcons.LEVEL_ICON));
				slicerDisplay.add(new JLabel(slicerMember.getLevel().getName() + point));

				List<JLabel> labels = new ArrayList<JLabel>();
				point = "";
				while (slicerMember != null) {
					labels.add(0, new JLabel(slicerMember.getName() + point));

					if (slicerMember instanceof Measure) {
						labels.add(0, new JLabel(OlapIcons.MEASURE_ICON));
					}

					slicerMember = slicerMember.getParentMember();
					point = " > ";
				}
				for (JLabel label : labels) {
					slicerDisplay.add(label);
				}
			}
			
			slicerDisplay.setBorder(DEFAULT_BORDER);
			slicerDisplay.setVisible(true);
			add(slicerDisplay);
			slicerDisplay.setBackground(Color.WHITE);
		} else {
			add(new JLabel(SLICER_TEXT));
			setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
			setBackground(Color.WHITE);
			CellSetTableHeaderComponent.addGreyedButtonsToPanel(this);
		}
	}
	
	
	/**
	 * This {@link DropTargetListener} does everything to do with the drag and drop
	 * onto the slicer axis. 
	 *
	 */
	private class SlicerPanelDropTargetListener implements DropTargetListener {

		public void dragEnter(DropTargetDragEvent dtde) {
			//Don't Care
		}

		public void dragExit(DropTargetEvent dte) {
			resetUI();
		}
		
		/**
		 * This method resets the border properly after a drag has been completed
		 */
		private void resetUI() {
			if (slicerDisplay == null) {
				SlicerPanel.this.setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
			} else {
				slicerDisplay.setBorder(DEFAULT_BORDER);
			}
		}

		public void dragOver(DropTargetDragEvent dtde) {
			if (canImport(SlicerPanel.this, dtde.getCurrentDataFlavors())) {
				if (slicerDisplay == null) {
					SlicerPanel.this.setBorder(DRAG_OVER_BORDER);
				} else {
					slicerDisplay.setBorder(DRAG_OVER_BORDER);
				}
				dtde.acceptDrag(dtde.getDropAction());
			} else {
				dtde.rejectDrag();
			}
		}
		
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (DataFlavor dataFlavor : transferFlavors) {
                if (dataFlavor == OlapMetadataTransferable.LOCAL_OBJECT_ARRAY_FLAVOUR) {
                    return true;
                }
            }
            return false;
        }

		public void drop(DropTargetDropEvent dtde) {
			if (canImport(SlicerPanel.this, dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(dtde.getDropAction());
				boolean success = importData(
						dtde.getTransferable(),
						dtde.getLocation());
				dtde.dropComplete(success);
			}
			resetUI();
		}
		
        public boolean importData(Transferable t, Point p) {
            if (t.isDataFlavorSupported(OlapMetadataTransferable.LOCAL_OBJECT_ARRAY_FLAVOUR)) {
                try {
                    
                    Object[] transferDataArray = (Object[]) t.getTransferData(OlapMetadataTransferable.LOCAL_OBJECT_ARRAY_FLAVOUR);
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
                    		m = (Member) transferData;
                    	} else {
                    		return false;
                    	}

                    	olapQuery.addToAxis(0, m, Axis.FILTER);
                    }
        			resetUI();
                    return true;

                } catch (Exception e) {
                    // note: exceptions thrown here get eaten by the DnD system
        			resetUI();
                }
            }
            return false;
        }
        
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//we don't care?
		}
		
	}

	private class MouseHandler implements MouseListener {

		public void mouseMoved(MouseEvent e) {
			//Do nothing
		}

		public void mouseClicked(MouseEvent e) {
			// hey you: don't implement "click" behaviour here. Use mousePressed() or mouseReleased().
		}

		public void mouseEntered(MouseEvent e) {
			//don't care				
		}

		public void mouseExited(MouseEvent e) {
			//don't care				
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e, true);

		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e, false);

		}
	}

	private void maybeShowPopup(MouseEvent e, boolean isMousePressed) {
		final Member slicerMember = olapQuery.getSlicerMember();
		if (slicerMember != null && e.isPopupTrigger()) {
			JPopupMenu popUpMenu = new JPopupMenu();
			JMenuItem removeItem = new JMenuItem(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					try {
						olapQuery.removeHierarchy(slicerMember.getHierarchy(), Axis.FILTER);
						olapQuery.execute();
					} catch (QueryInitializationException e1) {
						SPSUtils.showExceptionDialogNoReport(SlicerPanel.this, "Error occured while initializing " +
								"the query to remove the item on the filter axis.", e1);
					} catch (OlapException e2) {
						SPSUtils.showExceptionDialogNoReport(SlicerPanel.this, "Error occured " +
								"while removing the filter.", e2);					
					}
				}
			});
			removeItem.setText("Remove filter");
			popUpMenu.add(removeItem);
			Point mousePoint = e.getPoint();
			popUpMenu.show(SlicerPanel.this, mousePoint.x, mousePoint.y);
		}
	}
 
}
