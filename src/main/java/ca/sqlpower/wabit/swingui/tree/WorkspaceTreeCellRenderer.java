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
import ca.sqlpower.wabit.WabitBackgroundWorker;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer.ExistingChartTypes;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.FolderNode;

public class WorkspaceTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final Logger logger = Logger.getLogger(WorkspaceTreeCellRenderer.class);

    public static final Icon PAGE_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/page_white.png"));
    public static final Icon LAYOUT_ICON = WabitIcons.REPORT_ICON_16;
    public static final Icon BOX_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/shape_square.png"));
    public static final Icon QUERY_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/query-db-16.png"));
    public static final Icon STREAMING_QUERY_BADGE = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/stream-badge.png"));
    public static final Icon OLAP_QUERY_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/query-olap-16.png"));
    public static final Icon DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/connection-db-16.png"));
    public static final Icon OLAP_DB_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/connection-olap-16.png"));
    public static final Icon LABEL_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/label-16.png"));
    public static final Icon CHART_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-16.png"));
    public static final Icon CHART_BAR_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-bar-16.png"));
    public static final Icon CHART_SCATTER_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-scatter-16.png"));
    public static final Icon CHART_LINE_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-line-16.png"));
    public static final Icon CHART_PIE_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-pie-16.png")); //TODO ADD ME

    private static final Icon[] THROBBER_OVERLAYS;
    static {
        final int overlayFrameCount = 8;
        THROBBER_OVERLAYS = new Icon[overlayFrameCount];
        for (int imageNumber = 0; imageNumber < overlayFrameCount; imageNumber++) {
            String imageURL = String.format("icons/throbber16-%02d.png", (imageNumber + 1));
            logger.debug("Loading image: " + imageURL);
            THROBBER_OVERLAYS[imageNumber] = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource(imageURL));
        }
    }

    /**
     * Current frame number to use for the "busy bagde" ("throbber overlay").
     * 
     * @see #nextBusyBadgeFrame()
     * @see #createBusyIcon()
     */
    private int busyBadgeFrameNum;
    
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
                ReportContentRenderer cbChild = (ReportContentRenderer) cb.getChildren().get(0);
                if (cbChild instanceof ResultSetRenderer) {
                	setupForQueryCache(r, ((ResultSetRenderer) cbChild).getQuery());
                } else if (cbChild instanceof CellSetRenderer) {
                	r.setIcon(OLAP_QUERY_ICON);
                	r.setText(((CellSetRenderer) cbChild).getOlapQuery().getName());
                } else if (cbChild instanceof ImageRenderer) {
                    setupForWabitImage(r, ((ImageRenderer) cbChild).getImage());
                } else if (cbChild instanceof ChartRenderer) {
                	ChartRenderer chartRenderer = (ChartRenderer) cbChild;
                	ExistingChartTypes chartType = chartRenderer.getChartType();
                	if (chartType.equals(ExistingChartTypes.BAR)) {
                		r.setIcon(CHART_BAR_ICON);
                	} else if (chartType.equals(ExistingChartTypes.CATEGORY_LINE)) {
                		r.setIcon(CHART_LINE_ICON);
                	} else if (chartType.equals(ExistingChartTypes.SCATTER)) {
                		r.setIcon(CHART_SCATTER_ICON);
                	} else if (chartType.equals(ExistingChartTypes.LINE)) {
                		r.setIcon(CHART_LINE_ICON);
                	} else {
                		//TODO when pie charts are added change this
                		throw new UnsupportedOperationException("The pie chart icon needs to be added to the tree model");
//                		r.setIcon(CHART_ICON); 
//						r.setIcon(CHART_PIE_ICON);
                	}
					r.setText(chartRenderer.getQuery().getName());
                } else if (cbChild instanceof Label) {
                	r.setIcon(LABEL_ICON); 
	                r.setText(((Label) cbChild).getText());
                } else {
                	r.setIcon(BOX_ICON); 
	                r.setText(cbChild.getName());
                }
            } else if (wo instanceof Guide) {
            	Guide g = (Guide) wo;
            	r.setText(g.getName() + " @" + g.getOffset());
            } else if (wo instanceof QueryCache) {
            	setupForQueryCache(r, wo);
            } else if (wo instanceof OlapQuery) {
                r.setIcon(OLAP_QUERY_ICON);
            } else if (wo instanceof WabitImage) {
                setupForWabitImage(r, wo);
            }

            if (wo instanceof WabitBackgroundWorker) {
                if (((WabitBackgroundWorker) wo).isRunning()) {
                    r.setIcon(makeBusy(r.getIcon()));
                }
            }
            
        } else if (value instanceof FolderNode) {
        	FolderNode folder = ((FolderNode) value);
        	r.setText(folder.toString());
        }
        
        return r;
    }

	private void setupForWabitImage(WorkspaceTreeCellRenderer r, WabitObject wo) {
		final Image wabitImage = ((WabitImage) wo).getImage();
		if (wabitImage != null) {
		    final int width = DB_ICON.getIconWidth();
		    final int height = DB_ICON.getIconHeight();
		    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		    Graphics2D g = image.createGraphics();
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
		            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		    g.drawImage(wabitImage, 0, 0, width, height, new Color(0xffffffff, true), null);
		    g.dispose();
		
		    final ImageIcon icon = new ImageIcon(image);
		    r.setIcon(icon);
		}
		r.setText(wo.getName());
	}

	private void setupForQueryCache(WorkspaceTreeCellRenderer r, WabitObject wo) {
		if (((QueryCache) wo).isRunning()) {
			if (((QueryCache) wo).isStreaming()) {
				r.setIcon(ComposedIcon.getInstance(QUERY_ICON, STREAMING_QUERY_BADGE));
			} else {
			    r.setIcon(QUERY_ICON);
			}
		} else {
			r.setIcon(QUERY_ICON);
		}
		r.setText(wo.getName());
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
	public void nextBusyBadgeFrame() {
		if (!SwingUtilities.isEventDispatchThread()) {
		    throw new IllegalStateException("This method can only be called on the event dispatch thread");
		}
		if (busyBadgeFrameNum < THROBBER_OVERLAYS.length - 1) {
		    busyBadgeFrameNum++;
		} else {
		    busyBadgeFrameNum = 0;
		}
	}
	
	private Icon makeBusy(Icon base) {
	    return ComposedIcon.getInstance(base, THROBBER_OVERLAYS[busyBadgeFrameNum]);
	}
}
