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

import java.awt.Component;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.dbtree.DBTreeCellRenderer;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.swingui.ComposedIcon;
import ca.sqlpower.swingui.table.Arrow;
import ca.sqlpower.wabit.WabitBackgroundWorker;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.enterprise.client.ReportTask;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Guide;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.report.chart.ChartColumn;
import ca.sqlpower.wabit.report.chart.ChartType;
import ca.sqlpower.wabit.report.chart.ColumnRole;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WorkspaceGraphTreeNodeWrapper;
import ca.sqlpower.wabit.swingui.olap.Olap4JTreeCellRenderer;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeModel.Olap4jTreeObject;

public class WorkspaceTreeCellRenderer extends DefaultTreeCellRenderer {
	
	private static final Logger logger = Logger.getLogger(WorkspaceTreeCellRenderer.class);

    public static final Icon PAGE_ICON = new ImageIcon(WorkspaceTreeCellRenderer.class.getResource("/icons/page_white.png"));
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
    
    public static final Icon SQL_COLUMN_ICON = new ImageIcon(
            WorkspaceTreeCellRenderer.class.getClassLoader().getResource(
                    "ca/sqlpower/architect/swingui/dbtree/icons/Column16.png"));
    public static final Icon SORT_ASC_ARROW = new Arrow(false, SQL_COLUMN_ICON.getIconHeight() * 3 / 4, 
            0, SQL_COLUMN_ICON.getIconHeight() * 3 / 4, SQL_COLUMN_ICON.getIconHeight() / 3);
    public static final Icon SORT_DESC_ARROW = new Arrow(true, SQL_COLUMN_ICON.getIconHeight() * 3 / 4, 
            0, SQL_COLUMN_ICON.getIconHeight() * 3 / 4, SQL_COLUMN_ICON.getIconHeight() / 3);

    /** Category ChartColumn. Also available in {@link #CHART_COL_ROLE_ICONS}. */
    public static final Icon CHART_COL_CATEGORY = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-category-16.png"));

    /** Series ChartColumn. Also available in {@link #CHART_COL_ROLE_ICONS}. */
    public static final Icon CHART_COL_SERIES = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-series-16.png"));

    /** Unused (NONE) ChartColumn. Also available in {@link #CHART_COL_ROLE_ICONS}. */
    public static final Icon CHART_COL_NONE = new ImageIcon(WorkspaceTreeCellRenderer.class.getClassLoader().getResource("icons/chart-unused-16.png"));

    /** Mapping of ColumnRole types to their respective icons. */
    public static final Map<ColumnRole, Icon> CHART_COL_ROLE_ICONS;
    static {
        EnumMap<ColumnRole, Icon> m = new EnumMap<ColumnRole, Icon>(ColumnRole.class);
        m.put(ColumnRole.CATEGORY, CHART_COL_CATEGORY);
        m.put(ColumnRole.SERIES, CHART_COL_SERIES);
        m.put(ColumnRole.NONE, CHART_COL_NONE);
        CHART_COL_ROLE_ICONS = Collections.unmodifiableMap(m);
    }

    /**
     * Series of transparent icons that make the throbber animation overlay for
     * any tree node.
     */
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
    
    private final Olap4JTreeCellRenderer delegateOlap4jRenderer = new Olap4JTreeCellRenderer();
    private final DBTreeCellRenderer delegateSQLTreeCellRenderer = new DBTreeCellRenderer();
    
    /**
     * Current frame number to use for the "busy bagde" ("throbber overlay").
     * 
     * @see #nextBusyBadgeFrame()
     * @see #createBusyIcon()
     * @see #THROBBER_OVERLAYS
     */
    private int busyBadgeFrameNum;
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    	DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(
    			tree, value, sel, expanded, leaf, row, hasFocus);
        
