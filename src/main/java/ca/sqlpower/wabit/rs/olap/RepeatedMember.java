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

package ca.sqlpower.wabit.rs.olap;

import javax.annotation.Nonnull;

import org.olap4j.metadata.Member;

import ca.sqlpower.wabit.rs.OlapResultSet;

/**
 * Wraps an Olap4j Member. Instances of this class are used by
 * {@link OlapResultSet} as markers for repeated instances of members. This
 * makes identification of rollup levels easier, especially when charting. For
 * example, the table cell renderer for the chart panel renders repeated members
 * in grey and regular members in the default foreground colour (usually black).
 */
public class RepeatedMember {
    
    private final Member member;

    /**
     * Creates a new wrapper for the given member.
     * 
     * @param m The member to wrap. Must not be null.
     */
    public RepeatedMember(@Nonnull Member m) {
        if (m == null) {
            throw new NullPointerException("Null member");
        }
        this.member = m;
    }
    
    /**
     * Returns the member wrapped by this repeated member.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Returns true if obj is a RepeatedMember and its wrapped member is equal
     * to this RepeatedMember's wrapped member.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RepeatedMember && member.equals(((RepeatedMember) obj).member);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + member.hashCode();
    }
    
    /**
     * Returns the member's name.
     */
    @Override
    public String toString() {
        return member.getName();
    }
}
