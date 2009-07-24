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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Member;

public class CellSetTableModel implements TableModel {

    private final CellSet cellSet;
    private CellSetAxis columnsAxis;
    private CellSetAxis rowsAxis;

    public CellSetTableModel(CellSet cellSet) {
        this.cellSet = cellSet;
        columnsAxis = cellSet.getAxes().get(0);
        rowsAxis = cellSet.getAxes().get(1);
    }

    public void addTableModelListener(TableModelListener l) {
        // data doesn't change
    }


    public void removeTableModelListener(TableModelListener l) {
        // TODO Auto-generated method stub
        
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class; // TODO
    }

    public String getColumnName(int columnIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        for (Member member : columnsAxis.getPositions().get(columnIndex).getMembers()) {
            sb.append(member.getName()).append("<br>");
        }
        return sb.toString();
    }
    
    public int getColumnCount() {
        return columnsAxis.getPositionCount();
    }

    public int getRowCount() {
        return rowsAxis.getPositionCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return cellSet.getCell(
                columnsAxis.getPositions().get(columnIndex),
                rowsAxis.getPositions().get(rowIndex)).getFormattedValue();
    }
    
    public double getDoubleValueAt(int rowIndex, int columnIndex) {
    	try {
			return cellSet.getCell(
			        columnsAxis.getPositions().get(columnIndex),
			        rowsAxis.getPositions().get(rowIndex)).getDoubleValue();
		} catch (OlapException e) {
			throw new RuntimeException(e);
		}
    }
    
    public Object getObjectValueAt(int rowIndex, int columnIndex) {
    	return cellSet.getCell(
    			columnsAxis.getPositions().get(columnIndex),
    			rowsAxis.getPositions().get(rowIndex)).getValue();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not editable! Not by a long shot!");
    }
}
