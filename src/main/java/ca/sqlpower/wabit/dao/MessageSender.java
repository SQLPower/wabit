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

package ca.sqlpower.wabit.dao;

import ca.sqlpower.dao.SPPersistenceException;

/**
 * A simple interface for anything that sends a message. The implementation
 * can support buffering, but it is not mandatory.
 * 
 * @param <T>
 *            The Object type that will represent the content of the message
 */
public interface MessageSender<T> {
	/**
	 * Adds a message for sending. It may not necessarily send the message
	 * immediately, for example if the implementation supports buffering, in
	 * which case you may have to call {@link #flush()} to actually send the
	 * messages.
	 * 
	 * @param content
	 * @throws SPPersistenceException
	 */
	public void send(T content) throws SPPersistenceException;

	/**
	 * Should the implementation support buffering, calling this will clear the
	 * buffered messages and send them. If the implementation chooses not to
	 * support buffering, this can be a simply no-op.
	 * 
	 * @throws SPPersistenceException
	 */
	public void flush() throws SPPersistenceException;
	
	/**
	 * Empties the current queue of messages to be sent.
	 */
	public void clear();
}
