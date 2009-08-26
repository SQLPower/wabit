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

package ca.sqlpower.wabit.swingui.report;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.HorizontalAlignment;
import ca.sqlpower.wabit.report.ReportUtil;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.ReportContentRenderer.BackgroundColours;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;
import ca.sqlpower.wabit.swingui.Icons;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.umd.cs.piccolo.event.PInputEvent;

public class ResultSetSwingRenderer implements SwingContentRenderer {
    
    private static final Logger logger = Logger.getLogger(ResultSetSwingRenderer.class);
    
    private final ResultSetRenderer renderer;
    
    /**
     * Lists of Formatting Options for date
     */
    private final List<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();

    private final ReportLayoutPanel reportLayoutPanel;

    public ResultSetSwingRenderer(ResultSetRenderer renderer, ReportLayoutPanel reportLayoutPanel) {
        this.renderer = renderer;
        this.reportLayoutPanel = reportLayoutPanel;
        setUpFormats();
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

    public DataEntryPanel getPropertiesPanel() {
        FormLayout layout = new FormLayout("pref, 4dlu, fill:pref:grow, 4dlu, pref");
        final DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        // TODO gap (padding) between columns
        // TODO line under header?
        
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.setFont(renderer.getHeaderFont());
        fb.append("Header Font", headerFontExample, ReportUtil.createFontButton(headerFontExample));
        
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(renderer.getBodyFont());
        fb.append("Body Font", bodyFontExample, ReportUtil.createFontButton(bodyFontExample));
        fb.nextLine();

        final JTextField nullStringField = new JTextField(renderer.getNullString());
        fb.append("Null string", nullStringField);
        fb.nextLine();
        
        final JLabel colourLabel = new JLabel(" ");
        colourLabel.setBackground(renderer.getBackgroundColour());
        colourLabel.setOpaque(true);
        final JComboBox colourCombo = new JComboBox();
        colourCombo.setRenderer(new ColorCellRenderer(85, 30));
        for (BackgroundColours bgColour : BackgroundColours.values()) {
            colourCombo.addItem(bgColour.getColour());
        }
        colourCombo.setSelectedItem(renderer.getBackgroundColour());
        colourCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color colour = (Color) colourCombo.getSelectedItem();
                colourLabel.setBackground(colour);
            }
        });
        fb.append("Background", colourLabel, colourCombo);
        fb.nextLine();
        final JComboBox borderComboBox = new JComboBox(BorderStyles.values());
        borderComboBox.setSelectedItem(renderer.getBorderType());
        fb.append("Border", borderComboBox);
        fb.nextLine();
        final JCheckBox grandTotalsCheckBox = new JCheckBox("Grand totals");
        grandTotalsCheckBox.setSelected(renderer.isPrintingGrandTotals());
        fb.append("", grandTotalsCheckBox);
        fb.nextLine();
        
        fb.appendRow("fill:pref");
        Box box = Box.createVerticalBox();
        final List<DataEntryPanel> columnPanels = new ArrayList<DataEntryPanel>();
        final FormLayout columnLayout = new FormLayout(
                "min(pref; 100dlu):grow, 5dlu, min(pref; 100dlu):grow, 5dlu, pref:grow, 5dlu, pref:grow", 
                "pref, pref, pref, pref");
        for (ColumnInfo ci : renderer.getColumnInfoList()) {
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
                renderer.setHeaderFont(headerFontExample.getFont());
                renderer.setBodyFont(bodyFontExample.getFont());
                renderer.setNullString(nullStringField.getText());
                renderer.setBackgroundColour((Color) colourCombo.getSelectedItem());
                renderer.setBorderType((BorderStyles) borderComboBox.getSelectedItem());
                renderer.setPrintingGrandTotals(grandTotalsCheckBox.isSelected());
                
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
    
    /**
     * Helper method for {@link #getPropertiesPanel()}.
     */
    private DataEntryPanel createColumnPropsPanel(FormLayout layout, final ColumnInfo ci) {

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
        fb.append(new JLabel("Breaking and Grouping"));
        
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
        final ButtonGroup breakAndGroupButtons = new ButtonGroup();
        final JRadioButton noBreakOrGroupButton = new JRadioButton("None");
        breakAndGroupButtons.add(noBreakOrGroupButton);
        final JRadioButton breakRadioButton = new JRadioButton("Break Into Sections");
        breakAndGroupButtons.add(breakRadioButton);
        final JCheckBox subtotalCheckbox = new JCheckBox("Subtotal");
        if (ci.getDataType().equals(DataType.NUMERIC)) {
            fb.append(subtotalCheckbox);
            subtotalCheckbox.setSelected(ci.getWillSubtotal());
        } else {
            fb.append("");
        }
        
        final JRadioButton groupRadioButton = new JRadioButton("Group (Suppress Repeating Values)");
        breakAndGroupButtons.add(groupRadioButton);
        if (ci.getWillGroupOrBreak().equals(GroupAndBreak.GROUP)) {
            groupRadioButton.setSelected(true);
        } else if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)) {
            breakRadioButton.setSelected(true);
        } else {
            noBreakOrGroupButton.setSelected(true);
        }
        
        fb.append(noBreakOrGroupButton);
        fb.nextLine();
        fb.append(new JLabel(), 5);
        fb.append(breakRadioButton);
        fb.nextLine();
        fb.append(new JLabel(), 5);
        fb.append(groupRadioButton);
        
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

                if (groupRadioButton.isSelected()) {
                    ci.setWillGroupOrBreak(GroupAndBreak.GROUP);
                } else if (breakRadioButton.isSelected()) {
                    ci.setWillGroupOrBreak(GroupAndBreak.BREAK);
                } else {
                    ci.setWillGroupOrBreak(GroupAndBreak.NONE);
                }
                ci.setWillSubtotal(subtotalCheckbox.isSelected());
                
                renderer.clearResultSetLayout();
                
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
    
    /**
     * Helper method for {@link #createColumnPropsPanel(FormLayout, ColumnInfo)}.
     */
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
    
    /**
     * Helper method for {@link #createColumnPropsPanel(FormLayout, ColumnInfo)}
     */
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

    public void processEvent(PInputEvent event, int type) {
    	if (type == MouseEvent.MOUSE_MOVED) {
            final double mouseXPos = event.getPositionRelativeTo(event.getPickedNode()).getX() - renderer.getParent().getX();
            if (renderer.defineColumnBeingDragged(mouseXPos)) {
                reportLayoutPanel.getCursorManager().dragLineStarted();
            } else {
                reportLayoutPanel.getCursorManager().dragLineFinished();
            }
        } else if (type == MouseEvent.MOUSE_DRAGGED) {
            if (renderer.moveColumnBeingDragged(event.getDelta().getWidth())) {
                event.setHandled(true);
            }
        }
    }

}
