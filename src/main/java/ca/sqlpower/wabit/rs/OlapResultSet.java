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

package ca.sqlpower.wabit.rs;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.CachedResultSetMetaData;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.wabit.rs.olap.RepeatedMember;


/**
 * Creates a new result set based on the given cell set.
 * <p>
 * The result set will be laid out like this:
 * <table border>
 *  <tr>
 *   <th>Row&nbsp;Dim&nbsp;0 Level 0
 *   <th>Row&nbsp;Dim&nbsp;0 Level 1
 *   <th>...
 *   <th>Row&nbsp;Dim&nbsp;0 Level N
 *   <th>Row&nbsp;Dim&nbsp;1 Level 0
 *   <th>Row&nbsp;Dim&nbsp;1 Level 1
 *   <th>...
 *   <th>Row&nbsp;Dim&nbsp;1 Level N
 *   <th>Col&nbsp;Dim&nbsp;0 Level&nbsp;0&nbsp;Member<br>
 *       Col&nbsp;Dim&nbsp;0 Level 1&nbsp;Member<br>
 *       ...<br>
 *       Col&nbsp;Dim&nbsp;1 Level 0&nbsp;Member
 *   <th>Col&nbsp;Dim&nbsp;0 Level&nbsp;0&nbsp;Member<br>
 *       Col&nbsp;Dim&nbsp;0 Level 1&nbsp;Member<br>
 *       ...
 *       <br>Col&nbsp;Dim&nbsp;1 Level 0&nbsp;Member
 *   <th>...
 *  </tr>
 *  <tr>
 *   <td>Member Name
 *   <td>Member Name
 *   <td>...
 *   <td>Member Name
 *   <td>Member Name
 *   <td>Member Name
 *   <td>...
 *   <td>Member Name
 *   <td>Cell Value
 *   <td>Cell Value
 *   <td>...
 *  </tr>
 *  <tr>
 *   <td>Member Name
 *   <td>Member Name
 *   <td>...
 *   <td>Member Name
 *   <td>Member Name
 *   <td>Member Name
 *   <td>...
 *   <td>Member Name
 *   <td>Cell Value
 *   <td>Cell Value
 *   <td>...
 *  </tr>
 * </table>
 * <p>
 * There will be a row in the result set for every Columns axis position in
 * the given Cell set.
 */
public class OlapResultSet extends CachedRowSet {

    private static final Logger logger = Logger.getLogger(OlapResultSet.class);

    /**
     * List of all Levels represented in the original cell set. The order of the
     * items in this list corresponds with the column positions each level of
     * the rows axis is mapped to.
     * <p>
     * For example, if rowAxisColumns[0] contains the Country level of the
     * Geography dimension, then Country members appear in the first column of
     * this result set. If rowAxisColumns[3] contains the Education Level member
     * of the Education Level dimension, then the fourth column of this result
     * set contains the Education Level members.
     * <p>
     * Gets created and initialized in {@link #populate(CellSet)}.
     */
    private List<Level> rowAxisColumns;
    
    public OlapResultSet() {
        super();
    }

