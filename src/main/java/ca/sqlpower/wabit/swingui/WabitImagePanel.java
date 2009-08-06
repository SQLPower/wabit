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

package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.image.WabitImage;

/**
 * This panel will allow editing a WabitImage. WabitImages can be used
 * in reports.
 */
public class WabitImagePanel implements WabitPanel {

    private static final Logger logger = Logger.getLogger(WabitImagePanel.class);
    
    /**
     * This message will be placed where you can drag and drop images to change the image.
     */
    private static final String EMPTY_IMAGE_STRING = "Drag and drop an image here.";
    
    /**
     * Stores the image that will be used in other parts of Wabit.
     */
    private final WabitImage image;
    
    /**
     * The panel that allows editing a WabitImage.
     */
    private final JPanel panel = new JPanel();
    
    private final Action browseForImageAction = new AbstractAction("", WabitSwingSessionContextImpl.OPEN_WABIT_ICON) {
    
        public void actionPerformed(ActionEvent e) {
            JFileChooser imageChooser = new JFileChooser();
            int retVal = imageChooser.showOpenDialog(context.getFrame());
            if (retVal == JFileChooser.APPROVE_OPTION) {
                logger.debug("Chosen file is " + imageChooser.getSelectedFile().getAbsolutePath());
                try {
                    image.setImage(ImageIO.read(imageChooser.getSelectedFile()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                image.setName("Image: " + imageChooser.getSelectedFile().getName());
            } else if (image == null) {
                image.setName("Image: not defined");
            }    
        }
    };

    /**
     * This panel should have it's one child, the image of the {@link #image}
     * object, replaced when the image of the {@link #image} object changes.
     */
    private final JPanel imagePanel = new JPanel() {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (image.getImage() != null) {
                final int imageWidth = image.getImage().getWidth(null);
                final int imageHeight = image.getImage().getHeight(null);
                g.drawImage(image.getImage(), (getWidth() - imageWidth) / 2, 
                        (getHeight() - imageHeight) / 2, imageWidth, 
                        imageHeight, null);
            } else {
                g.drawString(EMPTY_IMAGE_STRING, 
                        (getWidth() - g.getFontMetrics().stringWidth(EMPTY_IMAGE_STRING)) / 2, 
                        (getHeight() - g.getFontMetrics().getHeight()) / 2);
            }
        }
    };

    /**
     * When the image changes this listener will cause the image panel to repaint.
     */
    private final PropertyChangeListener imageListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            imagePanel.repaint();
        }
    };
    
    private final DropTargetListener dndDropListener = new DropTargetListener() {
    
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub
    
        }
    
        public void drop(DropTargetDropEvent dtde) {
            //Taken from WebCMS PictureDropListener
            Transferable t = dtde.getTransferable();
            List<DataFlavor> flavorList = dtde.getCurrentDataFlavorsAsList();
            
            System.out.println("Got drop event!");
            System.out.println("Available Flavours: "+flavorList);
            
            boolean success;
            try {
                DataFlavor currentFlavor = null;
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<?> fileList = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    final ImageIcon droppedImage = new ImageIcon(((File) fileList.get(0)).toURL());
                    image.setImage(droppedImage.getImage());
                    success = true;
                } else if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    final Image droppedImage = (Image) t.getTransferData(DataFlavor.imageFlavor);
                    image.setImage(droppedImage);
                    success = true;
                } else if ((currentFlavor = searchListForType(flavorList, java.net.URL.class)) != null) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    URL url = (URL) t.getTransferData(currentFlavor);
                    ImageIcon droppedImage = new ImageIcon(url);
                    image.setImage(droppedImage.getImage());
                    success = true;
                } else {
                    // possible stuff that we should check for but don't yet:
                    // 1. an InputStream of type image/* (could try to read it into an ImageIcon)
                    dtde.rejectDrop();
                    return;
                }
            } catch (UnsupportedFlavorException e) {
                System.out.println("Auto-generated catch: "+e.getMessage());
                success = false;
            } catch (IOException e) {
                System.out.println("Auto-generated catch: "+e.getMessage());
                success = false;
            }
            dtde.dropComplete(success);
        }
        
        /**
         * Helper method for the {@link #drop(DropTargetDropEvent)} method.
         * This is taken from PictureDropListener in WebCMS.
         */
        private DataFlavor searchListForType(List<DataFlavor> flavorList, Class<?> clazz) {
            Iterator<DataFlavor> it = flavorList.iterator();
            while (it.hasNext()) {
                DataFlavor f = it.next();
                if (f.getRepresentationClass().equals(clazz)) {
                    return f;
                }
            }
            return null;
        }
    
        public void dragOver(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub
    
        }
    
        public void dragExit(DropTargetEvent dte) {
            // TODO Auto-generated method stub
    
        }
    
        public void dragEnter(DropTargetDragEvent dtde) {
            // TODO Auto-generated method stub
    
        }
    };
    
    private final WabitSwingSessionContext context;

    public WabitImagePanel(WabitImage image, WabitSwingSessionContext context) {
        this.image = image;
        this.context = context;

        JToolBar toolBar = new JToolBar();
        toolBar.add(browseForImageAction);
        
        panel.setLayout(new BorderLayout());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(imagePanel, BorderLayout.CENTER);
        imagePanel.setBackground(Color.WHITE);
        new DropTarget(imagePanel, dndDropListener);
        
        image.addPropertyChangeListener(imageListener);
    }

    public void maximizeEditor() {
        //no-op
    }

    public boolean applyChanges() {
        cleanup();
        return true;
    }

    public void discardChanges() {
        cleanup();
    }
    
    private void cleanup() {
        image.removePropertyChangeListener(imageListener);
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return false;
    }

}
