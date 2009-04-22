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

import java.io.IOException;
import java.util.Properties;

import ca.sqlpower.util.Version;
import ca.sqlpower.util.VersionFormatException;

public class WabitVersion {

    public static final Version VERSION;
    
    static {
        String versionStr = null;
        try {
            Properties wabitProps = new Properties();
            wabitProps.load(WabitVersion.class.getResourceAsStream("wabit.properties"));
            versionStr = wabitProps.getProperty("app.version");
            VERSION = new Version(versionStr);
        } catch (VersionFormatException ex) {
            throw new AssertionError("Version properties resource does not contain a valid version number!");
        } catch (IOException e) {
            throw new AssertionError("Version properties resource is missing!");
        }
    }
    
    private WabitVersion() {
        // no instances!
    }
}
