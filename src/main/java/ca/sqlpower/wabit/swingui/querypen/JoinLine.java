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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.query.SQLJoin;
import ca.sqlpower.wabit.query.SQLJoin.Comparators;
import ca.sqlpower.wabit.swingui.WabitNode;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.nodes.PStyledText;

/**
 * This object draws a join line between two columns in the GUI query pen.
 */
public class JoinLine extends PNode implements WabitNode {
	
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
	 * A buffer that defines how far from the mouse click we will consider the
	 * user actually meant to click on a specific part of the join line.
	 */
	private static final int MOUSE_CLICK_BUFFER = 4;

	/**
	 * The length of each dash that makes up a join line part when it is in
	 * outer join mode.
	 */
	private static final float DASH_WIDTH = 5;
	
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
	private final PStyledText symbolText;
	
	/**
	 * A circle to surround the join text.
	 */
	private final PPath textCircle;
	
	/**
	 * A box containing the optionSigns
	 */
	private final PNode optionBox;
	
	private JEditorPane editorPane;
	
	/**
	 * check if two joined tables are swapped
	 */
	private boolean isJoinedTablesSwapped;
	
	/**
	 * previous isJoinedTablesSwapped
	 */
	private boolean oldIsJoinedTableSwapped;
	
	/**
	 * the comparator for viewing in the circle of the Joined line
	 */
	private String viewCom;

	/**
	 * A Bezier curve that connects the left column to the text circle.
	 */
	private final PPath leftPath;
	
	/**
	 * A Bezier curve that connects the right column to the text circle.
	 */
	private final PPath rightPath;
	
	private final PNode joinCombo;
	
	private boolean clickedOnLeftPath;
	
	/**
	 * This will listen for right clicks and if the click is near the join
	 * line it will pop-up a list of join types (inner and outer) to let 
	 * the user change the join type.
	 */
	private final PInputEventListener joinChangeListener = new PBasicInputEventHandler() {
		
		@Override
		public void mouseReleased(PInputEvent event) {
			maybeShowPopup(event);
		}
		
		@Override
		public void mousePressed(PInputEvent event) {
			maybeShowPopup(event);
		}
		
		@Override
		public void mouseClicked(PInputEvent event) {
			maybeShowPopup(event);
		}
		
		private void maybeShowPopup(PInputEvent event) {
			if (event.isPopupTrigger()) {
				optionBox.translate(event.getPosition().getX() - optionBox.getFullBounds().getX() - BORDER_WIDTH, event.getPosition().getY() - optionBox.getFullBounds().getY() - BORDER_WIDTH);
				if (checkClickOnPath(event.getPosition().getX(), event.getPosition().getY(), textCircle)) {
					canvas.getLayer().addChild(optionBox);
					logger.debug("Clicked on textCircle");
					return;
				} 
				
				if (canvas.getLayer().getAllNodes().contains(optionBox)) {
					canvas.getLayer().removeChild(optionBox);
				}
				
				joinCombo.translate(event.getPosition().getX() - joinCombo.getFullBounds().getX() - BORDER_WIDTH, event.getPosition().getY() - joinCombo.getFullBounds().getY() - BORDER_WIDTH);
				if (canvas.getLayer().getAllNodes().contains(joinCombo)) {
					canvas.getLayer().removeChild(joinCombo);
				}
				

				if (checkClickOnPath(event.getPosition().getX(), event.getPosition().getY(), leftPath)) {
					clickedOnLeftPath = true;
					canvas.getLayer().addChild(joinCombo);
					logger.debug("Clicked on left path");
					return;
				} 
				
				if (checkClickOnPath(event.getPosition().getX(), event.getPosition().getY(), rightPath)) {
					clickedOnLeftPath = false;
					canvas.getLayer().addChild(joinCombo);
					logger.debug("Clicked on right path");
				}
				
			}
		};
		
	};

	/**
	 * The canvas to display combo boxes and this join on.
	 */
	private final PCanvas canvas;
	
	/**
	 * This is the model behind the JoinLine
	 */
	private SQLJoin model;
	
