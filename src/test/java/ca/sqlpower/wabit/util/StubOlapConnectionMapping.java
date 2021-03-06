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

package ca.sqlpower.wabit.util;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.olap4j.OlapConnection;
import org.olap4j.PreparedOlapStatement;

import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.sql.Olap4jDataSource;
import ca.sqlpower.wabit.OlapConnectionProvider;

public class StubOlapConnectionMapping implements OlapConnectionProvider {

    public OlapConnection createConnection(Olap4jDataSource dataSource)
            throws SQLException, ClassNotFoundException, NamingException {
        return null;
    }

    public PreparedOlapStatement createPreparedStatement(
    		Olap4jDataSource dataSource, String mdx, SPVariableHelper helper) 
    {
    	try {
    		OlapConnection conn = createConnection(dataSource);
			return helper.substituteForDb(conn, mdx);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
    }
}
