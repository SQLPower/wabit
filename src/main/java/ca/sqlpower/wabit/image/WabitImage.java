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

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

/**
 * The image saved in this object can be used in different reports in Wabit.
 */
public class WabitImage extends AbstractWabitObject {
    
    private Image image;
    
    public WabitImage() {
        super();
    }
    
    public WabitImage(String uuid) {
        super(uuid);
    }

    public void setImage(Image image) {
        Image oldImage = this.image;
        this.image = image;
        firePropertyChange("image", oldImage, image);
    }
    
    public Image getImage() {
        return image;
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return new ArrayList<WabitObject>();
    }

    public List<WabitObject> getDependencies() {
        return new ArrayList<WabitObject>();
    }

}
