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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.QueryException;

/**
 * Renders a JDBC result set using configurable absolute column widths.
 */
public class ResultSetRenderer implements ReportContentRenderer {

    private static final Logger logger = Logger.getLogger(ResultSetRenderer.class);
    
    /**
     * Default column width.
     */
    private static final int DEFAULT_COL_WIDTH = 72;
    
    /**
     * Column widths in Graphics2D units (screen pixels or 1/72 of an inch when printed).
     */
    private final List<Integer> columnWidths = new ArrayList<Integer>();
    
    /**
     * The string that will be rendered for null values in the result set.
     */
    private String nullString = "(null)";
    
    private final ResultSet rs;

    /**
     * If the query fails to execute, the corresponding exception will be saved here and
     * rendered instead of the results.
     */
    private Exception executeException;

    /**
     * Creates a new renderer that gets its results from the given web result set.
     * 
     * @param rs The result set to render. Must be scrollable (that is, calling beforeFirst()
     * must reposition the current row cursor just above the first row).
     */
    public ResultSetRenderer(ResultSet rs) throws SQLException {
        this.rs = rs;
        ResultSetMetaData rsmd = rs.getMetaData();
        initColumns(rsmd);
    }

    public ResultSetRenderer(Query query) {
        ResultSet executedRs = null;
        try {
            executedRs = query.execute(); // TODO run in background
            initColumns(executedRs.getMetaData());
        } catch (Exception ex) {
            executeException = ex;
        }
        rs = executedRs;
    }

    /**
     * Constructor subroutine.
     * 
     * @param rsmd The metadata for the current result set.
     * @throws SQLException If the resultset metadata methods fail.
     */
    private void initColumns(ResultSetMetaData rsmd) throws SQLException {
        for (int col = 1; col <= rsmd.getColumnCount(); col++) {
            columnWidths.add(DEFAULT_COL_WIDTH);
        }
    }
    

    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor) {
        if (executeException != null) {
            return renderFailure(g, contentBox, scaleFactor);
        } else {
            return renderSuccess(g, contentBox, scaleFactor);
        }
    }
    
    
    public boolean renderFailure(Graphics2D g, ContentBox contentBox, double scaleFactor) {
        List<String> errorMessage = new ArrayList<String>();
        if (executeException instanceof QueryException) {
            QueryException qe = (QueryException) executeException;
            Throwable cause = qe.getCause();
            errorMessage.add("Query failed: " + cause);
            errorMessage.addAll(Arrays.asList(qe.getQuery().split("\n")));
        } else {
            errorMessage.add("Query failed: " + executeException);
            Throwable cause = executeException.getCause();
            while (cause != null) {
                errorMessage.add("Caused by: " + cause);
            }
        }
        FontMetrics fm = g.getFontMetrics();
        int width = contentBox.getWidth();
        int height = contentBox.getHeight();
        int textHeight = fm.getHeight() * errorMessage.size();
        
        int y = Math.max(0, height/2 - textHeight/2);
        for (String text : errorMessage) {
            y += fm.getHeight();
            int textWidth = fm.stringWidth(text);
            g.drawString(text, width/2 - textWidth/2, y);
        }
        
        return false;
    }

    public boolean renderSuccess(Graphics2D g, ContentBox contentBox, double scaleFactor) {
        try {
            rs.beforeFirst(); // XXX temporary--must define API for resetting renderers
            ResultSetMetaData rsmd = rs.getMetaData();
            FontMetrics fm = g.getFontMetrics();
            int x = 0;
            int y = fm.getAscent();
            int colCount = rsmd.getColumnCount();
            for (int col = 1; col <= colCount; col++) {
                g.drawString(replaceNull(rsmd.getColumnLabel(col)), x, y);
                x += columnWidths.get(col-1);
            }
            while ( rs.next() && ((y + fm.getHeight()) < contentBox.getHeight()) ) {
                x = 0;
                y += fm.getHeight();
                
                
                for (int col = 1; col <= colCount; col++) {
                    // TODO respect alignment
                    g.drawString(replaceNull(rs.getString(col)), x, y);
                    x += columnWidths.get(col-1);
                }
            }
            return !rs.isAfterLast();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String replaceNull(String string) {
        if (string == null) {
            return nullString;
        } else {
            return string;
        }
    }
    
    
}
