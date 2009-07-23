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

package ca.sqlpower.wabit.olap;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;

public class DataSourceAdapter implements DataSource {

    private final SQLDatabase wrapMe;
    private PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(System.out));

    public DataSourceAdapter(SQLDatabase wrapMe) {
        this.wrapMe = wrapMe;
        
    }
    public Connection getConnection() throws SQLException {
        try {
            return wrapMe.getConnection();
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        try {
            return wrapMe.getConnection();
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        logWriter = out;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Login timeouts not implemented");
    }

    public boolean isWrapperFor(Class<?> iface) {
        return iface == SPDataSource.class;
    }
    
    public <T> T unwrap(Class<T> iface) {
        if (iface == SPDataSource.class) {
            return iface.cast(wrapMe);
        } else {
            return null;
        }
    }
}
