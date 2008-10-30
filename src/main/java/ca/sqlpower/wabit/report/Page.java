/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a page that a report can be output to. Includes
 * information about page dimensions and margins.
 */
public class Page {

    /**
     * This is the Graphics2D standard for pixels-to-inches conversions.
     */
    public static final int DPI = 72;

    /**
     * The default FontMetrics for this Page. All ContentBoxes in this
     * Page that use a FontMetrics will by default inherit this one.
     */
    private FontMetrics defaultFont;

    public static enum StandardPageSizes {
        /**
         * Approximation of ISO A4 (210x297mm) in 1/72 of an inch.
         */
        A4(595, 841),
        
        /**
         * Approximation of ISO A3 (297x420mm) in 1/72 of an inch.
         */
        A3(841, 1190),
        
        /**
         * US Letter (8.5x11 inches).
         */
        US_LETTER((int) (8.5*DPI), 11*DPI),
        
        /**
         * US Legal (8.5x14 inches).
         */
        US_LEGAL((int) (8.5*DPI), 14*DPI);
        
        private final int width;
        private final int height;
        
        private StandardPageSizes(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
    
    /**
     * Page width, inclusive of margins and non-printable area.
     */
    private int width;
    
    /**
     * Page height, inclusive of margins and non-printable area.
     */
    private int height;
    
    /**
     * Left margin width. Default is 1 inch.
     */
    private int leftMargin = DPI;
    
    /**
     * Right margin width. Default is 1 inch.
     */
    private int rightMargin = DPI;
    
    /**
     * Top margin height. Default is 1 inch.
     */
    private int topMargin = DPI;
    
    /**
     * Bottom margin height. Default is 1 inch.
     */
    private int bottomMargin = DPI;
    
    /**
     * The content boxes that provide the page's content and define its layout.
     */
    private final List<ContentBox> contentBoxes = new ArrayList<ContentBox>();
    
    /**
     * Creates a page with the given standard size and 1-inch margins on all sides.
     * 
     * @param size The standard page size this page should have.
     */
    public Page(StandardPageSizes size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    /**
     * Creates a page with the given custom width and height, and 1-inch margins. The units
     * for width and height are 1/72 of an inch, which correspond well with screen pixels
     * in Java 2D (72 pixels = 1 inch).
     *  
     * @param width The page width in units of 1/72 inch.
     * @param height The page height in units of 1/72 inch.
     */
    public Page(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }
    
    public FontMetrics getDefaultFont() {
        return defaultFont;
    }
    
    public void setDefaultFont(FontMetrics defaultFont) {
        this.defaultFont = defaultFont;
    }
    
    public void addContentBox(ContentBox addme) {
        if (addme.getPage() != null) {
            throw new IllegalStateException("That content box already belongs to a different page");
        }
        addme.setPage(this);
        contentBoxes.add(addme);
    }
    
    public void removeContentBox(ContentBox removeme) {
        if (removeme.getPage() != this) {
            throw new IllegalStateException("That's not my content box!");
        }
        contentBoxes.remove(removeme);
        removeme.setPage(null);
    }
}
