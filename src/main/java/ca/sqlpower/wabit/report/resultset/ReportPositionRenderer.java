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

package ca.sqlpower.wabit.report.resultset;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.CachedRowSet;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;
import ca.sqlpower.wabit.report.resultset.ResultSetCell.BorderType;

/**
 * This class renders the result set. A new renderer should be created each time
 * a new layout is required.
 */
public class ReportPositionRenderer {
    
    private static final Logger logger = Logger.getLogger(ReportPositionRenderer.class);

    /**
     * This is extra padding added on to cells where extra space is desired for border/separator lines.
     */
    public static final int BORDER_LINE_SIZE = 2;
    
    private final Font headerFont;
    
    private final Font bodyFont;

    private final BorderStyles borderType;

    /**
     * This is the available width to render in. This lets the renderer
     * define borders so they don't go out of bounds.
     */
    private final int availableWidth;
    
    private final String nullString;

    /**
     * This is the current page being created by
     * {@link #createResultSetLayout(Graphics2D, ResultSet, List, ContentBox, boolean)}.
     * Since the layout can only be done once this will only increment.
     */
    private int currentPage;
    
    /**
     * This boolean is used to enforce that the layout of a report position renderer can only
     * be done once.
     */
    private boolean hasLayoutStarted = false;

	private final Color dataColour;

	private final Color headerColour;
    
    public ReportPositionRenderer(Font headerFont, Font bodyFont, Color dataColour, Color headerColour, BorderStyles borderType, int availableWidth, String nullString) {
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
		this.dataColour = dataColour;
		this.headerColour = headerColour;
        this.borderType = borderType;
        this.availableWidth = availableWidth;
        this.nullString = nullString;
    }
    
