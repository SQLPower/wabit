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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;

import ca.sqlpower.query.Item;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sql.CachedRowSet.RowComparator;
import ca.sqlpower.wabit.AbstractWabitListener;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.CleanupExceptions;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.QueryException;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.resultset.ReportPositionRenderer;
import ca.sqlpower.wabit.report.resultset.ResultSetCell;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducerEvent;

/**
 * Renders a JDBC result set using configurable absolute column widths.
 */
public class ResultSetRenderer extends AbstractWabitObject implements WabitObjectReportRenderer {
    
    private static final Color DRAGGABLE_COL_LINE_COLOUR = new Color(0xcccccc);
    
    private static final Logger logger = Logger.getLogger(ResultSetRenderer.class);
    
	private static final int COLUMN_WIDTH_BUFFER = 5;

    /**
     * This is the number of pixels each cell will be indented by if there is a
     * border to the left of the cell. If no indent is given then the left part
     * of the text will be overwritten by the border itself.
     */
	public static final int BORDER_INDENT = 2;

	/**
	 * Notes a change to the query has occurred that would require a refresh to the renderer
	 */
	protected static final String QUERY = "query";
    
    private static DataType getDataType(ResultSetMetaData rsmd, int columnIndex) throws SQLException {
    	String className = rsmd.getColumnClassName(columnIndex);
    	if (className != null && className.length() > 0) {
    		try {
    			return getDataType(Class.forName(className));
    		} catch (ClassNotFoundException e) {
    			throw new RuntimeException("Invalid class name " + className + ", cannot find class",e);
    		}
    	} else {
    		// some drivers cheat and don't fill in the class names, so this is the backup
    		int jdbcType = rsmd.getColumnType(columnIndex);
    		return getDataTypeForJdbcType(jdbcType);
    	}
    	
    }
    
    private static DataType getDataTypeForJdbcType(int jdbcType) {
    	if (SQL.isNumeric(jdbcType)) {
    		return DataType.NUMERIC;
    	} else if (SQL.isDate(jdbcType)) {
    		return DataType.DATE;
    	}
    	return DataType.TEXT;
    }
    
    private static DataType getDataType(Class<?> clazz) {
    	logger.debug("trying to compare class Name:"+ clazz.toString());
    	
    	
    	if(clazz.isAssignableFrom(String.class)) {
    		logger.debug("class Name identified as a TEXT");
    		return(DataType.TEXT);
    	} else if(clazz.isAssignableFrom(Number.class)) {
    		logger.debug("class Name identified as a NUMERIC"+ Number.class);
    		return(DataType.NUMERIC);
    	} else if(clazz.isAssignableFrom(Integer.class)) {
    		logger.debug("class Name identified as a NUMERIC");
    		return(DataType.NUMERIC);
    	} else if(clazz.isAssignableFrom(Timestamp.class)) {
    		logger.debug("class Name identified as a DATE");
    		return(DataType.DATE);
    	} else if(clazz.isAssignableFrom(Date.class)) {
    		logger.debug("class Name identified as a DATE");
    		return(DataType.DATE);
    	} else if(clazz.isAssignableFrom(BigDecimal.class)) {
    		logger.debug("class Name identified as a NUMERIC");
    		return(DataType.NUMERIC);
    	} else {
    		logger.debug("failed on the class"+ clazz.toString());
    	}
    	return DataType.TEXT;
    	
    }
    
    /**
     * These border styles gives a border to the result set. The border
     * style is set on the renderer and is applied to each cell.
     */
    public enum BorderStyles {
    	NONE,
    	OUTSIDE,
    	INSIDE,
    	FULL,
    	VERTICAL,
    	HORIZONTAL
    }

