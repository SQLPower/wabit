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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.query.RectangularCellSetFormatter;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.table.TableUtils;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.CellSetTableCornerComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;

public class CellSetViewer {

    private static final Logger logger = Logger
            .getLogger(CellSetViewer.class);
    
    private final JPanel viewerComponent = new JPanel(new BorderLayout());
    private final JTable table;
    private final JScrollPane scrollPane;
    private final JLabel messageLabel = new JLabel("", JLabel.CENTER);
    private int minColumnWidth = 5;
    

    /**
     * This is the cell set most recently displayed in this viewer. This will be
     * null until the first cell set is displayed.
     */
    private CellSet cellSet;

    /**
     * If false the listeners that allow expanding and collapsing members
     * when clicking on the headers will be removed.
     */
    private final boolean allowMemberModification;
    
    
    private final JScrollPane slicerScrollPane;

	/**
	 * Creates a CellSetViewer on the given {@link OlapQuery} and by default
	 * allows Member modification
	 */
    public CellSetViewer(OlapQuery query) {
        this(query, true);
    }
    
	/**
	 * Creates a CellSetViewer on the given {@link OlapQuery}
	 * 
	 * @param query
	 *            The {@link OlapQuery} whose {@link CellSet} results this
	 *            viewer will be displaying
	 * @param allowMemberModification
	 *            Whether or not the CellSetViewer GUI will allow modification
	 *            of the members of the Query.
	 */
    public CellSetViewer(OlapQuery query, boolean allowMemberModification) {
    	this.allowMemberModification = allowMemberModification;
    	viewerComponent.setPreferredSize(new Dimension(640, 480));
    	slicerScrollPane = new JScrollPane();
    	slicerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	slicerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        showMessage(query, "No query defined");
        viewerComponent.add(scrollPane, BorderLayout.CENTER);
        viewerComponent.add(slicerScrollPane, BorderLayout.SOUTH);
    }

    public void showCellSet(OlapQuery query, CellSet cellSet) {
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
        CellSetTableHeaderComponent rowHeader = new CellSetTableHeaderComponent(query, cellSet, Axis.ROWS, table);
        rowHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        
        final CellSetTableHeaderComponent columnHeader = new CellSetTableHeaderComponent(query, cellSet, Axis.COLUMNS, table);
        columnHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        
        SlicerPanel slicerPanel = new SlicerPanel(query);
        slicerPanel.setVisible(true);
        slicerScrollPane.setViewportView(slicerPanel);
        
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
       
        CellSetTableCornerComponent corner = rowHeader.getCornerComponent();
        corner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
        
        scrollPane.setViewportView(table);
    	scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, corner);
		
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

	public void showMessage(OlapQuery query, String message) {
        messageLabel.setText(message);
        
        // This line needs to be called before setting the column header. If it
		// is called afterwards, it removes the column header that was just
		// added, resulting in a missing column header.
        scrollPane.setViewportView(messageLabel);
        scrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, null);
        
        CellSetTableHeaderComponent rowHeader;
        try {
            rowHeader = new CellSetTableHeaderComponent(query, Axis.ROWS);
            scrollPane.setRowHeaderView(rowHeader);
        } catch (QueryInitializationException e) {
            messageLabel.setText(e.getMessage());
            logger.error("Exception while creating row header.", e);
        }
        
        CellSetTableHeaderComponent columnHeader;
        try {
            columnHeader = new CellSetTableHeaderComponent(query, Axis.COLUMNS);
            scrollPane.setColumnHeaderView(columnHeader);
        } catch (QueryInitializationException e) {
            messageLabel.setText(e.getMessage());
            logger.error("Exception while creating row header.", e);
        }
        
        SlicerPanel slicerPanel = new SlicerPanel(query);
        slicerScrollPane.setPreferredSize(new Dimension(slicerScrollPane.getPreferredSize().height + 10, slicerPanel.getPreferredSize().height + 10));
		slicerScrollPane.setViewportView(slicerPanel);
	}
	
    public JComponent getViewComponent() {
        return viewerComponent;
    }
    
    public JTable getTable() {
        return table;
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
    
    /**
     * Executes the containing OlapQuery and updates the results in the
     * CellSetViewer.
     */
    public void updateCellSetViewer(OlapQuery query, CellSet cellSet) {
        try {
            if (query.getCurrentCube() == null) {
                showMessage(query, "No cube selected--please select one from the dropdown list");
                return;
            }
            
            if (cellSet == null) {
                List<Hierarchy> rowHierarchies;
                List<Hierarchy> columnHierarchies;
                try {
                    rowHierarchies = query.getRowHierarchies();
                    columnHierarchies = query.getColumnHierarchies();
                } catch (QueryInitializationException e) {
                    throw new RuntimeException(e);
                }
                
                if (rowHierarchies.isEmpty() && !columnHierarchies.isEmpty()) {
                    showMessage(query, "Rows axis is empty--please drop something on it");
                } else if (columnHierarchies.isEmpty() && !rowHierarchies.isEmpty()) {
                    showMessage(query, "Columns axis is empty--please drop something on it");
                } else {
                    showMessage(query, "No query defined");
                }
                return;
            }
            
            showCellSet(query, cellSet);
        } catch (Exception e) {
            showMessage(query, "Could not execute the query due to the following error: \n" + e.getClass().getName() + ":" + e.getMessage());
            logger.warn("Could not execute the query " + query, e);
        }
    }

}
