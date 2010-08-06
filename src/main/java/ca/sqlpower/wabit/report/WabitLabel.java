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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPLabel;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.SPVariableHelper;
import ca.sqlpower.object.SPVariableResolver;
import ca.sqlpower.wabit.WabitObject;

/**
 * A simple report content item that prints out some text with optional variable
 * substitution. Variables are described in the documentation for the
 * {@link Variables} class.
 */
public class WabitLabel extends SPLabel implements ReportContentRenderer {

    private static final Logger logger = Logger.getLogger(WabitLabel.class);
    
    /**
     * Creates a new label with the given initial text.
     * 
     * @param variableContext
     * @param text
     */
    public WabitLabel(String text) {
        setText(text);
        setName("Label");
        setBackgroundColour(BackgroundColours.DEFAULT_BACKGROUND_COLOUR.getColour());
    }
    
    /**
     * Copy constructor
     */
    public WabitLabel(WabitLabel label) {
    	setText(label.getText());
    	setFont(label.getFont());
    	setHorizontalAlignment(label.getHorizontalAlignment());
    	setBackgroundColour(label.getBackgroundColour());
    	setVerticalAlignment(label.getVerticalAlignment());
    	setName(label.getName());
    }
    
    public WabitLabel() {
        this("");
    }
    
    public Font getFont() {
        if (super.getFont() != null) {
            return super.getFont();
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
    public boolean renderReportContent(
    		Graphics2D g, 
    		double width,
    		double height,
    		double scaleFactor, 
    		int pageIndex, 
    		boolean printing, 
    		SPVariableResolver variablesContext) 
    {
        logger.debug("Rendering label...");
        logger.debug("Text before: " + getText());
        String[] textToRender = getVariableSubstitutedText();
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight() * textToRender.length;
        
        if (getBackgroundColour() != null) {
	        g.setColor(getBackgroundColour());
	        g.fillRect(0, 0, (int)width, (int)height);
	        g.setColor(Color.BLACK);
        }
        logger.debug("Rendering label text: " + Arrays.toString(textToRender));
        double y = getVerticalAlignment().calculateStartY(height, textHeight, fm);
        for (String text : textToRender) {
            int textWidth = fm.stringWidth(text);
            double x = getHorizontalAlignment().computeStartX(width, textWidth);
            g.drawString(text, (int)x, (int)y);
            y += fm.getHeight();
        }
        return false;
    }

    /**
     * Return the Label text with variables substituted.
     */
    public String[] getVariableSubstitutedText() {
    	return SPVariableHelper.substitute(getText(), new SPVariableHelper(this)).split("\n");
	}

    @Override
    public ContentBox getParent() {
        return (ContentBox) super.getParent();
    }
    
    public boolean allowsChildren() {
        return false;
    }

    public int childPositionOffset(Class<? extends SPObject> childType) {
        throw new UnsupportedOperationException("Labels don't have children");
    }

    public List<? extends WabitObject> getChildren() {
        return Collections.emptyList();
    }

    public void resetToFirstPage() {
        // no op -- labels don't paginate
    }

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);
    }

	public void refresh() {
		// Labels don't need refreshing, so no-op
	}

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }
    
    public List<Class<? extends SPObject>> getAllowedChildTypes() {
    	return Collections.emptyList();
    }
    
}
