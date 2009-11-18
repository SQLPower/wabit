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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.WorkspaceGraphModel;


public class WabitAccessManager {

	public enum Permission {
		CREATE(),
		MODIFY(),
		DELETE(),
		EXECUTE(),
		GRANT(),
		REMOVE_PROPERTY();
		
		private Permission() {
			//no state beyond enum constant
		}
	}
	
	private User currentUser;
	
	private WabitSession systemSession;
	
	private WabitSession currentSession;
	
	public WabitAccessManager() {
		// Due to jackrabbit's access manager interface, state is defined in init
	}
	
	public void init(User currentUser, WabitSession currentSession, WabitSession systemSession) {
		this.currentUser = currentUser;
		this.currentSession = currentSession;
		this.systemSession = systemSession;
		
	}
	
	public void init(User currentUser, WabitSession systemSession) {
		init(currentUser, null, systemSession);
	}
	
	private List<Grant> aggregateGrants(User user) {
		List<Grant> grants = new ArrayList<Grant>();
		grants.addAll(user.getGrants());

		final WabitWorkspace workspace = getSystemSession().getWorkspace();
		synchronized (workspace) {
			List<Group> groups = workspace.getGroups();
			for (Group group : groups) {
				for (GroupMember member : group.getChildren(GroupMember.class)) {
					if (member.getUser().equals(user)) {
						grants.addAll(group.getChildren(Grant.class));
						break;
					}
				}
			}
			return grants;
		}
	}
	
	/**
	 * Returns all objects that depend on the object represented by rootUuid
	 */
	private Collection<SPObject> aggregateDependantObjects(String rootUuid) {
		if (getCurrentSession() == null) {
			return Collections.emptyList();
		}
		final WabitWorkspace workspace = getCurrentSession().getWorkspace();
		synchronized (workspace) {
			WabitObject root = workspace
					.findByUuid(rootUuid, WabitObject.class);
			// Must find all dependent objects, but not ancestors
			WorkspaceGraphModel graph = new WorkspaceGraphModel(workspace,
					root, true, true);
			List<SPObject> parents = new LinkedList<SPObject>();
			for (SPObject wo : graph.getNodes()) {
				if (wo instanceof WabitWorkspace) {
					parents.add(wo);
					continue;
				}
				while (!(wo.getParent() instanceof WabitWorkspace)) {
					wo = wo.getParent();
				}
				parents.add(wo);
			}
			return parents;
		}
	}
	
	/**
	 * Checks if the current User has grants to perform the action represented
	 * by the given Grant
	 * 
	 */
	public boolean isGranted(String subject, String type, Set<Permission> permissions) {
		//logger.debug("    User is " + user.getUsername());

		if (getCurrentUser().getUUID().equals(subject)) {
			permissions.remove(Permission.EXECUTE);
			permissions.remove(Permission.MODIFY);
			if (permissions.isEmpty()) {
				//logger.debug("    User has sufficient permissions (Object is User)");
				return true;
			}
		}
		
		if (!isReadOnly(permissions) && !type.equals(WabitWorkspace.class.getSimpleName()) && !isWorkspaceGranted(EnumSet.of(Permission.MODIFY))) {
			//logger.debug("    User does not have permission (insufficient workspace permissions)");
			return false;
		}

		Collection<SPObject> dependantObjects = aggregateDependantObjects(subject);
		Set<String> dependants = new HashSet<String>();
		for (SPObject wo : dependantObjects) {
			dependants.add(wo.getUUID());
		}

		// This unsafe casting is allowed and desirable because if we ever
		// nest the users elsewhere, we'll get an exception from the tests here.
		WabitWorkspace workspace = (WabitWorkspace) getCurrentUser().getParent();
		synchronized (workspace) {
			List<Grant> grants = aggregateGrants(getCurrentUser());
			//logger.debug("    User has " + grants.size() + " grants");
			for (Grant grant : grants) {
				if (grant.getSubject() != null) {
					if (grant.getSubject().equals(subject)) {
						if (grant.isModifyPrivilege()) {
							permissions.remove(Permission.MODIFY);
							permissions.remove(Permission.REMOVE_PROPERTY);
							permissions.remove(Permission.EXECUTE);
						}
						if (grant.isDeletePrivilege()) {
							permissions.remove(Permission.DELETE);
							permissions.remove(Permission.REMOVE_PROPERTY);
							permissions.remove(Permission.EXECUTE);
						}
						if (grant.isExecutePrivilege()) {
							permissions.remove(Permission.EXECUTE);
						}
					} else if (dependants.contains(grant.getSubject())) {
						if (grant.isModifyPrivilege()) {
							permissions.remove(Permission.EXECUTE);
						}
						if (grant.isDeletePrivilege()) {
							permissions.remove(Permission.EXECUTE);
						}
						if (grant.isExecutePrivilege()) {
							permissions.remove(Permission.EXECUTE);
						}
					}
				}
				if (permissions.isEmpty()) {
					//logger.debug("    User has sufficient permissions (object-level)");
					return true;
				}
			}

			// specific object permissions didn't help; fall back on system
			// permissions
			if (doSystemGrantsPermit(type, grants, dependantObjects, permissions)) {
				//logger.debug("    User has sufficient permissions (system-level)");
				return true;
			} else {
				//logger.debug("    User does not have permission");
				return false;
			}
		}
	}
	
