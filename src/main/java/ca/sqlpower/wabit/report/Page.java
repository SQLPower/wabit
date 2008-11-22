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

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.Guide.Axis;

/**
 * A page is an arrangement of boxes and guides (usually page margins) on a
 * container of a particular size. Boxes have data provided to them by content
 * renderers, which can be fed by database queries, labels with variable
 * substitution, or anything else. The actual report will be rendered to one or
 * more pages--the content renderers specify whether or not they need another
 * page to finish rendering their data.
 */
public class Page extends AbstractWabitObject {

    /**
     * This is the Graphics2D standard for pixels-to-inches conversions.
     */
    public static final int DPI = 72;

    /**
     * The default FontMetrics for this Page. All ContentBoxes in this
     * Page that use a FontMetrics will by default inherit this one.
     */
    private Font defaultFont;

    /**
     * XXX this should not be an enum; page sizes should be user-definable and
     * stored in a config file and/or the project file
     */
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
     * Indicates the transformation that should be applied when this page is rendered.
     * Portrait is normal (the identity transform), landscape is rotated 90 degrees
     * clockwise, and reverse landscape is rotated 90 degrees counterclockwise.
     * <p>
     * The orientation doesn't affect what width and height the page reports having;
     * it simply prescribes which direction should be "up" for rendered content.
     */
    public static enum PageOrientation {
        PORTRAIT(PageFormat.PORTRAIT, AffineTransform.getRotateInstance(0.0)),
        LANDSCAPE(PageFormat.LANDSCAPE, AffineTransform.getRotateInstance(-Math.PI / 2.0)),
        REVERSE_LANDSCAPE(PageFormat.REVERSE_LANDSCAPE, AffineTransform.getRotateInstance(Math.PI / 2.0));
        
        private final int printApiCode;
        private final AffineTransform transform;

        PageOrientation(int printApiCode, AffineTransform transform) {
            this.printApiCode = printApiCode;
            this.transform = transform;
        }

        /**
         * Returns the correct integer code for this orientation in the Java
         * Print API.
         */
        public int getPrintApiCode() {
            return printApiCode;
        }

        public static PageOrientation forPrintApiCode(int apiCode) {
            for (PageOrientation po : values()) {
                if (po.printApiCode == apiCode) {
                    return po;
                }
            }
            throw new IllegalArgumentException("Unknown Java Print orientation code: " + apiCode);
        }

