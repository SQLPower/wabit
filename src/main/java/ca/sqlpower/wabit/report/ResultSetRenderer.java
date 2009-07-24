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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;

import ca.sqlpower.query.Item;
import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.sql.RowSetChangeEvent;
import ca.sqlpower.sql.RowSetChangeListener;
import ca.sqlpower.sql.SQL;
import ca.sqlpower.sql.CachedRowSet.RowComparator;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.query.StatementExecutor;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.QueryException;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.report.resultset.Position;
import ca.sqlpower.wabit.report.resultset.ReportPositionRenderer;
import ca.sqlpower.wabit.report.resultset.Section;
import ca.sqlpower.wabit.swingui.Icons;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Renders a JDBC result set using configurable absolute column widths.
 */
public class ResultSetRenderer extends AbstractWabitObject implements ReportContentRenderer {
    
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
     * represents the {@link Section}s in each page where each section in a page
     * may not be the entire section as defined by a {@link Position}. This will
     * be null if a change has occurred and requires the positions to be
     * recreated from the {@link ResultSetRenderer#createResultSetLayout()}
     * method. This is wrapped by a ThreadLocal to give each printing thread and
     * the UI thread different copies of the page positions. This is required
     * as the printing will render a result set without a limit while the screen
     * does have a limit to it.
     */
    private final ThreadLocal<List<List<Position>>> pagePositions = new ThreadLocal<List<List<Position>>>();
    
    /**
     * This decides if the grand totals will be printed at the end of a result
     * set.
     */
    private boolean isPrintingGrandTotals = false;
    
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
     * Lists of Formatting Options for date
     */
    private final List<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();
    /**
     * The string that will be rendered for null values in the result set.
     */
    private String nullString = "(null)";
    
    private Font headerFont;
    
    private Font bodyFont;
    
    /**
     * The query that provides the content data for this renderer.
     */
    private final QueryCache query;
    
    /**
     * A cached copy of the result set that came from the Query object.
     * This will be null when the result set is not being painted. Just
     * before the result set is to be rendered to the screen the 
     * executeQuery method should be called to populate this result set
     * and this value should be set back to null when rendering the result
     * set is finished.
     */
    private CachedRowSet paintingRS = null;
    
    /**
     * This result set should only be used when printing. If printing is not being
     * done then this result set should be null. This result set will contain the entire
     * result of the query instead of just a result set limited by a row limit.
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
	 * This listens for all property changes on the parent content box to 
	 * know when to recreate the layout of the result set. This also listens
	 * to changes in the result set renderer for changes that the ContentBox
	 * doesn't receive.
	 * <p>
	 * XXX Make the ContentBox aware of changes to the query. The content box
	 * should receive events when the query changes when it is listening to
	 * the children of a result set renderer.
	 */
	private final PropertyChangeListener parentChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            clearResultSetLayout();
        }
    };

	/**
	 * This change listener is placed on {@link CachedRowSet}s to monitor streaming result sets
	 * to know when a new row is added to the result set.
	 */
	private final RowSetChangeListener rowSetChangeListener = new RowSetChangeListener() {
		public void rowAdded(RowSetChangeEvent e) {
			firePropertyChange("resultSetRowAdded", null, e.getRow());
		}
	};
	
	/**
	 * This listener will fire a change event when the query changes to signal that
	 * the result set renderer needs to be repainted.
	 */
    private final PropertyChangeListener queryChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("name")) {
				setName("Result Set: " + query.getName());
			}
			firePropertyChange(QUERY, null, ResultSetRenderer.this.query);
		}
	};
    
    public ResultSetRenderer(QueryCache query) {
    	this(query, new ArrayList<ColumnInfo>());
    	setName("Result Set: " + query.getName());
    }
    
    public ResultSetRenderer(QueryCache query, List<ColumnInfo> columnInfoList) {
        this.query = query;
        if (query != null && query instanceof StatementExecutor) {
        	((QueryCache) query).addRowSetChangeListener(rowSetChangeListener);
        }
		query.addPropertyChangeListener(queryChangeListener);
        setUpFormats();
        columnInfo = new ArrayList<ColumnInfo>(columnInfoList);
        setName("Result Set: " + query.getName());
	}
    
    public void cleanup() {
    	if (query != null && query instanceof StatementExecutor) {
        	((QueryCache) query).removeRowSetChangeListener(rowSetChangeListener);
        }
    	query.removePropertyChangeListener(queryChangeListener);
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
        executeException = null;
		try {
            executedRs = query.fetchResultSet();
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
     * Adds some formats to the Numeric format as well as the Date Format
     * 
     */
    private void setUpFormats() {
    	// adding date Formats
    	dateFormats.add(new SimpleDateFormat("yyy/MM/dd"));
    	dateFormats.add(new SimpleDateFormat("yyy-MM-dd"));
    	dateFormats.add(new SimpleDateFormat("yyy MM dd h:mm:ss"));
    	dateFormats.add(new SimpleDateFormat("yyy/MM/dd h:mm:ss"));
    	dateFormats.add(new SimpleDateFormat("yyy-MM-dd h:mm:ss"));
    	dateFormats.add(new SimpleDateFormat("MMMM d, yy h:mm:ss"));
    	dateFormats.add(new SimpleDateFormat("MMMM d, yy"));
    	dateFormats.add(new SimpleDateFormat("MMMM d, yyyy"));
    	dateFormats.add((SimpleDateFormat)SimpleDateFormat.getDateTimeInstance());
    	dateFormats.add((SimpleDateFormat)SimpleDateFormat.getDateInstance());
    	dateFormats.add((SimpleDateFormat)SimpleDateFormat.getTimeInstance());
    }
    
    private Format getFormat(DataType dataType, String pattern){
    	logger.debug("dataType is"+ dataType+ " pattern is "+ pattern);
    	if(dataType == DataType.NUMERIC) {
    		for(DecimalFormat decimalFormat : ReportUtil.getNumberFormats()) {
    			if(decimalFormat.toPattern().equals(pattern)){
    				return decimalFormat;
    			}
    		}
    	} else if(dataType == DataType.DATE) {
    		for(SimpleDateFormat dateFormat: dateFormats) {
    			if((dateFormat.toPattern()).equals(pattern)){
    				return dateFormat;
    			}
    		}
    	}
    	
    	return null;
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
        			ci.setWidth(-1);
        		}
        	} else {
        		Item item = ((QueryCache) query).getQuery().getSelectedColumns().get(col - 1);
        		String columnKey = rsmd.getColumnLabel(col);
        		logger.debug("Matching key " + item.getName());
        		if (colKeyToInfoMap.get(item) != null) {
        			ci = colKeyToInfoMap.get(item);
        		} else {
        			ci = new ColumnInfo(item, columnKey);
        			ci.setWidth(-1);
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
        
        return false;
    }

    public boolean renderSuccess(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex, boolean printing) {
    	CachedRowSet rs = this.paintingRS;
    	if (printing) {
    		rs = printingResultSet;
    	}
    	
    	RowComparator comparator = new RowComparator();
    	for (int i = 0; i < getColumnInfoList().size(); i++) {
    	    if (getColumnInfoList().get(i).getWillBreak()) {
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

        if (pageIndex >= pagePositions.get().size()) {
            logger.warn("Trying to print page " + pageIndex + " but only " + pagePositions.get().size() + " pages exist.");
            return false;
        }
        
        Graphics2D g2 = (Graphics2D) g.create();
        List<Position> currentPagePositions = pagePositions.get().get(pageIndex);
        List<Position.PositionType> positionTypes = Arrays.asList(Position.PositionType.values());
        for (Position position : currentPagePositions) {
            final ReportPositionRenderer reportPositionRenderer = position.getReportPositionRenderer();
            if (positionTypes.indexOf(position.getFirstPositionType()) <= positionTypes.indexOf(Position.PositionType.SECTION_HEADER)
                    && positionTypes.indexOf(position.getLastPositionType()) >= positionTypes.indexOf(Position.PositionType.SECTION_HEADER)) {
                Dimension dim = reportPositionRenderer.renderSectionHeader(g2, position.getSection().getSectionHeader(), getColumnInfoList(), position.getSection());
                g2.translate(0, dim.getHeight());
            }
            
            if (positionTypes.indexOf(position.getFirstPositionType()) <= positionTypes.indexOf(Position.PositionType.COLUMN_HEADER)
                    && positionTypes.indexOf(position.getLastPositionType()) >= positionTypes.indexOf(Position.PositionType.COLUMN_HEADER)) {
                Dimension dim = reportPositionRenderer.renderColumnHeader(g2, columnInfo, position.getSection());
                g2.translate(0, dim.getHeight());
            }
            
            Graphics2D verticalBorderG = (Graphics2D) g2.create();
            int sectionHeight = 0;
            if (positionTypes.indexOf(position.getFirstPositionType()) <= positionTypes.indexOf(Position.PositionType.ROW)
                    && positionTypes.indexOf(position.getLastPositionType()) >= positionTypes.indexOf(Position.PositionType.ROW)) {
                int startingRow = position.getSection().getStartRow();
                if (position.getStartingRow() != null) {
                    startingRow = position.getStartingRow();
                }
                int endingRow = position.getSection().getEndRow();
                if (position.getEndingRow() != null) {
                    endingRow = position.getEndingRow();
                }
                for (int row = startingRow; row <= endingRow; row++) {
                    Dimension dim;
                    try {
                        rs.absolute(row);
                        dim = reportPositionRenderer.renderRow(g2, rs, columnInfo, position.getSection());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    g2.translate(0, dim.getHeight());
                    if (borderType == BorderStyles.HORIZONTAL || borderType == BorderStyles.INSIDE
                            || borderType == BorderStyles.FULL) {
                        g2.drawLine(0, 0, (int) contentBox.getWidth() - 1, 0);
                    }
                    sectionHeight += dim.getHeight();
                }
            }
            if (borderType == BorderStyles.VERTICAL || borderType == BorderStyles.INSIDE 
                    || borderType == BorderStyles.FULL) {
                int x = 0;
                for (int col = 0; col < columnInfo.size() - 1; col++) {
                    if (columnInfo.get(col).getWillBreak()) continue;
                    Insets padding = reportPositionRenderer.getPadding(columnInfo.get(col));
                    x += padding.left + columnInfo.get(col).getWidth() + padding.right;
                    verticalBorderG.drawLine(x, 0, x, sectionHeight);
                }
            }
            
            if (positionTypes.indexOf(position.getFirstPositionType()) <= positionTypes.indexOf(Position.PositionType.TOTALS)
                    && positionTypes.indexOf(position.getLastPositionType()) >= positionTypes.indexOf(Position.PositionType.TOTALS)) {
                Dimension dim = reportPositionRenderer.renderTotals(g2, position.getSection().getTotals(), columnInfo, position.getSection());
                g2.translate(0, dim.getHeight());
            }
        }
        
        if (borderType == BorderStyles.OUTSIDE || borderType == BorderStyles.FULL) {
            int contentBoxHeight = (int) contentBox.getHeight();
            int contentBoxWidth = (int) contentBox.getWidth();
			g.drawLine(0, 0, 0, contentBoxHeight - 1);
			g.drawLine(contentBoxWidth - 1, 0, contentBoxWidth - 1, contentBoxHeight);
            g.drawLine(0, contentBoxHeight - 1, contentBoxWidth - 1, contentBoxHeight - 1);
            g.drawLine(0, 0, contentBoxWidth - 1, 0);
        }
        
        //TODO come up with a better way to clear the result sets, probably at the same time the page positions are cleared to get fresh data.
        final boolean isLastPage = pagePositions.get().size() - 1 == pageIndex;
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
    private void clearResultSetLayout() {
        pagePositions.set(null);
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
     * This method will first create all of the sections based on the result set
     * and the columns defined as breaks. Then the {@link Position}s will be
     * created to lay out the result set. Multiple passes of the layout can be
     * done if desired to try and increase the look of the layout.
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
        if (pagePositions.get() != null) return; 
        
        List<Section> sectionList = new ArrayList<Section>();
        pagePositions.set(new ArrayList<List<Position>>());
        List<BigDecimal> grandTotals = new ArrayList<BigDecimal>();
        for (ColumnInfo ci : getColumnInfoList()) {
            if (ci.getDataType() == DataType.NUMERIC) {
                grandTotals.add(BigDecimal.ZERO);
            } else {
                grandTotals.add(null);
            }
        }
        
        final ReportPositionRenderer reportPositionRenderer = new ReportPositionRenderer(getHeaderFont(), getBodyFont(), borderType, (int) getParent().getWidth(), nullString);
        rs.beforeFirst();
        
        int sectionStartRow = 1;
        int sectionEndRow = 1;
        List<BigDecimal> sectionTotals = new ArrayList<BigDecimal>();
        List<Object> sectionKey = null;
        
        while (rs.next()) {
            
            List<Object> newSectionKey = new ArrayList<Object>();
            for (ColumnInfo ci : getColumnInfoList()) {
                if (ci.getWillBreak()) {
                    newSectionKey.add(rs.getObject(getColumnInfoList().indexOf(ci) + 1));
                } else {
                    newSectionKey.add(null);
                }
            }
            
            if (!rs.isFirst()) {
                if (sectionKey == null) throw new IllegalStateException("The initial section key was undefined! Cannot start laying out the result set.");
                if (!newSectionKey.equals(sectionKey)) {
                    Section newSection = new Section(sectionStartRow, sectionEndRow - 1, sectionTotals, sectionKey);
                    sectionList.add(newSection);
                    
                    sectionKey = newSectionKey;
                    sectionStartRow = sectionEndRow;
                    sectionTotals = new ArrayList<BigDecimal>();
                    for (ColumnInfo ci : getColumnInfoList()) {
                        if (ci.getWillSubtotal()) {
                            sectionTotals.add(new BigDecimal(0));
                        } else {
                            sectionTotals.add(null);
                        }
                    }
                }
            } else {
                sectionKey = newSectionKey;
                for (ColumnInfo ci : getColumnInfoList()) {
                    if (ci.getWillSubtotal()) {
                        sectionTotals.add(new BigDecimal(0));
                    } else {
                        sectionTotals.add(null);
                    }
                }
            }
            
            for (ColumnInfo ci : getColumnInfoList()) {
                final int colIndex = getColumnInfoList().indexOf(ci);
                if (ci.getWillSubtotal()) {
                    BigDecimal total = sectionTotals.get(colIndex);
                    total = total.add(rs.getBigDecimal(colIndex + 1));
                    sectionTotals.set(colIndex, total);
                }
                if (ci.getDataType() == DataType.NUMERIC) {
                    BigDecimal total = grandTotals.get(colIndex);
                    BigDecimal cellValue = rs.getBigDecimal(colIndex + 1);
                    if (cellValue == null) {
                        cellValue = BigDecimal.valueOf(0);
                    }
                    total = total.add(cellValue);
                    grandTotals.set(colIndex, total);
                }
                
            }
            
            sectionEndRow++;
        }
        sectionList.add(new Section(sectionStartRow, sectionEndRow - 1, sectionTotals, sectionKey));
        if (isPrintingGrandTotals) {
            sectionList.add(new Section(grandTotals));
        }
        
        //The way the Position objects are created here are tied to how
        //renderSuccess works. If renderSuccess changes such that the
        //header order or arrangement changes this section will need to be changed
        //as well.
        
        int y = 0;
        List<Position> onePagePositions = new ArrayList<Position>();
        Position.PositionType startingPositionType;
        Integer startingPositionRow;
        Position.PositionType endingPositionType;
        Integer endingPositionRow;
        
        //TODO refactor parts of this loop into a helper method
        for (Section section : sectionList) {
            startingPositionRow = null;
            endingPositionRow = null;
            startingPositionType = Position.PositionType.SECTION_HEADER;
            endingPositionType = Position.PositionType.SECTION_HEADER;
            
            Dimension headerDimension = reportPositionRenderer.renderSectionHeader(g, section.getSectionHeader(), getColumnInfoList(), section);
            if (headerDimension.height > getParent().getHeight()) {
                pagePositions.set(new ArrayList<List<Position>>());
                return;
            }
            if (y + headerDimension.getHeight() > getParent().getHeight()) {
                pagePositions.get().add(onePagePositions);
                onePagePositions = new ArrayList<Position>();
                y = 0;
            }
            
            y += headerDimension.getHeight();
            
            Dimension columnHeaderDimension = reportPositionRenderer.renderColumnHeader(g, columnInfo, section);
            if (columnHeaderDimension.height > getParent().getHeight()) {
                pagePositions.set(new ArrayList<List<Position>>());
                return;
            }
            if (y + columnHeaderDimension.getHeight() > getParent().getHeight()) {
                onePagePositions.add(new Position(section, startingPositionType, startingPositionRow, endingPositionType, endingPositionRow, reportPositionRenderer));
                pagePositions.get().add(onePagePositions);
                onePagePositions = new ArrayList<Position>();
                startingPositionType = Position.PositionType.COLUMN_HEADER;
                y = 0;
            }
            endingPositionType = Position.PositionType.COLUMN_HEADER;
            
            y += columnHeaderDimension.getHeight();
            
            for (int row = section.getStartRow(); row <= section.getEndRow(); row++) {
                rs.absolute(row);
                Dimension rowDimension = reportPositionRenderer.renderRow(g, rs, columnInfo, section);
                if (rowDimension.height > getParent().getHeight()) {
                    pagePositions.set(new ArrayList<List<Position>>());
                    return;
                }
                if (y + rowDimension.getHeight() > getParent().getHeight()) {
                    onePagePositions.add(new Position(section, startingPositionType, startingPositionRow, endingPositionType, endingPositionRow, reportPositionRenderer));
                    pagePositions.get().add(onePagePositions);
                    onePagePositions = new ArrayList<Position>();
                    startingPositionType = Position.PositionType.ROW;
                    startingPositionRow = Integer.valueOf(row);
                    y = 0;
                }
                endingPositionType = Position.PositionType.ROW;
                endingPositionRow = Integer.valueOf(row);
                
                y += rowDimension.getHeight();
            }
            
            Dimension totalsDimension = reportPositionRenderer.renderTotals(g, section.getTotals(), columnInfo, section);
            if (totalsDimension.getHeight() >  getParent().getHeight()) {
                pagePositions.set(new ArrayList<List<Position>>());
                return;
            }
            if (y + totalsDimension.getHeight() > getParent().getHeight()) {
                onePagePositions.add(new Position(section, startingPositionType, startingPositionRow, endingPositionType, endingPositionRow, reportPositionRenderer));
                pagePositions.get().add(onePagePositions);
                onePagePositions = new ArrayList<Position>();
                startingPositionType = Position.PositionType.TOTALS;
                endingPositionRow = null;
                y = 0;
            }
            endingPositionType = Position.PositionType.TOTALS;
            
            y += totalsDimension.getHeight();
            
            onePagePositions.add(new Position(section, startingPositionType, startingPositionRow, endingPositionType, endingPositionRow, reportPositionRenderer));
        }
        pagePositions.get().add(onePagePositions);
        
        logger.debug("The result set will print across " + pagePositions.get().size() + " pages.");
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
    
    public QueryCache getQuery() {
        return query;
    }
    
    public DataEntryPanel getPropertiesPanel() {
        FormLayout layout = new FormLayout("pref, 4dlu, fill:pref:grow, 4dlu, pref");
        final DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        // TODO gap (padding) between columns
        // TODO line under header?
        
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.setFont(getHeaderFont());
        fb.append("Header Font", headerFontExample, ReportUtil.createFontButton(headerFontExample));
        
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(getBodyFont());
        fb.append("Body Font", bodyFontExample, ReportUtil.createFontButton(bodyFontExample));
        fb.nextLine();

        final JTextField nullStringField = new JTextField(nullString);
        fb.append("Null string", nullStringField);
        fb.nextLine();
        
        final JLabel colourLabel = new JLabel(" ");
       	colourLabel.setBackground(getBackgroundColour());
        colourLabel.setOpaque(true);
        final JComboBox colourCombo = new JComboBox();
        colourCombo.setRenderer(new ColorCellRenderer(85, 30));
        for (BackgroundColours bgColour : BackgroundColours.values()) {
        	colourCombo.addItem(bgColour.getColour());
        }
        colourCombo.setSelectedItem(backgroundColour);
        colourCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color colour = (Color) colourCombo.getSelectedItem();
				colourLabel.setBackground(colour);
			}
		});
        fb.append("Background", colourLabel, colourCombo);
        fb.nextLine();
        final JComboBox borderComboBox = new JComboBox(BorderStyles.values());
        borderComboBox.setSelectedItem(borderType);
        fb.append("Border", borderComboBox);
        fb.nextLine();
        final JCheckBox grandTotalsCheckBox = new JCheckBox("Grand totals");
        grandTotalsCheckBox.setSelected(isPrintingGrandTotals);
        fb.append("", grandTotalsCheckBox);
        fb.nextLine();
        
        fb.appendRow("fill:pref");
        Box box = Box.createVerticalBox();
        final List<DataEntryPanel> columnPanels = new ArrayList<DataEntryPanel>();
        final FormLayout columnLayout = new FormLayout("min(pref; 100dlu):grow, 5dlu, min(pref; 100dlu):grow, 5dlu, pref:grow, 5dlu, pref:grow", "pref, pref");
        for (ColumnInfo ci : columnInfo) {
            DataEntryPanel columnPropsPanel = createColumnPropsPanel(columnLayout, ci);
            columnPanels.add(columnPropsPanel);
            box.add(columnPropsPanel.getPanel());
            box.add(Box.createHorizontalStrut(5));
        }
        JScrollPane columnScrollPane = new JScrollPane(box,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        columnScrollPane.setPreferredSize(new Dimension(columnScrollPane.getPreferredSize().width, 400));
        fb.append("Column info", columnScrollPane, 3);
        
        return new DataEntryPanel() {

			public boolean applyChanges() {
                setHeaderFont(headerFontExample.getFont());
                setBodyFont(bodyFontExample.getFont());
                setNullString(nullStringField.getText());
                setBackgroundColour((Color) colourCombo.getSelectedItem());
                borderType = (BorderStyles) borderComboBox.getSelectedItem();
                isPrintingGrandTotals = grandTotalsCheckBox.isSelected();
                
                boolean applied = true;
                for (DataEntryPanel columnPropsPanel : columnPanels) {
                    applied &= columnPropsPanel.applyChanges();
                }
                return applied;
            }

            public void discardChanges() {
                for (DataEntryPanel columnPropsPanel : columnPanels) {
                    columnPropsPanel.discardChanges();
                }
            }

            public JComponent getPanel() {
                return fb.getPanel();
            }

            public boolean hasUnsavedChanges() {
                boolean hasUnsaved = false;
                for (DataEntryPanel columnPropsPanel : columnPanels) {
                    hasUnsaved |= columnPropsPanel.hasUnsavedChanges();
                }
                return hasUnsaved;
            }
            
        };

    }
    
    public DataEntryPanel createColumnPropsPanel(FormLayout layout, final ColumnInfo ci) {

        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        final JTextField columnLabel = new JTextField(ci.getName());
        fb.append(columnLabel);
        
        // TODO better UI (auto/manual, and manual is based on a jtable with resizable headers)
        final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(ci.getWidth(), 0, Integer.MAX_VALUE, 12));
        fb.append(widthSpinner);
        
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(
                Icons.LEFT_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(
                Icons.CENTRE_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(
                Icons.RIGHT_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);
        Box alignmentBox = Box.createHorizontalBox();
        alignmentBox.add(leftAlign);
        alignmentBox.add(centreAlign);
        alignmentBox.add(rightAlign);
        alignmentBox.add(Box.createHorizontalGlue());
        fb.append(alignmentBox);
        
        fb.nextLine();
        
        final JComboBox dataTypeComboBox = new JComboBox(DataType.values());
        final JComboBox formatComboBox = new JComboBox();
        
        dataTypeComboBox.setSelectedItem(ci.getDataType());

        fb.append(dataTypeComboBox);
       
        if(dataTypeComboBox.getSelectedItem() == DataType.TEXT) {
        	formatComboBox.setEnabled(false);
        } else {
        	setItemforFormatComboBox(formatComboBox, (DataType)dataTypeComboBox.getSelectedItem());
        	if (ci.getFormat() != null) {
        		if (ci.getFormat() instanceof SimpleDateFormat) {
        			formatComboBox.setSelectedItem(((SimpleDateFormat) ci.getFormat()).toPattern());
        		} else if (ci.getFormat() instanceof DecimalFormat) {
        			formatComboBox.setSelectedItem(((DecimalFormat) ci.getFormat()).toPattern());
        		} else {
        			throw new ClassCastException("Cannot cast the format " + ci.getFormat().getClass() + " to a known format");
        		}
        	}
        }
        fb.append(formatComboBox);
        dataTypeComboBox.addActionListener(new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				if(((JComboBox)e.getSource()).getSelectedItem() == DataType.TEXT){
					formatComboBox.setEnabled(false);
				} else {
					formatComboBox.setEnabled(true);
				}
				setItemforFormatComboBox(formatComboBox, (DataType)dataTypeComboBox.getSelectedItem());
			}
		});
        final JCheckBox breakCheckbox = new JCheckBox("Break on Column");
        final JCheckBox subtotalCheckbox = new JCheckBox("Subtotal");
        fb.append(breakCheckbox);
        breakCheckbox.setSelected(ci.getWillBreak());
        if (ci.getDataType().equals(DataType.NUMERIC)) {
        	fb.append(subtotalCheckbox);
        	subtotalCheckbox.setSelected(ci.getWillSubtotal());
        }
        
        final JPanel panel = fb.getPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 3, 5));
        
        return new DataEntryPanel() {

            public boolean applyChanges() {
                ci.setName(columnLabel.getText());
                if (leftAlign.isSelected()) {
                    ci.setHorizontalAlignment(HorizontalAlignment.LEFT);
                } else if (centreAlign.isSelected()) {
                    ci.setHorizontalAlignment(HorizontalAlignment.CENTER);
                } else if (rightAlign.isSelected()) {
                	ci.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                }
                ci.setDataType((DataType)dataTypeComboBox.getSelectedItem());
                logger.debug("formatCombobBox.getSelectedItem is"+ (String)formatComboBox.getSelectedItem());
                
                if (formatComboBox.getSelectedItem() != null &&
                		((String)formatComboBox.getSelectedItem()).equals(ReportUtil.DEFAULT_FORMAT_STRING)) {
                		ci.setFormat(null);
                	}
                else {
                	ci.setFormat(getFormat(ci.getDataType(), (String)formatComboBox.getSelectedItem()));
                }
                ci.setWidth((Integer) widthSpinner.getValue());
                
                ci.setWillBreak(breakCheckbox.isSelected());
                ci.setWillSubtotal(subtotalCheckbox.isSelected());
                
                clearResultSetLayout();
                
                return true;
            }

            public void discardChanges() {
            	// no op
            }

            public JComponent getPanel() {
                return panel;
            }

            public boolean hasUnsavedChanges() {
                return true;
            }
            
        };
    }
    
    private void setItemforFormatComboBox(JComboBox combobox, DataType dataType) {
    	combobox.removeAllItems();
    	combobox.addItem(ReportUtil.DEFAULT_FORMAT_STRING);
    	if(dataType == DataType.NUMERIC) {
    		for(NumberFormat item : ReportUtil.getNumberFormats()) {
    			combobox.addItem(((DecimalFormat)item).toPattern());
    		}
    	} else if(dataType == DataType.DATE) {
    		for(DateFormat item : dateFormats) {
    			combobox.addItem(((SimpleDateFormat)item).toPattern());
    		}
    	}
    	
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

    public void processEvent(PInputEvent event, int type) {
        //TODO allow other cool things to happen with this event
    }

    public List<WabitObject> getDependencies() {
        List<WabitObject> dependencies = new ArrayList<WabitObject>();
        if (query != null) {
            dependencies.add(query);
        }
        dependencies.addAll(columnInfo);
        return dependencies;
    }
    
    @Override
    public void setParent(WabitObject parent) {
        if (getParent() != null) {
            getParent().removePropertyChangeListener(parentChangeListener);
        }
        super.setParent(parent);
        if (getParent() != null) {
            getParent().addPropertyChangeListener(parentChangeListener);
        }
    }

    /**
     * Package private for testing.
     */
    List<Section> findSections() {
        List<Section> sectionList = new ArrayList<Section>();
        for (List<Position> positionList : pagePositions.get()) {
            for (Position position : positionList) {
                if (!sectionList.contains(position.getSection())) {
                    sectionList.add(position.getSection());
                }
            }
        }
        return Collections.unmodifiableList(sectionList);
    }
}