	public boolean isGranted(String type, Set<Permission> permissions) {
		synchronized (getSystemSession().getWorkspace()) {
			List<Grant> grants = aggregateGrants(getCurrentUser());
			Set<SPObject> empty = Collections.emptySet();
			return doSystemGrantsPermit(type, grants, empty, permissions);
		}
	}
	
	public boolean isWorkspaceGranted(Set<Permission> permissions) {
		// Everyone has read access to the system workspace
		WabitWorkspace currentWorkspace = getCurrentSession().getWorkspace();
		if (currentWorkspace.getUUID().equals("system")) {
			if (isReadOnly(permissions)) {
				return true;
			} else if (permissions.contains(Permission.DELETE)) {
				return false;
			}
		}

		if (isGranted(currentWorkspace.getUUID(), WabitWorkspace.class.getSimpleName(), permissions)) {
			return true;
		}

		if (isReadOnly(permissions)) {
			for (SPObject wo : currentWorkspace.getChildren()) {
				if (isGranted(wo.getUUID(), wo.getClass().getSimpleName(), EnumSet.copyOf(permissions))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean doSystemGrantsPermit(@Nullable String type,
			List<Grant> grants, Collection<SPObject> dependantObjects, Set<Permission> permissions) {
		if (type == null) {
			return false;
		}
		
		Set<String> dependantTypes = new HashSet<String>();
		for (SPObject wo : dependantObjects) {
			dependantTypes.add(wo.getClass().getSimpleName());
		}
		
		for (Grant grant : grants) {
			if (grant.getSubject() == null) {
				if (grant.getType().equals(type)) {
					//logger.debug("        Grant is relevant");
					if (grant.isModifyPrivilege()) {
						permissions.remove(Permission.MODIFY);
						permissions.remove(Permission.REMOVE_PROPERTY);
						permissions.remove(Permission.EXECUTE);
					}
					if (grant.isDeletePrivilege()) {
						permissions.remove(Permission.DELETE);
						permissions.remove(Permission.REMOVE_PROPERTY);
						permissions.remove(Permission.EXECUTE);
					}
					if (grant.isExecutePrivilege()) {
						permissions.remove(Permission.EXECUTE);
					}
					if (grant.isCreatePrivilege()) {
						permissions.remove(Permission.CREATE);
					}
				}
				else if (dependantTypes.contains(grant.getType())) {
					if (grant.isModifyPrivilege()) {
						permissions.remove(Permission.EXECUTE);
					}
					if (grant.isDeletePrivilege()) {
						permissions.remove(Permission.EXECUTE);
					}
					if (grant.isExecutePrivilege()) {
						permissions.remove(Permission.EXECUTE);
					}
				}
				if (permissions.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isGrantGranted(Grant newGrant) {
		boolean createPrivilege = newGrant.isCreatePrivilege();
		boolean modifyPrivilege = newGrant.isModifyPrivilege();
		boolean deletePrivilege = newGrant.isDeletePrivilege();
		boolean executePrivilege = newGrant.isExecutePrivilege();
		boolean grantPrivilege = newGrant.isGrantPrivilege();
		
		String type = newGrant.getType();
		if (newGrant.getSubject() != null) {
			String subject = newGrant.getSubject();
			
			if (subject.equals(getCurrentUser().getUUID())
					&& !(modifyPrivilege || deletePrivilege || grantPrivilege)) {
				return true;
			}
			
			grantPrivilege = true; // Require Grant privilege

			List<Grant> grants = aggregateGrants(getCurrentUser());
			for (Grant grant : grants) {
				if (grant.getSubject() != null) {
					if (grant.getSubject().equals(subject)) {
						if (grant.isModifyPrivilege())
							modifyPrivilege = false;
						if (grant.isDeletePrivilege())
							deletePrivilege = false;
						if (grant.isGrantPrivilege())
							grantPrivilege = false;
						if (grant.isExecutePrivilege())
							executePrivilege = false;
					}
				} else if (grant.getType().equals(type)) {
					if (grant.isModifyPrivilege())
						modifyPrivilege = false;
					if (grant.isDeletePrivilege())
						deletePrivilege = false;
					if (grant.isGrantPrivilege())
						grantPrivilege = false;
					if (grant.isExecutePrivilege())
						executePrivilege = false;
				}
				if (!(modifyPrivilege || deletePrivilege || executePrivilege || grantPrivilege)) {
					return true;
				}
			}
		} else {
			grantPrivilege = true;
			List<Grant> grants = aggregateGrants(getCurrentUser());
			for (Grant grant : grants) {
				if (grant.getSubject() == null
						&& grant.getType().equals(type)) {
					if (grant.isCreatePrivilege())
						createPrivilege = false;
					if (grant.isModifyPrivilege())
						modifyPrivilege = false;
					if (grant.isDeletePrivilege())
						deletePrivilege = false;
					if (grant.isGrantPrivilege())
						grantPrivilege = false;
					if (grant.isExecutePrivilege())
						executePrivilege = false;
				}
				if (!(createPrivilege || modifyPrivilege || deletePrivilege
						|| executePrivilege || grantPrivilege)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public WabitSession getCurrentSession() {
		return currentSession;
	}
	
	public WabitSession getSystemSession() {
		return systemSession;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}
	
	private boolean isReadOnly(Set<Permission> permissions) {
		return permissions.contains(Permission.EXECUTE) && permissions.size() == 1;
	}
	
}
