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

package ca.sqlpower.wabit.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ca.sqlpower.object.HorizontalAlignment;
import ca.sqlpower.object.VerticalAlignment;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.WabitLabel;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.query.QueryCache;
import ca.sqlpower.wabit.swingui.WabitIcons;

public class CreateLayoutFromQueryAction extends AbstractAction {
    
    public static final Icon ADD_LAYOUT_ICON = new ImageIcon(CreateLayoutFromQueryAction.class.getResource("/icons/32x32/dashboard.png"));

    /**
     * The workspace we will add the new layout to when this action is invoked.
     */
    private final WabitWorkspace workspace;

    /**
     * The name of the new Layout, + " Layout" will be appended to the name.
     */
    private String layoutName;

    /**
     * This is the object that this action will create a layout for.
     */
    private final WabitObject objectToLayout;
    
    public CreateLayoutFromQueryAction(WabitWorkspace wabitworkspace, WabitObject objectToLayout, String layoutName) {
        super("Create Layout...", ADD_LAYOUT_ICON);
        this.objectToLayout = objectToLayout;
        putValue(SHORT_DESCRIPTION, "Create a page layout for this report (use this when you want to print)");
        this.workspace = wabitworkspace;
        this.layoutName = layoutName;
    }
    
    public void actionPerformed(ActionEvent e) {
        ReportContentRenderer contentRenderer;
        if (objectToLayout instanceof QueryCache) {
            contentRenderer = new ResultSetRenderer((QueryCache) objectToLayout);
        } else if (objectToLayout instanceof OlapQuery) {
        	
        	//Custom button text
			Object[] options = {"OLAP Viewer", "Relational Viewer"};
			
			final int n = JOptionPane.showOptionDialog(null,
			    "In what type of component would you like to render this query?",
			    "",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE,
			    WabitIcons.QUERY_32,
			    options,
			    options[0]);
			
			if (n == JOptionPane.CLOSED_OPTION) {
				return;
			} else if (n == 0) {
				contentRenderer = new CellSetRenderer((OlapQuery) objectToLayout);
			} else if (n == 1) {
				contentRenderer = new ResultSetRenderer((OlapQuery) objectToLayout);
			} else {
				throw new AssertionError();
			}
			
        } else {
            throw new IllegalStateException("Don't know how to create layouts for components of type " + objectToLayout.getClass() + ", object is " + objectToLayout.getName());
        }
        createDefaultLayout(workspace, contentRenderer, layoutName);
    }

	/**
	 * Creates a new layout with standard margins, headers, and footers. The
	 * body of the content is provided by the given
	 * {@link ReportContentRenderer}
	 * 
	 * @param workspace
	 *            The workspace that the new layout will be added to
	 * @param contentRenderer
	 *            This is the renderer which the new layout will use
	 * @param layoutName
	 *            This is the name of the new Layout, in the tree " Layout" will
	 *            be appended to this name.
	 * @return The new layout that was added to the workspace.
	 */
    public static Report createDefaultLayout(WabitWorkspace workspace, ReportContentRenderer contentRenderer, String layoutName) {
        Report l = new Report(layoutName + " Layout");
        Page p = l.getPage();
        final int pageBodyWidth = (int) (p.getRightMarginOffset() - p.getLeftMarginOffset());
        final int pageBodyHeight = (int) (p.getLowerMarginOffset() - p.getUpperMarginOffset());
        
        ContentBox body = new ContentBox();
        if (contentRenderer instanceof CellSetRenderer) {
	        body.setContentRenderer(contentRenderer);
        } else if (contentRenderer instanceof ResultSetRenderer) {
        	body.setContentRenderer(contentRenderer);
        } else {
        	throw new UnsupportedOperationException("Creating the default layout is unsupported for the current ReportContentRenderer being passed. It's name is: " + contentRenderer.getName());
        }
        p.addContentBox(body);
        body.setWidth(pageBodyWidth);
        body.setHeight(pageBodyHeight);
        body.setX(p.getLeftMarginOffset());
        body.setY(p.getUpperMarginOffset());
        
        ContentBox header = new ContentBox();
        header.setContentRenderer(new WabitLabel(layoutName));
        p.addContentBox(header);
        header.setWidth(pageBodyWidth / 2);
        header.setHeight(Page.DPI / 2); // TODO base this on the actual font metrics or something
        header.setX(p.getLeftMarginOffset());
        header.setY(p.getUpperMarginOffset() - header.getHeight());
        
        // shameless self promotion
        ContentBox dateHeader = new ContentBox();
        WabitLabel dateLabel = new WabitLabel("Generated on ${now}");
        dateHeader.setContentRenderer(dateLabel);
        p.addContentBox(dateHeader);
        dateLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        dateHeader.setWidth(pageBodyWidth - header.getWidth());
        dateHeader.setHeight(Page.DPI / 2); // TODO base this on the actual font metrics or something
        dateHeader.setX(header.getX() + header.getWidth());
        dateHeader.setY(p.getUpperMarginOffset() - dateHeader.getHeight());
        
        ContentBox footer = new ContentBox();
        WabitLabel footerLabel = new WabitLabel("Page ${page_number} of ${page_count}");
        footerLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
        footer.setContentRenderer(footerLabel);
        // TODO add option for horizontal and vertical alignment (left, center, right, top, middle, bottom) in label
        p.addContentBox(footer);
        footer.setWidth(pageBodyWidth);
        footer.setHeight(Page.DPI / 3); // TODO base this on the actual font metrics or something
        footer.setX(p.getLeftMarginOffset());
        footer.setY(p.getLowerMarginOffset());
        
        // shameless self promotion
        ContentBox shameless = new ContentBox();
        WabitLabel selfPromotionLabel = new WabitLabel(
                "Made with Wabit ${wabit_version} - Free Reporting That Just Works.  http://www.sqlpower.ca/wabit");
        selfPromotionLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        selfPromotionLabel.setVerticalAlignment(VerticalAlignment.TOP);
        shameless.setContentRenderer(selfPromotionLabel);
        p.addContentBox(shameless);
        shameless.setWidth(pageBodyWidth);
        shameless.setHeight(Page.DPI / 3); // TODO base this on the actual font metrics or something
        shameless.setX(p.getLeftMarginOffset());
        shameless.setY(footer.getY() + footer.getHeight());
        

        workspace.addReport(l);
        
        return l;
    }
}
