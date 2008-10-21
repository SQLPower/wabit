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
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.border.LineBorder;

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
	
	private final ContainerModel<C> model;

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
	private MouseStatePane mouseStates;
	
	/**
	 * The canvas this component is being drawn on.
	 */
	private PCanvas canvas;
	
	/**
	 * All of the {@link PStyledText} objects that represent an object in the model.
	 */
	private List<SQLColumnPNode> containedItems;
	
	public ContainerPane(MouseStatePane pen, PCanvas canvas) {
		this(pen, canvas, new ContainerModel<C>());
	}
	
	public ContainerPane(MouseStatePane pen, PCanvas canvas, ContainerModel<C> newModel) {
		model = newModel;
		this.mouseStates = pen;
		this.canvas = canvas;
		containedItems = new ArrayList<SQLColumnPNode>();
		logger.debug("Model name is " + model.getName());
		final PStyledText modelNameText = new PStyledText();
		JEditorPane nameEditor = new JEditorPane();
		nameEditor.setBorder(new LineBorder(nameEditor.getForeground()));
		nameEditor.setText(model.getName());
		modelNameText.setDocument(nameEditor.getDocument());
		addChild(modelNameText);
		
		int yLoc = 1;
		for (int i = 0; i < model.getContainerCount(); i++) {
			for (int j = 0; j < model.getContainerSize(i); j++) {
				final SQLColumnPNode newText = createTextLine(model.getContents(i, j));
				newText.translate(0, modelNameText.getHeight() * yLoc);
				addChild(newText);
				containedItems.add(newText);
				yLoc++;
			}
		}
		
		PBounds fullBounds = getFullBounds();
		this.addChild(PPath.createLine((float)getX() - BORDER_SIZE, (float)(getY() + modelNameText.getHeight()), (float)(getX() + fullBounds.width + BORDER_SIZE), (float)(getY() + modelNameText.getHeight())));
		outerRect = PPath.createRectangle((float)fullBounds.x - BORDER_SIZE, (float)fullBounds.y - BORDER_SIZE, (float)fullBounds.width + BORDER_SIZE * 2, (float)fullBounds.height + BORDER_SIZE * 2);
		this.addChild(outerRect);
		outerRect.moveToBack();
		setBounds(outerRect.getBounds());
	}

	/**
	 * Creates a {@link PStyledText} object that is editable by clicking on it.
	 */
	private SQLColumnPNode createTextLine(SQLObject sqlColumn) {
		final SQLColumnPNode modelNameText = new SQLColumnPNode(mouseStates, canvas, sqlColumn);
		modelNameText.getColumnText().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (outerRect != null) {
					outerRect.setWidth(Math.max(modelNameText.getColumnText().getWidth() + 2 * BORDER_SIZE, outerRect.getWidth()));
					setBounds(outerRect.getBounds());
				}
			}
		});
		return modelNameText;
	}
		
	public ContainerModel<C> getModel() {
		return model;
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

}
