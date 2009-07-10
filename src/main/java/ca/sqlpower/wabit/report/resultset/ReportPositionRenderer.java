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

package ca.sqlpower.wabit.report.resultset;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;
import ca.sqlpower.wabit.report.resultset.Section.TotalRenderStyle;

/**
 * This class renders the different parts of each result set. The rendering is
 * done at the origin of the graphics object and transformations should be used
 * on the graphics object to properly place the parts.
 * <p>
 * The size of the content box may be useful at some point to define available width
 * if centering is desired.
 */
public class ReportPositionRenderer {
    
    private static final Logger logger = Logger.getLogger(ReportPositionRenderer.class);
    
    private final Font headerFont;
    
    private final Font bodyFont;

    private final BorderStyles borderType;

    /**
     * This is the available width to render in. This lets the renderer
     * define borders so they don't go out of bounds.
     */
    private final int availableWidth;
    
    private final String nullString;
    
    public ReportPositionRenderer(Font headerFont, Font bodyFont, BorderStyles borderType, int availableWidth, String nullString) {
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
        this.borderType = borderType;
        this.availableWidth = availableWidth;
        this.nullString = nullString;
    }

    /**
     * This renders the current row of the result set given based on the
     * graphics and the column information. This method will not modify the
     * cursor position in the result set.
     * 
     * @param g
     *            The graphics to render the row into. This will also be
     *            used to define the dimension of the row.
     * @param rs
     *            The result set whose current row will be rendered into the
     *            graphics. The cursor will not be modified in this result
     *            set. The row the cursor is at will be the row rendered.
     * @param columnInformation
     *            This defines properties of the columns in the result set.
     *            This will define formatting, spacing, alignment, and other
     *            values specific to each column.
     */
    public Dimension renderRow(Graphics2D g, ResultSet rs, List<ColumnInfo> columnInformation, Section section) throws SQLException {
        if (!section.isShowingRows()) return new Dimension(0, 0);
        
        if (rs.getMetaData().getColumnCount() != columnInformation.size()) throw new IllegalArgumentException("The column information for rendering a row was missing columns for the given result set");
        
        FontMetrics fm = g.getFontMetrics(bodyFont);
        g.setFont(bodyFont);
        int x = 0;
        int y = fm.getHeight();
        if (borderType == BorderStyles.HORIZONTAL || borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL) {
            y += 2;
        }
        for (int col = 0; col < columnInformation.size(); col++) {
            ColumnInfo ci = columnInformation.get(col);
            if (ci.getWillBreak()) continue;
            //XXX This is probably not the best place to draw border lines.
            if ((borderType == BorderStyles.VERTICAL || borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL) && col != 0) {
                g.drawLine(x, 0, x, y);
                x += ResultSetRenderer.BORDER_INDENT;
            }
            
            Object value = rs.getObject(col + 1);
            String formattedValue;
            if (ci.getFormat() != null && value != null) {
                logger.debug("Format iss:"+ ci.getFormat()+ "string is:"+ rs.getString(col + 1));
                formattedValue = ci.getFormat().format(value);
            } else {
                formattedValue = replaceNull(rs.getString(col + 1));
            }
            
            int offset = ci.getHorizontalAlignment().computeStartX(
                    ci.getWidth(), fm.stringWidth(formattedValue));
            g.drawString(formattedValue, x + offset, fm.getHeight()); // TODO clip and/or line wrap and/or warn
            x += ci.getWidth();
        }
        if (borderType == BorderStyles.HORIZONTAL || borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL) {
            g.drawLine(0, y, availableWidth, y);
        }
        
        return new Dimension(x, y);
    }
    
