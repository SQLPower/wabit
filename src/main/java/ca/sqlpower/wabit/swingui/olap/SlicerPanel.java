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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;

import com.lowagie.text.Font;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.olap.OlapQuery;

public class SlicerPanel extends JPanel {
	private final SlicerPanelDropTargetListener slicerPanelDropTargetListener = new SlicerPanelDropTargetListener();
	private final OlapQuery olapQuery;
	private final String slicerText = "Drag Dimensions, Hierarchies, Measures, and Members here";
	private JPanel item;
	private static final Border DEFAULT_BORDER = BorderFactory.createEtchedBorder();
	private static final Border DRAG_OVER_BORDER = BorderFactory.createLineBorder(Color.BLACK, 5);
	
    private static final Logger logger = Logger.getLogger(SlicerPanel.class);
	
	public SlicerPanel(OlapQuery olapQuery) {
		super();
		this.olapQuery = olapQuery;
		updatePanel();
		repaint();
		setDropTarget(new DropTarget(this, slicerPanelDropTargetListener));
	}
	
	private void updatePanel() {
		removeAll();
		Member slicerMember = olapQuery.getSlicerMember();
		if (slicerMember != null) {
			item = new JPanel();
			
			JLabel axisTitle = new JLabel("Filter: ");
			item.add(axisTitle);
			if (slicerMember instanceof Measure) {

				if (slicerMember instanceof Measure) {
					item.add(new JLabel(OlapIcons.MEASURE_ICON));
				}
				item.add(new JLabel(slicerMember.getName()));

			} else {
				String point = " > ";
				item.add(new JLabel(OlapIcons.DIMENSION_ICON));
				item.add(new JLabel(slicerMember.getDimension().getName() + point));

				item.add(new JLabel(OlapIcons.HIERARCHY_ICON));
				item.add(new JLabel(slicerMember.getHierarchy().getName() + point));

				item.add(new JLabel(OlapIcons.LEVEL_ICON));
				item.add(new JLabel(slicerMember.getLevel().getName() + point));

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
					item.add(label);
				}
			}
			
			item.setBorder(DEFAULT_BORDER);
			item.setVisible(true);
			add(item);
			item.setBackground(Color.WHITE);
		} else {
			add(new JLabel(slicerText));
			setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
			setBackground(Color.WHITE);
			CellSetTableHeaderComponent.addGreyedButtonsToPanel(this);
		}
	}
	
	
	class SlicerPanelDropTargetListener implements DropTargetListener {

		public void dragEnter(DropTargetDragEvent dtde) {
			//Don't Care
		}

		public void dragExit(DropTargetEvent dte) {
			resetUI();
		}
		
		private void resetUI() {
			if (item == null) {
				SlicerPanel.this.setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
			} else {
				item.setBorder(DEFAULT_BORDER);
			}
		}

		public void dragOver(DropTargetDragEvent dtde) {
			if (canImport(SlicerPanel.this, dtde.getCurrentDataFlavors())) {
				if (item == null) {
					SlicerPanel.this.setBorder(DRAG_OVER_BORDER);
				} else {
					item.setBorder(DRAG_OVER_BORDER);
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
                    throw new RuntimeException(e);
                }
            }
            return false;
        }
        
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//we don't care?
		}
		
	}
 
}
