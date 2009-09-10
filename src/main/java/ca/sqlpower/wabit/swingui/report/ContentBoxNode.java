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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.wabit.WabitChildEvent;
import ca.sqlpower.wabit.WabitChildListener;
import ca.sqlpower.wabit.WabitWorkspace;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Label;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ReportContentRenderer;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.WabitObjectReportRenderer;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PInputManager;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PPaintContext;

public class ContentBoxNode extends PNode implements ReportNode {

    private static final Logger logger = Logger.getLogger(ContentBoxNode.class);
    
    private final WabitSwingSession session;
    
    private final ContentBox contentBox;

    private Color textColour = Color.BLACK;
    
    /**
     * This is the variable which determines whether or not the borders will be
     * painted on a content box.
     */
    private boolean paintBorders = false;
    
    private boolean showDropInfo = false;
    
    /**
     * The simple property change support object to fire basic property changes
     * from the content box node. This is different from the piccolo property
     * changes on this object as piccolo sends property codes with the change
     * event which is not needed for the simpler properties of this content box.
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private PInputEventListener inputHandler = new PInputManager() {
        
        @Override
        public void mouseClicked(PInputEvent event) {
            super.mouseClicked(event);
            if (event.getClickCount() == 2) {
                if (contentBox.getContentRenderer() == null) {
                    Label newLabel = new Label();
                    contentBox.setContentRenderer(newLabel);
                }
                
                DataEntryPanel propertiesPanel = getPropertiesPanel();
                if (propertiesPanel != null) {
                    String propertiesPanelName = "Properties for " + contentBox.getName();
                    JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
                            propertiesPanel, dialogOwner, propertiesPanelName, "OK");
                    d.setVisible(true);
                }
            }
            
        }
        
        @Override
        public void mousePressed(PInputEvent arg0) {
        	super.mousePressed(arg0);
        	maybeShowPopup(arg0);
        }
        
        @Override
        public void mouseReleased(PInputEvent arg0) {
        	super.mouseReleased(arg0);
        	maybeShowPopup(arg0);
        }
        
        @Override
        public void mouseEntered(PInputEvent event) {
        	super.mouseEntered(event);
        	paintBorders = true;
        	repaint();
        }
        
        @Override
        public void mouseExited(PInputEvent event) {
        	super.mouseExited(event);
        	paintBorders = false;
        	repaint();
        }
        
        /**
         * This will Display a List of options once you right click on the WorkspaceTree.
         */
        private void maybeShowPopup(PInputEvent e) {
        	if (!e.isPopupTrigger()) return;
        	JPopupMenu menu = new JPopupMenu();
        	JMenuItem properties = new JMenuItem(new AbstractAction() {
				public void actionPerformed(ActionEvent arg0) {
		        	DataEntryPanel propertiesPanel = getPropertiesPanel();
		            if (propertiesPanel != null) {
		                String propertiesPanelName = "Properties for " + contentBox.getName();
		                JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
		                        propertiesPanel, dialogOwner, propertiesPanelName, "OK");
		                d.setVisible(true);
		            }
				}
        	});
        	properties.setText("Properties");
        	menu.add(properties);
        	menu.addSeparator();
        	
