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

import javax.swing.JComponent;
import javax.swing.JLabel;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.ChartRenderer;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Charts are modified entirely by their main editor panel. This class is a
 * placeholder that exists only to provide the required API.
 */
public class ChartSwingRenderer implements SwingContentRenderer {
    
    /**
     * Creates a new renderer for charts in the Swing client environment.
     * 
     * @param session The session this renderer belongs to. Must not be null.
     * @param renderer The underlying layout preferences for this chart.
     */
    public ChartSwingRenderer(WabitWorkspace workspace, ChartRenderer renderer) {
        // no op
    }

    /**
     * Returns a data entry panel that just says the chart properties are all
     * controlled by the chart editor.
     */
    public DataEntryPanel getPropertiesPanel() {
        return new DataEntryPanel() {
            
            public boolean hasUnsavedChanges() { return false; }
            public boolean applyChanges()      { return true; }
            public void    discardChanges()    { /* no op */ }
            
            public JComponent getPanel() {
                return new JLabel("To modify the chart's properties, use its editor.");
            }
        };
    }

    public void processEvent(PInputEvent event, int type) {
        //do something cool here later.
    }

}
