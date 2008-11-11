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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.swingui.Item;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * This PNode displays a single constant. These PNodes will be contained by a 
 * {@link ConstantsPane}. A constant is allowed to be edited and can be removed
 * from the {@link ConstantsPane}.
 */
public class ConstantPNode extends PNode implements ItemPNode {
	
	private static final Logger logger = Logger.getLogger(ConstantPNode.class);
	
	private static final int SPACING_SIZE = 8;
	private static final String LONG_EMPTY_STRING = "        ";
	
	/**
	 * This listener simply trim the string of the styled text passed in
	 * and sets the text to a long empty string when the user finishes editing
	 * if they leave the text blank. The long empty string gives the user something
	 * to click on to edit the field again.
	 */
	private class EmptyTextListener implements EditStyledTextListener {
		
		private final EditablePStyledText text;
		private String oldText = "";
		private final String propertyChangeConstant;
		private boolean isEditing = false;
		
		public EmptyTextListener(EditablePStyledText text, String propertyChangeConstant) {
			this.text = text;
			this.propertyChangeConstant = propertyChangeConstant;
			
		}
		
		public void editingStopping() {
			if (isEditing) {
				if (text.getEditorPane().getText().length() <= 0) {
					text.getEditorPane().setText(LONG_EMPTY_STRING);
					text.syncWithDocument();
				}
				for (PropertyChangeListener listener : changeListeners) {
					listener.propertyChange(new PropertyChangeEvent(ConstantPNode.this, propertyChangeConstant, oldText, text.getEditorPane().getText().trim()));
				}
			}
			isEditing = false;
		}
		public void editingStarting() {
			isEditing = true;
			text.getEditorPane().setText(text.getEditorPane().getText().trim());
			oldText = text.getEditorPane().getText();
		}
	}

	/**
	 * The item this PNode is displaying.
	 */
	private final Item item;
	private JCheckBox selectionCheckbox;
	private EditablePStyledText constantText;

	private EditablePStyledText aliasText;

	private EditablePStyledText whereText;
	
	private final List<PropertyChangeListener> changeListeners;
	
	private EditStyledTextListener removeItemListener = new EditStyledTextListener() {
		private String oldText;
		public void editingStopping() {
			if (constantText.getEditorPane().getText().length() <= 0) {
				for (PropertyChangeListener l : changeListeners) {
					l.propertyChange(new PropertyChangeEvent(ConstantPNode.this, ItemPNode.PROPERTY_ITEM_REMOVED, item, null));
				}
				item.getParent().getParent().removeItem(item);
			} else if (item instanceof StringItem) {
				((StringItem)item).setName(constantText.getEditorPane().getText());
			}
			for (PropertyChangeListener listener : changeListeners) {
				listener.propertyChange(new PropertyChangeEvent(constantText, ItemPNode.PROPERTY_ITEM, oldText, constantText.getEditorPane().getText().trim()));
			}
		}
		public void editingStarting() {
			oldText = constantText.getEditorPane().getText();
		}
	};

	public ConstantPNode(Item source, QueryPen mouseStates, PCanvas canvas) {
		this.item = source;
		changeListeners = new ArrayList<PropertyChangeListener>();
		
		selectionCheckbox = new JCheckBox();
		PSwing swingCheckbox = new PSwing(selectionCheckbox);
		addChild(swingCheckbox);
		selectionCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (PropertyChangeListener listener : changeListeners) {
					listener.propertyChange(new PropertyChangeEvent(ConstantPNode.this, ItemPNode.PROPERTY_SELECTED, !selectionCheckbox.isSelected(), selectionCheckbox.isSelected()));
				}
			}
		});
		
		constantText = new EditablePStyledText(source.getName(), mouseStates, canvas);
		constantText.addEditStyledTextListener(removeItemListener);
		double yPos = (swingCheckbox.getFullBounds().getHeight() - constantText.getHeight())/2;
		constantText.translate(swingCheckbox.getFullBounds().getWidth() + SPACING_SIZE, yPos);
		addChild(constantText);
		
		aliasText = new EditablePStyledText(LONG_EMPTY_STRING, mouseStates, canvas);
		aliasText.addEditStyledTextListener(new EmptyTextListener(aliasText, ItemPNode.PROPERTY_ALIAS));
		aliasText.translate(swingCheckbox.getFullBounds().getWidth() + constantText.getWidth() + 2 * SPACING_SIZE, yPos);
		addChild(aliasText);
		
		whereText = new EditablePStyledText(LONG_EMPTY_STRING, mouseStates, canvas);
		whereText.addEditStyledTextListener(new EmptyTextListener(whereText, ItemPNode.PROPERTY_WHERE));
		whereText.translate(swingCheckbox.getFullBounds().getWidth() + constantText.getWidth() + aliasText.getWidth() + 3 * SPACING_SIZE, yPos);
		addChild(whereText);
	}
	
	public Item getItem() {
		return item;
	}
	
	public String getAlias() {
		return aliasText.getEditorPane().getText().trim();
	}
	
	public String getWhereText() {
		return whereText.getEditorPane().getText().trim();
	}
	
	public double getAliasOffset() {
		return selectionCheckbox.getWidth() + constantText.getWidth() + 2 * SPACING_SIZE;
	}
	
	public double getWhereOffset() {
		double offset = aliasText.getFullBounds().getX() + aliasText.getWidth() + SPACING_SIZE;
		logger.debug("Returning where offset of " + offset + " where position is currently " + whereText.getFullBounds().getX());
		return offset;
	}
	
	public void setAliasXPosition(double position) {
		aliasText.translate(position - aliasText.getFullBounds().getX(), 0);
		whereText.translate(position - aliasText.getFullBounds().getX(), 0);
	}
	
	public void setWhereXPosition(double position) {
		whereText.translate(position - whereText.getFullBounds().getX(), 0);
	}
	
	public void addChangeListener(PropertyChangeListener l) {
		changeListeners.add(l);
	}
	
	public void removeChangeListener(PropertyChangeListener l) {
		changeListeners.remove(l);
	}

	public void setSelected(boolean selected) {
		selectionCheckbox.setSelected(selected);
	}

}