        	if (contentBox.getContentRenderer() instanceof WabitObjectReportRenderer){
	        	JMenuItem source = new JMenuItem(new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						session.getWorkspace().setEditorPanelModel(((WabitObjectReportRenderer)contentBox.getContentRenderer()).getContent());
					}
	        	});
	        	source.setText("Go to Editor");
	        	menu.add(source);
	        	menu.addSeparator();
        	}
        	
        	final PNode node = getParent();
        	JMenuItem delete = new JMenuItem(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					node.removeChild(ContentBoxNode.this);
				}
        	});
        	delete.setText("Delete");
        	menu.add(delete);
        	
        	JMenuItem clear = new JMenuItem(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    contentBox.setContentRenderer(null);
                }
            });
        	clear.setText("Clear");
        	menu.add(clear);
        	
        	final PCanvas canvas = (PCanvas) e.getComponent();
        	Point2D position = e.getCanvasPosition();
			menu.show(canvas, (int) position.getX(), (int) position.getY());
        }
        
    };
    
    public void setDropFeedback(boolean dropInfo) {
		this.showDropInfo = dropInfo;
		this.paintBorders = dropInfo;
	}
    
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

    /**
     * This {@link ResultSetRenderer} compliment will handle operations on the
     * renderer that are swing specific.
     */
    private SwingContentRenderer swingRenderer;
    
    private final WabitWorkspace workspace;
    
    /**
     * This is the contentRenderer listener which listens to changes is the 
     */
    private WabitChildListener contentRendererListener = new WabitChildListener() {
		public void wabitChildRemoved(WabitChildEvent e) {
			setSwingContentRenderer(null);
		}
		
		public void wabitChildAdded(WabitChildEvent e) {
			ReportContentRenderer renderer = (ReportContentRenderer) e.getChild();
	        setSwingContentRenderer(renderer);

		}

	};
    
	/**
	 * This method will set the {@link SwingContentRenderer} on this content box node
	 * this should only really be used in the constructor because after that it will
	 * add a listener on the content box to listen for changes to the contentrenderer
	 */
	private void setSwingContentRenderer(ReportContentRenderer renderer) {
	    SwingContentRenderer oldSwingRenderer = swingRenderer;
		if (swingRenderer != null) {
			removeInputEventListener(swingRenderer);
		}
		if (renderer instanceof CellSetRenderer) {
            swingRenderer = new CellSetSwingRenderer((CellSetRenderer) renderer);
        } else if (renderer instanceof ResultSetRenderer) {
            swingRenderer = new ResultSetSwingRenderer((ResultSetRenderer) renderer, parentPanel);
        } else if (renderer instanceof ImageRenderer) {
            swingRenderer = new ImageSwingRenderer(workspace, (ImageRenderer) renderer);
        } else if (renderer instanceof ChartRenderer) {
            swingRenderer = new ChartSwingRenderer(workspace, (ChartRenderer) renderer);
        } else if (renderer instanceof Label) {
            swingRenderer = new SwingLabel((Label) renderer);
        } else if (renderer == null) {
            swingRenderer = null;
        } else {
            throw new IllegalStateException("Unknown renderer of type " + renderer.getClass() 
                    + ". The swing components of this renderer type are missing.");
        }
		if (swingRenderer != null) {
			addInputEventListener(swingRenderer);
		}
		pcs.firePropertyChange(new PropertyChangeEvent(this, "swingRenderer", oldSwingRenderer,
		        swingRenderer));
	}
	
	private final LayoutPanel parentPanel;
	
    public ContentBoxNode(WabitSwingSession session, Window dialogOwner, WabitWorkspace workspace, LayoutPanel parentPanel, ContentBox contentBox) {
    	this.session = session;
        this.dialogOwner = dialogOwner;
        logger.debug("Creating new contentboxnode for " + contentBox);
        this.contentBox = contentBox;
        this.parentPanel = parentPanel;
        this.workspace = workspace;
        
        setSwingContentRenderer(contentBox.getContentRenderer());
		contentBox.addChildListener(contentRendererListener);
        setBounds(contentBox.getX(), contentBox.getY(), contentBox.getWidth(), contentBox.getHeight());
        contentBox.addPropertyChangeListener(modelChangeHandler);
        addInputEventListener(inputHandler);
        updateBoundsFromContentBox();

    }
    
    private void updateBoundsFromContentBox() {
        super.setBounds(contentBox.getX(), contentBox.getY(),
        		contentBox.getWidth(), contentBox.getHeight());
    }
    
    @Override
    public boolean setBounds(double x, double y, double width, double height) {
    	logger.debug("settingBounds: x="+x+" y="+y+" width="+width+" height="+ height);
    	contentBox.setX(x);
    	contentBox.setY(y);
    	contentBox.setWidth(width);
    	contentBox.setHeight(height);
        return true;
    }
    
    private boolean draggedOver = false;
    
    public void setDraggedOver(boolean draggedOver) {
    	this.draggedOver = draggedOver;
    	repaint();
    }
    
    @Override
    protected void paint(PPaintContext paintContext) {
    	ReportContentRenderer contentRenderer = contentBox.getContentRenderer();
    	if (contentRenderer != null && contentRenderer.getBackgroundColour() != null) {
    		setPaint(contentRenderer.getBackgroundColour());
    	}
    	else{
    		setPaint(null);
    	}
        super.paint(paintContext);
        PCamera camera = paintContext.getCamera();
        Graphics2D g2 = paintContext.getGraphics();
        
        Color borderColor;
        BasicStroke borderStroke;
        if (draggedOver) {
        	borderStroke = new BasicStroke(3f);
        	borderColor = Color.BLACK;
        } else {
        	borderStroke = new BasicStroke(1f);
    		borderColor = Color.LIGHT_GRAY;
        }
        
        String str = "Drag content here!";;
    	double y = getY() + (getHeight() / 2);
    	Rectangle2D rect;
    	double x = 0;
        if (showDropInfo) {
        	rect = g2.getFontMetrics().getStringBounds(str, g2);
        	x = getX() + ((getWidth() /2) - (rect.getWidth() / 2));
        }
        
		if (contentRenderer != null) {
            g2.setColor(textColour);
            logger.debug("Rendering content");
            Graphics2D contentGraphics = (Graphics2D) g2.create(
                    (int) getX(), (int) getY(),
                    (int) getWidth(), (int) getHeight());
            contentGraphics.setFont(contentBox.getFont()); // XXX could use piccolo attribute to do this magically
            contentRenderer.resetToFirstPage();
            contentRenderer.renderReportContent(contentGraphics, contentBox, camera.getViewScale(), 0, false);
            contentGraphics.dispose();
            if (showDropInfo) {
            	g2.setColor(borderColor);
                g2.drawString(str, (int) x,(int) y);
            }
        } else {
        	if (showDropInfo) {
        		g2.setColor(borderColor);
				g2.drawString(str, (int) x,(int) y);
        	} else {
        		g2.setColor(Color.LIGHT_GRAY);
            	rect = g2.getFontMetrics().getStringBounds("Empty box\u2014" + str, g2);
            	x = getX() + ((getWidth() /2) - (rect.getWidth() / 2));
        		g2.drawString("Empty box\u2014" + str, (int) x,(int) y);
        	}
        }
        if (paintBorders) {
	        g2.setStroke(SPSUtils.getAdjustedStroke(borderStroke, camera.getViewScale()));
	        g2.setColor(borderColor);
	        g2.draw(getBounds());
        }
    }
    @Override
    public void offset(double dx, double dy) {
    	logger.debug("setting offset: x="+dx+" y="+dy);
    	double x = contentBox.getX() + dx;
    	double y = contentBox.getY() + dy;
    	contentBox.setX(x);
    	contentBox.setY(y);
    }
    @Override
    public void setParent(PNode newParent) {
        
        if (newParent instanceof PageNode) {
            Page p = ((PageNode) newParent).getModel();
            if (contentBox.getParent() != null) {
                if (p != contentBox.getParent()) {
                    contentBox.getParent().removeContentBox(contentBox);
                    p.addContentBox(contentBox);
                }
            }
        } else if ( newParent == null) {
        	contentBox.getParent().removeContentBox(contentBox);
        }
        super.setParent(newParent);
    }

    public void cleanup() {
        contentBox.removePropertyChangeListener(modelChangeHandler);
        contentBox.removeChildListener(contentRendererListener);
    }

    public ContentBox getModel() {
        return contentBox;
    }

    public DataEntryPanel getPropertiesPanel() {
        if (contentBox.getContentRenderer() != null) {
            DataEntryPanel propertiesPanel = swingRenderer.getPropertiesPanel();
            if (propertiesPanel == null) {
                logger.debug("Content renderer has no properties dialog: " + contentBox.getContentRenderer());
            }
            return propertiesPanel;
        } else {
            logger.debug("Content box has no renderer: " + contentBox);
            return null;
        }
    }
    
    public PInputEventListener getKeyboardListener() {
        if (swingRenderer != null) {
            return swingRenderer;
        }
    	return inputHandler;
    }
    
    /**
     * Adds a property change listener that listens to content box specific
     * properties. This is different from the property change listener that you
     * can add to listen to piccolo property changes. Piccolo property changes
     * come with a property code that is not necessary for ContentBoxNode property
     * changes.
     */
    public void addContentBoxPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removeContentBoxPropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
