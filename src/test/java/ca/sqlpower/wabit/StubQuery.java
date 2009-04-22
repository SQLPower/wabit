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

import java.sql.ResultSet;
import java.sql.SQLException;

import ca.sqlpower.sql.SPDataSource;

public class StubQuery extends StubWabitObject implements Query {

    public ResultSet execute() throws QueryException {
        return null;
    }

	public void setDataSource(SPDataSource ds) {
		// TODO Auto-generated method stub
		
	}

	public void setName(String string) {
		// TODO Auto-generated method stub
		
	}

	public String generateQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isScriptModified() {
		// TODO Auto-generated method stub
		return false;
	}

	public WabitDataSource getWabitDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	public ResultSet fetchResultSet() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public WabitSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRowLimit(int newLimit) {
		// TODO Auto-generated method stub
		
	}

	public void setSession(WabitSession session) {
		// TODO Auto-generated method stub
		
	}

	public int getStreamingRowLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setStreamingRowLimit(int streamingRowLimit) {
		// TODO Auto-generated method stub
		
	}

	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	public void stopRunning() {
		// TODO Auto-generated method stub
		
	}

}
