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

import java.sql.Types;
import java.util.List;

import javax.annotation.Nonnull;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

import com.rc.retroweaver.runtime.Collections;

/**
 * This class handles some of the generic methods to the ColumnIdentifier.
 * This will not store the specific object that makes the column be uniquely
 * identified.
 */
public class ChartColumn extends AbstractWabitObject {
    
    /**
     * Enumeration of the data types a chart column can have.
     * <p>
     * Names are chosen for compatibility with {@link ca.sqlpower.wabit.report.DataType},
     * with which this enum will eventually be merged.
     */
    public static enum DataType {
        TEXT, NUMERIC, DATE;

        /**
         * Returns the appropriate data type based on the given JDBC type. If
         * the type is non-standard or not recognized, it will be assumed to be
         * a string.
         * <p>
         * FIXME because of variability of database platforms, it would be far
         * better if we had the help of the DataSourceType object for the
         * platform the type code came from. It could be passed in as a
         * parameter to this method.
         * <p>
         * XXX we do this in enough places in all our products (and in fact
         * {@link ca.sqlpower.wabit.report.DataType elsewhere in Wabit} that it
         * would make sense to create a common SimplifiedSQLType enum in
         * ca.sqlpower.sql and use it everywhere. Such a type would be written
         * to get the help of the DataSourceType in question.
         * 
         * @param jdbcType
         *            the type code from {@link Types java.sql.Types}.
         * @return the data type that most closely represents the given type
         *         code.
         */
        public static DataType forJDBCType(int jdbcType) {
            if (SQL.isNumeric(jdbcType)) {
                return NUMERIC;
            } else if (SQL.isDate(jdbcType)) {
                return DATE;
            } else {
                return TEXT;
            }
        }
    };
    
    private ColumnRole roleInChart;
    
    private ChartColumn xAxisIdentifier;
    
    private final String columnName;

    private final DataType dataType;

    /**
     * Creates a new chart column descriptor for the given name and JDBC data
     * type.
     * 
     * @param columnName
     *            The column's name (generally case sensitive)
     * @param jdbcType
     *            The {@link Types java.sql.Types} type code.
     */
    public ChartColumn(@Nonnull String columnName, int jdbcType) {
        this(columnName, DataType.forJDBCType(jdbcType));
    }

    /**
     * Creates a new chart column descriptor for the given name simplified SQL
     * data type.
     * 
     * @param columnName
     *            The column's name (generally case sensitive)
     * @param jdbcType
     *            The {@link Types java.sql.Types} type code.
     */
    public ChartColumn(@Nonnull String columnName, @Nonnull DataType dataType) {
        if (columnName == null) {
            throw new NullPointerException("null column name not allowed");
        }
        if (dataType == null) {
            throw new NullPointerException("null data type not allowed");
        }
        this.columnName = columnName;
        setName(columnName);
        this.dataType = dataType;
        setRoleInChart(ColumnRole.NONE);
    }

    public ColumnRole getRoleInChart() {
        return roleInChart;
    }

    public void setRoleInChart(ColumnRole dataType) {
        ColumnRole oldType = this.roleInChart;
        this.roleInChart = dataType;
        firePropertyChange("roleInChart", oldType, dataType);
    }

    public ChartColumn getXAxisIdentifier() {
        return xAxisIdentifier;
    }

    public void setXAxisIdentifier(ChartColumn xAxisIdentifier) {
        ChartColumn oldIdentifier = this.xAxisIdentifier;
        this.xAxisIdentifier = xAxisIdentifier;
        firePropertyChange("XAxisIdentifier", oldIdentifier, xAxisIdentifier);
    }
    
    public DataType getDataType() {
        return dataType;
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
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
    
    public void removeDependency(SPObject dependency) {
        //do nothing.
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    /**
     * Two identifiers for the same column name are considered equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChartColumn) {
            ChartColumn ci = (ChartColumn) obj;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getColumnName());
        if (getRoleInChart() != ColumnRole.NONE) {
            sb.append(" (").append(getRoleInChart());
            if (getXAxisIdentifier() != null) {
                sb.append(" vs. ").append(getXAxisIdentifier().getName());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

}
