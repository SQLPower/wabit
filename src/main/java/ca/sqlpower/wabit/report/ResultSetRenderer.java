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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.FontSelector;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.Query;
import ca.sqlpower.wabit.QueryException;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.swingui.Icons;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Renders a JDBC result set using configurable absolute column widths.
 */
public class ResultSetRenderer extends AbstractWabitObject implements ReportContentRenderer {

    private static final Logger logger = Logger.getLogger(ResultSetRenderer.class);
    
    private static final String defaultFormatString = "Default Format";
    
    private static DataType getDataType(String className) {
    	try {
			return getDataType(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Invalid class Name cannot find class",e);
		}
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
    	return null;
    	
    }
    
    private final List<ColumnInfo> columnInfo = new ArrayList<ColumnInfo>();
    
    /**
     * Lists of Formatting Options for number and date
     */
    private final List<DecimalFormat> numberFormats = new ArrayList<DecimalFormat>();
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
    private final Query query;
    
    /**
     * A cached copy of the result set that came from the Query object.
     * TODO: dump this when the query changes, (delay re-executing it until it's needed again)  
     */
    private final ResultSet rs;

    /**
     * If the query fails to execute, the corresponding exception will be saved here and
     * rendered instead of the results.
     */
    private Exception executeException;

    /**
	 * A list containing the ResultSet row numbers that each page starts with.
	 * The List index corresponds with the page index.
	 */
    private List<Integer> pageRowNumberList = new ArrayList<Integer>();
    
    public ResultSetRenderer(Query query) {
    	this(query, Collections.EMPTY_LIST);
    }
    
    public ResultSetRenderer(Query query, List<ColumnInfo> columnInfoList) {
        this.query = query;
        // TODO listen to query for changes
        ResultSet executedRs = null;
        setUpFormats();
        try {
            executedRs = query.execute(); // TODO run in background
            initColumns(executedRs.getMetaData(), columnInfoList);
        } catch (Exception ex) {
            executeException = ex;
        }
        setName("Result Set: " + query.getName());
        rs = executedRs;
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
    	
    	numberFormats.add(new DecimalFormat("#,##0.00"));
    	numberFormats.add(new DecimalFormat("#,##0.00%"));
    	numberFormats.add(new DecimalFormat("(#,000.00)"));
    	numberFormats.add(new DecimalFormat("(#,000)"));
    	numberFormats.add(new DecimalFormat("##0.##E0"));
    	numberFormats.add((DecimalFormat)NumberFormat.getCurrencyInstance());
    	numberFormats.add((DecimalFormat)NumberFormat.getInstance());
    	numberFormats.add((DecimalFormat)NumberFormat.getPercentInstance());
    }
    
    private Format getFormat(DataType dataType, String pattern){
    	logger.debug("dataType is"+ dataType+ " pattern is "+ pattern);
    	if(dataType == DataType.NUMERIC) {
    		for(DecimalFormat decimalFormat : numberFormats) {
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
	 * @param columnInfoList
	 *            The list of column information for the result set. This allows
	 *            defining column information from a load.
	 * @throws SQLException
	 *             If the resultset metadata methods fail.
	 */
    private void initColumns(ResultSetMetaData rsmd, List<ColumnInfo> columnInfoList) throws SQLException {
    	Map<String, ColumnInfo> colKeyToInfoMap = new HashMap<String, ColumnInfo>();
    	for (ColumnInfo info : columnInfoList) {
    		logger.debug("Loaded key " + info.getColumnInfoKey());
    		colKeyToInfoMap.put(info.getColumnInfoKey(), info);
    	}
        for (int col = 1; col <= rsmd.getColumnCount(); col++) {
        	logger.debug(rsmd.getColumnClassName(col));
        	String columnKey = rsmd.getColumnLabel(col);
        	logger.debug("Matching key " + columnKey);
        	ColumnInfo ci;
        	if (colKeyToInfoMap.get(columnKey) != null) {
        		ci = colKeyToInfoMap.get(columnKey);
        	} else {
        		ci = new ColumnInfo(columnKey);
        	}
            ci.setDataType(ResultSetRenderer.getDataType(rsmd.getColumnClassName(col)));
            ci.setParent(ResultSetRenderer.this);
            columnInfo.add(ci);
            fireChildAdded(ColumnInfo.class, ci, col-1);
            
        }
    }

    public void resetToFirstPage() {
        try {
            if (rs != null) rs.beforeFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex) {
        if (executeException != null) {
            return renderFailure(g, contentBox, scaleFactor, pageIndex);
        } else {
            return renderSuccess(g, contentBox, scaleFactor, pageIndex);
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

    public boolean renderSuccess(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex) {
        try {
        	if (pageIndex >= pageRowNumberList.size() || pageRowNumberList.get(pageIndex) == null) {
        		while (pageRowNumberList.size() < pageIndex) {
        			pageRowNumberList.add(null);
        		}
        		pageRowNumberList.add(rs.getRow());
        	}
        	
        	int pageToSet = pageRowNumberList.get(pageIndex);
        	rs.absolute(pageRowNumberList.get(pageIndex));
   			
        	ResultSetMetaData rsmd = rs.getMetaData();
            
            g.setFont(getHeaderFont());
            FontMetrics fm = g.getFontMetrics();
            int x = 0;
            int y = fm.getAscent();
            int colCount = rsmd.getColumnCount();
            
            for (int col = 1; col <= colCount; col++) {
                ColumnInfo ci = columnInfo.get(col-1);
                g.drawString(replaceNull(ci.getName()), x, y);
                x += ci.getWidth();
            }
            
            y += fm.getHeight();
            g.drawLine(0, y - fm.getHeight()/2, contentBox.getWidth(), y - fm.getHeight()/2);
            
            g.setFont(getBodyFont());
            fm = g.getFontMetrics();
            
            List<String> lastRenderedRow = new ArrayList<String>();
            Map<ColumnInfo, Double> subtotalForCols = new HashMap<ColumnInfo, Double>();
            while ( rs.next() && ((y + fm.getHeight()) < contentBox.getHeight()) ) {
            	List<String> renderedRow = new ArrayList<String>();
            	
                x = 0;
                
                for (int col = 1; col <= colCount; col++) {
                    ColumnInfo ci = columnInfo.get(col-1);
                    Object value = rs.getObject(col);
                    String formattedValue;
                    if (ci.getFormat() != null && value != null) {
                    	logger.debug("Format iss:"+ ci.getFormat()+ "string is:"+ rs.getString(col));
                    	formattedValue = ci.getFormat().format(value);
                    } else {
                    	formattedValue = replaceNull(rs.getString(col));
                    }
                    renderedRow.add(formattedValue);
                }
                
                for (int i = 0; i < colCount; i++) {
                	ColumnInfo ci = columnInfo.get(i);
                	if (ci.getWillBreak() && i < lastRenderedRow.size() && !lastRenderedRow.get(i).equals(renderedRow.get(i))) {
                		y += fm.getHeight();
                		y = renderSubtotals(g, fm, y, colCount,
                				subtotalForCols);
                		g.drawLine(0, y - fm.getHeight()/2, contentBox.getWidth(), y - fm.getHeight()/2);
                		subtotalForCols.clear();
                		break;
                	}
                }
                
                y += fm.getHeight();
                lastRenderedRow.clear();
                for (int col = 0; col < colCount; col++) {
                	ColumnInfo ci = columnInfo.get(col);
                	String formattedValue = renderedRow.get(col);
                	int offset = ci.getHorizontalAlignment().computeStartX(
                			ci.getWidth(), fm.stringWidth(formattedValue));
                	g.drawString(formattedValue, x + offset, y); // TODO clip and/or line wrap and/or warn
                	x += ci.getWidth();
                	lastRenderedRow.add(formattedValue);
                	if (ci.getDataType().equals(DataType.NUMERIC)) {
                		try {
                			if (subtotalForCols.get(ci) == null) {
                				subtotalForCols.put(ci, Double.parseDouble(rs.getString(col + 1)));
                			} else {
                				subtotalForCols.put(ci, subtotalForCols.get(ci) + Double.parseDouble(formattedValue));
                			}
                		} catch (NumberFormatException e) {
                			//If the formatted value is null then the parse of a double
                			//will fail. We will not sum null values and treat them as 0.
                		}
                	}
                }
            }
            y += fm.getHeight();
            renderSubtotals(g, fm, y, colCount, subtotalForCols);
            return !rs.isAfterLast();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * This will render the subtotals at the current y position.
     * @param g The graphic to render the subtotals to.
     * @param fm The current font metrics.
     * @param y The y position for the subtotals to start at.
     * @param colCount The number of columns that exist in the result set.
     * @param subtotalForCols A map that maps column info to their subtotal if they have one.
     * @return The new y position after the subtotal rows.
     */
	private int renderSubtotals(Graphics2D g, FontMetrics fm, int y,
			int colCount, Map<ColumnInfo, Double> subtotalForCols) {
		if (!subtotalForCols.isEmpty()) {
			int localX = 0;

			boolean firstSubtotal = true;
			for (int subCol = 0; subCol < colCount; subCol++) {
				ColumnInfo colInfo = columnInfo.get(subCol);
				Double subtotal = subtotalForCols.get(colInfo);
				if (colInfo.getWillSubtotal() && subtotal != null) {
					if (firstSubtotal) {
						y += fm.getHeight();
						g.setFont(getHeaderFont());
						g.drawString("Subtotal", 0, y);
						y += g.getFontMetrics().getHeight();
						g.setFont(getBodyFont());
						firstSubtotal = false;
					}
					String formattedValue;
					if (colInfo.getFormat() != null) {
						formattedValue = colInfo.getFormat().format(subtotal);
					} else {
						formattedValue = subtotal.toString();
					}
					int offset = colInfo.getHorizontalAlignment().computeStartX(
							colInfo.getWidth(), fm.stringWidth(formattedValue));
					g.drawString(formattedValue, localX + offset, y); // TODO clip and/or line wrap and/or warn
				}
				localX += colInfo.getWidth();
			}
			y += fm.getHeight();
		}
		return y;
	}

    private String replaceNull(String string) {
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
        return columnInfo.indexOf(childType);
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
    
    public Query getQuery() {
        return query;
    }
    
    public DataEntryPanel getPropertiesPanel() {
        FormLayout layout = new FormLayout("pref, 4dlu, fill:pref:grow, 4dlu, pref");
        final DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        // TODO gap (padding) between columns
        // TODO line under header?
        
        // TODO header font
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.setFont(getHeaderFont());
        fb.append("Header Font", headerFontExample, createFontButton(headerFontExample));
        
        // TODO body font
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(getBodyFont());
        fb.append("Body Font", bodyFontExample, createFontButton(bodyFontExample));
        fb.nextLine();

        // TODO null string (per column?)
        final JTextField nullStringField = new JTextField(nullString);
        fb.append("Null string", nullStringField);
        fb.nextLine();
        
        fb.appendRow("fill:pref");
        Box box = Box.createHorizontalBox();
        final List<DataEntryPanel> columnPanels = new ArrayList<DataEntryPanel>();
        for (ColumnInfo ci : columnInfo) {
            DataEntryPanel columnPropsPanel = createColumnPropsPanel(ci);
            columnPanels.add(columnPropsPanel);
            box.add(columnPropsPanel.getPanel());
            box.add(Box.createHorizontalStrut(5));
        }
        JScrollPane columnScrollPane = new JScrollPane(box,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        columnScrollPane.setPreferredSize(new Dimension(600, columnScrollPane.getPreferredSize().height));
        fb.append("Column info", columnScrollPane, 3);
        
        return new DataEntryPanel() {

            public boolean applyChanges() {
                setHeaderFont(headerFontExample.getFont());
                setBodyFont(bodyFontExample.getFont());
                setNullString(nullStringField.getText());
                
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
    
    public DataEntryPanel createColumnPropsPanel(final ColumnInfo ci) {

        FormLayout layout = new FormLayout("fill:pref:grow");
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        final JTextField columnLabel = new JTextField(ci.getName());
        fb.append(columnLabel);
        
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
        
        // TODO better UI (auto/manual, and manual is based on a jtable with resizable headers)
        final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(ci.getWidth(), 0, 1000, 12));
        fb.append(widthSpinner);
        
        final JComboBox dataTypeComboBox = new JComboBox(DataType.values());
        final JComboBox formatComboBox = new JComboBox();
        
        dataTypeComboBox.setSelectedItem(ci.getDataType());

        fb.append(dataTypeComboBox);
       
        if(dataTypeComboBox.getSelectedItem() == DataType.TEXT) {
        	formatComboBox.setEnabled(false);
        } else {
        	setItemforFormatComboBox(formatComboBox, (DataType)dataTypeComboBox.getSelectedItem());
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
                		((String)formatComboBox.getSelectedItem()).equals(defaultFormatString)) {
                		ci.setFormat(null);
                	}
                else {
                	ci.setFormat(getFormat(ci.getDataType(), (String)formatComboBox.getSelectedItem()));
                }
                ci.setWidth((Integer) widthSpinner.getValue());
                
                ci.setWillBreak(breakCheckbox.isSelected());
                ci.setWillSubtotal(subtotalCheckbox.isSelected());
                
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
    	combobox.addItem(defaultFormatString);
    	if(dataType == DataType.NUMERIC) {
    		for(NumberFormat item : numberFormats) {
    			combobox.addItem(((DecimalFormat)item).toPattern());
    		}
    	} else if(dataType == DataType.DATE) {
    		for(DateFormat item : dateFormats) {
    			combobox.addItem(((SimpleDateFormat)item).toPattern());
    		}
    	}
    	
    }

    private JButton createFontButton(final JComponent fontTarget) {
        JButton button = new JButton("Choose...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FontSelector fs = new FontSelector(fontTarget.getFont());
                Window dialogParent = SwingUtilities.getWindowAncestor(fontTarget);
                JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(fs, dialogParent, "Choose a Font", "OK");
                d.setModal(true);
                d.setVisible(true);
                fontTarget.setFont(fs.getSelectedFont());
            }
        });
        return button;
    }
}
