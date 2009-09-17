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

package ca.sqlpower.wabit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * If errors occur during cleanup this object will contain the exceptions
 * and error messages.
 */
public class CleanupExceptions {
    
    private final List<Exception> exceptions = new ArrayList<Exception>();
    private final List<String> errorMessages = new ArrayList<String>();

    /**
     * Adds an exception to this set of exceptions that occurred during cleanup.
     */
    public void add(Exception e) {
        exceptions.add(e);
    }

    /**
     * Adds an error message to this set of errors that occurred during cleanup.
     */
    public void add(String message) {
        errorMessages.add(message);
    }
    
    /**
     * Add all of the exceptions and error messages in the given CleanupExceptions
     * to this exception.
     */
    public void add(CleanupExceptions ex) {
        exceptions.addAll(ex.getExceptions());
        errorMessages.addAll(ex.getErrorMessages());
    }
    
    /**
     * Returns true if there were no exceptions or error messages during cleanup.
     */
    public boolean isCleanupSuccessful() {
        return exceptions.isEmpty() && errorMessages.isEmpty();
    }
    
    /**
     * Returns an unmodifiable copy of the exceptions that occurred during cleanup.
     */
    public List<Exception> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Returns an unmodifiable copy of the error messages that occurred during cleanup.
     * @return
     */
    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }
}