    public Dimension renderSectionHeader(Graphics2D g, List<Object> sectionHeader, List<ColumnInfo> colInfo, Section section) {
        if (!section.isShowingSectionHeader()) return new Dimension(0, 0);
        
        StringBuffer headerBuffer = new StringBuffer();
        for (Object headerObject : sectionHeader) {
            if (headerObject != null) {
                if (headerBuffer.length() > 0) {
                    headerBuffer.append(", ");
                }
                headerBuffer.append(colInfo.get(sectionHeader.indexOf(headerObject)).getName() + ":" + headerObject);
            }
        }
        String header = headerBuffer.toString();
        
        //Centres the header if there is enough space and the header
        //isn't too large.
        FontMetrics fm = g.getFontMetrics(headerFont);
        int tableWidth = 0;
        for (ColumnInfo ci : colInfo) {
            if (!ci.getWillBreak()) {
                tableWidth += ci.getWidth();
            }
        }
        int offset;
        int maxWidth = Math.min(tableWidth, availableWidth);
        int textWidth = fm.stringWidth(header);
        if (textWidth < maxWidth) {
            offset = (maxWidth - textWidth) / 2;
        } else {
            offset = 0;
        }
        
        if (header.trim().length() == 0) {
            return new Dimension(0, 0);
        }
        g.setFont(headerFont);
        g.drawString(header, offset, fm.getHeight());
        return new Dimension(fm.stringWidth(header), fm.getHeight());
    }
    
    public Dimension renderColumnHeader(Graphics2D g, List<ColumnInfo> colInfo, Section section) {
        if (!section.isShowingColumnHeader()) return new Dimension(0, 0);
        
        int x = 0;
        FontMetrics fm = g.getFontMetrics(headerFont);
        g.setFont(headerFont);
        int y = 0;
        if (borderType == BorderStyles.FULL || borderType == BorderStyles.OUTSIDE) {
            x += ResultSetRenderer.BORDER_INDENT;
        }
        for (int col = 1; col <= colInfo.size(); col++) {
            ColumnInfo ci = colInfo.get(col-1);
            if (ci.getWillBreak()) continue;
            final String colHeaderName = replaceNull(ci.getName());
            int offset = ci.getHorizontalAlignment().computeStartX(
                    ci.getWidth(), fm.stringWidth(colHeaderName));
            g.drawString(colHeaderName, x + offset, fm.getHeight());
            x += ci.getWidth();
            if (borderType == BorderStyles.VERTICAL || borderType == BorderStyles.FULL || borderType == BorderStyles.INSIDE) {
                x += ResultSetRenderer.BORDER_INDENT;
            }
        }
        
        y += fm.getHeight() + fm.getHeight()/2;
        g.drawLine(0, y, availableWidth, y);
        return new Dimension(x, y);
    }
    
