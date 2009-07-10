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

package ca.sqlpower.wabit.report.resultset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A section of a result set is all of the values under a unique break
 * in the result set. If no breaks are defined there should be one section
 * that defines the entire document.
 */
public class Section {

    /**
     * This describes the way a total can be rendered for a section.
     */
    public enum TotalRenderStyle {
        SUBTOTAL,
        GRAND_TOTAL
    }
    
    /**
     * This is the starting row of this section. This value should be greater or equal to 1
     * to allow it to be used with result sets and to set the absolute row position of a result set.
     */
    private final int startRow;
    
    /**
     * This is the ending row of this section. This value should be greater or equal to 1
     * to allow it to be used with result sets and to set the absolute row position of a result set.
     */
    private final int endRow;
    
    /**
     * This is the list of totals in the result set. Each entry in this list is either null
     * if the column is not being totalled or a value that represents the total of the
     * current section.
     */
    private final List<BigDecimal> totals;
    
    /**
     * This is the list of unique objects that defines a section. The order of the objects
     * is the same order that they come in the table. There are the same number of entries
     * in this list as there are columns where each column that is not used to define the
     * section has a null value in its position.
     */
    private final List<Object> sectionKey;
    
    /**
     * If this is true the section header should be displayed at the start of each section.
     */
    private final boolean showingSectionHeader;
    
    /**
     * If true the rows in this section will be displayed.
     */
    private final boolean showingRows;
    
    /**
     * If true the column headers will be displayed for this section.
     */
    private final boolean showingColumnHeader;

    /**
     * Describes the way a total of a section will be rendered.
     */
    private final TotalRenderStyle totalRenderStyle;
    
    public Section(int startRow, int endRow, List<BigDecimal> totals, 
            List<Object> sectionKey) {
        this.startRow = startRow;
        this.endRow = endRow;
        this.totals = totals;
        this.sectionKey = sectionKey;
        showingSectionHeader = true;
        showingRows = true;
        showingColumnHeader = true;
        totalRenderStyle = TotalRenderStyle.SUBTOTAL;
    }
    
    /**
     * This constructor creates a section that is used to display the grand total
     * of the result set.
     */
    public Section(List<BigDecimal> grandTotals) {
        startRow = -1;
        endRow = -2;
        totals = grandTotals;
        sectionKey = new ArrayList<Object>();
        showingSectionHeader = false;
        showingRows = false;
        showingColumnHeader = false;
        totalRenderStyle = TotalRenderStyle.GRAND_TOTAL;
    }
    
    public int getStartRow() {
        return startRow;
    }
    
    public int getEndRow() {
        return endRow;
    }
    
    public List<Object> getSectionHeader() {
        return Collections.unmodifiableList(sectionKey);
    }
    
    public List<BigDecimal> getTotals() {
        return Collections.unmodifiableList(totals);
    }
    
    public boolean isShowingSectionHeader() {
        return showingSectionHeader;
    }

    public boolean isShowingRows() {
        return showingRows;
    }

    public boolean isShowingColumnHeader() {
        return showingColumnHeader;
    }

    public TotalRenderStyle getTotalRenderStyle() {
        return totalRenderStyle;
    }
}
