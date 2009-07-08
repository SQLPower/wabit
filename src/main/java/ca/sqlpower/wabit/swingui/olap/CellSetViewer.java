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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.query.Query;
import org.olap4j.query.RectangularCellSetFormatter;

import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;

public class CellSetViewer {

    private static final Logger logger = Logger
            .getLogger(CellSetViewer.class);
    
    private final JPanel viewerComponent = new JPanel(new BorderLayout());
    private final JTable table;
    private final JScrollPane scrollPane;
    private final JLabel messageLabel = new JLabel("", JLabel.CENTER);
    private final List<AxisListener> axisListeners = new ArrayList<AxisListener>();
    private int minColumnWidth = 5;

    /**
     * This is the cell set most recently displayed in this viewer. This will be
     * null until the first cell set is displayed.
     */
    private CellSet cellSet;

    /**
     * Rebroadcasts axis events to listeners of this viewer.
     */
    private final AxisListener axisEventHandler = new AxisListener() {
        public void memberClicked(MemberEvent e) {
            fireMemberClickedEvent(e);
        }

        public void memberDropped(MemberDroppedEvent e) {
        	logger.debug("AxisListener memberDropped");
            fireMemberDroppedEvent(e);
        }

		public void memberRemoved(MemberEvent e) {
			fireMemberRemovedEvent(e);
		}

        public void memberExcluded(MemberEvent e) {
            fireMemberExcludedEvent(e);
        }
    };

    /**
     * If false the listeners that allow expanding and collapsing members
     * when clicking on the headers will be removed.
     */
    private final boolean allowMemberModification;

    /**
     * This is the row header placed on the current table.
     */
    private CellSetTableHeaderComponent rowHeader;
    
    public CellSetViewer() {
        this(true);
    }
    
    public CellSetViewer(boolean allowMemberModification) {
        this.allowMemberModification = allowMemberModification;
        viewerComponent.setPreferredSize(new Dimension(640, 480));
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        showMessage("No query defined");
        viewerComponent.add(scrollPane);
    }

