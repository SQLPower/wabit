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

package ca.sqlpower.wabit.swingui.report;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.report.ImageRenderer;
import edu.umd.cs.piccolo.event.PInputEvent;

public class ImageSwingRenderer implements SwingContentRenderer {
    
    private static final Logger logger = Logger.getLogger(ImageSwingRenderer.class);

    private static class ImageEntryPanel implements DataEntryPanel {
        
        private final JPanel panel;
        private JList imageList;
        private int startingImageIndex;
        private ListCellRenderer oldCellRenderer;
        private final ImageRenderer renderer;
        
        public ImageEntryPanel(WabitWorkspace workspace, ImageRenderer renderer) {
            this.renderer = renderer;
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
            startingImageIndex = workspace.getImages().indexOf(renderer.getImage());
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
            renderer.setImage((WabitImage) imageList.getSelectedValue());
            return true;
        }
    };
    
    private final WabitWorkspace workspace;
    private final ImageRenderer renderer;
    
    public ImageSwingRenderer(WabitWorkspace workspace, ImageRenderer renderer) {
        this.workspace = workspace;
        this.renderer = renderer;
        
    }

    public DataEntryPanel getPropertiesPanel() {
        return new ImageEntryPanel(workspace, renderer);
    }

    public void processEvent(PInputEvent event, int type) {
        if (type == KeyEvent.KEY_PRESSED) {
        	if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
        		renderer.setPreserveAspectRatioWhenResizing(false);
        	}
        } else if (type == KeyEvent.KEY_RELEASED) {
        	if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
        		renderer.setPreserveAspectRatioWhenResizing(true);
        	}
        }
    }

}
