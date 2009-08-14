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
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitChildListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;

/**
 * Represents a box on the page which has an absolute position and size.
 * The content of the box is provided by a ContentRenderer implementation.
 * Whenever the content renderer's appearance changes, this box will fire
 * a PropertyChangeEvent with the property name "content". The old and new
 * values will
 */
public class ContentBox extends AbstractWabitObject {

    private double x;
    private double y;
    private double width;
    private double height;

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
            if (evt.getPropertyName().equals("name") && evt.getNewValue() != null 
                    && ((String) evt.getNewValue()).length() > 0) {
                setName("Content from " + (String) evt.getNewValue());
            }
            firePropertyChange("content", null, null);
        }
        
    };

    /**
     * This adds and removes listeners from children of the renderer when the
     * children of a renderer changes.
     */
    private final WabitChildListener emptyChildListener = new WabitChildListener() {
        
		public void wabitChildRemoved(WabitChildEvent e) {
		    WabitUtils.unlistenToHierarchy(e.getChild(), rendererChangeHandler, emptyChildListener);
		}
		
		public void wabitChildAdded(WabitChildEvent e) {
		    WabitUtils.listenToHierarchy(e.getChild(), rendererChangeHandler, emptyChildListener);
		}
		
	};
    
    public ContentBox() {
        // This is just to initialize this content box's name
        setContentRenderer(null);
    }
    
    /**
     * Copy Constructor
     * @param contentBox
     */
    public ContentBox(ContentBox contentBox) {
    	this.x = contentBox.x;
    	this.y = contentBox.y;
    	this.font = contentBox.font;
    	this.height = contentBox.height;
    	this.width = contentBox.width;
    	setName(contentBox.getName() + " Copy");
    	
    	ReportContentRenderer oldContentRenderer = contentBox.contentRenderer;
    	ReportContentRenderer newContentRenderer = null;
    	if (oldContentRenderer instanceof ResultSetRenderer) {
    		newContentRenderer = new ResultSetRenderer((ResultSetRenderer) oldContentRenderer);
    	} else if (oldContentRenderer instanceof CellSetRenderer) {
    		newContentRenderer = new CellSetRenderer((CellSetRenderer) oldContentRenderer);
    	} else if (oldContentRenderer instanceof ImageRenderer) {
    		newContentRenderer = new ImageRenderer((ImageRenderer) oldContentRenderer);
    	} else if (oldContentRenderer instanceof Label) {
    		Label newLabel = new Label((Label) oldContentRenderer);
			newContentRenderer = newLabel;
    		newLabel.setParent(this);
    	} else if (oldContentRenderer instanceof ChartRenderer) {
    		//TODO
//    		newContentRenderer = new ChartRenderer((ChartRenderer) oldContentRenderer);
    	} else {
    		throw new UnsupportedOperationException("ContentRenderer of type " + oldContentRenderer.getClass().getName()
    				+ " not yet supported for copying.");
    		
    	}
    	setContentRenderer(newContentRenderer);
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
        	oldContentRenderer.cleanup();
            WabitUtils.unlistenToHierarchy(oldContentRenderer, rendererChangeHandler, emptyChildListener);
            oldContentRenderer.setParent(null);
            fireChildRemoved(ReportContentRenderer.class, oldContentRenderer, 0);
        }
        this.contentRenderer = contentRenderer;
        firePropertyChange("contentRenderer", oldContentRenderer, contentRenderer);
        if (contentRenderer != null) {
            if(getName() == null || getName().contains("Empty content box")) {
            	if (getParent() != null) {
					getParent().setUniqueName(ContentBox.this,
							"Content from " + contentRenderer.getName());
				} else {
					setName("Content from " + contentRenderer.getName());
				}
            }
            contentRenderer.setParent(this);
            WabitUtils.listenToHierarchy(contentRenderer, rendererChangeHandler, emptyChildListener);
            fireChildAdded(ReportContentRenderer.class, contentRenderer, 0);
        } else if (getName() == null){
            setName("Empty content box");
        }
    }
    
    public ReportContentRenderer getContentRenderer() {
        return contentRenderer;
    }
    
    @Override
    public Page getParent() {
        return (Page) super.getParent();
    }
    
    public double getX() {
        return x;
    }
    public void setX(double x) {
        double oldX = this.x;
        this.x = x;
        firePropertyChange("x", oldX, x);
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
    	double oldY = this.y;
        this.y = y;
        firePropertyChange("y", oldY, y);
    }
    
    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        double oldWidth = this.width;
        this.width = width;
        firePropertyChange("width", oldWidth, width);
    }
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        double oldHeight = this.height;
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

	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(x, y, width, height);
	}
	
	public void cleanup() {
		contentRenderer.cleanup();
	}

    public List<WabitObject> getDependencies() {
        if (contentRenderer == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(contentRenderer));
    }

}
