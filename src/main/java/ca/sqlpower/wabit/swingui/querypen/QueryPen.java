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
import java.awt.Toolkit;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLRelationship;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLRelationship.ColumnMapping;
import ca.sqlpower.architect.swingui.dbtree.DnDTreePathTransferable;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.swingui.Container;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.event.CreateJoinEventHandler;
import ca.sqlpower.wabit.swingui.event.QueryPenSelectionEventHandler;
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
	
	public static final String PROPERTY_TABLE_ADDED = "TABLE_ADDED";
	
	public static final String PROPERTY_TABLE_REMOVED = "TABLE_REMOVED";
	
	public static final String PROPERTY_WHERE_MODIFIED = "WHERE_MODIFIED";
	
	public static final String PROPERTY_JOIN_ADDED = "JOIN_ADDED";
	
	public static final String PROPERTY_JOIN_REMOVED = "JOIN_REMOVED";
	
	private static final Color SELECTION_COLOUR = new Color(0xcc333333);
	
    private static final String DELETE_ACTION = "Delete";
    
    private static final String ZOOM_IN_ACTION = "Zoom In";
    
    private static final String ZOOM_OUT_ACTION = "Zoom Out";
    
    private static final String JOIN_ACTION = "Create Join";

    
    private AbstractAction zoomInAction;
    private AbstractAction zoomOutAction;
    private JToolBar queryPenBar;
    
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
					pane.addQueryChangeListener(queryChangeListener);
					Point location = dtde.getLocation();
					Point2D movedLoc = canvas.getCamera().localToView(location);
					pane.translate(movedLoc.getX(), movedLoc.getY());
					topLayer.addChild(pane);
					queryChangeListener.propertyChange(new PropertyChangeEvent(canvas, PROPERTY_TABLE_ADDED, null, pane));
					for (ItemPNode itemNode : pane.getContainedItems()) {
						itemNode.setInSelected(true);
					}
					
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
										JoinLine join = new JoinLine(QueryPen.this, canvas, pkItemNode, fkItemNode);
										joinLayer.addChild(join);
										for (PropertyChangeListener l : queryListeners) {
											l.propertyChange(new PropertyChangeEvent(canvas, PROPERTY_JOIN_ADDED, null, join));
										}
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
										JoinLine join = new JoinLine(QueryPen.this, canvas, pkItemNode, fkItemNode);
										joinLayer.addChild(join);
										for (PropertyChangeListener l : queryListeners) {
											l.propertyChange(new PropertyChangeEvent(canvas, PROPERTY_JOIN_ADDED, null, join));
										}
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
	
	/**
	 * This text area is for any part of the WHERE clause
	 * that a user would want to add in that is not specific
	 * to a column in a table.
	 */
	private final JTextField globalWhereText;

	private final WabitSwingSession session;
	
	private final String acceleratorKeyString;
	
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
	private final Action deleteAction = new AbstractAction(){
		public void actionPerformed(ActionEvent e) {
			if (lastPickPath != null) {
				PNode pickedNode = lastPickPath.getPickedNode();
				if (pickedNode.getParent() == topLayer) {
					topLayer.removeChild(pickedNode);
					if (pickedNode instanceof ContainerPane<?>) {
						ContainerPane<?> pane = ((ContainerPane<?>)pickedNode);
						pane.removeQueryChangeListener(queryChangeListener);
						queryChangeListener.propertyChange(new PropertyChangeEvent(canvas, PROPERTY_TABLE_REMOVED, pane, null));
					}
				}
				if (pickedNode.getParent() == joinLayer) {
					joinLayer.removeChild(pickedNode);
					if (pickedNode instanceof JoinLine) {
						JoinLine join = (JoinLine) pickedNode;
						for (PropertyChangeListener l : queryListeners) {
							l.propertyChange(new PropertyChangeEvent(canvas, PROPERTY_JOIN_REMOVED, join, null));
						}
					}
				}
			}
		}
	};
	
	/**
	 * Listeners that will be notified when the query string has been modified.
	 */
	private List<PropertyChangeListener> queryListeners = new ArrayList<PropertyChangeListener>();

	/**
	 * This change listener will be invoked whenever a change is made to the query pen
	 * that will result in a change to the SQL script.
	 */
	private PropertyChangeListener queryChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			for (PropertyChangeListener l : queryListeners) {
				l.propertyChange(evt);
			}
		}
	};
	
	public JPanel createQueryPen() {
		JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(getScrollPane(), BorderLayout.CENTER);
        ImageIcon joinIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/wabit/swingui/querypen/delete.png"));
        JButton deleteButton = new JButton(getDeleteAction());
        deleteButton.setToolTipText(DELETE_ACTION+ " (Shortcut Delete)");
        deleteButton.setIcon(joinIcon);
        
        queryPenBar = new JToolBar(JToolBar.VERTICAL);
        queryPenBar.setToolTipText("QueryPen Toolbar");
        queryPenBar.add(getZoomInButton());
        queryPenBar.add(getZoomOutButton());
        queryPenBar.add(getCreateJoinButton());
        queryPenBar.add(deleteButton);
        
        panel.add(queryPenBar, BorderLayout.EAST);
        panel.setBackground(Color.WHITE);
		return panel;
	}

	public QueryPen(WabitSwingSession s) {
		session = s;
		if(s.getContext().isMacOSX()) {
			acceleratorKeyString = "Cmd";
		} else {
			acceleratorKeyString= "Ctrl";
		}
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
		
		canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
	                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ACTION);
	    canvas.getActionMap().put(DELETE_ACTION, deleteAction);

        canvas.setPanEventHandler( null );
        topLayer = canvas.getLayer();
        joinLayer = new PLayer();
        canvas.getRoot().addChild(joinLayer);
        canvas.getCamera().addLayer(0, joinLayer);
        
        ImageIcon zoomInIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/wabit/swingui/querypen/zoom_in16.png"));
        zoomInAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		PCamera camera = canvas.getCamera();
        		camera.setViewScale(camera.getViewScale() + ZOOM_CONSTANT);
        	}
        };
        
        zoomInButton = new JButton(zoomInAction);
        zoomInButton.setToolTipText( ZOOM_IN_ACTION+ " (Shortcut "+ acceleratorKeyString+ " Shift +)");
        zoomInButton.setIcon(zoomInIcon);
        canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK)
                
                , ZOOM_IN_ACTION);
        canvas.getActionMap().put(ZOOM_IN_ACTION, zoomInAction);
        ImageIcon zoomOutIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/wabit/swingui/querypen/zoom_out16.png"));
        
        zoomOutAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				PCamera camera = canvas.getCamera();
				if (camera.getViewScale() > ZOOM_CONSTANT) {
					camera.setViewScale(camera.getViewScale() - ZOOM_CONSTANT);
				}
			}
        };
        canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK)
                
                , ZOOM_OUT_ACTION);
        canvas.getActionMap().put(ZOOM_OUT_ACTION, zoomOutAction);
        
        zoomOutButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				PCamera camera = canvas.getCamera();
				if (camera.getViewScale() > ZOOM_CONSTANT) {
					camera.setViewScale(camera.getViewScale() - ZOOM_CONSTANT);
				}
			}
		});
        zoomOutButton.setToolTipText(ZOOM_OUT_ACTION+ " (Shortcut "+ acceleratorKeyString+ " Shift -)");
        zoomOutButton.setIcon(zoomOutIcon);
        
        ImageIcon joinIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("ca/sqlpower/wabit/swingui/querypen/join.png"));
        AbstractAction joinAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		setMouseState(MouseStates.CREATE_JOIN);
        	}
        };
        createJoinButton = new JButton(joinAction);
        createJoinButton.setToolTipText(JOIN_ACTION + " (Shortcut "+ acceleratorKeyString+ " J)");
        createJoinButton.setIcon(joinIcon);
        canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                
                , JOIN_ACTION);
        canvas.getActionMap().put(JOIN_ACTION, joinAction);
        
        CreateJoinEventHandler createJoinListener = new CreateJoinEventHandler(this, joinLayer, canvas);
		canvas.addInputEventListener(createJoinListener);
		createJoinListener.addCreateJoinListener(queryChangeListener);
        
        new DropTarget(canvas, new QueryPenDropTargetListener(this));
        List<PLayer> layerList = new ArrayList<PLayer>();
        layerList.add(topLayer);
        layerList.add(joinLayer);
        PSelectionEventHandler selectionEventHandler = new QueryPenSelectionEventHandler(topLayer, layerList);
        selectionEventHandler.setMarqueePaint(SELECTION_COLOUR);
        selectionEventHandler.setMarqueePaintTransparency(SELECTION_TRANSPARENCY);
		canvas.addInputEventListener(selectionEventHandler);
		
		globalWhereText = new JTextField();
		globalWhereText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				queryChangeListener.propertyChange(new PropertyChangeEvent(globalWhereText, PROPERTY_WHERE_MODIFIED, globalWhereText.getText(), globalWhereText.getText()));
			}
			public void focusGained(FocusEvent e) {
				//do nothing
			}
		});
		globalWhereText.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				//Do Nothing
			}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					queryChangeListener.propertyChange(new PropertyChangeEvent(globalWhereText, PROPERTY_WHERE_MODIFIED, globalWhereText.getText(), globalWhereText.getText()));
				}
			}
			public void keyPressed(KeyEvent e) {
				//Do nothing
			}
		});
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
	
	public JTextField getGlobalWhereText() {
		return globalWhereText;
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
	
	public void addQueryListener(PropertyChangeListener l) {
		queryListeners.add(l);
	}
	
	public void removeQueryListener(PropertyChangeListener l) {
		queryListeners.remove(l);
	}
	
	public JToolBar getQueryPenBar () {
		return queryPenBar;
	}
	
	public String getAcceleratorKeyString () {
		return acceleratorKeyString;
	}
	
	public PSwingCanvas getQueryPenCavas () {
		return canvas;
	}
}
