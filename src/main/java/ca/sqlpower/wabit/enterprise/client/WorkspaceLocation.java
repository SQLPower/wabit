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

package ca.sqlpower.wabit.enterprise.client;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Describes the location of a remote workspace.
 */
@Immutable
public class WorkspaceLocation {

	/**
	 * The user-given name for the workspace pointed to by this instance.
	 */
	private final String name;
	
	/**
	 * The UUID of the workspace pointed to by this instance.
	 */
	private final String uuid;
	
	/**
	 * The server location of the workspace pointed to by this instance.
	 */
	private final WabitServerInfo serviceInfo;

	public WorkspaceLocation(
			 @Nonnull String name,
			 @Nonnull String uuid,
			 @Nonnull WabitServerInfo serviceInfo) {
		if (name == null) throw new NullPointerException("Null name not permitted");
		this.name = name;

		if (uuid == null) throw new NullPointerException("Null workspace uuid not permitted");
		this.uuid = uuid;
		
		if (serviceInfo == null) throw new NullPointerException("Null serviceInfo not permitted");
		this.serviceInfo = serviceInfo;
	}

	public @Nonnull String getName() {
		return name;
	}

	public @Nonnull String getUuid() {
		return uuid;
	}

	public @Nonnull WabitServerInfo getServiceInfo() {
		return serviceInfo;
	}
	
}
