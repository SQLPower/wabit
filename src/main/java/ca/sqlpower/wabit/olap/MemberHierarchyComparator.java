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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Member;

public class MemberHierarchyComparator implements Comparator<Member> {
    
    private static final Logger logger = Logger.getLogger(MemberHierarchyComparator.class);

    public int compare(Member m1, Member m2) {
        if (m1.equals(m2)) return 0;
        
        // Find common ancestor
        List<Member> m1path = path(m1);
        List<Member> m2path = path(m2);
        
        int i = 0;
        while (i < m1path.size() && i < m2path.size()) {
            if (! m1path.get(i).equals((m2path).get(i))) break;
            i++;
        }
        
        // Lowest common ancestor is m1path.get(i - 1), but we don't care
        
        if (m1path.size() == i) return -1;
        if (m2path.size() == i) return 1;
        logger.debug("m1path[i] ordinal=" + m1path.get(i).getOrdinal() + " name=" + m1path.get(i).getName());
        logger.debug("m2path[i] ordinal=" + m2path.get(i).getOrdinal() + " name=" + m2path.get(i).getName());
        return m1path.get(i).getName().compareToIgnoreCase(m2path.get(i).getName());
    }
    
    private List<Member> path(Member m) {
        List<Member> path = new LinkedList<Member>();
        Member temp = m;
        while (temp != null) {
            path.add(0, temp);
            temp = temp.getParentMember();
        }
        return path;
    }
}
