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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.ComposedIcon;
import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.swingui.MouseState;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitNode;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContextImpl;
import ca.sqlpower.wabit.swingui.action.ExportWabitObjectAction;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class ReportLayoutPanel implements WabitPanel, MouseState {

	private static final Logger logger = Logger.getLogger(ReportLayoutPanel.class);

	private static final Icon STREAM_BADGE = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/stream-badge.png"));
	private static final Icon QUERY_DB = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/query-db-16.png"));
	private static final Icon ZOOM_OUT_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/zoom-minus.png"));
	private static final Icon ZOOM_IN_ICON = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/32x32/zoom-plus.png"));
    public static final Icon CREATE_BOX_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/text.png"));		
    public static final Icon CREATE_HORIZONTAL_GUIDE_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/guideH.png"));
    public static final Icon CREATE_VERTICAL_GUIDE_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/guideV.png"));
    public static final Icon ZOOM_TO_FIT_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/zoom-fit.png"));
    private static final Icon CREATE_GRAPH_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/chart.png"));
    private static final Icon OLAP_QUERY_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/query-olap-16.png"));
    private static final Icon THROBBER_BADGE = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/throbber16-01.png")); //XXX we should be animating this...
    private static final Icon REFRESH_ICON = new ImageIcon(ReportLayoutPanel.class.getClassLoader().getResource("icons/32x32/refresh.png"));
    
    private final JSlider zoomSlider;
    
    /**
	 * The amount to scale the camera view by so that there is a slight space
	 * between the outside guides and the viewable canvas, for better viewing.
	 */
    private static final double AUT0_FIT_SPACER = 0.1;

    /**
     * The amount to multiply the exact zoom factor by in order to come up
     * with the actual zoom factor to use.  The default value of 0.9 leaves
     * at 10% border of empty space around the zoomed region, which is
     * usually a good comfortable amount. 
     */
    private static final double OVER_ZOOM_COEFF = 0.97;
    
    private class QueryDropListener implements DropTargetListener {

		public void dragEnter(DropTargetDragEvent dtde) {
			//no-op
		}

		public void dragExit(DropTargetEvent dte) {
			//no-op
		}

		public void dragOver(DropTargetDragEvent dtde) {
			//no-op
		}

		public void drop(DropTargetDropEvent dtde) {
			if (!dtde.isLocalTransfer()) {
			    logger.debug("Rejecting non-local transfer");
			    dtde.rejectDrop();
				return;
			}
			
			if (!dtde.isDataFlavorSupported(ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR)) {
                logger.debug("Rejecting transfer of unknown flavour");
                dtde.rejectDrop();
				return;
			}			
			
			WabitObject[] queries;
			try {
				queries = (WabitObject[]) dtde.getTransferable().getTransferData(ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR);
			} catch (UnsupportedFlavorException e) {
				dtde.dropComplete(false);
				dtde.rejectDrop();
				throw new RuntimeException(e);
			} catch (IOException e) {
				dtde.dropComplete(false);
				dtde.rejectDrop();
				throw new RuntimeException(e);
			}
			
			for (WabitObject wabitObject : queries) {
			    if (wabitObject instanceof QueryCache) {
			        QueryCache queryCache = (QueryCache) wabitObject;
			        ContentBox contentBox = new ContentBox();
			        ResultSetRenderer rsRenderer = new ResultSetRenderer(queryCache);
			        contentBox.setContentRenderer(rsRenderer);
			        ContentBoxNode newCBNode = new ContentBoxNode(parentFrame,
			                session.getWorkspace(), ReportLayoutPanel.this, contentBox);
			        newCBNode.setBounds(dtde.getLocation().getX(), dtde.getLocation().getY(),
			                (report.getPage().getRightMarginOffset() - report.getPage().getLeftMarginOffset()) / 2,
			                pageNode.getHeight() / 10);
			        pageNode.addChild(newCBNode);
			    } else if (wabitObject instanceof OlapQuery) {
			        OlapQuery olapQuery = (OlapQuery) wabitObject;
			        ContentBox contentBox = new ContentBox();
			        CellSetRenderer renderer = new CellSetRenderer(olapQuery);
			        contentBox.setContentRenderer(renderer);
			        ContentBoxNode newCBNode = new ContentBoxNode(parentFrame,
			                session.getWorkspace(), ReportLayoutPanel.this, contentBox);
                    newCBNode.setBounds(dtde.getLocation().getX(), dtde.getLocation().getY(),
                            (report.getPage().getRightMarginOffset() - report.getPage().getLeftMarginOffset()) / 2,
                            pageNode.getHeight() / 10);
                    pageNode.addChild(newCBNode);
			    } else if (wabitObject instanceof WabitImage) {
			        WabitImage image = (WabitImage) wabitObject;
			        ContentBox contentBox = new ContentBox();
			        ImageRenderer renderer = new ImageRenderer();
			        renderer.setImage(image);
			        renderer.setName(image.getName());
			        contentBox.setContentRenderer(renderer);
			        ContentBoxNode newCBNode = new ContentBoxNode(parentFrame, session.getWorkspace(), 
			                ReportLayoutPanel.this, contentBox);
			        int width;
			        int height;
			        if (image.getImage() != null) {
			        	height = image.getImage().getHeight(null);
			        	width = image.getImage().getWidth(null);
			        } else {
			        	//arbitrary values to make an empty image content box.
			        	height = (int) (pageNode.getHeight() / 10);
			        	width = (int) (pageNode.getWidth() / 10);
			        }
			        newCBNode.setBounds(dtde.getLocation().getX(), dtde.getLocation().getY(),
			                width, height);
			        pageNode.addChild(newCBNode);
			    } else {
			        throw new IllegalStateException("Unknown query dragged into the report layout. Object was " + wabitObject.getClass());
			    }
			}
			
			dtde.dropComplete(true);
			dtde.acceptDrop(dtde.getDropAction());
			
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			//no-op
		}
    	
    }
    
    private final JPanel panel;
    private final PCanvas canvas;
    private final PageNode pageNode;
    private final Layout report;
    
	/**
	 * The mouse state in this LayoutPanel.
	 */
	private MouseStates mouseState = MouseStates.READY;
	
    /**
     * The cursor manager for this Query pen.
     */
	private final CursorManager cursorManager;

	/**
	 * This split pane will split the layout editor between the page layout
	 * and the list of queries that can be dragged into the layout.
	 */
	private JSplitPane mainSplitPane;
	
	private final AbstractAction addContentBoxAction = new AbstractAction("",  ReportLayoutPanel.CREATE_BOX_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_BOX);
			cursorManager.placeModeStarted();
		}
	};
	
	private final WabitSwingSession session;
	private final JFrame parentFrame;
	
	private final AbstractAction addHorizontalGuideAction = new AbstractAction("",  ReportLayoutPanel.CREATE_HORIZONTAL_GUIDE_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_HORIZONTAL_GUIDE);
			cursorManager.placeModeStarted();
		}
	};
	
	private final AbstractAction addVerticalGuideAction = new AbstractAction("",  ReportLayoutPanel.CREATE_VERTICAL_GUIDE_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_VERTICAL_GUIDE);
			cursorManager.placeModeStarted();
		}
	};
	
	private final AbstractAction zoomToFitAction = new AbstractAction("", ZOOM_TO_FIT_ICON){
		public void actionPerformed(ActionEvent e) {
			Rectangle rect = canvas.getVisibleRect();
			Page page = pageNode.getModel();
			double zoom = Math.min(rect.getHeight() / (page.getLowerMarginOffset() - page.getUpperMarginOffset()),
					rect.getWidth() / (page.getRightMarginOffset() - page.getLeftMarginOffset()));
			zoom *= OVER_ZOOM_COEFF;
			canvas.getCamera().setViewScale(zoom);
			zoomSlider.setValue((int)((zoomSlider.getMaximum() - zoomSlider.getMinimum()) / 2 * zoom));
			canvas.getCamera().setViewOffset(- (page.getLeftMarginOffset() * zoom - page.getLeftMarginOffset() * AUT0_FIT_SPACER), -(page.getUpperMarginOffset() * zoom - page.getUpperMarginOffset()*AUT0_FIT_SPACER));
		}};
		
	private final Action addGraphBoxAction = new AbstractAction("", CREATE_GRAPH_ICON) {
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_GRAPH);
			cursorManager.placeModeStarted();
		}
	};
	
	private final Action refreshDataAction = new AbstractAction("", REFRESH_ICON) {
		public void actionPerformed(ActionEvent e) {
			// TODO: Implement query data refresh
			JOptionPane.showMessageDialog(ReportLayoutPanel.this.panel, "When implemented, this will refresh the data from all queries");
		}
	};
	
    public ReportLayoutPanel(final WabitSwingSession session, final Layout report) {
        this.session = session;
        parentFrame = ((WabitSwingSessionContext) session.getContext()).getFrame();
		this.report = report;
		canvas = new PCanvas();
        canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setPanEventHandler(null);
        canvas.setBackground(Color.LIGHT_GRAY);
        canvas.setPreferredSize(new Dimension(400,600));
        cursorManager = new CursorManager(canvas);
        
        pageNode = new PageNode(session, this, report.getPage());
        canvas.getLayer().addChild(pageNode);
        
        // XXX why is this being done? skipping it appears to have no effect
        pageNode.setBounds(0, 0, pageNode.getWidth(), pageNode.getHeight());
        
        PSelectionEventHandler selectionEventHandler = new GuideAwareSelectionEventHandler(pageNode, pageNode);
        canvas.addInputEventListener(selectionEventHandler);
        pageNode.setPickable(false);
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
        
        
        AbstractAction cancelBoxCreateAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (mouseState == MouseStates.CREATE_BOX || mouseState == MouseStates.CREATE_HORIZONTAL_GUIDE 
        				|| mouseState == MouseStates.CREATE_VERTICAL_GUIDE ) {
        			setMouseState(MouseStates.READY);
        			cursorManager.placeModeFinished();
        		}
        	}
        };
		
        canvas.getActionMap().put(addContentBoxAction.getClass(), addContentBoxAction);
		InputMap inputMap = canvas.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke('b'), addContentBoxAction.getClass());
		
		addContentBoxAction.putValue(Action.SHORT_DESCRIPTION, "Add content box");
		addGraphBoxAction.putValue(Action.SHORT_DESCRIPTION, "Add chart");
		addHorizontalGuideAction.putValue(Action.SHORT_DESCRIPTION, "Add horizontal guide");
		addVerticalGuideAction.putValue(Action.SHORT_DESCRIPTION, "Add vertical guide");
		zoomToFitAction.putValue(Action.SHORT_DESCRIPTION, "Zoom to fit");
		
		canvas.addInputEventListener(new CreateNodeEventHandler(session, this));
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
       
        JButton button = new JButton(refreshDataAction);
        setupToolBarButtonLabel(button, "Refresh");
        toolbar.add(button);
        toolbar.addSeparator();
        
        button = new JButton(addContentBoxAction);
        setupToolBarButtonLabel(button, "Label");
        toolbar.add(button);

        button = new JButton(addGraphBoxAction);
        setupToolBarButtonLabel(button, "Chart");
        toolbar.add(button);
        
        button = new JButton(addHorizontalGuideAction);
        setupToolBarButtonLabel(button, "H. Guide");
        toolbar.add(button);
        
        button = new JButton(addVerticalGuideAction);
        setupToolBarButtonLabel(button, "V.Guide");
        toolbar.add(button);
        
        toolbar.addSeparator();
        JPanel zoomPanel = new JPanel(new BorderLayout());
        zoomPanel.add(new JLabel(ZOOM_OUT_ICON), BorderLayout.WEST);
        final int defaultSliderValue = 500;
        zoomSlider= new JSlider(JSlider.HORIZONTAL, 1, 1000, defaultSliderValue);
        zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    final double newScale = (double)zoomSlider.getValue()/defaultSliderValue;
                final PCamera camera = canvas.getCamera();
                double oldScale = camera.getViewScale();
                camera.scaleViewAboutPoint(newScale/oldScale, camera.getViewBounds().getCenterX(), camera.getViewBounds().getCenterY());
                logger.debug("Camera scaled by " + newScale/oldScale + " and is now at " + camera.getViewScale());
				ReportLayoutPanel.this.report.setZoomLevel(zoomSlider.getValue());
			}
		});
        zoomSlider.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseReleased(MouseEvent e) {
        		if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) {
        			zoomSlider.setValue(defaultSliderValue);
        		}
        	}
		});
        if (report.getZoomLevel() > zoomSlider.getMinimum() && report.getZoomLevel() < zoomSlider.getMaximum()) {
        	zoomSlider.setValue(report.getZoomLevel());
        }
        zoomPanel.add(zoomSlider, BorderLayout.CENTER);
		zoomPanel.add(new JLabel(ZOOM_IN_ICON), BorderLayout.EAST);
        zoomPanel.setMaximumSize(new Dimension((int)zoomSlider.getPreferredSize().getWidth(), 200));
        toolbar.add(zoomPanel);
        button = new JButton(zoomToFitAction);
        setupToolBarButtonLabel(button, "Zoom to Fit");
        toolbar.add(button);
        toolbar.addSeparator();
        
        button = new JButton(new PageFormatAction(report.getPage()));
        setupToolBarButtonLabel(button, "Page Settings");
        toolbar.add(button);
        
		button = new JButton(new ExportWabitObjectAction<Layout>(session,
				report, WabitIcons.EXPORT_ICON_32,
				"Export Report to Wabit file"));
        setupToolBarButtonLabel(button, "Export");
        toolbar.add(button);
        
        button = new JButton(new PrintPreviewAction(parentFrame, report));
        setupToolBarButtonLabel(button, "Preview");
        toolbar.add(button);

        button = new JButton(new PrintAction(report, toolbar, session));
        setupToolBarButtonLabel(button, "Print");
        toolbar.add(button);
        
        button = new JButton(new PDFAction(session, toolbar, report));
        setupToolBarButtonLabel(button, "Print PDF");
        toolbar.add(button);
        
        
        JToolBar wabitBar = new JToolBar();
        wabitBar.setFloatable(false);
        JButton forumButton = new JButton(WabitSwingSessionContextImpl.FORUM_ACTION);
		forumButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		wabitBar.add(forumButton);
        
        JToolBar mainbar = new JToolBar();
        mainbar.setLayout(new BorderLayout());
        mainbar.add(toolbar, BorderLayout.CENTER);
        mainbar.add(wabitBar, BorderLayout.EAST);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(mainbar, BorderLayout.NORTH);
        PScrollPane canvasScrollPane = new PScrollPane(canvas);
		canvasScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        leftPanel.add(canvasScrollPane, BorderLayout.CENTER);
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setResizeWeight(1);
        mainSplitPane.add(leftPanel, JSplitPane.LEFT);
        
        final JList queryList = new JList(new DraggableWabitObjectListModel(session.getWorkspace()));
        queryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        queryList.setCellRenderer(new DefaultListCellRenderer() {
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(queryList, value, index, isSelected, cellHasFocus);
				((JLabel) c).setText(((WabitObject) value).getName());
				
				if (value instanceof QueryCache) {
				    if (((QueryCache) value).isRunning()) {
				        if (((QueryCache) value).isStreaming()) {
				            ((JLabel) c).setIcon(new ComposedIcon(Arrays.asList(new Icon[]{QUERY_DB, STREAM_BADGE})));
				        } else {
				            ((JLabel) c).setIcon(new ComposedIcon(Arrays.asList(new Icon[]{QUERY_DB, THROBBER_BADGE})));
				        }
				    } else {
				        ((JLabel) c).setIcon(QUERY_DB);
				    }
				} else if (value instanceof OlapQuery) {
				    ((JLabel) c).setIcon(OLAP_QUERY_ICON);
				} else if (value instanceof WabitImage) {
				    final Image wabitImage = ((WabitImage) value).getImage();
				    if (wabitImage != null) {
				        JLabel label = ((JLabel) c);
				        final int width = QUERY_DB.getIconWidth();
				        final int height = QUERY_DB.getIconHeight();
				        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

				        Graphics2D g = image.createGraphics();
				        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				        g.drawImage(wabitImage, 0, 0, width, height, new Color(0xffffffff, true), null);
				        g.dispose();

				        final ImageIcon icon = new ImageIcon(image);
				        label.setIcon(icon);
				    }
				}
				return c;
			}
		});
        
        DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(queryList, DnDConstants.ACTION_COPY, new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent dge) {
				if (queryList.getSelectedValues() == null || queryList.getSelectedValues().length <= 0) {
					return;
				}
				List<WabitObject> queries = new ArrayList<WabitObject>();
				for (Object q : queryList.getSelectedValues()) {
					queries.add((WabitObject) q);
				}
				Transferable dndTransferable = new ReportQueryTransferable(queries);
				dge.getDragSource().startDrag(dge, null, dndTransferable, new DragSourceAdapter() {
					//This is a drag source adapter with empty methods.
				});
			}
		});
		new DropTarget(canvas, new QueryDropListener());
		
        mainSplitPane.add(new JScrollPane(queryList), JSplitPane.RIGHT);
                
        panel = new JPanel(new BorderLayout());
        panel.add(mainSplitPane, BorderLayout.CENTER);
        
        panel.getActionMap().put(cancelBoxCreateAction.getClass(), cancelBoxCreateAction);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelBoxCreateAction.getClass());
    }
    
    /**
     * Frees any resources and references that would not have been freed otherwise (by virtue
     * of this panel being removed from the GUI).
     */
    private void cleanup() {
        recursiveCleanup(pageNode);
    }
    
    /**
     * On every PNode in the tree rooted at node which implements ReportNode, calls cleanup().
     * 
     * @param node
     */
    private void recursiveCleanup(PNode node) {
        Iterator<?> nodeChildrenIterator = node.getChildrenIterator();
        while (nodeChildrenIterator.hasNext()) {
            PNode child = (PNode) nodeChildrenIterator.next();
            recursiveCleanup(child);
        }
        
        if (node instanceof WabitNode) {
            ((WabitNode) node).cleanup();
        }
    }
    
    // ==================== DataEntryPanel implementation ==================

    public boolean applyChanges() {
        cleanup();
        return true;
    }

    public void discardChanges() {
        cleanup();
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
        return false;
    }

	public MouseStates getMouseState() {
		return this.mouseState;
	}

	public void setMouseState(MouseStates state) {
		this.mouseState = state;		
	}

	public Layout getReport() {
		return report;
	}

	public PageNode getPageNode() {
		return pageNode;
	}

	public CursorManager getCursorManager() {
		return cursorManager;
	}

	public JSplitPane getSplitPane() {
		return mainSplitPane;
	}

	public void maximizeEditor() {
		if (mainSplitPane.getDividerLocation() == mainSplitPane.getMaximumDividerLocation()) {
			mainSplitPane.setDividerLocation(mainSplitPane.getLastDividerLocation());
		} else {
			mainSplitPane.setDividerLocation(mainSplitPane.getMaximumDividerLocation());
		}
	}
	
	public String getTitle() {
		return "Report Editor - " + report.getName();
	}
	
	/**
	 * Adds a text label with the given label String, and sets it at the bottom
	 * center of the button
	 */
	private void setupToolBarButtonLabel(JButton button, String label) {
		button.setText(label);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		// Removes button borders on OS X 10.5
		button.putClientProperty("JButton.buttonType", "toolbar");
	}
}
