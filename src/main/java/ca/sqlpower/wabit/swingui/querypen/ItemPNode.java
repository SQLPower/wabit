package ca.sqlpower.wabit.swingui.querypen;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.swingui.Item;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.nodes.PStyledText;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * This PNode represents a SQL column on a table.
 */
public class ItemPNode extends PNode {
	
	private static final Logger logger = Logger.getLogger(ItemPNode.class);
	
	public static final String PROPERTY_ALIAS = "ALIAS";
	
	public static final String PROPERTY_SELECTED = "SELECTED";
	
	public static final String PROPERTY_WHERE = "WHERE";

	/**
	 * The amount of space to place between the column name text and
	 * the where clause.
	 */
	private static final double WHERE_BUFFER = 5;
	
	/**
	 * This text will go in the whereText field when there is
	 * no where clause on the current item.
	 */
	private static final String WHERE_START_TEXT = "        ";

	/**
	 * The item this node is displaying.
	 */
	private final Item item;
	
	/**
	 * A text area to allow showing the name of the item and
	 * for editing its alias.
	 */
	private final EditablePStyledText columnText;
	
	/**
	 * The text area storing the where clause for a given item.
	 */
	private final EditablePStyledText whereText;
	
	/**
	 * The user defined alias for this sql column.
	 */
	private String aliasText;
	
	/**
	 * Tracks if the item is in the select portion of a select
	 * statement.
	 */
	private final JCheckBox isInSelectCheckBox;
	
	private boolean isJoined = false;
	
	/**
	 * These listeners will fire a change event when an element on this object
	 * is changed that affects the resulting generated query.
	 */
	private final Collection<PropertyChangeListener> queryChangeListeners;

	/**
	 * A listener to properly display the alias and column name when the
	 * {@link EditablePStyledText} is switching from edit to non-edit mode and
	 * back. This listener for the nameEditor will show only the alias when the
	 * alias is being edited. When the alias is not being edited it will show
	 * the alias and column name, in brackets, if an alias is specified.
	 * Otherwise only the column name will be displayed.
	 */
	private EditStyledTextListener editingTextListener = new EditStyledTextListener() {
		/**
		 * Tracks if we are in an editing state or not. Used to keep the
		 * editingStopped method from running only once per stop edit (some
		 * cases the editingStopped can be called from multiple places on the
		 * same stopEditing).
		 */
		private boolean editing = false;
		
		public void editingStopping() {
			String oldAlias = aliasText;
			if (editing) {
				JEditorPane nameEditor = columnText.getEditorPane();
				aliasText = nameEditor.getText();
				if (nameEditor.getText() != null && nameEditor.getText().length() > 0 && !nameEditor.getText().equals(item.getName())) {
					nameEditor.setText(aliasText + " (" + item.getName() + ")");
				} else {
					logger.debug("item name is " + item.getName());
					nameEditor.setText(item.getName());
					aliasText = "";
				}
				logger.debug("editor has text " + nameEditor.getText() + " alias is " + aliasText);
				columnText.syncWithDocument();
			}
			if(isJoined) {
				highLightText();
			}
			editing = false;
			if (!aliasText.equals(oldAlias)) {
				for (PropertyChangeListener l : queryChangeListeners) {
					l.propertyChange(new PropertyChangeEvent(ItemPNode.this, PROPERTY_ALIAS, oldAlias, aliasText));
				}
			}
		}
		
		public void editingStarting() {
			editing = true;
			if (aliasText != null && aliasText.length() > 0) {
				columnText.getEditorPane().setText(aliasText);
				logger.debug("Setting editor text to " + aliasText);
			}
		}
	};
	
	private EditStyledTextListener whereTextListener = new EditStyledTextListener() {
		private String oldWhere;
		public void editingStopping() {
			if (whereText.getEditorPane().getText() == null || whereText.getEditorPane().getText().length() == 0) {
				whereText.getEditorPane().setText(WHERE_START_TEXT);
				whereText.syncWithDocument();
			}
			if (!getWhereText().equals(oldWhere)) {
				for (PropertyChangeListener l : queryChangeListeners) {
					l.propertyChange(new PropertyChangeEvent(ItemPNode.this, PROPERTY_WHERE, oldWhere, getWhereText()));
				}
			}
		}
		public void editingStarting() {
			oldWhere = getWhereText();
			if (whereText.getEditorPane().getText().equals(WHERE_START_TEXT)) {
				whereText.getEditorPane().setText("");
			}
		}
	};
	