    public Dimension renderTotals(Graphics2D g, List<BigDecimal> totalsRow, List<ColumnInfo> colInfo, Section section) {
        int localX = 0;
        final int subtotalLineYShift = 2; //Shifting the subtotal line slightly down to make it look nicer.
        
        boolean hasTotals = false;
        for (BigDecimal total : totalsRow) {
            if (total != null) {
                hasTotals = true;
            }
        }
        if (!hasTotals) {
            return new Dimension(0, 0);
        }
        
        if (section.getTotalRenderStyle() == TotalRenderStyle.SUBTOTAL) {

            FontMetrics bodyFM = g.getFontMetrics(bodyFont);
            FontMetrics headerFM = g.getFontMetrics(headerFont);

            final int rowHeight = Math.max(bodyFM.getHeight(), headerFM.getHeight());
            int y = rowHeight;

            g.setFont(headerFont);
            g.drawString("Subtotal", ResultSetRenderer.BORDER_INDENT, y);
            g.setFont(bodyFont);

            for (int subCol = 0; subCol < totalsRow.size(); subCol++) {
                ColumnInfo ci = colInfo.get(subCol);
                if (ci.getWillBreak()) continue;
                if ((borderType == BorderStyles.VERTICAL || borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL) && subCol != 0) {
                    localX += ResultSetRenderer.BORDER_INDENT;
                }
                BigDecimal subtotal = totalsRow.get(subCol);
                if (subtotal != null) {
                    String formattedValue;
                    if (ci.getFormat() != null) {
                        formattedValue = ci.getFormat().format(subtotal);
                    } else {
                        formattedValue = subtotal.toString();
                    }
                    int offset = ci.getHorizontalAlignment().computeStartX(
                            ci.getWidth(), bodyFM.stringWidth(formattedValue));
                    Stroke oldStroke = g.getStroke();

                    //Thinning the stroke for the subtotal line for looks. We can't get the
                    //line width from a regular stroke so if the stroke is somehow different
                    //from a BasicStroke we will just log the warning. (For cases where the
                    //platform may make the Stroke significantly different.)
                    if (g.getStroke() instanceof BasicStroke) {
                        BasicStroke currentStroke = ((BasicStroke) g.getStroke());
                        BasicStroke newStroke = new BasicStroke(currentStroke.getLineWidth() / 2, 
                                currentStroke.getEndCap(), currentStroke.getLineJoin(), 
                                currentStroke.getMiterLimit(), currentStroke.getDashArray(), 
                                currentStroke.getDashPhase());
                        g.setStroke(newStroke);
                    } else {
                        logger.warn("The stroke was of type " + g.getStroke().getClass() + " when drawing the totals line. We only change BasicStroke lines.");
                    }
                    g.drawLine(localX, y - rowHeight + subtotalLineYShift, localX + ci.getWidth(), y - rowHeight + subtotalLineYShift);
                    g.setStroke(oldStroke);

                    g.drawString(formattedValue, localX + offset, y); // TODO clip and/or line wrap and/or warn
                }
                localX += ci.getWidth();
            }
            y += rowHeight / 2;
            return new Dimension(localX, y);
        } else if (section.getTotalRenderStyle() == TotalRenderStyle.GRAND_TOTAL) {

            Font boldBodyFont = bodyFont.deriveFont(Font.BOLD);
            FontMetrics bodyFM = g.getFontMetrics(boldBodyFont);
            FontMetrics headerFM = g.getFontMetrics(headerFont);

            final int rowHeight = Math.max(bodyFM.getHeight(), headerFM.getHeight());
            int y = rowHeight;

            g.setFont(headerFont);
            g.drawString("Grand Total", ResultSetRenderer.BORDER_INDENT, y);
            g.setFont(boldBodyFont);

            for (int subCol = 0; subCol < totalsRow.size(); subCol++) {
                ColumnInfo ci = colInfo.get(subCol);
                if (ci.getWillBreak()) continue;
                if ((borderType == BorderStyles.VERTICAL || borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL) && subCol != 0) {
                    localX += ResultSetRenderer.BORDER_INDENT;
                }
                BigDecimal subtotal = totalsRow.get(subCol);
                if (subtotal != null) {
                    String formattedValue;
                    if (ci.getFormat() != null) {
                        formattedValue = ci.getFormat().format(subtotal);
                    } else {
                        formattedValue = subtotal.toString();
                    }
                    int offset = ci.getHorizontalAlignment().computeStartX(
                            ci.getWidth(), bodyFM.stringWidth(formattedValue));
                    Stroke oldStroke = g.getStroke();

                    //Thinning the stroke for the subtotal line for looks. We can't get the
                    //line width from a regular stroke so if the stroke is somehow different
                    //from a BasicStroke we will just log the warning. (For cases where the
                    //platform may make the Stroke significantly different.)
                    if (g.getStroke() instanceof BasicStroke) {
                        BasicStroke currentStroke = ((BasicStroke) g.getStroke());
                        BasicStroke newStroke = new BasicStroke(currentStroke.getLineWidth() / 2, 
                                currentStroke.getEndCap(), currentStroke.getLineJoin(), 
                                currentStroke.getMiterLimit(), currentStroke.getDashArray(), 
                                currentStroke.getDashPhase());
                        g.setStroke(newStroke);
                    } else {
                        logger.warn("The stroke was of type " + g.getStroke().getClass() + " when drawing the totals line. We only change BasicStroke lines.");
                    }
                    g.drawLine(localX, y - rowHeight + subtotalLineYShift, localX + ci.getWidth(), y - rowHeight + subtotalLineYShift);
                    g.drawLine(localX, y - rowHeight + subtotalLineYShift + 2, localX + ci.getWidth(), y - rowHeight + subtotalLineYShift + 2);
                    g.setStroke(oldStroke);

                    g.drawString(formattedValue, localX + offset, y); // TODO clip and/or line wrap and/or warn
                }
                localX += ci.getWidth();
            }
            y += rowHeight / 2;
            return new Dimension(localX, y);
        } else {
            throw new IllegalStateException("The totals of the section " + section.getSectionHeader() + " are being displayed in the style " + section.getTotalRenderStyle() + " which is unknown.");
        }
    }
    
    /**
     * This will replace null values with the designated null string.
     */
    public String replaceNull(String string) {
        if (string == null) {
            return nullString;
        } else {
            return string;
        }
    }
}
