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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;

/**
 * This class will let users import an image into their layout.
 */
public class ImageRenderer extends AbstractWabitObject implements
		ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(ImageRenderer.class);
	
	private WabitImage image;

	private String filename;

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
	private final PropertyChangeListener contentBoxResizingListener = 
		new PropertyChangeListener() {
	
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("width") 
					|| evt.getPropertyName().equals("height")) {
				setPreservingAspectRatio(isPreserveAspectRatioWhenResizing()); 
			}
		}
	};
	
	public void cleanup() {
		//do nothing
	}
	
	public ImageRenderer() {
		//default constructor
	}
	
	/**
	 * Copy constructor
	 */
	public ImageRenderer(ImageRenderer imageRenderer) {
		this.image = imageRenderer.getImage();
		this.filename = imageRenderer.getFilename();
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
		
		g.drawImage(image.getImage(), 0, 0, width, height, null);
		logger.debug("Image rendered");
		return false;
	}

	public void resetToFirstPage() {
		//no-op
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.emptyList();
	}
	
	public WabitImage getImage() {
		return image;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setImage(WabitImage image) {
	    WabitImage oldImage = this.image;
		this.image = image;
		firePropertyChange("image", oldImage, image);
	}

    public List<WabitObject> getDependencies() {
        if (getImage() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getImage()));
    }
    
    @Override
    public void setParent(WabitObject parent) {
    	if (getParent() != parent) {
    		if (getParent() != null) {
    			getParent().removePropertyChangeListener(
    					contentBoxResizingListener);
    		}
    		if (parent != null) {
    			parent.addPropertyChangeListener(
    					contentBoxResizingListener);
    		}
    	}
    	super.setParent(parent);
    }

	public void setPreserveAspectRatioWhenResizing(
			boolean preserveAspectRatioWhenResizing) {
		this.preserveAspectRatioWhenResizing = preserveAspectRatioWhenResizing;
	}

	public boolean isPreserveAspectRatioWhenResizing() {
		return preserveAspectRatioWhenResizing;
	}

	public void setPreservingAspectRatio(boolean preservingAspectRatio) {
		this.preservingAspectRatio = preservingAspectRatio;
	}

	public boolean isPreservingAspectRatio() {
		return preservingAspectRatio;
	}

}