    /**
     * This list contains an in-order list of positions per page. Each entry in
     * the outer list represents one page. The values in the inner list
     * represents the {@link ResultSetCell}s in each page. This will be null if
     * a change has occurred and requires the positions to be recreated from the
     * {@link ReportPositionRenderer#createResultSetLayout(Graphics2D, ResultSet, List, ContentBox, boolean)}
     * method. This is wrapped by a ThreadLocal to give each printing thread and
     * the UI thread different copies of the page positions. This is required as
     * the printing will render a result set without a limit while the screen
     * does have a limit to it.
     */
    private final ThreadLocal<List<List<ResultSetCell>>> pageCells = new ThreadLocal<List<List<ResultSetCell>>>();
    
    /**
     * This decides if the grand totals will be printed at the end of a result
     * set.
     */
    private boolean printingGrandTotals = false;
    
    /**
     * This enum is used to describe the types of rows that are able to
     * be printed. Some row types may not be printable due to font sizes
     * and the remaining amount of space to print in. 
     * <p>
     * Package private for testing.
     */
    enum PrintableRowTypes {
        NONE,
        HEADER,
        BODY,
        BOTH
    }
    
    private BorderStyles borderType = BorderStyles.NONE;
    
    private final List<ColumnInfo> columnInfo;
    
    /**
     * The string that will be rendered for null values in the result set.
     */
    private String nullString = "(null)";
    
    private Font headerFont;
    
    private Font bodyFont;
    
    /**
     * The query that provides the content data for this renderer.
     * <p>
     * TODO: change this to ResultSetProducer
     */
    private final QueryCache query;
    
    /**
     * A cached copy of the result set that came from the Query object.
     * This will be null when the result set is not being painted. Just
     * before the result set is to be rendered to the screen the 
     * executeQuery method should be called to populate this result set
     * and this value should be set back to null when rendering the result
     * set is finished.
     * <p>
     * Note: This can be null when painting if the result set returned
     * by the query is null.
     */
    private CachedRowSet paintingRS = null;
    
    /**
     * This result set should only be used when printing. If printing is not being
     * done then this result set should be null. This result set will contain the entire
     * result of the query instead of just a result set limited by a row limit.
     * <p>
     * Note: This can be null when painting if the result set returned
     * by the query is null.
     */
    private CachedRowSet printingResultSet = null;

    /**
     * If the query fails to execute, the corresponding exception will be saved here and
     * rendered instead of the results.
     */
    private Exception executeException;

	/**
	 * This will store the background colour for the result set renderer.
	 */
	private Color backgroundColour;
	
	/**
     * This is the column whose right side is currently being dragged in the editor.
     * If this is null then no column is being dragged.
     */
    private ColumnInfo colBeingDragged = null;
	
	/**
	 * This listens for all property changes on the parent content box to 
	 * know when to recreate the layout of the result set. This also listens
	 * to changes in the result set renderer for changes that the ContentBox
	 * doesn't receive.
	 * <p>
	 * XXX Make the ContentBox aware of changes to the query. The content box
	 * should receive events when the query changes when it is listening to
	 * the children of a result set renderer.
	 */
	private final WabitListener parentChangeListener = new AbstractWabitListener() {
        public void propertyChangeImpl(PropertyChangeEvent evt) {
            clearResultSetLayout();
        }
    };

