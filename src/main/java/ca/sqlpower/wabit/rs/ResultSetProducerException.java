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

/**
 * A checked exception that can be thrown by result set producers. Typically, an
 * exception of this type is wrapping an underlying exception such as
 * SQLException or OlapException.
 */
public class ResultSetProducerException extends Exception {

    public ResultSetProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultSetProducerException(String message) {
        super(message);
    }

    public ResultSetProducerException(Throwable cause) {
        super(cause);
    }

}
