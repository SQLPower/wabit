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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.report.HorizontalAlignment;

/**
 * This represents one cell in the result set. This includes each cell that
 * is a value in the result set, headers, totals, and anything else that fits
 * in one position in a normal table.
 */
public class ResultSetCell {
    
    private static final Logger logger = Logger.getLogger(ResultSetCell.class);
    
    /**
     * These are the different locations of borders that can appear on a cell.
     * A cell can have an arbitrary number of these borders on it. If the cell
     * has two of the same borders, eg top, there will be two border lines on
     * that side, eg for grand totals.
     */
    public enum BorderType {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    /**
     * The text this cell displays
     */
    private final String text;
    
    /**
     * The bounds this cell takes up in a page. This is in relation to the top left
     * of the context box of the current page.
     */
    private final Rectangle bounds;

    private final Insets insets;
    
    /**
     * These are the different locations of borders that can appear on a cell.
     * A cell can have an arbitrary number of these borders on it. If the cell
     * has two of the same borders, eg top, there will be two border lines on
     * that side, eg for grand totals.
     */
    private final List<BorderType> borderTypes;

    private final Font font;

    private final HorizontalAlignment align;

    public ResultSetCell(String text, Font font, Rectangle bounds, Insets insets, 
            HorizontalAlignment align, List<BorderType> borderTypes) {
        this.text = text;
        this.font = font;
        this.bounds = bounds;
        this.insets = insets;
        this.align = align;
        this.borderTypes = borderTypes;
    }
    
    public ResultSetCell(ResultSetCell cellToCopy) {
        this.text = cellToCopy.text;
        this.font = cellToCopy.font;
        this.bounds = new Rectangle(cellToCopy.bounds);
        this.insets = new Insets(cellToCopy.insets.top, cellToCopy.insets.left, 
                cellToCopy.insets.bottom, cellToCopy.insets.right);
        this.align = cellToCopy.align;
        this.borderTypes = cellToCopy.borderTypes;
    }

    public String getText() {
        return text;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Changes the bounds of this cell to have it's top left point
     * be equal to this point.
     */
    public void moveCell(Point p) {
        bounds.setLocation(p);
    }
    
    /**
     * Paints the cell on the graphics object with respect to the context box.
     * The graphics object should not be translated to have it's origin at the
     * top left of this cell.
     */
    public void paint(Graphics2D g) {
        Font oldFont = g.getFont();
        g.setFont(font);
        final FontMetrics fm = g.getFontMetrics();
        int offset = align.computeStartX(bounds.width - insets.left - insets.right, fm.stringWidth(text));
        
        String printingText = text;
        double stringLength = fm.getStringBounds(text, g).getWidth();
        final double widthInsideInsets = getBounds().getWidth() - insets.left - insets.right;
        
        String ellipse = "...";
        double ellipseLength = fm.getStringBounds(ellipse, g).getWidth();
        
        if (widthInsideInsets < ellipseLength) {
            printingText = "";
        } else if (stringLength > widthInsideInsets) {
            int charLimit = 0;
            while (fm.getStringBounds(text, 0, charLimit, g).getWidth() < widthInsideInsets - ellipseLength) {
                charLimit++;
            }
            if (charLimit > 0) {
                charLimit--;
            }
            printingText = text.substring(0, charLimit) + ellipse;
        }
        
        int textY = getBounds().y + insets.top + fm.getHeight();
        for (BorderType type : borderTypes) {
            if (type.equals(BorderType.TOP)) {
                textY += ReportPositionRenderer.BORDER_LINE_SIZE;
            }
        }
        g.drawString(printingText, getBounds().x + insets.left + offset, 
                textY);
        g.setFont(oldFont);
        
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
            logger.warn("The stroke was of type " + g.getStroke().getClass() 
                    + " when drawing the totals line. We only change BasicStroke lines.");
        }
        
        int topIndent = 0;
        for (BorderType border : getBorderTypes()) {
            switch (border) {
                case LEFT:
                    g.drawLine(getBounds().x, getBounds().y,
                            getBounds().x, getBounds().y + getBounds().height);
                    break;
                case RIGHT:
                    g.drawLine(getBounds().x + getBounds().width, getBounds().y,
                            getBounds().x + getBounds().width, getBounds().y + getBounds().height);
                    break;
                case TOP:
                    g.drawLine(getBounds().x, getBounds().y + topIndent,
                            getBounds().x + getBounds().width, getBounds().y + topIndent);
                    topIndent += ReportPositionRenderer.BORDER_LINE_SIZE;
                    break;
                case BOTTOM:
                    g.drawLine(getBounds().x, getBounds().y + getBounds().height,
                            getBounds().x + getBounds().width, getBounds().y + getBounds().height);
                    break;
                default:
                    throw new IllegalStateException("Unknown side of a cell " + border + ", " +
                    		"cannot break out into the third or fourth dimension.");
            }
        }
        
        g.setStroke(oldStroke);
    }

    public List<BorderType> getBorderTypes() {
        return borderTypes;
    }
}
