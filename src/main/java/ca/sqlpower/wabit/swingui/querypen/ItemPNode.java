package ca.sqlpower.wabit.swingui.querypen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolox.nodes.PStyledText;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 * This PNode represents a SQL column on a table.
 */
public class ItemPNode extends PNode {
	
	private static final Logger logger = Logger.getLogger(ItemPNode.class);

	/**
	 * The amount of space to place between the column name text and
	 * the where clause.
	 */
	private static final double WHERE_BUFFER = 5;
	
	/**
	 * This text will go in the whereText field when there is
	 * no where clause on the current item.
	 */
	private static final String WHERE_START_TEXT = "WHERE...";

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
	
	/**
	 * These listeners will fire a change event when an element on this object
	 * is changed that affects the resulting generated query.
	 */
	private final Collection<ChangeListener> queryChangeListeners;

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
			if (editing) {
				JEditorPane nameEditor = columnText.getEditorPane();
				aliasText = nameEditor.getText();
				if (nameEditor.getText() != null && nameEditor.getText().length() > 0 && !nameEditor.getText().equals(item.getName())) {
					nameEditor.setText(aliasText + " (" + item.getName() + ")");
				} else {
					logger.debug("item name is " + item.getName());
					nameEditor.setText(item.getName());
				}
				logger.debug("editor has text " + nameEditor.getText() + " alias is " + aliasText);
				columnText.syncWithDocument();
			}
			editing = false;
			for (ChangeListener l : queryChangeListeners) {
				l.stateChanged(new ChangeEvent(ItemPNode.this));
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
		public void editingStopping() {
			if (whereText.getEditorPane().getText() == null || whereText.getEditorPane().getText().length() == 0) {
				whereText.getEditorPane().setText(WHERE_START_TEXT);
				whereText.syncWithDocument();
			}
			for (ChangeListener l : queryChangeListeners) {
				l.stateChanged(new ChangeEvent(ItemPNode.this));
			}
		}
		public void editingStarting() {
			if (whereText.getEditorPane().getText().equals(WHERE_START_TEXT)) {
				whereText.getEditorPane().setText("");
			}
		}
	};

	/**
	 * The check box for selection wrapped as a PSwing 
	 * object.
	 */
	private PSwing swingCheckBox;

	public ItemPNode(MouseState mouseStates, PCanvas canvas, Item item) {
		this.item = item;
		aliasText = "";
		queryChangeListeners = new ArrayList<ChangeListener>();
		
		isInSelectCheckBox = new JCheckBox();
		isInSelectCheckBox.setSelected(true);
		swingCheckBox = new PSwing(isInSelectCheckBox);
		addChild(swingCheckBox);
		
		isInSelectCheckBox.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				for (ChangeListener l : queryChangeListeners) {
					l.stateChanged(new ChangeEvent(ItemPNode.this));
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
	
	public boolean isInSelect() {
		return isInSelectCheckBox.isSelected();
	}
	
	public String getAlias() {
		return aliasText;
	}
	
	public void addQueryChangeListener(ChangeListener l) {
		queryChangeListeners.add(l);
	}
	
	public void removeQueryChangeListener(ChangeListener l) {
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
