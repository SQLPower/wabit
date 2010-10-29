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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.apache.log4j.Logger;

import ca.sqlpower.object.HorizontalAlignment;
import ca.sqlpower.swingui.AlignmentIcons;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.wabit.report.ColumnInfo;
import ca.sqlpower.wabit.report.DataType;
import ca.sqlpower.wabit.report.ReportUtil;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.ColumnInfo.GroupAndBreak;
import ca.sqlpower.wabit.report.ResultSetRenderer.BorderStyles;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.Font;

import edu.umd.cs.piccolo.event.PInputEvent;

public class ResultSetSwingRenderer implements SwingContentRenderer {
    
    private static final Logger logger = Logger.getLogger(ResultSetSwingRenderer.class);
    
    private final ResultSetRenderer renderer;
    
    /**
     * Lists of Formatting Options for date
     */
    private final List<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();
    
    private final List<DecimalFormat> numberFormats = ReportUtil.getNumberFormats();

    private final LayoutPanel reportLayoutPanel;

    public ResultSetSwingRenderer(ResultSetRenderer renderer, LayoutPanel reportLayoutPanel) {
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
        
    	FormLayout layout = new FormLayout("20dlu, 4dlu, pref, 4dlu, 300dlu:grow, 4dlu, pref");
        final DefaultFormBuilder fb = new DefaultFormBuilder(layout);
       
        
        
        final JLabel visOptionsLabel = new JLabel("Visual");
        visOptionsLabel.setFont(visOptionsLabel.getFont().deriveFont(Font.BOLD));
        fb.append(visOptionsLabel,7);
        fb.nextLine();
        fb.append("");
        
        
        final JLabel headerFontExample = new JLabel("Header Font Example");
        headerFontExample.setFont(renderer.getHeaderFont());
        fb.append("Headers Font", headerFontExample, ReportUtil.createFontButton(headerFontExample, renderer));
        fb.nextLine();
        fb.append("");
        
        final JLabel bodyFontExample = new JLabel("Body Font Example");
        bodyFontExample.setFont(renderer.getBodyFont());
        fb.append("Body Font", bodyFontExample, ReportUtil.createFontButton(bodyFontExample, renderer));
        fb.nextLine();
        fb.append("");
        


        final JLabel backgroundColourLabel = new JLabel("  ");
        final JButton backgroundColorPickerButton = new JButton("Choose...");
        backgroundColourLabel.setOpaque(true);
        backgroundColourLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        backgroundColourLabel.setBackground(renderer.getBackgroundColour() == null ? Color.WHITE : renderer.getBackgroundColour());
        backgroundColourLabel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				// no op
			}
			public void mousePressed(MouseEvent e) {
				// no op
			}
			public void mouseExited(MouseEvent e) {
				// no op
			}
			public void mouseEntered(MouseEvent e) {
				// no op
			}
			public void mouseClicked(MouseEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  backgroundColorPickerButton,
		                  "Choose a background color", 
		                  backgroundColourLabel.getBackground());
		        if (c != null) {
		        	backgroundColourLabel.setBackground(c);
		        }
			}
		});
        backgroundColorPickerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  backgroundColorPickerButton,
		                  "Choose a background color", 
		                  backgroundColourLabel.getBackground());
		        if (c != null) {
		        	backgroundColourLabel.setBackground(c);
		        }
			}
		});
        
        fb.append("Background color", backgroundColourLabel, backgroundColorPickerButton);
        fb.append("");
        fb.nextLine();
        fb.append("");
        
        
        
        
        
        
        final JLabel headerColourLabel = new JLabel("  ");
        final JButton headerColorPickerButton = new JButton("Choose...");
        headerColourLabel.setOpaque(true);
        headerColourLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        headerColourLabel.setBackground(renderer.getHeaderColour() == null ? Color.WHITE : renderer.getHeaderColour());
        headerColourLabel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				// no op
			}
			public void mousePressed(MouseEvent e) {
				// no op
			}
			public void mouseExited(MouseEvent e) {
				// no op
			}
			public void mouseEntered(MouseEvent e) {
				// no op
			}
			public void mouseClicked(MouseEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  headerColorPickerButton,
		                  "Choose a header color", 
		                  headerColourLabel.getBackground());
		        if (c != null) {
		        	headerColourLabel.setBackground(c);
		        }
			}
		});
        headerColorPickerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  headerColorPickerButton,
		                  "Choose a header color", 
		                  headerColourLabel.getBackground());
		        if (c != null) {
		        	headerColourLabel.setBackground(c);
		        }
			}
		});
        
        fb.append("Headers color", headerColourLabel, headerColorPickerButton);
        fb.append("");
        fb.nextLine();
        fb.append("");
        
        
        
        
        final JLabel dataColourLabel = new JLabel("  ");
        final JButton dataColorPickerButton = new JButton("Choose...");
        dataColourLabel.setOpaque(true);
        dataColourLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dataColourLabel.setBackground(renderer.getDataColour() == null ? Color.WHITE : renderer.getDataColour());
        dataColourLabel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				// no op
			}
			public void mousePressed(MouseEvent e) {
				// no op
			}
			public void mouseExited(MouseEvent e) {
				// no op
			}
			public void mouseEntered(MouseEvent e) {
				// no op
			}
			public void mouseClicked(MouseEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  dataColorPickerButton,
		                  "Choose a data color", 
		                  dataColourLabel.getBackground());
		        if (c != null) {
		        	dataColourLabel.setBackground(c);
		        }
			}
		});
        dataColorPickerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c;
		        c = JColorChooser.showDialog(
		                  dataColorPickerButton,
		                  "Choose a data color", 
		                  dataColourLabel.getBackground());
		        if (c != null) {
		        	dataColourLabel.setBackground(c);
		        }
			}
		});
        
        fb.append("Data cells color", dataColourLabel, dataColorPickerButton);
        fb.append("");
        fb.nextLine();
        fb.append("");
        
        
        
        
        final JComboBox borderComboBox = new JComboBox(BorderStyles.values());
        borderComboBox.setSelectedItem(renderer.getBorderType());
        fb.append("Border", borderComboBox);
        fb.nextLine();
        fb.append("");
        
        
        fb.appendUnrelatedComponentsGapRow();
        fb.nextLine();
        
        final JLabel dataOptionsLabel = new JLabel("Data");
        dataOptionsLabel.setFont(dataOptionsLabel.getFont().deriveFont(Font.BOLD));
        fb.append(dataOptionsLabel,7);
        fb.nextLine();
        fb.append("");
        

        final JTextField nullStringField = new JTextField(renderer.getNullString());
        fb.append("Null string", nullStringField);
        fb.nextLine();
        fb.append("");
        
        final JCheckBox grandTotalsCheckBox = new JCheckBox("Add Grand totals");
        grandTotalsCheckBox.setSelected(renderer.isPrintingGrandTotals());
        fb.append("", grandTotalsCheckBox);
        fb.nextLine();
        
        
        
        fb.appendUnrelatedComponentsGapRow();
        final JLabel columnOptionsLabel = new JLabel("Columns");
        columnOptionsLabel.setFont(columnOptionsLabel.getFont().deriveFont(Font.BOLD));
        fb.append(columnOptionsLabel,7);
        fb.nextLine();
        fb.append("");
        
        
        
		JTabbedPane tabbedPane = new JTabbedPane();
	    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	    final List<DataEntryPanel> columnPanels = new ArrayList<DataEntryPanel>();
		for (ColumnInfo ci : renderer.getColumnInfoList()) {
			DataEntryPanel dep = createColumnPropsPanel(ci);
			columnPanels.add(dep);
			tabbedPane.add(ci.getName(), dep.getPanel());
		}
		
		fb.append(tabbedPane, 5);
		fb.nextLine();
		fb.appendUnrelatedComponentsGapRow();
	   
        return new DataEntryPanel() {

            public boolean applyChanges() {
                renderer.setHeaderFont(headerFontExample.getFont());
                renderer.setBodyFont(bodyFontExample.getFont());
                renderer.setNullString(nullStringField.getText());
                renderer.setBackgroundColour(backgroundColourLabel.getBackground());
                renderer.setDataColour(dataColourLabel.getBackground());
                renderer.setHeaderColour(headerColourLabel.getBackground());
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
    private DataEntryPanel createColumnPropsPanel(final ColumnInfo ci) {

    	final FormLayout layout = new FormLayout(
                "80dlu, 10dlu, min(pref; 100dlu):grow, 4dlu, pref:grow, 10dlu, pref:grow");
    	
        DefaultFormBuilder fb = new DefaultFormBuilder(layout);
        
        
        final JTextField columnLabel = new JTextField(ci.getName());
        
        final JLabel widthLabel = new JLabel("Size");
        final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(ci.getWidth(), 0, Integer.MAX_VALUE, 12));
        
        final JComboBox dataTypeComboBox = new JComboBox(DataType.values());
        final JLabel dataTypeLabel = new JLabel("Type");
        
        final JComboBox formatComboBox = new JComboBox();
        formatComboBox.setEditable(true);
        final JLabel formatLabel = new JLabel("Format");
        
        
        final JCheckBox subtotalCheckbox = new JCheckBox("Print Subtotals");
        
        final JLabel linkingLabel = new JLabel("Link to report");
        final JComboBox linkingBox = new JComboBox();
        // XXX Enable only when this is working.
        linkingBox.setEnabled(false);
        
        final ButtonGroup breakAndGroupButtons = new ButtonGroup();
        final JRadioButton noBreakOrGroupButton = new JRadioButton("None");
        breakAndGroupButtons.add(noBreakOrGroupButton);
        final JRadioButton breakRadioButton = new JRadioButton("Break Into Sections");
        breakAndGroupButtons.add(breakRadioButton);
        final JRadioButton pageBreakRadioButton = new JRadioButton("Break Into Sections (page break)");
        breakAndGroupButtons.add(pageBreakRadioButton);
        final JRadioButton groupRadioButton = new JRadioButton("Group (Suppress Repeating Values)");
        breakAndGroupButtons.add(groupRadioButton);
        
        
        final JLabel alignmentLabel = new JLabel("Alignment");
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(
        		AlignmentIcons.LEFT_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(
        		AlignmentIcons.CENTRE_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(
        		AlignmentIcons.RIGHT_ALIGN_ICON, ci.getHorizontalAlignment() == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);
        Box alignmentBox = Box.createHorizontalBox();
        alignmentBox.add(leftAlign);
        alignmentBox.add(centreAlign);
        alignmentBox.add(rightAlign);
        alignmentBox.add(Box.createHorizontalGlue());
        
        
        
        /*
         * ROW 1 - Caption text field + two labels
         */
        JLabel captionLabel = new JLabel("Caption");
        captionLabel.setFont(captionLabel.getFont().deriveFont(Font.BOLD));
        fb.append(captionLabel);
        JLabel visLabel = new JLabel("Visual options");
        visLabel.setFont(visLabel.getFont().deriveFont(Font.BOLD));
        fb.append(visLabel,3);
        JLabel advLabel = new JLabel("Advanced options");
        advLabel.setFont(advLabel.getFont().deriveFont(Font.BOLD));
        fb.append(advLabel);
        fb.nextLine();
        
        
        /*
         * Separator row
         */
        fb.append(new JSeparator(), 7);
        fb.nextLine();
        
        
        /*
         * Row 2
         */
        fb.append(columnLabel);
        fb.append(widthLabel);
        fb.append(widthSpinner);
        fb.append(subtotalCheckbox);
        subtotalCheckbox.setSelected(ci.getWillSubtotal());
        if (ci.getDataType().equals(DataType.NUMERIC)) {
        	subtotalCheckbox.setEnabled(true);
        } else {
        	subtotalCheckbox.setEnabled(false);
        }
        fb.nextLine();
        
        
        
        
        /*
         * Row 3
         */
        fb.append(new JLabel());
        fb.append(dataTypeLabel);
        fb.append(dataTypeComboBox);
        fb.append(linkingLabel);
        fb.nextLine();
        

        
        /*
         * Row 4
         */
        fb.append(new JLabel());
        fb.append(formatLabel);
        fb.append(formatComboBox);
        fb.append(linkingBox);
        fb.nextLine();
        
        
        
        /*
         * Row 5
         */
        fb.append(new JLabel());
        fb.append(alignmentLabel);
        fb.append(alignmentBox);
        fb.append(new JLabel("Breaking and Grouping"));
        fb.nextLine();
        
        
        
        /*
         * Row 6
         */
        fb.append(new JLabel(), 5);
        fb.append(noBreakOrGroupButton);
        fb.nextLine();
        
        
        
        /*
         * Row 7
         */
        fb.append(new JLabel(), 5);
        fb.append(breakRadioButton);
        fb.nextLine();
        
        

        /*
         * Row 8
         */
        fb.append(new JLabel(), 5);
        fb.append(pageBreakRadioButton);
        fb.nextLine();
        
        
        /*
         * spacer
         */
        fb.append(new JLabel(), 5);
        fb.append(groupRadioButton);
        fb.nextLine();
        
        
        dataTypeComboBox.setSelectedItem(ci.getDataType());
        if(dataTypeComboBox.getSelectedItem() == DataType.TEXT) {
            formatComboBox.setEnabled(false);
        } else {
            setItemforFormatComboBox(formatComboBox, (DataType)dataTypeComboBox.getSelectedItem());
            if (ci.getFormat() != null) {
            	this.setOrInsertFormat(
            			formatComboBox,
            			ci.getFormat());                
            }
        }
        
        dataTypeComboBox.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                if(((JComboBox)e.getSource()).getSelectedItem() == DataType.TEXT){
                    formatComboBox.setEnabled(false);
                    subtotalCheckbox.setEnabled(false);
                } else if(((JComboBox)e.getSource()).getSelectedItem() == DataType.DATE){
                    formatComboBox.setEnabled(true);
                    subtotalCheckbox.setEnabled(false);
                } else if(((JComboBox)e.getSource()).getSelectedItem() == DataType.NUMERIC){
                	formatComboBox.setEnabled(true);
                    subtotalCheckbox.setEnabled(true);
                }
                setItemforFormatComboBox(formatComboBox, (DataType)dataTypeComboBox.getSelectedItem());
            }
        });

        
        if (ci.getWillGroupOrBreak().equals(GroupAndBreak.GROUP)) {
            groupRadioButton.setSelected(true);
        } else if (ci.getWillGroupOrBreak().equals(GroupAndBreak.BREAK)) {
            breakRadioButton.setSelected(true);
        } else if (ci.getWillGroupOrBreak().equals(GroupAndBreak.PAGEBREAK)) {
            pageBreakRadioButton.setSelected(true);
        } else {
            noBreakOrGroupButton.setSelected(true);
        }
        
        // XXX Enable only when this is working
//        for (Report report : WabitUtils.getWorkspace(ci).getReports()) {
//        	linkingBox.addItem(report);
//        }
//        linkingBox.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				// TODO populate and add a listener to the linking box
//			}
//		});
        
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
                
                if (((DataType)dataTypeComboBox.getSelectedItem()).equals(DataType.TEXT) ||
                		(formatComboBox.getSelectedItem() != null &&
                        ((String)formatComboBox.getSelectedItem()).equals(ReportUtil.DEFAULT_FORMAT_STRING))) {
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
                } else if (pageBreakRadioButton.isSelected()) {
                    ci.setWillGroupOrBreak(GroupAndBreak.PAGEBREAK);
                } else {
                    ci.setWillGroupOrBreak(GroupAndBreak.NONE);
                }
                ci.setWillSubtotal(subtotalCheckbox.isSelected());
                
                renderer.refresh();
                
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
    
    private void setOrInsertFormat(
    		JComboBox formatComboBox, 
    		Format format) {
		
    	final String pattern;
    	if (format instanceof SimpleDateFormat) {
    		pattern = ((SimpleDateFormat)format).toPattern();
    	} else if (format instanceof DecimalFormat) {
    		pattern = ((DecimalFormat)format).toPattern();
    	} else {
    		throw new AssertionError("Unknown format class.");
    	}
    	
    	for (int i = 0; i < formatComboBox.getItemCount(); i++) {
    		String currentPattern = (String)formatComboBox.getItemAt(i);
    		if (currentPattern.equals(pattern)) {
    			// It's already there.
    			formatComboBox.setSelectedIndex(i);
    			return;
    		}
    	}
    	
    	// It was not in the list before. Add and select.
    	formatComboBox.addItem(pattern);
    	formatComboBox.setSelectedItem(pattern);
	}

	/**
     * Helper method for {@link #createColumnPropsPanel(FormLayout, ColumnInfo)}.
     */
    private void setItemforFormatComboBox(JComboBox combobox, DataType dataType) {
        combobox.removeAllItems();
        combobox.addItem(ReportUtil.DEFAULT_FORMAT_STRING);
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
            // Could not find it. Let's create one.
            return new DecimalFormat(pattern);
        } else if(dataType == DataType.DATE) {
            for(SimpleDateFormat dateFormat: dateFormats) {
                if((dateFormat.toPattern()).equals(pattern)){
                    return dateFormat;
                }
            }
            // Could not find it. Let's create one.
            return new SimpleDateFormat(pattern);
        } else {
        	throw new AssertionError("Unknown expected format.");
        }
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
