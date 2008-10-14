package ca.sqlpower.wabit.swingui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;
import javax.swing.border.LineBorder;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.event.PStyledTextEventHandler;
import edu.umd.cs.piccolox.nodes.PComposite;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This container pane displays a list of values stored in its model. The elements displayed
 * in a container pane can be broken into groups and will be separated by a line for each 
 * group.
 * 
 * @param <C> The type of object this container is displaying.
 */
public class ContainerPane<C extends Object> extends PComposite {
	
	private static final int BORDER_SIZE = 5;
	
	private class DoubleClickEditInputEventHandler extends PBasicInputEventHandler {
		@Override
		public void mousePressed(PInputEvent e) {
			super.mousePressed(e);
			if (lastPickedNode != null && e.getClickCount() == 2) {
				System.out.println("Last picked node on mouse double click " + lastPickedNode + " type " +lastPickedNode.getClass());
				for (Object o : lastPickedNode.getListenerList().getListenerList()) {
					if (o instanceof PInputEventListener) {
						PInputEventListener listener = (PInputEventListener)o;
						listener.processEvent(e, MouseEvent.MOUSE_PRESSED);
					}
				}
			}
		}
	}
	
	private final ContainerModel<C> model;

	/**
	 * The outer rectangle of this component. All parts of this component should be within this 
	 * rectangle and it should be resized if the components inside are changed.
	 */
	private PPath outerRect;
	
	/**
	 * The canvas this component is being drawn on.
	 */
	private PCanvas canvas;

	/**
	 * The last node picked by the mouse when a child of this component was
	 * picked. This allows events to be sent to the child node if this class
	 * does not want to handle the event. This will be null if the last item
	 * picked is not this or a child of this.
	 */
	private PNode lastPickedNode = null;
	
	public ContainerPane(PCanvas canvas) {
		this(canvas, new ContainerModel<C>());
	}
	
	public ContainerPane(PCanvas canvas, ContainerModel<C> newModel) {
		model = newModel;
		this.canvas = canvas;
		System.out.println("Model name is " + model.getName());
		final PStyledText modelNameText = createTextLine(model.getName());
		addChild(modelNameText);
		
		int yLoc = 1;
		for (int i = 0; i < model.getContainerCount(); i++) {
			for (int j = 0; j < model.getContainerSize(i); j++) {
				final PStyledText newText = createTextLine(model.getContents(i, j).toString());
				newText.translate(0, modelNameText.getHeight() * yLoc);
				addChild(newText);
				yLoc++;
			}
		}
		
		PBounds fullBounds = getFullBounds();
		this.addChild(PPath.createLine((float)getX() - BORDER_SIZE, (float)(getY() + modelNameText.getHeight()), (float)(getX() + fullBounds.width + BORDER_SIZE), (float)(getY() + modelNameText.getHeight())));
		outerRect = PPath.createRectangle((float)fullBounds.x - BORDER_SIZE, (float)fullBounds.y - BORDER_SIZE, (float)fullBounds.width + BORDER_SIZE * 2, (float)fullBounds.height + BORDER_SIZE * 2);
		this.addChild(outerRect);
		outerRect.moveToBack();
	
		this.addInputEventListener(new DoubleClickEditInputEventHandler());
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
		final PStyledTextEventHandler styledTextEventHandler = new PStyledTextEventHandler(canvas, nameEditor);
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
	
	@Override
	public boolean pick(PPickPath path) {
		lastPickedNode = path.getPickedNode();
		while (lastPickedNode != null) {
			System.out.println(lastPickedNode);
			lastPickedNode = lastPickedNode.getParent();
		}
		lastPickedNode = path.getPickedNode();
		return super.pick(path);
	}
		
	public ContainerModel<C> getModel() {
		return model;
	}

}