    /**
     * This change listener is placed on {@link CachedRowSet}s to monitor
     * streaming result sets to know when a new row is added to the result set.
     * It is kept attached to the right row set by {@link #resultSetHandler}.
     */
	private final RowSetChangeListener rowSetChangeListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
		    if (getParent() != null) {
		        getParent().repaint();
		    }
		}
	};
	
	/**
	 * This listener will fire a change event when the query changes to signal that
	 * the result set renderer needs to be repainted.
	 */
    private final WabitListener queryChangeListener = new AbstractWabitListener() {
		public void propertyChangeImpl(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("name")) {
				setName("Result Set: " + query.getName());
			}
			if (getParent() != null) {
			    getParent().repaint();
			}
		}
	};

	// TODO eventually, this will be the thing that triggers a repaint of this
	// renderer. When we make that change, we'll only depend on ResultSetProducer
	// and not all of QueryCache.
	private final class ResultSetHandler implements ResultSetListener {
	    private CachedRowSet currentRowSet = null;
	    
        public void resultSetProduced(ResultSetProducerEvent evt) {
            cleanup();
            currentRowSet = evt.getResults().getFirstNonNullResultSet();
            if (currentRowSet != null) {
                currentRowSet.addRowSetListener(rowSetChangeListener);
            }
        }
        
        /**
         * Removes the listener on the current row set, if there is one.
         */
        public void cleanup() {
            if (currentRowSet != null) {
                currentRowSet.removeRowSetListener(rowSetChangeListener);
            }
        }
	}
	
	private final ResultSetHandler resultSetHandler = new ResultSetHandler();
	
    public ResultSetRenderer(@Nonnull QueryCache query) {
    	this(query, new ArrayList<ColumnInfo>());
    }
    
    public ResultSetRenderer(@Nonnull QueryCache query, @Nonnull List<ColumnInfo> columnInfoList) {
        this.query = query;
        query.addResultSetListener(resultSetHandler);
		query.addWabitListener(queryChangeListener);
        columnInfo = new ArrayList<ColumnInfo>(columnInfoList);
        setName("Result Set: " + query.getName());
	}
    
    /**
     * Copy constructor
     */
    public ResultSetRenderer(ResultSetRenderer resultSetRenderer) {
    	this.query = resultSetRenderer.query;
    	this.backgroundColour = resultSetRenderer.backgroundColour;
    	this.borderType = resultSetRenderer.borderType;
    	this.executeException = resultSetRenderer.executeException;
    	this.headerFont = resultSetRenderer.headerFont;
    	this.bodyFont = resultSetRenderer.bodyFont;
    	this.nullString = resultSetRenderer.nullString;
    	this.paintingRS = resultSetRenderer.paintingRS;
    	this.printingResultSet = resultSetRenderer.printingResultSet;
    	this.printingGrandTotals = resultSetRenderer.printingGrandTotals;
    	
    	this.columnInfo = new ArrayList<ColumnInfo>();
    	for (ColumnInfo column : resultSetRenderer.columnInfo) {
    		ColumnInfo newColumnInfo = new ColumnInfo(column);
    		newColumnInfo.setParent(this);
			columnInfo.add(newColumnInfo);
    	}
    	
    	query.addWabitListener(queryChangeListener);
    	query.addResultSetListener(resultSetHandler);
    	
    	if (resultSetRenderer.resultSetHandler.currentRowSet != null) {
    	    try {
                resultSetHandler.currentRowSet =
                    resultSetRenderer.resultSetHandler.currentRowSet.createShared();
            } catch (SQLException wontHappen) {
                throw new AssertionError(wontHappen);
            }
    	}
    	
    	setName(resultSetRenderer.getName());
    }
    
    public QueryCache getContent(){
    	return query;
    }
    
    @Override
    public CleanupExceptions cleanup() {
        query.removeResultSetListener(resultSetHandler);
    	query.removeWabitListener(queryChangeListener);
    	resultSetHandler.cleanup();
    	return new CleanupExceptions();
    }

	/**
	 * This will execute the query contained in this result set and set the
	 * result set to be a new result set.
	 * <p>
	 * NOTE: This method should not contain any method calls that could fire
	 * events on the result set renderer. Firing a property change event can
	 * cause the content box to update and cause this class to call
	 * {@link #clearResultSetLayout()} causing the result set to be nulled out.
	 * <p>
	 * Package private for testing.
	 */
	void executeQuery() {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting to fetch a new result set for query " + query.generateQuery());
		}
        ResultSet executedRs = null;
        paintingRS = null;
        executeException = null;
		try {
		    //TODO This method should just call query.execute() and the ResultSetHandler
		    //should set the result set in this class to the query's results. The 
		    //render report content method should also give a message notifying the user
		    //that the query is being refreshed.
            executedRs = query.execute().get().getFirstNonNullResultSet();
            if (executedRs == null) {
                return;
            }
            initColumns(executedRs);
            if (executedRs instanceof CachedRowSet) {
                paintingRS = ((CachedRowSet) executedRs).createShared();
            } else {
                paintingRS = new CachedRowSet();
                paintingRS.populate(executedRs);
            }
        } catch (Exception ex) {
            executeException = ex;
        }
        
        if (logger.isDebugEnabled()) {
			logger.debug("Finished fetching results for query " + query.generateQuery());
		}
	}
	
    /**
     * This will execute the query contained in this result set and
     * set the result set to be a new result set.
     */
	private void executeQueryForPrinting() {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting to fetch a new result set for query " + query.generateQuery());
		}
        CachedRowSet executedRs = null;
        executeException = null;
        QueryCache cachedQuery = new QueryCache(query);
		try {
			cachedQuery.executeStatement(true);
			boolean hasNext = true;
			while (hasNext) {
            	if (cachedQuery.getResultSet() != null) {
            		executedRs = cachedQuery.getCachedRowSet();
            		break;
            	}
                boolean sqlResult = cachedQuery.getMoreResults();
                hasNext = !((sqlResult == false) && (cachedQuery.getUpdateCount() == -1));
            }
			if (executedRs == null) {
				executeException = new SQLException("There are no results in the executed query.");
			}
        } catch (Exception ex) {
            executeException = ex;
        }
        printingResultSet = executedRs;
        if (logger.isDebugEnabled()) {
			logger.debug("Finished fetching results for query " + query.generateQuery());
		}
	}
	
	/**
	 * Constructor subroutine.
	 * 
	 * @param rsmd
	 *            The metadata for the current result set.
	 * @param columnInfo
	 *            The list of column information for the result set. This allows
	 *            defining column information from a load.
	 * @throws SQLException
	 *             If the resultset metadata methods fail.
	 */
    private void initColumns(ResultSet rs) throws SQLException {
    	ResultSetMetaData rsmd = rs.getMetaData();
    	Map<Item, ColumnInfo> colKeyToInfoMap = new HashMap<Item, ColumnInfo>();
    	for (ColumnInfo info : columnInfo) {
    		logger.debug("Loaded key " + info.getColumnInfoItem());
    		colKeyToInfoMap.put(info.getColumnInfoItem(), info);
    	}
    	Map<String, ColumnInfo> colAliasToInfoMap = new HashMap<String, ColumnInfo>();
    	for (ColumnInfo info : columnInfo) {
    		colAliasToInfoMap.put(info.getColumnAlias(), info);
    	}
    	List<ColumnInfo> newColumnInfo = new ArrayList<ColumnInfo>();
        for (int col = 1; col <= rsmd.getColumnCount(); col++) {
        	logger.debug(rsmd.getColumnClassName(col));
        	ColumnInfo ci;
        	if (((QueryCache) query).isScriptModified()) {
        		String columnKey = rsmd.getColumnLabel(col);
        		if (colAliasToInfoMap.get(columnKey) != null) {
        			ci = colAliasToInfoMap.get(columnKey);
        		} else {
        			ci = new ColumnInfo(columnKey);
//        			ci.setWidth(-1); XXX i don't know why this is here but taking it out makes columns size properly
        		}
        	} else {
        		Item item = query.getSelectedColumns().get(col - 1);
        		String columnKey = rsmd.getColumnLabel(col);
        		logger.debug("Matching key " + item.getName());
        		if (colKeyToInfoMap.get(item) != null) {
        			ci = colKeyToInfoMap.get(item);
        		} else {
        			ci = new ColumnInfo(item, columnKey);
//        			ci.setWidth(-1); XXX i don't know why this is here but taking it out makes columns size properly
        		}
        	}
            ci.setDataType(ResultSetRenderer.getDataType(rsmd, col));
            ci.setParent(ResultSetRenderer.this);
            newColumnInfo.add(ci);
        }
        
        logger.debug("Initializing columns: now have " + newColumnInfo.size() + " columns, previously had " + columnInfo.size());
        for (int i = Math.min(newColumnInfo.size(), columnInfo.size()) - 1; i >= 0; i--) {
        	if (newColumnInfo.get(i) != columnInfo.get(i)) {
        		ColumnInfo removedColumn = columnInfo.remove(i);
        		fireChildRemoved(ColumnInfo.class, removedColumn, i);
        		columnInfo.add(i, newColumnInfo.get(i));
        		fireChildAdded(ColumnInfo.class, newColumnInfo.get(i), i);
        	}
        }
        
        if (newColumnInfo.size() > columnInfo.size()) {
        	logger.debug("New columns have been added. There should be " + (newColumnInfo.size() - columnInfo.size()) + " columns added.");
        	for (int i = columnInfo.size(); i < newColumnInfo.size(); i++) {
        		columnInfo.add(newColumnInfo.get(i));
        		logger.debug("Adding column info to position " + i);
        		fireChildAdded(ColumnInfo.class, columnInfo.get(i), i);
        	}
        } else if (newColumnInfo.size() < columnInfo.size()) {
        	logger.debug("Columns have been removed. There should be " + (columnInfo.size() - newColumnInfo.size()) + " columns removed.");
        	for (int i = columnInfo.size() - 1; i >= newColumnInfo.size(); i--) {
        		ColumnInfo removedCI = columnInfo.remove(i);
        		fireChildRemoved(ColumnInfo.class, removedCI, i);
        	}
        }
    }

    public void resetToFirstPage() {
        clearResultSetLayout();
    }

    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex, boolean printing) {
    	if (printing && printingResultSet == null) {
    		executeQueryForPrinting();
    	} else if (!printing && paintingRS == null) {
    		executeQuery();
    	}
        if (executeException != null) {
            return renderFailure(g, contentBox, scaleFactor, pageIndex);
        } else {
            return renderSuccess(g, contentBox, scaleFactor, pageIndex, printing);
        }
    }
    
    public boolean renderFailure(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex) {
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
                cause = cause.getCause();
            }
            logger.debug("Exception on rendering " + executeException.getMessage());
            for (StackTraceElement ste : executeException.getStackTrace()) {
            	logger.debug(ste);
            }
        }
        renderMessage(g, contentBox, errorMessage);
        
        return false;
    }

    /**
     * Helper method for rendering a list of strings in the middle of the
     * content box. Used for rendering a message to the user if the result set
     * cannot be displayed for any reason.
     * 
     * @param g
     *            The graphics to draw into.
     * @param contentBox
     *            The context box that defines the bounds that can be drawn in.
     * @param errorMessage
     *            The list of strings that will be displayed as a message.
     */
    private void renderMessage(Graphics2D g, ContentBox contentBox,
            List<String> errorMessage) {
        FontMetrics fm = g.getFontMetrics();
        int width =  (int) contentBox.getWidth();
        int height = (int) contentBox.getHeight();
        int textHeight = fm.getHeight() * errorMessage.size();
        
        int y = Math.max(0, height/2 - textHeight/2);
        for (String text : errorMessage) {
            y += fm.getHeight();
            int textWidth = fm.stringWidth(text);
            g.drawString(text, width/2 - textWidth/2, y);
        }
    }

    public boolean renderSuccess(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex, boolean printing) {
    	CachedRowSet rs = this.paintingRS;
    	if (printing) {
    		rs = printingResultSet;
    	}
    	
    	if (rs == null) {
    	    renderMessage(g, contentBox, 
    	            Collections.singletonList("The result set from " 
    	                    + query.getName() + " is empty."));
    	    return false;
    	}
    	
    	RowComparator comparator = new RowComparator();
    	for (int i = 0; i < getColumnInfoList().size(); i++) {
    	    if (!getColumnInfoList().get(i).getWillGroupOrBreak().equals(GroupAndBreak.NONE)) {
    	        comparator.addSortColumn(i + 1, true);
    	    }
    	}
    	try {
    	    rs = rs.createSharedSorted(comparator);
    	    
    	    autosizeColumnInformation(g, contentBox, rs);

    	    Graphics2D zeroClipGraphics = (Graphics2D) g.create(0, 0, 0, 0);
            createResultSetLayout(zeroClipGraphics, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (pageIndex >= pageCells.get().size()) {
            logger.warn("Trying to print page " + pageIndex + " but only " + pageCells.get().size() + " pages exist.");
            return false;
        }
        
        Graphics2D g2 = (Graphics2D) g.create();
        List<ResultSetCell> currentPagePositions = pageCells.get().get(pageIndex);
        for (ResultSetCell position : currentPagePositions) {
            position.paint(g2);
        }
        
        if (borderType == BorderStyles.OUTSIDE || borderType == BorderStyles.FULL) {
            int contentBoxHeight = (int) contentBox.getHeight();
            int contentBoxWidth = (int) contentBox.getWidth();
			g.drawLine(0, 0, 0, contentBoxHeight - 1);
			g.drawLine(contentBoxWidth - 1, 0, contentBoxWidth - 1, contentBoxHeight);
            g.drawLine(0, contentBoxHeight - 1, contentBoxWidth - 1, contentBoxHeight - 1);
            g.drawLine(0, 0, contentBoxWidth - 1, 0);
        }
        
        if (colBeingDragged != null) {
            int xLocation = 0;
            for (ColumnInfo ci : getColumnInfoList()) {
                if (ci.getWillGroupOrBreak() == GroupAndBreak.BREAK) continue;
                if (ci != colBeingDragged) {
                    xLocation += ci.getWidth();
                } else {
                    xLocation += ci.getWidth();
                    break;
                }
            }
            
            Color oldColor = g.getColor();
            Stroke oldStroke = g.getStroke();
            g.setColor(DRAGGABLE_COL_LINE_COLOUR);
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{5, 5}, 0));
            g.drawLine(xLocation, 0, xLocation, (int) contentBox.getHeight());
            g.setColor(oldColor);
            g.setStroke(oldStroke);
        }
        
        //TODO come up with a better way to clear the result sets, probably at the same time the page positions are cleared to get fresh data.
        final boolean isLastPage = pageCells.get().size() - 1 == pageIndex;
        if (isLastPage) {
            if (printing) {
                printingResultSet = null;
            } else {
                this.paintingRS = null;
            }
        }
        return !isLastPage;
    }
    
    /**
     * Call this method if something changes in the result set that causes
     * the need to redefine the layout of the result set. 
     */
    public void clearResultSetLayout() {
        pageCells.set(null);
        printingResultSet = null;
        paintingRS = null;
    }

    /**
     * This method does all of the layout of each section of a result set. This
     * should be executed any time a part of the result set renderer changes. If
     * all of the properties that defines a layout are set this method will
     * return immediately. This includes but is not limited to: font changes,
     * break changes, new columns being sub-totaled, different graphics in use
     * such as printing vs painting, and changes to the query.
     * <p>
     * The bulk of the work is done by the ReportPositionRenderer.
     * <p>
     * Package private for testing.
     * 
     * @param g
     *            This should be a graphics object that is the same as the
     *            graphics the result set will be rendered into. If this
     *            graphics object is different the components may be laid out in
     *            a way that will have text clipped by the bounding
     *            {@link ContentBox}.
     * @param rs
     *            This result set will be iterated over to lay out the result
     *            set. If the result set pointer should not be changed a copy of
     *            the result set, using a CachedRowSet or calling createShared
     *            on a {@link CachedRowSet} should be passed instead. The result
     *            set should also be sorted by the columns defined as breaks to
     *            avoid sections that are identified by the same section.
     */
    void createResultSetLayout(Graphics2D g, ResultSet rs) throws SQLException {
        if (pageCells.get() != null) return; 
        
        final ReportPositionRenderer reportPositionRenderer = new ReportPositionRenderer(getHeaderFont(), getBodyFont(), borderType, (int) getParent().getWidth(), nullString);
        
        List<List<ResultSetCell>> createResultSetLayout = reportPositionRenderer.createResultSetLayout(
                g, rs, getColumnInfoList(), getParent(), isPrintingGrandTotals());
        pageCells.set(createResultSetLayout);
    }
    
    /**
     * This is a helper method for printing. If a column is found to have an
     * invalid width (ie: less than 0) its width will be redefined to be the
     * width of the largest value in the column. This is mainly used for new
     * columns added to a table when the query is modified.
     */
    private void autosizeColumnInformation(Graphics2D g,
            ContentBox contentBox, ResultSet rs)
            throws SQLException {
        FontMetrics fm = g.getFontMetrics(getBodyFont());
        for (ColumnInfo ci : columnInfo) {
        	if (ci.getWidth() < 0) {
        		int currentRow = rs.getRow();
        		rs.beforeFirst();
        		int columnIndex = columnInfo.indexOf(ci) + 1;
        		double maxWidth = fm.getStringBounds(rs.getMetaData().getColumnName(columnIndex), g).getWidth();
        		double currentHeight = 0;
        		while (rs.next() && currentHeight < contentBox.getHeight()) {
        			if (rs.getString(columnIndex) == null) {
        				continue;
        			}
        			Rectangle2D stringBounds = fm.getStringBounds(rs.getString(columnIndex), g);
        			double stringLength = stringBounds.getWidth();
        			currentHeight += stringBounds.getHeight();
        			if (stringLength > maxWidth) {
        				maxWidth = stringLength;
        			}
        		}
        		rs.absolute(currentRow);
        		ci.setWidth((int) maxWidth + COLUMN_WIDTH_BUFFER);
        	}
        }
    }
    
    /**
     * This will replace null values with the designated null string.
     */
    public String replaceNull(String string) {
        if (string == null) {
            return nullString;
        } else {
            return string;
        }
    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.unmodifiableList(columnInfo);
    }
    
    public Font getHeaderFont() {
        if (headerFont != null) {
            return headerFont;
        } else if (getBodyFont() != null) {
            return getBodyFont().deriveFont(Font.BOLD);
        } else {
            return null;
        }
    }
    
    public void setHeaderFont(Font headerFont) {
        Font oldFont = getHeaderFont();
        this.headerFont = headerFont;
        firePropertyChange("headerFont", oldFont, getHeaderFont());
    }
    
    public Font getBodyFont() {
        if (bodyFont != null) {
            return bodyFont;
        } else if (getParent() != null) {
            return getParent().getFont();
        } else {
            return null;
        }
    }
    
    public void setBodyFont(Font bodyFont) {
        Font oldFont = getBodyFont();
        this.bodyFont = bodyFont;
        firePropertyChange("bodyFont", oldFont, getBodyFont());
    }
    
    public String getNullString() {
        return nullString;
    }
    
    public void setNullString(String nullString) {
        String oldNullString = this.nullString;
        this.nullString = nullString;
        firePropertyChange("nullString", oldNullString, nullString);
    }
    
    @Override
    public ContentBox getParent() {
        return (ContentBox) super.getParent();
    }
    
	public void setBackgroundColour(Color backgroundColour) {
		firePropertyChange("backgroundColour", this.backgroundColour, backgroundColour);
		this.backgroundColour = backgroundColour;
	}
	
	public Color getBackgroundColour() {
		return backgroundColour;
	}
	public BorderStyles getBorderType() {
		return borderType;
	}
	public void setBorderType(BorderStyles borderType) {
		firePropertyChange("borderType", this.borderType, borderType);
		this.borderType = borderType;
	}
	
	public List<ColumnInfo> getColumnInfoList() {
		return columnInfo;
	}

    /**
     * This method will look for a column edge that is near the given x
     * location. If the edge of a column is close to this value it will be
     * defined as the column edge to be dragged. This is to support the
     * operation of defining column widths by clicking and dragging.
     * 
     * @param mouseXPos
     *            The distance from the left side of the parent content box to
     *            look for an edge of a column. This cannot be null.
     * @return True if a column is now able to be dragged, false otherwise.
     */
    public boolean defineColumnBeingDragged(final double mouseXPos) {
        setColBeingDragged(null);
        int overallWidth = 0;
        for (ColumnInfo ci : getColumnInfoList()) {
            if (ci.getWillGroupOrBreak() == GroupAndBreak.BREAK) continue;
            overallWidth += ci.getWidth();
            if (mouseXPos > overallWidth - 5 && mouseXPos < overallWidth + 5) {
                
                logger.debug("OVER CORRECT POSITION " + ci.getName() + " overall width is " 
                        + overallWidth + " mouse x position is " + mouseXPos);
                
                setColBeingDragged(ci);
                return true;
            }
        }
        return false;
    }

    /**
     * This is package private for testing. Other classes don't need to know
     * which column is being dragged, modifying a column by dragging should be
     * done through the appropriate methods.
     * @see {@link #defineColumnBeingDragged(double)} {@link #moveColumnBeingDragged(double)}
     */
    ColumnInfo getColBeingDragged() {
        return colBeingDragged;
    }

    /**
     * This will move the edge of the column defined to be dragged by the delta.
     * To shrink the column give a negative value as the delta. If no column is
     * being dragged this will do nothing.
     * 
     * @param moveDelta
     *            The amount to resize the column width. Cannot be null.
     * @return True if a column was resized. False otherwise.
     */
    public boolean moveColumnBeingDragged(double moveDelta) {
        if (colBeingDragged != null) {
            int newColWidth = (int) (colBeingDragged.getWidth() + moveDelta);
            if (newColWidth < 0) {
                newColWidth = 0;
            }
            colBeingDragged.setWidth(newColWidth);
            return true;
        }
        return false;
    }

    public List<WabitObject> getDependencies() {
        List<WabitObject> dependencies = new ArrayList<WabitObject>();
        if (query != null) {
            dependencies.add(query);
        }
        return dependencies;
    }
    
    public void removeDependency(WabitObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }
    
    @Override
    public void setParent(WabitObject parent) {
        if (getParent() != null) {
            getParent().removeWabitListener(parentChangeListener);
        }
        super.setParent(parent);
        if (getParent() != null) {
            getParent().addWabitListener(parentChangeListener);
        }
    }

    /**
     * This method should ONLY be needed for testing.
     */
    List<List<ResultSetCell>> findCells() {
        return Collections.unmodifiableList(pageCells.get());
    }
    
    public void setColBeingDragged(ColumnInfo colBeingDragged) {
        ColumnInfo oldCol = this.colBeingDragged;
        this.colBeingDragged = colBeingDragged;
        firePropertyChange("colBeingDragged", oldCol, colBeingDragged);
    }

    public void setPrintingGrandTotals(boolean isPrintingGrandTotals) {
        boolean oldGrandTotals = this.printingGrandTotals;
        this.printingGrandTotals = isPrintingGrandTotals;
        firePropertyChange("printingGrandTotals", oldGrandTotals, isPrintingGrandTotals);
    }

    public boolean isPrintingGrandTotals() {
        return printingGrandTotals;
    }

	public void refresh() {
		try {
			// Force the query to get a new result set
			query.executeStatement();
			executeQuery();
		} catch (SQLException ex) {
			executeException = ex;
		}
	}

    /**
     * Returns the exception that occurred when executing the statement that
     * returns the result set to be rendered. Returns null if there was no
     * exception.
     * <p>
     * Package private for testing.
     */
	Exception getExecuteException() {
        return executeException;
    }

    @Override
    /*
     * This method should only be used internally or in special cases such as
     * an undo manager or synchronizing with a server.
     */
    protected boolean removeChildImpl(WabitObject child) {
        if (columnInfo.contains(child)) {
            int index = columnInfo.indexOf(child);
            columnInfo.remove(child);
            fireChildRemoved(child.getClass(), child, index);
            return true;
        }
        return false;
    }
    
    @Override
    /*
     * This method should only be used internally or in special cases such as
     * an undo manager or synchronizing with a server.
     */
    protected void addChildImpl(WabitObject child, int index) {
        columnInfo.add(index, (ColumnInfo) child);
        child.setParent(this);
        fireChildAdded(child.getClass(), child, index);
    }
}
