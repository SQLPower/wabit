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

import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ChartRenderer.DataTypeSeries;

import com.rc.retroweaver.runtime.Collections;

/**
 * This class handles some of the generic methods to the ColumnIdentifier.
 * This will not store the specific object that makes the column be uniquely
 * identified.
 */
public abstract class AbstractColumnIdentifier extends AbstractWabitObject implements ColumnIdentifier {
    
    private DataTypeSeries dataType;
    
    private ColumnIdentifier xAxisIdentifier;
    
    public AbstractColumnIdentifier() {
        setDataType(DataTypeSeries.NONE);
    }

    public DataTypeSeries getDataType() {
        return dataType;
    }

    public ColumnIdentifier getXAxisIdentifier() {
        return xAxisIdentifier;
    }

    public void setDataType(DataTypeSeries dataType) {
        DataTypeSeries oldType = this.dataType;
        this.dataType = dataType;
        firePropertyChange("dataType", oldType, dataType);
    }

    public void setXAxisIdentifier(ColumnIdentifier xAxisIdentifier) {
        ColumnIdentifier oldIdentifier = this.xAxisIdentifier;
        this.xAxisIdentifier = xAxisIdentifier;
        firePropertyChange("xAxisIdentifier", oldIdentifier, xAxisIdentifier);
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

}
