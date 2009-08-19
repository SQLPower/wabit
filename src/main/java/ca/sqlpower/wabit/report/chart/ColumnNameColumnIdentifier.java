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



/**
 * This class uses the unique name of the column to identify each column. This
 * is used in relational queries.
 */
public class ColumnNameColumnIdentifier extends AbstractColumnIdentifier {

    private final String columnName;

    public ColumnNameColumnIdentifier(String columnName) {
        this.columnName = columnName;
    }
    
    public String getName() {
        return getColumnName();
    }

    public Object getUniqueIdentifier() {
        return getColumnName();
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).equals(getColumnName());
        } else if (obj instanceof ColumnNameColumnIdentifier) {
            ColumnNameColumnIdentifier ci = (ColumnNameColumnIdentifier) obj;
            return getColumnName().equals(ci.getColumnName());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + columnName.hashCode();
        return result;
    }

}
