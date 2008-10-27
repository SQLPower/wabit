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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;
import ca.sqlpower.architect.swingui.dbtree.DnDTreePathTransferable;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.event.CreateJoinEventHandler;
import ca.sqlpower.wabit.swingui.event.QueryPenSelectionEventHandler;

import com.jgoodies.forms.builder.ButtonStackBuilder;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;
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
			
			for (Object arrayListObject : (ArrayList<?>)draggedObject) {
				if (!(arrayListObject instanceof int[])) {
					continue;
				}
				int[] path = (int[]) arrayListObject;
				SQLObject draggedSQLObject;
				try {
					draggedSQLObject = DnDTreePathTransferable.getNodeForDnDPath(session.getRootNode(), path);
				} catch (ArchitectException e1) {
					throw new RuntimeException(e1);
				}

				if (draggedSQLObject instanceof SQLTable) {
					SQLTable table = (SQLTable) draggedSQLObject;
					Container model = new TableContainer(table);

					ContainerPane<SQLObject> pane = new ContainerPane<SQLObject>(mouseState, canvas, model);
					Point location = dtde.getLocation();
					Point2D movedLoc = canvas.getCamera().localToView(location);
					pane.translate(movedLoc.getX(), movedLoc.getY());
					topLayer.addChild(pane);
					
					try {
						for (SQLRelationship relation : table.getExportedKeys()) {
							List<ContainerPane<?>> fkContainers = getContainerPane(relation.getFkTable());
							for (ContainerPane<?> fkContainer : fkContainers) {
								for (ColumnMapping mapping : relation.getMappings()) {
									logger.debug("PK container has model name " + pane.getModel().getName() + " looking for col named " + mapping.getPkColumn().getName());
									ItemPNode pkItemNode = pane.getItemPNode(mapping.getPkColumn());
									logger.debug("PK item node is " + pkItemNode);
									logger.debug("fK container has model name " + fkContainer.getModel().getName() + " looking for col named " + mapping.getFkColumn().getName());
									ItemPNode fkItemNode = fkContainer.getItemPNode(mapping.getFkColumn());
									logger.debug("FK item node is " + fkItemNode);
									if (pkItemNode != null && fkItemNode != null) {
										joinLayer.addChild(new JoinLine(QueryPen.this, canvas, pkItemNode, fkItemNode));
									} else {
										throw new IllegalStateException("Trying to join two columns, one of which does not exist");
									}
								}
							}
						}
						
						for (SQLRelationship relation : table.getImportedKeys()) {
							List<ContainerPane<?>> pkContainers = getContainerPane(relation.getPkTable());
							for (ContainerPane<?> pkContainer : pkContainers) {
								for (ColumnMapping mapping : relation.getMappings()) {
									ItemPNode pkItemNode = pane.getItemPNode(mapping.getFkColumn());
									ItemPNode fkItemNode = pkContainer.getItemPNode(mapping.getPkColumn());
									if (pkItemNode != null && fkItemNode != null) {
										joinLayer.addChild(new JoinLine(QueryPen.this, canvas, pkItemNode, fkItemNode));
									} else {
										throw new IllegalStateException("Trying to join two columns, one of which does not exist");
									}
								}
							}
						}
					} catch (ArchitectException e) {
						throw new RuntimeException(e);
					}

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
	private final PLayer topLayer;
	
	private final JButton zoomInButton;
	private final JButton zoomOutButton;
	private final JButton createJoinButton;

	private final WabitSwingSession session;
	
	/**
	 * The mouse state in this query pen.
	 */
	private MouseStates mouseState = MouseStates.READY;
	
	/**
	 * The pick path stored from the last mouse up event.
	 */
	private PPickPath lastPickPath;

	/**
	 * Deletes the selected item from the QueryPen.
	 */
	private final Action deleteAction = new AbstractAction("Delete"){
		public void actionPerformed(ActionEvent e) {
			if (lastPickPath != null) {
				PNode pickedNode = lastPickPath.getPickedNode();
				if (pickedNode.getParent() == topLayer) {
					topLayer.removeChild(pickedNode);
				}
				if (pickedNode.getParent() == joinLayer) {
					joinLayer.removeChild(pickedNode);
				}
			}
		}
	};
	
	public JPanel createQueryPen(WabitSwingSession session) {
		JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getScrollPane(), BorderLayout.CENTER);
        ButtonStackBuilder buttonStack = new ButtonStackBuilder();
        buttonStack.addGridded(getZoomInButton());
        buttonStack.addRelatedGap();
        buttonStack.addGridded(getZoomOutButton());
        buttonStack.addUnrelatedGap();
        buttonStack.addGridded(getCreateJoinButton());
        buttonStack.addRelatedGap();
        buttonStack.addGridded(new JButton(getDeleteAction()));
        buttonStack.addUnrelatedGap();
        Action createQuery = new AbstractAction("Create Query") {
		
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				logger.debug("Query is : " + createQueryString());
			}
		};
        buttonStack.addGridded(new JButton(createQuery));
        panel.add(buttonStack.getPanel(), BorderLayout.EAST);
        panel.setBackground(Color.WHITE);
		return panel;
	}

	public QueryPen(WabitSwingSession s) {
		session = s;
		canvas = new PSwingCanvas();
		canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		scrollPane = new PScrollPane(canvas);
		
		canvas.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseReleased(PInputEvent event) {
				lastPickPath = event.getPath();
			}
		});

        canvas.setPanEventHandler( null );
        topLayer = canvas.getLayer();
        joinLayer = new PLayer();
        canvas.getRoot().addChild(joinLayer);
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
        List<PLayer> layerList = new ArrayList<PLayer>();
        layerList.add(topLayer);
        layerList.add(joinLayer);
        PSelectionEventHandler selectionEventHandler = new QueryPenSelectionEventHandler(topLayer, layerList);
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
	
	public Action getDeleteAction() {
		return deleteAction;
	}
	
	/**
	 * A basic query string generator. This is being done quickly for the
	 * demo today and should be enhanced later.
	 */
	public String createQueryString() {
		StringBuffer query = new StringBuffer();
		query.append("SELECT ");
		boolean firstSelect = true;
		
		StringBuffer from = new StringBuffer();
		from.append("FROM ");
		boolean firstFrom = true;
		
		for (Object o : topLayer.getAllNodes()) {
			if (o instanceof ContainerPane) {
				ContainerPane<?> container = (ContainerPane<?>)o;
				
				if (!firstFrom) {
					from.append(", ");
				} else {
					firstFrom = false;
				}
				from.append(((SQLObject)container.getModel().getContainedObject()).getName() + " ");
				
				for (Section section : container.getModel().getSections()) {
					for (Item item : section.getItems()) {
						ItemPNode itemNode = container.getItemPNode(item.getItem());
						if (itemNode != null && itemNode.isInSelect() && item.getItem() instanceof SQLColumn) {
							if (!firstSelect) {
								query.append(", ");
							} else {
								firstSelect = false;
							}
							SQLColumn column = (SQLColumn)item.getItem();
							query.append(column.getName() + " ");
							if (itemNode.getAlias() != null && itemNode.getAlias().length() > 0) {
								query.append("AS " + itemNode.getAlias() + " ");
							}
						}
					}
				}
			}
		}
		query.append(from.toString());
		logger.debug("Select is : "  + query);
		return query.toString();
	}

	/**
	 * Returns a list of container panes, where each one wraps the same
	 * SQLTable, in the QueryPen. If no container panes wraps the SQLTable in
	 * the QueryPen then this will return an empty list.
	 */
	private List<ContainerPane<?>> getContainerPane(SQLTable table) {
		List<ContainerPane<?>> containerList = new ArrayList<ContainerPane<?>>();
		for (Object node : topLayer.getAllNodes()) {
			if (node instanceof ContainerPane && ((ContainerPane<?>)node).getModel().getContainedObject() == table) {
				containerList.add((ContainerPane<?>)node);
			}
		}
		return containerList;
	}
}
