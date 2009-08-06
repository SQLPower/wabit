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

package ca.sqlpower.wabit.swingui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.swingui.ComposedIcon;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.FolderNode;

public class WorkspaceTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final Logger logger = Logger.getLogger(WorkspaceTreeCellRenderer.class);

    public static final Icon PAGE_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/page_white.png"));
    public static final Icon LAYOUT_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/layout.png"));
    public static final Icon BOX_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/shape_square.png"));
    public static final Icon QUERY_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/query-db.png"));
    public static final Icon STREAMING_QUERY_BADGE = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/stream-badge.png"));
    public static final Icon OLAP_QUERY_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/query-olap.png"));
    public static final Icon DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/dataSources-db.png"));
    public static final Icon OLAP_DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/dataSources-olap.png"));
    
    /**
     * This map contains {@link WabitObject}s that have an image or badge
     * that is currently moving. The Integer value notes at what point in
     * the sequence the image should be.
     */
    private final Map<WabitObject, Integer> objectToTimedImageMap = new HashMap<WabitObject, Integer>();
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        WorkspaceTreeCellRenderer r = (WorkspaceTreeCellRenderer) super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof WabitObject) {
            WabitObject wo = (WabitObject) value;
            
            r.setText(wo.getName());

            if (wo instanceof WabitDataSource) {
                SPDataSource ds = ((WabitDataSource) wo).getSPDataSource();
                if (ds instanceof JDBCDataSource) {
                    r.setIcon(DB_ICON);
                } else if (ds instanceof Olap4jDataSource) {
                    r.setIcon(OLAP_DB_ICON);
                } else {
                    r.setIcon(DBTreeCellRenderer.DB_ICON);
                }
            } else if (wo instanceof Page) {
                Page page = (Page) wo;
                r.setIcon(PAGE_ICON);
                r.setText(page.getName() + " (" + page.getWidth() + "x" + page.getHeight() + ")");
            } else if (wo instanceof Layout) {
                r.setIcon(LAYOUT_ICON);
            } else if (wo instanceof ContentBox) {
                ContentBox cb = (ContentBox) wo;
                r.setIcon(BOX_ICON);
                r.setText(cb.getName() + " ("+cb.getX()+","+cb.getY()+" "+cb.getWidth()+"x"+cb.getHeight()+")");
            } else if (wo instanceof Guide) {
            	Guide g = (Guide) wo;
            	r.setText(g.getName() + " @" + g.getOffset());
            } else if (wo instanceof QueryCache) {
            	if (((QueryCache) wo).isRunning()) {
            		if (((QueryCache) wo).isStreaming()) {
            			r.setIcon(new ComposedIcon(Arrays.asList(new Icon[]{QUERY_ICON, STREAMING_QUERY_BADGE})));
            		} else {
            			if (objectToTimedImageMap.containsKey(wo)) {
            				logger.debug("The image for " + wo + " should be at position " + objectToTimedImageMap.get(wo));
            				int imageNumber = (objectToTimedImageMap.get(wo) % 12) + 1;
            				final String imageURL = "icons/throbber-badge_" + imageNumber + ".png";
            				logger.debug("Loading image: " + imageURL);
							r.setIcon(new ComposedIcon(Arrays.asList(new Icon[]{QUERY_ICON, new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource(imageURL))})));
            			} else { 
            				r.setIcon(QUERY_ICON);
            			}
            		}
            	} else {
            		r.setIcon(QUERY_ICON);
            	}
            } else if (wo instanceof OlapQuery) {
                r.setIcon(OLAP_QUERY_ICON);
            } else if (wo instanceof WabitImage) {
                final Image wabitImage = ((WabitImage) wo).getImage();
                if (wabitImage != null) {
                    final int width = r.getIcon().getIconWidth();
                    final int height = r.getIcon().getIconHeight();
                    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g = image.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(wabitImage, 0, 0, width, height, new Color(0xffffffff, true), null);
                    g.dispose();
                
                    final ImageIcon icon = new ImageIcon(image);
                    r.setIcon(icon);
                }
            }

        } else if (value instanceof FolderNode) {
        	FolderNode folder = ((FolderNode) value);
        	r.setText(folder.toString());
        }
        return r;
    }

    /**
     * Updates the frame counter for the given wabit object's "busy badge."
     * <p>
     * Important things to keep in mind:
     * <ol>
     *  <li>Calling this method does not cause the bagde to repaint; you will
     *      have to ask the tree to repaint on your own
     *  <li>this method <i>must</i> be called on the AWT/Swing Event Dispatch
     *      Thread.
     * </ol>
     * 
     * @param object
     *            The tree object that should have a "busy" badge on its icon
     * @param frameNum
     *            The new frame number. This value will automatically be
     *            "wrapped" to the actual number of frames in the badge's
     *            animation, so a monotonically increasing integer is
     *            sufficient.
     */
	public void updateTimer(WabitObject object, Integer frameNum) {
		logger.debug("Received update event of " + frameNum);
		if (!SwingUtilities.isEventDispatchThread()) {
		    throw new IllegalStateException("This method can only be called on the event dispatch thread");
		}
		if (object instanceof QueryCache && !((QueryCache) object).isPhantomQuery()) {
		    objectToTimedImageMap.put(object, frameNum);
		}
	}
	
    /**
     * Causes the given Wabit object to not have a "busy badge" next time it is rendered.
     * <p>
     * Important things to keep in mind:
     * <ol>
     *  <li>Calling this method does not cause the bagde to repaint; you will
     *      have to ask the tree to repaint on your own
     *  <li>this method <i>must</i> be called on the AWT/Swing Event Dispatch
     *      Thread.
     * </ol>
     * 
     * @param object
     *            The tree object that should have a "busy" badge on its icon
     */
	public void removeTimer(WabitObject object) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method can only be called on the event dispatch thread");
        }
		objectToTimedImageMap.remove(object);
	}
}
