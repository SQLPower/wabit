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

package ca.sqlpower.wabit.swingui.olap;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;

import ca.sqlpower.swingui.ColourScheme;

public class CellSetTableRowHeaderComponent extends JComponent {

    public CellSetTableRowHeaderComponent(CellSet cellSet) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        CellSetAxis rowAxis = cellSet.getAxes().get(1);
        CellSetAxisMetaData axisMetaData = rowAxis.getAxisMetaData();
        int hierarchyCount = axisMetaData.getHierarchies().size();
        for (int i = 0; i < hierarchyCount; i++) {
            CellSetHierarchyComponent hierarchyComponent =
                new CellSetHierarchyComponent(rowAxis, i);
            hierarchyComponent.setBackground(
                    ColourScheme.BACKGROUND_COLOURS[i % ColourScheme.BACKGROUND_COLOURS.length]);
            add(hierarchyComponent);
        }
        
    }
}
