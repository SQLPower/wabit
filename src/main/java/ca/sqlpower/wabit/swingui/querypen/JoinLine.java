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

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.nodes.PComposite;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This object draws a join line between two columns in the GUI query pen.
 */
public class JoinLine extends PComposite {
	
	private static Logger logger = Logger.getLogger(JoinLine.class);

	/**
	 * The border width of the ellipse that surrounds the join expression.
	 */
	private static final float BORDER_WIDTH = 5;
	
	/**
	 * This is the minimum amount the join line will stick out of the container
	 * PNode. This will help the user see where the join lines come out of and
	 * where they go to when the join line goes behind the connected container
	 * PNode.
	 */
	private static final float JOIN_LINE_STICKOUT_LENGTH = 50;
	
	/**
	 * One of the columns that is being joined on.
	 */
	private final UnmodifiableItemPNode leftNode;
	
	/**
	 * The other column that is being joined on.
	 */
	private final UnmodifiableItemPNode rightNode;
	
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
	private final PStyledText equalityText;
	
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
	public JoinLine(MouseState mouseState, PCanvas canvas, UnmodifiableItemPNode leftNode, UnmodifiableItemPNode rightNode) {
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
		
		equalityText = new PStyledText();
		JEditorPane editorPane = new JEditorPane();
		editorPane.setText("=");
		equalityText.setDocument(editorPane.getDocument());
		addChild(equalityText);
		
		updateLine();
	}

	/**
	 * Updates the line end points and control points. The text area is also moved.
	 */
	private void updateLine() {
		setBounds(0, 0, 0, 0);
		
		PBounds leftBounds = this.leftNode.getGlobalFullBounds();
		PBounds rightBounds = this.rightNode.getGlobalFullBounds();
		PBounds leftContainerBounds = leftContainerPane.getGlobalBounds();
		PBounds rightContainerBounds = rightContainerPane.getGlobalBounds();		
		
		leftPath.reset();
		rightPath.reset();
		double leftY = leftBounds.getY() + leftBounds.getHeight()/2;
		double rightY = rightBounds.getY() + rightBounds.getHeight()/2;
		double midY = Math.abs(leftY - rightY) / 2 + Math.min(leftY, rightY);
		
		double leftX = leftContainerBounds.getX();
		double rightX = rightContainerBounds.getX();
		double midX;
		int rightContainerFirstControlPointDirection = -1;
		int leftContainerFirstControlPointDirection = 1;
		if (leftX + leftContainerBounds.getWidth() < rightX) {
			leftX += leftContainerBounds.getWidth();
			midX = leftX + (rightX - leftX)/2;
			rightContainerFirstControlPointDirection = 1;
			logger.debug("Left container is to the left of the right container.");
		} else if (leftX < rightContainerBounds.getWidth() + rightContainerBounds.getX()) {
			leftX += leftContainerBounds.getWidth();
			rightX += rightContainerBounds.getWidth();
			midX = Math.max(JOIN_LINE_STICKOUT_LENGTH + leftX, JOIN_LINE_STICKOUT_LENGTH + rightX);
			logger.debug("The containers are above or below eachother.");
		} else {
			rightX += rightContainerBounds.getWidth();
			midX = leftX + (rightX - leftX)/2;
			leftContainerFirstControlPointDirection = -1;
			logger.debug("The right container is to the left of the left container.");
		}
		
		logger.debug("Left x position is " + leftX + " and mid x position is " + midX);
		
		// For two Bezier curves to be connected the last point in the first
		// curve must equal the first point in the second curve.
		// For two Bezier curves to be continuous on the first derivative the
		// connecting point must be on the line made by the second control point
		// of the first curve and the first control point of the second curve.
		leftPath.moveTo((float)(leftX), (float)(leftY));
		leftPath.curveTo((float)(leftX + leftContainerFirstControlPointDirection * Math.max(JOIN_LINE_STICKOUT_LENGTH, Math.abs(rightX - leftX)/6)), (float)leftY, (float)midX, (float)(leftY + (rightY - leftY)/6), (float)midX, (float)midY);
		
		rightPath.moveTo((float)midX, (float)midY);
		rightPath.curveTo((float)midX, (float)(leftY + (rightY - leftY)*5/6), (float)(rightX - rightContainerFirstControlPointDirection * Math.max(JOIN_LINE_STICKOUT_LENGTH, Math.abs(rightX - leftX)/6)), (float)rightY, (float)(rightX), (float)(rightY));
		
		double textMidX = midX - equalityText.getWidth()/2;
		double textMidY = midY - equalityText.getHeight()/2;
		equalityText.setX(textMidX);
		equalityText.setY(textMidY);
		
		textCircle.setPathToEllipse((float)(textMidX - BORDER_WIDTH),
				(float)(textMidY - BORDER_WIDTH),
				(float)equalityText.getWidth() + 2 * BORDER_WIDTH,
				(float)equalityText.getHeight() + 2 * BORDER_WIDTH);
		
		//Compute the bounds only by the paths and the circle.
		//This prevents the bound handles from being included.
		Rectangle2D boundUnion = textCircle.getBounds();
		boundUnion = boundUnion.createUnion(leftPath.getBounds());
		boundUnion = boundUnion.createUnion(rightPath.getBounds());
		setBounds(boundUnion);
		
	}
	
	public UnmodifiableItemPNode getLeftNode() {
		return leftNode;
	}
	
	public UnmodifiableItemPNode getRightNode() {
		return rightNode;
	}
	
}
