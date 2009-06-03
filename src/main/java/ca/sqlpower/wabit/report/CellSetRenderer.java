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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.OlapException;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.swingui.Icons;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableModel;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.LayoutItem;

/**
 * Renders a CellSet from a MDX query on a report layout.
 */
public class CellSetRenderer extends AbstractWabitObject implements
        ReportContentRenderer {
    
    /**
     * This is the OLAP query being displayed by this cell set renderer.
     */
    private final OlapQuery olapQuery;
    
    /**
     * This is the current cell set being displayed.
     * TODO be able to replace this when the olapQuery changes.
     */
    private CellSet cellSet;
    
    /**
     * The font of the column and row headers.
     */
    private Font headerFont;
    
    /**
     * The font of the text in the cell set.
     */
    private Font bodyFont;
    
    /**
     * The alignment of the text in the body of this
     * cell set.
     */
    private HorizontalAlignment bodyAlignment = HorizontalAlignment.RIGHT;
    
    /**
     * This is the overriding format of each cell in the cell set. If this is set
     * the format here will be used to format the cell values. If this is not set
     * the format stored in the cell set will be used.
     */
    private DecimalFormat bodyFormat;

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
        final JPanel panel = new JPanel(new MigLayout("", "[][grow][]", ""));
        
        panel.add(new JLabel("Header Font"), "gap related");
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.setFont(getHeaderFont());
        panel.add(headerFontExample, "gap related");
        panel.add(ReportUtil.createFontButton(headerFontExample), "wrap");
        
        panel.add(new JLabel("Body Font"), "gap related");
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(getBodyFont());
        panel.add(bodyFontExample, "gap related");
        panel.add(ReportUtil.createFontButton(bodyFontExample), "wrap");
        
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(
                Icons.LEFT_ALIGN_ICON, bodyAlignment == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(
                Icons.CENTRE_ALIGN_ICON, bodyAlignment == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(
                Icons.RIGHT_ALIGN_ICON, bodyAlignment == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);
        Box alignmentBox = Box.createHorizontalBox();
        alignmentBox.add(leftAlign);
        alignmentBox.add(centreAlign);
        alignmentBox.add(rightAlign);
        alignmentBox.add(Box.createHorizontalGlue());
        panel.add(new JLabel("Column Alignment"), "gap related");
        panel.add(alignmentBox, "span 2, wrap");
        
        final JComboBox bodyFormatComboBox = new JComboBox();
        bodyFormatComboBox.addItem(ReportUtil.DEFAULT_FORMAT_STRING);
        for (NumberFormat item : ReportUtil.getNumberFormats()) {
            bodyFormatComboBox.addItem(((DecimalFormat) item).toPattern());
        }
        if (bodyFormat != null) {
            bodyFormatComboBox.setSelectedItem(bodyFormat.toPattern());
        } else {
            bodyFormatComboBox.setSelectedItem(ReportUtil.DEFAULT_FORMAT_STRING);
        }
        panel.add(new JLabel("Body Format"), "gap related");
        panel.add(bodyFormatComboBox, "span 2, wrap");
        
        return new DataEntryPanel() {
            
        
            public boolean hasUnsavedChanges() {
                return true;
            }
        
            public JComponent getPanel() {
                return panel;
            }
        
            public void discardChanges() {
                //do nothing
            }
        
            public boolean applyChanges() {
                setHeaderFont(headerFontExample.getFont());
                setBodyFont(bodyFontExample.getFont());
                
                if (leftAlign.isSelected()) {
                    setBodyAlignment(HorizontalAlignment.LEFT);
                } else if (rightAlign.isSelected()) {
                    setBodyAlignment(HorizontalAlignment.RIGHT);
                } else if (centreAlign.isSelected()) {
                    setBodyAlignment(HorizontalAlignment.CENTER);
                }
                
                if (bodyFormatComboBox.getSelectedItem().equals(ReportUtil.DEFAULT_FORMAT_STRING)) {
                    setBodyFormat(null);
                } else {
                    setBodyFormat(new DecimalFormat((String) bodyFormatComboBox.getSelectedItem()));
                }
                
                return true;
            }
        };
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
        int colourSchemeNum = 0;
        Color oldForeground = g.getColor();
        for (HierarchyComponent hierarchyComponent : columnHeaderComponent.getHierarchies()) {
            hierarchyComponent.getPreferredSize();//XXX just laying out items
            g.setColor(ColourScheme.BACKGROUND_COLOURS[colourSchemeNum]);
            //XXX come back and figure out why the y position needs to be increased by 2.
            g.fillRect((int) (hierarchyComponent.getX() + rowHeaderWidth), (int) (hierarchyComponent.getY() + colHeaderSumHeight + 2), (int) contentBox.getWidth(), (int) hierarchyComponent.getPreferredSize().getHeight());
            g.setColor(oldForeground);
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
                g.drawString(layoutItem.getText(), (float) (layoutItem.getBounds().getX() + rowHeaderWidth), (float) (layoutItem.getBounds().getY() + colHeaderSumHeight + headerFontHeight));
            }
            colHeaderSumHeight += hierarchyComponent.getPreferredSize().getHeight();
            colourSchemeNum++;
        }
        g.setBackground(oldForeground);
        
        double columnHeaderHeight = columnHeaderComponent.getPreferredSize().getHeight();
        double rowHeaderSumWidth = 0;
        colourSchemeNum = 0;
        for (HierarchyComponent hierarchyComponent : rowHeaderComponent.getHierarchies()) {
            hierarchyComponent.getPreferredSize();//XXX just laying out items
            g.setColor(ColourScheme.BACKGROUND_COLOURS[colourSchemeNum]);
            g.fillRect((int) (hierarchyComponent.getX() + rowHeaderSumWidth), (int) (hierarchyComponent.getY() + colHeaderSumHeight), (int) hierarchyComponent.getPreferredSize().getWidth(), (int) contentBox.getHeight());
            g.setColor(oldForeground);
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
                g.drawString(layoutItem.getText(), (float) (layoutItem.getBounds().getX() + rowHeaderSumWidth), (float) (layoutItem.getBounds().getY() + columnHeaderHeight + headerFontHeight));
            }
            rowHeaderSumWidth += hierarchyComponent.getPreferredSize().getWidth();
            colourSchemeNum++;
        }
        g.setBackground(oldForeground);
        
        CellSetAxis columnsAxis = cellSet.getAxes().get(0);
        CellSetAxis rowsAxis = cellSet.getAxes().get(1);
        
        g.setFont(getBodyFont());
        for (int row = 0; row < rowsAxis.getPositionCount(); row++) {
            int colPosition = 0;
            for (int col = 0; col < columnsAxis.getPositionCount(); col++) {
                String formattedValue;
                if (bodyFormat != null) {
                    try {
                        formattedValue = bodyFormat.format(cellSet.getCell(
                                columnsAxis.getPositions().get(col),
                                rowsAxis.getPositions().get(row)).getDoubleValue());
                    } catch (OlapException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    formattedValue = cellSet.getCell(
                            columnsAxis.getPositions().get(col),
                            rowsAxis.getPositions().get(row)).getFormattedValue();
                }
                
                double alignmentShift = 0;
                final int columnWidth = tableAsModel.getColumnModel().getColumn(col).getWidth();
                final double textWidthInContext = getBodyFont().getStringBounds(formattedValue, g.getFontRenderContext()).getWidth();
                switch (bodyAlignment) {
                    case RIGHT:
                        alignmentShift = columnWidth - textWidthInContext;
                        break;
                    case LEFT:
                        break;
                    case CENTER:
                        alignmentShift = (columnWidth - textWidthInContext) / 2;
                        break;
                    default:
                        throw new IllegalStateException("Unknown alignment of type " + bodyAlignment);
                }
                
                g.drawString(formattedValue, (int) (rowHeaderWidth + colPosition + alignmentShift), (int) (columnHeaderHeight + (row * maxRowHeight) + headerFontHeight));
                colPosition += columnWidth;
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
        Font oldFont = this.headerFont;
        this.headerFont = headerFont;
        firePropertyChange("headerFont", oldFont, headerFont);
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public void setBodyFont(Font bodyFont) {
        Font oldFont = this.bodyFont;
        this.bodyFont = bodyFont;
        firePropertyChange("bodyFont", oldFont, bodyFont);
    }

    public Font getBodyFont() {
        return bodyFont;
    }
    
    public void setBodyAlignment(HorizontalAlignment bodyAlignment) {
        HorizontalAlignment oldAlign = this.bodyAlignment;
        this.bodyAlignment = bodyAlignment;
        firePropertyChange("bodyAlignment", oldAlign, bodyAlignment);
    }
    
    public void setBodyFormat(DecimalFormat bodyFormat) {
        DecimalFormat oldFormat = this.bodyFormat;
        this.bodyFormat = bodyFormat;
        firePropertyChange("bodyFormat", oldFormat, bodyFormat);
    }

}
