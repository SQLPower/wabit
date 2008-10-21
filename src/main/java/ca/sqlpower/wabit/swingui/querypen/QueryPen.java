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

package ca.sqlpower.wabit.swingui.querypen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.dbtree.DnDTreePathTransferable;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
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
public class QueryPen implements MouseState {
	
	private static Logger logger = Logger.getLogger(QueryPen.class);
	
	private static final Color SELECTION_COLOUR = new Color(0xcc333333);
	
	private final class QueryPenDropTargetListener implements
			DropTargetListener {
		private MouseState mouseState ;

		public QueryPenDropTargetListener(MouseState mouseState) {
			this.mouseState = mouseState;
		}
		
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//no-op
		}

		public void drop(DropTargetDropEvent dtde) {
			if (!dtde.isLocalTransfer()) {
				return;
			}
			
			if (!dtde.isDataFlavorSupported(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR)) {
				return;
			}

			Object draggedObject;
			try {
				draggedObject = dtde.getTransferable().getTransferData(DnDTreePathTransferable.TREEPATH_ARRAYLIST_FLAVOR);
			} catch (UnsupportedFlavorException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			if (draggedObject == null || !(draggedObject instanceof ArrayList)) {
				return;
			}
			
			for (int[] path : (ArrayList<int[]>)draggedObject) {
				SQLObject draggedSQLObject;
				try {
					draggedSQLObject = DnDTreePathTransferable.getNodeForDnDPath(session.getRootNode(), path);
				} catch (ArchitectException e1) {
					throw new RuntimeException(e1);
				}

				if (draggedSQLObject instanceof SQLTable) {
					SQLTable table = (SQLTable) draggedSQLObject;
					ContainerModel<SQLObject> model = new ContainerModel<SQLObject>();
					model.setName(table.getName());
					model.addContainer();
					try {
						for (SQLColumn column : table.getColumns()) {
							model.addItem(0, column);
						}
					} catch (ArchitectException e) {
						throw new RuntimeException(e);
					}

					ContainerPane<SQLObject> pane = new ContainerPane<SQLObject>(mouseState, canvas, model);
					Point location = dtde.getLocation();
					Point2D movedLoc = canvas.getCamera().localToView(location);
					pane.translate(movedLoc.getX(), movedLoc.getY());
					topLayer.addChild(pane);

					canvas.repaint();
					dtde.acceptDrop(dtde.getDropAction());
					dtde.dropComplete(true);
				} else {
					logger.debug("dragged " + draggedObject.toString());
				}
			}
			
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
        panel.setBackground(Color.WHITE);
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
        canvas.addInputEventListener(new CreateJoinEventHandler(this, joinLayer, canvas));
        
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
