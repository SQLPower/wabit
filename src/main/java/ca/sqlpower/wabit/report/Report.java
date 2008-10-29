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

package ca.sqlpower.wabit.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.NoRowidException;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.WebResultFormatter;
import ca.sqlpower.sql.WebResultHTMLFormatter;
import ca.sqlpower.sql.WebResultSet;
import ca.sqlpower.wabit.report.Page.StandardPageSizes;

/**
 * Represents a report in the Wabit. A report is a combination of one or more
 * queries against a particular database, an output specifier that determines
 * file format, page layout and other formatting attributes, and an output destination.
 */
public class Report implements Runnable, Callable<Void> {

    private static final Logger logger = Logger.getLogger(Report.class);
    
    /**
     * The source of the report's data and column structure information.
     */
    private WebResultSet reportData;
    
    /**
     * The formatter for this report.
     */
    private WebResultFormatter formatter; // TODO extract an interface that we can (ab)use
    
    /**
     * The page size and margin info.
     */
    private Page page = new Page(StandardPageSizes.US_LETTER); // TODO this should probably belong to the formatter
    
    /**
     * The place the formatted report should be written.
     */
    private File targetFile; // TODO create a ReportDestination interface instead of using a file
    
    /**
     * A wrapper for {@link #call()} that achieves two purposes: firstly, it allows Report
     * to implement Runnable; secondly it conveniently wraps any checked exceptions
     * declared by call() in a RuntimeException when/if they are thrown. 
     */
    public void run() {
        try {
            call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates the report.
     */
    public Void call() throws SQLException, IOException, NoRowidException {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)));
            formatter.formatToStream(reportData, out);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
        return null;
    }
    
    public WebResultSet getReportData() {
        return reportData;
    }
    
    public void setReportData(WebResultSet reportData) {
        this.reportData = reportData;
    }
    
    public WebResultFormatter getFormatter() {
        return formatter;
    }


    public void setFormatter(WebResultFormatter formatter) {
        this.formatter = formatter;
    }


    public File getTargetFile() {
        return targetFile;
    }


    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public Page getPage() {
        return page;
    }
    
    public static void main(String[] args) throws Exception {
        PlDotIni dataSources = new PlDotIni();
        dataSources.read(new File(System.getProperty("user.home"), "pl.ini"));
        SPDataSource ds = dataSources.getDataSource("Local PostgreSQL fuerth");
        File targetFile = new File("report_test_"+System.currentTimeMillis()+".html");

        String sqlQuery = "select * from activity";
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = ds.createConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(sqlQuery);
            WebResultSet wrs = new WebResultSet(rs, sqlQuery);
            
            Report r = new Report();
            r.setReportData(wrs);
            r.setFormatter(new WebResultHTMLFormatter());
            r.setTargetFile(targetFile);
            r.call();

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
        
        // preview on OS X: shows the output using the "open" command, which chooses
        // the application the same way Finder does when you double click
        if (System.getProperty("mrj.version") != null) {
            Runtime.getRuntime().exec("open " + targetFile);
        } else {
            System.out.println("Output is in file " + targetFile);
        }
    }
}
