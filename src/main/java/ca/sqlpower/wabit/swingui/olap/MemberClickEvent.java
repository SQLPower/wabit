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

package ca.sqlpower.wabit.swingui.olap;

import javax.swing.JComponent;

import org.olap4j.Axis;
import org.olap4j.metadata.Member;

/**
 * Represents a user input event on some axis in the OLAP viewer.
 */
public class MemberClickEvent {

    public static enum Type {
        MEMBER_CLICKED, MEMBER_DROPPED
    }
    
    /**
     * The hierarchy component that the click happened on.
     */
    private final JComponent source;
    
    /**
     * The member that was clicked.
     */
    private final Member member;

    /**
     * The type of axis event this instance represents.
     */
    private final Type type;

    /**
     * The axis this event pertains to. Normally ROWS, COLUMNS, or FILTER.
     */
    private final Axis axis;

    /**
     * @param source
     *            The hierarchy component that the click happened on.
     *            <p>
     *            XXX breaks encapsulation--the ultimate consumer expects this
     *            event to come from the CellSetViewer; intermediate consumer
     *            expects it to come from the row header component. A compromise
     *            would be to make the hierarchy component an inner class of the
     *            header component class, and let the header component be the
     *            source.
     * @param type
     *            The type of axis event this instance represents.
     * @param axis
     *            The axis this event pertains to. Normally ROWS, COLUMNS, or
     *            FILTER.
     * @param member
     *            The member that was clicked, dropped, or is otherwise the
     *            subject of this event.
     */
    public MemberClickEvent(JComponent source, Type type, Axis axis, Member member) {
        this.source = source;
        this.type = type;
        this.axis = axis;
        this.member = member;
    }
    
    /**
     * The GUI component where the click originated.
     */
    public JComponent getSource() {
        return source;
    }

    /**
     * The type of axis event this instance represents.
     */
    public Type getType() {
        return type;
    }
    
    /**
     * The axis this event pertains to. Normally ROWS, COLUMNS, or FILTER.
     */
    public Axis getAxis() {
        return axis;
    }

    /**
     * The member that was clicked, dropped, or is otherwise the subject of this
     * event.
     */
    public Member getMember() {
        return member;
    }
}