	/**
	 * HighLights the columnText. This will be called when the item is joined or Deleted.
	 */
	public void highLightText() {
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		if(isJoined) {
			StyleConstants.setForeground(attributeSet, Color.blue);
		} else {
			StyleConstants.setForeground(attributeSet, Color.black);
		}
		DefaultStyledDocument leftDoc = (DefaultStyledDocument)columnText.getDocument();
		leftDoc.setCharacterAttributes(0, leftDoc.getLength(), attributeSet, false);
		columnText.repaint();
		columnText.syncWithDocument();
		
	}

	/**
	 * The check box for selection wrapped as a PSwing 
	 * object.
	 */
	private PSwing swingCheckBox;

	public ItemPNode(MouseState mouseStates, PCanvas canvas, Item item) {
		this.item = item;
		aliasText = "";
		queryChangeListeners = new ArrayList<PropertyChangeListener>();
		
		isInSelectCheckBox = new JCheckBox();
		isInSelectCheckBox.setSelected(true);
		swingCheckBox = new PSwing(isInSelectCheckBox);
		addChild(swingCheckBox);
		
		isInSelectCheckBox.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				for (PropertyChangeListener l : queryChangeListeners) {
					l.propertyChange(new PropertyChangeEvent(ItemPNode.this, PROPERTY_SELECTED, !isInSelectCheckBox.isSelected(), isInSelectCheckBox.isSelected()));
				}
			}
		});
		
		columnText = new EditablePStyledText(item.getName(), mouseStates, canvas);
		columnText.addEditStyledTextListener(editingTextListener);
		double textYTranslation = (swingCheckBox.getFullBounds().height - columnText.getFullBounds().height)/2;
		columnText.translate(swingCheckBox.getFullBounds().width, textYTranslation);
		addChild(columnText);
		
		whereText = new EditablePStyledText(WHERE_START_TEXT, mouseStates, canvas);
		whereText.addEditStyledTextListener(whereTextListener);
		whereText.translate(0, textYTranslation);
		addChild(whereText);
		
		logger.debug("Pnode " + item.getName() + " created.");
	}

	public void setIsJoined(boolean joined) {
		isJoined = joined;
		highLightText();
	}
	 
	public Item getItem() {
		return item;
	}

	public PStyledText getItemText() {
		return columnText;
	}
	
	public PStyledText getWherePStyledText() {
		return whereText;
	}
	
	public String getWhereText() {
		String text = whereText.getEditorPane().getText();
		if (text.equals(WHERE_START_TEXT)) {
			return "";
		} else {
			return text;
		}
	}
	
	public void setInSelected(boolean selected) {
		isInSelectCheckBox.setSelected(selected);
		for (PropertyChangeListener l : queryChangeListeners) {
			l.propertyChange(new PropertyChangeEvent(ItemPNode.this, PROPERTY_SELECTED, !isInSelectCheckBox.isSelected(), isInSelectCheckBox.isSelected()));
		}
	}
	
	public boolean isInSelect() {
		return isInSelectCheckBox.isSelected();
	}
	
	public String getAlias() {
		return aliasText;
	}
	
	public void setAlias(String newAlias) {
		aliasText = newAlias;
	}
	
	public void addQueryChangeListener(PropertyChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeListener(PropertyChangeListener l) {
		queryChangeListeners.remove(l);
	}
	
	public double getDistanceForWhere() {
		return swingCheckBox.getFullBounds().width + columnText.getWidth() + WHERE_BUFFER;
	}
	
	public void positionWhere(double xpos) {
		logger.debug("Moving where text: xpos = " + xpos + ", text x position = " + whereText.getFullBounds().getX() + " x offset " + whereText.getXOffset());
		whereText.translate(xpos - whereText.getXOffset(), 0);
	}

}
