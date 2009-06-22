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

import org.olap4j.OlapException;

import ca.sqlpower.wabit.WabitProject;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.VerticalAlignment;

public class CreateLayoutFromQueryAction extends AbstractAction {
    
    private static final Icon ADD_LAYOUT_ICON = new ImageIcon(CreateLayoutFromQueryAction.class.getResource("/icons/layout_add.png"));

    /**
     * The project we will add the new layout to when this action is invoked.
     */
    private final WabitProject project;

    /**
     * The {@link ReportContentRenderer} associated with the new default layout being used.
     */
    private ReportContentRenderer contentRenderer;
    
    /**
     * The name of the new Layout, + " Layout" will be appended to the name.
     */
    private String layoutName;
    
    public CreateLayoutFromQueryAction(WabitProject wabitProject, ReportContentRenderer contentRenderer, String layoutName) {
        super("Create Layout...", ADD_LAYOUT_ICON);
        putValue(SHORT_DESCRIPTION, "Create a page layout for this report (use this when you want to print)");
        this.project = wabitProject;
        this.contentRenderer = contentRenderer;
        this.layoutName = layoutName;
    }
    
    public void actionPerformed(ActionEvent e) {
        createDefaultLayout(project, contentRenderer, layoutName);
    }

	/**
	 * Creates a new layout with standard margins, headers, and footers. The
	 * body of the content is provided by the given
	 * {@link ReportContentRenderer}
	 * 
	 * @param project
	 *            The project that the new layout will be added to
	 * @param contentRenderer
	 *            This is the renderer which the new layout will use
	 * @param layoutName
	 *            This is the name of the new Layout, in the tree " Layout" will
	 *            be appended to this name.
	 * @return The new layout that was added to the project.
	 */
    public static Layout createDefaultLayout(WabitProject project, ReportContentRenderer contentRenderer, String layoutName) {
        Layout l = new Layout(layoutName + " Layout");
        Page p = l.getPage();
        final int pageBodyWidth = p.getRightMarginOffset() - p.getLeftMarginOffset();
        final int pageBodyHeight = p.getLowerMarginOffset() - p.getUpperMarginOffset();
        
        ContentBox body = new ContentBox();
        if (contentRenderer instanceof CellSetRenderer) {
        	((CellSetRenderer) contentRenderer).updateMDXQuery();
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
        header.setContentRenderer(new Label(l, layoutName));
        p.addContentBox(header);
        header.setWidth(pageBodyWidth / 2);
        header.setHeight(Page.DPI / 2); // TODO base this on the actual font metrics or something
        header.setX(p.getLeftMarginOffset());
        header.setY(p.getUpperMarginOffset() - header.getHeight());
        
        // shameless self promotion
        ContentBox dateHeader = new ContentBox();
        Label dateLabel = new Label(l, "Generated on ${now}");
        dateLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        dateHeader.setContentRenderer(dateLabel);
        p.addContentBox(dateHeader);
        dateHeader.setWidth(pageBodyWidth - header.getWidth());
        dateHeader.setHeight(Page.DPI / 2); // TODO base this on the actual font metrics or something
        dateHeader.setX(header.getX() + header.getWidth());
        dateHeader.setY(p.getUpperMarginOffset() - dateHeader.getHeight());
        
        ContentBox footer = new ContentBox();
        Label footerLabel = new Label(l, "Page ${page_number} of ${page_count}");
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
        Label selfPromotionLabel = new Label(l,
                "Made with Wabit ${wabit_version} - Free Reporting That Just Works.  http://www.sqlpower.ca/wabit");
        selfPromotionLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        selfPromotionLabel.setVerticalAlignment(VerticalAlignment.TOP);
        shameless.setContentRenderer(selfPromotionLabel);
        p.addContentBox(shameless);
        shameless.setWidth(pageBodyWidth);
        shameless.setHeight(Page.DPI / 3); // TODO base this on the actual font metrics or something
        shameless.setX(p.getLeftMarginOffset());
        shameless.setY(footer.getY() + footer.getHeight());
        

        project.addLayout(l);
        
        return l;
    }
}
