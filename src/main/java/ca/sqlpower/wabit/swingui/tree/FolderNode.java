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

package ca.sqlpower.wabit.swingui.tree;

import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitDataSource;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.enterprise.client.Group;
import ca.sqlpower.wabit.enterprise.client.User;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;

/**
 * This is the FolderNode class and it represents the folder object in the tree.
 * This does not implement wabit object although it does use some of the same
 * class names as the WabitObject. 
 */
public class FolderNode {
	
    public static enum FolderType {
    	CONNECTIONS,
    	QUERIES,
    	CHARTS,
    	IMAGES,
    	REPORTS,
    	TEMPLATES,
    	USERS,
    	GROUPS
    }
    
    public static FolderType getProperFolderParent(WabitObject object) {
    	if (object instanceof WabitDataSource) {
    		return FolderType.CONNECTIONS;
    	} else if (object instanceof QueryCache || object instanceof OlapQuery) {
    		return FolderType.QUERIES; 
    	} else if (object instanceof Chart) {
    	    return FolderType.CHARTS;
        } else if (object instanceof WabitImage) {
            return FolderType.IMAGES;
    	} else if (object instanceof Report) {
			return FolderType.REPORTS;
    	} else if (object instanceof Template) {
    		return FolderType.TEMPLATES;
    	} else if (object instanceof User) {
    		return FolderType.USERS;
    	} else if (object instanceof Group) {
    		return FolderType.GROUPS;
    	}
    	throw new UnsupportedOperationException("Trying to find the parent folder of object of type: " + object.getClass());
    }
    
	private WabitWorkspace parent;
	private FolderType folderType;

	/**
	 * This is the FolderNode class and it represents the folder object in the tree.
	 * This does not implement wabit object although it does use some of the same
	 * class names as the WabitObject. 
	 * 
	 * @param parent
	 * 		The parent workspace this folder belongs to
	 * @param folderType
	 * 		This is the type of the folder.
	 */
	public FolderNode(WabitWorkspace parent, FolderType folderType) {
		this.parent = parent; //XXX this should be in a getter and setter, passing in the parent is wrong
		this.folderType = folderType;
	}

	public WabitWorkspace getParent() {
		return parent;
	}

	public FolderType getFolderType() {
		return folderType;
	}

	public boolean allowsChildren() {
		return true;
	}


	public List<? extends WabitObject> getChildren() {
		List<WabitObject> childList = new ArrayList<WabitObject>();
		switch (folderType) {
		case CONNECTIONS:
			childList.addAll(parent.getDataSources());
			break;
		case QUERIES:
			childList.addAll(parent.getQueries());
			childList.addAll(parent.getOlapQueries());
			break;
		case CHARTS:
		    childList.addAll(parent.getCharts());
		    break;
        case IMAGES:
            childList.addAll(parent.getImages());
            break;
		case REPORTS:
			childList.addAll(parent.getReports());
			break;
		case TEMPLATES:
			childList.addAll(parent.getTemplates());
			break;
		case USERS:
			childList.addAll(parent.getUsers());
			break;
		case GROUPS:
			childList.addAll(parent.getGroups());
			break;
		}
		return childList;
	}

	@Override
	public String toString() {
		String name = null;
		switch (folderType) {
		case CONNECTIONS:
			name = "Connections";
			break;
		case QUERIES:
			name = "Queries";
			break;
		case CHARTS:
		    name = "Charts";
		    break;
        case IMAGES:
            name = "Images";
            break;
		case REPORTS:
			name = "Reports & Dashboards";
			break;
		case TEMPLATES:
			name = "Templates";
			break;
		case USERS:
			name = "Users";
			break;
		case GROUPS:
			name = "Groups";
			break;
		}
		return name;
	}

	public int childPositionOffset(Class<? extends WabitObject> childType) {
		return 0;
	}
}
