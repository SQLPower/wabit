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
import java.awt.Rectangle;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.help.UnsupportedOperationException;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.wabit.QueryCache;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.image.WabitImage;
import ca.sqlpower.wabit.olap.OlapQuery;
import ca.sqlpower.wabit.report.CellSetRenderer;
import ca.sqlpower.wabit.report.ChartRenderer;
import ca.sqlpower.wabit.report.ContentBox;
import ca.sqlpower.wabit.report.ImageRenderer;
import ca.sqlpower.wabit.report.Layout;
import ca.sqlpower.wabit.report.Page;
import ca.sqlpower.wabit.report.Report;
import ca.sqlpower.wabit.report.ResultSetRenderer;
import ca.sqlpower.wabit.report.Template;
import ca.sqlpower.wabit.report.chart.Chart;
import ca.sqlpower.wabit.swingui.MouseState;
import ca.sqlpower.wabit.swingui.WabitIcons;
import ca.sqlpower.wabit.swingui.WabitNode;
import ca.sqlpower.wabit.swingui.WabitPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.WabitSwingSessionContext;
import ca.sqlpower.wabit.swingui.WabitToolBarBuilder;
import ca.sqlpower.wabit.swingui.action.ExportWabitObjectAction;
import ca.sqlpower.wabit.swingui.action.ReportFromTemplateAction;
import ca.sqlpower.wabit.swingui.tree.WorkspaceTreeCellRenderer;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.swing.PScrollPane;

public class LayoutPanel implements WabitPanel, MouseState {

	private static final Logger logger = Logger.getLogger(LayoutPanel.class);

    public static final Icon CREATE_BOX_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/text.png"));		
    public static final Icon CREATE_HORIZONTAL_GUIDE_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/guideH.png"));
    public static final Icon CREATE_VERTICAL_GUIDE_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/guideV.png"));
    public static final Icon ZOOM_TO_FIT_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/zoom-fit.png"));
    private static final Icon REFRESH_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/refresh.png"));
    private static final Icon CONTENTBOX_ICON = new ImageIcon(LayoutPanel.class.getClassLoader().getResource("icons/32x32/content.png"));
    
    private final JSlider zoomSlider;
    
    /**
     * The amount to multiply the exact zoom factor by in order to come up
     * with the actual zoom factor to use.  The default value of 0.9 leaves
     * at 10% border of empty space around the zoomed region, which is
     * usually a good comfortable amount. 
     */
    private static final double OVER_ZOOM_COEFF = 0.98;
    
    private ContentBoxNode focusedCBNode = null;

	private class QueryDropListener implements DropTargetListener {

		public void dragEnter(DropTargetDragEvent dtde) {
			showDropInfo(true);
		}

		public void dragExit(DropTargetEvent dte) {
			showDropInfo(false);
		}
		
		private void showDropInfo(boolean shouldShow) {
			for (int i = 0; i < pageNode.getChildrenCount(); i++) {
				PNode node = pageNode.getChild(i);
				if (node instanceof ContentBoxNode) {
					((ContentBoxNode) node).setDropFeedback(shouldShow);
					node.repaint();
				}
				
			}
		}

		public void dragOver(DropTargetDragEvent dtde) {
			Point2D point = dtde.getLocation();
			PPickPath path = canvas.getCamera().pick(point.getX(), point.getY(), 1);
			PNode node =  path.getPickedNode();
			if (node == focusedCBNode) return;
			
			if (focusedCBNode != null) {
				focusedCBNode.setDraggedOver(false);
			}
			if (node != null && node instanceof ContentBoxNode) {
				ContentBoxNode contentNode = (ContentBoxNode) node;
				contentNode.setDraggedOver(true);
				focusedCBNode = contentNode;
			} else {
				focusedCBNode = null;
			}
		}

