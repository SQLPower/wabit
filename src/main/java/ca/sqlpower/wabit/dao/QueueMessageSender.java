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

import java.util.Queue;

import ca.sqlpower.dao.SPPersistenceException;

/**
 * A {@link MessageSender} implementation that places message contents onto a
 * Queue.
 * 
 * @param <T>
 *            The Object type that will represent message content
 */
public class QueueMessageSender<T> implements MessageSender<T> {

	private final Queue<T> queue;

	public QueueMessageSender(Queue<T> queue) {
		this.queue = queue;
	}

	public Queue<T> getQueue() {
		return queue;
	}

	public void send(T content) throws SPPersistenceException {
		queue.add(content);
	}

	public void flush() throws SPPersistenceException {
		// no-op. Not sure what 'flushing' a queue would really do
	}
	
	public void clear() {
		queue.clear();
	}
}
