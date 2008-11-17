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

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.Variables;

/**
 * A simple report content item that prints out some text with optional variable
 * substitution. Variables are described in the documentation for the
 * {@link Variables} class.
 */
public class Label implements ReportContentRenderer {

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
        this.text = text;
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
        hAlignment = alignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return vAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment alignment) {
        vAlignment = alignment;
    }

    /**
     * Renders this label to the given graphics, with the baseline centered in the content box.
     */
    public boolean renderReportContent(Graphics2D g, ContentBox contentBox, double scaleFactor) {
        String[] textToRender = Variables.substitute(text, variableContext).split("\n");
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight() * textToRender.length;
        
        logger.debug("Rendering label text: " + textToRender);
        
        int y;
        if (vAlignment == VerticalAlignment.TOP) {
            y = fm.getHeight();
        } else if (vAlignment == VerticalAlignment.MIDDLE) {
            y = contentBox.getHeight()/2 - textHeight/2 + fm.getAscent();
        } else if (vAlignment == VerticalAlignment.BOTTOM) {
            y = contentBox.getHeight() - textHeight - fm.getDescent();
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
}
