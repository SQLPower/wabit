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

package ca.sqlpower.wabit.swingui;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Static collection of icons we use in the Wabit's GUI.
 */
public class Icons {

    public static final Icon LEFT_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_left.png"));
    public static final Icon RIGHT_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_right.png"));
    public static final Icon CENTRE_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_center.png"));

    public static final Icon TOP_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_top.png"));
    public static final Icon MIDDLE_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_middle.png"));
    public static final Icon BOTTOM_ALIGN_ICON = new ImageIcon(Icons.class.getResource("/icons/text_align_bottom.png"));

}