    /**
     * Populates this result set from the given cell set according to the rules
     * outlined in the class-level comment.
     * 
     * @param cellSet
     *            The cell set to convert. Must not be null.
     * @return The result set that represents the data in the cell set.
     * @throws SQLException
     */
    public void populate(CellSet cellSet) throws SQLException {
    	
    	rsmd = new CachedResultSetMetaData();
        data = new ArrayList<Object[]>();
        
    	if (cellSet == null) {
    		// Nothing to populate.
    		return;
    	}
    	
        List<CellSetAxis> axes = cellSet.getAxes();
        if (axes.size() != 2) {
            throw new IllegalArgumentException(
                    "Only 2-axis Cell Sets are convertible to Result Sets. " +
                    "The given Cell Set has " + axes.size() + " axes.");
        }
        
        // Rows axis: each represented level of each dimension is a column
        CellSetAxis rowsAxis = axes.get(Axis.ROWS.axisOrdinal());
        rowAxisColumns = determineRowAxisColumns(rowsAxis);
        for (Level l : rowAxisColumns) {
            Dimension d = l.getDimension();
            String colName = d.getName() + " " + l.getName();

            rsmd.addColumn(false, false, false, false, DatabaseMetaData.columnNullable,
                    true, 10, colName, colName, null, 1000, 0, null, null, Types.VARCHAR,
                    "Dimension Member", true, false, false, "java.lang.String");
        }
        
        // columns axis: the member names (across all levels of all dimensions)
        // at each position are a column
        CellSetAxis colsAxis = axes.get(Axis.COLUMNS.axisOrdinal());
        for (Position p : colsAxis.getPositions()) {
            StringBuilder colNameBuilder = new StringBuilder();
            boolean first = true;
            for (Member m : p.getMembers()) {
                if (!first) {
                    colNameBuilder.append(" ");
                }
                colNameBuilder.append(m.getName());
                first = false;
            }
            String colName = colNameBuilder.toString();
            rsmd.addColumn(false, false, false, false, DatabaseMetaData.columnNullable,
                    true, 10, colName, colName, null, 10, 2, null, null, Types.DOUBLE,
                    "Dimension Member", true, false, false, "java.lang.Double");
        }

        // the data: each position along the rows axis is a row in the result set!
        final Map<Level, RepeatedMember> currentMembers = new HashMap<Level, RepeatedMember>();
        for (Position p : rowsAxis.getPositions()) {
            moveToInsertRow();
            int col;
            for (Member m : p.getMembers()) {
                col = findColumnForLevel(m.getLevel());
                if (col == -1) {
                    throw new IllegalStateException(
                            "Found a member in the rows axis whose level doesn't" +
                            " have a column in the result set!");
                }
                updateString(col, m.getName());
                currentMembers.put(m.getLevel(), new RepeatedMember(m));
                
                // backfill higher levels of this dimension
                col--;
                while (col > 0 && getLevelForColumn(col) != null) {
                    Level l = getLevelForColumn(col);
                    if ( ! l.getDimension().equals(m.getDimension()) ) break;
                    RepeatedMember ancestor = currentMembers.get(l);
                    if (ancestor != null) {
                        updateObject(col, ancestor);
                    }
                    col--;
                }
            }
            col = rowAxisColumns.size() + 1;
            for (Position colPos : colsAxis.getPositions()) {
                Cell cell = cellSet.getCell(colPos, p);
                Object value = cell.getValue();
                if (value instanceof Number) {
                    updateObject(col, ((Number) value).doubleValue());
                } else if (value == null) {
                    updateObject(col, null);
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info(
                                "Value at position " + p + " isn't a number and isn't null: " +
                                value + "(" + value.getClass().getName() + ")");
                    }
                    updateObject(col, null);
                }
                col++;
            }
            insertRow();
        }
        
        beforeFirst();
    }

    /**
     * Returns the column number in this result set that the given level's
     * members are shown in.
     * 
     * @param level
     *            The level whose column number to look up. Hint: only levels
     *            that were in the original cell set's rows axis will have their
     *            own column in this result set. The levels of the columns axis
     *            are handled differently.
     * @return The column in this result set that corresponds with the given
     *         Level, or -1 if the given level does not have a column in this
     *         result set.
     */
    private int findColumnForLevel(Level level) {
        int index = rowAxisColumns.indexOf(level);
        if (index >= 0) {
            return index + 1;
        } else {
            return -1;
        }
    }

    /**
     * Provides the inverse mapping of {@link #findColumnForLevel(Level)}.
     * 
     * @param colNum
     *            The result set column number (1-based; first column is 1).
     * @return The Level associated with the given column, or null if the given
     *         column does not exist or is not a Level column.
     * @throws SQLException
     *             if colNum is not a legal column index in this result set.
     */
    private Level getLevelForColumn(int colNum) throws SQLException {
        if (colNum > rsmd.getColumnCount() || colNum < 1) {
            throw new SQLException(
                    "Column index out of bounds. You requested " + colNum +
                    "; legal range is 1.." + rsmd.getColumnCount());
        } else if (colNum <= rowAxisColumns.size()) {
            return rowAxisColumns.get(colNum - 1);
        } else {
            return null;
        }
    }

    /**
     * 
     * @param axis
     * @return
     */
    private List<Level> determineRowAxisColumns(CellSetAxis axis) {
        Set<Level> levelsEncountered = new HashSet<Level>();
        for (Position p : axis.getPositions()) {
            for (Member m : p.getMembers()) {
                levelsEncountered.add(m.getLevel());
            }
        }
        
        List<Level> columnAssignments = new ArrayList<Level>();
        for (Hierarchy h : axis.getAxisMetaData().getHierarchies()) {
            for (Level l : h.getLevels()) {
                if (levelsEncountered.contains(l)) {
                    columnAssignments.add(l);
                }
            }
        }
        
        return columnAssignments;
    }
}