		public void drop(DropTargetDropEvent dtde) {
			if (!dtde.isLocalTransfer()) {
			    logger.debug("Rejecting non-local transfer");
			    dtde.rejectDrop();
			    resetUIAfterDrag();
				return;
			}
			
			if (!dtde.isDataFlavorSupported(ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR)) {
                logger.debug("Rejecting transfer of unknown flavour");
                dtde.rejectDrop();
                resetUIAfterDrag();
				return;
			}			
			
			WabitObject[] wabitDroppings;
			try {
				wabitDroppings = (WabitObject[]) dtde.getTransferable().getTransferData(ReportQueryTransferable.LOCAL_QUERY_ARRAY_FLAVOUR);
			} catch (UnsupportedFlavorException e) {
				dtde.dropComplete(false);
				dtde.rejectDrop();
				resetUIAfterDrag();
				throw new RuntimeException(e);
			} catch (IOException e) {
				dtde.dropComplete(false);
				dtde.rejectDrop();
				resetUIAfterDrag();
				throw new RuntimeException(e);
			}
			
			boolean isFirstTime = true; //case where user drags multiple items onto a contentbox
			for (WabitObject wabitObject : wabitDroppings) {
				ContentBox contentBox; 
				ContentBoxNode cbNode;
				if (focusedCBNode != null && isFirstTime) {
					cbNode = focusedCBNode;
					contentBox = focusedCBNode.getModel();
					isFirstTime = false;
				} else {
					contentBox = new ContentBox();
					cbNode = new ContentBoxNode(parentFrame, session.getWorkspace(), 
							LayoutPanel.this, contentBox);
				}

				int width = 0;
				int height = 0;
				height = (int) (pageNode.getHeight() / 10);
				width = (int) (pageNode.getWidth() / 10);

				if (wabitObject instanceof QueryCache) {
					QueryCache queryCache = (QueryCache) wabitObject;
					ResultSetRenderer rsRenderer = new ResultSetRenderer(queryCache);
					contentBox.setContentRenderer(rsRenderer);
				} else if (wabitObject instanceof OlapQuery) {
					OlapQuery olapQuery = (OlapQuery) wabitObject;
					CellSetRenderer renderer = new CellSetRenderer(olapQuery);
					contentBox.setContentRenderer(renderer);
				} else if (wabitObject instanceof Chart) {
					Chart chart = (Chart) wabitObject;
					ChartRenderer renderer = new ChartRenderer(chart);
					contentBox.setContentRenderer(renderer);
				} else if (wabitObject instanceof WabitImage) {
					WabitImage image = (WabitImage) wabitObject;
					ImageRenderer renderer = new ImageRenderer();
					renderer.setImage(image);
					contentBox.setContentRenderer(renderer);

					if (image.getImage() != null) {
						height = image.getImage().getHeight(null);
						width = image.getImage().getWidth(null);
					}
				} else {
					dtde.dropComplete(false);
					dtde.rejectDrop();
					resetUIAfterDrag();
					throw new IllegalStateException("Unknown item dragged into the report layout. Object was " + wabitObject.getClass());
				}
				if (focusedCBNode == null) {
					Point2D location = canvas.getCamera().localToView(dtde.getLocation());
					cbNode.setBounds(location.getX(), location.getY(), height, width);
					pageNode.addChild(cbNode);
				}
			}

			dtde.dropComplete(true);
			resetUIAfterDrag();
			dtde.acceptDrop(dtde.getDropAction());

		}
		
		private void resetUIAfterDrag() {
			if (focusedCBNode != null) {
				focusedCBNode.setDraggedOver(false);
			}
			showDropInfo(false);
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
			//no-op
		}

	}

	private final JPanel panel;
	private final PCanvas canvas;
	private final PageNode pageNode;
	private final Layout layout;

	/**
	 * The mouse state in this LayoutPanel.
	 */
	private MouseStates mouseState = MouseStates.READY;

	/**
     * The cursor manager for this Query pen.
     */
	private final CursorManager cursorManager;

