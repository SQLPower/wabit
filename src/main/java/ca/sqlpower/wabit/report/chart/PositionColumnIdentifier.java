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

import java.util.ArrayList;
import java.util.List;

import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

/**
 * This identifier represents a column in a {@link CellSet} represented by a {@link Position}.
 * The Position is not stored itself but the members that make up the position are stored as
 * they are unique.
 */
public class PositionColumnIdentifier extends AbstractColumnIdentifier {
    
    private final List<String> uniqueMemberNames = new ArrayList<String>();
    private final List<String> memberNames = new ArrayList<String>();

    /**
     * Returns the current object being used as the unique identifier of a
     * position. This value may change when the Position object is comparable.
     * 
     * @param position
     *            The position to get the unique identifier used in this column
     *            identifier.
     * @return The unique identifier that this class makes from the position to
     *         use in comparisions.
     */
    public static List<String> generateUniqueIdentifier(Position position) {
        List<String> uniqueMemberNames = new ArrayList<String>();
        for (Member member : position.getMembers()) {
            uniqueMemberNames.add(member.getUniqueName());
        }
        return uniqueMemberNames;
    }
    
    public PositionColumnIdentifier(Position position) {
        uniqueMemberNames.addAll(generateUniqueIdentifier(position));
        for (Member member : position.getMembers()) {
            memberNames.add(member.getName());
        }
    }

    public PositionColumnIdentifier(List<String> memberPositionNames) {
        uniqueMemberNames.addAll(memberPositionNames);
        for (String memberName : uniqueMemberNames) {
            String[] uniqueMemberNameList = memberName.split("\\]\\.\\[");
            String name = uniqueMemberNameList[uniqueMemberNameList.length - 1];
            name = name.substring(0, name.length() - 1);
            memberNames.add(name);
        }
    }

    public Object getUniqueIdentifier() {
        return getUniqueMemberNames();
    }
    
    public List<String> getUniqueMemberNames() {
        return uniqueMemberNames;
    }
    
    public Position getPosition(CellSet cellSet) {
        List<Member> members = new ArrayList<Member>();
        try {
            for (String memberName : uniqueMemberNames) {
                String[] uniqueMemberNameList = memberName.split("\\]\\.\\[");
                uniqueMemberNameList[0] = uniqueMemberNameList[0].substring(1); //remove starting [ bracket
                final int lastMemberNamePosition = uniqueMemberNameList.length - 1;
                uniqueMemberNameList[lastMemberNamePosition] = uniqueMemberNameList[lastMemberNamePosition].substring(0, uniqueMemberNameList[lastMemberNamePosition].length() - 1); //remove ending ] bracket
                members.add(cellSet.getMetaData().getCube().lookupMember(uniqueMemberNameList));
            }
        } catch (OlapException e) {
            throw new RuntimeException(e);
        }
        
        return getColumnPositionFromMembers(cellSet, members);
    }
    
    
    /**
     * This is a helper method for finding a {@link Position} based on the list of 
     * members identifying a column used in a chart. If the position defined by the
     * member list is not found null is returned.
     */
    private static Position getColumnPositionFromMembers(CellSet cellSet, List<Member> members) {
        CellSetAxis columnAxis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());
        for (Position position : columnAxis.getPositions()) {
            if (position.getMembers().equals(members)) {
                return position;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        
        if (obj instanceof PositionColumnIdentifier) {
            PositionColumnIdentifier ci = (PositionColumnIdentifier) obj;
            return getUniqueMemberNames().equals(ci.getUniqueMemberNames());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + uniqueMemberNames.hashCode();
        return result;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        for (String memberName : memberNames) {
            if (memberNames.indexOf(memberName) != 0) {
                sb.append(", ");
            }
            sb.append(memberName);
        }
        return sb.toString();
    }

}
