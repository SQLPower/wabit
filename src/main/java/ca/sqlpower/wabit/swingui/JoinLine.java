package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PStyledText;

public class JoinLine extends PNode {

	private static final float BORDER_WIDTH = 5;
	private final PNode leftNode;
	private final PNode rightNode;
	private final PNode leftContainerPane;
	private final PNode rightContainerPane;
	
	private final PStyledText joinText;
	private final PPath textCircle;
	
	private final PPath leftPath;
	private final PPath rightPath;
	
	public JoinLine(PNode leftNode, PNode rightNode) {
		super();
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		leftContainerPane = leftNode.getParent();
		rightContainerPane = rightNode.getParent();
		leftContainerPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateLine();
			}
		});
		rightContainerPane.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateLine();
			}
		});
		
		leftPath = new PPath();
		addChild(leftPath);
		rightPath = new PPath();
		addChild(rightPath);
		
		textCircle = PPath.createEllipse(0, 0, 0, 0);
		addChild(textCircle);
		
		joinText = new PStyledText();
		JEditorPane editorPane = new JEditorPane();
		editorPane.setText("=");
		joinText.setDocument(editorPane.getDocument());
		addChild(joinText);
		
		updateLine();		
	}

	private void updateLine() {
		PBounds leftBounds = this.leftNode.getGlobalBounds();
		PBounds rightBounds = this.rightNode.getGlobalBounds();
		PBounds leftContainerBounds = leftContainerPane.getGlobalBounds();
		PBounds rightContainerBounds = rightContainerPane.getGlobalBounds();
		if (leftBounds.getCenterX() > rightBounds.getCenterX()) {
			PBounds tempBounds = leftBounds;
			leftBounds = rightBounds;
			rightBounds = tempBounds;
			
			tempBounds = leftContainerBounds;
			leftContainerBounds = rightContainerBounds;
			rightContainerBounds = tempBounds;
		}
		
		leftPath.reset();
		rightPath.reset();
		double leftX = leftBounds.getX() + leftBounds.getWidth();
		double midX = leftX + (rightBounds.getX() - leftX)/2;
		double midY = Math.abs(leftBounds.getY() - rightBounds.getY()) / 2 + Math.min(leftBounds.getY(), rightBounds.getY());
		
		// For two Bezier curves to be connected the last point in the first
		// curve must equal the first point in the second curve.
		// For two Bezier curves to be continuous on the first derivative the
		// connecting point must be on the line made by the second control point
		// of the first curve and the first control point of the second curve.
		leftPath.moveTo((float)(leftContainerBounds.getX() + leftContainerBounds.getWidth()), (float)(leftBounds.getY() + leftBounds.getHeight()/2));
		leftPath.curveTo((float)(midX - (rightBounds.getX() - leftX)/6), (float)leftBounds.getY(), (float)midX, (float)(leftBounds.getY() - Math.abs(leftBounds.getY() - rightBounds.getY())/6), (float)midX, (float)midY);
		rightPath.moveTo((float)midX, (float)midY);
		rightPath.curveTo((float)midX, (float)(rightBounds.getY() + Math.abs(leftBounds.getY() - rightBounds.getY())/6), (float)(midX + (rightBounds.getX() - leftX)/6), (float)rightBounds.getY(), (float)(rightContainerBounds.getX()), (float)(rightBounds.getY() + rightBounds.getHeight()/2));
		
		double textMidX = midX - joinText.getWidth()/2;
		double textMidY = midY - joinText.getHeight()/2;
		joinText.setX(textMidX);
		joinText.setY(textMidY);
		
		textCircle.setPathToEllipse((float)(textMidX - BORDER_WIDTH),
				(float)(textMidY - BORDER_WIDTH),
				(float)joinText.getWidth() + 2 * BORDER_WIDTH,
				(float)joinText.getHeight() + 2 * BORDER_WIDTH);
	}
}