	/**
	 * This join listener will update the view when the model changes.
	 */
	private final PropertyChangeListener joinListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			updateLine();
		}
	};

	private final QueryPen queryPen;

	/**
	 * This join icon will be displayed when the join is first created and when it is in
	 * an equality. If the two columns in the join is not being equated on a circle with
	 * the comparator will be show instead.
	 * <p>
	 * This icon is for when the join is selected.
	 */
	private final ImageIcon joinSelectedIcon;
	
	/**
	 * This join icon will be displayed when the join is first created and when it is in
	 * an equality. If the two columns in the join is not being equated on a circle with
	 * the comparator will be show instead.
	 * <p>
	 * This icon is for when the join is selected.
	 */
	private final ImageIcon joinUnselectedIcon;

	/**
	 * This is the PNode wrapper to the JoinIcon. This contains the selected image.
	 */
	private PImage selectedImageNode;

	/**
	 * This is the PNode wrapper to the JoinIcon. This contains the unselected image.
	 */
	private PImage unselectedImageNode;

	/**
	 * This is a background to the images. This will prevent the line from going
	 * behind the join images and making the image more clear.
	 */
	private PNode selectedImageBackground;
	
	/**
	 * This will create a join line with properties taken from the model. The ItemPNodes passed in must
	 * contain the left and right items respectively so the JoinLine can connect itself correctly. Failing
	 * to pass in the correct ItemPNodes will result in an IllegalStateException.
	 */
	public JoinLine(QueryPen queryPen, PCanvas c, UnmodifiableItemPNode leftNode, UnmodifiableItemPNode rightNode, SQLJoin model) throws IllegalStateException {
		this(queryPen, c, leftNode, rightNode);
		if (model.getLeftColumn() != leftNode.getItem()) {
			throw new IllegalStateException("The left column items were not equal.");
		}
		if (model.getRightColumn() != rightNode.getItem()) {
			throw new IllegalStateException("The right column items were not equal.");
		}
		this.model = model;
		editorPane.setText(model.getComparator());
		symbolText.syncWithDocument();
		updateLine();
	}
	
	/**
	 * Creates the line representing a join between two columns.
	 * The parent of these nodes will be listened to for movement
	 * to update the position of the line.
	 */
	public JoinLine(QueryPen queryPen, PCanvas c, UnmodifiableItemPNode leftNode, UnmodifiableItemPNode rightNode) {
		super();
		this.queryPen = queryPen;
		this.canvas = c;
		model = new SQLJoin(leftNode.getItem(), rightNode.getItem());

		model.addJoinChangeListener(joinListener);
		this.leftNode = leftNode;
		this.rightNode = rightNode;
		leftNode.JoinTo(this);
		rightNode.JoinTo(this);
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
		leftPath.setStroke(new BasicStroke(2));
		rightPath = new PPath();
		addChild(rightPath);
		rightPath.setStroke(new BasicStroke(2));
		
		joinSelectedIcon = new ImageIcon(JoinLine.class.getClassLoader().getResource("icons/j_on.png"));
		joinUnselectedIcon = new ImageIcon(JoinLine.class.getClassLoader().getResource("icons/j_off.png"));
		selectedImageNode = new PImage(joinSelectedIcon.getImage());
		unselectedImageNode = new PImage(joinUnselectedIcon.getImage());
		selectedImageBackground = new PNode();
		selectedImageBackground.setBounds(selectedImageNode.getBounds());
		addChild(selectedImageBackground);
		selectedImageBackground.setPaint(Color.BLACK);
		addChild(selectedImageNode);
		addChild(unselectedImageNode);
		selectedImageNode.setVisible(false);
		textCircle = PPath.createEllipse(0, 0, 0, 0);
		addChild(textCircle);
		textCircle.setStroke(new BasicStroke(2));
		
		oldIsJoinedTableSwapped = false;
		viewCom = "=";
		isJoinedTablesSwapped = false;
		optionBox = new PNode();
		symbolText = new PStyledText();
		editorPane = new JEditorPane();
		editorPane.setText("=");
		symbolText.setDocument(editorPane.getDocument());
		int textHeight = 0;
		for (Comparators aComparator: Comparators.values()) {
			final PText tempText = new PText(aComparator.getComparator());
			tempText.translate(0, textHeight);
			tempText.addInputEventListener(new PBasicInputEventHandler() {
				public void mousePressed(PInputEvent event) {
					viewCom = tempText.getText();
					editorPane.setText(viewCom);
					symbolText.syncWithDocument();
					if (isJoinedTablesSwapped ) {
						model.setComparator(getOppositeSymbol(viewCom));
					} else {
						model.setComparator(viewCom );
					}
					canvas.getLayer().removeChild(optionBox);
				}
			});
			optionBox.addChild(tempText);
			textHeight += tempText.getHeight();
		}
		PPath optionBoxouterRect = PPath.createRectangle((float)- BORDER_WIDTH, (float)- BORDER_WIDTH, (float)(optionBox.getFullBounds().getWidth() + 2 * BORDER_WIDTH), (float)(optionBox.getFullBounds().getHeight() + 2 * BORDER_WIDTH));
		optionBox.addChild(optionBoxouterRect);
		optionBox.setBounds(optionBoxouterRect.getBounds());
		optionBoxouterRect.moveToBack();
		
		addChild(symbolText);
		updateLine();
		
		this.addInputEventListener(joinChangeListener);
		
		joinCombo = new PNode();
		joinCombo.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseExited(PInputEvent event) {
				if (!joinCombo.getBounds().contains(joinCombo.globalToLocal(event.getPosition())) && canvas.getLayer().getAllNodes().contains(joinCombo)) {
					canvas.getLayer().removeChild(joinCombo);
				}
			}
		});
		
		optionBox.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseExited(PInputEvent event) {
				if (!optionBox.getBounds().contains(optionBox.globalToLocal(event.getPosition())) && canvas.getLayer().getAllNodes().contains(optionBox)) {
					canvas.getLayer().removeChild(optionBox);
				}
			}
		});
		
		PText innerJoinComboItem = new PText("Inner Join");
		innerJoinComboItem.addAttribute(StyleConstants.FontFamily, UIManager.getFont("List.font").getFamily());
		joinCombo.addChild(innerJoinComboItem);
		innerJoinComboItem.addInputEventListener(new PBasicInputEventHandler() {

			@Override
			public void mouseReleased(PInputEvent event) {
				canvas.getLayer().removeChild(joinCombo);
				if (clickedOnLeftPath) {
					model.setLeftColumnOuterJoin(false);
				} else {
					model.setRightColumnOuterJoin(false);
				}
				updateLine();
			}
		});
		PText outerJoinComboItem = new PText("Outer Join");
		outerJoinComboItem.addAttribute(StyleConstants.FontFamily, UIManager.getFont("List.font").getFamily());
		outerJoinComboItem.translate(0, innerJoinComboItem.getHeight() + BORDER_WIDTH);
		joinCombo.addChild(outerJoinComboItem);
		outerJoinComboItem.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseReleased(PInputEvent event) {
				if (clickedOnLeftPath) {
					model.setLeftColumnOuterJoin(true);
				} else {
					model.setRightColumnOuterJoin(true);
				}
				canvas.getLayer().removeChild(joinCombo);
				updateLine();
			}
		});
		
		PPath outerRect = PPath.createRectangle((float)- BORDER_WIDTH, (float)- BORDER_WIDTH, (float)(joinCombo.getFullBounds().getWidth() + 2 * BORDER_WIDTH), (float)(joinCombo.getFullBounds().getHeight() + 2 * BORDER_WIDTH));
		joinCombo.addChild(outerRect);
		joinCombo.setBounds(outerRect.getBounds());
		outerRect.moveToBack();
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
			isJoinedTablesSwapped = false;
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
			isJoinedTablesSwapped = true;
			logger.debug("The right container is to the left of the left container.");
		}
		
		handleJoinedTablesSwapped ();
		logger.debug("Left x position is " + leftX + " and mid x position is " + midX);
		
		// For two Bezier curves to be connected the last point in the first
		// curve must equal the first point in the second curve.
		// For two Bezier curves to be continuous on the first derivative the
		// connecting point must be on the line made by the second control point
		// of the first curve and the first control point of the second curve.
		leftPath.moveTo((float)(leftX), (float)(leftY));
		Point2D leftControlPoint1 = new Point2D.Float((float)(leftX + leftContainerFirstControlPointDirection * Math.max(JOIN_LINE_STICKOUT_LENGTH, Math.abs(rightX - leftX)/6)), (float)leftY);
		Point2D leftControlPoint2 = new Point2D.Float((float)midX, (float)(leftY + (rightY - leftY)/6));
		leftPath.curveTo((float)leftControlPoint1.getX(), (float)leftControlPoint1.getY(), (float)leftControlPoint2.getX(), (float)leftControlPoint2.getY(), (float)midX, (float)midY);	
		
		rightPath.moveTo((float)midX, (float)midY);
		Point2D rightControlPoint1 = new Point2D.Float((float)midX, (float)(leftY + (rightY - leftY)*5/6));
		Point2D rightControlPoint2 = new Point2D.Float( (float)(rightX - rightContainerFirstControlPointDirection * Math.max(JOIN_LINE_STICKOUT_LENGTH, Math.abs(rightX - leftX)/6)), (float)rightY);
		rightPath.curveTo((float)rightControlPoint1.getX(), (float)rightControlPoint1.getY(), (float)rightControlPoint2.getX(), (float)rightControlPoint2.getY(), (float)(rightX), (float)(rightY));
		

		float[] dash = { DASH_WIDTH, DASH_WIDTH };
		if (model.isLeftColumnOuterJoin()) {
			leftPath.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
		} else {
			leftPath.setStroke(new BasicStroke());
		}
		if (model.isRightColumnOuterJoin()) {
			rightPath.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
		} else {
			rightPath.setStroke(new BasicStroke());
		}
		
		double textMidX = midX - symbolText.getWidth()/2;
		double textMidY = midY - symbolText.getHeight()/2;
		symbolText.setX(textMidX);
		symbolText.setY(textMidY);
		
		textCircle.setPathToEllipse((float)(textMidX - BORDER_WIDTH),
				(float)(textMidY - BORDER_WIDTH),
				(float)symbolText.getWidth() + 2 * BORDER_WIDTH,
				(float)symbolText.getHeight() + 2 * BORDER_WIDTH);
		
		logger.debug("The model's comparator is \"" + model.getComparator() + "\" looking for " + SQLJoin.Comparators.EQUAL_TO.getComparator());
		selectedImageNode.setX(midX - selectedImageNode.getWidth()/2);
		selectedImageNode.setY(midY - selectedImageNode.getHeight()/2);
		unselectedImageNode.setX(midX - unselectedImageNode.getWidth()/2);
		unselectedImageNode.setY(midY - unselectedImageNode.getHeight()/2);
		selectedImageBackground.setX(midX - unselectedImageNode.getWidth()/2);
		selectedImageBackground.setY(midY - unselectedImageNode.getHeight()/2);
		if (model.getComparator().equals(SQLJoin.Comparators.EQUAL_TO.getComparator())) {
			selectedImageNode.setVisible(false);
			unselectedImageNode.setVisible(true);
			selectedImageBackground.setVisible(true);
			textCircle.setVisible(false);
		} else {
			selectedImageNode.setVisible(false);
			unselectedImageNode.setVisible(false);
			selectedImageBackground.setVisible(false);
			textCircle.setVisible(true);
		}
		
		Rectangle2D boundUnion = textCircle.getBounds();
		boundUnion = boundUnion.createUnion(leftPath.getBounds());
		boundUnion = boundUnion.createUnion(rightPath.getBounds());
		setBounds(boundUnion);
	}
	
	/**
	 *  flip the symbols when joined tables are swapped
	 */
	public void handleJoinedTablesSwapped () {
		if (isJoinedTablesSwapped != oldIsJoinedTableSwapped) {
			viewCom = getOppositeSymbol(viewCom);
			editorPane.setText(viewCom);
			symbolText.syncWithDocument();
			oldIsJoinedTableSwapped = isJoinedTablesSwapped;
		} 
	}
	
	public String getOppositeSymbol (String symbol) {
		if ( symbol.equals(">") ) {
			return "<";
		} else if ( symbol.equals(">=") ) {
			return "<=";
		} else if ( symbol.equals("<") ) {
			return ">";
		} else if ( symbol.equals("<=") ) {
			return ">=";
		} else {
			return symbol;
		}
	}
	
	/**
	 * Returns true if the user clicked on or near the given path. Returns
	 * false otherwise. Clicking in the join circle will not be considered
	 * clicking near the line.
	 */
	private boolean checkClickOnPath(double mouseX, double mouseY, PPath path) {
		Rectangle2D mouseClickRectangle = new Rectangle((int)mouseX - MOUSE_CLICK_BUFFER, (int)mouseY - MOUSE_CLICK_BUFFER, 2 * MOUSE_CLICK_BUFFER, 2 * MOUSE_CLICK_BUFFER);
		PathIterator iter = path.getPathReference().getPathIterator(path.getTransform(), 1);
		float [] linePoints = new float[2];
		Point2D oldPoints;
		iter.currentSegment(linePoints);
		iter.next();
		if (textCircle.getPathReference().contains(mouseX, mouseY)) {
			return true;
		}
		while (!iter.isDone()) {
			oldPoints = new Point2D.Float(linePoints[0], linePoints[1]);
			iter.currentSegment(linePoints);
			if (mouseClickRectangle.intersectsLine(oldPoints.getX(), oldPoints.getY(), linePoints[0], linePoints[1])) {
				return true;
			}
			iter.next();
		}
		return false;
	}
	
	public UnmodifiableItemPNode getLeftNode() {
		return leftNode;
	}
	
	public UnmodifiableItemPNode getRightNode() {
		return rightNode;
	}

	public SQLJoin getModel() {
		return model;
	}
	
	public JEditorPane getEditorPane(){
		return editorPane;
	}
	
	public void disconnectJoin() {
		model.removeAllListeners();
		leftNode.removeJoinedLine(this);
		rightNode.removeJoinedLine(this);
	}
	
	@Override
	/*
	 * Overriding the fullPick here so that the join line only actually
	 * gets picked when you click on the circle or close to a line 
	 */
	public boolean fullPick(PPickPath pickPath) {
		boolean superPick = super.fullPick(pickPath);
		if (superPick 
				&& (pickPath.getPickedNode() != this 
						|| checkClickOnPath(pickPath.getPickBounds().getX(), pickPath.getPickBounds().getY(), leftPath) 
						|| checkClickOnPath(pickPath.getPickBounds().getX(), pickPath.getPickBounds().getY(), rightPath))) {
			PNode picked = pickPath.getPickedNode();
			while (picked != this) {
				pickPath.popTransform(picked.getTransformReference(false));
				pickPath.popNode(picked);
				picked = pickPath.getPickedNode();
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void paint(PPaintContext paintContext) {
		setFocusColour(false);
		boolean hasFocus = queryPen.getMultipleSelectEventHandler().getSelection().contains(this);
		if (hasFocus) {
			setFocusColour(hasFocus);
		}
		super.paint(paintContext);
	}
	
	private void setFocusColour(boolean hasFocus) {
		if (hasFocus) {
			leftPath.setStrokePaint(QueryPen.SELECTED_CONTAINER_COLOUR);
			rightPath.setStrokePaint(QueryPen.SELECTED_CONTAINER_COLOUR);
			textCircle.setStrokePaint(QueryPen.SELECTED_CONTAINER_COLOUR);
			unselectedImageNode.setVisible(false);
			selectedImageNode.setVisible(true);
		} else {
			leftPath.setStrokePaint(QueryPen.UNSELECTED_CONTAINER_COLOUR);
			rightPath.setStrokePaint(QueryPen.UNSELECTED_CONTAINER_COLOUR);
			textCircle.setStrokePaint(QueryPen.UNSELECTED_CONTAINER_COLOUR);
			unselectedImageNode.setVisible(true);
			selectedImageNode.setVisible(false);
		}
	}

	public void cleanup() {
		model.removeJoinChangeListener(joinListener);
	}
}
