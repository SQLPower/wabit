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
import java.util.Collections;
import java.util.Iterator;
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
import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.validation.swingui.StatusComponent;
import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.ItemContainer;
import ca.sqlpower.wabit.query.QueryCache;
import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.wabit.query.TableContainer;
import ca.sqlpower.wabit.swingui.QueryPanel;
import ca.sqlpower.wabit.swingui.WabitSwingSession;
import ca.sqlpower.wabit.swingui.event.CreateJoinEventHandler;
import ca.sqlpower.wabit.swingui.event.QueryPenSelectionEventHandler;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * The pen where users can graphically create sql queries.
 */
public class QueryPen implements MouseState {
	
	private static Logger logger = Logger.getLogger(QueryPen.class);
	
	private static final Color SELECTION_COLOUR = new Color(0xcc333333);
	
    private static final String DELETE_ACTION = "Delete";
    
    private static final String ZOOM_IN_ACTION = "Zoom In";
    
    private static final String ZOOM_OUT_ACTION = "Zoom Out";

    private static final String JOIN_ACTION = "Create Join";
    
    private static final int TABLE_SPACE = 5;
    
    private JPanel panel;
    
    
    private AbstractAction zoomInAction;
    private AbstractAction zoomOutAction;
    private JToolBar queryPenBar;
    