    public void showCellSet(CellSet cellSet) {
        if (logger.isDebugEnabled()) {
            logger.debug("Showing cell set:");
            RectangularCellSetFormatter f = new RectangularCellSetFormatter(true);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
            f.format(cellSet, pw);
            pw.flush();
        }
        
        // note: we have attached our axis handler to the existing row header component,
        // but it's going away now and its old reference to us should be of no consequence.
        // So we are not going to go to the trouble of digging it out of the scrollpane here.
        
        table.setModel(new CellSetTableModel(cellSet));
        setCellSet(cellSet);
        rowHeader = new CellSetTableHeaderComponent(cellSet, Axis.ROWS, table);
        rowHeader.addAxisListener(axisEventHandler);
        
        final CellSetTableHeaderComponent columnHeader = new CellSetTableHeaderComponent(cellSet, Axis.COLUMNS, table);
        columnHeader.addAxisListener(axisEventHandler);
        
        if (!allowMemberModification) {
            for (HierarchyComponent hierarchy : rowHeader.getHierarchies()) {
                for (MouseListener l : hierarchy.getMouseListeners()) {
                    hierarchy.removeMouseListener(l);
                }
                for (MouseMotionListener l : hierarchy.getMouseMotionListeners()) {
                    hierarchy.removeMouseMotionListener(l);
                }
            }
            for (HierarchyComponent hierarchy : columnHeader.getHierarchies()) {
                for (MouseListener l : hierarchy.getMouseListeners()) {
                    hierarchy.removeMouseListener(l);
                }
                for (MouseMotionListener l : hierarchy.getMouseMotionListeners()) {
                    hierarchy.removeMouseMotionListener(l);
                }
            }
        }
       
        scrollPane.setViewportView(table);
    	scrollPane.setRowHeaderView(rowHeader);
    	scrollPane.setColumnHeaderView(columnHeader);
    	TableCellRenderer defaultRenderer = new TableCellRenderer() {
    		public Component getTableCellRendererComponent(JTable table,
    				Object value, boolean isSelected, boolean hasFocus,
    				int row, int column) {
    			Dimension d = columnHeader.getMemberSize(column);
    			JLabel label = new JLabel();
    			label.setPreferredSize(d);
    			return label;
    		}
    	};
    	TableCellRenderer bodyRenderer = new DefaultTableCellRenderer() {
    		@Override
    		public Component getTableCellRendererComponent(JTable table,
    				Object value, boolean isSelected, boolean hasFocus,
    				int row, int column) {
    			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
    					row, column);
    			label.setHorizontalAlignment(JLabel.RIGHT);
    			return label;
    		}
    	};
    	for (int i = 0; i < table.getColumnCount(); i++) {
    		table.setDefaultRenderer(table.getColumnClass(i), bodyRenderer);
    	}
    	table.getTableHeader().setDefaultRenderer(defaultRenderer);
    	TableUtils.fitColumnWidths(table, minColumnWidth, -1, 5); //The max width is set to -1 to not use the max width when resizing columns.
    }

    public void showMessage(String message) {
    	showMessage(message, null, null);
    }
    
	public void showMessage(String message, List<Hierarchy> rowHierarchies, List<Hierarchy> columnHierarchies) {
        messageLabel.setText(message);
        
        // This line needs to be called before setting the column header. If it
		// is called afterwards, it removes the column header that was just
		// added, resulting in a missing column header.
        scrollPane.setViewportView(messageLabel);
        
        CellSetTableHeaderComponent rowHeader = new CellSetTableHeaderComponent(Axis.ROWS, rowHierarchies);
        rowHeader.addAxisListener(axisEventHandler);
        scrollPane.setRowHeaderView(rowHeader);
        
        CellSetTableHeaderComponent columnHeader = new CellSetTableHeaderComponent(Axis.COLUMNS, columnHierarchies);
        columnHeader.addAxisListener(axisEventHandler);
        scrollPane.setColumnHeaderView(columnHeader);
	}
    
    public JComponent getViewComponent() {
        return viewerComponent;
    }
    
    /**
     * Fires a member clicked event to all axis listeners currently registered
     * on this viewer.
     */
    private void fireMemberClickedEvent(MemberEvent e) {
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(0).memberClicked(e);
        }
    }

    /**
     * Fires a member clicked event to all axis listeners currently registered
     * on this viewer.
     */
    private void fireMemberDroppedEvent(MemberDroppedEvent e) {
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(0).memberDropped(e);
        }
    }

    /**
     * Fires a member removed event to all axis listeners currently registered
     * on this viewer.
     */
    private void fireMemberRemovedEvent(MemberEvent e) {
    	for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(0).memberRemoved(e);
        }
	}
    
    private void fireMemberExcludedEvent(MemberEvent e) {
        for (int i = axisListeners.size() - 1; i >= 0; i--) {
            axisListeners.get(0).memberExcluded(e);
        }
    }
    
    /**
     * Adds the given axis listener to this component. Note that source field on
     * events received from this viewer will (unfortunately) be an internal
     * component, and the actual object identity of the source may change from
     * time to time.
     * 
     * @param l
     *            The listener to add. Must not be null.
     */
    public void addAxisListener(AxisListener l) {
        if (l == null) throw new NullPointerException("Null listener not allowed");
        axisListeners.add(l);
    }
    
    public void removeAxisLisener(AxisListener l) {
        axisListeners.remove(l);
    }
    
    public JTable getTable() {
        return table;
    }
    
    public CellSetTableHeaderComponent getRowHeader() {
        return rowHeader;
    }
    
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    public void setMinColumnWidth(int minimumColumnWidth) {
        this.minColumnWidth = minimumColumnWidth;
    }

    public CellSet getCellSet() {
        return cellSet;
    }

    public void setCellSet(CellSet cellSet) {
        this.cellSet = cellSet;
    }

}
