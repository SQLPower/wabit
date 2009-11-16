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

import org.olap4j.Position;
import org.olap4j.metadata.Member;

import ca.sqlpower.wabit.rs.olap.MemberHierarchyComparator;

/**
 * This object is used to define a row in a category dataset for an OLAP
 * dataset. Each row can be defined by a combination of a {@link Position} and
 * any number of strings which are the values in the columns defined as
 * categories. A position is always less than a string for the comparison and a
 * shorter list is less than a longer one.
 */
public class ComparableCategoryRow implements Comparable<ComparableCategoryRow> {

    /**
     * This list contains the elements being compared to in the order they are to be compared.
     */
    private final List<Object> comparableObjects = new ArrayList<Object>();
    
    private final MemberHierarchyComparator comparator = new MemberHierarchyComparator();
    
    public int compareTo(ComparableCategoryRow o) {
        int i;
        for (i = 0; i < comparableObjects.size(); i++) {
            if (o.comparableObjects.size() == i) return 1;
            
            Object thisObject = comparableObjects.get(i);
            Object otherObject = o.comparableObjects.get(i);
            
            if (thisObject instanceof String && otherObject instanceof Position) return 1;
            if (thisObject instanceof Position && otherObject instanceof String) return -1;
            if (thisObject instanceof String && otherObject instanceof String) {
                int comparedValue = ((String) thisObject).compareTo((String) otherObject);
                if (comparedValue != 0) return comparedValue;
            } else if (thisObject instanceof Position && otherObject instanceof Position) {
                int j;
                final Position thisPosition = (Position) thisObject;
                final Position otherPosition = (Position) otherObject;
                for (j = 0; j < thisPosition.getMembers().size(); j++) {
                    if (otherPosition.getMembers().size() == j) return 1;
                    Member thisMember = thisPosition.getMembers().get(j);
                    Member otherMember = otherPosition.getMembers().get(j);
                    int comparedValue = comparator.compare(thisMember, otherMember);
                    if (comparedValue != 0) return comparedValue;
                }
                if (j < otherPosition.getMembers().size()) return -1;
            }
        }
        if (i < o.comparableObjects.size()) return -1;
        return 0;
    }

    public void add(String formattedValue) {
        comparableObjects.add(formattedValue);
    }

    public void add(Position position) {
        comparableObjects.add(position);
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object o : comparableObjects) {
            if (!first) sb.append(", ");
            if (o instanceof String) {
                sb.append((String) o);
            } else if (o instanceof Position) {
                boolean firstMember = true;
                for (Member member : ((Position) o).getMembers()) {
                    if (!first || !firstMember) sb.append(", ");
                    sb.append(member.getName());
                    firstMember = false;
                }
            }
            first = false;
        }
        return sb.toString();
    }
    
}
