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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.FontSelector;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.Variables;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.swingui.Icons;
import ca.sqlpower.wabit.swingui.InsertVariableButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A simple report content item that prints out some text with optional variable
 * substitution. Variables are described in the documentation for the
 * {@link Variables} class.
 */
public class Label extends AbstractWabitObject implements ReportContentRenderer {

    private static final Logger logger = Logger.getLogger(Label.class);
    
    /**
     * The current text of this label. May include variables encoded as
     * described in the class-level docs.
     */
    private String text;

    private HorizontalAlignment hAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment vAlignment = VerticalAlignment.MIDDLE;
    
    private final VariableContext variableContext;
    
    /**
     * The font that this label is using to display text. If null, getFont()
     * will return the parent content box's font.
     */
    private Font font;

    /**
     * The background colour defined for this label.
     */
	private Color backgroundColour;
    
    /**
     * Creates a new label with the given initial text.
     * 
     * @param variableContext
     * @param text
     */
    public Label(VariableContext variableContext, String text) {
        this.variableContext = variableContext;
        this.text = text;
        setName("Label");
    }
    
    public Label(VariableContext variableContext) {
        this(variableContext, null);
        setName("Label");
    }
    
    /**
     * Sets the new text for this label. The text may include variables as described
     * in the class-level docs of {@link Variables}.
     */
    public void setText(String text) {
        String oldText = this.text;
        this.text = text;
        firePropertyChange("text", oldText, text);
    }
    
    /**
     * Returns the text of this label without substituting the variables.
     */
    public String getText() {
        return text;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return hAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment alignment) {
        HorizontalAlignment oldAlignment = this.hAlignment;
        hAlignment = alignment;
        firePropertyChange("horizontalAlignment", oldAlignment, alignment);
    }

    public VerticalAlignment getVerticalAlignment() {
        return vAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment alignment) {
        VerticalAlignment oldAlignment = vAlignment;
        vAlignment = alignment;
        firePropertyChange("verticalAlignment", oldAlignment, alignment);
    }

    public void setFont(Font font) {
        Font oldFont = this.font;
        this.font = font;
        firePropertyChange("font", oldFont, font);
    }
    
    public Font getFont() {
        if (font != null) {
            return font;
        } else if (getParent() != null) {
            return getParent().getFont();
        } else {
            return null;
        }
    }

	/**
	 * Renders this label to the given graphics, with the baseline centered in
	 * the content box. Note that specifying a pageIndex has no effect, since
	 * Labels are intended to be the same on every page.
	 */
    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor, int pageIndex, boolean printing) {
        logger.debug("Rendering label...");
        logger.debug("Text before: " + text);
        String[] textToRender = getVariableSubstitutedText();
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight() * textToRender.length;
        
        logger.debug("Rendering label text: " + Arrays.toString(textToRender));
        
        int y = vAlignment.calculateStartY(contentBox.getHeight(), textHeight, fm);
        
        for (String text : textToRender) {
            int textWidth = fm.stringWidth(text);
            int x = hAlignment.computeStartX(contentBox.getWidth(), textWidth);
            g.drawString(text, x, y);
            y += fm.getHeight();
        }
        return false;
    }

    /**
     * Return the Label text with variables substituted.
     */
    String[] getVariableSubstitutedText() {
		return Variables.substitute(text, variableContext).split("\n");
	}

    public DataEntryPanel getPropertiesPanel() {
       
        final DefaultFormBuilder fb = new DefaultFormBuilder(new FormLayout("pref, 4dlu, 250dlu:grow"));
        
        final JTextArea textArea = new JTextArea(text);
        JButton variableButton = new InsertVariableButton(variableContext, textArea);
        
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(Icons.LEFT_ALIGN_ICON, hAlignment == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(Icons.CENTRE_ALIGN_ICON, hAlignment == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(Icons.RIGHT_ALIGN_ICON, hAlignment == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);

        ButtonGroup vAlignmentGroup = new ButtonGroup();
        final JToggleButton topAlign = new JToggleButton(Icons.TOP_ALIGN_ICON, vAlignment == VerticalAlignment.TOP);
        vAlignmentGroup.add(topAlign);
        final JToggleButton middleAlign = new JToggleButton(Icons.MIDDLE_ALIGN_ICON, vAlignment == VerticalAlignment.MIDDLE);
        vAlignmentGroup.add(middleAlign);
        final JToggleButton bottomAlign = new JToggleButton(Icons.BOTTOM_ALIGN_ICON, vAlignment == VerticalAlignment.BOTTOM);
        vAlignmentGroup.add(bottomAlign);

        Box alignmentBox = Box.createHorizontalBox();
        alignmentBox.add(leftAlign);
        alignmentBox.add(centreAlign);
        alignmentBox.add(rightAlign);
        alignmentBox.add(Box.createHorizontalStrut(5));
        alignmentBox.add(topAlign);
        alignmentBox.add(middleAlign);
        alignmentBox.add(bottomAlign);
        alignmentBox.add(Box.createHorizontalGlue());
        alignmentBox.add(variableButton);
        fb.append("Alignment", alignmentBox);

        fb.appendRelatedComponentsGapRow();
        fb.nextLine();
        
        fb.appendRow("fill:90dlu:grow");
        fb.nextLine();
        textArea.setFont(getFont());
        JLabel textLabel = fb.append("Text", new JScrollPane(textArea));
        textLabel.setVerticalTextPosition(JLabel.TOP);
        
        fb.nextLine();
        final FontSelector fontSelector = new FontSelector(getFont());
        logger.debug("FontSelector got passed Font " + getFont());
        fontSelector.setShowingPreview(false);
        fontSelector.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                logger.debug("Changing font to: " + fontSelector.getSelectedFont());
                textArea.setFont(fontSelector.getSelectedFont());
            }
        });
        fb.append("Font", fontSelector.getPanel());
        
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
        JPanel colourPanel = new JPanel(new BorderLayout());
        colourPanel.add(colourLabel, BorderLayout.CENTER);
        colourPanel.add(colourCombo, BorderLayout.EAST);
        fb.append("Background", colourPanel);
        
        DataEntryPanel dep = new DataEntryPanel() {

            public boolean applyChanges() {

                fontSelector.applyChanges();
                setFont(fontSelector.getSelectedFont());

                setText(textArea.getText());
                
                if (leftAlign.isSelected()) {
                    setHorizontalAlignment(HorizontalAlignment.LEFT);
                } else if (centreAlign.isSelected()) {
                    setHorizontalAlignment(HorizontalAlignment.CENTER);
                } else if (rightAlign.isSelected()) {
                    setHorizontalAlignment(HorizontalAlignment.RIGHT);
                }

                if (topAlign.isSelected()) {
                    setVerticalAlignment(VerticalAlignment.TOP);
                } else if (middleAlign.isSelected()) {
                    setVerticalAlignment(VerticalAlignment.MIDDLE);
                } else if (bottomAlign.isSelected()) {
                    setVerticalAlignment(VerticalAlignment.BOTTOM);
                }
                
                setBackgroundColour((Color) colourCombo.getSelectedItem());

                return true;
            }

            public void discardChanges() {
                // no op
            }

            public JComponent getPanel() {
                return fb.getPanel();
            }

            public boolean hasUnsavedChanges() {
                return true;
            }
            
        };
        return dep;
    }

    @Override
    public ContentBox getParent() {
        return (ContentBox) super.getParent();
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

    public void resetToFirstPage() {
        // no op -- labels don't paginate
    }

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Color backgroundColour) {
		firePropertyChange("backgroundColour", this.backgroundColour, backgroundColour);
		this.backgroundColour = backgroundColour;
	}
}
