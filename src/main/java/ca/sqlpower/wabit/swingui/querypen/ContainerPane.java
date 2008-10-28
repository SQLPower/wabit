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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This container pane displays a list of values stored in its model. The elements displayed
 * in a container pane can be broken into groups and will be separated by a line for each 
 * group.
 * 
 * @param <C> The type of object this container is displaying.
 */
public class ContainerPane<C extends SQLObject> extends PNode {
	
	private static Logger logger = Logger.getLogger(ContainerPane.class);

	/**
	 * The size of the border to place around the text in this container pane
	 * for readability.
	 */
	private static final int BORDER_SIZE = 5;
	
	private final Container model;

	/**
	 * The outer rectangle of this component. All parts of this component should
	 * be within this rectangle and it should be resized if the components
	 * inside are changed.
	 */
	private PPath outerRect;
	
	/**
	 * The pane that contains the current state of the mouse for that this component
	 * is attached to.
	 */
	private MouseState mouseStates;
	
	/**
	 * The canvas this component is being drawn on.
	 */
	private PCanvas canvas;
	
	/**
	 * All of the {@link PStyledText} objects that represent an object in the model.
	 */
	private List<ItemPNode> containedItems;
	
	/**
	 * The PPath lines that separate the header from the columns and
	 * different groups of columns.
	 */
	private List<PPath> separatorLines;
	
	/**
	 * These listeners will fire a change event when an element on this object
	 * is changed that affects the resulting generated query.
	 */
	private final Collection<ChangeListener> queryChangeListeners;
	
	/**
	 * A change listener for use on items stored in this container pane.
	 */
	private ChangeListener itemChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			for (ChangeListener l : queryChangeListeners) {
				l.stateChanged(e);
			}
		}
	};
	
	/**
	 * This listener will resize the bounding box of the container
	 * when properties of components it is attached to change.
	 */
	private PropertyChangeListener resizeOnEditChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			repositionWhereClauses();
			if (outerRect != null) {
				double maxWidth = ContainerPane.this.getFullBounds().getWidth();
				outerRect.setWidth(maxWidth);
				for (PPath line : separatorLines) {
					line.setWidth(maxWidth);
				}
				setBounds(outerRect.getBounds());
			}
		}
	};
	
	public ContainerPane(MouseState pen, PCanvas canvas, Container newModel) {
		model = newModel;
		queryChangeListeners = new ArrayList<ChangeListener>();
		this.mouseStates = pen;
		this.canvas = canvas;
		containedItems = new ArrayList<ItemPNode>();
		separatorLines = new ArrayList<PPath>();
		logger.debug("Model name is " + model.getName());
		final PStyledText modelNameText = new EditablePStyledText(model.getName(), pen, canvas);
		addChild(modelNameText);
		
		int yLoc = 1;
		for (Section sec : model.getSections()) {
			for (Item item : sec.getItems()) {
				final ItemPNode newText = createTextLine(item);
				newText.translate(0, (modelNameText.getHeight() + BORDER_SIZE) * yLoc);
				addChild(newText);
				containedItems.add(newText);
				yLoc++;
			}
		}
		repositionWhereClauses();
		
		PBounds fullBounds = getFullBounds();
		PPath headerLine = PPath.createLine((float)getX() - BORDER_SIZE, (float)(getY() + modelNameText.getHeight()), (float)(getX() + fullBounds.width + BORDER_SIZE), (float)(getY() + modelNameText.getHeight()));
		separatorLines.add(headerLine);
		this.addChild(headerLine);
		outerRect = PPath.createRectangle((float)fullBounds.x - BORDER_SIZE, (float)fullBounds.y - BORDER_SIZE, (float)fullBounds.width + BORDER_SIZE * 2, (float)fullBounds.height + BORDER_SIZE * 2);
		this.addChild(outerRect);
		outerRect.moveToBack();
		setBounds(outerRect.getBounds());
	}

	/**
	 * Creates a {@link PStyledText} object that is editable by clicking on it.
	 */
	private ItemPNode createTextLine(Item sqlColumn) {
		final ItemPNode modelNameText = new ItemPNode(mouseStates, canvas, sqlColumn);
		modelNameText.getItemText().addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		modelNameText.getWherePStyledText().addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		modelNameText.addQueryChangeListener(itemChangeListener);
		return modelNameText;
	}
		
	public Container getModel() {
		return model;
	}

	/**
	 * Returns the ItemPNode that represents the Item that contains the object
	 * passed into this method. If there is no ItemPNode in this container that
	 * represents the given item null is returned.
	 */
	public ItemPNode getItemPNode(Object item) {
		Item itemInModel = model.getItem(item);
		if (itemInModel == null) {
			logger.debug("Item " + item  + " not in model.");
			return null;
		}
		for (ItemPNode itemNode : containedItems) {
			if (itemInModel.getItem() == itemNode.getItem().getItem()) {
				return itemNode;
			}
		}
		return null;
	}
	
	@Override
	/*
	 * Taken from PComposite. This keeps the title and container lines together in
	 * a unit but is modified to allow picking of internal components.
	 */
	public boolean fullPick(PPickPath pickPath) {
		if (super.fullPick(pickPath)) {
			PNode picked = pickPath.getPickedNode();
			
			// this code won't work with internal cameras, because it doesn't pop
			// the cameras view transform.
			for (PNode node : containedItems) {
				if (node.getAllNodes().contains(picked)) {
					return true;
				}
			}
			while (picked != this) {
				pickPath.popTransform(picked.getTransformReference(false));
				pickPath.popNode(picked);
				picked = pickPath.getPickedNode();
			}
			
			return true;
		}
		return false;
	}
	
	public void addQueryChangeListener(ChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeListener(ChangeListener l) {
		queryChangeListeners.remove(l);
	}
	
	private void repositionWhereClauses() {
		double maxXPos = 0;
		for (ItemPNode itemNode : containedItems) {
			maxXPos = Math.max(maxXPos, itemNode.getDistanceForWhere());
		}
		for (ItemPNode itemNode : containedItems) {
			itemNode.positionWhere(maxXPos);
		}
	}
	
	public List<ItemPNode> getContainedItems() {
		return Collections.unmodifiableList(containedItems);
	}

}
