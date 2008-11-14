/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import ca.sqlpower.swingui.event.SessionLifecycleListener;

/**
 * The basic interface for a Wabit session. This interface provides all the
 * UI-independent state and behaviour of a Wabit session. 
 */
public interface WabitSession {

	public void addSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	public void removeSessionLifecycleListener(SessionLifecycleListener<WabitSession> l);

	/**
	 * Returns the context this session belongs to.
	 */
	public WabitSessionContext getContext();

	/**
	 * Ends this session, disposing its frame and releasing any system
	 * resources that were obtained explicitly by this session. Also
	 * fires a sessionClosing lifecycle event, so any resources used up
	 * by subsystems dependent on this session can be freed by the appropriate
	 * parties.
	 */
	public void close();

	/**
	 * Returns the project associated with this session.
	 */
	public WabitProject getProject();
}
