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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent;

/**
 * This panel will allow editing a WabitImage. WabitImages can be used
 * in reports.
 */
public class WabitImagePanel implements WabitPanel {

    private static final Logger logger = Logger.getLogger(WabitImagePanel.class);
    
    /**
     * This message will be placed where you can drag and drop images to change the image.
     */
    private static final String EMPTY_IMAGE_STRING = "Drop an image here or click to browse.";
    
    /**
     * Stores the image that will be used in other parts of Wabit.
     */
    private final WabitImage image;
    
    /**
     * The panel that allows editing a WabitImage.
     */
    private final JPanel panel = new JPanel();
    
    private final Action browseForImageAction = new AbstractAction("", new ImageIcon(WabitImagePanel.class.getClassLoader().getResource("icons/32x32/open.png"))) {
    
        public void actionPerformed(ActionEvent e) {
            showImageBrowser();   
        }
    };

    private JLabel imageLabel = new JLabel(EMPTY_IMAGE_STRING);

    /**
     * When the image changes this listener will cause the image panel to repaint.
     */
    private final PropertyChangeListener imageListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            resetImage();
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
            
            logger.debug("Got drop event!");
            logger.debug("Available Flavours: "+flavorList);
            
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
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    logger.info("Could not drop the image in the following transferable");
                    for (DataFlavor df : t.getTransferDataFlavors()) {
                        Object transferable = t.getTransferData(df);
                        if (transferable instanceof InputStream) {
                            BufferedReader stream = new BufferedReader(new InputStreamReader((InputStream) transferable));
                            logger.info(df.getMimeType() + " : " + stream.readLine());
                        } else if (df.getRepresentationClass().isArray()) {
                            String stringRep;
                            if (transferable instanceof Object[]) {
                                stringRep = Arrays.toString((Object[]) transferable);
                            } else {
                                //Since this wasn't an Object array it must be one of the native arrays.
                                try {
                                    Method m = Arrays.class.getMethod("toString", transferable.getClass());
                                    stringRep = (String) m.invoke(null, transferable);
                                } catch (Exception e) {
                                    stringRep = "Couldn't convert array of type " + transferable.getClass();
                                }
                                
                            }
                            logger.info(df.getMimeType() + " : " + stringRep);
                        } else {
                            logger.info(df.getMimeType() + " : " + transferable);
                        }
                    }
                    
                    // possible stuff that we should check for but don't yet:
                    // 1. an InputStream of type image/* (could try to read it into an ImageIcon)
                    return;
                }
            } catch (UnsupportedFlavorException e) {
                logger.debug("Auto-generated catch: "+e.getMessage());
                success = false;
            } catch (IOException e) {
                logger.debug("Auto-generated catch: "+e.getMessage());
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
        
        /**
         * Helper method for the {@link #drop(DropTargetDropEvent)} method.
         * This is taken from PictureDropListener in WebCMS.
         */
        private DataFlavor searchListForType(List<DataFlavor> flavorList, String mimeType, Class<?> clazz) {
            Iterator<DataFlavor> it = flavorList.iterator();
            while (it.hasNext()) {
                DataFlavor f = it.next();
                if (f.getRepresentationClass().equals(clazz) && f.getMimeType().contains(mimeType)) {
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

    /**
     * The panel that contains the image label. Resizing this panel will cause the image to resize.
     */
    private JPanel imagePanel;

    public WabitImagePanel(WabitImage image, WabitSwingSessionContext context) {
        this.image = image;
        this.context = context;

        JToolBar toolBar = new JToolBar();
        toolBar.add(browseForImageAction);
        imagePanel = new JPanel(new MigLayout("align 50% 50%"));
        
        panel.setLayout(new BorderLayout());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(imagePanel, BorderLayout.CENTER);
        imagePanel.setBackground(Color.WHITE);
        imagePanel.add(imageLabel);
        
        new DropTarget(imagePanel, dndDropListener);
        
        imagePanel.addMouseListener(new MouseAdapter() {
        
            @Override
            public void mouseReleased(MouseEvent e) {
                showImageBrowser();
            }
        
        });
        
        imagePanel.addComponentListener(new ComponentAdapter() {
        
            public void componentResized(ComponentEvent e) {
                resetImage();
            }
        
        });
        
        imagePanel.setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
        
        image.addPropertyChangeListener(imageListener);
    }
    
    /**
     * Helper method to update the image displayed in the editor based on
     * the image in the {@link WabitImage}.
     */
    private void resetImage() {
        if (image.getImage() != null) {
            imageLabel.setText("");
            ImageIcon icon = new ImageIcon(image.getImage());
            
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            double reduceWidthRatio = 1;
            double reduceHeightRatio = 1;
            
            if (iconWidth > imagePanel.getWidth()) {
                reduceWidthRatio = ((double) imagePanel.getWidth()) / ((double) iconWidth); 
            }
            if (iconHeight > imagePanel.getHeight()) {
                reduceHeightRatio = ((double) imagePanel.getHeight()) / ((double) iconHeight); 
            }
            
            double reduceSizeRatio = Math.min(reduceWidthRatio, reduceHeightRatio);
            
            iconWidth = (int) (reduceSizeRatio * iconWidth);
            iconHeight = (int) (reduceSizeRatio * iconHeight);
            
            imageLabel.setIcon(new ImageIcon(image.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH)));
            imagePanel.setBorder(null);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText(EMPTY_IMAGE_STRING);
            imagePanel.setBorder(CellSetTableHeaderComponent.ROUNDED_DASHED_BORDER);
        }
    }
    
    /**
     * Helper method to show the {@link JFileChooser} to choose an image.
     */
    private void showImageBrowser() {
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

    public String getTitle() {
		return "Image Viewer - " + image.getName();
	}
}