	/**
	 * This method does all of the layout of each section of a result set. This
	 * should be executed any time a part of the result set renderer changes. If
	 * all of the properties that defines a layout are set this method will
	 * return immediately. This includes but is not limited to: font changes,
	 * break changes, new columns being sub-totaled, different graphics in use
	 * such as printing vs painting, and changes to the query.
	 * 
	 * @param g
	 *            This should be a graphics object that is the same as the
	 *            graphics the result set will be rendered into. If this
	 *            graphics object is different the components may be laid out in
	 *            a way that will have text clipped by the bounding
	 *            {@link ContentBox}.
	 * @param rs
	 *            This is the result set we want to display.
	 * 
	 * @param columnInfoList
	 *            List of the columns metadata we know about.
	 * 
	 * @param boxHeight
	 *            The height of the box that contains the result set.
	 * 
	 * @param isPrintingGrandTotals
	 *            Wether or not to display grand totals at the end.
	 */
    public List<List<ResultSetCell>> createResultSetLayout(Graphics2D g, ResultSet rs, List<ColumnInfo> columnInfoList,
    		double boxHeight, boolean isPrintingGrandTotals) throws SQLException {
        
    	if (hasLayoutStarted) 
    			throw new IllegalStateException("The layout of a report position renderer should only " +
        		"be done once per renderer. Create a new renderer if a new layout is needed");
        hasLayoutStarted = true;
        
        CachedRowSet rsCopy = new CachedRowSet();
        rsCopy.populate(rs);
        rsCopy.beforeFirst();
        
        List<BigDecimal> grandTotals = new ArrayList<BigDecimal>();
        for (ColumnInfo ci : columnInfoList) {
            if (ci.getWillSubtotal()
            		&& ci.getDataType() == DataType.NUMERIC) {
                grandTotals.add(BigDecimal.ZERO);
            } else {
                grandTotals.add(null);
            }
        }
        
        List<BigDecimal> sectionTotals = new ArrayList<BigDecimal>();
        sectionTotals = new ArrayList<BigDecimal>();
        for (ColumnInfo ci : columnInfoList) {
            if (ci.getWillSubtotal()) {
                sectionTotals.add(BigDecimal.ZERO);
            } else {
                sectionTotals.add(null);
            }
        }
        
        Map<Integer, List<BigDecimal>> groupingTotalMap = new HashMap<Integer, List<BigDecimal>>();
        
        for (int i = 0; i < columnInfoList.size(); i++) {
            if (columnInfoList.get(i).getWillGroupOrBreak().equals(GroupAndBreak.GROUP)) {
                List<BigDecimal> groupingTotals = new ArrayList<BigDecimal>();
                for (ColumnInfo ci : columnInfoList) {
                    if (ci.getWillSubtotal()) {
                        groupingTotals.add(BigDecimal.ZERO);
                    } else {
                        groupingTotals.add(null);
                    }
                }
                groupingTotalMap.put(Integer.valueOf(i), groupingTotals);
            }
        }
        
        int yPosition = 0;
        List<Object> sectionKey = null;
        
        List<List<ResultSetCell>> cellsGroupedPerPage = new ArrayList<List<ResultSetCell>>();
        
        currentPage = 0;
        cellsGroupedPerPage.add(new ArrayList<ResultSetCell>());
       
        List<List<ResultSetCell>> headerRows = new ArrayList<List<ResultSetCell>>();

        //for each result set entry
        while (rsCopy.next()) {
        
        	// This is a temp list of rows we will need to print.
            List<List<ResultSetCell>> rowsToAdd = new ArrayList<List<ResultSetCell>>();
            
            if (!rsCopy.isFirst() && sectionKey == null) 
                throw new IllegalStateException("The initial section key was undefined! " +
                        "Cannot start laying out the result set.");
                      
            boolean forcePrintHeaders = false;
            boolean forcePageBreak = false;
            
            final List<Object> newSectionKey = createRowSectionKey(rsCopy, columnInfoList);
            
            if (sectionKey == null 
            		|| !newSectionKey.equals(sectionKey)
                    || rsCopy.isFirst()) 
            {
                sectionKey = newSectionKey;
                forcePrintHeaders = true;
                if (hasOnePageBreakingColumn(columnInfoList)){
                	forcePageBreak = true;
                }
            }
            
            headerRows.clear();
            headerRows.add(Collections.singletonList(renderSectionHeader(g, sectionKey, columnInfoList)));
            headerRows.add(renderColumnHeaders(g, columnInfoList));
            
            //create a row of values, decide if we need to hide grouped columns
            List<ResultSetCell> rowCells = renderRow(g, rsCopy, columnInfoList, false);
            rowsToAdd.addAll(Collections.singletonList(rowCells));
            
            //Increment totals counters
            for (ColumnInfo ci : columnInfoList) {
                final int colIndex = columnInfoList.indexOf(ci);
                if (ci.getWillSubtotal()) {
                    final BigDecimal valueToAdd = rsCopy.getBigDecimal(colIndex + 1);
                    BigDecimal total = sectionTotals.get(colIndex);
                    total = total.add(valueToAdd);
                    sectionTotals.set(colIndex, total);
                    
                    for (List<BigDecimal> subtotals : groupingTotalMap.values()) {
                        BigDecimal groupTotal = subtotals.get(colIndex);
                        groupTotal = groupTotal.add(valueToAdd);
                        subtotals.set(colIndex, groupTotal);
                    }
                }
                if (ci.getWillSubtotal()
                		&& ci.getDataType() == DataType.NUMERIC) {
                    final BigDecimal valueToAdd = rsCopy.getBigDecimal(colIndex + 1);
                    BigDecimal total = grandTotals.get(colIndex);
                    BigDecimal cellValue = valueToAdd;
                    if (cellValue == null) {
                        cellValue = BigDecimal.valueOf(0);
                    }
                    total = total.add(cellValue);
                    grandTotals.set(colIndex, total);
                }
            }

            //decide if we need to print subtotals for breaks
            boolean hasNext = rsCopy.next();
            List<Object> nextSectionKey = null;
            if (hasNext) {
                nextSectionKey = createRowSectionKey(rsCopy, columnInfoList);
            }
            List<Object> nextRowValues = new ArrayList<Object>();
            if (hasNext) {
                for (int i = 0; i < columnInfoList.size(); i++) {
                    nextRowValues.add(rsCopy.getObject(i + 1));
                }
            }
            rsCopy.previous();

            
            // Now decide if we need to print sub-totals, due to grouping
            // changes or section breaks;
            
            // Start by looking for section breaks
            if (!hasNext || !nextSectionKey.equals(sectionKey)) { 
                for (int i = columnInfoList.size() - 1; i >= 0; i--) {
                    if (groupingTotalMap.get(i) != null) {
                        List<BigDecimal> groupingTotals = groupingTotalMap.get(i);
                        String groupingText = "Total for " + rsCopy.getString(i + 1);

                        rowsToAdd.addAll(
                        		renderTotals(
		                        		g, 
		                        		groupingTotals, 
		                        		columnInfoList, 
		                        		false,
		                                groupingText, 
		                                i,
		                                cellsGroupedPerPage,
		                                boxHeight,
		                                false));

                        for (int j = 0; j < groupingTotals.size(); j++) {
                            if (groupingTotals.get(j) != null) {
                                groupingTotals.set(j, BigDecimal.ZERO);
                            }
                        }
                    }
                }
                
                StringBuffer sectionKeyText = new StringBuffer();
                for (int i = 0; i < sectionKey.size(); i++) {
                	Object value = sectionKey.get(i);
                	if (value != null) {
                		if (sectionKeyText.length() > 0) {
                			sectionKeyText.append(";");
                		}
                		sectionKeyText.append("Total for " + value);
                	}
                }
                
                if (!sectionKeyText.toString().equals("")) {
                	rowsToAdd.addAll(
                			renderTotals(
                					g, 
                					sectionTotals, 
                					columnInfoList, 
                					false,
                					sectionKeyText.toString(), 
                					0,
                					cellsGroupedPerPage,
                					boxHeight,
                					false));
                	
                	sectionTotals = new ArrayList<BigDecimal>();
                	for (ColumnInfo ci : columnInfoList) {
                		if (ci.getWillSubtotal()) {
                			sectionTotals.add(BigDecimal.ZERO);
                		} else {
                			sectionTotals.add(null);
                		}
                	}
                }
                
            // Now look for grouping changes
            } else if (hasNext) { 
                
                for (int i = columnInfoList.size() - 1; i >= 0; i--) {
                    Object oldValue = rsCopy.getObject(i + 1);
                    Object nextValue = nextRowValues.get(i);
                    if (groupingTotalMap.get(i) != null && 
                            ((oldValue != null && !oldValue.equals(nextValue))
                            || (oldValue == null && nextValue != null))) {
                    	
                        List<BigDecimal> groupingTotals = groupingTotalMap.get(i);
                        String groupingText = String.valueOf(rsCopy.getString(i + 1));
                        
                        rowsToAdd.addAll(
                        		renderTotals(
                        				g, 
                        				groupingTotals, 
                        				columnInfoList, 
                        				false,
                        				"Total for " + groupingText, 
                        				i,
                        				cellsGroupedPerPage,
                        				boxHeight,
                        				true));
                        
                        
                        for (int j = 0; j < groupingTotals.size(); j++) {
                            if (groupingTotals.get(j) != null) {
                                groupingTotals.set(j, BigDecimal.ZERO);
                            }
                        }
                    }
                }
                
            }
            
            // Print whatever sub total rows.
            yPosition = addRowsGroup(
            				rowsToAdd, 
            				cellsGroupedPerPage, 
            				headerRows, 
            				boxHeight, 
            				yPosition, 
            				forcePrintHeaders,
            				forcePageBreak);
            
        }
        
        // We might need to print grand totals.
        if (isPrintingGrandTotals) {
        	yPosition = addRowsGroup(
        			renderTotals(
        				g, 
        				grandTotals, 
        				columnInfoList, 
        				true, 
        				"Grand Total", 
        				0,
        				cellsGroupedPerPage,
        				boxHeight,
        				false),
    				cellsGroupedPerPage,
    				headerRows,
    				boxHeight,
    				yPosition,
    				false,
    				false);
        }
        
        return cellsGroupedPerPage;
    }

