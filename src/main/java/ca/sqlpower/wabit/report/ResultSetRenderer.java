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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
    
    /**
     * Default column width.
     */
    private static final int DEFAULT_COL_WIDTH = 72;
    
    // TODO this should probably be a Wabit Object (child of ResultSetRenderer)
    private static class ColumnInfo {

        String label;
        
        /**
         * Column width in Graphics2D units (screen pixels or 1/72 of an inch when printed).
         */
        int width = DEFAULT_COL_WIDTH;
        
        HorizontalAlignment hAlign = HorizontalAlignment.LEFT;
        
        Format format = null;
        
        public ColumnInfo(String label) {
            setLabel(label);
        }
        
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public int getWidth() {
            return width;
        }
        public void setWidth(int width) {
            this.width = width;
        }
        public HorizontalAlignment getHorizontalAlignment() {
            return hAlign;
        }
        public void setHorizontalAlignment(HorizontalAlignment align) {
            hAlign = align;
        }
        public Format getFormat() {
            return format;
        }
        public void setFormat(Format format) {
            this.format = format;
        }
    }
    
    private final List<ColumnInfo> columnInfo = new ArrayList<ColumnInfo>();
    
    /**
     * The string that will be rendered for null values in the result set.
     */
    private String nullString = "(null)";
    
    private Font headerFont;
    
    private Font bodyFont;
    
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
        setName("Result Set Renderer");
    }

    public ResultSetRenderer(Query query) {
        ResultSet executedRs = null;
        try {
            executedRs = query.execute(); // TODO run in background
            initColumns(executedRs.getMetaData());
        } catch (Exception ex) {
            executeException = ex;
        }
        setName("Result Set Renderer for " + query.getName());
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
            ColumnInfo ci = new ColumnInfo(rsmd.getColumnLabel(col));
            columnInfo.add(ci);
        }
    }

    public void resetToFirstPage() {
        try {
            if (rs != null) rs.beforeFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            ResultSetMetaData rsmd = rs.getMetaData();
            
            g.setFont(getHeaderFont());
            FontMetrics fm = g.getFontMetrics();
            int x = 0;
            int y = fm.getAscent();
            int colCount = rsmd.getColumnCount();
            
            for (int col = 1; col <= colCount; col++) {
                ColumnInfo ci = columnInfo.get(col-1);
                g.drawString(replaceNull(ci.getLabel()), x, y);
                x += ci.getWidth();
            }
            
            y += fm.getHeight();
            g.drawLine(0, y - fm.getHeight()/2, contentBox.getWidth(), y - fm.getHeight()/2);
            
            g.setFont(getBodyFont());
            fm = g.getFontMetrics();
            while ( rs.next() && ((y + fm.getHeight()) < contentBox.getHeight()) ) {
                x = 0;
                y += fm.getHeight();
                
                
                for (int col = 1; col <= colCount; col++) {
                    ColumnInfo ci = columnInfo.get(col-1);
                    String formattedValue = replaceNull(rs.getString(col)); // TODO format
                    int offset = ci.getHorizontalAlignment().computeStartX(
                            ci.getWidth(), fm.stringWidth(formattedValue));
                    g.drawString(formattedValue, x + offset, y); // TODO clip and/or line wrap and/or warn
                    x += ci.getWidth();
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

    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        throw new UnsupportedOperationException("Labels don't have children");
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
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
        
        final JTextField columnLabel = new JTextField(ci.getLabel());
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
        
        // TODO data type
        
        // TODO formatter
        
        final JPanel panel = fb.getPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 3, 5));
        
        return new DataEntryPanel() {

            public boolean applyChanges() {
                ci.setLabel(columnLabel.getText());
                if (leftAlign.isSelected()) {
                    ci.setHorizontalAlignment(HorizontalAlignment.LEFT);
                } else if (centreAlign.isSelected()) {
                    ci.setHorizontalAlignment(HorizontalAlignment.CENTER);
                } else if (rightAlign.isSelected()) {
                    ci.setHorizontalAlignment(HorizontalAlignment.RIGHT);
                }
                ci.setWidth((Integer) widthSpinner.getValue());
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
