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

package ca.sqlpower.wabit.enterprise.client.security;

import java.util.Set;

import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.enterprise.client.User;


public class CachingWabitAccessManager extends WabitAccessManager {

	private static final long CACHE_FLUSH_INTERVAL = 5000;

	private final GrantCache cache = new GrantCache(CACHE_FLUSH_INTERVAL);
	
	/**
	 * Checks if the current User has grants to perform an action on the given
	 * type of object
	 * 
	 * @param type
	 *            The type of wabit object in question. This is the simple name
	 *            of the class
	 * @param permissions
	 *            The permissions requested, as a set of {@link Permission} enums
	 */
	public boolean isGranted(String type, Set<Permission> permissions) {
		final GrantCache.CacheKey cacheKey = new GrantCache.CacheKey(null, type, permissions);
		Boolean answer = cache.get(cacheKey);
		if (answer == null) {
			answer = super.isGranted(type, permissions);
			cache.put(cacheKey, answer);
		}
		return answer;
	}
	
	/**
	 * Checks if the current User has grants to perform an action on the given
	 * UUID
	 * 
	 * @param subject
	 *            The UUID of the wabit object in question. This must be a
	 *            secured object (A WabitWorkspace, or a first level child of a
	 *            workspace)
	 * @param type
	 *            The type of the wabit object in question. This is the simple
	 *            name of the class of the object.
	 * @param permissions
	 *            The permissions requested, as a set of {@link Permission} enums
	 */
	public boolean isGranted(String subject, String type, Set<Permission> permissions) {
		
		final GrantCache.CacheKey cacheKey = new GrantCache.CacheKey(subject, type, permissions);
		Boolean answer = cache.get(cacheKey);
		if (answer == null) {
			answer = super.isGranted(subject, type, permissions);
			cache.put(cacheKey, answer);
		}
		return answer;
	}

}
