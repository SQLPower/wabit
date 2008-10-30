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

package ca.sqlpower.wabit.swingui.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.WebResultHTMLFormatter;
import ca.sqlpower.sql.WebResultSet;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;

public class AddContentBoxAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(AddContentBoxAction.class);
    
    private final Report report;
    private final PageNode addTo;

    public AddContentBoxAction(Report report, PageNode addTo) {
        super("Add Content Box");
        this.report = report;
        this.addTo = addTo;
    }
    
    public void actionPerformed(ActionEvent e) {
        ContentBoxNode newCBNode = new ContentBoxNode(new ContentBox());
        newCBNode.setBounds(addTo.getWidth() / 2, addTo.getHeight() / 2, 30, 30); // XXX should be near mouse pointer
        addTo.addChild(newCBNode);
        
        // XXX temporary from here to end of method
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            PlDotIni dataSources = new PlDotIni();
            dataSources.read(new File(System.getProperty("user.home"), "pl.ini"));
            SPDataSource ds = dataSources.getDataSource("Local PostgreSQL fuerth");
            
            String sqlQuery = "select * from activity";
            
            con = ds.createConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(sqlQuery);

            CachedRowSet crs = new CachedRowSet();
            crs.populate(rs);
            ResultSetRenderer rsr = new ResultSetRenderer(crs);
            newCBNode.getContentBox().setContentRenderer(rsr);
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                logger.warn("Failed to close result set. Squishing this exception: ", ex);
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {
                logger.warn("Failed to close statement. Squishing this exception: ", ex);
            }
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                logger.warn("Failed to close database connection. Squishing this exception: ", ex);
            }
        }

    }
}
