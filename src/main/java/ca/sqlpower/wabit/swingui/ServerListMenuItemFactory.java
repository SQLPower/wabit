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

import java.awt.Component;

import javax.jmdns.ServiceInfo;
import javax.swing.JMenuItem;

/**
 * A factory interface used by the {@link ServerListMenu}.
 */
public interface ServerListMenuItemFactory {

    /**
     * Creates a JMenuItem or JMenu which is the entry corresponding to the
     * given service info object.
     * 
     * @param serviceInfo
     *            The server this menu or menu item is for
     * @param dialogOwner
     *            The component that should own any dialogs generated when the
     *            menu item's action is invoked.
     * @return A new menu or menu item
     */
    JMenuItem createMenuEntry(ServiceInfo serviceInfo, Component dialogOwner);
}
