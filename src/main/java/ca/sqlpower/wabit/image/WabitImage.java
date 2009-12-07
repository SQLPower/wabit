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

package ca.sqlpower.wabit.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * The image saved in this object can be used in different reports in Wabit.
 */
public class WabitImage extends AbstractWabitObject {
    
    private Image image;
    
    /**
     * This will cache the image as a 16x16 icon for use in trees, lists, and other
     * places.
     */
    private Icon imageAsIcon;
    
    public WabitImage(WabitImage wabitImage) {
    	super();
    	image = wabitImage.getImage();
    	setImageAsIcon();
    }
    
    public WabitImage() {
        super();
    }
    
    public WabitImage(String uuid) {
        super(uuid);
    }

    public void setImage(Image image) {
        Image oldImage = this.image;
        this.image = image;
        setImageAsIcon();
        firePropertyChange("image", oldImage, image);
    }

    /**
     * This will cache the current image in this object as an icon.
     */
    private void setImageAsIcon() {
        if (image != null) {
            final int width = 16;
            final int height = 16;
            final BufferedImage bufferedImage = 
                new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = bufferedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, new Color(0xffffffff, true), null);
            g.dispose();
        
            imageAsIcon = new ImageIcon(bufferedImage);
        } else {
            imageAsIcon = null;
        }
    }
    
    public Image getImage() {
        return image;
    }
    
    public Icon getImageAsIcon() {
        return imageAsIcon;
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return new ArrayList<WabitObject>();
    }

    public List<WabitObject> getDependencies() {
        return new ArrayList<WabitObject>();
    }
    
    public void removeDependency(SPObject dependency) {
        //do nothing
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }

}
