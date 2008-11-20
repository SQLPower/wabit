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

import java.awt.Graphics2D;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.WabitObject;

/**
 * Interface for providers of rendered (absolute layout) content.
 */
public interface ReportContentRenderer extends WabitObject {

    /**
     * Renders as much report content as will fit within the bounds of the given
     * content box.
     * <p>
     * Report content renderers can be implemented in two different ways:
     * <ul>
     *  <li>Label-like renderers attempt to show all their content every time
     *      they are called, and never ask for another page. These renderers
     *      don't necessarily render exactly the same content on every page--a
     *      footer label with a page number variable is a good example.
     *  <li>Resultset-like renderers that show as much content as possible each
     *      time they are called, and keep track of what to start rendering on the
     *      next call. These types of renderers ask for more pages until they
     *      have nothing left to render. If called again, they simply do not draw
     *      anything.
     * 
     * @param g
     *            The graphics to render into. The origin (top left corner or
     *            (0,0)) of this graphics is translated to the top-left corner
     *            of the content box.
     * @param contentBox
     *            The box that determines the size and shape that the rendered
     *            data must fit within. You can ignore the X and Y coordinates
     *            of the box because the given graphics object's origin is
     *            already set to this box's origin.
     * @param scaleFactor
     *            The amount of scaling currently in effect. The nominal size of
     *            a unit when displayed via the given graphics is scaleFactor/72 inches.
     * @return True if this renderer has more data to render, and would like to
     *         be called upon again. Returning true will typically cause the report to
     *         grow by another page. The final page of the report is the first one where
     *         all content renderers involved return false.
     */
    boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor);

    /**
     * Tells this content renderer that the next call to
     * {@link #renderReportContent(Graphics2D, ContentBox, double)} should
     * produce the first page of output again.
     */
    void resetToFirstPage();
    
    /**
     * Returns the data entry panel that the user can use to manipulate all
     * the properties of this content renderer. Everything that can be maniuplated
     * in the panel can also be done programatically (via the API) through the
     * JavaBeans properties of this report content renderer.
     * <p>
     * NOTE: We thought we could get away with defining this here, because it's convenient
     * to have the properties panel provided by the content renderer itself. However,
     * it has proven to be a bit too much of a violation of the system design to put this
     * Swing stuff into the core classes that implement this interface. So this method
     * will be removed soon.
     */
    DataEntryPanel getPropertiesPanel();
}
