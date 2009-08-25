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

package ca.sqlpower.wabit.olap;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

import ca.sqlpower.sql.CachedResultSetMetaData;
import ca.sqlpower.sql.CachedRowSet;


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

    public OlapResultSet() throws SQLException {
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
        List<CellSetAxis> axes = cellSet.getAxes();
        if (axes.size() != 2) {
            throw new IllegalArgumentException(
                    "Only 2-axis Cell Sets are convertible to Result Sets. " +
                    "The given Cell Set has " + axes.size() + " axes.");
        }
        
        rsmd = new CachedResultSetMetaData();
        data = new ArrayList<Object[]>();
        
        // Rows axis: each represented level of each dimension is a column
        CellSetAxis rowsAxis = axes.get(Axis.ROWS.axisOrdinal());
        if (rowsAxis.getPositionCount() > 0) {
            Position firstRowPosition = rowsAxis.getPositions().get(0);
            for (Member m : firstRowPosition.getMembers()) {
                Dimension d = m.getDimension();
                Level l = m.getLevel();
                String colName = d.getName() + " " + l.getName();

                rsmd.addColumn(false, false, false, false, DatabaseMetaData.columnNullable,
                        true, 10, colName, colName, null, 1000, 0, null, null, Types.VARCHAR,
                        "Dimension Member", true, false, false, "java.lang.String");
            }
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

        // rows: each position along the rows axis!
        for (Position p : rowsAxis.getPositions()) {
            moveToInsertRow();
            int col = 1;
            for (Member m : p.getMembers()) {
                updateString(col, m.getName()); // TODO figure out placeholder value thing
                col++;
            }
            for (Position colPos : colsAxis.getPositions()) {
                Cell cell = cellSet.getCell(colPos, p);
                updateObject(col, cell.getDoubleValue()); // TODO what about empty cells?
                col++;
            }
            insertRow();
        }
        
        beforeFirst();
    }

}
