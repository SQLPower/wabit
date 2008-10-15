package ca.sqlpower.wabit.swingui.event;

import ca.sqlpower.wabit.swingui.JoinLine;
import ca.sqlpower.wabit.swingui.MouseStatePane;
import ca.sqlpower.wabit.swingui.MouseStatePane.MouseStates;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * Creates a join between two columns in two different tables.
 */
public class CreateJoinEventHandler extends PBasicInputEventHandler {
	
	private MouseStatePane mouseStatePane;
	private PStyledText leftText;
	private PStyledText rightText;
	private PLayer joinLayer;

	public CreateJoinEventHandler(MouseStatePane mouseStatePane, PLayer joinLayer) {
		this.mouseStatePane = mouseStatePane;
		this.joinLayer = joinLayer;
	}
	
	@Override
	public void mousePressed(PInputEvent event) {
		super.mousePressed(event);
		if (mouseStatePane.getMouseState().equals(MouseStates.CREATE_JOIN)) {
			if (event.getPickedNode() instanceof PStyledText) {
				if (leftText == null) {
					leftText = (PStyledText)event.getPickedNode();
				} else if (rightText == null) {
					rightText = (PStyledText)event.getPickedNode();
					joinLayer.addChild(new JoinLine(leftText, rightText));
					leftText = null;
					rightText = null;
					mouseStatePane.setMouseState(MouseStates.READY);
				} else {
					throw new IllegalStateException("Trying to create a join while both ends have already been specified.");
				}
			} else {
				leftText = null;
				rightText = null;
				mouseStatePane.setMouseState(MouseStates.READY);
			}
		}
	}
}
