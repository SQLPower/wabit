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

import ca.sqlpower.object.AbstractPoolingSPListener;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.query.Item;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sql.CachedRowSet.RowComparator;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.resultset.ReportPositionRenderer;
import ca.sqlpower.wabit.report.resultset.ResultSetCell;
import ca.sqlpower.wabit.report.selectors.ContextAware;
import ca.sqlpower.wabit.rs.ResultSetEvent;
import ca.sqlpower.wabit.rs.ResultSetHandle;
import ca.sqlpower.wabit.rs.ResultSetListener;
import ca.sqlpower.wabit.rs.ResultSetProducerEvent;
import ca.sqlpower.wabit.rs.ResultSetProducerException;
import ca.sqlpower.wabit.rs.ResultSetProducerListener;
import ca.sqlpower.wabit.rs.WabitResultSetProducer;
import ca.sqlpower.wabit.rs.ResultSetHandle.ResultSetStatus;
import ca.sqlpower.wabit.rs.ResultSetHandle.ResultSetType;
import ca.sqlpower.wabit.rs.query.QueryException;

/**
 * Renders a JDBC result set using configurable absolute column widths.
 */
public class ResultSetRenderer extends AbstractWabitObject 
		implements WabitObjectReportRenderer, ContextAware {
    
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
     */
    private final WabitResultSetProducer query;
    
    /**
     * A cached copy of the result set that came from the Query object.
     */
    private ResultSetHandle resultSetHandle = null;

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
	 * know when to recreate the layout of the result set.
	 */
	private final SPListener parentChangeListener = new AbstractPoolingSPListener() {
        public void propertyChangeImpl(PropertyChangeEvent evt) {
        	pageCells.remove();
        }
    };


    /**
     * Listens to the result set handle and refreshes as new
     * data comes in.
     */
    private ResultSetListener resultSetListener = new ResultSetListener() {
		public void newData(ResultSetEvent evt) {
			if (evt.getSourceHandle().getResultSetType().equals(ResultSetType.STREAMING) &&
					getParent() != null) {
				pageCells.remove();
			    ResultSetRenderer.this.getParent().repaint();
			}
		}
		public void executionComplete(ResultSetEvent evt) {
			pageCells.remove();
			if (ResultSetRenderer.this.getParent() != null) {
				ResultSetRenderer.this.getParent().repaint();
			}
		}
		public void executionStarted(ResultSetEvent evt) {
			// don't care.
		};
	};
	
	private final SPListener columnInfoListener = new AbstractPoolingSPListener() {
		protected void propertyChangeImpl(PropertyChangeEvent evt) {
			pageCells.remove();
			ResultSetRenderer.this.getParent().repaint();
		};
	};
	
	/**
	 * This listener will fire a change event when the query changes to signal that
	 * the result set renderer needs to be repainted.
	 */
    private final ResultSetProducerListener queryChangeListener = new ResultSetProducerListener() {
		
		public void structureChanged(ResultSetProducerEvent evt) {
			refresh();
			dirty = true;
		}
		public void executionStopped(ResultSetProducerEvent evt) {
			// not interested
		}
		public void executionStarted(ResultSetProducerEvent evt) {
			// not interested
		}
	};

	private Exception internalError = null;

	private boolean dirty = false;
	
	
    public ResultSetRenderer(@Nonnull WabitResultSetProducer query) {
    	this(query, new ArrayList<ColumnInfo>());
    }
    
    public ResultSetRenderer(@Nonnull WabitResultSetProducer query, @Nonnull List<ColumnInfo> columnInfoList) {
        this.query = query;
        query.addResultSetProducerListener(queryChangeListener);
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
    	this.resultSetHandle = resultSetRenderer.resultSetHandle;
    	this.printingGrandTotals = resultSetRenderer.printingGrandTotals;
    	
    	this.columnInfo = new ArrayList<ColumnInfo>();
    	for (ColumnInfo column : resultSetRenderer.columnInfo) {
    		ColumnInfo newColumnInfo = new ColumnInfo(column);
    		addChild(newColumnInfo, columnInfo.size());
    	}
    	
    	// Listen to modifications to the query structure.
    	query.addResultSetProducerListener(queryChangeListener);
    	if (this.resultSetHandle != null) {
    		this.resultSetHandle.addResultSetListener(resultSetListener);
    	}
    	
    	setName(resultSetRenderer.getName());
    }
    
    public WabitResultSetProducer getContent(){
    	return query;
    }
    
    @Override
    public CleanupExceptions cleanup() {
    	query.removeResultSetProducerListener(queryChangeListener);
    	if (this.resultSetHandle != null) {
    		this.resultSetHandle.removeResultSetListener(resultSetListener);
    	}
    	return new CleanupExceptions();
    }
    
    public void resetToFirstPage() {
    	this.pageCells.remove();
		this.executeException = null;
		this.internalError = null;
    }
    
    private void setResultSetHandle(ResultSetHandle rsh) {
    	
    	this.internalError = null;
    	
    	// Clean up all previous listeners
    	if (this.resultSetHandle != null) {
    		this.resultSetHandle.removeResultSetListener(resultSetListener);
    		this.resultSetHandle.cancel();
    	}
    	
    	refresh();
    	
		this.resultSetHandle = rsh;
    }
	
	/**
	 * Synchronizes this renderer to a resultset contents.
	 * 
	 * @param rs
	 * 				The RS to map onto
	 * @throws SQLException
	 *             If the resultset metadata methods fail.
	 */
    private void initColumns(ResultSet rs) {
    	
    	this.internalError = null;
    	
        try {
        	ResultSetMetaData rsmd = rs.getMetaData();
        	
        	//id columns by items and alias in cases where the query is text based.
        	Map<Item, ColumnInfo> colKeyToInfoMap = new HashMap<Item, ColumnInfo>();
        	for (ColumnInfo info : columnInfo) {
        		logger.debug("Loaded key " + info.getColumnInfoItem());
        		if (info.getColumnInfoItem() != null) {
        			colKeyToInfoMap.put(info.getColumnInfoItem(), info);
        		}
        	}
        	Map<String, ColumnInfo> colAliasToInfoMap = new HashMap<String, ColumnInfo>();
        	for (ColumnInfo info : columnInfo) {
        		colAliasToInfoMap.put(
        				info.getColumnAlias() == null
        						? info.getName()
        						: info.getColumnAlias(), 
        				info);
        	}
        	
        	//sort column info into the new desired positions.
        	List<ColumnInfo> newColumnInfo = new ArrayList<ColumnInfo>();
        	
        	for (int col = 1; col <= rsmd.getColumnCount(); col++) {
        		
        		logger.debug(rsmd.getColumnClassName(col));
        		
        		ColumnInfo ci;
        		
        		String columnKey = rsmd.getColumnLabel(col).toUpperCase();
        		if (colAliasToInfoMap.get(columnKey) != null) {
        			
        			ci = colAliasToInfoMap.get(columnKey);
        		
        		} else {
        		
        			ci = new ColumnInfo(columnKey);
        			
        			ci.setDataType(ResultSetRenderer.getDataType(rsmd, col));
            		
            		if (ci.getDataType().equals(DataType.NUMERIC)) {
            			ci.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            		}
            		
            		ci.setParent(ResultSetRenderer.this);
        		}
        		
        		newColumnInfo.add(ci);
        	}
        	
        	//rearrange columns to match result set.
        	logger.debug("Initializing columns: now have " + newColumnInfo.size() + " columns, previously had " + columnInfo.size());
        	
        	//Be careful, iterating forward while removing elements from columnInfo
        	//can cause us to run off the end before we finish iterating through 
        	//newColumnInfo
        	for (int i = 0 ; i < newColumnInfo.size(); i++) {
        		
        		if (i >= columnInfo.size()) {
        			
        			logger.debug("Adding column info to position " + i);
        			addChild(newColumnInfo.get(i), i);
        			
        		} else {
        			
        			String newColumnName = newColumnInfo.get(i).getColumnAlias();
        			if (newColumnName == null) {
        				newColumnName = newColumnInfo.get(i).getName();
        			}
        			
        			String oldColumnName = columnInfo.get(i).getColumnAlias();
        			if (oldColumnName == null) {
        				oldColumnName = columnInfo.get(i).getName();
        			}
        			
        			if (!oldColumnName.equalsIgnoreCase(newColumnName)) {
        				
        				ColumnInfo oldInfo = columnInfo.get(i);
        				ColumnInfo newInfo = newColumnInfo.get(i);
        				removeChild(oldInfo);
        				for (ColumnInfo currentColumn : columnInfo) {
        					if (newInfo.getColumnAlias().equalsIgnoreCase(currentColumn.getColumnAlias())) {
        						removeChild(currentColumn);
        						break;
        					}
        				}
        				addChild(newInfo, i);        			
        				
        			}
        		}
        	}
        	
        	if (newColumnInfo.size() < columnInfo.size()) {
        		logger.debug("Columns have been removed. There should be " + (columnInfo.size() - newColumnInfo.size()) + " columns removed.");
        		for (int i = columnInfo.size() - 1; i >= newColumnInfo.size(); i--) {
        			removeChild(columnInfo.get(i));
        		}
        	}
        } catch (Exception e) {
        	
        	logger.error(e);
        	e.printStackTrace();
        	this.internalError = e;
        	
        }
    }

    public synchronized boolean renderReportContent(
    		Graphics2D g,
    		double width,
    		double height,
    		double scaleFactor, 
    		int pageIndex, 
    		boolean printing, 
    		SPVariableResolver variablesContext) 
    {
    	
    	if (resultSetHandle == null || dirty) {
    		try {
				this.setResultSetHandle(
						query.execute(new SPVariableHelper(ResultSetRenderer.this), resultSetListener, !printing));
				if (!printing) {
					return false;
				}
			} catch (ResultSetProducerException e) {
				this.internalError = e;
			} finally {
				dirty = false;
			}
    	}
    	
    	if (printing) {
    		colBeingDragged = null;
    	}
    	
    	if (this.resultSetHandle == null) {
    		renderMessage(
    				g, 
    				width,
    				height,
    				Collections.singletonList("The associated query does not return any results."));
    		return false;
    	}
    	
    	initColumns(resultSetHandle.getResultSet());
    	
    	if (this.resultSetHandle.getStatus().equals(ResultSetStatus.ERROR)) {
    		
    		return renderFailure(
    				this.resultSetHandle.getException(), 
    				g, 
    				width,
    				height,
    				scaleFactor, 
    				pageIndex);
    	
    	}else if (this.internalError != null) {
    		
    		return renderFailure(
					this.internalError, 
					g, 
					width,
					height,
					scaleFactor, 
					pageIndex);
			
    	} else {
    		boolean pagesLeft = renderSuccess(g, width, height, scaleFactor, pageIndex, printing);
    		if (printing) {
    			return pagesLeft;
    		} else {
    			return false;
    		}
    	}
    }
    
    private boolean renderFailure(
    		Exception failure, 
    		Graphics2D g, 
    		double width,
    		double height, 
    		double scaleFactor, 
    		int pageIndex) 
    {
        List<String> errorMessage = new ArrayList<String>();
        if (failure instanceof QueryException) {
            QueryException qe = (QueryException) failure;
            Throwable cause = qe.getCause();
            errorMessage.add("Query failed: " + cause);
            errorMessage.addAll(Arrays.asList(qe.getQuery().split("\n")));
        } else {
            errorMessage.add("Query failed: " + failure);
            if (failure != null) {
            	Throwable cause = failure.getCause();
            	while (cause != null) {
            		errorMessage.add("Caused by: " + cause);
            		cause = cause.getCause();
            	}
            	logger.debug("Exception on rendering " + failure.getMessage());
            	for (StackTraceElement ste : failure.getStackTrace()) {
            		logger.debug(ste);
            	}            	
            }
        }
        renderMessage(g, width, height, errorMessage);
        
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
    private void renderMessage(
    		Graphics2D g, 
    		double width,
    		double height,
            List<String> errorMessage) 
    {
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight() * errorMessage.size();
        
        int y = Math.max(0, (int)height/2 - textHeight/2);
        for (String text : errorMessage) {
            y += fm.getHeight();
            int textWidth = fm.stringWidth(text);
            g.drawString(text, (int)width/2 - textWidth/2, y);
        }
    }

    private boolean renderSuccess(
    		Graphics2D g, 
    		double width,
    		double height,
    		double scaleFactor, 
    		int pageIndex, 
    		boolean printing) 
    {
    	
    	try {
    		
    		CachedRowSet rs = (CachedRowSet)this.resultSetHandle.getResultSet();
    		
        	if (rs.getData().size() == 0) {
        	    renderMessage(g, width, height, 
        	            Collections.singletonList("The result set from " 
        	                    + query.getName() + " is empty."));
        	    return false;
        	}
        	
            maybeCreateResultSetLayout(g, rs, width, height);
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (pageIndex >= pageCells.get().size()) {
            logger.warn("Trying to print page " + pageIndex + " but only " + pageCells.get().size() + " pages exist.");
            return false;
        }
        
        List<ResultSetCell> currentPagePositions = pageCells.get().get(pageIndex);
        for (ResultSetCell position : currentPagePositions) {
        	Graphics2D g2 = (Graphics2D) g.create();
            position.paint(g2);
            g2.dispose();
        }
        
        if (borderType == BorderStyles.OUTSIDE || borderType == BorderStyles.FULL) {
			g.drawLine(0, 0, 0, (int)height - 1);
			g.drawLine((int)width - 1, 0, (int)width - 1, (int)height);
            g.drawLine(0, (int)height - 1, (int)width - 1, (int)height - 1);
            g.drawLine(0, 0, (int)width - 1, 0);
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
            g.drawLine(xLocation, 0, xLocation, (int)height);
            g.setColor(oldColor);
            g.setStroke(oldStroke);
        }
        
        boolean isLastPage = pageCells.get().size() - 1 == pageIndex;
        return !isLastPage;
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
    private void maybeCreateResultSetLayout(
    		Graphics2D g, 
    		CachedRowSet rs, 
    		double width,
    		double height) throws SQLException {
    	
    	if (pageCells.get() != null) return; 
        
    	RowComparator comparator = new RowComparator();
    	for (int i = 0; i < getColumnInfoList().size(); i++) {
    	    if (!getColumnInfoList().get(i).getWillGroupOrBreak().equals(GroupAndBreak.NONE)) {
    	        comparator.addSortColumn(i + 1, true);
    	    }
    	}
    	
    	CachedRowSet rsCopy = rs.sort(comparator);
    	
	    autosizeColumnInformation(g, width, height, rsCopy);
	    Graphics2D zeroClipGraphics = (Graphics2D) g.create(0, 0, 0, 0);
    	
        final ReportPositionRenderer reportPositionRenderer = 
        		new ReportPositionRenderer(
        				getHeaderFont(), 
        				getBodyFont(), 
        				borderType, 
        				(int) getParent().getWidth(), 
        				nullString);
        
        List<List<ResultSetCell>> layout = 
        		reportPositionRenderer.createResultSetLayout(
        				zeroClipGraphics, 
        				rsCopy, 
        				getColumnInfoList(), 
        				height, 
        				isPrintingGrandTotals());
        
        pageCells.set(layout);
    }
    
    /**
     * This is a helper method for printing. If a column is found to have an
     * invalid width (ie: less than 0) its width will be redefined to be the
     * width of the largest value in the column. This is mainly used for new
     * columns added to a table when the query is modified.
     */
    private void autosizeColumnInformation(
    		Graphics2D g,
    		double width,
    		double height, 
            ResultSet rs) throws SQLException 
    {
        FontMetrics fm = g.getFontMetrics(getBodyFont());
        for (ColumnInfo ci : columnInfo) {
        	if (ci.getWidth() < 0) {
        		int currentRow = rs.getRow();
        		rs.beforeFirst();
        		int columnIndex = columnInfo.indexOf(ci) + 1;
        		double maxWidth = fm.getStringBounds(rs.getMetaData().getColumnName(columnIndex), g).getWidth();
        		double currentHeight = 0;
        		while (rs.next() && currentHeight < height) {
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

    public int childPositionOffset(Class<? extends SPObject> childType) {
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
    
    public void removeDependency(SPObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }

    @Override
    public void setParent(SPObject parent) {
    	 if (getParent() != null) {
             getParent().removeSPListener(parentChangeListener);
         }
         super.setParent(parent);
         if (getParent() != null) {
             getParent().addSPListener(parentChangeListener);
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
		this.pageCells.remove();
		this.executeException = null;
		this.internalError = null;
		this.dirty = true;
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
    protected boolean removeChildImpl(SPObject child) {
        if (columnInfo.contains(child)) {
            int index = columnInfo.indexOf(child);
            columnInfo.remove(child);
            child.removeSPListener(columnInfoListener);
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
    protected void addChildImpl(SPObject child, int index) {
        columnInfo.add(index, (ColumnInfo) child);
        child.setParent(this);
        child.addSPListener(columnInfoListener);
        fireChildAdded(child.getClass(), child, index);
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	List<Class<? extends SPObject>> types = new ArrayList<Class<? extends SPObject>>();
    	types.add(ColumnInfo.class);
    	return types;
    }
}
