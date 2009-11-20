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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;

import ca.sqlpower.enterprise.client.Grant;
import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.GroupMember;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.enterprise.client.security.SPAccessManager;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SPSession;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.WorkspaceGraphModel;


public class WabitAccessManager implements SPAccessManager{

	private static final Logger logger = Logger
			.getLogger(WabitAccessManager.class);
	
	private User currentUser;
	
	private SPSession systemSession;
	
	private SPSession currentSession;
	
	public WabitAccessManager() {
		// Due to jackrabbit's access manager interface, state is defined in init
	}

	/**
	 * Initializes this Access Manager.
	 * 
	 * @param currentUser
	 *            The user to determine access for.
	 * @param currentSession
	 *            The session that contains all objects in question. It will be
	 *            used to look up dependencies.
	 * @param systemSession
	 *            The system session that contains currentUser, as well as all
	 *            groups and grants that apply
	 */
	public void init(@Nonnull User currentUser, @Nullable SPSession currentSession, @Nonnull SPSession systemSession) {
		this.currentUser = currentUser;
		this.currentSession = currentSession;
		this.systemSession = systemSession;
		
	}

	/**
	 * Initializes this Access Manager. It will not resolve dependencies when
	 * checking access to an object.
	 * 
	 * @param currentUser
	 *            The user to determine access for.
	 * @param systemSession
	 *            The system session that contains currentUser, as well as all
	 *            groups and grants that apply.
	 */
	public void init(@Nonnull User currentUser, @Nonnull WabitSession systemSession) {
		init(currentUser, null, systemSession);
	}

