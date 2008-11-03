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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import javax.swing.AbstractAction;
import javax.swing.JCheckBox;


import javax.swing.JEditorPane;


import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.wabit.swingui.Container;
import ca.sqlpower.wabit.swingui.Item;
import ca.sqlpower.wabit.swingui.Section;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.nodes.PStyledText;
import edu.umd.cs.piccolox.pswing.PSwing;

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
	
	/**
	 * Defines the property change to be a name change on the container.
	 */
	public static final String PROPERTY_CONTAINTER_ALIAS = "CONTAINER_ALIAS";
	
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
	 * This is the Text for the Where ColumnHeader. We need to store the variable so we can change its position when the column names or headers get resized
	 */
	private PStyledText whereHeader; 
	
	/**
	 * this is a checkBox in the header which checks all the items checkBoxes 
	 */
	private PSwing swingCheckBox;
	
	/**
	 * All of the {@link PStyledText} objects that represent an object in the model.
	 */
	private List<ItemPNode> containedItems;
	
	/**
	 * This will store the distance for the whereHeader
	 */
	private double whereHeaderDistance = 0;
	
	/**
	 * The PPath lines that separate the header from the columns and
	 * different groups of columns.
	 */
	private List<PPath> separatorLines;
	
	/**
	 * These listeners will fire a change event when an element on this object
	 * is changed that affects the resulting generated query.
	 */
	private final Collection<PropertyChangeListener> queryChangeListeners;
	
	/**
	 * Stores the alias given to this container.
	 */
	private String containerAlias;
	
	/**
	 * A listener to properly display the alias and column name when the
	 * {@link EditablePStyledText} is switching from edit to non-edit mode and
	 * back. This listener for the nameEditor will show only the alias when the
	 * alias is being edited. When the alias is not being edited it will show
	 * the alias and column name, in brackets, if an alias is specified.
	 * Otherwise only the column name will be displayed.
	 */
	private EditStyledTextListener editingTextListener = new EditStyledTextListener() {
		/**
		 * Tracks if we are in an editing state or not. Used to keep the
		 * editingStopped method from running only once per stop edit (some
		 * cases the editingStopped can be called from multiple places on the
		 * same stopEditing).
		 */
		private boolean editing = false;
		
		public void editingStopping() {
			String oldAlias = containerAlias;
			if (editing) {
				JEditorPane nameEditor = modelNameText.getEditorPane();
				containerAlias = nameEditor.getText();
				String name;
				if (model.getContainedObject() instanceof SQLObject) {
					name = ((SQLObject)model.getContainedObject()).getName();
				} else {
					name = model.getContainedObject().toString();
				}
				if (nameEditor.getText() != null && nameEditor.getText().length() > 0 && !nameEditor.getText().equals(name)) {
					nameEditor.setText(containerAlias + " (" + name + ")");
				} else {
					logger.debug("item name is " + name);
					nameEditor.setText(name);
					containerAlias = "";
				}
				logger.debug("editor has text " + nameEditor.getText() + " alias is " + containerAlias);
				modelNameText.syncWithDocument();
			}
			editing = false;
			if (!containerAlias.equals(oldAlias)) {
				for (PropertyChangeListener l : queryChangeListeners) {
					l.propertyChange(new PropertyChangeEvent(ContainerPane.this, PROPERTY_CONTAINTER_ALIAS, oldAlias, containerAlias));
				}
			}
		}
		
		public void editingStarting() {
			editing = true;
			if (containerAlias != null && containerAlias.length() > 0) {
				modelNameText.getEditorPane().setText(containerAlias);
				logger.debug("Setting editor text to " + containerAlias);
			}
		}
	};
	
	/**
	 * A change listener for use on items stored in this container pane.
	 */
	private PropertyChangeListener itemChangeListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			for (PropertyChangeListener l : queryChangeListeners) {
				l.propertyChange(evt);
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

	private EditablePStyledText modelNameText;
	
	public ContainerPane(MouseState pen, PCanvas canvas, Container newModel) {
		model = newModel;
		queryChangeListeners = new ArrayList<PropertyChangeListener>();
		this.mouseStates = pen;
		this.canvas = canvas;
		containedItems = new ArrayList<ItemPNode>();
		separatorLines = new ArrayList<PPath>();
		logger.debug("Model name is " + model.getName());
		containerAlias = "";
		modelNameText = new EditablePStyledText(model.getName(), pen, canvas);
		modelNameText.addEditStyledTextListener(editingTextListener);
		modelNameText.addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		addChild(modelNameText);
		
		PNode header = createColumnHeader();
		header.translate(0, modelNameText.getHeight()+ BORDER_SIZE);
		addChild(header);
		
		int yLoc = 2;
		for (Section sec : model.getSections()) {
			for (Item item : sec.getItems()) {
				final ItemPNode newText = createTextLine(item);
				newText.translate(0, (modelNameText.getHeight() + BORDER_SIZE) * yLoc+ BORDER_SIZE);
				addChild(newText);
				containedItems.add(newText);
				yLoc++;
			}
		}
		repositionWhereClauses();
		
		PBounds fullBounds = getFullBounds();
		PPath headerLine = PPath.createLine((float)getX() - BORDER_SIZE, (float)(getY() +(modelNameText.getHeight()+ BORDER_SIZE)*2+ BORDER_SIZE), (float)(getX() + fullBounds.width + BORDER_SIZE), (float)(getY() + (modelNameText.getHeight()+ BORDER_SIZE)*2+ BORDER_SIZE));
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
	
	private PNode createColumnHeader() {
		
		int whereBuffer = 5;
		PNode itemHeader = new PNode();
		JCheckBox allCheckBox = new JCheckBox();
		allCheckBox.addActionListener(new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				for (ItemPNode itemNode : containedItems) {
					if(itemNode.isInSelect() != ((JCheckBox)e.getSource()).isSelected()) {
						itemNode.setInSelected(((JCheckBox)e.getSource()).isSelected());						
					}
				}
			} 
		});
		allCheckBox.setSelected(true);
		swingCheckBox = new PSwing(allCheckBox);
		itemHeader.addChild(swingCheckBox);
		
		PStyledText columnNameHeader = new EditablePStyledText("Column/Alias", mouseStates, canvas);
		double textYTranslation = (swingCheckBox.getFullBounds().height - columnNameHeader.getFullBounds().height)/2;
		columnNameHeader.translate(swingCheckBox.getFullBounds().width+ 5, textYTranslation);
		itemHeader.addChild(columnNameHeader);
		
		whereHeader = new EditablePStyledText("WHERE:", mouseStates, canvas);
		whereHeader.translate(0, textYTranslation);
		itemHeader.addChild(whereHeader);
	
		whereHeaderDistance = swingCheckBox.getFullBounds().width+ 5+ columnNameHeader.getWidth() + whereBuffer;
		
		return itemHeader;
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
			
			if (picked == swingCheckBox || picked == modelNameText) {
				return true;
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
	
	public void addQueryChangeListener(PropertyChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeListener(PropertyChangeListener l) {
		queryChangeListeners.remove(l);
	}
	
	private void repositionWhereClauses() {
		double maxXPos= whereHeaderDistance ;
		for (ItemPNode itemNode : containedItems) {
			maxXPos = Math.max(maxXPos, itemNode.getDistanceForWhere());
		}
		whereHeader.translate(maxXPos - whereHeader.getXOffset(), 0);
		for (ItemPNode itemNode : containedItems) {
			itemNode.positionWhere(maxXPos);
		}
	}
	
	public List<ItemPNode> getContainedItems() {
		return Collections.unmodifiableList(containedItems);
	}

	public String getContainerAlias() {
		return containerAlias;
	}

}
