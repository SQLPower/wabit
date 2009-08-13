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

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.ReportContentRenderer;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;

/**
 * Renderers of this type are the swing compliment the {@link ReportContentRenderer}.
 * The methods defined here are swing specific but would otherwise be included
 * in the renderer this class wraps.
 */
public interface SwingContentRenderer extends PInputEventListener {

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
     * will be removed soon. Also pull out processEvent and the {@link PInputEventListener}
     * interface with this method.
     */
    DataEntryPanel getPropertiesPanel();
    
    /**
     * This method comes from {@link PInputEventListener} but is documented here
     * because it is not documented in {@link PInputEventListener}. This method
     * is used to process key, mouse, mouse wheel, and focus events that occur
     * in the view. This allows each renderer to change based on user input.
     * 
     * @param event
     *            This is the input event given from some input by the user.
     * @param type
     *            This is the type of input event. Valid values come from
     *            {@link KeyEvent}, {@link MouseEvent}, {@link MouseWheelEvent},
     *            and {@link FocusEvent}
     */
    public void processEvent(PInputEvent event, int type);
}
