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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.swingui.event.ExtendedStyledTextEventHandler;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.nodes.PStyledText;


/**
 * This class is similar to the EditablePStyledText class except this class also handles a Option Box which 
 * will line up right under the JEditorPane. It will handle mouse events on the options and append to the
 * JEditorPane.
 */
public class EditablePStyledTextWithOptionBox  extends PStyledText {
	
private static final Logger logger = Logger.getLogger(EditablePStyledText.class);

	/**
	 * One pixel space for adjusting the whereOptionBox with the whereTextBox.
	 */
	private static final int ONE_PIXEL_SPACE=1;
	
	/**
	 * The editor pane shown when the text is clicked. The text entered into this
	 * pane will modify the text shown in this PStyledText.
	 */
	private final JEditorPane editorPane;
	
	
	/**
	 * The options box that will display a list of where clause options
	 */
	private PPath whereOptionBox; 
	
	/**
	 * An attribute set that contains the font family for lists. This will set the
	 * font of this PStyledText to be a more normal looking font within the app.
	 */
	private final SimpleAttributeSet attributeSet;
	
	/**
	 * This handles the mouse click on the text and shows the editor if the mouse
	 * has actually clicked on the text.
	 */
	private final ExtendedStyledTextEventHandler styledTextEventHandler;
	
	/**
	 * This listener will set the text of this PStyledText and hide the editor
	 * pane when the editor pane loses focus (ie: clicked away from the editor).
	 */
	private FocusListener editorFocusListener = new FocusListener() {
		public void focusLost(FocusEvent e) {
			if(boxClicked) {
				boxClicked = false;
				editorPane.requestFocus();
				return;
			} else {
				whereOptionBox.removeFromParent();
				styledTextEventHandler.stopEditing();
			}
		}
		public void focusGained(FocusEvent e) {
			whereOptionBox.translate(getGlobalFullBounds().getX()-whereOptionBox.getXOffset()-ONE_PIXEL_SPACE
					,(getGlobalFullBounds().getY() + getHeight())-whereOptionBox.getYOffset());
			queryPen.getTopLayer().addChild(whereOptionBox);
			whereOptionBox.moveToFront();
		}
	};

	/**
	 * The document shared between the editor pane and this PStyledText object.
	 * This will contain the shared text between the two objects and has
	 * the attribute set attached to it.
	 */
	private DefaultStyledDocument doc;

	/**
	 * A list of listeners that fire when this styled text's text is starting or
	 * stopping from being in an editable state.
	 */
	private List<EditStyledTextListener> editingListeners;
	
	
	/**
	 * This is the width of the WHERE option's box
	 */
	private static final int WHERE_OPTION_BOX_WIDTH = 95;
	
	/**
	 * This is the height of the Where option's box
	 */
	private static final int WHERE_OPTION_BOX_HIEGHT = 85;
	
	
	private boolean boxClicked;
	
	private QueryPen queryPen;
	
	/**
	 *  This is an Array of Where Options for the whereOptionsBox
	 */
	private static final String[] whereOptions = new String[]{"<", ">", 
		"=", "<>", ">=", "<=", "BETWEEN", "LIKE", "IN", "NOT" };
	
	
	public EditablePStyledTextWithOptionBox(QueryPen mouseState, PCanvas canvas) {
		this("", mouseState, canvas);
	}
	
	public EditablePStyledTextWithOptionBox(String startingText, QueryPen mouseStates, PCanvas canvas) {
		editorPane = new JEditorPane();
		queryPen = mouseStates;
		editingListeners = new ArrayList<EditStyledTextListener>();
		boxClicked = false;
		doc = new DefaultStyledDocument();
		attributeSet = new SimpleAttributeSet();
		attributeSet.addAttribute(StyleConstants.FontFamily, UIManager.getFont("List.font").getFamily());
		editorPane.setDocument(doc);
		editorPane.setBorder(new LineBorder(editorPane.getForeground()));
		editorPane.setText(startingText);
		doc.setParagraphAttributes(0, editorPane.getText().length(), attributeSet, false);
		setDocument(editorPane.getDocument());
		
		styledTextEventHandler = new ExtendedStyledTextEventHandler(mouseStates, canvas, editorPane) {
			@Override
			public void startEditing(PInputEvent event, PStyledText text) {
				for (EditStyledTextListener l : editingListeners) {
					l.editingStarting();
				}
				super.startEditing(event, text);
				
			}
			
			@Override
			public void stopEditing() {
				editorPane.setText(editorPane.getText().replaceAll("\n", "").trim());
				syncWithDocument();
				for (EditStyledTextListener l : editingListeners) {
					l.editingStopping();
				}
				super.stopEditing();
				logger.debug("Editing stopped.");
			}
		};
		addInputEventListener(styledTextEventHandler);
		
		editorPane.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				//Do nothing
			}
			public void keyReleased(KeyEvent e) {
				//Do nothing
			}
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					styledTextEventHandler.stopEditing();
				}
			}
		});
		
		editorPane.addFocusListener(editorFocusListener);
		
		whereOptionBox = PPath.createRectangle(0, 0
				, (float)WHERE_OPTION_BOX_WIDTH, (float)WHERE_OPTION_BOX_HIEGHT);
		
		whereOptionBox.addInputEventListener(new PBasicInputEventHandler() {
		
			@Override
			public void mousePressed(PInputEvent event) {
				boxClicked = true;
			}
		});
		
		// Add the whereOptions to the whereOptionsBox with Mouse Listeners.
		int yLoc = 0;
		int xLoc = 0;
		for(String whereOption : whereOptions) {
			final PText newOption = new PText(whereOption);
			newOption.addAttribute(StyleConstants.FontFamily, UIManager.getFont("List.font").getFamily());
			newOption.translate((WHERE_OPTION_BOX_WIDTH/3)*xLoc+ ONE_PIXEL_SPACE*2, (getHeight()+ 2) * yLoc+ ONE_PIXEL_SPACE*5);
			newOption.addInputEventListener(new PBasicInputEventHandler() {

				@Override
				public void mousePressed(PInputEvent event) {
					JEditorPane whereEditorPane = getEditorPane();
					try {
						whereEditorPane.getDocument().insertString(whereEditorPane.getCaretPosition()
								, newOption.getText(), null);
					} catch (BadLocationException e) {
						logger.debug("Bad Location when trying to insert whereOption on whereText");
						throw new IllegalStateException(e);
					}
				}
			});
			yLoc++;
			if(yLoc > 4) {
				yLoc = 0;
				xLoc = 1;
			}
			whereOptionBox.addChild(newOption);
		}
		
	}
	
	public void addEditStyledTextListener(EditStyledTextListener l) {
		editingListeners.add(l);
	}
	
	public void removeEditStyledTextListener(EditStyledTextListener l) {
		editingListeners.add(l);
	}
	
	public JEditorPane getEditorPane() {
		return editorPane;
	}
	
	public PPath getOptionBox() {
		return whereOptionBox;
	}
	
}
