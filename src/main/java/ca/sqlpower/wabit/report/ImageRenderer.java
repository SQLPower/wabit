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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class will let users import an image into their layout.
 */
public class ImageRenderer extends AbstractWabitObject implements
		ReportContentRenderer {
	
	private static final Logger logger = Logger.getLogger(ImageRenderer.class);
	
	private class ImageEntryPanel implements DataEntryPanel {
    
	    private final JPanel panel;
        private JList imageList;
        private int startingImageIndex;
        private ListCellRenderer oldCellRenderer;
	    
	    public ImageEntryPanel(WabitWorkspace workspace, WabitImage startingImage) {
	        panel = new JPanel(new BorderLayout());
	        imageList = new JList(workspace.getImages().toArray());
	        
	        oldCellRenderer = imageList.getCellRenderer();
	        imageList.setCellRenderer(new ListCellRenderer() {
            
                public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    Component comp = 
                        oldCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setText(((WabitObject) value).getName());
                    }
                    return comp;
                }
            });
	        startingImageIndex = workspace.getImages().indexOf(startingImage);
            imageList.setSelectedIndex(startingImageIndex);
            imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            panel.add(new JLabel("Select the desired image below"), BorderLayout.NORTH);
	        panel.add(new JScrollPane(imageList), BorderLayout.CENTER);
	        logger.debug("Created the image entry panel.");
	    }
	    
        public boolean hasUnsavedChanges() {
            return !(startingImageIndex == imageList.getSelectedIndex());
        }
    
        public JComponent getPanel() {
            return panel;
        }
    
        public void discardChanges() {
            //no-op
        }
    
        public boolean applyChanges() {
            setImage((WabitImage) imageList.getSelectedValue());
            return true;
        }
    };
	
	private WabitImage image;

	private String filename;
    private final WabitWorkspace workspace;
	
	public ImageRenderer(WabitWorkspace workspace, boolean showSelectOnCreation) {
        this.workspace = workspace;
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
	    return new ImageEntryPanel(workspace, getImage());
	}

	public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
			double scaleFactor, int pageIndex, boolean printing) {
		g.drawImage(image.getImage(), 0, 0, (int) contentBox.getWidth(), (int) contentBox.getHeight(), null);
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

    public void processEvent(PInputEvent event, int type) {
        //do nothing
    }

    public List<WabitObject> getDependencies() {
        if (getImage() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getImage()));
    }

}
