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
import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Represents a box on the page which has an absolute position and size.
 */
public class ContentBox extends AbstractWabitObject {

    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * The font for this content box's contents. If null, the containing page's
     * default font will be used.
     */
    private Font font;
    
    /**
     * The renderer that provides visual content for this box.
     */
    private ReportContentRenderer contentRenderer;
    
    public ContentBox() {
        setName("Empty content box");
    }
    
    public void setContentRenderer(ReportContentRenderer contentRenderer) {
        ReportContentRenderer oldContentRenderer = this.contentRenderer;
        this.contentRenderer = contentRenderer;
        setName("Content from " + contentRenderer);
        firePropertyChange("contentRenderer", oldContentRenderer, contentRenderer);
    }
    
    public ReportContentRenderer getContentRenderer() {
        return contentRenderer;
    }
    
    @Override
    public Page getParent() {
        return (Page) super.getParent();
    }
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        int oldX = this.x;
        this.x = x;
        firePropertyChange("x", oldX, x);
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        int oldY = this.y;
        this.y = y;
        firePropertyChange("y", oldY, y);
    }
    
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        int oldWidth = this.width;
        this.width = width;
        firePropertyChange("width", oldWidth, width);
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        int oldHeight = this.height;
        this.height = height;
        firePropertyChange("height", oldHeight, height);
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        throw new UnsupportedOperationException("Content boxes don't have children");
    }

    public List<WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public Font getFont() {
        if (font == null && getParent() != null) {
            return getParent().getDefaultFont();
        } else {
            return font;
        }
    }

    public void setFont(Font font) {
        Font oldFont = this.font;
        this.font = font;
        firePropertyChange("font", oldFont, font);
    }

}
