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

package ca.sqlpower.wabit.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This object draws a join line between two columns in the GUI query pen.
 */
public class JoinLine extends PNode {

	/**
	 * The border width of the ellipse that surrounds the join expression.
	 */
	private static final float BORDER_WIDTH = 5;
	
	/**
	 * One of the columns that is being joined on.
	 */
	private final PNode leftNode;
	
	/**
	 * The other column that is being joined on.
	 */
	private final PNode rightNode;
	
	/**
	 * The parent to the leftNode. This will be used to know where
	 * to draw the join line and when to update it on a move.
	 */
	private final PNode leftContainerPane;
	
	/**
	 * The parent to the rightNode. This will be used to know where
	 * to draw the join line and when to update it on a move.
	 */
	private final PNode rightContainerPane;
	
	/**
	 * The text of the type of join the two columns are being joined by.
	 */
	private final PStyledText joinText;
	
	/**
	 * A circle to surround the join text.
	 */
	private final PPath textCircle;
	
	/**
	 * A Bezier curve that connects the left column to the text circle.
	 */
	private final PPath leftPath;
	
	/**
	 * A Bezier curve that connects the right column to the text circle.
	 */
	private final PPath rightPath;
	
	/**
	 * Creates the line representing a join between two columns.
	 * The parent of these nodes will be listened to for movement
	 * to update the position of the line.
	 */
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

	/**
	 * Updates the line end points and control points. The text area is also moved.
	 */
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