	private final AbstractAction addLabelAction = new AbstractAction("",  LayoutPanel.CREATE_BOX_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_LABEL);
			cursorManager.placeModeStarted();
		}
	};
	
    AbstractAction addContentBoxAction = new AbstractAction("", CONTENTBOX_ICON) {
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_BOX);
			cursorManager.placeModeStarted();
		}
	};
	
	private final WabitSwingSession session;
	private final JFrame parentFrame;
	
	private final AbstractAction addHorizontalGuideAction = new AbstractAction("",  LayoutPanel.CREATE_HORIZONTAL_GUIDE_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_HORIZONTAL_GUIDE);
			cursorManager.placeModeStarted();
		}
	};
	
	private final AbstractAction addVerticalGuideAction = new AbstractAction("",  LayoutPanel.CREATE_VERTICAL_GUIDE_ICON){
		public void actionPerformed(ActionEvent e) {
			setMouseState(MouseStates.CREATE_VERTICAL_GUIDE);
			cursorManager.placeModeStarted();
		}
	};
	
	
	/**
	 * Centres the Page in the Report view and sets the zoom level so that the
	 * entire page just fits into the view.
	 * TODO: Also add zoom to fit margins, and zoom to fit selection
	 */
	private final AbstractAction zoomToFitAction = new AbstractAction("", ZOOM_TO_FIT_ICON) {
		public void actionPerformed(ActionEvent e) {
			zoomToFit();
		}
	};
		
	private final Action refreshDataAction = new AbstractAction("", REFRESH_ICON) {
		public void actionPerformed(ActionEvent e) {
			for (Page page: layout.getChildren()) {
				for (ContentBox content: page.getContentBoxes()){
					content.getContentRenderer().refresh();
					//TODO: Catch exceptions and call a new ReportContentRenderer 'renderError' method
				}
			}
			canvas.repaint();
		}
	};

    private PScrollPane canvasScrollPane;

    /**
     * The component that acts as a drag source, holding everything that can be
     * dragged into the current report.
     */
    private final JList sourceList;
    
    /**
     * Scroll pane that contains {@link #sourceList}. This is the value
     * returned by {@link #getSourceComponent()}.
     */
    private final JScrollPane sourceListScrollPane;
	
    public LayoutPanel(final WabitSwingSession session, final Layout layout) {
        this.session = session;
        parentFrame = ((WabitSwingSessionContext) session.getContext()).getFrame();
		this.layout = layout;
		canvas = new PCanvas();
        canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.setPanEventHandler(null);
        canvas.setBackground(Color.LIGHT_GRAY);
        canvas.setPreferredSize(new Dimension(400,600));
        canvas.setZoomEventHandler(null);
        cursorManager = new CursorManager(canvas);
        
        pageNode = new PageNode(session, this, layout.getPage());
        canvas.getLayer().addChild(pageNode);
        
        // XXX why is this being done? skipping it appears to have no effect
        pageNode.setBounds(0, 0, pageNode.getWidth(), pageNode.getHeight());
        
        PSelectionEventHandler selectionEventHandler = 
        	new GuideAwareSelectionEventHandler(canvas, pageNode, pageNode);
        canvas.addInputEventListener(selectionEventHandler);
        pageNode.setPickable(false);
        canvas.getRoot().getDefaultInputManager().setKeyboardFocus(selectionEventHandler);
        
        
        AbstractAction cancelBoxCreateAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		if (mouseState == MouseStates.CREATE_BOX || mouseState == MouseStates.CREATE_LABEL|| mouseState == MouseStates.CREATE_HORIZONTAL_GUIDE 
        				|| mouseState == MouseStates.CREATE_VERTICAL_GUIDE ) {
        			setMouseState(MouseStates.READY);
        			cursorManager.placeModeFinished();
        		}
        	}
        };
		
        canvas.getActionMap().put(addLabelAction.getClass(), addLabelAction);
		InputMap inputMap = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke('b'), addLabelAction.getClass());
		
		addLabelAction.putValue(Action.SHORT_DESCRIPTION, "Add content box");
		addHorizontalGuideAction.putValue(Action.SHORT_DESCRIPTION, "Add horizontal guide");
		addVerticalGuideAction.putValue(Action.SHORT_DESCRIPTION, "Add vertical guide");
		zoomToFitAction.putValue(Action.SHORT_DESCRIPTION, "Zoom to fit");
		
		canvas.addInputEventListener(new CreateNodeEventHandler(session, this));
		
		WabitToolBarBuilder toolBarBuilder = new WabitToolBarBuilder();
		toolBarBuilder.add(refreshDataAction, "Refresh");
		toolBarBuilder.addSeparator();
		toolBarBuilder.add(addContentBoxAction, "Content Box");
		toolBarBuilder.add(addLabelAction, "Label");
		toolBarBuilder.add(addHorizontalGuideAction, "H. Guide");
		toolBarBuilder.add(addVerticalGuideAction, "V. Guide");
        toolBarBuilder.addSeparator();

        JPanel zoomPanel = new JPanel(new BorderLayout());
        zoomPanel.add(new JLabel(WabitIcons.ZOOM_OUT_ICON_16), BorderLayout.WEST);
        final int defaultSliderValue = 500;
        zoomSlider= new JSlider(JSlider.HORIZONTAL, 1, 1000, defaultSliderValue);
        zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    final double newScale = (double)zoomSlider.getValue()/defaultSliderValue;
                final PCamera camera = canvas.getCamera();
                double oldScale = camera.getViewScale();
                camera.scaleViewAboutPoint(newScale/oldScale, camera.getViewBounds().getCenterX(), camera.getViewBounds().getCenterY());
                logger.debug("Camera scaled by " + newScale/oldScale + " and is now at " + camera.getViewScale());
				LayoutPanel.this.layout.setZoomLevel(zoomSlider.getValue());
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
        zoomSlider.setValue(1);
        zoomPanel.add(zoomSlider, BorderLayout.CENTER);
		zoomPanel.add(new JLabel(WabitIcons.ZOOM_IN_ICON_16), BorderLayout.EAST);
        zoomPanel.setMaximumSize(new Dimension((int)zoomSlider.getPreferredSize().getWidth(), 200));
        toolBarBuilder.add(zoomPanel);
        toolBarBuilder.add(zoomToFitAction, "Zoom To Fit");
        toolBarBuilder.addSeparator();
        
        if (layout instanceof Template) {
        	toolBarBuilder.add(new ReportFromTemplateAction(session, (Template) layout), "Create Report");
	        toolBarBuilder.addSeparator();
        }
        
        toolBarBuilder.add(new PageFormatAction(layout.getPage()), "Page Settings");
        toolBarBuilder.add(new ExportWabitObjectAction<Layout>(session,
				layout, WabitIcons.EXPORT_ICON_32, "Export Report to Wabit file"), 
				"Export");

        if (layout instanceof Report) {
        	toolBarBuilder.add(new PrintPreviewAction(parentFrame, layout), "Preview");
        	toolBarBuilder.add(new PrintAction(layout, toolBarBuilder.getToolbar(), session), "Print");
        	toolBarBuilder.add(new PDFAction(session, toolBarBuilder.getToolbar(), layout), "Print PDF");
        }

        canvasScrollPane = new PScrollPane(canvas);
		canvasScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        
        sourceList = new JList(new DraggableWabitObjectListModel(session.getWorkspace()));
        sourceListScrollPane = new JScrollPane(sourceList);
        sourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // TODO factor out the guts of WorkspaceTreeCellRenderer so this can be less ugly
        sourceList.setCellRenderer(new DefaultListCellRenderer() {
            final JTree dummyTree = new JTree();
            final WorkspaceTreeCellRenderer delegate = new WorkspaceTreeCellRenderer();
        	@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return delegate.getTreeCellRendererComponent(
				        dummyTree, value, isSelected, false, true, 0, cellHasFocus);
			}
		});
        
        DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(sourceList, DnDConstants.ACTION_COPY, new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent dge) {
				if (sourceList.getSelectedValues() == null || sourceList.getSelectedValues().length <= 0) {
					return;
				}
				List<WabitObject> queries = new ArrayList<WabitObject>();
				for (Object q : sourceList.getSelectedValues()) {
					queries.add((WabitObject) q);
				}
				Transferable dndTransferable = new ReportQueryTransferable(queries);
				dge.getDragSource().startDrag(dge, null, dndTransferable, new DragSourceAdapter() {
					//This is a drag source adapter with empty methods.
				});
			}
		});
		new DropTarget(canvas, new QueryDropListener());
		
        panel = new JPanel(new BorderLayout());
        panel.add(canvasScrollPane, BorderLayout.CENTER);
        panel.add(toolBarBuilder.getToolbar(), BorderLayout.NORTH);
        
        panel.getActionMap().put(cancelBoxCreateAction.getClass(), cancelBoxCreateAction);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelBoxCreateAction.getClass());
        
        canvasScrollPane.addComponentListener(new ComponentAdapter() {
        	@Override
        	public void componentResized(ComponentEvent e) {
        		zoomToFit();
        	}
        });
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

	public Layout getLayout() {
		return layout;
	}

	public PageNode getPageNode() {
		return pageNode;
	}

	public CursorManager getCursorManager() {
		return cursorManager;
	}

	public String getTitle() {
		if (layout instanceof Report) {
			return "Report Editor - " + layout.getName();
		} else if (layout instanceof Template) {
			return "Template Editor - " + layout.getName();
		} else {
			throw new UnsupportedOperationException(
					"Layout panel only supports Layout's of " +
					"type Report and Template not of type " + layout.getClass());
		}
	}
	
	public JComponent getSourceComponent() {
	    return sourceListScrollPane;
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
	
	private void zoomToFit() {
		Rectangle rect = canvas.getVisibleRect();
		Page page = pageNode.getModel();
		double zoom = Math.min(rect.getHeight() / page.getHeight(),
				rect.getWidth() / page.getWidth());
		zoom *= OVER_ZOOM_COEFF;
		logger.debug("zoom = " + zoom);
		canvas.getCamera().setViewScale(zoom);
		zoomSlider.setValue((int)((zoomSlider.getMaximum() - zoomSlider.getMinimum()) / 2 * zoom));
		double x = (rect.getWidth() - (page.getWidth() * zoom)) / 2;
		double y = (rect.getHeight() - (page.getHeight() * zoom)) / 2;
		logger.debug("camera x = " + x + ", camera y = " + y);
		canvas.getCamera().setViewOffset(x, y);
	}
}
