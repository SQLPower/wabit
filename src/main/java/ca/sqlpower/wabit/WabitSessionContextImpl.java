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

package ca.sqlpower.wabit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;

/**
 * A placeholder for all state and behaviour that is shared among
 * Wabit sessions. Every session belongs to a session context, and
 * there is typically one session context in each JVM. However,
 * the limit of one session context is not enforced or required. It's
 * just typical.
 */
public class WabitSessionContextImpl implements WabitSessionContext {
	
	private static final Logger logger = Logger.getLogger(WabitSessionContextImpl.class);

	/**
	 * This is a preference that stores the location of the pl.ini.
	 */
	private static final String PREFS_PL_INI_PATH = "PL_INI_PATH";
	
	private DataSourceCollection dataSources;
	private final List<WabitSession> childSessions = new ArrayList<WabitSession>();
	
	/**
	 * If this flag is true, this session context will halt the VM when its
	 * last session closes.
	 */
	private boolean terminateWhenLastSessionCloses;
	
	/**
	 *  Stores true when the OS is MAC
	 */
    private static final boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    
    /**
     * This is the path to the user's pl.ini file.
     */
    private String plDotIniPath;
    
    /**
     * This prefs node stores context specific prefs. At current this is the pl.ini location.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(WabitSessionContextImpl.class);
	
	/**
	 * Creates a new Wabit session context.
	 * 
	 * @param terminateWhenLastSessionCloses
	 *            If this flag is true, this session context will halt the VM
	 *            when its last session closes.
	 * @throws IOException
	 *             If the startup configuration files can't be read
	 * @throws SQLObjectException If the pl.ini is invalid.
	 */
	public WabitSessionContextImpl(boolean terminateWhenLastSessionCloses) throws IOException, SQLObjectException {
		this.terminateWhenLastSessionCloses = terminateWhenLastSessionCloses;
		
        setPlDotIniPath(prefs.get(PREFS_PL_INI_PATH, null));
        logger.debug("pl.ini is at " + getPlDotIniPath());
        
        setPlDotIniPath(ArchitectUtils.checkForValidPlDotIni(getPlDotIniPath(), "Wabit"));
        
		getDataSources();
	}

	/**
	 * Adds the given Wabit session to the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void registerChildSession(WabitSession child) {
		childSessions.add(child);
	}
	
	/**
     * Tries to read the plDotIni if it hasn't been done already.  If it can't be read,
     * returns null and leaves the plDotIni property as null as well. See {@link #plDotIni}.
     */
    public DataSourceCollection getDataSources() {
        String path = getPlDotIniPath();
        if (path == null) return null;
        
        if (dataSources == null) {
        	dataSources = new PlDotIni();
        	String iniToLoad = "ca/sqlpower/sql/default_database_types.ini";
            try {
                logger.debug("Reading PL.INI defaults");
                dataSources.read(getClass().getClassLoader().getResourceAsStream(iniToLoad));
                iniToLoad = "/ca/sqlpower/demodata/example_database.ini";
                dataSources.read(WabitSessionContextImpl.class.getResourceAsStream(iniToLoad));
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read system resource " + iniToLoad,e));
            }
            try {
                if (dataSources != null) {
                    logger.debug("Reading new PL.INI instance");
                    dataSources.read(new File(path));
                }
            } catch (IOException e) {
                throw new SQLObjectRuntimeException(new SQLObjectException("Failed to read pl.ini at \""+getPlDotIniPath()+"\"", e));
            }
        }
        return dataSources;
    }

	/**
	 * Removes the given Wabit session from the list of child sessions for this
	 * context. This is normally done by the sessions themselves, so you
	 * shouldn't need to call this method from your own code.
	 */
	public void deregisterChildSession(WabitSession child) {
		childSessions.remove(child);
		
		logger.debug("Deregistered a child session " + childSessions.size() + " sessions still remain.");
		if (childSessions.isEmpty()) {
			logger.debug("Saving pl.ini");
	        prefs.put(PREFS_PL_INI_PATH, getPlDotIniPath());
			try {
	            dataSources.write(new File(getPlDotIniPath()));
	        } catch (IOException e) {
	            logger.error("Couldn't save PL.INI file!", e); //$NON-NLS-1$
	        }
		}
		
		if (terminateWhenLastSessionCloses && childSessions.isEmpty()) {
			System.exit(0);
		}
	}
	/**
	 * returns true if the OS is Mac
	 * @return
	 */
	public boolean isMacOSX() {
		return MAC_OS_X ; 
	}

	/**
	 * This does not create a session as there is no current core session implementation.
	 */
	public WabitSession createSession() {
		throw new UnsupportedOperationException("There is no session defined for the core objects.");
	}

	public void setPlDotIniPath(String plDotIniPath) {
		this.plDotIniPath = plDotIniPath;
	}

	public String getPlDotIniPath() {
		return plDotIniPath;
	}
	
	public int getSessionCount() {
		return childSessions.size();
	}

	public void close() {
		for (int i = childSessions.size() - 1; i >= 0; i--) {
			if (!childSessions.get(i).close()) {
				return;
			}
		}
		System.exit(0);
	}
	
}