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

package ca.sqlpower.wabit.report.chart;

import org.olap4j.Axis;
import org.olap4j.CellSetAxis;

/**
 * This column identifier specifically represents the rows axis.
 */
public class RowAxisColumnIdentifier implements ColumnIdentifier {

    private final Axis axis = Axis.ROWS;
    
    public Object getUniqueIdentifier() {
        return getAxis();
    }
    
    public Axis getAxis() {
        return axis;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        
        if (obj instanceof Axis) {
            return ((CellSetAxis) obj).equals(getAxis());
        } else if (obj instanceof RowAxisColumnIdentifier) {
            RowAxisColumnIdentifier ci = (RowAxisColumnIdentifier) obj;
            return getAxis() == ci.getAxis();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + axis.hashCode();
        return result;
    }

    public String getName() {
        return axis.name();
    }

}
