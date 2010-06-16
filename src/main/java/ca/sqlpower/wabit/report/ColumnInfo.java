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

import java.text.Format;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.query.Item;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class ColumnInfo extends AbstractWabitObject{
    
    /**
     * Defines if the column is a grouping, break
     * or neither.
     */
    public enum GroupAndBreak {
        GROUP,
        BREAK,
        PAGEBREAK,
        NONE
    }

	/**
	 * The item this column information is describing.
	 */
    private final Item columnInfoItem;

	/**
	 * Column width in Graphics2D units (screen pixels or 1/72 of an inch when printed).
	 */
	private static final int DEFAULT_COL_WIDTH = 72;
	public static final String FORMAT_CHANGED = "format";
	public static final String DATATYPE_CHANGED = "dataType";
	public static final String HORIZONAL_ALIGNMENT_CHANGED = "horizontalAlignment";
	public static final String COLUMN_INFO_ITEM_CHANGED = "columnInfoItem";
	public static final String WIDTH_CHANGED = "width";
	public static final String WILL_GROUP_OR_BREAK_CHANGED = "willGroupOrBreak";
	public static final String WILL_SUBTOTAL_CHANGED = "willSubtotal";
	public static final String COLUMN_ALIAS = "columnAlias";
	
	private int width = DEFAULT_COL_WIDTH;

	private HorizontalAlignment hAlign = HorizontalAlignment.LEFT;

	private DataType dataType = null;

	private Format format = null;
	
	/**
	 * Defines if the column described by this information
	 * should have a break or group after every value change.
	 */
	private GroupAndBreak willGroupOrBreak = GroupAndBreak.NONE;
	
	/**
	 * defines if this column should be totaled before each new break. Only
	 * numeric columns should allow subtotals.
	 */
	private boolean willSubtotal = false;

    /**
     * This is the column name as it is in the result set. If you want the
     * pretty user name of the column get the name of this object. If you want
     * to access the column in a result set by its column name use this value.
     */
	private String columnAlias;
	
	public ColumnInfo(Item item, String label) {
		columnInfoItem = item;
		setName(label);
		
	}
	
	public ColumnInfo(String label) {
		this(label, label);
	}
	
	public ColumnInfo(String alias, String label) {
		setColumnAlias(alias);
		setName(label);
		columnInfoItem = null;
	}
	
	public ColumnInfo(ColumnInfo columnInfo) {
		this.columnInfoItem = columnInfo.columnInfoItem;
		this.columnAlias = columnInfo.columnAlias;
		this.dataType = columnInfo.dataType;
		this.format = columnInfo.format;
		this.hAlign = columnInfo.hAlign;
		this.width = columnInfo.width;
		setName(columnInfo.getName());
		this.willGroupOrBreak = columnInfo.willGroupOrBreak;
		this.willSubtotal = columnInfo.willSubtotal;
	}

	/**
	 * This value can be null. There is no Item defined for columns that 
	 * are generated from users modifying the SQL script manually.
	 */
	public Item getColumnInfoItem() {
		return columnInfoItem;
	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		firePropertyChange(WIDTH_CHANGED, this.width, width);
		this.width = width;
	}
	public HorizontalAlignment getHorizontalAlignment() {
		return hAlign;
	}
	public void setHorizontalAlignment(HorizontalAlignment align) {
		firePropertyChange(HORIZONAL_ALIGNMENT_CHANGED, hAlign, align);
		hAlign = align;
	}
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType type) {
		firePropertyChange(DATATYPE_CHANGED, this.dataType, type);
		dataType = type;
	}
	public Format getFormat() {
		return format;
	}
	public void setFormat(Format format) {
		firePropertyChange(FORMAT_CHANGED, this.format, format);
		this.format = format;
	}

	public boolean allowsChildren() {
		return false;
	}

	public int childPositionOffset(Class<? extends SPObject> childType) {
		throw new UnsupportedOperationException("should not have Children");
	}

	public List<? extends WabitObject> getChildren() {
		return Collections.emptyList();
	}

	public GroupAndBreak getWillGroupOrBreak() {
		return willGroupOrBreak;
	}

	public void setWillGroupOrBreak(GroupAndBreak willGroupOrBreak) {
		firePropertyChange(WILL_GROUP_OR_BREAK_CHANGED, this.willGroupOrBreak, willGroupOrBreak);
		this.willGroupOrBreak = willGroupOrBreak;
	}

	public boolean getWillSubtotal() {
		return willSubtotal;
	}

	public void setWillSubtotal(boolean subtotal) {
		firePropertyChange(WILL_SUBTOTAL_CHANGED, this.willSubtotal, subtotal);
		this.willSubtotal = subtotal;
	}

	public void setColumnAlias(String columnAlias) {
		firePropertyChange(COLUMN_ALIAS, this.columnAlias, columnAlias);
		this.columnAlias = columnAlias;
	}

	public String getColumnAlias() {
		return columnAlias;
	}

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        //do nothing
    }

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }

}

