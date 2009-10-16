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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;

public class Grant extends AbstractWabitObject {

    private final String type;
    private final String subject;
    private boolean dirty = false;
    private boolean createPrivilege = false;
    private boolean modifyPrivilege = false;
    private boolean deletePrivilege = false;
    private boolean executePrivilege = false;
    private boolean grantPrivilege = false;

    /**
     * Creates a grant object.
     * @param subject The object we want to grant access to. Can be null
     * if the type parameter is used.
     * @param type The class of wabit object to grant access to. Can be null
     * if the subject parameter is used.
     * @param create 
     * @param modify
     * @param delete
     * @param execute
     * @param grant
     */
    public Grant(String subject, String type,
            boolean create, boolean modify, boolean delete, boolean execute,
            boolean grant) 
    {
        this.subject = subject;
        this.type = type;
        this.createPrivilege = create;
        this.modifyPrivilege = modify;
        this.deletePrivilege = delete;
        this.executePrivilege = execute;
        this.grantPrivilege = grant;
    }

    @Override
    public String getName() {
        if (this.subject != null) {
            return this.subject.concat(" - ").concat(this.getPermsString());
        } else if (this.type != null){
            return "All ".concat(this.type)
                .concat(" - ").concat(this.getPermsString());
        } else {
            throw new RuntimeException("Badly constructed grant object.");
        }
    }

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }

    public void removeDependency(WabitObject dependency) {
        // no-op
    }

    public boolean isCreatePrivilege() {
        return createPrivilege;
    }

    public void setCreatePrivilege(boolean createPrivilege) {
        boolean oldValue = this.createPrivilege;
        this.createPrivilege = createPrivilege;
        firePropertyChange("createPrivilege", oldValue, this.createPrivilege);
    }

    public boolean isModifyPrivilege() {
        return modifyPrivilege;
    }

    public void setModifyPrivilege(boolean modifyPrivilege) {
        boolean oldValue = this.modifyPrivilege;
        this.modifyPrivilege = modifyPrivilege;
        firePropertyChange("modifyPrivilege", oldValue, this.modifyPrivilege);
    }

    public boolean isDeletePrivilege() {
        return deletePrivilege;
    }

    public void setDeletePrivilege(boolean deletePrivilege) {
        boolean oldValue = this.deletePrivilege;
        this.deletePrivilege = deletePrivilege;
        firePropertyChange("deletePrivilege", oldValue, this.deletePrivilege);
    }

    public boolean isExecutePrivilege() {
        return executePrivilege;
    }

    public void setExecutePrivilege(boolean executePrivilege) {
        boolean oldValue = this.executePrivilege;
        this.executePrivilege = executePrivilege;
        firePropertyChange("executePrivilege", oldValue, this.executePrivilege);
    }

    public boolean isGrantPrivilege() {
        return grantPrivilege;
    }

    public void setGrantPrivilege(boolean grantPrivilege) {
        boolean oldValue = this.grantPrivilege;
        this.grantPrivilege = grantPrivilege;
        firePropertyChange("grantPrivilege", oldValue, this.grantPrivilege);
    }

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }
    
    private String getPermsString() {
        StringBuilder sb = new StringBuilder("");
        if (this.createPrivilege) {
            sb.append("C");
        }
        if (this.modifyPrivilege) {
            sb.append("M");
        }
        if (this.deletePrivilege) {
            sb.append("D");
        }
        if (this.executePrivilege) {
            sb.append("E");
        }
        if (this.grantPrivilege) {
            sb.append("G");
        }
        return sb.toString();
    }

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
