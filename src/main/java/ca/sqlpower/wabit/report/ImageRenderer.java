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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	public void cleanup() {
		//do nothing
	}

	public Color getBackgroundColour() {
		return null;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
		g.drawImage(image.getImage(), 0, 0, (int) contentBox.getWidth(), (int) contentBox.getHeight(), null);
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

}
