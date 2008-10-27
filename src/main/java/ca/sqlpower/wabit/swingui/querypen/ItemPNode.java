package ca.sqlpower.wabit.swingui.querypen;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;

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

	private final Item item;
	
	/**
	 * A text area to allow showing the name of the sql column and
	 * for editing its alias.
	 */
	private final EditablePStyledText columnText;
	
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
		}
		
		public void editingStarting() {
			editing = true;
			if (aliasText != null && aliasText.length() > 0) {
				columnText.getEditorPane().setText(aliasText);
				logger.debug("Setting editor text to " + aliasText);
			}
		}
	};

	public ItemPNode(MouseState mouseStates, PCanvas canvas, Item item) {
		this.item = item;
		aliasText = "";
		
		isInSelectCheckBox = new JCheckBox();
		isInSelectCheckBox.setSelected(true);
		PSwing swingCheckBox = new PSwing(isInSelectCheckBox);
		addChild(swingCheckBox);
		
		columnText = new EditablePStyledText(item.getName(), mouseStates, canvas);
		columnText.addEditStyledTextListener(editingTextListener);
		columnText.translate(swingCheckBox.getFullBounds().width, (swingCheckBox.getFullBounds().height - columnText.getFullBounds().height)/2);
		addChild(columnText);
		logger.debug("Pnode " + item.getName() + " created.");
	}

	public Item getItem() {
		return item;
	}

	public PStyledText getColumnText() {
		return columnText;
	}
	
	public boolean isInSelect() {
		return isInSelectCheckBox.isSelected();
	}
	
	public String getAlias() {
		return aliasText;
	}

}
