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

package ca.sqlpower.wabit.swingui.olap;

import java.sql.SQLException;

import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.olap.QueryInitializationException;
import ca.sqlpower.wabit.swingui.WabitSwingSession;

/**
 * Static utility methods for working with the olap4j aspect of the Wabit's GUI.
 */
public class OlapGuiUtil {

    private OlapGuiUtil() {
        throw new UnsupportedOperationException("Class not instantiable");
    }

    /**
     * Runs the given query in an SPSwingWorker that registers with the given
     * session. The worker's "responsible object" is set to the given query, so
     * its tree node should get a "throbber" on it while the query is running.
     * 
     * @param query
     * @param session
     * @return
     */
    public static SPSwingWorker asyncExecute(final OlapQuery query, final WabitSwingSession session) {
        SPSwingWorker worker = new SPSwingWorker(session, query) {

            @Override
            public void doStuff() throws QueryInitializationException, InterruptedException, SQLException {
                query.executeOlapQuery();
            }
            
            @Override
            public void cleanup() throws Exception {
                if (getDoStuffException() != null) {
                    SPSUtils.showExceptionDialogNoReport(
                            session.getTree(), "OLAP query " + query.getName() + " failed",
                            getDoStuffException());
                }
            }
            
        };
        new Thread(worker).start();
        return worker;
    }
}