    /**
     * This helper method for {@link #createResultSetLayout(Graphics2D, ResultSet, List, ContentBox, boolean)}
     * will return a list of objects that defines a new section of the result set when laid out.
     * The list contains one value per column in the result set where each value could be the value
     * in that column in the current row if it is part of the section header or null if it is not
     * part of the section header.
     */
    private List<Object> createRowSectionKey(ResultSet rs,
            List<ColumnInfo> columnInfoList) throws SQLException {
        List<Object> newSectionKey = new ArrayList<Object>();
        for (ColumnInfo ci : columnInfoList) {
            if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
            		|| ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) {
                newSectionKey.add(rs.getObject(columnInfoList.indexOf(ci) + 1));
            } else {
                newSectionKey.add(null);
            }
        }
        return newSectionKey;
    }
    
    private boolean hasOnePageBreakingColumn(List<ColumnInfo> columnInfoList) 
    {
        for (ColumnInfo ci : columnInfoList) {
            if (ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This is a helper method for
     * {@link #createResultSetLayout(Graphics2D, ResultSet)}. This will add the
     * groups of rows to the current or next page of the cellsGroupedPerPage as well as
     * increment the yPosition and possibly the current page count.
     * 
     * This method takes a list of rows to add and will make all those stick
     * together. It wont add a page break between those.
     * 
     * @param cells
     *            The {@link ResultSetCell}s to add to the pages
     * @param cellsGroupedPerPage
     *            The list of pages that contain cells. This value will be
     *            modified to contain the given cells in the current or next
     *            page.
     * @param contentBox
     *            The content box the {@link ResultSetRenderer} is contained in
     *            to get visible dimesnsions.
     * @param yPosition
     *            The current y position that can be used to layout cells. The
     *            space above this position contains cells already.
     * @param forcePrintHeaders 
     * @return The new y position that defines where new cells can be placed
     *         below
     */
    private int addRowsGroup(
    		List<List<ResultSetCell>> rows, 
    		List<List<ResultSetCell>> cellsGroupedPerPage,
    		List<List<ResultSetCell>> headers,
            double boxHeight, 
            int yPosition, 
            boolean forcePrintHeaders,
            boolean forcePageBreak) 
    {
    	boolean headerAreIn = false;
    	List<List<ResultSetCell>> rowsToPrint = new ArrayList<List<ResultSetCell>>();

    	// Maybe we need to force the headers to appear
    	if (forcePrintHeaders) {
    		rowsToPrint.addAll(headers);
    		headerAreIn = true;
    	}
    	rowsToPrint.addAll(rows);
    	
    	// First, figure the total height of this group of rows.
    	int totalHeight = 0;
    	for (List<ResultSetCell> currentRow : rowsToPrint) {
    		
    		// For each row, find the tallest cell
    		int maxHeight = 0;
    		for (ResultSetCell cell : currentRow) {
    			maxHeight = Math.max(cell.getBounds().height, maxHeight);
    		}
    		totalHeight += maxHeight;
    	}
    	
    	// Build the list of all rows/cells to print.
    	// We might have to add the headers rows
    	if (!headerAreIn && (totalHeight + yPosition) > boxHeight) {
    		rowsToPrint.clear();
    		rowsToPrint.addAll(headers);
    		rowsToPrint.addAll(rows);
    	}
    	
    	// Now we need to space the rows vertically.
    	int tmpY = yPosition;
    	totalHeight = 0;
    	for (List<ResultSetCell> currentRow : rowsToPrint) {
    		
    		// For each row, find the tallest cell
    		int maxHeight = 0;
    		for (ResultSetCell cell : currentRow) {
    			maxHeight = Math.max(cell.getBounds().height, maxHeight);
    		}
    		
    		// we move the cells a bit further down
    		// for the next row...
    		for (ResultSetCell movingCell : currentRow) {
    			movingCell.moveCell(
						new Point(movingCell.getBounds().x, 
						tmpY));
			}
    		
    		tmpY += maxHeight;
    		totalHeight += maxHeight;
    	}
    	
    	// Does this group fit in the remaining space...
    	if (yPosition == 0 ||
    			(totalHeight + yPosition) < boxHeight && !forcePageBreak) {
    		
    		// It fits. we can add them all to this page.
    		for (List<ResultSetCell> currentRow : rowsToPrint) {
    			cellsGroupedPerPage.get(currentPage).addAll(currentRow);
    		}
    		
    		// Let's not forget to increment the yPosition pointer.
    		yPosition += totalHeight;
    		
    	} else {
    		
    		// We need to start a new page because the group of rows
    		// is taller than the remaining space on this page
    		
			yPosition = 0;
			currentPage++;
			cellsGroupedPerPage.add(new ArrayList<ResultSetCell>());
			
			// For every row of cells...
			for (List<ResultSetCell> currentRow : rowsToPrint) {
				
				// Move those cells to their new position
				for (ResultSetCell movingCell : currentRow) {
					movingCell.moveCell(new Point(movingCell.getBounds().x, yPosition));
				}
			
				// Find the actual height of this row
				int maxY = 0;
				for (ResultSetCell columnHeaderCell : currentRow) {
					maxY = Math.max(columnHeaderCell.getBounds().height, maxY);
				}
				
				yPosition += maxY;
				cellsGroupedPerPage.get(currentPage).addAll(currentRow);
			}
    	}
    	
        return yPosition;
    }

    /**
     * This renders the current row of the result set given based on the
     * graphics and the column information. This method will not modify the
     * cursor position in the result set.
     * 
     * @param g
     *            The graphics to use to determine dimension information.
     * @param rs
     *            The result set whose current row will be used to create
     *            {@link ResultSetCell}s. The cursor will not be modified in
     *            this result set. The row the cursor is at will be the row
     *            rendered.
     * @param columnInformation
     *            This defines properties of the columns in the result set. This
     *            will define formatting, spacing, alignment, and other values
     *            specific to each column.
     * @param showGroupsAsRepeat
     *            If true cells that would be blank as they are repeated values
     *            will appear but marked as a continued value.
     */
    private List<ResultSetCell> renderRow(
    		Graphics2D g, 
    		ResultSet rs, 
    		List<ColumnInfo> columnInformation, 
            boolean showGroupsAsRepeat) throws SQLException 
    {
        
        if (rs.getMetaData().getColumnCount() != columnInformation.size()) 
            throw new IllegalArgumentException("The column information for rendering a row was " +
            		"missing columns for the given result set");
        
        List<ResultSetCell> rowCells = new ArrayList<ResultSetCell>();
        
        FontMetrics fm = g.getFontMetrics(bodyFont);
        g.setFont(bodyFont);
        int x = 0;
        for (int col = 0; col < columnInformation.size(); col++) {
            int cellHeight = fm.getHeight();
            ColumnInfo ci = columnInformation.get(col);
            
            if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
            		|| ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) continue;
            
            Insets padding = getPadding(ci);
            cellHeight += padding.top;
            
            Object value = rs.getObject(col + 1);
            String formattedValue;
            if (ci.getFormat() != null && value != null) {
                if (logger.isDebugEnabled()) {
                	logger.debug("Format is:"+ ci.getFormat()+ "string is:"+ rs.getString(col + 1));
                }
                formattedValue = ci.getFormat().format(value);
            } else {
                formattedValue = replaceNull(rs.getString(col + 1));
            }
            
            cellHeight += padding.bottom;
            
            Font cellFont = bodyFont;
            if (ci.getWillGroupOrBreak().equals(GroupAndBreak.GROUP)) {
                boolean prevExists = rs.previous();
                if (prevExists &&
                        ((rs.getObject(col + 1) != null && rs.getObject(col + 1).equals(value)) ||
                        (rs.getObject(col + 1) == null && value == null))) {
                    if (showGroupsAsRepeat) {
                        formattedValue = "(" + formattedValue + ")";
                        cellFont = toggleItalicness(cellFont);
                    } else {
                        formattedValue = "";
                    }
                }
                rs.next();
            }
            
            List<BorderType> borders = new ArrayList<BorderType>();
            if (borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL || 
                    borderType == BorderStyles.HORIZONTAL) {
                borders.add(BorderType.BOTTOM);
                borders.add(BorderType.TOP);
            }
            
            if ((borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL || 
                    borderType == BorderStyles.VERTICAL)) {
                if (col != 0) {
                    borders.add(BorderType.LEFT);
                }
                if (col != columnInformation.size() - 1) {
                    borders.add(BorderType.RIGHT);
                }
            }
            
            rowCells.add(
            		new ResultSetCell(
            				formattedValue, 
            				cellFont,
            				dataColour,
            				new Rectangle(
            						x, 
            						0, 
            						ci.getWidth(), 
            						cellHeight),
            				padding, 
            				ci.getHorizontalAlignment(),
            				borders));
            
            x += ci.getWidth();
        }
        
        return rowCells;
    }

    private Font toggleItalicness(Font cellFont) {
        if (cellFont.isItalic()) {
            cellFont = cellFont.deriveFont(cellFont.getStyle() & ~Font.ITALIC);
        } else {
            cellFont = cellFont.deriveFont(cellFont.getStyle() | Font.ITALIC);
        }
        return cellFont;
    }

    /**
     * This renders each section header above the column headers.
     * 
     * @param g
     *            Used to define dimension information when defining sizes of
     *            the {@link ResultSetCell}.
     * @param sectionHeader
     *            These objects define a unique section. Each column in the
     *            result set should have an entry in this list. If the column is
     *            not part of the section header the value should be null.
     *            Otherwise the value should be a unique entry in the result set
     *            and will be displayed in the header.
     * @param colInfo
     *            The column information describing the columns of the result
     *            set.
     */
    private ResultSetCell renderSectionHeader(
    		Graphics2D g, 
    		List<Object> sectionHeader, 
            List<ColumnInfo> colInfo) 
    {
        StringBuffer headerBuffer = new StringBuffer();
        for (Object headerObject : sectionHeader) {
            if (headerObject != null) {
                if (headerBuffer.length() > 0) {
                    headerBuffer.append(", ");
                }
                headerBuffer.append(colInfo.get(sectionHeader.indexOf(headerObject)).getName() + ":" + headerObject);
            }
        }
        String header = headerBuffer.toString();
        
        //Centers the header if there is enough space and the header
        //isn't too large.
        FontMetrics fm = g.getFontMetrics(headerFont);
        int tableWidth = 0;
        for (ColumnInfo ci : colInfo) {
            if (!ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
            		&& !ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) {
                tableWidth += ci.getWidth();
            }
        }
        int maxWidth = Math.min(tableWidth, availableWidth);
        
        Insets padding = getPadding(null);
        
        if (header.trim().length() == 0) {
            return new ResultSetCell(
            		"", 
            		headerFont, 
            		headerColour,
            		new Rectangle(0, 0), 
            		new Insets(0, 0, 0, 0), 
                    HorizontalAlignment.LEFT, 
                    new ArrayList<BorderType>());
        }
        
        List<BorderType> borders = new ArrayList<BorderType>();
        borders.add(BorderType.BOTTOM);
        int height = fm.getHeight() + padding.top + padding.bottom;
        height += BORDER_LINE_SIZE;
        return new ResultSetCell(
        				header, 
        				headerFont,
        				headerColour,
        				new Rectangle(0, 0, maxWidth, height), 
        				padding, 
        				HorizontalAlignment.CENTER, 
        				borders);
    }
    
    /**
     * Creates new {@link ResultSetCell}s for each column header.
     */
    private List<ResultSetCell> renderColumnHeaders(Graphics2D g, List<ColumnInfo> colInfo) {
        List<ResultSetCell> headerCells = new ArrayList<ResultSetCell>();
        
        int x = 0;
        FontMetrics fm = g.getFontMetrics(headerFont);
        g.setFont(headerFont);
        for (int col = 0; col < colInfo.size(); col++) {
            int y = 0;
            ColumnInfo ci = colInfo.get(col);
            
            if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
            		|| ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) continue;
            
            Insets padding = getPadding(ci);
            y += padding.top;
            y += fm.getHeight();
            
            final String colHeaderName = replaceNull(ci.getName());
            y += padding.bottom;
            ResultSetCell newCell = new ResultSetCell(
            							colHeaderName, 
            							headerFont,
            							headerColour,
            							new Rectangle(x, 0, ci.getWidth(), y), 
            							padding, 
            							ci.getHorizontalAlignment(), 
            							new ArrayList<BorderType>());
            headerCells.add(newCell);
            x += ci.getWidth();
        }
        
        return headerCells;
    }

    /**
     * This method will render the totals of a section. The section can either
     * be tracking totals for a subtotal or totals for a grand total.
     * 
     * @param g
     *            Used to define size dimensions of the {@link ResultSetCell}.
     * @param totalsRow
     *            A list of totals for each column. If the column has no totals
     *            the entry in the list should be null.
     * @param colInfo
     *            A list of column information for each column in the result
     *            set.
     * @param cellsGroupedPerPage 
     */
    private List<List<ResultSetCell>> renderTotals(
    		Graphics2D g, 
    		List<BigDecimal> totalsRow, 
    		List<ColumnInfo> colInfo,
            boolean isGrandTotal, 
            String breakText, 
            int breakTextPosition,
            List<List<ResultSetCell>> cellsGroupedPerPage,
            double boxHeight,
            boolean insertPaddingBelow) 
    {
        
    	List<List<ResultSetCell>> rowsToAdd = new ArrayList<List<ResultSetCell>>();
        int localX = 0;
        
        boolean hasTotals = false;
        for (BigDecimal total : totalsRow) {
            if (total != null) {
                hasTotals = true;
                break;
            }
        }
        
        if (hasTotals) {
        	Font boldBodyFont = bodyFont.deriveFont(Font.BOLD);
        	FontMetrics bodyFM = g.getFontMetrics(boldBodyFont);
        	FontMetrics headerFM = g.getFontMetrics(headerFont);
        	
        	int tableWidth = 0;
        	for (int i = breakTextPosition; i < colInfo.size(); i++) {
                tableWidth += colInfo.get(i).getWidth();
            }
        	
        	final int rowHeight = Math.max(bodyFM.getHeight(), headerFM.getHeight());
        	
        	int totalTextX = 0;
        	if (!isGrandTotal) {
        		for (int i = 0; i < breakTextPosition; i++) {
        			if (!colInfo.get(i).getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
        					&& !colInfo.get(i).getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) {
        				totalTextX += colInfo.get(i).getWidth();
        			}
        		}
        	}
        	
        	Insets textInsets = getPadding(null);
        	int height = headerFM.getHeight() + textInsets.top + textInsets.bottom;
        	height += headerFM.getHeight() / 2;
        	if (isGrandTotal) {
        		height += 2 * BORDER_LINE_SIZE;
        	}
        	
        	// Add an empty row before as a buffer.
        	final ResultSetCell emptyCell = 
    			new ResultSetCell(
    					" ", 
    					bodyFont, 
    					dataColour,
    					new Rectangle(0, 0, 1, rowHeight), 
    					getPadding(null), 
    					HorizontalAlignment.LEFT,
    					new ArrayList<BorderType>());
    		rowsToAdd.add(Collections.singletonList(emptyCell));
        	
        	// Create the "Total:" alike label
        	ResultSetCell textCell = new ResultSetCell(
        			breakText, 
        			headerFont, 
        			dataColour,
        			new Rectangle(totalTextX, 0, tableWidth, height),
        			textInsets, 
        			HorizontalAlignment.LEFT, 
        			new ArrayList<BorderType>());
        	rowsToAdd.add(Collections.singletonList(textCell));
        	
        	List<ResultSetCell> newCells = new ArrayList<ResultSetCell>();
        	for (int subCol = 0; subCol < totalsRow.size(); subCol++) {
        		int y = rowHeight;
        		ColumnInfo ci = colInfo.get(subCol);
        		
        		if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
        				|| ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) continue;
        		
        		Insets padding = getPadding(ci);
        		y += padding.top;
        		BigDecimal subtotal = totalsRow.get(subCol);
        		y += padding.bottom;
        		if (subtotal != null) {
        			String formattedValue;
        			if (ci.getFormat() != null) {
        				formattedValue = ci.getFormat().format(subtotal);
        			} else {
        				formattedValue = subtotal.toString();
        			}
        			
        			List<BorderType> grandTotalBorders = new ArrayList<BorderType>();
        			grandTotalBorders.add(BorderType.TOP);
        			y += BORDER_LINE_SIZE;
        			if (isGrandTotal) {
        				grandTotalBorders.add(BorderType.TOP);
        				y += BORDER_LINE_SIZE;
        			}
        			
        			ResultSetCell totalCell = 
        					new ResultSetCell(
        							formattedValue, 
        							boldBodyFont, 
        							dataColour,
        							new Rectangle(localX, 0, ci.getWidth(), y), 
        							padding, 
        							ci.getHorizontalAlignment(),
        							grandTotalBorders);
        			newCells.add(totalCell);
        		}
        		localX += ci.getWidth();
        	}

        	// Add the totals to the list of rows to return.
        	rowsToAdd.add(newCells);
        	
        	// Maybe an empty row after as well
        	if (insertPaddingBelow) {
        		rowsToAdd.add(Collections.singletonList(emptyCell));
        	}

        }
        
        return rowsToAdd;
    }
    
    /**
     * This will replace null values with the designated null string.
     */
    private String replaceNull(String string) {
        if (string == null) {
            return nullString;
        } else {
            return string;
        }
    }

    /**
     * This method will return the insets of each cell based on the border type
     * being used.
     */
    public Insets getPadding(ColumnInfo ci) {
        
    	if (ci != null && 
        		(ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)
        				|| ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK))) 
        {
        	return new Insets(0, 0, 0, 0);
    	}
        
        Insets insets = new Insets(1, 1, 1, 1);
        
        if (borderType == BorderStyles.VERTICAL) {
            insets.left += ResultSetRenderer.BORDER_INDENT;
            insets.right += ResultSetRenderer.BORDER_INDENT;
        } else if (borderType == BorderStyles.HORIZONTAL) {
            insets.top += ResultSetRenderer.BORDER_INDENT;
            insets.bottom += ResultSetRenderer.BORDER_INDENT;
        } else if (borderType == BorderStyles.INSIDE || borderType == BorderStyles.FULL
                || borderType == BorderStyles.OUTSIDE) {
            insets.left += ResultSetRenderer.BORDER_INDENT;
            insets.right += ResultSetRenderer.BORDER_INDENT;
            insets.top += ResultSetRenderer.BORDER_INDENT;
            insets.bottom += ResultSetRenderer.BORDER_INDENT;
        }
        return insets;
    }
}
