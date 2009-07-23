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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class will let users import an image into their layout.
 */
public class ImageRenderer extends AbstractWabitObject implements
		ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(ImageRenderer.class);
	
	private BufferedImage image;
	private final ContentBox parent;

	private final Component parentComponent;
	
	private String filename;
	
	public ImageRenderer(ContentBox parent, Component parentComponent, boolean showSelectOnCreation) {
		this.parent = parent;
		this.parentComponent = parentComponent;
		if (showSelectOnCreation) {
			getPropertiesPanel();
		}
	}
	
	public void cleanup() {
		//do nothing
	}

	public Color getBackgroundColour() {
		return null;
	}

	public DataEntryPanel getPropertiesPanel() {
		String oldFileName = null;
		if (getName() != null) {
			oldFileName = getName();
		}
		JFileChooser imageChooser = new JFileChooser();
		int retVal = imageChooser.showOpenDialog(parentComponent);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			logger.debug("Chosen file is " + imageChooser.getSelectedFile().getAbsolutePath());
			try {
				image = ImageIO.read(imageChooser.getSelectedFile());
				filename = imageChooser.getSelectedFile().getAbsolutePath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			setName("Image: " + imageChooser.getSelectedFile().getName());
			// If name for the content box has not been changed to something
			// user-defined, we change it everytime a
			// different image is selected
			if (parent.getName() != null && oldFileName != null	&& (parent.getName().contains(oldFileName))) {
				parent.getParent().setUniqueName(parent,
						parent.getName().replace(oldFileName, getName()));
			}
			parent.setWidth(image.getWidth(parentComponent));
			parent.setHeight(image.getHeight(parentComponent));
		} else if (image == null) {
			//Giving the content box some size to let the user click on it.
			parent.setWidth(100);
			parent.setHeight(100);
			setName("Image: not defined");
		}
		return null;
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
		g.drawImage(image, 0, 0, null);
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
	
	public BufferedImage getImage() {
		return image;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

    public void processEvent(PInputEvent event, int type) {
        //do nothing
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

}
