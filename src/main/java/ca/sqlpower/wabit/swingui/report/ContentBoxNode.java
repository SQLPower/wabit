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

package ca.sqlpower.wabit.swingui.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PPaintContext;

public class ContentBoxNode extends PNode implements ReportNode {

    private static final Logger logger = Logger.getLogger(ContentBoxNode.class);
    
    private final ContentBox contentBox;

    private Color borderColour = new Color(0xcccccc);
    private BasicStroke borderStroke = new BasicStroke(1f);

    private Color textColour = Color.BLACK;

    private PInputEventListener mouseInputHandler = new PBasicInputEventHandler() {
        @Override
        public void mouseClicked(PInputEvent event) {
            super.mouseClicked(event);
            if (event.getClickCount() == 2) {
                DataEntryPanel propertiesPanel = getPropertiesPanel();
                if (propertiesPanel == null) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    String propertiesPanelName = "Properties for " + contentBox.getName();
                    JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                            propertiesPanel, dialogOwner, propertiesPanelName, "OK");
                    d.setVisible(true);
                }
            }
        }
    };
    
    /**
     * Reacts to changes in the content box by repainting this pnode.
     */
    private final PropertyChangeListener modelChangeHandler = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        	updateBoundsFromContentBox();
            repaint();
            
        }
    };
    
    private final Window dialogOwner;
    
    public ContentBoxNode(Window dialogOwner, ContentBox contentBox) {
        this.dialogOwner = dialogOwner;
        logger.debug("Creating new contentboxnode for " + contentBox);
        this.contentBox = contentBox;
        setBounds(contentBox.getX(), contentBox.getY(), contentBox.getWidth(), contentBox.getHeight());
        addInputEventListener(mouseInputHandler);
        contentBox.addPropertyChangeListener(modelChangeHandler);
        updateBoundsFromContentBox();
    }
    
    private void updateBoundsFromContentBox() {
        super.setBounds(contentBox.getX(), contentBox.getY(),
        		contentBox.getWidth(), contentBox.getHeight());
    }
    
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
    	logger.debug("settingBounds: x="+x+" y="+y+" width="+width+" height="+ height);
    	contentBox.setX((int) x);
    	contentBox.setY((int) y);
    	contentBox.setWidth((int) width);
    	contentBox.setHeight((int) height);
        return true;
    }
    
    @Override
    public void offset(double dx, double dy) {
    	logger.debug("setting offset: x="+dx+" y="+dy);
    	double x = contentBox.getX()+dx;
    	double y = contentBox.getY()+dy;
    	contentBox.setX((int)x);
    	contentBox.setY((int)y);
    }
    
    @Override
    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
        PCamera camera = paintContext.getCamera();
        Graphics2D g2 = paintContext.getGraphics();
        
        g2.setColor(borderColour);
        g2.setStroke(SPSUtils.getAdjustedStroke(borderStroke, camera.getViewScale()));
        g2.draw(getBounds());
        
        g2.setColor(textColour);
        
        ReportContentRenderer contentRenderer = contentBox.getContentRenderer();
        if (contentRenderer != null) {
            logger.debug("Rendering content");
            Graphics2D contentGraphics = (Graphics2D) g2.create(
                    (int) getX(), (int) getY(),
                    (int) getWidth(), (int) getHeight());
            contentGraphics.setFont(contentBox.getFont()); // XXX could use piccolo attribute to do this magically
            contentRenderer.resetToFirstPage();
            contentRenderer.renderReportContent(contentGraphics, contentBox, camera.getViewScale());
            contentGraphics.dispose();
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Empty box\u2014drag content provider here!", 0, (int) (getHeight() / 2));
        }
    }
    
    @Override
    public void setParent(PNode newParent) {
        super.setParent(newParent);
        
        if (newParent instanceof PageNode) {
            Page p = ((PageNode) newParent).getModel();
            if (contentBox.getParent() != null) {
                if (p != contentBox.getParent()) {
                    contentBox.getParent().removeContentBox(contentBox);
                    p.addContentBox(contentBox);
                }
            }
        }
    }

    public void cleanup() {
        contentBox.removePropertyChangeListener(modelChangeHandler);
    }

    public ContentBox getModel() {
        return contentBox;
    }

    public DataEntryPanel getPropertiesPanel() {
        if (contentBox.getContentRenderer() != null) {
            DataEntryPanel propertiesPanel = contentBox.getContentRenderer().getPropertiesPanel();
            if (propertiesPanel == null) {
                logger.debug("Content renderer has no properties dialog: " + contentBox.getContentRenderer());
            }
            return propertiesPanel;
        } else {
            logger.debug("Content box has no renderer: " + contentBox);
            return null;
        }
    }
}
