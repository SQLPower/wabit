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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.WabitWorkspace;

/**
 * Represents a box on the page which has an absolute position and size.
 * The content of the box is provided by a ContentRenderer implementation.
 * Whenever the content renderer's appearance changes, this box will fire
 * a PropertyChangeEvent with the property name "content". The old and new
 * values will
 */
public class ContentBox extends AbstractWabitObject {
	
	private static final Logger logger = Logger.getLogger(ContentBox.class);
	
	private static final String EMPTY_BOX_NAME = "Empty Content Box";

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

   /**
    * The listeners that will be notified when this content box makes a request
    * to repaint.
    */
   private final List<RepaintListener> repaintListeners = new ArrayList<RepaintListener>();
    
    /**
     * This adds and removes listeners from children of the renderer when the
     * children of a renderer changes. It also fires a repaint request when 
     * there is a change to the children of this content box.
     */
    private final SPListener childListener = new AbstractSPListener() {
        
        @Override
		public void childRemovedImpl(SPChildEvent e) {
        	SQLPowerUtils.unlistenToHierarchy(e.getChild(), childListener);
		}
		
        @Override
		public void childAddedImpl(SPChildEvent e) {
        	SQLPowerUtils.listenToHierarchy(e.getChild(), childListener);
		}

        @Override
        public void propertyChangeImpl(PropertyChangeEvent evt) {
        	WabitWorkspace workspace = WabitUtils.getWorkspace(ContentBox.this);
            if (evt.getPropertyName().equals("name") && evt.getNewValue() != null 
                    && ((String) evt.getNewValue()).length() > 0
                    && evt.getSource() == contentRenderer
                    && (workspace == null || workspace.isMagicEnabled())) {
                setName("Content from " + (String) evt.getNewValue());
            }
            repaint();           
        }

	};
    
    public ContentBox() {
        setName(EMPTY_BOX_NAME);
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
    		newContentRenderer = new ChartRenderer((ChartRenderer) oldContentRenderer);
    	} else if (oldContentRenderer == null) {
    		newContentRenderer = null;
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
    	//not setting the content renderer if it is already set as it would
    	//clean up the content renderer.
    	if (contentRenderer == this.contentRenderer)  return;
    	
        ReportContentRenderer oldContentRenderer = this.contentRenderer;
        if (oldContentRenderer != null) {
        	CleanupExceptions cleanupObject = SQLPowerUtils.cleanupSPObject(oldContentRenderer);
        	SQLPowerUtils.displayCleanupErrors(cleanupObject, getSession().getContext());
        	SQLPowerUtils.unlistenToHierarchy(oldContentRenderer, childListener);
            oldContentRenderer.setParent(null);
            fireChildRemoved(ReportContentRenderer.class, oldContentRenderer, 0);
        }
        this.contentRenderer = contentRenderer;
        WabitWorkspace workspace = WabitUtils.getWorkspace(this);
        if (contentRenderer != null) {
        	if (workspace == null || workspace.isMagicEnabled()) {
        		if (getParent() != null) {
        			getParent().setUniqueName(ContentBox.this,
        					"Content from " + contentRenderer.getName());
        		} else {
        			setName("Content from " + contentRenderer.getName());
        		}
        	}
            contentRenderer.setParent(this);
            SQLPowerUtils.listenToHierarchy(contentRenderer, childListener);
            fireChildAdded(ReportContentRenderer.class, contentRenderer, 0);
        } else {
        	if (workspace == null || workspace.isMagicEnabled()) {
        		setName(EMPTY_BOX_NAME);
        	}
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

    public int childPositionOffset(Class<? extends SPObject> childType) {
    	if (ReportContentRenderer.class.isAssignableFrom(childType)) {
    		return 0;
    	} else {
            throw new IllegalArgumentException("Content boxes don't have children of type " + childType);
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
        Font oldFont = getFont();
        this.font = font;
        firePropertyChange("font", oldFont, font);
    }

	public Rectangle2D getBounds() {
		return new Rectangle2D.Double(x, y, width, height);
	}
	
    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        if (contentRenderer != null) {
            contentRenderer.removeDependency(dependency);
        }
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        if (child != null && child.equals(getContentRenderer())) {
            setContentRenderer(null);
            return true;
        }
        return false;
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
        if (index > 0) {
            throw new IllegalArgumentException("There is only one child in a content box, " +
            		"index " + index + " is out of range");
        }
        setContentRenderer((ReportContentRenderer) child);
    }

    /**
     * Adds a listener to this object that will be notified when the object
     * wants to repaint.
     */
    public void addRepaintListener(RepaintListener listener) {
        repaintListeners.add(listener);
    }
    
    public void removeRepaintListener(RepaintListener listener) {
        repaintListeners.remove(listener);
    }
    
    public void repaint() {
        runInForeground(new Runnable() {
            public void run() {
                for (int i = repaintListeners.size() - 1; i >= 0; i--) {
                    repaintListeners.get(i).requestRepaint();
                }
            }
        });
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(ResultSetRenderer.class);
    	types.add(CellSetRenderer.class);
    	types.add(ChartRenderer.class);
    	types.add(ImageRenderer.class);
    	types.add(Label.class);
    	return types;
    }

}
