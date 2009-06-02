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

package ca.sqlpower.wabit.report;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.OlapException;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableModel;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.LayoutItem;

/**
 * Renders a CellSet from a MDX query on a report layout.
 */
public class CellSetRenderer extends AbstractWabitObject implements
        ReportContentRenderer {

    private final OlapQuery olapQuery;
    private CellSet cellSet;
    
    private Font headerFont;
    
    private Font bodyFont;

    private final static Logger logger = Logger.getLogger(CellSetRenderer.class);
    
    public CellSetRenderer(OlapQuery olapQuery) {
        this.olapQuery = olapQuery;
        //TODO create a cached OLAP query to store this value.
        try {
            cellSet = olapQuery.getMdxQuery().execute();
        } catch (OlapException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanup() {
        // TODO Auto-generated method stub

    }

    public Color getBackgroundColour() {
        // TODO Auto-generated method stub
        return null;
    }

    public DataEntryPanel getPropertiesPanel() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
            double scaleFactor, int pageIndex, boolean printing) {
        if (getBodyFont() == null) {
            setBodyFont(g.getFont());
        }
        if (getHeaderFont() == null) {
            setHeaderFont(g.getFont());
        }
        
        g.setFont(getHeaderFont());
        int headerFontHeight = g.getFontMetrics().getHeight();
        CellSetAxis cellSetAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
        CellSetAxisMetaData axisMetaData = cellSetAxis.getAxisMetaData();
        int hierarchyCount = axisMetaData.getHierarchies().size();
        int totalHeaderHeight = headerFontHeight * hierarchyCount;
        
        g.setFont(getBodyFont());
        FontMetrics bodyFM = g.getFontMetrics();
        int maxRowHeight = Math.max(headerFontHeight, bodyFM.getHeight());
        
        int numRows = (contentBox.getHeight() - totalHeaderHeight) / maxRowHeight;
        
        if (numRows <= 0) return false; //Can't display anything here because there's not enough space.
        
        int firstRecord = numRows * pageIndex;
        
        CellSetTableModel tableModel = new CellSetTableModel(cellSet);
        final JTable tableAsModel = new JTable(tableModel);
        //TODO update the JTable we're using as a model to have the proper font and column sizes.
        
        CellSetTableHeaderComponent columnHeaderComponent = new CellSetTableHeaderComponent(cellSet, Axis.COLUMNS, tableAsModel.getColumnModel(), g.create(), getHeaderFont());
        
        CellSetTableHeaderComponent rowHeaderComponent = new CellSetTableHeaderComponent(cellSet, Axis.ROWS, tableAsModel.getColumnModel(), g.create(), getHeaderFont());
        
        g.setFont(getHeaderFont());
        double rowHeaderWidth = rowHeaderComponent.getPreferredSize().getWidth();
        double colHeaderSumHeight = 0;
        for (HierarchyComponent hierarchyComponent : columnHeaderComponent.getHierarchies()) {
            hierarchyComponent.getPreferredSize();//XXX just laying out items
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
                g.drawString(layoutItem.getText(), (float) (layoutItem.getBounds().getX() + rowHeaderWidth), (float) (layoutItem.getBounds().getY() + colHeaderSumHeight + headerFontHeight));
            }
            colHeaderSumHeight += hierarchyComponent.getPreferredSize().getHeight();
        }
        
        double columnHeaderHeight = columnHeaderComponent.getPreferredSize().getHeight();
        double rowHeaderSumWidth = 0;
        for (HierarchyComponent hierarchyComponent : rowHeaderComponent.getHierarchies()) {
            hierarchyComponent.getPreferredSize();//XXX just laying out items
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
                g.drawString(layoutItem.getText(), (float) (layoutItem.getBounds().getX() + rowHeaderSumWidth), (float) (layoutItem.getBounds().getY() + columnHeaderHeight + headerFontHeight));
            }
            rowHeaderSumWidth += hierarchyComponent.getPreferredSize().getWidth();
        }
        
        CellSetAxis columnsAxis = cellSet.getAxes().get(0);
        CellSetAxis rowsAxis = cellSet.getAxes().get(1);
        
        g.setFont(getBodyFont());
        for (int row = 0; row < rowsAxis.getPositionCount(); row++) {
            int colPosition = 0;
            for (int col = 0; col < columnsAxis.getPositionCount(); col++) {
                String formattedValue = cellSet.getCell(
                        columnsAxis.getPositions().get(col),
                        rowsAxis.getPositions().get(row)).getFormattedValue();
                g.drawString(formattedValue, (int) (rowHeaderWidth + colPosition), (int) (columnHeaderHeight + (row * maxRowHeight) + headerFontHeight));
                colPosition += tableAsModel.getColumnModel().getColumn(col).getWidth();
            }
        }
        return false;
    }

    public void resetToFirstPage() {
        // TODO Auto-generated method stub

    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return new ArrayList<WabitObject>();
    }

    public void setHeaderFont(Font headerFont) {
        this.headerFont = headerFont;
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public void setBodyFont(Font bodyFont) {
        this.bodyFont = bodyFont;
    }

    public Font getBodyFont() {
        return bodyFont;
    }

}
