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

/**
 * This is the position of a {@link Section} that is printed on a page. A section
 * may not start from the header of the section but my start part way through in
 * cases where the section is printing across multiple pages.
 */
public class Position {

    /**
     * These positions are used to define if a header or totals is supposed to be
     * displayed from a position first. Depending on where a page break happens
     * a position may only print some rows and the totals of a section without
     * printing headers.
     */
    public enum PositionType {
        SECTION_HEADER,
        COLUMN_HEADER,
        ROW,
        TOTALS
    }
    
    /**
     * This is the starting row to print from if the {@link PositionType} is
     * ROW. If a header or total is printed instead this value will be null.
     */
    private final Integer startingRow;

    /**
     * Defines if a header, total, or row is to be printed first.
     */
    private final PositionType firstPositionType;
    
    /**
     * Defines if a header, total, or row is to be printed last.
     */
    private final PositionType lastPositionType;
    
    /**
     * This is the ending row to print if the {@link PositionType} is
     * ROW. If a header or total is printed instead this value will be null.
     */
    private final Integer endingRow;
    
    private final Section section;

    /**
     * This is the one constructor that builds all of the positions.
     * If the position type is ROW then the corresponding row number should
     * not be null. If the position type is not row then the corresponding row
     * number will be null.
     */
    public Position(Section section, PositionType firstPositionType, 
            Integer startingRow, PositionType lastPositionType, Integer endingRow) {
        this.section = section;
        this.firstPositionType = firstPositionType;
        this.lastPositionType = lastPositionType;
        if (firstPositionType == PositionType.ROW) {
            this.startingRow = startingRow;
        } else {
            this.startingRow = null;
        }
        if (lastPositionType == PositionType.ROW) {
            this.endingRow = endingRow;
        } else {
            this.endingRow = null;
        }
    }
    
    public Section getSection() {
        return section;
    }
    
    public Integer getStartingRow() {
        return startingRow;
    }
    
    public PositionType getFirstPositionType() {
        return firstPositionType;
    }
    
    public PositionType getLastPositionType() {
        return lastPositionType;
    }
    
    public Integer getEndingRow() {
        return endingRow;
    }
    
}