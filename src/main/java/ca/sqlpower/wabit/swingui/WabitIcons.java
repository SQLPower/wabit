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

package ca.sqlpower.wabit.swingui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A utility class acting as a single place to store commonly-used icons in
 * Wabit
 */
public class WabitIcons {
	
	public static final Icon REPORT_ICON_16 = makeIcon("dashboard-16");
	public static final Icon REPORTTASK_ICON_16 = makeIcon("scheduled-report-16");
	public static final Icon TEMPLATE_ICON_16 = makeIcon("template-16");
	public static final Icon WABIT_FILE_ICON_16 = makeIcon("wabitFile-16"); 
	public static final Icon EXPORT_ICON_32 = makeIcon("32x32/export");
	public static final Icon RUN_ICON_32 = makeIcon("32x32/run");
	public static final Icon ZOOM_OUT_ICON_16 = makeIcon("zoomMinus-16");
	public static final Icon ZOOM_IN_ICON_16 = makeIcon("zoomPlus-16");
	public static final Icon SAVE_ICON_16 = makeIcon("save-16");
	public static final Icon CANCEL_ICON_32 = makeIcon("32x32/cancel");
	public static final Icon REFRESH_ICON_32 = makeIcon("32x32/refresh");
	public static final Icon USER_ICON_16 = makeIcon("user-16");
	public static final Icon SECURITY_ICON_16 = makeIcon("badge");
	public static final Icon SECURITY_ICON_32 = makeIcon("32x32/badge-32");
	public static final Icon GROUP_ICON_16 = makeIcon("group-16");
	public static final Icon DELETE_ICON_32 = makeIcon("32x32/cancel");
	public static final Icon DELETE_ICON_16 = makeIcon("delete");
	public static final Icon STOP_16 = makeIcon("stop");
	public static final Icon SERVER_ICON_32 = makeIcon("32x32/wabitServer-32");
	public static final Icon CLOSE_WORKSPACE = makeIcon("closeWorkspace-12");
	public static final Icon EDIT = makeIcon("edit-12");
	public static final Icon QUERY_32 = makeIcon("32x32/query");
	public static final Icon COPY_16 = makeIcon("page_white_stack");
	public static final Icon CONNECTION_16 = makeIcon("connection-16");
	public static final Icon CONNECTION_STREAM_16 = makeIcon("connection-stream-16");
	public static final Icon RENAME_16 = makeIcon("label-16");
	
	public static final Icon ADD_12 = makeIcon("add-12");
	public static final Icon DELETE_12 = makeIcon("delete-12");
	public static final Icon EDIT_12 = makeIcon("edit-12");
	
	public static final Icon PARAMETERS_12 = makeIcon("parameters-12");
	public static final Icon PARAMETERS_NEW_32 = makeIcon("parameters-new-32");
	public static final Icon PARAMETERS_COMBO_16 = makeIcon("parameters-combo-16");
	public static final Icon PARAMETERS_TEXT_16 = makeIcon("parameters-text-16");
	
	// FIXME need proper icon
	public static final Icon PARAMETERS_DATE_16 = makeIcon("parameters-text-16");
	 
	
    private static final ImageIcon makeIcon(String resourceName) {
        URL iconUrl = WabitIcons.class.getClassLoader().getResource(
                "icons/" + resourceName + ".png");
        if (iconUrl == null) {
            throw new RuntimeException("Missing icon for " + resourceName);
        } else {
            return new ImageIcon(iconUrl);
        }
    }
}
