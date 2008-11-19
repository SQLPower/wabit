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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.FontSelector;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.Variables;
import ca.sqlpower.wabit.WabitObject;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A simple report content item that prints out some text with optional variable
 * substitution. Variables are described in the documentation for the
 * {@link Variables} class.
 */
public class Label extends AbstractWabitObject implements ReportContentRenderer {

    private static final char DOWN_ARROW = '\u25be';
    
    public static enum HorizontalAlignment { LEFT, CENTER, RIGHT }
    public static enum VerticalAlignment { TOP, MIDDLE, BOTTOM }
    
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
     * Creates a new label with the given initial text.
     * 
     * @param variableContext
     * @param text
     */
    public Label(VariableContext variableContext, String text) {
        this.variableContext = variableContext;
        this.text = text;
    }
    
    public Label(VariableContext variableContext) {
        this.variableContext = variableContext;
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
        } else {
            return getParent().getFont();
        }
    }
    
    /**
     * Renders this label to the given graphics, with the baseline centered in the content box.
     */
    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor) {
        String[] textToRender = Variables.substitute(text, variableContext).split("\n");
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight() * textToRender.length;
        
        logger.debug("Rendering label text: " + textToRender);
        
        int y;
        if (vAlignment == VerticalAlignment.TOP) {
            y = fm.getHeight();
        } else if (vAlignment == VerticalAlignment.MIDDLE) {
            y = contentBox.getHeight()/2 - textHeight/2 + fm.getAscent();
        } else if (vAlignment == VerticalAlignment.BOTTOM) {
            y = contentBox.getHeight() - textHeight + fm.getAscent();
        } else {
            throw new IllegalStateException("Unknown vertical alignment: " + vAlignment);
        }
        
        for (String text : textToRender) {
            int textWidth = fm.stringWidth(text);
            int x;
            if (hAlignment == HorizontalAlignment.LEFT) {
                x = 0;
            } else if (hAlignment == HorizontalAlignment.CENTER) {
                x = contentBox.getWidth()/2 - textWidth/2;
            } else if (hAlignment == HorizontalAlignment.RIGHT) {
                x = contentBox.getWidth() - textWidth;
            } else {
                throw new IllegalStateException("Unknown horizontal alignment: " + hAlignment);
            }
            g.drawString(text, x, y);
            y += fm.getHeight();
        }
        return false;
    }
    
    public DataEntryPanel getPropertiesPanel() {
        final Icon LEFT_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_left.png"));
        final Icon RIGHT_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_right.png"));
        final Icon CENTRE_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_center.png"));

        final Icon TOP_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_top.png"));
        final Icon MIDDLE_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_middle.png"));
        final Icon BOTTOM_ALIGN_ICON = new ImageIcon(getClass().getResource("/icons/text_align_bottom.png"));
        
        final DefaultFormBuilder fb = new DefaultFormBuilder(new FormLayout("pref, 4dlu, 250dlu:grow"));
        
        JButton variableButton = new JButton("Variable " + DOWN_ARROW);
        variableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = new JPopupMenu();
                for (String varname : variableContext.getVariableNames()) {
                    menu.add(varname);//(new InsertVariableAction(varname));
                }
                Component invoker = (Component) e.getSource();
                menu.show(invoker, invoker.getHeight(), 0);
            }
        });
        
        ButtonGroup hAlignmentGroup = new ButtonGroup();
        final JToggleButton leftAlign = new JToggleButton(LEFT_ALIGN_ICON, hAlignment == HorizontalAlignment.LEFT);
        hAlignmentGroup.add(leftAlign);
        final JToggleButton centreAlign = new JToggleButton(CENTRE_ALIGN_ICON, hAlignment == HorizontalAlignment.CENTER);
        hAlignmentGroup.add(centreAlign);
        final JToggleButton rightAlign = new JToggleButton(RIGHT_ALIGN_ICON, hAlignment == HorizontalAlignment.RIGHT);
        hAlignmentGroup.add(rightAlign);

        ButtonGroup vAlignmentGroup = new ButtonGroup();
        final JToggleButton topAlign = new JToggleButton(TOP_ALIGN_ICON, vAlignment == VerticalAlignment.TOP);
        vAlignmentGroup.add(topAlign);
        final JToggleButton middleAlign = new JToggleButton(MIDDLE_ALIGN_ICON, vAlignment == VerticalAlignment.MIDDLE);
        vAlignmentGroup.add(middleAlign);
        final JToggleButton bottomAlign = new JToggleButton(BOTTOM_ALIGN_ICON, vAlignment == VerticalAlignment.BOTTOM);
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
        final JTextArea textArea = new JTextArea(text);
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
}
