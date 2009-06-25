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
import java.util.List;
import java.util.prefs.Preferences;

import javax.jmdns.JmDNS;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.ModalDialogUserPrompter;
import ca.sqlpower.swingui.RecentMenu;
import ca.sqlpower.swingui.action.ForumAction;
import ca.sqlpower.util.UserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.WabitSession;
import ca.sqlpower.wabit.WabitSessionContext;
import ca.sqlpower.wabit.enterprise.client.WabitServerInfo;
import ca.sqlpower.wabit.swingui.action.AboutAction;
import ca.sqlpower.wabit.swingui.action.OpenWorkspaceAction;

/**
 * This is the swing version of the WabitSessionContext. Swing specific operations for
 * the context will be done in this implementation 
 */
public class WabitSwingSessionContextImpl implements WabitSwingSessionContext {

    private static final Logger logger = Logger.getLogger(WabitSwingSessionContextImpl.class);

    /**
     * The core session context that this swing session context delegates its
     * "core" operations to.
     */
    private final WabitSessionContext delegateContext;
    
    /**
     * This is a preference that stores if the app should start up on the welcome screen
     * or it should start on the last loaded/saved workspace. 
     */
    private static final String PREFS_START_ON_WELCOME_SCREEN = "START_ON_WELCOME_SCREEN";
    
    public static final ForumAction FORUM_ACTION = new ForumAction(new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/wabit-24px.png")), "Go to Wabit support forum");
    
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
	public WabitSwingSessionContextImpl(WabitSessionContext delegateContext, boolean headless)
			throws IOException, SQLObjectException {
		this.delegateContext = delegateContext;
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		if (!headless) {
			welcomeScreen = new WabitWelcomeScreen(this);
			macOSXRegistration();
		}
	}
	
	public WabitSession createSession() {
		WabitSwingSession session = new WabitSwingSessionImpl(this);
		return session;
	}
	
	public RecentMenu createRecentMenu() {
		RecentMenu menu = new RecentMenu(this.getClass()) {
			
			@Override
			public void loadFile(String fileName) throws IOException {
				File file = new File(fileName);
				OpenWorkspaceAction.loadFile(file, WabitSwingSessionContextImpl.this);
			}
		};
		
		menu.setText("Open Recent Workspace");
		
		return menu;
	}
//
//	public JMenu createServerListMenu(Component dialogOwner) {
//	    return new ServerListMenu(this, "Open On Server", dialogOwner);
//	}
	
	public WabitWelcomeScreen getWelcomeScreen() {
		return welcomeScreen;
	}
	
	public void deregisterChildSession(WabitSession child) {
	    delegateContext.deregisterChildSession(child);
		if (getSessionCount() == 0) {
			welcomeScreen.showFrame();
			getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, true);
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
		getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
	}

	public boolean startOnWelcomeScreen() {
		return getPrefs().getBoolean(PREFS_START_ON_WELCOME_SCREEN, false);
	}

    public void close() {
        getPrefs().putBoolean(PREFS_START_ON_WELCOME_SCREEN, getSessionCount() == 0);
        delegateContext.close();
    }

    public DataSourceCollection getDataSources() {
        return delegateContext.getDataSources();
    }

    public List<WabitServerInfo> getEnterpriseServers(boolean includeDiscoveredServers) {
        return delegateContext.getEnterpriseServers(includeDiscoveredServers);
    }

    public void addServer(WabitServerInfo serverInfo) {
        delegateContext.addServer(serverInfo);
    }

    public void removeServer(WabitServerInfo si) {
        delegateContext.removeServer(si);
    }

    public JmDNS getJmDNS() {
        return delegateContext.getJmDNS();
    }

    public int getSessionCount() {
        return delegateContext.getSessionCount();
    }

    public boolean isMacOSX() {
        return delegateContext.isMacOSX();
    }

    public void registerChildSession(WabitSession child) {
        delegateContext.registerChildSession(child);
    }

    public Preferences getPrefs() {
        return delegateContext.getPrefs();
    }

    public JMenu createServerListMenu(Component dialogOwner, String name,
            ServerListMenuItemFactory itemFactory) {
        return new ServerListMenu(this, name, dialogOwner, itemFactory);
    }

	public String getName() {
		return delegateContext.getName();
	}

    public UserPrompter createUserPrompter(String question,
            UserPromptType responseType, UserPromptOptions optionType,
            UserPromptResponse defaultResponseType, Object defaultResponse,
            String... buttonNames) {
        return new ModalDialogUserPrompter(optionType, defaultResponseType, null, question, buttonNames);
    }

	
}
