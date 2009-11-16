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

package ca.sqlpower.wabit.report;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.CellSetAxisMetaData;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import ca.sqlpower.swingui.ColourScheme;
import ca.sqlpower.wabit.AbstractWabitListener;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.CleanupExceptions;
import ca.sqlpower.wabit.WabitListener;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitUtils;
import ca.sqlpower.wabit.enterprise.client.Watermarker;
import ca.sqlpower.wabit.rs.olap.OlapQuery;
import ca.sqlpower.wabit.rs.olap.OlapQueryEvent;
import ca.sqlpower.wabit.rs.olap.OlapQueryListener;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableModel;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.HierarchyComponent;
import ca.sqlpower.wabit.swingui.olap.CellSetTableHeaderComponent.LayoutItem;

/**
 * Renders a CellSet from a MDX query on a report layout.
 */
public class CellSetRenderer extends AbstractWabitObject implements
        WabitObjectReportRenderer {
    
    private final static Logger logger = Logger.getLogger(CellSetRenderer.class);
    
    /**
     * This message will be displayed if the cell set being rendered is null.
     */
    private static final String EMPTY_CELL_SET_MESSAGE = "Empty cell set.";
    
    /**
     * This is the OLAP query this {@link CellSetRenderer} was originally based off of
     */
    private final OlapQuery olapQuery;
    
    private final static int PADDING = 10;

    
    /**
     * This is the OLAP query being displayed by this cell set renderer.
     */
    private OlapQuery modifiedOlapQuery;
    
    /**
     * This is the current cell set being displayed.
     */
    private CellSet cellSet;
    
    /**
     * The font of the column and row headers.
     */
    private Font headerFont;
    
    /**
     * The font of the text in the cell set.
     */
    private Font bodyFont;
    
    private String errorMessage = null;
    
    /**
     * The alignment of the text in the body of this
     * cell set.
     */
    private HorizontalAlignment bodyAlignment = HorizontalAlignment.RIGHT;
    
    /**
     * This is the overriding format of each cell in the cell set. If this is set
     * the format here will be used to format the cell values. If this is not set
     * the format stored in the cell set will be used.
     */
    private DecimalFormat bodyFormat;

    /**
     * This map stores the header position of each member the last time the
     * header was rendered not in printing mode. This is used to tell if the
     * mouse has moved over a header to highlight it and allow drilling down.
     * This map stores both row and column headers.
     */
    private final Map<Member, Set<Rectangle>> memberHeaderMap = new HashMap<Member, Set<Rectangle>>();
    
    /**
     * Updates the name of this renderer if the name of the query backing it
     * has changed.
     */
    private WabitListener nameListener = new AbstractWabitListener() {
        
        @Override
		public void propertyChangeImpl(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("name")) {
				setName((String) evt.getNewValue());
			}
		}

    };
    
    /**
     * This member is the member the user is over with their mouse. This property
     * is only here until the render report content is moved to the swing component
     * to this class.
     */
    private Member selectedMember;

    /**
     * Listens to the query and updates the view every time the query has been
     * executed. If the event is received on the event dispatch thread, the GUI
     * update is done immediately; otherwise it's scheduled in the Swing event
     * queue.
     */
    private final OlapQueryListener queryListener = new OlapQueryListener() {
		public void queryExecuted(final OlapQueryEvent evt) {
		    runInForeground(new Runnable() {
		        public void run() {
		            try {
		                setCellSet(evt.getCellSet());
		            } catch (Exception e) {
		                throw new RuntimeException(e);
		            }
		        }
		    });
		}
    };
    
    private boolean initDone = false;

    public CellSetRenderer(CellSetRenderer cellSetRenderer) {
    	this.olapQuery = cellSetRenderer.getContent();
    	try {
			this.setModifiedOlapQuery(OlapQuery.copyOlapQuery(cellSetRenderer.getModifiedOlapQuery()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	setName(cellSetRenderer.getName());
    	this.olapQuery.addWabitListener(nameListener);
    	this.headerFont = cellSetRenderer.headerFont;
    	this.bodyFont = cellSetRenderer.bodyFont;
    	this.bodyAlignment = cellSetRenderer.bodyAlignment;
    	this.bodyFormat = cellSetRenderer.bodyFormat;
    	this.cellSet = cellSetRenderer.cellSet;
    	this.errorMessage = cellSetRenderer.errorMessage;
    	this.initDone = false;
    }
    
    public CellSetRenderer(OlapQuery olapQuery) {
        logger.debug("Initializing a new cellset renderer.");
        this.olapQuery = olapQuery;
        setName(olapQuery.getName());
        olapQuery.addWabitListener(nameListener);
    }
    
    public void init() {
        if (this.initDone) return;
        try {
        	if (modifiedOlapQuery == null) {
        		setModifiedOlapQuery(OlapQuery.copyOlapQuery(olapQuery));

        		// This code will eventually fire the change and set the cellset
        		// (must be done synchronously--don't use asyncExecute!)
        		modifiedOlapQuery.executeOlapQuery();
            } else if (modifiedOlapQuery.hasCachedAttributes()) {
            	modifiedOlapQuery.executeOlapQuery();
            }
        	this.initDone=true;
        } catch (Exception e) {
            logger.warn("Error while executing Olap Query", e);
            errorMessage = "Error when executing query:\n" + e;
        }
    }
    
    public OlapQuery getContent(){
    	return olapQuery;
    }

    @Override
    public CleanupExceptions cleanup() {
    	if (modifiedOlapQuery != null && !this.initDone) {
    		modifiedOlapQuery.removeOlapQueryListener(queryListener);
    	}
        olapQuery.removeWabitListener(nameListener);
        return new CleanupExceptions();
    }

    public Color getBackgroundColour() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This method renders the report content in the CellSetRenderer
     */
    public boolean renderReportContent(Graphics2D g, ContentBox contentBox,
            double scaleFactor, int pageIndex, boolean printing) {
    	init();
        if (getBodyFont() == null) {
            setBodyFont(g.getFont());
        }
        if (getHeaderFont() == null) {
            setHeaderFont(g.getFont());
        }
        
        g.setFont(getHeaderFont());
        
        if (getCellSet() == null) {
        	g.drawString(EMPTY_CELL_SET_MESSAGE, 0, g.getFontMetrics().getHeight());
        	return false;
        }
        
        int headerFontHeight = g.getFontMetrics().getHeight();
        
        g.setFont(getBodyFont());
        FontMetrics bodyFM = g.getFontMetrics();
        int bodyFontHeight = bodyFM.getHeight();
        
        int maxRowHeight = Math.max(headerFontHeight, bodyFontHeight);
        
        CellSetAxis cellSetAxis = getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal());
        CellSetAxisMetaData axisMetaData = cellSetAxis.getAxisMetaData();
        int hierarchyCount = axisMetaData.getHierarchies().size();
        Position firstPosition = cellSetAxis.getPositions().get(0);
		int[] parentDepth = new int[firstPosition.getMembers().size()];
		int totalDepthToSubtract = 0; //this variable is for drill member
        for (int j = 0; j < firstPosition.getMembers().size(); j++) {
        	Member member = firstPosition.getMembers().get(j);
        	parentDepth[j] = member.getDepth();
        	totalDepthToSubtract += parentDepth[j];
        }
        int totalHeaderHeight = headerFontHeight * (hierarchyCount - totalDepthToSubtract);
        
        // divide by 0 error when going to smallest zoom level if maxRowHeight is 0
        if (maxRowHeight == 0) {
        	maxRowHeight = 1;
        }
        int numRows = (int) ((contentBox.getHeight() - totalHeaderHeight) / maxRowHeight);
        if (numRows <= 0) return false;
        
        int firstRecord = numRows * pageIndex;
        CellSetTableModel tableModel = new CellSetTableModel(getCellSet());
        final JTable tableAsModel = new JTable(tableModel);
        tableAsModel.setRowHeight(maxRowHeight);
        if (!printing) {
            memberHeaderMap.clear();
        }
        
        CellSetTableHeaderComponent rowHeaderComponent =
        	new CellSetTableHeaderComponent(
        			modifiedOlapQuery, getCellSet(), Axis.ROWS, tableAsModel, g.create(),
        			getHeaderFont());
        double rowHeaderWidth = rowHeaderComponent.getPreferredSize().getWidth();
        Color oldForeground = g.getColor();
        int[] columnWidthList = getDesiredColumnWidths(g, tableAsModel);
        
        // Actually print
        int colHeaderSumHeight = printColumnHeaders(g, contentBox, printing,
				headerFontHeight, maxRowHeight, parentDepth,
				tableAsModel, rowHeaderWidth, oldForeground, columnWidthList);
        printRowHeaders(g, contentBox, printing, maxRowHeight, numRows,
				firstRecord, rowHeaderComponent, oldForeground,
				colHeaderSumHeight, columnWidthList);
        boolean shouldContinue = printBody(g, maxRowHeight, numRows, firstRecord, rowHeaderWidth,
				colHeaderSumHeight, columnWidthList, oldForeground);
        
        return shouldContinue;
    }

	private int[] getDesiredColumnWidths(Graphics2D g, final JTable tableAsModel) {
		//get all the headers widths
        g.setFont(getHeaderFont());
		CellSetAxis cellAxis = getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal());
		int[] columnWidthList = new int[tableAsModel.getColumnCount()];
		int i = 0;
		Map<Integer, String> lastMember = new HashMap<Integer, String>();
        for (Position position : cellAxis.getPositions()) {
        	for (int j = 0; j < position.getMembers().size(); j++) {
        		Member member = position.getMembers().get(j);
        		if (lastMember.get(j) != null && member.getUniqueName() == lastMember.get(j)) continue;
        		for (int k = (j + 1); k < position.getMembers().size(); k++) {
        			lastMember.put(k, null);
        		}
	            String name = member.getName();
	            int colWidth = (int) getHeaderFont().getStringBounds(name, g.getFontRenderContext()).getWidth();
	            columnWidthList[i] = Math.max(columnWidthList[i], colWidth + PADDING);
	            lastMember.put(j, member.getUniqueName());
        	}
        	i++;
        }
        
        //get all the data's widths
        g.setFont(getBodyFont());
        for (int row = 0; row < tableAsModel.getRowCount(); row++) {
        	for (int col = 0; col < tableAsModel.getColumnCount(); col++) {
        		String columnString = (String) tableAsModel.getValueAt(row, col);
        		int colWidth = (int) getBodyFont().getStringBounds(columnString, g.getFontRenderContext()).getWidth();
        		columnWidthList[col] = Math.max(columnWidthList[col], colWidth + PADDING);
        	}
        }
        return columnWidthList;
	}
	
	/**
	 * Prints the body in the CellSetRenderer
	 */
	private boolean printBody(Graphics2D g, int maxRowHeight, int numRows,
			int firstRecord, double rowHeaderWidth, int colHeaderSumHeight,
			int[] columnWidthList, Color oldForeground) {
		
		g.setBackground(oldForeground);
		
		CellSetAxis columnsAxis = getCellSet().getAxes().get(0);
        CellSetAxis rowsAxis = getCellSet().getAxes().get(1);
        g.setFont(getBodyFont());
        for (int row = firstRecord; row < rowsAxis.getPositionCount(); row++) {
            if (row == numRows + firstRecord) {
                return true;
            }
            int colPosition = 0;
            for (int col = 0; col < columnsAxis.getPositionCount(); col++) {
                String formattedValue;
                Cell cell = getCellSet().getCell(
				        columnsAxis.getPositions().get(col),
				        rowsAxis.getPositions().get(row));
				if (bodyFormat != null) {
                    try {
                        formattedValue = bodyFormat.format(cell.getDoubleValue());
                    } catch (OlapException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    formattedValue = cell.getFormattedValue();
                }
                
                double alignmentShift = 0;
                int columnWidth = columnWidthList[col];
                //final int columnWidth = tableAsModel.getColumnModel().getColumn(col).getWidth();
                final double textWidthInContext = getBodyFont().getStringBounds(formattedValue, g.getFontRenderContext()).getWidth();
                switch (bodyAlignment) {
                    case RIGHT:
                        alignmentShift = columnWidth - textWidthInContext;
                        break;
                    case LEFT:
                        break;
                    case CENTER:
                        alignmentShift = (columnWidth - textWidthInContext) / 2;
                        break;
                    default:
                        throw new IllegalStateException("Unknown alignment of type " + bodyAlignment);
                }
//                g.setBackground(Color.decode((String) cell.getPropertyValue(Property.StandardCellProperty.BACK_COLOR)));
                logger.debug("");
                logger.debug("Value: " + cell.getPropertyValue(Property.StandardCellProperty.VALUE));
                logger.debug("Cell evaluation list: " + cell.getPropertyValue(Property.StandardCellProperty.CELL_EVALUATION_LIST));
                logger.debug("Font flags: " + cell.getPropertyValue(Property.StandardCellProperty.FONT_FLAGS));
                logger.debug("Fore Color: "  + cell.getPropertyValue(Property.StandardCellProperty.FORE_COLOR));
                logger.debug("Back Color " + cell.getPropertyValue(Property.StandardCellProperty.BACK_COLOR));
                logger.debug("Formatted Value " + cell.getPropertyValue(Property.StandardCellProperty.FORMATTED_VALUE));
                logger.debug("Non empty behaviour" + cell.getPropertyValue(Property.StandardCellProperty.NON_EMPTY_BEHAVIOR));
//                g.setColor((Color) cell.getPropertyValue(Property.StandardCellProperty.FORE_COLOR));
                g.drawString(formattedValue, (int) (rowHeaderWidth + colPosition + alignmentShift), (int) (colHeaderSumHeight + ((row - firstRecord) * maxRowHeight) + maxRowHeight));
                colPosition += columnWidth;
            }
        }
        return false;
	}
	
	/**
	 * Prints the Row Headers in the Cell Set Renderer
	 */
	private void printRowHeaders(Graphics2D g, ContentBox contentBox,
			boolean printing, int maxRowHeight, int numRows, int firstRecord,
			CellSetTableHeaderComponent rowHeaderComponent,
			Color oldForeground, int colHeaderSumHeight, int[] columnWidthList) {
		
		 g.setBackground(oldForeground);
		int colourSchemeNum;
		
		//XXX properly size up the rows with the font width, this will take an iteration through all the headers
        double rowHeaderSumWidth = 0;
        colourSchemeNum = 0;
        for (HierarchyComponent hierarchyComponent : rowHeaderComponent.getHierarchies()) {
            hierarchyComponent.createLayout();
            g.setColor(ColourScheme.HEADER_COLOURS[colourSchemeNum]);
            g.fillRect((int) (hierarchyComponent.getX() + rowHeaderSumWidth), (int) (colHeaderSumHeight), (int) hierarchyComponent.getPreferredSize().getWidth(), (int) contentBox.getHeight());
            g.setColor(oldForeground);
            Member lastMemberDisplayed = null;
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
                if (layoutItem.getMember().equals(lastMemberDisplayed)) continue;
                lastMemberDisplayed = layoutItem.getMember();
                final double x = layoutItem.getBounds().getX() + rowHeaderSumWidth;
                double y = layoutItem.getBounds().getY() + colHeaderSumHeight + maxRowHeight;
                if (firstRecord * maxRowHeight > y - maxRowHeight || (firstRecord + numRows) * maxRowHeight < y - maxRowHeight) continue;
                y = y - (firstRecord * maxRowHeight);
                if (!printing) {
                    Set<Rectangle> memberRanges = memberHeaderMap.get(layoutItem.getMember());
                    if (memberRanges == null) {
                        memberRanges = new HashSet<Rectangle>();
                        memberHeaderMap.put(layoutItem.getMember(), memberRanges);
                    }
                    memberRanges.add(new Rectangle((int) x, (int) y - maxRowHeight, (int) layoutItem.getBounds().getWidth(), (int) layoutItem.getBounds().getHeight()));
                }
                Color oldColour = g.getColor();
                if (selectedMember != null && selectedMember.equals(layoutItem.getMember())) {
                    g.setColor(Color.BLUE);//XXX choose a better selected colour, probably based on the current l&f
                }
                
                g.drawString(layoutItem.getText(), (float) x, (float) y);
                g.setColor(oldColour);
            }
            rowHeaderSumWidth += hierarchyComponent.getPreferredSize().getWidth();
            colourSchemeNum++;
        }
	}
	
	/**
	 * Prints the column headers in the CellSetRenderer.
	 */
	private int printColumnHeaders(Graphics2D g, ContentBox contentBox,
			boolean printing, int headerFontHeight, int maxRowHeight,
			int[] parentDepth, JTable tableAsModel, double rowHeaderWidth, 
			Color oldForeground, int[] columnWidthList) {
		
		int colourSchemeNum = 0;
		
        CellSetTableHeaderComponent columnHeaderComponent =
        	new CellSetTableHeaderComponent(
        			modifiedOlapQuery, getCellSet(), Axis.COLUMNS, tableAsModel, g.create(),
        			getHeaderFont());
        
		int colHeaderSumHeight = 0;
        g.setFont(getHeaderFont());
        int hierarchyComponentIndex = 0;
        for (HierarchyComponent hierarchyComponent : columnHeaderComponent.getHierarchies()) {
            hierarchyComponent.createLayout();
            int maxDepth = 0;
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
            	maxDepth = Math.max((layoutItem.getMember().getDepth() - parentDepth[hierarchyComponentIndex]) + 1, maxDepth);
            }
            
            g.setColor(ColourScheme.HEADER_COLOURS[colourSchemeNum]);
            int hierarchyHeight = (int) maxDepth * (headerFontHeight);
			g.fillRect((int) (hierarchyComponent.getX() + rowHeaderWidth), (int) (hierarchyComponent.getY() + colHeaderSumHeight), (int) contentBox.getWidth(), hierarchyHeight);
            g.setColor(oldForeground);
            Member lastMemberDisplayed = null;
            
            double columnPosition = rowHeaderWidth;
            int col = 0;
            int hierarchySize = 0;
            for (LayoutItem layoutItem : hierarchyComponent.getLayoutItems()) {
            	col++;
            	double x = columnPosition;
            	columnPosition += columnWidthList[col - 1];
                if (layoutItem.getMember().equals(lastMemberDisplayed)) continue;
                lastMemberDisplayed = layoutItem.getMember();
                
                //final double x = layoutItem.getBounds().getX() + rowHeaderWidth;
                int relativeMemberDepth = layoutItem.getMember().getDepth() - parentDepth[hierarchyComponentIndex];
				final double y = (relativeMemberDepth * headerFontHeight) + colHeaderSumHeight + headerFontHeight;
                if (!printing) {
                    Set<Rectangle> memberRanges = memberHeaderMap.get(layoutItem.getMember());
                    if (memberRanges == null) {
                        memberRanges = new HashSet<Rectangle>();
                        memberHeaderMap.put(layoutItem.getMember(), memberRanges);
                    }
                    memberRanges.add(new Rectangle((int) x, (int) y - maxRowHeight, (int) columnWidthList[col - 1], headerFontHeight));
                }
                Color oldColour = g.getColor();
                if (selectedMember != null && selectedMember.equals(layoutItem.getMember())) {
                    g.setColor(Color.BLUE);//XXX choose a better selected colour, probably based on the current l&f
                }
                
                String headerText = layoutItem.getText();
                double alignmentShift = 0;
                int columnWidth = columnWidthList[col - 1];
                //final int columnWidth = tableAsModel.getColumnModel().getColumn(col).getWidth();
                final double textWidthInContext = getBodyFont().getStringBounds(headerText, g.getFontRenderContext()).getWidth();
                switch (bodyAlignment) {
                    case RIGHT:
                        alignmentShift = columnWidth - textWidthInContext;
                        break;
                    case LEFT:
                        break;
                    case CENTER:
                        alignmentShift = (columnWidth - textWidthInContext) / 2;
                        break;
                    default:
                        throw new IllegalStateException("Unknown alignment of type " + bodyAlignment);
                }
				g.drawString(headerText, (float)(x + alignmentShift), (float) y);
                g.setColor(oldColour);
                hierarchySize = (int) Math.max((relativeMemberDepth * headerFontHeight) + headerFontHeight, hierarchySize);
            }
            colHeaderSumHeight += hierarchySize;
            colourSchemeNum++;
            hierarchyComponentIndex++;
        }
		return colHeaderSumHeight;
	}
    

    public void resetToFirstPage() {
        // TODO Auto-generated method stub

    }

    public boolean allowsChildren() {
        return true;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        return 0;
    }

    public List<? extends WabitObject> getChildren() {
    	if (modifiedOlapQuery != null) {
    		return Collections.singletonList(modifiedOlapQuery);
    	} else {
    		return Collections.emptyList();
    	}
    }

    @Override
    protected void addChildImpl(WabitObject child, int index) {
    	if (index != 0) throw new IllegalArgumentException("CellSetRenderers can only " +
    			"have 1 child and should have an index of 0 not " + index);
    	setModifiedOlapQuery((OlapQuery) child);
    }
    
    public void setHeaderFont(Font headerFont) {
        Font oldFont = this.headerFont;
        this.headerFont = headerFont;
        firePropertyChange("headerFont", oldFont, headerFont);
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public void setBodyFont(Font bodyFont) {
        Font oldFont = this.bodyFont;
        this.bodyFont = bodyFont;
        firePropertyChange("bodyFont", oldFont, bodyFont);
    }

    public Font getBodyFont() {
        return bodyFont;
    }
    
    public void setBodyAlignment(HorizontalAlignment bodyAlignment) {
        HorizontalAlignment oldAlign = this.bodyAlignment;
        this.bodyAlignment = bodyAlignment;
        firePropertyChange("bodyAlignment", oldAlign, bodyAlignment);
    }
    
    public void setBodyFormat(DecimalFormat bodyFormat) {
        DecimalFormat oldFormat = this.bodyFormat;
        this.bodyFormat = bodyFormat;
        firePropertyChange("bodyFormat", oldFormat, bodyFormat);
    }

    public void setSelectedMember(Member selectedMember) {
        this.selectedMember = selectedMember;
        getParent().repaint();
    }
    
    public Member getSelectedMember() {
    	return selectedMember;
    }

    private void setCellSet(CellSet cellSet) {
        this.cellSet = cellSet;
        getParent().repaint();
    }

    @Override
    public ContentBox getParent() {
    	return (ContentBox) super.getParent();
    }
    
    public CellSet getCellSet() {
    	init();
        return cellSet;
    }

    public HorizontalAlignment getBodyAlignment() {
        return bodyAlignment;
    }
    
    public DecimalFormat getBodyFormat() {
        return bodyFormat;
    }
    
    public OlapQuery getModifiedOlapQuery() {
        return modifiedOlapQuery;
    }
    
    public void setModifiedOlapQuery(OlapQuery modifiedOlapQuery) {
    	if (this.modifiedOlapQuery != null) {
    		this.modifiedOlapQuery.removeOlapQueryListener(queryListener);
    		fireChildRemoved(OlapQuery.class, this.modifiedOlapQuery, 0);
    	}
        OlapQuery oldQuery = this.modifiedOlapQuery;
        this.modifiedOlapQuery = modifiedOlapQuery;
        this.modifiedOlapQuery.setParent(this);
        this.modifiedOlapQuery.addOlapQueryListener(queryListener);
        firePropertyChange("modifiedOlapQuery", oldQuery, modifiedOlapQuery);
        fireChildAdded(OlapQuery.class, modifiedOlapQuery, 0);
    }
    
    public String getErrorMessage() {
		return errorMessage;
	}
    
    public List<WabitObject> getDependencies() {
        if (getContent() == null) return Collections.emptyList();
        return new ArrayList<WabitObject>(Collections.singleton(getContent()));
    }
    
    public void removeDependency(WabitObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }

    /**
     * This method will look for a member located at the given point. If a
     * member is found it will be defined to be the selected member. If no
     * member exists at the given point the selected member will be null.
     * 
     * @param p
     *            Used to locate the member to be defined as selected. Cannot be
     *            null. The origin of this point is the top left point of the parent
     *            content box.
     */
    public void setMemberSelectedAtPoint(Point2D p) {
        for (Map.Entry<Member, Set<Rectangle>> entry : memberHeaderMap.entrySet()) {
            for (Rectangle rect : entry.getValue()) {
                if (rect.contains(p)) {
                    setSelectedMember(entry.getKey());
                    return;
                }
            }
        }
        setSelectedMember(null);
    }

    /**
     * If the selected member is not null it will be expanded or collapsed
     * depending on the current state of the member. If the selected member
     * is null this is a no-op.
     */
    public void toggleSelectedMember() {
        if (selectedMember != null) {
            try {
                modifiedOlapQuery.toggleMember(selectedMember);
                modifiedOlapQuery.executeOlapQuery();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }

	public void refresh() {
		//TODO: Implement cellset refresh
	}

    @Override
    protected boolean removeChildImpl(WabitObject child) {
        return false;
    }

}
