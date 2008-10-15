package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PStyledText;

public class JoinLine extends PPath {

	private final PStyledText leftNode;
	private final PStyledText rightNode;
	
	public JoinLine(PStyledText leftNode, PStyledText rightNode) {
		super();
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		updateLine();
		
		leftNode.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("C'mon you can show this.");
				updateLine();
			}
		});
		
	}

	private void updateLine() {
		PBounds leftBounds = this.leftNode.getGlobalBounds();
		PBounds rightBounds = this.rightNode.getGlobalBounds();
		if (leftBounds.getCenterX() > rightBounds.getCenterX()) {
			PBounds tempBounds = leftBounds;
			leftBounds = rightBounds;
			rightBounds = tempBounds;
		}
		double leftX = leftBounds.getX() + leftBounds.getWidth();
		moveTo((float)leftX, (float)(leftBounds.getY() + leftBounds.getHeight()/2));
		double midX = leftX + (rightBounds.getX() - leftX)/2;
		curveTo((float)midX, (float)leftBounds.getY(), (float)midX, (float)rightBounds.getY(), (float)rightBounds.getX(), (float)(rightBounds.getY() + rightBounds.getHeight()/2));
	}
}
