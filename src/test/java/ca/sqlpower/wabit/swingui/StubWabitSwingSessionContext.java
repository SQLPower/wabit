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

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JSpinner;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.SwingWorkerRegistry;
import ca.sqlpower.wabit.StubWabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;

public class StubWabitSwingSessionContext extends StubWabitSessionContext implements WabitSwingSessionContext {

    @Override
    public WabitSwingSession createSession() {
        return new StubWabitSwingSession();
    }
    
    @Override
    public WabitSwingSession createServerSession(WabitServerInfo serverInfo) {
        return null;
    }
    
    public RecentMenu createRecentMenu() {
        // TODO Auto-generated method stub
        return null;
    }

    public JMenu createServerListMenu(Component dialogOwner, String name,
            ServerListMenuItemFactory itemFactory) {
        // TODO Auto-generated method stub
        return null;
    }

    public WabitSwingSession getActiveSwingSession() {
        // TODO Auto-generated method stub
        return null;
    }

    public JFrame getFrame() {
        // TODO Auto-generated method stub
        return null;
    }

    public SwingWorkerRegistry getLoadingRegistry() {
        // TODO Auto-generated method stub
        return null;
    }

    public JSpinner getRowLimitSpinner() {
        // TODO Auto-generated method stub
        return null;
    }

    public void putRecentFileName(String fileName) {
        // TODO Auto-generated method stub
        
    }

    public boolean setEditorPanel() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setStatusMessage(String msg) {
        // TODO Auto-generated method stub
        
    }
}
