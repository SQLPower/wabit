package ca.sqlpower.wabit.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.sqlpower.wabit.swingui.event.CreateJoinEventHandler;

import com.jgoodies.forms.builder.ButtonStackBuilder;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * The pen where users can graphically create sql queries.
 */
public class QueryPen implements MouseStatePane {
	
	private static final Color SELECTION_COLOUR = new Color(0xcc333333);
	
	private final class QueryPenDropTargetListener implements
			DropTargetListener {
		private MouseStatePane mouseState ;

		public QueryPenDropTargetListener(MouseStatePane mouseState) {
			this.mouseState = mouseState;
		}
		
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//no-op
		}

		public void drop(DropTargetDropEvent dtde) {
			System.out.println("Drop fired");
			
			Object draggedObject;
			DataFlavor flavour = null;
			for (DataFlavor f: dtde.getCurrentDataFlavors()) {
				if (f != null) {
					flavour = f;
					break;
				}
			}
			try {
				draggedObject = dtde.getTransferable().getTransferData(flavour);
			} catch (UnsupportedFlavorException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			ContainerModel<String> model = new ContainerModel<String>();
			model.setName(draggedObject.toString());
			model.addContainer();
			for (int i = 0; i < session.getTree().getModel().getChildCount(draggedObject); i++) {
				model.addItem(0, session.getTree().getModel().getChild(draggedObject, i).toString());
			}
			
			ContainerPane<String> pane = new ContainerPane<String>(mouseState, canvas, model);
			Point location = dtde.getLocation();
			Point2D movedLoc = canvas.getCamera().localToView(location);
			pane.translate(movedLoc.getX(), movedLoc.getY());
			topLayer.addChild(pane);
			
			canvas.repaint();
			dtde.acceptDrop(dtde.getDropAction());
			dtde.dropComplete(true);
		}

		public void dragOver(DropTargetDragEvent dtde) {
			//no-op
		}

		public void dragExit(DropTargetEvent dte) {
			//no-op
		}

		public void dragEnter(DropTargetDragEvent dtde) {
			//no-op
		}
	}

	protected static final double ZOOM_CONSTANT = 0.1;

	private static final float SELECTION_TRANSPARENCY = 0.33f;
	
	/**
	 * The scroll pane that contains the visual query a user is building.
	 */
	private final JScrollPane scrollPane;

	/**
	 * The Piccolo canvas that allows zooming and the JComponents are placed in.
	 */
	private final PSwingCanvas canvas;
	
	/**
	 * The layer that contains all of the join lines. This will be behind the top layer.
	 */
	private final PLayer joinLayer;
	
	/**
	 * The top layer that has the tables and columns added to it. This should be used
	 * instead of getting the first layer from the canvas.
	 */
	private final PNode topLayer;
	
	private final JButton zoomInButton;
	private final JButton zoomOutButton;
	private final JButton createJoinButton;

	private final WabitSwingSession session;
	
	/**
	 * The mouse state in this query pen.
	 */
	private MouseStates mouseState = MouseStates.READY;
	
	public static JPanel createQueryPen(WabitSwingSession session) {
		JPanel panel = new JPanel();
		QueryPen pen = new QueryPen(session);
        panel.setLayout(new BorderLayout());
        panel.add(pen.getScrollPane(), BorderLayout.CENTER);
        ButtonStackBuilder buttonStack = new ButtonStackBuilder();
        buttonStack.addGridded(pen.getZoomInButton());
        buttonStack.addRelatedGap();
        buttonStack.addGridded(pen.getZoomOutButton());
        buttonStack.addUnrelatedGap();
        buttonStack.addGridded(pen.getCreateJoinButton());
        panel.add(buttonStack.getPanel(), BorderLayout.EAST);
		return panel;
	}

	public QueryPen(WabitSwingSession s) {
		session = s;
		canvas = new PSwingCanvas();
		scrollPane = new PScrollPane(canvas);

        canvas.setPanEventHandler( null );
        topLayer = canvas.getLayer();
        joinLayer = new PLayer();
        canvas.getCamera().addLayer(0, joinLayer);
        
        zoomInButton = new JButton(new AbstractAction("Zoom In") {
        	public void actionPerformed(ActionEvent e) {
        		PCamera camera = canvas.getCamera();
        		camera.setViewScale(camera.getViewScale() + ZOOM_CONSTANT);
        	}
        });
        zoomOutButton = new JButton(new AbstractAction("Zoom Out"){
			public void actionPerformed(ActionEvent e) {
				PCamera camera = canvas.getCamera();
				if (camera.getViewScale() > ZOOM_CONSTANT) {
					camera.setViewScale(camera.getViewScale() - ZOOM_CONSTANT);
				}
			}
		});
        
        createJoinButton = new JButton(new AbstractAction("Create Join") {
        	public void actionPerformed(ActionEvent e) {
        		setMouseState(MouseStates.CREATE_JOIN);
        	}
        });
        canvas.addInputEventListener(new CreateJoinEventHandler(this, joinLayer));
        
        new DropTarget(canvas, new QueryPenDropTargetListener(this));
        PSelectionEventHandler selectionEventHandler = new PSelectionEventHandler(topLayer, topLayer);
        selectionEventHandler.setMarqueePaint(SELECTION_COLOUR);
        selectionEventHandler.setMarqueePaintTransparency(SELECTION_TRANSPARENCY);
		canvas.addInputEventListener(selectionEventHandler);
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JButton getZoomInButton() {
		return zoomInButton;
	}

	public JButton getZoomOutButton() {
		return zoomOutButton;
	}
	
	public JButton getCreateJoinButton() {
		return createJoinButton;
	}
	
	public PSwingCanvas getCanvas() {
		return canvas;
	}

	public MouseStates getMouseState() {
		return mouseState;
	}

	public synchronized void setMouseState(MouseStates mouseState) {
		this.mouseState = mouseState;
	}
}