	/**
	 * Collects all grants that apply to the given user.
	 * 
	 * @return A {@link List} of all grants for the given user, or any group
	 *         they are a member of.
	 */
	private List<Grant> aggregateGrants(@Nonnull User user) {
		List<Grant> grants = new ArrayList<Grant>();
		grants.addAll(user.getGrants());

		final SPObject workspace = getSystemSession().getWorkspace();
		synchronized (workspace) {
			List<Group> groups = workspace.getChildren(Group.class);
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
	 * Returns all objects that depend on the object represented by rootUuid. If
	 * a user has read access on an object, he must also have read access on all
	 * objects it depends upon.
	 * 
	 * @param rootUuid
	 *            The UUID of the object in question.
	 * @return A Collection of all objects that depend on the given object.
	 */
	private Collection<SPObject> aggregateDependantObjects(@Nonnull String rootUuid) {
		if (getCurrentSession() == null) {
			return Collections.emptyList();
		}
		final SPObject workspace = getCurrentSession().getWorkspace();
		synchronized (workspace) {
			SPObject root = SQLPowerUtils.findByUuid(workspace, rootUuid, SPObject.class);
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
	 * Checks if the current user has permission to perform the action
	 * represented by the given set of {@link Permission}s on the object with
	 * UUID <code>subject</code>.
	 * 
	 * @param subject
	 *            The UUID of the object in question.
	 * @param type
	 *            The simple name of the class of the object in question. If
	 *            null, system level permissions will not be checked.
	 * @param permissions
	 *            A Set of {@link Permission}s that represent the action in
	 *            question. If empty, this method returns true.
	 */
	public boolean isGranted(@Nonnull String subject, @Nullable String type, @Nonnull Set<Permission> permissions) {

		// Users are given read and modify privilege on themselves
		if (getCurrentUser().getUUID().equals(subject)) {
			permissions.remove(Permission.EXECUTE);
			permissions.remove(Permission.MODIFY);
			if (permissions.isEmpty()) {
				logger.debug("    User has sufficient permissions (Object is User)");
				return true;
			}
		}
		
		if (permissions.isEmpty()) {
			return true;
		}
		
		// This is rule specific for the Wabit. To modify any object in a
		// workspace, a User must have modify privilege on the Workspace.
		if (!isReadOnly(permissions) && !WabitWorkspace.class.getSimpleName().equals(type) && !isWorkspaceGranted(EnumSet.of(Permission.MODIFY))) {
			logger.debug("    User does not have permission (insufficient workspace permissions)");
			return false;
		}

		Collection<SPObject> dependantObjects = aggregateDependantObjects(subject);
		Set<String> dependants = new HashSet<String>();
		for (SPObject wo : dependantObjects) {
			dependants.add(wo.getUUID());
		}

		// This unsafe casting is allowed and desirable because if we ever
		// nest the users elsewhere, we'll get an exception from the tests here.
		//
		// If we decide to move the AccessManager into the library for use in
		// Architect EE, this will have to change.
		WabitWorkspace workspace = (WabitWorkspace) getCurrentUser().getParent();
		synchronized (workspace) {
			List<Grant> grants = aggregateGrants(getCurrentUser());
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
					logger.debug("    User has sufficient permissions (object-level)");
					return true;
				}
			}

			// specific object permissions didn't help; fall back on system
			// permissions
			if (doSystemGrantsPermit(type, grants, dependantObjects, permissions)) {
				logger.debug("    User has sufficient permissions (system-level)");
				return true;
			} else {
				logger.debug("    User does not have permission");
				return false;
			}
		}
	}

	/**
	 * Checks if the current user has permission to perform the action
	 * represented by the given set of {@link Permission}s on objects of class
	 * <code>type</code>
	 * 
	 * @param type
	 *            The simple name of the class of object in question.
	 * @param permissions
	 *            A Set of {@link Permission}s that represent the action in
	 *            question. If empty, this method returns true.
	 */
	public boolean isGranted(@Nonnull String type, @Nonnull Set<Permission> permissions) {
		synchronized (getSystemSession().getWorkspace()) {
			List<Grant> grants = aggregateGrants(getCurrentUser());
			Set<SPObject> empty = Collections.emptySet();
			return doSystemGrantsPermit(type, grants, empty, permissions);
		}
	}

	/**
	 * Checks if the current user has permission to perform the action
	 * represented by the given set of {@link Permission}s on the current
	 * workspace.
	 * 
	 * @param permissions
	 *            A Set of {@link Permission}s that represent the action in
	 *            question. If empty, this method returns true.
	 */
	public boolean isWorkspaceGranted(@Nonnull Set<Permission> permissions) {
		if (permissions.isEmpty()) {
			return true;
		}
		
		// Everyone has read access to the system workspace
		SPObject currentWorkspace = getCurrentSession().getWorkspace();
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

		// This is rule specific for Wabit. Read permission on any top level
		// child grants read access to the workspace.
		if (isReadOnly(permissions)) {
			for (SPObject wo : currentWorkspace.getChildren()) {
				if (isGranted(wo.getUUID(), wo.getClass().getSimpleName(), EnumSet.copyOf(permissions))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given grants give permission to perform the action
	 * represented by <code>permissions</code> on the given type of object. Note
	 * that this method will not necessarily represent whether the current User
	 * can perform the action, only whether a User with the given grants can.
	 * 
	 * @param type
	 *            The simple name of the class of object in question.
	 * @param grants
	 *            A Collection of grants to check for the given permissions.
	 * @param dependantObjects
	 *            A List of all objects that are dependant on an object in
	 *            question. If the purpose of this call is to check if system
	 *            level permissions apply to a specific object, this should be
	 *            its dependant objects. Else, if the purpose is to check
	 *            permissions on all objects of the type, this should be empty.
	 * @param permissions
	 *            A Set of {@link Permission}s that represent the action in
	 *            question. If empty, this method returns true.
	 */
	public boolean doSystemGrantsPermit(@Nullable String type,
			List<Grant> grants, Collection<SPObject> dependantObjects, Set<Permission> permissions) {
		if (type == null) {
			return false;
		}
		if (permissions.isEmpty()) {
			return true;
		}
		
		Set<String> dependantTypes = new HashSet<String>();
		for (SPObject wo : dependantObjects) {
			dependantTypes.add(wo.getClass().getSimpleName());
		}
		
		for (Grant grant : grants) {
			if (grant.getSubject() == null) {
				if (grant.getType().equals(type)) {
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
	
	/**
	 * Checks if the current user has sufficient privilege to create the given {@link Grant}.
	 */
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
	
	public SPSession getCurrentSession() {
		return currentSession;
	}
	
	public SPSession getSystemSession() {
		return systemSession;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Checks if the given Set contains the execute privilege, and no others.
	 */
	private boolean isReadOnly(Set<Permission> permissions) {
		return permissions.contains(Permission.EXECUTE) && permissions.size() == 1;
	}
	
}
