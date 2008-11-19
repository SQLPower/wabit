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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * Represents a box on the page which has an absolute position and size.
 * The content of the box is provided by a ContentRenderer implementation.
 * Whenever the content renderer's appearance changes, this box will fire
 * a PropertyChangeEvent with the property name "content". The old and new
 * values will
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
    
    private PropertyChangeListener rendererChangeHandler = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange("content", null, null);
        }
        
    };
    
    public ContentBox() {
        setName("Empty content box");
    }

    /**
     * Sets the given content renderer as this box's provider of rendered
     * content.
     * <p>
     * Although content renderers are considered children of the content box
     * (and this method does cause child added/removed events), a content box
     * can only have one content renderer at a time, so if you call this method
     * when the current content renderer is non-null, the old renderer will be
     * replaced by the new one.
     * 
     * @param contentRenderer
     *            The new content renderer to use. Can be null, which means to
     *            remove the content render and render this content box
     *            incontent.
     */
    public void setContentRenderer(ReportContentRenderer contentRenderer) {
        ReportContentRenderer oldContentRenderer = this.contentRenderer;
        if (oldContentRenderer != null) {
            oldContentRenderer.removePropertyChangeListener(rendererChangeHandler);
            oldContentRenderer.setParent(null);
            fireChildRemoved(ReportContentRenderer.class, oldContentRenderer, 0);
        }
        this.contentRenderer = contentRenderer;
        setName("Content from " + contentRenderer);
        firePropertyChange("contentRenderer", oldContentRenderer, contentRenderer);
        if (contentRenderer != null) {
            contentRenderer.setParent(this);
            contentRenderer.addPropertyChangeListener(rendererChangeHandler);
            fireChildAdded(ReportContentRenderer.class, contentRenderer, 0);
        }
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
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        if (childType == ReportContentRenderer.class) {
            return 0;
        } else {
            throw new UnsupportedOperationException("Content boxes don't have children of type " + childType);
        }
    }

    /**
     * Included to complete the WabitObject implementation. For direct use of
     * this class, it's usually better to use {@link #getContentRenderer()} because
     * there can only ever be 0 or 1 children.
     */
    public List<WabitObject> getChildren() {
        if (contentRenderer == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList((WabitObject) getContentRenderer());
        }
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