    	Object originalValue = value;
    	if (value instanceof WorkspaceGraphTreeNodeWrapper) {
    	    value = ((WorkspaceGraphTreeNodeWrapper) value).getWrappedObject();
    	}
    	
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
            } else if (wo instanceof Report) {
                r.setIcon(WabitIcons.REPORT_ICON_16);
            } else if (wo instanceof ReportTask) {
                r.setIcon(WabitIcons.REPORT_ICON_16);
            } else if (wo instanceof Template) {
            	r.setIcon(WabitIcons.TEMPLATE_ICON_16);
            } else if (wo instanceof Group) {
            	r.setIcon(WabitIcons.GROUP_ICON_16);
            } else if (wo instanceof User) {
            	r.setIcon(WabitIcons.USER_ICON_16);
            } else if (wo instanceof ContentBox || wo instanceof ReportContentRenderer) {
                final ReportContentRenderer cbChild;
                if (wo instanceof ContentBox) {
                    ContentBox cb = (ContentBox) wo;
                    if (cb.getChildren().size() > 0) {
                        cbChild = (ReportContentRenderer) cb.getChildren().get(0);
                    } else {
                        cbChild = null;
                    }
                } else if (wo instanceof ReportContentRenderer) {
                    cbChild = (ReportContentRenderer) wo;
                } else {
                    throw new IllegalStateException("Cannot render this object.");
                }
                
                if (cbChild == null) {
                    r.setIcon(BOX_ICON);
                    r.setText("Empty content box");
                } else if (cbChild instanceof ResultSetRenderer) {
                	setupForQueryCache((WorkspaceTreeCellRenderer) r, ((ResultSetRenderer) cbChild).getContent());
                } else if (cbChild instanceof CellSetRenderer) {
                	r.setIcon(OLAP_QUERY_ICON);
                	r.setText(((CellSetRenderer) cbChild).getContent().getName());
                } else if (cbChild instanceof ImageRenderer) {
                    setupForWabitImage((WorkspaceTreeCellRenderer) r, ((ImageRenderer) cbChild).getImage());
                } else if (cbChild instanceof ChartRenderer) {
                	ChartRenderer chartRenderer = (ChartRenderer) cbChild;
                	setupForChart(r, chartRenderer.getContent());
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
            	setupForQueryCache((WorkspaceTreeCellRenderer) r, wo);
            } else if (wo instanceof OlapQuery) {
                r.setIcon(OLAP_QUERY_ICON);
            } else if (wo instanceof Chart) {
                setupForChart(r, (Chart) wo);
            } else if (wo instanceof ChartColumn) {
                ChartColumn cc = (ChartColumn) wo;
                r.setText(cc.toString());
                r.setIcon(CHART_COL_ROLE_ICONS.get(cc.getRoleInChart()));
            } else if (wo instanceof WabitImage) {
                setupForWabitImage((WorkspaceTreeCellRenderer) r, wo);
            }

            if (wo instanceof WabitBackgroundWorker) {
                if (((WabitBackgroundWorker) wo).isRunning()) {
                    r.setIcon(makeBusy(r.getIcon()));
                }
            }
            
        } else if (value instanceof FolderNode) {
        	FolderNode folder = ((FolderNode) value);
        	r.setText(folder.toString());
        } else if (value instanceof Olap4jTreeObject) {
        	Object olapObject =((Olap4jTreeObject) value).getOlapObject();
        	Component delegateComponent = delegateOlap4jRenderer.getTreeCellRendererComponent(tree, 
        			olapObject, sel, expanded, leaf, row, hasFocus);
			r = (DefaultTreeCellRenderer) delegateComponent;
        } else if (value instanceof SQLObject) {
        	Component delegateComponent = delegateSQLTreeCellRenderer.getTreeCellRendererComponent(tree, value, sel, 
        			expanded, leaf, row, hasFocus);
			r = (DefaultTreeCellRenderer) delegateComponent;
        }
        
        if (originalValue instanceof WorkspaceGraphTreeNodeWrapper) {
            r.setText(((WorkspaceGraphTreeNodeWrapper) originalValue).getName());
        }
        
        return r;
    }

    private void setupForChart(DefaultTreeCellRenderer r, Chart chart) {
        ChartType chartType = chart.getType();
        if (chartType == null) {
            r.setIcon(CHART_ICON);
        } else if (chartType.equals(ChartType.BAR)) {
            r.setIcon(CHART_BAR_ICON);
        } else if (chartType.equals(ChartType.PIE)) {
            r.setIcon(CHART_PIE_ICON);
        } else if (chartType.equals(ChartType.CATEGORY_LINE)) {
            r.setIcon(CHART_LINE_ICON);
        } else if (chartType.equals(ChartType.SCATTER)) {
            r.setIcon(CHART_SCATTER_ICON);
        } else if (chartType.equals(ChartType.LINE)) {
            r.setIcon(CHART_LINE_ICON);
        } else {
            logger.warn("Using generic icon for unknown chart type " + chartType);
            r.setIcon(CHART_ICON); 
        }
        r.setText(chart.getName());
    }

	private void setupForWabitImage(WorkspaceTreeCellRenderer r, WabitObject wo) {
		final Icon wabitIcon = ((WabitImage) wo).getImageAsIcon();
		if (wabitIcon != null) {
		    r.setIcon(wabitIcon);
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
