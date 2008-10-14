package ca.sqlpower.wabit.swingui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.border.LineBorder;

import ca.sqlpower.wabit.swingui.event.ExtendedStyledTextEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This container pane displays a list of values stored in its model. The elements displayed
 * in a container pane can be broken into groups and will be separated by a line for each 
 * group.
 * 
 * @param <C> The type of object this container is displaying.
 */
public class ContainerPane<C extends Object> extends PNode {

	/**
	 * The size of the border to place around the text in this container pane
	 * for readability.
	 */
	private static final int BORDER_SIZE = 5;
	
	/**
	 * A simple drag handler that only allows dragging the object if the picked
	 * object is the same as the object passed in the constructor.
	 */
	private class DragTargetEventHandler extends PDragEventHandler {
		private Object parent;
		
		/**
		 * The parent is the object this event handler should be allowed to drag.
		 */
		public DragTargetEventHandler(Object parent) {
			this.parent = parent;
		}
		
		@Override
		protected boolean shouldStartDragInteraction(PInputEvent e) {
			if (super.shouldStartDragInteraction(e)) {
				return e.getPickedNode() == parent;
			}
			return false;
		}
	};
	
	private final ContainerModel<C> model;

	/**
	 * The outer rectangle of this component. All parts of this component should
	 * be within this rectangle and it should be resized if the components
	 * inside are changed.
	 */
	private PPath outerRect;
	
	/**
	 * The canvas this component is being drawn on.
	 */
	private PCanvas canvas;
	
	/**
	 * All of the {@link PStyledText} objects that represent an object in the model.
	 */
	private List<PStyledText> containedItems;
	
	public ContainerPane(PCanvas canvas) {
		this(canvas, new ContainerModel<C>());
	}
	
	public ContainerPane(PCanvas canvas, ContainerModel<C> newModel) {
		model = newModel;
		this.canvas = canvas;
		containedItems = new ArrayList<PStyledText>();
		System.out.println("Model name is " + model.getName());
		final PStyledText modelNameText = createTextLine(model.getName());
		addChild(modelNameText);
		
		int yLoc = 1;
		for (int i = 0; i < model.getContainerCount(); i++) {
			for (int j = 0; j < model.getContainerSize(i); j++) {
				final PStyledText newText = createTextLine(model.getContents(i, j).toString());
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
		
		addInputEventListener(new DragTargetEventHandler(this));
	}

	/**
	 * Creates a {@link PStyledText} object that is editable by clicking on it.
	 */
	private PStyledText createTextLine(String text) {
		final PStyledText modelNameText = new PStyledText();
		JEditorPane nameEditor = new JEditorPane();
		nameEditor.setBorder(new LineBorder(nameEditor.getForeground()));
		nameEditor.setText(text);
		modelNameText.setDocument(nameEditor.getDocument());
		final PStyledTextEventHandler styledTextEventHandler = new ExtendedStyledTextEventHandler(canvas, nameEditor);
		addInputEventListener(styledTextEventHandler);
		nameEditor.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				styledTextEventHandler.stopEditing();
			}
			public void focusGained(FocusEvent e) {
			}
		});
		modelNameText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (outerRect != null) {
					outerRect.setWidth(Math.max(modelNameText.getWidth() + 2 * BORDER_SIZE, outerRect.getWidth()));
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
			while (picked != this && !containedItems.contains(picked)) {
				pickPath.popTransform(picked.getTransformReference(false));
				pickPath.popNode(picked);
				picked = pickPath.getPickedNode();
			}
			
			return true;
		}
		return false;
	}

}
