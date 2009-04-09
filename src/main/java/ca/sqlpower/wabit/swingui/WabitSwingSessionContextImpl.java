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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.AboutAction;
import ca.sqlpower.wabit.swingui.action.LoadProjectsAction;

/**
 * This is the swing version of the WabitSessionContext. Swing specific operations for
 * the context will be done in this implementation 
 */
public class WabitSwingSessionContextImpl extends WabitSessionContextImpl implements WabitSwingSessionContext {

    private static final Logger logger = Logger.getLogger(WabitSwingSessionContextImpl.class);
    
	private WabitWelcomeScreen welcomeScreen;

	/**
	 * @param terminateWhenLastSessionCloses
	 *            Set to true if the context should stop the app when the last
	 *            session is closed. If false the app will have to be closed in
	 *            a way other than closing all of the sessions.
	 * @param headless
	 *            Set to true to not create any GUI objects when the context
	 *            starts. This stops the welcome screen from being created.
	 */
	public WabitSwingSessionContextImpl(boolean terminateWhenLastSessionCloses, boolean headless)
			throws IOException, SQLObjectException {
		super(terminateWhenLastSessionCloses);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		if (!headless) {
			welcomeScreen = new WabitWelcomeScreen(this);
			macOSXRegistration();
		}
	}
	
	@Override
	public WabitSession createSession() {
		WabitSwingSession session = new WabitSwingSessionImpl(this);
		return session;
	}
	
	public RecentMenu createRecentMenu() {
		return new RecentMenu(this.getClass()) {
			
			@Override
			public void loadFile(String fileName) throws IOException {
				File file = new File(fileName);
				LoadProjectsAction.loadFile(file, WabitSwingSessionContextImpl.this);
			}
		};
	}

	public JMenu createServerListMenu(Component dialogOwner) {
	    return new ServerListMenu(this, dialogOwner);
	}
	
	public WabitWelcomeScreen getWelcomeScreen() {
		return welcomeScreen;
	}
	
	@Override
	public void deregisterChildSession(WabitSession child) {
		super.deregisterChildSession(child);
		if (getSessionCount() == 0) {
			welcomeScreen.showFrame();
			prefs.putBoolean(PREFS_START_ON_WELCOME_SCREEN, true);
		}
	}
	
	/**
     * Registers this application in Mac OS X if we're running on that platform.
     *
     * <p>This code came from Apple's "OS X Java Adapter" example.
     */
    private void macOSXRegistration() {

        Action prefAction = new AbstractAction() {
		
			public void actionPerformed(ActionEvent e) {
				// TODO Implement prefs in Mac
			}
		};
		
		Action aboutAction = new AboutAction(welcomeScreen.getFrame());
		Action exitAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		};

        // Whether or not this is OS X, the three actions we're referencing must have been initialized by now.
        if (exitAction == null) throw new IllegalStateException("Exit action has not been initialized"); //$NON-NLS-1$
        if (prefAction == null) throw new IllegalStateException("Prefs action has not been initialized"); //$NON-NLS-1$
        if (aboutAction == null) throw new IllegalStateException("About action has not been initialized"); //$NON-NLS-1$

        if (isMacOSX()) {
            try {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("ca.sqlpower.architect.swingui.OSXAdapter"); //$NON-NLS-1$

                // The main registration method.  Takes quitAction, prefsAction, aboutAction.
                Class[] defArgs = { Action.class, Action.class, Action.class };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs); //$NON-NLS-1$
                Object[] args = { exitAction, prefAction, aboutAction };
                registerMethod.invoke(osxAdapter, args);

                // The enable prefs method.  Takes a boolean.
                defArgs = new Class[] { boolean.class };
                Method prefsEnableMethod =  osxAdapter.getDeclaredMethod("enablePrefs", defArgs); //$NON-NLS-1$
                args = new Object[] {Boolean.TRUE};
                prefsEnableMethod.invoke(osxAdapter, args);
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
                // above NoClassDefFoundError first.
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

	public void putRecentFileName(String fileName) {
		createRecentMenu().putRecentFileName(fileName);
		prefs.putBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
	}

	public boolean startOnWelcomeScreen() {
		return prefs.getBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
	}

}
