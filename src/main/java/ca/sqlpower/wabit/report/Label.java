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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.Variables;
import ca.sqlpower.wabit.WabitObject;

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
    
    private VariableContext variableContext;
    
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
    public Label(String text) {
        this.text = text;
        setName("Label");
        setBackgroundColour(BackgroundColours.DEFAULT_BACKGROUND_COLOUR.getColour());
    }
    
    /**
     * Copy constructor
     */
    public Label(Label label) {
    	this.text = label.getText();
    	this.font = label.getFont();
    	this.hAlignment = label.getHorizontalAlignment();
    	this.backgroundColour = label.getBackgroundColour();
    	this.vAlignment = label.getVerticalAlignment();
    	setName(label.getName());
    }
    
    public Label() {
        this("");
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
        Font oldFont = getFont();
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
     * ONLY USED FOR TESTING
     */
    public void setVariableContext(VariableContext variableContext) {
		this.variableContext = variableContext;
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
        
        if (getBackgroundColour() != null) {
	        g.setColor(getBackgroundColour());
	        g.fillRect(0, 0, (int) contentBox.getWidth(), (int) contentBox.getHeight());
	        g.setColor(Color.BLACK);
        }
        logger.debug("Rendering label text: " + Arrays.toString(textToRender));
        int y = vAlignment.calculateStartY((int) contentBox.getHeight(), textHeight, fm);
        for (String text : textToRender) {
            int textWidth = fm.stringWidth(text);
            int x = hAlignment.computeStartX((int) contentBox.getWidth(), textWidth);
            g.drawString(text, x, y);
            y += fm.getHeight();
        }
        return false;
    }

    /**
     * Return the Label text with variables substituted.
     */
    String[] getVariableSubstitutedText() {
		return Variables.substitute(text, getVariableContext()).split("\n");
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

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Color backgroundColour) {
		firePropertyChange("backgroundColour", this.backgroundColour, backgroundColour);
		this.backgroundColour = backgroundColour;
	}

    public List<WabitObject> getDependencies() {
        return Collections.emptyList();
    }
    
    public void removeDependency(SPObject dependency) {
        ((ContentBox) getParent()).setContentRenderer(null);        
    }

    //XXX Should the getter be setting the property it is getting?
    public VariableContext getVariableContext() {
    	variableContext = ((Layout)getParent().getParent().getParent()).getVarContext(); //XXX not good.
        return variableContext;
    }

	public void refresh() {
		// Labels don't need refreshing, so no-op
	}

    @Override
    protected boolean removeChildImpl(SPObject child) {
        return false;
    }

	public List<Class<? extends SPObject>> allowedChildTypes() {
		return Collections.emptyList();
	}
    
}