	private final class QueryPenDropTargetListener implements
			DropTargetListener {
		
		private QueryPen mouseState ;

		public QueryPenDropTargetListener(QueryPen mouseState) {
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
			int tempTranslateLocation = 0;
			for (Object arrayListObject : (ArrayList<?>)draggedObject) {
				if (!(arrayListObject instanceof int[])) {
					continue;
				}
				int[] path = (int[]) arrayListObject;
				SQLObject draggedSQLObject;
				try {
					draggedSQLObject = DnDTreePathTransferable.getNodeForDnDPath(queryPanel.getRootNode(), path);
				} catch (ArchitectException e1) {
					throw new RuntimeException(e1);
				}

				if (draggedSQLObject instanceof SQLTable) {
					SQLTable table = (SQLTable) draggedSQLObject;
					TableContainer model = new TableContainer(table);

					ContainerPane pane = new ContainerPane(mouseState, canvas, model);
					pane.addQueryChangeListener(queryChangeListener);
					Point location = dtde.getLocation();
					Point2D movedLoc = canvas.getCamera().localToView(location);
					pane.translate(movedLoc.getX() + tempTranslateLocation, movedLoc.getY());
					tempTranslateLocation += pane.getWidth() + TABLE_SPACE;
					
					int aliasCounter = 0;
					ArrayList<String> aliasNames = new ArrayList<String>();
					
					// This basically check if there exist a table with the same name as the one being dropped
					// compare all the alias names to see which number it needs to not create a duplicate table name.
					for(Object node: topLayer.getAllNodes()) {
						if(node instanceof ContainerPane) {
							ContainerPane containerNode = (ContainerPane)node;
							if(containerNode.getModelTextName().equals(pane.getModelTextName())){
								logger.debug("Found same tableName, going to alias");
								aliasNames.add(containerNode.getModel().getAlias());
							}
						}
					}
					Collections.sort(aliasNames);
					for(String alias : aliasNames) {
						if(alias.equals(pane.getModel().getName()+ "_"+ aliasCounter)
								|| alias.equals("")) {
							aliasCounter++;
						}
					}
					if(aliasCounter != 0) {
						pane.setContainerAlias(pane.getModel().getName()+ "_"+ aliasCounter);
					}

					topLayer.addChild(pane);
					queryChangeListener.propertyChange(new PropertyChangeEvent(canvas, Container.PROPERTY_TABLE_ADDED, null, pane.getModel()));
					for (UnmodifiableItemPNode itemNode : pane.getContainedItems()) {
						itemNode.setInSelected(true);
					}
					
					try {
						for (SQLRelationship relation : table.getExportedKeys()) {
							List<ContainerPane> fkContainers = getContainerPane(relation.getFkTable());
							for (ContainerPane fkContainer : fkContainers) {
								for (ColumnMapping mapping : relation.getMappings()) {
									logger.debug("PK container has model name " + pane.getModel().getName() + " looking for col named " + mapping.getPkColumn().getName());
									UnmodifiableItemPNode pkItemNode = pane.getItemPNode(mapping.getPkColumn());
									UnmodifiableItemPNode fkItemNode = fkContainer.getItemPNode(mapping.getFkColumn());
									logger.debug("FK item node is " + fkItemNode);
									if (pkItemNode != null && fkItemNode != null) {
										JoinLine join = new JoinLine(QueryPen.this, canvas, pkItemNode, fkItemNode);
										join.getModel().addJoinChangeListener(queryChangeListener);
										joinLayer.addChild(join);
										for (PropertyChangeListener l : queryListeners) {
											l.propertyChange(new PropertyChangeEvent(canvas, SQLJoin.PROPERTY_JOIN_ADDED, null, join.getModel()));
										}
									} else {
										throw new IllegalStateException("Trying to join two columns, one of which does not exist");
									}
								}
							}
						}
						
						for (SQLRelationship relation : table.getImportedKeys()) {
							List<ContainerPane> pkContainers = getContainerPane(relation.getPkTable());
							for (ContainerPane pkContainer : pkContainers) {
								for (ColumnMapping mapping : relation.getMappings()) {
									UnmodifiableItemPNode fkItemNode = pkContainer.getItemPNode(mapping.getPkColumn());
									UnmodifiableItemPNode pkItemNode = pane.getItemPNode(mapping.getFkColumn());
									if (pkItemNode != null && fkItemNode != null) {
										logger.debug(" pkItemNode" + ((ContainerPane)pkItemNode.getParent()).getModel().getName());
										logger.debug(" fkItemNode" + ((ContainerPane)fkItemNode.getParent()).getModel().getName());
										JoinLine join = new JoinLine(QueryPen.this, canvas, fkItemNode, pkItemNode);
										join.getModel().addJoinChangeListener(queryChangeListener);
										joinLayer.addChild(join);
										for (PropertyChangeListener l : queryListeners) {
											l.propertyChange(new PropertyChangeEvent(canvas, SQLJoin.PROPERTY_JOIN_ADDED, null, join.getModel()));
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

	private final String acceleratorKeyString;
	
	/**
	 * This is the queryPen's model
	 */
	private final QueryCache model;
	
	/**
	 * The mouse state in this query pen.
	 */
	private MouseStates mouseState = MouseStates.READY;
	
	/**
	 * A SelectionEventHandler that supports multiple select on Tables for deletion and dragging.
	 */
	private PSelectionEventHandler selectionEventHandler;
	
    /**
     * The cursor manager for this Query pen.
     */
	private final CursorManager cursorManager;
	
	/**
	 * This is the container pane that will hold constants to allow users to join on special
	 * things or add unusual values to a select statement.
	 */
	private ConstantsPane constantsContainer;

	/**
	 * Deletes the selected item from the QueryPen.
	 */
	private final Action deleteAction = new AbstractAction(){
		public void actionPerformed(ActionEvent e) {

			Iterator<?> selectedIter = selectionEventHandler.getSelection().iterator();
			while(selectedIter.hasNext()) {
				PNode pickedNode = (PNode) selectedIter.next();
				if (pickedNode.getParent() == topLayer) {
					if (pickedNode == constantsContainer) {
						return;
					}
					topLayer.removeChild(pickedNode);
					if (pickedNode instanceof ContainerPane) {
						ContainerPane pane = ((ContainerPane)pickedNode);
						List<UnmodifiableItemPNode> items = pane.getContainedItems();

						for(UnmodifiableItemPNode item : items) {
							List<JoinLine> joinedLines = item.getJoinedLines();
							for(int i = joinedLines.size()-1; i >= 0 ; i--) {
								if(joinedLines.get(i).getParent() == joinLayer) {
									deleteJoinLine(joinedLines.get(i));
								}
							}
						}
						pane.removeQueryChangeListener(queryChangeListener);
						
						queryChangeListener.propertyChange(new PropertyChangeEvent(canvas, Container.PROPERTY_TABLE_REMOVED, pane.getModel(), null));
					}
				}
				if (pickedNode.getParent() == joinLayer) {
					deleteJoinLine((JoinLine)pickedNode);
				}
			}
		}
	};
	/**
	 * This method will remove the Joined line from its left and right Nodes and remove it from the joinLayer
	 * It also fires the propertyChange event to update the query
	 */
	public void deleteJoinLine(JoinLine pickedNode) {
		pickedNode.disconnectJoin();
		joinLayer.removeChild(pickedNode);
		for (PropertyChangeListener l : queryListeners) {
			l.propertyChange(new PropertyChangeEvent(canvas, SQLJoin.PROPERTY_JOIN_REMOVED, pickedNode.getModel(), null));
		}
	}
	
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
			logger.debug("Query pen received state change.");
			for (PropertyChangeListener l : queryListeners) {
				l.propertyChange(evt);
			}
		}
	};

	private final QueryPanel queryPanel;
	
	public JPanel createQueryPen() {
        panel.setLayout(new BorderLayout());
        panel.add(getScrollPane(), BorderLayout.CENTER);
        ImageIcon joinIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/delete.png"));
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
	
	public QueryPen(WabitSwingSession s, QueryPanel queryPanel, QueryCache model) {
		this.queryPanel = queryPanel;
		this.model = model;
		panel = new JPanel();
	    cursorManager = new CursorManager(panel);
		if(s.getContext().isMacOSX()) {
			acceleratorKeyString = "Cmd";
		} else {
			acceleratorKeyString= "Ctrl";
		}
		canvas = new PSwingCanvas();
		canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING); 
		scrollPane = new PScrollPane(canvas);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		
		canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
	                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ACTION);
	    canvas.getActionMap().put(DELETE_ACTION, deleteAction);
	    

        canvas.setPanEventHandler( null );
        topLayer = canvas.getLayer();
        joinLayer = new PLayer();
        canvas.getRoot().addChild(joinLayer);
        canvas.getCamera().addLayer(0, joinLayer);
        
        constantsContainer = new ConstantsPane(this, canvas, new ItemContainer("CONSTANTS"));
        constantsContainer.addChangeListener(queryChangeListener);
        topLayer.addChild(constantsContainer);
        
        ImageIcon zoomInIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/zoom_in16.png"));
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
        ImageIcon zoomOutIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/zoom_out16.png"));
        
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
        
        ImageIcon joinIcon = new ImageIcon(StatusComponent.class.getClassLoader().getResource("icons/join.png"));
        AbstractAction joinAction = new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        		setMouseState(MouseStates.CREATE_JOIN);
        		cursorManager.placeModeStarted();
        	}
        };
        createJoinButton = new JButton(joinAction);
        createJoinButton.setToolTipText(JOIN_ACTION + " (Shortcut "+ acceleratorKeyString+ " J)");
        createJoinButton.setIcon(joinIcon);
        canvas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
                
                , JOIN_ACTION);
        canvas.getActionMap().put(JOIN_ACTION, joinAction);
        
        CreateJoinEventHandler createJoinListener = new CreateJoinEventHandler(this, joinLayer, canvas, cursorManager);
		canvas.addInputEventListener(createJoinListener);
		createJoinListener.addCreateJoinListener(queryChangeListener);
        
        new DropTarget(canvas, new QueryPenDropTargetListener(this));
        List<PLayer> layerList = new ArrayList<PLayer>();
        layerList.add(topLayer);
        layerList.add(joinLayer);
        selectionEventHandler = new QueryPenSelectionEventHandler(topLayer, layerList);
        selectionEventHandler.setMarqueePaint(SELECTION_COLOUR);
        selectionEventHandler.setMarqueePaintTransparency(SELECTION_TRANSPARENCY);
		canvas.addInputEventListener(selectionEventHandler);
		
		globalWhereText = new JTextField();
		globalWhereText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				queryChangeListener.propertyChange(new PropertyChangeEvent(globalWhereText, Container.PROPERTY_WHERE_MODIFIED, globalWhereText.getText(), globalWhereText.getText()));
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
					queryChangeListener.propertyChange(new PropertyChangeEvent(globalWhereText, Container.PROPERTY_WHERE_MODIFIED, globalWhereText.getText(), globalWhereText.getText()));
				}
			}
			public void keyPressed(KeyEvent e) {
				//Do nothing
			}
		});
	}
	public PSelectionEventHandler getMultipleSelectEventHandler(){
		return selectionEventHandler;
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
	
	public PLayer getJoinLayer() {
		return joinLayer;
	}
	
	public PLayer getTopLayer() {
		return topLayer;
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
	
	public CursorManager getCursorManager() {
		return cursorManager;
	}

	/**
	 * Returns a list of container panes, where each one wraps the same
	 * SQLTable, in the QueryPen. If no container panes wraps the SQLTable in
	 * the QueryPen then this will return an empty list.
	 */
	private List<ContainerPane> getContainerPane(SQLTable table) {
		List<ContainerPane> containerList = new ArrayList<ContainerPane>();
		for (Object node : topLayer.getAllNodes()) {
			if (node instanceof ContainerPane && ((ContainerPane)node).getModel().getContainedObject() == table) {
				containerList.add((ContainerPane)node);
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
	
	public QueryCache getModel() {
		return model;
	}
	
	public String getAcceleratorKeyString () {
		return acceleratorKeyString;
	}
	
	public PSwingCanvas getQueryPenCavas () {
		return canvas;
	}
}
