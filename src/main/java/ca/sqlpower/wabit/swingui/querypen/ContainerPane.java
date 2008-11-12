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

import java.awt.event.ActionEvent;
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

import ca.sqlpower.wabit.query.Container;
import ca.sqlpower.wabit.query.Item;
import ca.sqlpower.wabit.query.Section;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
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
public class ContainerPane extends PNode {
	
	private static Logger logger = Logger.getLogger(ContainerPane.class);

	/**
	 * The size of the border to place around the text in this container pane
	 * for readability.
	 */
	private static final int BORDER_SIZE = 5;
	
	/**
	 * The size of separators between different fields.
	 */
	private static final int SEPARATOR_SIZE = 5;
	
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
	private QueryPen mouseStates;
	
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
	private List<UnmodifiableItemPNode> containedItems;
	
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
			
			if (editing) {
				createAliasName();
			}
			editing = false;

		}
		
		public void editingStarting() {
			editing = true;
			if (model.getAlias() != null && model.getAlias().length() > 0) {
				modelNameText.getEditorPane().setText(model.getAlias());
				logger.debug("Setting editor text to " + model.getAlias());
			}
		}
	};
	
	
	private void createAliasName() {
		JEditorPane nameEditor = modelNameText.getEditorPane();
		String name = model.getName();
		if (nameEditor.getText() != null && nameEditor.getText().length() > 0 && !nameEditor.getText().equals(name)) {
			model.setAlias(nameEditor.getText());
			nameEditor.setText(model.getAlias() + " (" + name + ")");
		} else {
			logger.debug("item name is " + name);
			nameEditor.setText(name);
			model.setAlias("");
		}
		logger.debug("editor has text " + nameEditor.getText() + " alias is " + model.getAlias());
		modelNameText.syncWithDocument();
		
	}
	
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
			repositionWhereAndResize();
		}
	};

	private EditablePStyledText modelNameText;

	/**
	 * This is the header that defines which column is the select check boxes,
	 * which column is the column name and alias, and which column is the where
	 * clause.
	 */
	private final PNode header;

	/**
	 * This is the header for column names and aliases.
	 */
	private PStyledText columnNameHeader;

	public ContainerPane(QueryPen pen, PCanvas canvas, Container newModel) {
		model = newModel;
		model.addChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Container.CONTAINTER_ITEM_ADDED)) {
					addItem((Item)evt.getNewValue());
					logger.debug("Added " + ((Item)evt.getNewValue()).getName() + " to the container pane.");
				} else if (evt.getPropertyName().equals(Container.CONTAINER_ITEM_REMOVED)) {
					removeItem((Item)evt.getOldValue());
				} else if (evt.getPropertyName().equals(Container.CONTAINTER_ALIAS_CHANGED)) {
					for (PropertyChangeListener l : queryChangeListeners) {
						l.propertyChange(evt);
					}
				}
			}
		});
		queryChangeListeners = new ArrayList<PropertyChangeListener>();
		this.mouseStates = pen;
		this.canvas = canvas;
		containedItems = new ArrayList<UnmodifiableItemPNode>();
		separatorLines = new ArrayList<PPath>();
		logger.debug("Model name is " + model.getName());
		model.setAlias("");
		modelNameText = new EditablePStyledText(model.getName(), pen, canvas);
		modelNameText.addEditStyledTextListener(editingTextListener);
		modelNameText.addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		modelNameText.addInputEventListener(new PBasicInputEventHandler() {
			
			@Override
			public void mousePressed(PInputEvent event){
				if(!mouseStates.getMultipleSelectEventHandler().isSelected(ContainerPane.this)){
					mouseStates.getMultipleSelectEventHandler().unselectAll();
				}
				mouseStates.getMultipleSelectEventHandler().select(ContainerPane.this);
			
		}});
		addChild(modelNameText);
		
		header = createColumnHeader();
		header.translate(0, modelNameText.getHeight()+ BORDER_SIZE);
		addChild(header);
		
		int yLoc = 2;
		for (Section sec : model.getSections()) {
			for (Item item : sec.getItems()) {
				final UnmodifiableItemPNode newText = createTextLine(item);
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
	 * Creates a {@link PStyledText} object that is editable by clicking on it
	 * if it's a column, and not editable if it's a table from which everything
	 * is being selected.
	 */
	private UnmodifiableItemPNode createTextLine(Item item) {
		final UnmodifiableItemPNode modelNameText;
		modelNameText = new UnmodifiableItemPNode(mouseStates, canvas, item);
		modelNameText.getItemText().addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		modelNameText.getWherePStyledText().addPropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
		modelNameText.addQueryChangeListener(itemChangeListener);
		return modelNameText;
	}
	
	private PNode createColumnHeader() {
		
		PNode itemHeader = new PNode();
		JCheckBox allCheckBox = new JCheckBox();
		allCheckBox.addActionListener(new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				for (UnmodifiableItemPNode itemNode : containedItems) {
					if(itemNode.isInSelect() != ((JCheckBox)e.getSource()).isSelected()) {
						itemNode.setInSelected(((JCheckBox)e.getSource()).isSelected());						
					}
				}
			} 
		});
		allCheckBox.setSelected(true);
		swingCheckBox = new PSwing(allCheckBox);
		itemHeader.addChild(swingCheckBox);
		
		columnNameHeader = new EditablePStyledText("Column/Alias", mouseStates, canvas);
		double textYTranslation = (swingCheckBox.getFullBounds().height - columnNameHeader.getFullBounds().height)/2;
		columnNameHeader.translate(swingCheckBox.getFullBounds().width + SEPARATOR_SIZE, textYTranslation);
		itemHeader.addChild(columnNameHeader);
		
		whereHeader = new EditablePStyledText("WHERE:", mouseStates, canvas);
		whereHeader.translate(0, textYTranslation);
		itemHeader.addChild(whereHeader);
	
		return itemHeader;
	}
	
		public Container getModel() {
			return model;
	}
		
		public String getModelTextName() {
			return model.getName();
		}

	/**
	 * Returns the ItemPNode that represents the Item that contains the object
	 * passed into this method. If there is no ItemPNode in this container that
	 * represents the given item null is returned.
	 */
	public UnmodifiableItemPNode getItemPNode(Object item) {
		Item itemInModel = model.getItem(item);
		if (itemInModel == null) {
			logger.debug("Item " + item  + " not in model.");
			return null;
		}
		for (UnmodifiableItemPNode itemNode : containedItems) {
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
		double maxXPos = swingCheckBox.getFullBounds().width + SEPARATOR_SIZE + columnNameHeader.getWidth() + SEPARATOR_SIZE;
		for (UnmodifiableItemPNode itemNode : containedItems) {
			maxXPos = Math.max(maxXPos, itemNode.getDistanceForWhere());
		}
		whereHeader.translate(maxXPos - whereHeader.getXOffset(), 0);
		for (UnmodifiableItemPNode itemNode : containedItems) {
			itemNode.positionWhere(maxXPos);
		}
	}
	
	public List<UnmodifiableItemPNode> getContainedItems() {
		return Collections.unmodifiableList(containedItems);
	}

	private void addItem(Item item) {
		UnmodifiableItemPNode itemNode = createTextLine(item);
		itemNode.translate(0, (modelNameText.getHeight() + BORDER_SIZE) * (2 + containedItems.size()) + BORDER_SIZE);
		addChild(itemNode);
		containedItems.add(itemNode);
		repositionWhereAndResize();
	}
	
	private void removeItem(Item item) {
		UnmodifiableItemPNode itemNode = getItemPNode(item.getItem());
		if (itemNode != null) {
			int containedItemsLocation = containedItems.indexOf(itemNode);
			removeChild(itemNode);
			containedItems.remove(itemNode);
			itemNode.getItemText().removePropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
			itemNode.getWherePStyledText().removePropertyChangeListener(PNode.PROPERTY_BOUNDS, resizeOnEditChangeListener);
			itemNode.removeQueryChangeListener(itemChangeListener);
			for (int i = containedItemsLocation; i < containedItems.size(); i++) {
				containedItems.get(i).translate(0, - modelNameText.getHeight() - BORDER_SIZE);
			}
			repositionWhereAndResize();
		}
	}
	
	private void repositionWhereAndResize() {
		repositionWhereClauses();
		if (outerRect != null) {
			double maxWidth = Math.max(header.getFullBounds().getWidth(), modelNameText.getFullBounds().getWidth());
			logger.debug("Header width is " + header.getFullBounds().getWidth() + " and the container name has width " + modelNameText.getFullBounds().getWidth());
			for (UnmodifiableItemPNode node : containedItems) {
				maxWidth = Math.max(maxWidth, node.getFullBounds().getWidth());
			}
			logger.debug("Max width of the container pane is " + maxWidth);
			maxWidth += 2 * BORDER_SIZE;
			outerRect.setWidth(maxWidth);
			for (PPath line : separatorLines) {
				line.setWidth(maxWidth);
			}
			
			int numStaticRows = 2;
			outerRect.setHeight((modelNameText.getHeight() + BORDER_SIZE) * (numStaticRows + containedItems.size()) + BORDER_SIZE * 3);
			
			setBounds(outerRect.getBounds());
		}
	}

	public void setContainerAlias(String newAlias) {
		modelNameText.getEditorPane().setText(newAlias);
		createAliasName();
	}
	
}
