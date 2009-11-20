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

import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;

/**
 * This class will let users import an image into their layout.
 */
public class ImageRenderer extends AbstractWabitObject implements
		WabitObjectReportRenderer {
	
	private static final Logger logger = Logger.getLogger(ImageRenderer.class);
	
	/**
	 * XXX This should be a final value given to the constructor rather than be settable.
	 */
	private WabitImage image;

	/**
	 * If the image is preserving the aspect ratio then this will decide
	 * its position in the content box horizontally.
	 */
	private HorizontalAlignment hAlign = HorizontalAlignment.CENTER;
	
	/**
	 * If the image is preserving the aspect ratio then this will decide
	 * its position in the content box vertically.
	 */
	private VerticalAlignment vAlign = VerticalAlignment.MIDDLE;

	/**
	 * If this is true then the image will be displayed with its aspect ratio
	 * preserved. This will prevent odd stretching of the image. If this is
	 * false then the image will be stretched to fit the content box.
	 */
	private boolean preservingAspectRatio = true;
	
	/**
	 * If this value is true and the content box this renderer is contained in
	 * is resized the aspect ratio will no longer be preserved. Otherwise the
	 * resizing of the content box will preserve the aspect ratio.
	 * <p>
	 * This property does not need to be persisted.
	 */
	private boolean preserveAspectRatioWhenResizing = true;

	/**
	 * This listener us attached to the parent content box of this image
	 * renderer and will listen for changes to the width and height. If the
	 * box's width or height changes it will update the
	 * {@link #preservingAspectRatio} value based on the
	 * {@link #preserveAspectRatioWhenResizing} value.
	 */
	private final SPListener contentBoxResizingListener = 
		new AbstractSPListener() {
	
		public void propertyChangeImpl(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("width") 
					|| evt.getPropertyName().equals("height")) {
				setPreservingAspectRatio(isPreserveAspectRatioWhenResizing()); 
			}
		}
	};
	
	private final SPListener imageListener = 
		new AbstractSPListener() {
		
		public void propertyChangeImpl(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("image")) {
				if (getParent() != null) {
					((ContentBox) getParent()).repaint();
				}
			}
		}
	};
	
	public ImageRenderer() {
		setName("ImageRenderer");
	}
	
	/**
	 * Copy constructor
	 */
	public ImageRenderer(ImageRenderer imageRenderer) {
	    this();
		this.image = imageRenderer.getImage();
		this.hAlign = imageRenderer.getHAlign();
		this.vAlign = imageRenderer.getVAlign();
		this.preserveAspectRatioWhenResizing = imageRenderer.isPreserveAspectRatioWhenResizing();
		this.preservingAspectRatio = imageRenderer.isPreservingAspectRatio();
	}
	
	public WabitObject getContent(){
		return image;
	}

	public Color getBackgroundColour() {
		return null;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
		if (image.getImage() == null) {
			g.drawString("Empty image", 0, g.getFontMetrics().getHeight());
			return false;
		}
		
		ImageIcon imageIcon = new ImageIcon(image.getImage());
		int width;
		int height;
		if (isPreservingAspectRatio()) {
		
			double widthRatio = 
				(double) contentBox.getWidth() / (double) imageIcon.getIconWidth();
			double heightRatio = 
				(double) contentBox.getHeight() / (double) imageIcon.getIconHeight();
			double sizeRatio = Math.min(widthRatio, heightRatio);
			width = (int) (imageIcon.getIconWidth() * sizeRatio);
			height = (int) (imageIcon.getIconHeight() * sizeRatio);
		} else {
			width = (int) contentBox.getWidth();
			height = (int) contentBox.getHeight();
		}
		
		int x = 0;
		if (isPreservingAspectRatio()) {
		    switch (getHAlign()) {
    		    case LEFT:
    		        x = 0;
    		        break;
    		    case CENTER:
    		        x = (int) (contentBox.getWidth() - width) / 2;
    		        break;
    		    case RIGHT:
    		        x = (int) (contentBox.getWidth() - width);
    		        break;
		    }
		}
		
		int y = 0;
		if (isPreservingAspectRatio()) {
		    switch (getVAlign()) {
    		    case TOP:
    		        y = 0;
    		        break;
    		    case MIDDLE:
    		        y = (int) ((contentBox.getHeight() - height) / 2);
    		        break;
    		    case BOTTOM:
    		        y = (int) (contentBox.getHeight() - height);
    		        break;
		    }
		}
		
		g.drawImage(image.getImage(), x, y, width, height, null);
		logger.debug("Image rendered");
		return false;
	}

	public void resetToFirstPage() {
		//no-op
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.emptyList();
	}
	
	public WabitImage getImage() {
		return image;
	}
	
	public void setImage(WabitImage image) {
	    WabitImage oldImage = this.image;
	    if (oldImage != null) {
	    	oldImage.removeSPListener(imageListener);
	    }
		this.image = image;
		if (image != null) {
			image.addSPListener(imageListener);
		} 
		firePropertyChange("image", oldImage, image);
	}

    public List<WabitObject> getDependencies() {
        if (getImage() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getImage()));
    }
    
    public void removeDependency(SPObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }

    @Override
    public void setParent(SPObject parent) {
    	if (getParent() != parent) {
    		if (getParent() != null) {
    			getParent().removeSPListener(
    					contentBoxResizingListener);
    		}
    		if (parent != null) {
    			parent.addSPListener(
    					contentBoxResizingListener);
    		}
    	}
    	super.setParent(parent);
    }

	public void setPreserveAspectRatioWhenResizing(
			boolean preserveAspectRatioWhenResizing) {
	    boolean oldValue = this.preserveAspectRatioWhenResizing;
		this.preserveAspectRatioWhenResizing = preserveAspectRatioWhenResizing;
		firePropertyChange("preserveAspectRatioWhenResizing", oldValue, 
		        preserveAspectRatioWhenResizing);
	}

	public boolean isPreserveAspectRatioWhenResizing() {
		return preserveAspectRatioWhenResizing;
	}

	public void setPreservingAspectRatio(boolean preservingAspectRatio) {
	    boolean oldValue = this.preservingAspectRatio;
		this.preservingAspectRatio = preservingAspectRatio;
		firePropertyChange("preservingAspectRatio", oldValue, preservingAspectRatio);
	}

	public boolean isPreservingAspectRatio() {
		return preservingAspectRatio;
	}

	public void refresh() {
		// no-op for now, but if the image file changes, perhaps there's a use for it? 
	}

    public void setHAlign(HorizontalAlignment hAlign) {
        HorizontalAlignment oldAlign = this.hAlign;
        this.hAlign = hAlign;
        firePropertyChange("HAlign", oldAlign, hAlign);
    }

    public HorizontalAlignment getHAlign() {
        return hAlign;
    }

    public void setVAlign(VerticalAlignment vAlign) {
        VerticalAlignment oldAlign = this.vAlign;
        this.vAlign = vAlign;
        firePropertyChange("VAlign", oldAlign, vAlign);
    }

    public VerticalAlignment getVAlign() {
        return vAlign;
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
}