        public AffineTransform getTransform() {
            return transform;
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
     * Indicates the orientation for this page's contents.
     */
    private PageOrientation orientation = PageOrientation.PORTRAIT;
    
    /**
     * The content boxes that provide the page's content and define its layout.
     */
    private final List<ContentBox> contentBoxes = new ArrayList<ContentBox>();

    /**
     * The guides of this page. This list includes but is not limited to the
     * default page margins. They don't print visibly, but they give reference
     * points for the layout.
     */
    private final List<Guide> guides = new ArrayList<Guide>();

    /**
     * Creates a page with the given standard size and 1-inch margins on all sides.
     * 
     * @param size The standard page size this page should have.
     */
    public Page(String name, StandardPageSizes size) {
        this(name, size.getWidth(), size.getHeight());
    }

    /**
     * Creates a page with the given custom width and height, and 1-inch margins. The units
     * for width and height are 1/72 of an inch, which correspond well with screen pixels
     * in Java 2D (72 pixels = 1 inch).
     *  
     * @param width The page width in units of 1/72 inch.
     * @param height The page height in units of 1/72 inch.
     */
    public Page(String name, int width, int height) {
        setName(name);
        this.width = width;
        this.height = height;
        
        // Default margins of 1 inch
        addGuide(new Guide(Axis.VERTICAL, DPI));
        addGuide(new Guide(Axis.VERTICAL, width - DPI));
        addGuide(new Guide(Axis.HORIZONTAL, DPI));
        addGuide(new Guide(Axis.HORIZONTAL, height - DPI));
        
        setDefaultFont(Font.decode("dialog 8"));
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        int oldWidth = this.width;
        this.width = width;
        firePropertyChange("width", oldWidth, width);
        adjustMarginForPageWidth(oldWidth, width);
    }
    
    private void adjustMarginForPageWidth(int oldWidth, int newWidth) {
        Guide rightMargin = getGuideWithLargestOffset(Axis.VERTICAL);
        if (rightMargin != null) {
            int oldMarginWidth = oldWidth - rightMargin.getOffset();
            rightMargin.setOffset(newWidth - oldMarginWidth); // XXX respect orientation!
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        int oldHeight = this.height;
        this.height = height;
        firePropertyChange("height", oldHeight, height);
        adjustMarginForPageHeight(oldHeight, height); // XXX respect orientation!
    }

    private void adjustMarginForPageHeight(int oldHeight, int newHeight) {
        Guide bottomMargin = getGuideWithLargestOffset(Axis.HORIZONTAL);
        if (bottomMargin != null) {
            int oldMarginHeight = oldHeight - bottomMargin.getOffset();
            bottomMargin.setOffset(newHeight - oldMarginHeight);
        }
    }
    
    public Font getDefaultFont() {
        return defaultFont;
    }
    
    public void setDefaultFont(Font defaultFont) {
        Font oldFont = this.defaultFont;
        this.defaultFont = defaultFont;
        firePropertyChange("defaultFont", oldFont, defaultFont);
    }
    
    public PageOrientation getOrientation() {
        return orientation;
    }
    
    public void setOrientation(PageOrientation orientation) {
        PageOrientation oldOrientation = this.orientation;
        this.orientation = orientation;
        firePropertyChange("orientation", oldOrientation, orientation);
        if (oldOrientation != orientation) {
            if (oldOrientation == PageOrientation.PORTRAIT) {
                adjustMarginForPageHeight(getHeight(), getWidth());
                adjustMarginForPageWidth(getWidth(), getHeight());
            } else {
                adjustMarginForPageHeight(getWidth(), getHeight());
                adjustMarginForPageWidth(getHeight(), getWidth());
            }
        }
    }
    
    public void addContentBox(ContentBox addme) {
        if (addme.getParent() != null) {
            throw new IllegalStateException("That content box already belongs to a different page");
        }
        int index = contentBoxes.size() + childPositionOffset(ContentBox.class);
        addme.setParent(this);
        contentBoxes.add(addme);
        fireChildAdded(ContentBox.class, addme, index);
    }
    
    public void removeContentBox(ContentBox removeme) {
        if (removeme.getParent() != this) {
            throw new IllegalStateException("That's not my content box!");
        }
        int index = contentBoxes.indexOf(removeme);
        if (index != -1) {
        	contentBoxes.remove(removeme);
        	removeme.setParent(null);
        	fireChildRemoved(ContentBox.class, removeme, index);
    	}
    }
    
    /**
     * Returns an unmodifiable view of this page's content boxes.
     */
    public List<ContentBox> getContentBoxes() {
        return Collections.unmodifiableList(contentBoxes);
    }
    
    public void addGuide(Guide addme) {
        if (addme.getParent() != null) {
            throw new IllegalStateException("That guide already belongs to a different page");
        }
        int index = guides.size() + childPositionOffset(Guide.class);
        addme.setParent(this);
        guides.add(addme);
        fireChildAdded(Guide.class, addme, index);
    }

    public int getLeftMarginOffset() {
        Guide leftMargin = getGuideWithSmallestOffset(Axis.VERTICAL);
        if (leftMargin != null) {
            return leftMargin.getOffset();
        } else {
            return 0;
        }
    }
    
    public int getRightMarginOffset() {
        Guide rightMargin = getGuideWithLargestOffset(Axis.VERTICAL);
        if (rightMargin != null) {
            return rightMargin.getOffset();
        } else {
            return width;
        }
    }

    public int getUpperMarginOffset() {
        Guide upperMargin = getGuideWithSmallestOffset(Axis.HORIZONTAL);
        if (upperMargin != null) {
            return upperMargin.getOffset();
        } else {
            return 0;
        }
    }
    
    public int getLowerMarginOffset() {
        Guide lowerMargin = getGuideWithLargestOffset(Axis.HORIZONTAL);
        if (lowerMargin != null) {
            return lowerMargin.getOffset();
        } else {
            return height;
        }
    }

    private Guide getGuideWithLargestOffset(Axis axis) {
        Guide largest = null;
        for (Guide guide: guides) {
            if (guide.getAxis().equals(axis)) {
                if (largest == null || largest.getOffset() < guide.getOffset()) {
                    largest = guide;
                }
            }
        }
        return largest;
    }
    
    private Guide getGuideWithSmallestOffset(Axis axis) {
        Guide smallest = null;
        for (Guide guide: guides) {
            if (guide.getAxis().equals(axis)) {
                if (smallest == null || smallest.getOffset() > guide.getOffset()) {
                    smallest = guide;
                }
            }
        }
        return smallest;
    }
    
    
    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        if (childType == ContentBox.class) {
            return 0;
        } else if (childType == Guide.class) {
            return contentBoxes.size();
        } else {
            throw new IllegalArgumentException("Pages don't have children of type " + childType);
        }
    }

    /**
     * Returns an unmodifiable view of this page's boxes.
     */
    public List<WabitObject> getChildren() {
        List<WabitObject> children = new ArrayList<WabitObject>();
        children.addAll(contentBoxes);
        children.addAll(guides);
        return Collections.unmodifiableList(children);
    }
    
    /**
     * Removes the first instance of this guide from the Page. Returns
     * true if the guide was successfully removed. Returns false otherwise.
     * @param guide
     * @return
     */
    public boolean removeGuide(Guide guide) {
    	return guides.remove(guide);
    }

    /**
     * Gets a page format instance that describes this page's geometry in terms
     * of the Java print API.
     * 
     * @return A throwaway PageFormat object that describes this page's current
     *         geometry. Changes to the returned PageFormat object will not
     *         affect this page in any way.
     */
    public PageFormat getPageFormat() {
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(getOrientation().getPrintApiCode());
        Paper paper = new Paper();
        paper.setSize(getWidth(), getHeight());

        // the imageable area on the page format we return determines the clipping
        // region for the print API, so we always want to set it as big as possible
        // regardless of our own margin guides
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());

        pageFormat.setPaper(paper);
        
        return pageFormat;
    }
    
    /**
     * Applies the given Java print API page format to this page object. The aspects that
     * are affected are:
     * <ul>
     *  <li>width (paper width)
     *  <li>height (paper height)
     *  <li>orientation
     *  <li>TODO: margins (determined by imageable area)
     * </ul>
     * If any changes are made to the page as a result of this operation, property change
     * events for the individually affected properties will be fired as if those methods
     * had been called directly.
     * 
     * @param pageFormat The page format to apply to this page.
     */
    public void applyPageFormat(PageFormat pageFormat) {
        Paper paper = pageFormat.getPaper();
        
        setWidth((int) paper.getWidth());
        setHeight((int) paper.getHeight());
        setOrientation(PageOrientation.forPrintApiCode(pageFormat.getOrientation()));
        // TODO update margins
    }
}
